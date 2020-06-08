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

import static org.metadatacenter.cadsr.ingestor.util.Constants.CADSR_CATEGORY_SCHEMA_ORG_ID;
import static org.metadatacenter.cadsr.ingestor.util.Constants.CDE_CATEGORY_IDS_FIELD;
import static org.metadatacenter.model.ModelNodeNames.JSON_LD_ID;

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
      categoryTree = CategoryUtil.classificationsToCdeCategoryTree(classifications);
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

  public static List<CategoryTreeNode> classificationsToCdeCategoryTree(Classifications classifications) {
    List<Category> categories = classificationsToCategoriesList(classifications);
    List<CategoryTreeNode> categoryTreeNodes = cdeCategoriesListToTree(categories);
    return categoryTreeNodes;
  }

  public static List<Category> classificationsToCategoriesList(Classifications classifications) {

    List<Category> categories = new ArrayList<>();

    for (Context context : classifications.getContext()) {

      // Level 1 (root categories)
      Category ctxCategory = generateCategory(context.getPreferredName(), Optional.empty(), Optional.empty(),
          Optional.empty(),
          Optional.empty(), Optional.empty(), context.getVersion().toString(), CADSR_CATEGORY_SCHEMA_ORG_ID);
      addCategoryToList(ctxCategory, categories);

      // Level 2
      for (ClassificationScheme cs : context.getClassificationScheme()) {
        Category csCategory = generateCategory(cs.getPreferredName(), Optional.of(ctxCategory.getLocalId()),
            Optional.empty(),
            Optional.of(cs.getPublicId().toString()), Optional.of(cs.getLongName()), Optional.empty(),
            cs.getVersion().toString(), ctxCategory.getUniqueId());
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

    List<Integer> positionOfCategoriesWithSameName = new ArrayList<>();

    for (int i = 0; i < categories.size(); i++) {
      Category category = categories.get(i);
      if (newCategory.getParentUniqueId().equals(category.getParentUniqueId()) && newCategory.getName().equals(category.getName())) {
        logger.warn("Found two categories with the same name at the same level! Name: " + category.getName() + ". Category Ids: " + category.getUniqueId() + "; " + newCategory.getUniqueId());
        positionOfCategoriesWithSameName.add(i);
      }
    }

    // Update names of categories with the same name than newCategory
    for (int position : positionOfCategoriesWithSameName) {
      Category existingCategory = categories.get(position);
      categories.remove(position);
      String existingCategoryNewName = existingCategory.getName() + " " + existingCategory.getVersion();
      logger.info("Updating category name to avoid confusion: " + existingCategory.getName() + " (" + existingCategory.getUniqueId() + ")" + " -> " + existingCategoryNewName);
      existingCategory.setName(existingCategoryNewName);
      categories.add(existingCategory);
    }

    // Update name of newCategory
    if (!positionOfCategoriesWithSameName.isEmpty()) {
      String newName = newCategory.getName() + " " + newCategory.getVersion();
      logger.info("Updating category name to avoid confusion: " + newCategory.getName() + " (" + newCategory.getUniqueId() + ")" + " -> " + newName);
      newCategory.setName(newName);
    }

    categories.add(newCategory);

    return categories;
  }


  private static Category generateCategory(String name, Optional<String> ctxLocalId, Optional<String> csLocalId,
                                           Optional<String> publicId,
                                           Optional<String> longName, Optional<String> type, String version,
                                           String parentUniqueId) {

    String categoryPublicId = publicId.isPresent() ? publicId.get() : null;
    String localId = generateCategoryLocalId(name, publicId, version);
    String cadsrId = generateCadsrCategoryId(localId, ctxLocalId, csLocalId);
    String uniqueId = generateCdeCategoryUniqueId(name, publicId, version, parentUniqueId);
    String categoryName = longName.isPresent() ? longName.get().trim() : name.trim();
    String description = longName.isPresent() ? longName.get().trim() : name.trim();
    String categoryType = type.isPresent() ? type.get() : null;

    return new Category(categoryPublicId, version, localId, cadsrId, uniqueId, categoryName, description,
        categoryType, parentUniqueId);
  }

  public static List<CategoryTreeNode> cdeCategoriesListToTree(List<Category> categories) {
    return getChildrenNodes(CADSR_CATEGORY_SCHEMA_ORG_ID, categories);
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

  public static String generateCategoryLocalId(String name, Optional<String> publicId, String version) {
    final String separator = "_";
    String categoryId = (publicId.isPresent() ? publicId.get() : name) + separator + "V" + version;
    return categoryId.replaceAll("\\s", "").replaceAll("/", ""); // Remove spaces and slashes
  }

  public static String generateCadsrCategoryId(String categoryLocalId, Optional<String> ctxLocalId,
                                               Optional<String> csLocalId) {
    String categoryId = CADSR_CATEGORY_SCHEMA_ORG_ID;
    if (ctxLocalId.isPresent()) {
      if (csLocalId.isPresent()) {
        return categoryId + "/" + ctxLocalId.get() + "/" + csLocalId.get() + "/" + categoryLocalId;
      } else {
        return categoryId + "/" + ctxLocalId.get() + "/" + categoryLocalId;
      }
    } else {
      return categoryId + "/" + categoryLocalId;
    }
  }

//  public static String generateCategoryUniqueId(String name, Optional<String> type, Optional<String> publicId,
//                                                String version, String parentCategoryUniqueId) {
//
//    String categoryId = generateCategoryLocalId(name, type, publicId, version);
//
//    if (parentCategoryUniqueId.equals(Constants.ROOT_CATEGORY_KEY)) {
//      return categoryId;
//    } else {
//      return parentCategoryUniqueId + "/" + categoryId;
//    }
//  }

  public static String generateCdeCategoryUniqueId(String name, Optional<String> publicId,
                                                   String version, String parentCategoryUniqueId) {

    String categoryId = generateCategoryLocalId(name, publicId, version);
    return parentCategoryUniqueId + "/" + categoryId;

//    if (parentCategoryUniqueId.equals(CADSR_CATEGORY_SCHEMA_ORG_ID)) {
//      return categoryId;
//    } else {
//      return parentCategoryUniqueId + "/" + categoryId;
//    }
  }

  public static Map<String, String> getCategoryIdsFromCategoryTree(JsonNode cedarCategoryTree) {
    return getCategoryIds(cedarCategoryTree, new HashMap<>());
  }

  // Generates a map of categoryId to cedarCategoryId
  private static Map<String, String> getCategoryIds(JsonNode category, Map<String, String> categoryIds) {

    if (category.hasNonNull(JSON_LD_ID) && category.hasNonNull(ModelNodeNames.SCHEMA_ORG_IDENTIFIER)) {
      categoryIds.put(category.get(ModelNodeNames.SCHEMA_ORG_IDENTIFIER).asText(),
          category.get(JSON_LD_ID).asText());
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
    return addCdeCategoriesToMap(currentCedarCategoryTree.getChildren(), existingCategoriesMap);
  }

  public static Map<String, CategorySummary> extractCdeCategoriesMap(Map<String, CategorySummary> allCategories) {
    Map<String, CategorySummary> cdeCategories = new HashMap<>();
    for (String categoryUniqueId : allCategories.keySet()) {
      // Ignore the root NCI caDSR category
      if (categoryUniqueId.startsWith(CADSR_CATEGORY_SCHEMA_ORG_ID) &&
          !categoryUniqueId.equals(CADSR_CATEGORY_SCHEMA_ORG_ID)) {
        cdeCategories.put(categoryUniqueId, allCategories.get(categoryUniqueId));
      }
    }
    return cdeCategories;
  }

  /**
   * Reads a list of CedarCategory objects and stores them into a map (key: category unique id; value: CategorySummary)
   * @param categories
   * @param categoriesMap
   * @return Categories map
   */
  private static Map<String, CategorySummary> addCdeCategoriesToMap(List<CedarCategory> categories, Map<String,
      CategorySummary> categoriesMap) {
    for (CedarCategory category : categories) {
      if (!categoriesMap.containsKey(category.getId())) {
        String categoryHash = CategoryUtil.generateCategoryHashCode(category);
        categoriesMap.put(category.getId(), new CategorySummary(category.getCedarId(),
            category.getParentCategoryCedarId(), categoryHash));
        addCdeCategoriesToMap(category.getChildren(), categoriesMap);
      } else {
        throw new InternalError("Category already in map: " + category.getId());
      }
    }
    return categoriesMap;
  }

  public static List<String> extractCategoryIdsFromCdeField(DataElement cde) {
    Map<String, Object> cdeFieldMap = CdeUtil.getFieldMapFromDataElement(cde);
    return extractCategoryIdsFromCdeField(cdeFieldMap);
  }

  public static List<String> extractCategoryIdsFromCdeField(Map<String, Object> cdeFieldMap) {
    if (cdeFieldMap.containsKey(CDE_CATEGORY_IDS_FIELD)) {
      return (List) cdeFieldMap.get(CDE_CATEGORY_IDS_FIELD);
    } else {
      logger.warn("No categories found for CDE: " + cdeFieldMap.get(JSON_LD_ID));
      return new ArrayList<>();
    }
  }

  public static List<String> extractCategoryCedarIdsFromCdeField(DataElement cde,
                                                                 Map<String, String> categoryIdsToCategoryCedarIds) {
    Map<String, Object> cdeFieldMap = CdeUtil.getFieldMapFromDataElement(cde);
    return extractCategoryCedarIdsFromCdeField(cdeFieldMap, categoryIdsToCategoryCedarIds);
  }

  public static List<String> extractCategoryCedarIdsFromCdeField(Map<String, Object> cdeFieldMap,
                                                                 Map<String, String> categoryIdsToCategoryCedarIds) {
    List<String> categoryIds = new ArrayList<>();
    if (cdeFieldMap.containsKey(CDE_CATEGORY_IDS_FIELD)) {
      categoryIds = (List) cdeFieldMap.get(CDE_CATEGORY_IDS_FIELD);
      cdeFieldMap.remove(CDE_CATEGORY_IDS_FIELD);
    } else {
      logger.warn("No categories found for CDE: " + cdeFieldMap.get(JSON_LD_ID));
    }
    List<String> categoryCedarIds = new ArrayList<>();
    for (String categoryId : categoryIds) {
      if (categoryIdsToCategoryCedarIds.containsKey(categoryId)) {
        categoryCedarIds.add(categoryIdsToCategoryCedarIds.get(categoryId));
      } else {
        logger.error("Could not find CEDAR Category Id for Category Id: " + categoryId);
      }
    }
    return categoryCedarIds;
  }
}
