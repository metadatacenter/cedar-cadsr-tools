package org.metadatacenter.cadsr.ingestor.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.metadatacenter.cadsr.category.schema.CSI;
import org.metadatacenter.cadsr.category.schema.ClassificationScheme;
import org.metadatacenter.cadsr.category.schema.Classifications;
import org.metadatacenter.cadsr.category.schema.Context;
import org.metadatacenter.cadsr.cde.schema.DataElement;
import org.metadatacenter.cadsr.cde.schema.PermissibleValuesITEM;
import org.metadatacenter.cadsr.ingestor.category.Category;
import org.metadatacenter.cadsr.ingestor.category.CategorySummary;
import org.metadatacenter.cadsr.ingestor.category.CategoryTreeNode;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarEnvironment;
import org.metadatacenter.cadsr.ingestor.category.CedarCategory;
import org.metadatacenter.model.ModelNodeNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.HttpURLConnection;
import java.text.DecimalFormat;
import java.util.*;

public class CategoryUtil {

  private static final Logger logger = LoggerFactory.getLogger(CategoryUtil.class);

  private static final DecimalFormat countFormat = new DecimalFormat("#,###,###,###");

  private static ObjectMapper objectMapper = new ObjectMapper();

  /**
   * @param inputFile
   * @param outputDir
   * @return Category Tree file
   * @throws IOException
   */
  public static File convertCdeCategoriesFromFile(File inputFile, File outputDir) throws IOException {
    logger.info("Processing input file at " + inputFile.getAbsolutePath());
    List<CategoryTreeNode> categoryTree;
    File categoryTreeFile = null;
    try {
      Classifications classifications = CategoryUtil.getClassifications(new FileInputStream(inputFile));
      categoryTree = CategoryUtil.classificationsToCategoryTree(classifications);
      logger.info("Generating categories file...");
      String categoriesFileName = inputFile.getName().substring(0, inputFile.getName().lastIndexOf('.')) + ".json";
      categoryTreeFile = new File(outputDir, categoriesFileName);
      new ObjectMapper().writeValue(categoryTreeFile, categoryTree);
    } catch (JAXBException e) {
      e.printStackTrace();
    }
    return categoryTreeFile;
  }

  public static void convertCdeCategoriesFromDirectory(File inputDir, File outputDir) throws IOException {
    for (final File inputFile : inputDir.listFiles()) {
      convertCdeCategoriesFromFile(inputFile, outputDir);
    }
  }

  public static void uploadCategoriesFromDirectory(File inputDir, String cedarRootCategoryId,
                                                   CedarEnvironment environment,
                                                   String apiKey) throws IOException {
    for (final File inputFile : inputDir.listFiles()) {
      uploadCategoriesFromFile(inputFile, cedarRootCategoryId, environment, apiKey);
    }
  }

  public static void uploadCategoriesFromFile(File inputFile, String cedarRootCategoryId, CedarEnvironment environment,
                                              String apiKey) throws IOException {

    List<CategoryTreeNode> categoryTreeNodes = readCategoriesFromFile(inputFile);
    for (CategoryTreeNode categoryTreeNode : categoryTreeNodes) {
      CedarServices.createCategory(categoryTreeNode, cedarRootCategoryId, environment, apiKey);
    }
  }

  private static List<CategoryTreeNode> readCategoriesFromFile(File inputFile) throws IOException {
    logger.info("Processing input file at " + inputFile.getAbsolutePath());
    List<CategoryTreeNode> categories =
        objectMapper.readValue(inputFile, objectMapper.getTypeFactory().constructCollectionType(List.class,
            CategoryTreeNode.class));
    return categories;
  }

