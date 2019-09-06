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
import java.util.Optional;
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
      Category category1 = generateCategory(Constants.CONTEXT_PREFIX, context.getPreferredName(), Optional.empty(),
          Optional.empty(), Constants.ROOT_CATEGORY_KEY, context.getVersion().toString());
      categories.add(category1);

      // Level 2
      for (ClassificationScheme cs : context.getClassificationScheme()) {
        Category category2 = generateCategory(Constants.CLASSIFICATION_SCHEME_PREFIX, cs.getPreferredName(),
            Optional.of(cs.getPublicId().toString()), Optional.of(cs.getLongName()), category1.getUniqueId(),
            cs.getVersion().toString());
        categories.add(category2);

        // Levels 3 and beyond
        for (CSI csi : cs.getCSI()) {
          categories.addAll(classificationSchemeItemToCategories(csi, category2.getUniqueId(), new ArrayList<>()));
        }
      }
    }
    return categories;
  }

  private static List<Category> classificationSchemeItemToCategories(CSI csi, String parentId, List<Category> categories) {

    Category category = generateCategory(Constants.CLASSIFICATION_SCHEME_ITEM_PREFIX, csi.getClassificationSchemeItemName(),
        Optional.of(csi.getPublicId().toString()), Optional.empty(), parentId, csi.getVersion().toString());

    categories.add(category);

    for (CSI csiChildren : csi.getCSI()) {
      classificationSchemeItemToCategories(csiChildren, category.getUniqueId(), categories);
    }

    return categories;
  }

  private static Category generateCategory(String prefix, String name, Optional<String> publicId, Optional<String> longName, String parentId, String version) {
    String id = generateCategoryId(prefix, name, publicId, version);
    String uniqueId = UUID.randomUUID().toString();
    String description = generateCategoryDescription(longName.isPresent() ? longName.get() : name);
    return new Category(id, uniqueId, name, description, parentId);
  }

  private static List<CategoryTreeNode> categoriesListToTree(List<Category> categories) {
    return getChildrenNodes(Constants.ROOT_CATEGORY_KEY, categories);
  }

  private static List<CategoryTreeNode> getChildrenNodes(String parentId, List<Category> categories) {
    List<CategoryTreeNode> childrenNodes = new ArrayList<>();
    for (Category category : categories) {
      if (category.getParentId().equals(parentId)) {
        CategoryTreeNode node = new CategoryTreeNode(category.getId(), category.getName(), category.getDescription(),
            getChildrenNodes(category.getUniqueId(), categories));
        childrenNodes.add(node);
      }
    }
    return childrenNodes;
  }

  private static String generateCategoryId(String prefix, String  name, Optional<String> publicId, String version) {

    return prefix + "-" + (publicId.isPresent() ? publicId.get() : name) + "v" + version;
  }

  private static String generateCategoryDescription(String name) {
    return name + " category";
  }

}
