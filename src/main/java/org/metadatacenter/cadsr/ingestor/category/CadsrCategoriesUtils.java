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
      String id1 = generateCategoryId("CTX", name1, context.getVersion().toString());
      String description1 = generateCategoryDescription(name1);
      List<String> path1 = new ArrayList<>();
      path1.add(Constants.ROOT_CATEGORY_KEY);
      path1.add(id1);
      Category category1 = new Category(id1, name1, description1, path1);
      if (!categories.contains(category1)) {
        categories.add(category1);
      }

      // Level 2
      for (ClassificationScheme cs : context.getClassificationScheme()) {
        String name2 = cs.getPreferredName();
        String id2 = generateCategoryId("CS", cs.getPublicId().toString(), cs.getVersion().toString());
        String description2 = generateCategoryDescription(cs.getLongName());
        List<String> path2 = new ArrayList<>();
        path2.addAll(path1);
        path2.add(id2);
        Category category2 = new Category(id2, name2, description2, path2);
        if (!categories.contains(category2)) {
          categories.add(category2);
        }

        // Levels 3 and beyond
        for (CSI csi : cs.getCSI()) {
          categories.addAll(classificationSchemeItemToCategories(csi, path2, new ArrayList<>()));
        }
      }
    }
    return categories;
  }

  private static List<Category> classificationSchemeItemToCategories(CSI csi, List<String> parentPath,
                                                                     List<Category> categories) {

    String name = csi.getClassificationSchemeItemName();
    String id = generateCategoryId("CSI", csi.getPublicId().toString(), csi.getVersion().toString());
    String description = generateCategoryDescription(name);
    List<String> path = new ArrayList<>();
    path.addAll(parentPath);
    path.add(id);
    Category category = new Category(id, name, description, path);
    categories.add(category);

    for (CSI csiChildren : csi.getCSI()) {
      classificationSchemeItemToCategories(csiChildren, path, categories);
    }

    return categories;
  }

  private static String generateCategoryId(String prefix, String name, String version) {
    return prefix + "-" + name + "v" + version;
  }

  private static String generateCategoryDescription(String name) {
    return name + " category";
  }

  private static List<CategoryTreeNode> categoriesListToTree(List<Category> categories) {

    List<String> rootPath = new ArrayList<>();
    rootPath.add(Constants.ROOT_CATEGORY_KEY);
    List<CategoryTreeNode> nodes = getChildrenNodes(rootPath, categories);
    return nodes;

  }

  private static List<CategoryTreeNode> getChildrenNodes(List<String> categoryPath, List<Category> categories) {

    List<CategoryTreeNode> childrenNodes = new ArrayList<>();

    for (Category category : categories) {

      List<String> parentPath = category.getParentPath();
      if (category.getParentPath().equals(categoryPath)) {

        CategoryTreeNode node = new CategoryTreeNode(category.getId(), category.getName(), category.getDescription(),
            getChildrenNodes(category.getPath(), categories));

        childrenNodes.add(node);
      }
    }
    return childrenNodes;

  }

}