  public static Classifications getClassifications(InputStream is) throws JAXBException,
      IOException {
    // Note that CLASSIFICATIONSLISTCATEGORIES is a class name that we created to diferentiate the categories extracted
    // from the categories file that the NCI sent us from the CLASSIFICATIONSLIST elements extracted from the original
    // caDSR CDEs XML file
    JAXBContext jaxbContext = JAXBContext.newInstance(Classifications.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    InputStream cleanIs = GeneralUtil.processInvalidXMLCharacters(is);
    return (Classifications) jaxbUnmarshaller.unmarshal(new InputStreamReader(cleanIs, Constants.CHARSET));
  }

  public static List<CategoryTreeNode> classificationsToCategoryTree(Classifications classifications) {
    List<Category> categories = classificationsToCategoriesList(classifications);
    List<CategoryTreeNode> categoryTreeNodes = categoriesListToTree(categories);
    return categoryTreeNodes;
  }

  public static List<Category> classificationsToCategoriesList(Classifications classifications) {

    List<Category> categories = new ArrayList<>();

    for (Context context : classifications.getContext()) {

      // Level 1 (root categories)
      Category ctxCategory = generateCategory(context.getPreferredName(), Optional.empty(), Optional.empty(),
          Optional.empty(),
          Optional.empty(), Optional.empty(), context.getVersion().toString(), Constants.ROOT_CATEGORY_KEY);
      //categories.add(ctxCategory);
      addCategoryToList(ctxCategory, categories);

      // Level 2
      for (ClassificationScheme cs : context.getClassificationScheme()) {
        Category csCategory = generateCategory(cs.getPreferredName(), Optional.of(ctxCategory.getLocalId()),
            Optional.empty(),
            Optional.of(cs.getPublicId().toString()), Optional.of(cs.getLongName()), Optional.empty(),
            cs.getVersion().toString(), ctxCategory.getUniqueId());
        //categories.add(csCategory);
        addCategoryToList(csCategory, categories);

        // Levels 3 and beyond
        for (CSI csi : cs.getCSI()) {
          categories.addAll(classificationSchemeItemToCategories(csi, Optional.of(ctxCategory.getLocalId()),
              Optional.of(csCategory.getLocalId()), csCategory.getUniqueId(), new ArrayList<>()));
        }
      }
    }
    return categories;
  }

  public static List<Category> classificationSchemeItemToCategories(CSI csi, Optional<String> ctxLocalId,
                                                                    Optional<String> csLocalId, String parentUniqueId
      , List<Category> categories) {

    Category category = generateCategory(csi.getClassificationSchemeItemName(), ctxLocalId, csLocalId,
        Optional.of(csi.getPublicId().toString()), Optional.empty(), Optional.of(csi.getClassificationSchemeItemType()),
        csi.getVersion().toString(), parentUniqueId);

    //categories.add(category);
    addCategoryToList(category, categories);

    for (CSI csiChildren : csi.getCSI()) {
      classificationSchemeItemToCategories(csiChildren, ctxLocalId, csLocalId, category.getUniqueId(), categories);
    }

    return categories;
  }

  /**
   * Category names should be unique at the same level. In order to ensure that, we add a suffix for categories that
   * have the same name at the same level
   *
   * @param newCategory
   * @param categories
   * @return
   */
  public static List<Category> addCategoryToList(Category newCategory, List<Category> categories) {

    List<Integer> positionsSameCategory = new ArrayList<>();

    for (int i = 0; i < categories.size(); i++) {
      Category category = categories.get(i);
      if (newCategory.getParentUniqueId().equals(category.getParentUniqueId()) && newCategory.getName().equals(category.getName())) {
        positionsSameCategory.add(i);
      }
    }

    for (int position : positionsSameCategory) {
      Category existingCategory = categories.get(position);
      categories.remove(position);
      existingCategory.setName(existingCategory.getName() + " " + existingCategory.getVersion());
      categories.add(existingCategory);
    }

    if (!positionsSameCategory.isEmpty()) {
      newCategory.setName(newCategory.getName() + " " + newCategory.getVersion());
    }

    categories.add(newCategory);

    return categories;

  }


  private static Category generateCategory(String name, Optional<String> ctxLocalId, Optional<String> csLocalId,
                                           Optional<String> publicId,
                                           Optional<String> longName, Optional<String> type, String version,
                                           String parentUniqueId) {

    String categoryPublicId = publicId.isPresent() ? publicId.get() : null;
    String localId = generateCategoryLocalId(name, type, publicId, version);
    String cadsrId = generateCadsrCategoryId(localId, ctxLocalId, csLocalId);
    String uniqueId = generateCategoryUniqueId(name, type, publicId, version, parentUniqueId);
    String categoryName = longName.isPresent() ? longName.get().trim() : name.trim();
    String description = longName.isPresent() ? longName.get().trim() : name.trim();
    String categoryType = type.isPresent() ? type.get() : null;
    return new Category(categoryPublicId, version, localId, cadsrId, uniqueId, categoryName, description,
        categoryType, parentUniqueId);
  }

  public static List<CategoryTreeNode> categoriesListToTree(List<Category> categories) {
    return getChildrenNodes(Constants.ROOT_CATEGORY_KEY, categories);
  }

  public static List<CategoryTreeNode> getChildrenNodes(String parentUniqueId, List<Category> categories) {
    List<CategoryTreeNode> childrenNodes = new ArrayList<>();
    for (Category category : categories) {
      if (category.getParentUniqueId().equals(parentUniqueId)) {
        CategoryTreeNode node = new CategoryTreeNode(category.getUniqueId(), category.getPublicId(),
            category.getName(), category.getDescription(),
            getChildrenNodes(category.getUniqueId(), categories), category.getParentUniqueId(), category.getVersion());
        childrenNodes.add(node);
      }
    }
    return childrenNodes;
  }

  public static String generateCategoryLocalId(String name, Optional<String> type, Optional<String> publicId,
                                               String version) {
    final String separator = "_";
    // Format: type_name_id_version
//    String categoryId = (name + separator + (type.isPresent() ? (type.get() + separator) : "")
//        + (publicId.isPresent() ? (publicId.get() + separator) : "") + "v" + version);
    // If publicId is not available (it happens for the first level of categories in the tree), we use the name
    String categoryId = (publicId.isPresent() ? publicId.get() : name) + separator + "V" + version;
    return categoryId.replaceAll("\\s", "").replaceAll("/", ""); // Remove spaces and slashes
  }

  public static String generateCadsrCategoryId(String categoryLocalId, Optional<String> ctxLocalId,
                                               Optional<String> csLocalId) {


    if (ctxLocalId.isPresent()) {
      if (csLocalId.isPresent()) {
        return ctxLocalId.get() + "/" + csLocalId.get() + "/" + categoryLocalId;
      } else {
        return ctxLocalId.get() + "/" + categoryLocalId;
      }
    } else {
      return categoryLocalId;
    }
  }

  public static String generateCategoryUniqueId(String name, Optional<String> type, Optional<String> publicId,
                                                String version, String parentCategoryUniqueId) {

    String categoryId = generateCategoryLocalId(name, type, publicId, version);

    if (parentCategoryUniqueId.equals(Constants.ROOT_CATEGORY_KEY)) {
      return categoryId;
    } else {
      return parentCategoryUniqueId + "/" + categoryId;
    }
  }

  public static Map<String, String> getCategoryIdsFromCategoryTree(JsonNode cedarCategoryTree) {
    return getCategoryIds(cedarCategoryTree, new HashMap<>());
  }

  // Generates a map of categoryId to cedarCategoryId
  private static Map<String, String> getCategoryIds(JsonNode category, Map<String, String> categoryIds) {

    if (category.hasNonNull(ModelNodeNames.JSON_LD_ID) && category.hasNonNull(ModelNodeNames.SCHEMA_ORG_IDENTIFIER)) {
      categoryIds.put(category.get(ModelNodeNames.SCHEMA_ORG_IDENTIFIER).asText(),
          category.get(ModelNodeNames.JSON_LD_ID).asText());
    }

    if (category.hasNonNull(Constants.CEDAR_CATEGORY_CHILDREN_FIELD_NAME)) {
      JsonNode children = category.get(Constants.CEDAR_CATEGORY_CHILDREN_FIELD_NAME);
      if (children.isArray()) {
        for (JsonNode childCategory : children) {
          getCategoryIds(childCategory, categoryIds);
        }
      }
    }

    return categoryIds;
  }

  /**
   * Generates a hash code based on the attributes of a category. This hash code is used to identify changes in the
   * category and decide if it should be updated in CEDAR. Given that the goal is to keep categories updated in
   * CEDAR, this function needs to focus only on the attributes that CEDAR stores for categories.
   */
  public static String generateCategoryHashCode(Category category) {
    return generateCategoryHashCode(category.toCedarCategory());
  }

  public static String generateCategoryHashCode(CedarCategory cedarCategory) {
    return GeneralUtil.getSha1(cedarCategory.getId() + cedarCategory.getName() + cedarCategory.getDescription());
  }

  public static Map<String, CategorySummary> generateExistingCategoriesMap(CedarEnvironment environment,
                                                                           String apiKey) throws IOException {
    Map<String, CategorySummary> existingCategoriesMap = new HashMap<>();
    CedarCategory currentCedarCategoryTree = CedarServices.getCedarCategoryTree(environment, apiKey);
    return addCategoriesToMap(currentCedarCategoryTree.getChildren(), existingCategoriesMap, null);
  }

  private static Map<String, CategorySummary> addCategoriesToMap(List<CedarCategory> categories, Map<String,
      CategorySummary> categoriesMap, String parentId) {
    for (CedarCategory category : categories) {
      if (!categoriesMap.containsKey(category.getId())) {
        logger.info(category.getId());
        String categoryHash = CategoryUtil.generateCategoryHashCode(category);
        categoriesMap.put(category.getId(), new CategorySummary(category.getCedarId(), category.getParentCategoryCedarId(), categoryHash));
        addCategoriesToMap(category.getChildren(), categoriesMap, category.getId());
      } else {
        throw new InternalError("Category already in map: " + category.getId());
      }
    }
    return categoriesMap;
  }

}
