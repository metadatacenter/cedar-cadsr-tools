package org.metadatacenter.cadsr.ingestor.tools;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.metadatacenter.cadsr.category.schema.Classifications;
import org.metadatacenter.cadsr.cde.schema.DataElement;
import org.metadatacenter.cadsr.cde.schema.DataElementsList;
import org.metadatacenter.cadsr.ingestor.category.Category;
import org.metadatacenter.cadsr.ingestor.category.CategorySummary;
import org.metadatacenter.cadsr.ingestor.category.action.CategoryActionsProcessor;
import org.metadatacenter.cadsr.ingestor.category.action.CreateCategoryAction;
import org.metadatacenter.cadsr.ingestor.category.action.DeleteCategoryAction;
import org.metadatacenter.cadsr.ingestor.category.action.UpdateCategoryAction;
import org.metadatacenter.cadsr.ingestor.cde.CdeSummary;
import org.metadatacenter.cadsr.ingestor.cde.ValueSetsOntologyManager;
import org.metadatacenter.cadsr.ingestor.cde.action.CdeActionsProcessor;
import org.metadatacenter.cadsr.ingestor.cde.action.CreateCdeAction;
import org.metadatacenter.cadsr.ingestor.cde.action.DeleteCdeAction;
import org.metadatacenter.cadsr.ingestor.cde.action.UpdateCdeAction;
import org.metadatacenter.cadsr.ingestor.util.*;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class CadsrCategoriesAndCdesUpdaterTool {

  private static final Logger logger = LoggerFactory.getLogger(CadsrCategoriesAndCdesUpdaterTool.class);

  // TODO: DELETE
  final static int MAX_CDES_TO_PROCESS = 6;

  public static void main(String[] args) {

    /*** INPUTS ***/

    // Id of the CEDAR CDE folder where the contents will be uploaded
    final String cedarCdeFolderShortId = "03f2d7f0-a54c-4a36-a0a8-c53159ec4aab";
    // caDSR Admin user's api key
    final String apiKey = "apiKey 8d1fdf56f8147054388432716b06e4dac940aa8db326d13e7bfceb17a9ec4b9c"; // In my local
    // Cedar environment
    final CedarEnvironment cedarEnvironment = CedarEnvironment.LOCAL;

    final String EXECUTION_RESOURCES_PATH = "/Users/marcosmr/Development/DEV_EXECUTIONS/2020" +
        "-05_cdes_upload_production_process";
//    final String existingCdesMapFilePath = EXECUTION_RESOURCES_PATH + "/cdesMap.json";

    final String categoriesFolderPath = EXECUTION_RESOURCES_PATH + "/categories";
    //final String categoriesZipFilePath = categoriesFolderPath + "/xml_cscsi_20204110528.zip";
    //final String categoriesZipFilePath = categoriesFolderPath + "/xml_cscsi_20204110528_modified.zip";
    final String categoriesZipFilePath = categoriesFolderPath + "/xml_cscsi_20204110528_deleted.zip";
//    final String categoriesZipFilePath = categoriesFolderPath + "/xml_cscsi_20204110528_deleted2.zip";
    final String categoriesUnzippedFolderPath = categoriesFolderPath + "/unzipped";

    final String cdesFolderPath = EXECUTION_RESOURCES_PATH + "/cdes";
    //final String cdesZipFilePath = cdesFolderPath + "/xml_cde_20205110558.zip";
    final String cdesZipFilePath = cdesFolderPath + "/xml_cde_20205110558_lite.zip";
    //final String cdesZipFilePath = cdesFolderPath + "/xml_cde_20205110558_lite_modified.zip";
    final String cdesUnzippedFolderPath = cdesFolderPath + "/unzipped";

    /*** OUTPUTS ***/
    final String ontologyFilePath = EXECUTION_RESOURCES_PATH + "/ontology/cadsr-vs.owl";

    try {

      /*** STEP 1. UPDATE CATEGORIES ***/
      logger.info("*****************************************");
      logger.info("*  STEP 1. UPDATE NCI caDSR CATEGORIES  *");
      logger.info("*****************************************");

      // Download and parse new Categories TODO: Download them using FTPClient once we have the FTP host
      logger.info("Downloading most recent categories.");
      FileUtils.deleteDirectory(new File(categoriesUnzippedFolderPath));
      UnzipUtility.unzip(categoriesZipFilePath, categoriesUnzippedFolderPath);
      File classificationsFile = (new File(categoriesUnzippedFolderPath)).listFiles()[0];
      Classifications newClassifications = CategoryUtil.getClassifications(new FileInputStream(classificationsFile));
      List<Category> newCategories = CategoryUtil.classificationsToCategoriesList(newClassifications);
      logger.info("Finished downloading categories. " + newCategories.size() + " categories found.");
      updateCategories(newCategories, cedarEnvironment, apiKey);

      /*** STEP 2. UPDATE CDEs and CDE-Category relations ***/
      logger.info("*****************************************");
      logger.info("*         STEP 2. UPDATE CDEs           *");
      logger.info("*****************************************");

      // Download and parse new CDEs // TODO: Read them using FTPClient once we have the FTP host
      FileUtils.deleteDirectory(new File(cdesUnzippedFolderPath));
      UnzipUtility.unzip(cdesZipFilePath, cdesUnzippedFolderPath);
      List<DataElement> newDataElements = new ArrayList<>();
      for (final File inputFile : new File(cdesUnzippedFolderPath).listFiles()) {
        logger.info("Processing CDEs file: " + inputFile.getAbsolutePath());
        DataElementsList newDataElementList = CdeUtil.getDataElementLists(new FileInputStream(inputFile));
        newDataElements.addAll(newDataElementList.getDataElement());
      }

      updateCDEs(newDataElements, cedarCdeFolderShortId, ontologyFilePath, cedarEnvironment, apiKey);

    } catch (IOException | JAXBException e) {
      e.printStackTrace();
    }
  }

  private static void updateCategories(List<Category> newCategories, CedarEnvironment cedarEnvironment,
                                       String apiKey) throws IOException {

    // Generate map with existing Categories based on the current Category Tree in CEDAR
    logger.info("Retrieving current NCI caDSR categories from CEDAR.");
    Map<String, CategorySummary> allCategoriesMap =
        Collections.unmodifiableMap(CategoryUtil.generateExistingCategoriesMap(cedarEnvironment, apiKey));
    // Save the identifier of the top-level CDE category
    String cadsrCategoryCedarId = allCategoriesMap.get(Constants.CADSR_CATEGORY_SCHEMA_ORG_ID).getCedarId();
    // Keep only the CDE categories (ignoring the top-level CDE category)
    Map<String, CategorySummary> existingCdeCategoriesMap = CategoryUtil.extractCdeCategoriesMap(allCategoriesMap);
    logger.info("Number of NCI caDSR categories retrieved from CEDAR: " + existingCdeCategoriesMap.size() + ".");

    // Check category modifications and store them as actions that will be applied later
    logger.info("Checking category changes and generating category actions.");
    List<CreateCategoryAction> createCategoryActions = new ArrayList<>();
    List<UpdateCategoryAction> updateCategoryActions = new ArrayList<>();
    List<DeleteCategoryAction> deleteCategoryActions = new ArrayList<>();
    Map<String, CategorySummary> categoriesToDeleteMap = new HashMap<>(existingCdeCategoriesMap);
    for (Category newCategory : newCategories) {
      if (existingCdeCategoriesMap.containsKey(newCategory.getUniqueId())) {
        String existingCategoryCedarId = existingCdeCategoriesMap.get(newCategory.getUniqueId()).getCedarId();
        String newCategoryHash = CategoryUtil.generateCategoryHashCode(newCategory);
        if (existingCdeCategoriesMap.get(newCategory.getUniqueId()).getHashCode().equals(newCategoryHash)) {
          // The category exists in CEDAR and it didn't change. Do nothing.
        } else {
          // The category exists in CEDAR and it changed. We'll need to update it.
          updateCategoryActions.add(new UpdateCategoryAction(existingCategoryCedarId,
              newCategory.toCategoryTreeNode()));
        }
      } else {
        // The category is not in CEDAR yet. We'll need to create it. We'll see if its parent is in CEDAR. The fact
        // that the parent category is not in CEDAR means that the parent will have to be created as well. At some
        // point, the top of the hierarchy of categories to be created will point to a parent CEDAR category.
        String parentCategoryCedarId = null;
        if (existingCdeCategoriesMap.containsKey(newCategory.getParentUniqueId())) {
          parentCategoryCedarId = existingCdeCategoriesMap.get(newCategory.getParentUniqueId()).getCedarId();
        }
        createCategoryActions.add(new CreateCategoryAction(newCategory.toCategoryTreeNode(),
            parentCategoryCedarId));
      }
      // Update the map to remove the categories that were already visited.
      categoriesToDeleteMap.remove(newCategory.getUniqueId());
    }

    // The remaining categories in the map are not part of the new caDSR file. We'll delete them from CEDAR.
    for (CategorySummary categorySummary : categoriesToDeleteMap.values()) {
      deleteCategoryActions.add(new DeleteCategoryAction(categorySummary.getCedarId()));
    }

    // Process category actions
    CategoryActionsProcessor categoryActionsProcessor =
        new CategoryActionsProcessor(createCategoryActions, updateCategoryActions, deleteCategoryActions,
            cadsrCategoryCedarId, cedarEnvironment, apiKey);
    categoryActionsProcessor.logActionsSummary();
    categoryActionsProcessor.executeCategoryActions();
  }

  private static Map<String, CdeSummary> updateCDEs(List<DataElement> newDataElements,
                                                    String cedarFolderShortId, String ontologyFilePath,
                                                    CedarEnvironment cedarEnvironment, String apiKey) throws IOException {

    // Retrieve existing CDEs from CEDAR
    logger.info("Retrieving current CDEs from CEDAR (folder short id: " + cedarFolderShortId + ").");
    List fieldNamesToInclude = new ArrayList(Arrays.asList(new String[]{"schema:identifier", "pav:version",
        "sourceHash"}));
    List<CdeSummary> cdeSummaries = CedarServices.findCdeSummariesInFolder(cedarFolderShortId,
        fieldNamesToInclude, true, cedarEnvironment, apiKey);
    logger.info("Number of CDEs retrieved from CEDAR: " + cdeSummaries.size() + ").");

    // Create CDE Map (key: cdeId (PublicId + "V" + Version); Value: CdeSummary)
    Map<String, CdeSummary> existingCdesMap = new HashMap<>();
    for (CdeSummary cdeSummary : cdeSummaries) {
      String cdeMapKey = CdeUtil.generateCdeUniqueId(cdeSummary.getId(), cdeSummary.getVersion());
      existingCdesMap.put(cdeMapKey, cdeSummary);
    }

    // Read the categoryIds from CEDAR to be able to link CDEs to them
    Map<String, String> categoryIdsToCedarCategoryIds =
        CedarServices.getCategoryIdsToCedarCategoryIdsMap(cedarEnvironment, apiKey);

    // Check CDE changes
    logger.info("Checking CDEs changes and generating actions.");
    List<CreateCdeAction> createCdeActions = new ArrayList<>();
    List<UpdateCdeAction> updateCdeActions = new ArrayList<>();
    List<DeleteCdeAction> deleteCdeActions = new ArrayList<>();
    Map<String, CdeSummary> cdesToDeleteMap = new HashMap<>(existingCdesMap);
    int count = 0;
    for (DataElement newDataElement : newDataElements) {
      Map<String, Object> newCdeFieldMap = CdeUtil.getFieldMapFromDataElement(newDataElement);
      String newCdeHashCode = CdeUtil.generateCdeHashCode(newDataElement);
      String newCdeUniqueId = CdeUtil.generateCdeUniqueId(newCdeFieldMap);
      List<String> categoryCedarIds = CategoryUtil.extractCategoryCedarIdsFromCdeField(newCdeFieldMap,
          categoryIdsToCedarCategoryIds);
      // Check whether the CDE is new
      if (existingCdesMap.containsKey(newCdeUniqueId)) {
        if (existingCdesMap.get(newCdeUniqueId).getHashCode().equals(newCdeHashCode)) {
          // The CDE exists in CEDAR and it didn't change. Do nothing.
        } else {
          // The CDE exists in CEDAR and it changed. This is wrong. In this case, a new version should have been
          // generated in the caDSR XML. Different XMLs shouldn't contain different CDEs with the same public id and version
          logger.error("Error: The CDE has changed, but its public Id and Version were not updated in the caDSR XML: " + newCdeUniqueId);
        }
      } else {
        // The CDE doesn't exist in CEDAR. We'll have to create it.
        createCdeActions.add(new CreateCdeAction(newCdeFieldMap, newCdeHashCode, cedarFolderShortId, categoryCedarIds));
      }
      // Update the map to remove the cdes that have been visited.
      cdesToDeleteMap.remove(newCdeUniqueId);
      // Stop when reaching limit
      if (count++ == MAX_CDES_TO_PROCESS) {
        break;
      }
    }
    // The remaining CDEs in the map are not part of the new caDSR file. That's wrong. All CDEs should be kept in the
    // XML. The CDEs that are not used anymore will have RETIRED status.
    for (CdeSummary cdeSummary : cdesToDeleteMap.values()) {
      logger.error("Error: The CDE is not part of the caDSR XML any more: " +
          CdeUtil.generateCdeUniqueId(cdeSummary.getId(), cdeSummary.getVersion()));
    }

    // Process CDE actions
    CdeActionsProcessor cdeActionsProcessor =
        new CdeActionsProcessor(createCdeActions, updateCdeActions, deleteCdeActions,
            new HashMap<>(existingCdesMap), cedarEnvironment, apiKey);
    cdeActionsProcessor.logActionsSummary();
    cdeActionsProcessor.executeCdeActions();

    // Save ontology
    ValueSetsOntologyManager.saveOntology(new File(ontologyFilePath));

    return cdeActionsProcessor.getCdesMap();
  }

}
