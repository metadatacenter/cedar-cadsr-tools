package org.metadatacenter.cadsr.ingestor.Util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.metadatacenter.cadsr.category.schema.CSI;
import org.metadatacenter.cadsr.category.schema.ClassificationScheme;
import org.metadatacenter.cadsr.category.schema.Classifications;
import org.metadatacenter.cadsr.category.schema.Context;
import org.metadatacenter.cadsr.ingestor.category.Category;
import org.metadatacenter.cadsr.ingestor.category.CategoryTreeNode;
import org.metadatacenter.cadsr.ingestor.Util.Constants.CedarEnvironment;
import org.metadatacenter.cadsr.ingestor.category.CedarCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.HttpURLConnection;
import java.text.DecimalFormat;
import java.util.*;

import static org.metadatacenter.cadsr.ingestor.Util.Constants.*;

public class CategoryUtil {

  private static final Logger logger = LoggerFactory.getLogger(CategoryUtil.class);

  private static final DecimalFormat countFormat = new DecimalFormat("#,###,###,###");

  private static ObjectMapper objectMapper = new ObjectMapper();

  /**
   *
   * @param inputFile
   * @param outputDir
   * @return Category Tree file
   * @throws IOException
   */
  public static File convertCdeCategoriesFromFile(File inputFile, File outputDir) throws IOException {
    logger.info("Processing input file at " + inputFile.getAbsolutePath());
    //File outputSubDir = Util.createDirectoryBasedOnInputFileName(inputFile, outputDir);
    List<CategoryTreeNode> categoryTree;
    File categoryTreeFile = null;
    try {
      Classifications classifications = CategoryUtil.getClassifications(new FileInputStream(inputFile));
      categoryTree = CategoryUtil.classificationsToCategoryTree(classifications);
      logger.info("Generating categories file...");
      String categoriesFileName = inputFile.getName().substring(0,inputFile.getName().lastIndexOf('.')) + ".json";
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

  public static int uploadCategoriesFromDirectory(File inputDir, String cedarRootCategoryId, CedarEnvironment environment,
                                                   String apiKey) throws IOException {
    int totalCategories = 0;
    for (final File inputFile : inputDir.listFiles()) {
      totalCategories += uploadCategoriesFromFile(inputFile, cedarRootCategoryId, environment, apiKey);
    }
    return totalCategories;
  }

  public static int uploadCategoriesFromFile(File inputFile, String cedarRootCategoryId, CedarEnvironment environment,
                                             String apiKey) throws IOException {

    List<CategoryTreeNode> categoryTreeNodes = readCategoriesFromFile(inputFile);
    int counter = 0;
    for (CategoryTreeNode categoryTreeNode : categoryTreeNodes) {
      uploadCategory(categoryTreeNode, cedarRootCategoryId, environment, apiKey, counter);
    }
    return counter;
  }

  private static void uploadCategory(CategoryTreeNode category, String cedarParentCategoryId,
                                     CedarEnvironment environment, String apiKey, int counter) {

    CedarCategory cedarCategory =
        new CedarCategory(category.getCadsrId(), category.getName(), category.getDescription(), cedarParentCategoryId);

    HttpURLConnection conn = null;
    try {
      Thread.sleep(50);
      logger.info("Trying to upload: " + category.getCadsrId());
      String payload = objectMapper.writeValueAsString(cedarCategory);
      String url = CedarServerUtil.getCategoriesRestEndpoint(environment);
      conn = ConnectionUtil.createAndOpenConnection("POST", url, apiKey);
      OutputStream os = conn.getOutputStream();
      os.write(payload.getBytes());
      os.flush();
      int responseCode = conn.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_CREATED) {
        logger.error("Error creating category: " + category.getCadsrId());
        GeneralUtil.logErrorMessage(conn);
      } else {
        logger.info("Category created: " + payload);
        String response = ConnectionUtil.readResponseMessage(conn.getInputStream());
        String cedarCategoryId = JsonUtil.extractJsonFieldValue(response, "@id");
        counter++;
        //logger.info(String.format("Uploading categories (%d/%d)", counter, allCategories.size()));
        for (CategoryTreeNode categoryTreeNode : category.getChildren()) {
          uploadCategory(categoryTreeNode, cedarCategoryId, environment, apiKey, counter);
        }
      }
    } catch (JsonProcessingException e) {
      logger.error(e.toString());
    } catch (IOException e) {
      logger.error(e.toString());
    } catch (InterruptedException e) {
      logger.error(e.getMessage());
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
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

  private static List<Category> classificationsToCategoriesList(Classifications classifications) {

    List<Category> categories = new ArrayList<>();

    for (Context context : classifications.getContext()) {

      // Level 1 (root categories)
      Category ctxCategory = generateCategory(context.getPreferredName(), Optional.empty(), Optional.empty(), Optional.empty(),
          Optional.empty(), Optional.empty(), context.getVersion().toString(), Constants.ROOT_CATEGORY_KEY);
      //categories.add(ctxCategory);
      addCategoryToList(ctxCategory, categories);

      // Level 2
      for (ClassificationScheme cs : context.getClassificationScheme()) {
        Category csCategory = generateCategory(cs.getPreferredName(), Optional.of(ctxCategory.getId()), Optional.empty(),
            Optional.of(cs.getPublicId().toString()), Optional.of(cs.getLongName()), Optional.empty(),
            cs.getVersion().toString(), ctxCategory.getUniqueId());
        //categories.add(csCategory);
        addCategoryToList(csCategory, categories);

        // Levels 3 and beyond
        for (CSI csi : cs.getCSI()) {
          categories.addAll(classificationSchemeItemToCategories(csi, Optional.of(ctxCategory.getId()),
              Optional.of(csCategory.getId()), csCategory.getUniqueId(), new ArrayList<>()));
        }
      }
    }
    return categories;
  }

  public static List<Category> classificationSchemeItemToCategories(CSI csi, Optional<String> ctxId,
                                                                     Optional<String> csId, String parentUniqueId, List<Category> categories) {

    Category category = generateCategory(csi.getClassificationSchemeItemName(), ctxId, csId,
        Optional.of(csi.getPublicId().toString()), Optional.empty(), Optional.of(csi.getClassificationSchemeItemType()),
        csi.getVersion().toString(), parentUniqueId);

    //categories.add(category);
    addCategoryToList(category, categories);

    for (CSI csiChildren : csi.getCSI()) {
      classificationSchemeItemToCategories(csiChildren, ctxId, csId, category.getUniqueId(), categories);
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

    for (int i=0; i<categories.size(); i++) {
      Category category = categories.get(i);
      if (newCategory.getParentId().equals(category.getParentId()) && newCategory.getName().equals(category.getName())) {
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


  private static Category generateCategory(String name, Optional<String> ctxId, Optional<String> csId, Optional<String> publicId,
                                           Optional<String> longName, Optional<String> type, String version, String parentUniqueId) {

    String id = generateCategoryId(name, type, publicId, version);
    String cadsrId = generateCadsrCategoryId(id, ctxId, csId);
    String uniqueId = UUID.randomUUID().toString();
    String categoryName = longName.isPresent() ? longName.get().trim() : name.trim();
    String description = longName.isPresent() ? longName.get().trim() : name.trim();
    String categoryType = type.isPresent() ? type.get() : null;
    return new Category(id, cadsrId, uniqueId, categoryName, description, categoryType, parentUniqueId, version);
  }

  public static List<CategoryTreeNode> categoriesListToTree(List<Category> categories) {
    return getChildrenNodes(Constants.ROOT_CATEGORY_KEY, categories);
  }

  public static List<CategoryTreeNode> getChildrenNodes(String parentId, List<Category> categories) {
    List<CategoryTreeNode> childrenNodes = new ArrayList<>();
    for (Category category : categories) {
      if (category.getParentId().equals(parentId)) {
        CategoryTreeNode node = new CategoryTreeNode(category.getUniqueId(), category.getCadsrId(), category.getName(), category.getDescription(),
            getChildrenNodes(category.getUniqueId(), categories));
        childrenNodes.add(node);
      }
    }
    return childrenNodes;
  }

  public static String generateCategoryId(String name, Optional<String> type, Optional<String> publicId, String version) {
    final String sep = "-";
    // Format type_name_id_version_name
    String cleanName = name.replaceAll("\n", "");
    cleanName = cleanName.replaceAll(" ", "");
    return cleanName + sep + (type.isPresent() ? (type.get() + sep) : "")
        + (publicId.isPresent() ? (publicId.get() + sep) : "") + "v" + version;
  }

  public static String generateCadsrCategoryId(String categoryId, Optional<String> ctxId, Optional<String> csId) {
    if (ctxId.isPresent()) {
      if (csId.isPresent()) {
        return ctxId.get() + "/" + csId.get() + "/" + categoryId;
      }
      else {
        return ctxId.get() + "/" + categoryId;
      }
    }
    else {
      return categoryId;
    }
  }




}
