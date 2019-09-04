package org.metadatacenter.cadsr.ingestor.category;

import org.metadatacenter.cadsr.category.schema.CSI;
import org.metadatacenter.cadsr.category.schema.ClassificationScheme;
import org.metadatacenter.cadsr.category.schema.Classifications;
import org.metadatacenter.cadsr.category.schema.Context;
import org.metadatacenter.cadsr.ingestor.Constants;
import org.metadatacenter.cadsr.ingestor.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CadsrCategoriesUtils {

  private static final Logger logger = LoggerFactory.getLogger(CadsrCategoriesUtils.class);

  public static Classifications getClassifications(InputStream is) throws JAXBException,
      IOException {
    // Note that CLASSIFICATIONSLISTCATEGORIES is a class name that we created to diferentiate the categories extracted
    // from the categories file that the NCI sent us from the CLASSIFICATIONSLIST elements extracted from the original
    // caDSR CDEs XML file
    JAXBContext jaxbContext = JAXBContext.newInstance(Classifications.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    InputStream cleanIs = Util.processInvalidXMLCharacters(is);
    return (Classifications) jaxbUnmarshaller.unmarshal(new InputStreamReader(cleanIs, Constants.CHARSET));
  }

  public static List<CategoryTreeNode> classificationsToCategoryTree(Classifications classifications) {
    List<Category> categories = classificationsToCategoriesList(classifications);
    List<CategoryTreeNode> categoryTreeNodes = categoriesListToTree(categories);
    return categoryTreeNodes;
  }

  private static List<Category> classificationsToCategoriesList(Classifications classifications) {

    List<Category> categories = new ArrayList<>();

    for (Context context : classifications.getContext()) {

      // Level 1 (root categories)
      String name1 = context.getPreferredName();
      String id1 = UUID.randomUUID().toString();
      String description1 = generateCategoryDescription(name1);
      String parentId1 = Constants.ROOT_CATEGORY_KEY;
      Category category1 = new Category(id1, name1, description1, parentId1);
      categories.add(category1);

      // Level 2
      for (ClassificationScheme cs : context.getClassificationScheme()) {
        String name2 = cs.getPreferredName();
        String id2 = UUID.randomUUID().toString();
        String description2 = generateCategoryDescription(cs.getLongName());
        String parentId2 = id1;
        Category category2 = new Category(id2, name2, description2, parentId2);
        categories.add(category2);

        // Levels 3 and beyond
        for (CSI csi : cs.getCSI()) {
          categories.addAll(classificationSchemeItemToCategories(csi, id2, new ArrayList<>()));
        }
      }
    }
    return categories;
  }

  private static List<Category> classificationSchemeItemToCategories(CSI csi, String parentId, List<Category> categories) {

    String name = csi.getClassificationSchemeItemName();
    String id = UUID.randomUUID().toString();
    String description = generateCategoryDescription(name);
    Category category = new Category(id, name, description, parentId);
    categories.add(category);

    for (CSI csiChildren : csi.getCSI()) {
      classificationSchemeItemToCategories(csiChildren, id, categories);
    }

    return categories;
  }

  private static String generateCategoryDescription(String name) {
    return name + " category";
  }

  private static List<CategoryTreeNode> categoriesListToTree(List<Category> categories) {
    return getChildrenNodes(Constants.ROOT_CATEGORY_KEY, categories);
  }

  private static List<CategoryTreeNode> getChildrenNodes(String parentId, List<Category> categories) {
    List<CategoryTreeNode> childrenNodes = new ArrayList<>();
    for (Category category : categories) {
      if (category.getParentId().equals(parentId)) {
        CategoryTreeNode node = new CategoryTreeNode(category.getId(), category.getName(), category.getDescription(),
            getChildrenNodes(category.getId(), categories));
        childrenNodes.add(node);
      }
    }
    return childrenNodes;
  }

}
