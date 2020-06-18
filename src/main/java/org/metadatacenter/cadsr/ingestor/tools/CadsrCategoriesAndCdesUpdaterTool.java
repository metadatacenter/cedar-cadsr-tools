package org.metadatacenter.cadsr.ingestor.tools;

import com.google.common.base.Stopwatch;
import org.apache.commons.io.FileUtils;
import org.metadatacenter.cadsr.category.schema.Classifications;
import org.metadatacenter.cadsr.cde.schema.DataElement;
import org.metadatacenter.cadsr.cde.schema.DataElementsList;
import org.metadatacenter.cadsr.ingestor.category.Category;
import org.metadatacenter.cadsr.ingestor.category.CategoryStats;
import org.metadatacenter.cadsr.ingestor.category.CategorySummary;
import org.metadatacenter.cadsr.ingestor.category.action.CategoryActionsProcessor;
import org.metadatacenter.cadsr.ingestor.category.action.CreateCategoryAction;
import org.metadatacenter.cadsr.ingestor.category.action.DeleteCategoryAction;
import org.metadatacenter.cadsr.ingestor.category.action.UpdateCategoryAction;
import org.metadatacenter.cadsr.ingestor.cde.CdeStats;
import org.metadatacenter.cadsr.ingestor.cde.CdeSummary;
import org.metadatacenter.cadsr.ingestor.cde.ValueSetsOntologyManager;
import org.metadatacenter.cadsr.ingestor.cde.action.CdeActionsProcessor;
import org.metadatacenter.cadsr.ingestor.cde.action.CreateCdeAction;
import org.metadatacenter.cadsr.ingestor.cde.action.RetireCdeAction;
import org.metadatacenter.cadsr.ingestor.util.*;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarEnvironment;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.config.environment.CedarEnvironmentVariableProvider;
import org.metadatacenter.model.SystemComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CadsrCategoriesAndCdesUpdaterTool {

  private static final Logger logger = LoggerFactory.getLogger(CadsrCategoriesAndCdesUpdaterTool.class);

  private final static CedarConfig cedarConfig;
  private final static int MAX_CDES_TO_PROCESS = Integer.MAX_VALUE;

  static {
    SystemComponent systemComponent = SystemComponent.NCI_CADSR_TOOLS;
    Map<String, String> environment = CedarEnvironmentVariableProvider.getFor(systemComponent);
    cedarConfig = CedarConfig.getInstance(environment);
  }

  public static void main(String[] args) {

    final Stopwatch stopwatch = Stopwatch.createStarted();
    final LocalDateTime startTime = LocalDateTime.now();

    String cedarCdeFolderShortId = args[0];
    CedarEnvironment targetEnvironment = CedarServerUtil.toCedarEnvironment(args[1]);
    String cadsrAdminApikey = "apiKey " + args[2];

    /*** INPUTS ***/

    final Optional<String> classificationsZipFileName = Optional.empty(); //Optional.of("xml_cscsi_20205110511.zip");
    final Optional<String> cdesZipFilePath = Optional.empty(); //Optional.of("xml_cde_20205110558.zip");
    final boolean cleanupCedarCategories = false;

    /*** OUTPUTS ***/
    final String ontologyFilePath = "/Users/marcosmr/Development/DEV_EXECUTIONS/2020" +
        "-05_cdes_upload_production_process/ontology/cadsr-vs.owl";

    try {

      logger.info("#####################################################################");
      logger.info("# Execution started at " + startTime);
      logger.info("# Execution settings:");
      logger.info("#   - CEDAR folder shord Id: " + cedarCdeFolderShortId);
      logger.info("#   - CEDAR environment: " + targetEnvironment.name());
      logger.info("#####################################################################\n");

      /*** STEP 1. UPDATE CATEGORIES ***/
      logger.info("#########################################");
      logger.info("#      Updating caDSR Categories...     #");
      logger.info("#########################################");

      updateCategories(Constants.CLASSIFICATIONS_TMP_FOLDER_PATH, classificationsZipFileName, cleanupCedarCategories,
          targetEnvironment, cadsrAdminApikey);

      /*** STEP 2. UPDATE CDEs and CDE-Category relations ***/
      logger.info("#########################################");
      logger.info("#            Updating CDEs...           #");
      logger.info("#########################################");

      updateCDEs(Constants.CDES_TMP_FOLDER_PATH, cdesZipFilePath, cedarCdeFolderShortId, ontologyFilePath,
          targetEnvironment, cadsrAdminApikey);

      printSummary(stopwatch, startTime, ontologyFilePath);

    } catch (IOException | JAXBException e) {
      logger.error("Error: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static void updateCategories(String tmpExecutionFolderPath, Optional<String> classificationsZipFileName,
                                       boolean cleanupCategories, CedarEnvironment cedarEnvironment,
                                       String apiKey) throws IOException, JAXBException {

    // Delete categories temporal folder if it exists
    FileUtils.deleteDirectory(new File(tmpExecutionFolderPath));

    String unzippedCategoriesFolderPath = tmpExecutionFolderPath + "/" + Constants.CLASSIFICATIONS_UNZIP_FOLDER_NAME;
    if (classificationsZipFileName.isPresent()) { // Read categories from file
      UnzipUtility.unzip(tmpExecutionFolderPath + "/" + classificationsZipFileName.get(), unzippedCategoriesFolderPath);
    } else { // Download most recent categories from the NCI FTP servers
      logger.info("Downloading most recent categories");
      File categoriesZipFile = FtpUtil.downloadMostRecentFile(cedarConfig.getNciCadsrToolsConfig().getFtp().getHost(),
          cedarConfig.getNciCadsrToolsConfig().getFtp().getUser(),
          cedarConfig.getNciCadsrToolsConfig().getFtp().getPassword(),
          cedarConfig.getNciCadsrToolsConfig().getFtp().getClassificationsDirectory(),
          tmpExecutionFolderPath);
      UnzipUtility.unzip(categoriesZipFile.getAbsolutePath(), unzippedCategoriesFolderPath);
    }

    if (cleanupCategories) {
      logger.info("Deleting all existing caDSR categories in CEDAR.");
      CategoryUtil.deleteAllNciCadsrCategories(cedarEnvironment, apiKey);
      CategoryStats.resetStats();
    }

    File classificationsFile = (new File(unzippedCategoriesFolderPath)).listFiles()[0];
    Classifications newClassifications = CategoryUtil.getClassifications(new FileInputStream(classificationsFile));
    List<Category> newCategories = CategoryUtil.classificationsToCategoriesList(newClassifications);
    logger.info("Finished downloading categories. " + newCategories.size() + " categories found.");

    // Generate map with existing Categories based on the current Category Tree in CEDAR
    logger.info("Retrieving current NCI caDSR categories from CEDAR.");
    Map<String, CategorySummary> allCategoriesMap =
        Collections.unmodifiableMap(CategoryUtil.generateExistingCategoriesMap(cedarEnvironment, apiKey));
    // Save the identifier of the top-level CDE category
    String cadsrCategoryCedarId = allCategoriesMap.get(Constants.CADSR_CATEGORY_SCHEMA_ORG_ID).getCedarId();
    // Keep only the CDE categories (ignoring the top-level CDE category)
    Map<String, CategorySummary> existingCdeCategoriesMap = CategoryUtil.extractCdeCategoriesMap(allCategoriesMap);
    logger.info("Number of NCI caDSR categories retrieved from CEDAR: " + existingCdeCategoriesMap.size() + ".");
    CategoryStats.getInstance().numberOfExistingCategories = existingCdeCategoriesMap.size();

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

    // Remove temporal files
    logger.info("Deleting folder generated during categories update: " + new File(Constants.CLASSIFICATIONS_TMP_FOLDER_PATH).getAbsolutePath());
    FileUtils.deleteDirectory(new File(Constants.CLASSIFICATIONS_TMP_FOLDER_PATH));
  }

  private static Map<String, CdeSummary> updateCDEs(String tmpExecutionFolderPath, Optional<String> cdesZipFileName,
                                                    String cedarFolderShortId, String ontologyFilePath,
                                                    CedarEnvironment cedarEnvironment, String apiKey) throws IOException, JAXBException {

    // Delete CDEs temporal folder if it exists
    FileUtils.deleteDirectory(new File(tmpExecutionFolderPath));

    String unzippedCdesFolderPath = tmpExecutionFolderPath + "/" + Constants.CDES_UNZIP_FOLDER_NAME;
    if (cdesZipFileName.isPresent()) {
      UnzipUtility.unzip(tmpExecutionFolderPath + "/" + cdesZipFileName.get(), unzippedCdesFolderPath);
    } else { // read CDEs from file
      logger.info("Downloading most recent CDEs");
      File cdesZipFile = FtpUtil.downloadMostRecentFile(cedarConfig.getNciCadsrToolsConfig().getFtp().getHost(),
          cedarConfig.getNciCadsrToolsConfig().getFtp().getUser(),
          cedarConfig.getNciCadsrToolsConfig().getFtp().getPassword(),
          cedarConfig.getNciCadsrToolsConfig().getFtp().getCdesDirectory(),
          tmpExecutionFolderPath);
      UnzipUtility.unzip(cdesZipFile.getAbsolutePath(), unzippedCdesFolderPath);
    }

    List<DataElement> newDataElements = new ArrayList<>();
    for (final File inputFile : new File(unzippedCdesFolderPath).listFiles()) {
      logger.info("Processing CDEs file: " + inputFile.getAbsolutePath());
      DataElementsList newDataElementList = CdeUtil.getDataElementLists(new FileInputStream(inputFile));
      newDataElements.addAll(newDataElementList.getDataElement());
    }

    // Retrieve existing CDEs from CEDAR
    logger.info("Retrieving current CDEs from CEDAR (folder short id: " + cedarFolderShortId + ").");
    List fieldNamesToInclude = new ArrayList(Arrays.asList(new String[]{"schema:identifier", "pav:version",
        "sourceHash"}));
    List<CdeSummary> cdeSummaries = CedarServices.findCdeSummariesInFolder(cedarFolderShortId,
        fieldNamesToInclude, true, cedarEnvironment, apiKey);
    logger.info("Number of CDEs retrieved from CEDAR: " + cdeSummaries.size() + ".");
    CdeStats.getInstance().numberOfExistingCdes = cdeSummaries.size();

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
    List<RetireCdeAction> retireCdeActions = new ArrayList<>();
    Map<String, CdeSummary> cdesToDeleteMap = new HashMap<>(existingCdesMap);
    int count = 1;
    for (DataElement newDataElement : newDataElements) {
      // ------ START NEW BLOCK ----------
//      CdeStats.getInstance().numberOfInputCdes++;
//      logger.info("Processing CDE " + count + "/" + newDataElements.size());
//      String newCdeUniqueId = CdeUtil.generateCdeUniqueId(newDataElement);
//      String newCdeHashCode = CdeUtil.generateCdeHashCode(newDataElement);
//      // Check whether the CDE is already in CEDAR. We assume that if a CDE is already in CEDAR, is supported and
//      // therefore it can be parsed without throwing an UnsupportedDataElementException
//      if (existingCdesMap.containsKey(newCdeUniqueId)) {
//        if (existingCdesMap.get(newCdeUniqueId).getHashCode().equals(newCdeHashCode)) {
//          // The CDE exists in CEDAR and it didn't change. Do nothing.
//        } else {
//          // The CDE exists in CEDAR and it changed. This is wrong. In this case, a new version should have been
//          // generated in the caDSR XML. Different XMLs shouldn't contain different CDEs with the same public id
//          // and version
//          String reason = "Error: The CDE has changed, but its public Id and Version were not updated in the XML";
//          logger.error(reason + newCdeUniqueId);
//          CdeStats.getInstance().numberOfCdesFailed++;
//          CdeStats.getInstance().addFailed(reason);
//        }
//        // Update the map to remove the cdes that have been visited.
//        cdesToDeleteMap.remove(newCdeUniqueId);
//      }
//      // The CDE doesn't exist in CEDAR yet. We'll have to process it and, if it's supported, create it
//      else {
//        // The following call will perform a full parsing of the CDE. It is a heavy call so we tried to use it only
//        // when we really need to process the CDE
//        Map<String, Object> newCdeFieldMap = CdeUtil.getFieldMapFromDataElement(newDataElement);
//        if (newCdeFieldMap != null) { // If the cde is not supported, newCdeFieldMap will be null
//          CdeStats.getInstance().numberOfCdesProcessedOk++;
//          List<String> categoryCedarIds = CategoryUtil.extractCategoryCedarIdsFromCdeField(newCdeFieldMap,
//              categoryIdsToCedarCategoryIds);
//          List<String> categoryIds = CategoryUtil.extractCategoryIdsFromCdeField(newCdeFieldMap);
//          createCdeActions.add(new CreateCdeAction(newCdeFieldMap, newCdeHashCode, cedarFolderShortId,
//              categoryCedarIds, categoryIds));
//        } else {
//          // The CDE is not supported.
//        }
//
//      }
      // ------ END NEW BLOCK ----------

      // ------ START OLD BLOCK ----------
      logger.info("Processing CDE " + count + "/" + newDataElements.size());
      Map<String, Object> newCdeFieldMap = CdeUtil.getFieldMapFromDataElement(newDataElement);
      CdeStats.getInstance().numberOfInputCdes++;
      if (newCdeFieldMap != null) { // If the cde is not supported, newCdeFieldMap will be null
        CdeStats.getInstance().numberOfCdesProcessedOk++;
        String newCdeHashCode = CdeUtil.generateCdeHashCode(newDataElement);
        String newCdeUniqueId = CdeUtil.generateCdeUniqueId(newDataElement);
        List<String> categoryCedarIds = CategoryUtil.extractCategoryCedarIdsFromCdeField(newCdeFieldMap,
            categoryIdsToCedarCategoryIds);
        List<String> categoryIds = CategoryUtil.extractCategoryIdsFromCdeField(newCdeFieldMap);
        // Check whether the CDE is new
        if (existingCdesMap.containsKey(newCdeUniqueId)) {
          if (existingCdesMap.get(newCdeUniqueId).getHashCode().equals(newCdeHashCode)) {
            // The CDE exists in CEDAR and it didn't change. Do nothing.
          } else {
            // The CDE exists in CEDAR and it changed. This is wrong. In this case, a new version should have been
            // generated in the caDSR XML. Different XMLs shouldn't contain different CDEs with the same public id
            // and version
            String reason = "Error: The CDE has changed, but its public Id and Version were not updated in the XML";
            logger.error(reason + newCdeUniqueId);
            CdeStats.getInstance().numberOfCdesFailed++;
            CdeStats.getInstance().addFailed(reason);
          }
        } else {
          // The CDE doesn't exist in CEDAR. We'll have to create it.
          createCdeActions.add(new CreateCdeAction(newCdeFieldMap, newCdeHashCode, cedarFolderShortId,
              categoryCedarIds, categoryIds));
        }
        // Update the map to remove the cdes that have been visited.
        cdesToDeleteMap.remove(newCdeUniqueId);
      }
      // ------ END OLD BLOCK ----------

      // Stop when reaching limit
      if (count++ == MAX_CDES_TO_PROCESS) {
        break;
      }

    }
    // The remaining CDEs in the map are not part of the new caDSR file with RELEASED status so they will have RETIRED
    // status. We will have to set them as RETIRED in CEDAR
    for (CdeSummary cdeSummary : cdesToDeleteMap.values()) {
      logger.warn("The CDE is not part of the caDSR XML any more with RELEASED status: " +
          CdeUtil.generateCdeUniqueId(cdeSummary.getId(), cdeSummary.getVersion()));
      retireCdeActions.add(new RetireCdeAction(cdeSummary.getCedarId(), cdeSummary.getId(), cdeSummary.getVersion(),
          cdeSummary.getHashCode()));
    }

    // Process CDE actions
    CdeActionsProcessor cdeActionsProcessor =
        new CdeActionsProcessor(createCdeActions, retireCdeActions,
            new HashMap<>(existingCdesMap), cedarEnvironment, apiKey);
    cdeActionsProcessor.logActionsSummary();
    cdeActionsProcessor.executeCdeActions();

    // Save ontology
    ValueSetsOntologyManager.saveOntology(new File(ontologyFilePath));

    return cdeActionsProcessor.getCdesMap();
  }

  private static void printSummary(Stopwatch stopwatch, LocalDateTime startTime, String ontologyFilePath) {
    final DecimalFormat countFormat = new DecimalFormat("#,###,###,###");
    long elapsedTimeInSeconds = stopwatch.elapsed(TimeUnit.SECONDS);
    long hours = elapsedTimeInSeconds / 3600;
    long minutes = (elapsedTimeInSeconds % 3600) / 60;
    long seconds = (elapsedTimeInSeconds % 60);
    logger.info("#####################################################################");
    logger.info("#                        EXECUTION SUMMARY                          #");
    logger.info("#####################################################################");
    logger.info("#  - Total time: " + String.format("%02d:%02d:%02d", hours, minutes, seconds));
    logger.info("#  - Started at: " + startTime);
    logger.info("#  - Finished at: " + LocalDateTime.now());
    logger.info("#  caDSR CATEGORIES SUMMARY:");
    logger.info("#    - Number of categories read from the XML file: " + countFormat.format(CategoryStats.getInstance().numberOfInputCategories));
    logger.info("#    - Number of categories in CEDAR: " + countFormat.format(CategoryStats.getInstance().numberOfExistingCategories));
    logger.info("#    - Number of categories to be CREATED: " + countFormat.format(CategoryStats.getInstance().numberOfCategoriesToBeCreated));
    logger.info("#    - Number of categories created successfully: " + countFormat.format(CategoryStats.getInstance().numberOfCategoriesCreated));
    logger.info("#    - Number of categories to be UPDATED: " + countFormat.format(CategoryStats.getInstance().numberOfCategoriesToBeUpdated));
    logger.info("#    - Number of categories updated successfully: " + countFormat.format(CategoryStats.getInstance().numberOfCategoriesUpdated));
    logger.info("#    - Number of categories to be DELETED: " + countFormat.format(CategoryStats.getInstance().numberOfCategoriesToBeDeleted));
    logger.info("#    - Number of categories deleted successfully: " + countFormat.format(CategoryStats.getInstance().numberOfCategoriesDeleted));
    logger.info("#    - Number of categories referenced in CDEs but not found in source XML: " + countFormat.format(CategoryStats.getInstance().idsOfCategoriesNotFoundInSource.size()));
    if (CategoryStats.getInstance().idsOfCategoriesNotFoundInSource.size() > 0) {
      logger.info("#    - List of categories referenced in CDEs but not found in source XML: ");
      for (String categoryId : CategoryStats.getInstance().idsOfCategoriesNotFoundInSource) {
        logger.info("#      - " + categoryId);
      }
    }
    logger.info("#  CDEs SUMMARY:");
    logger.info("#    - Number of CDEs read from the XML files: " + countFormat.format(CdeStats.getInstance().numberOfInputCdes));
    logger.info("#    - Number of CDEs in CEDAR: " + countFormat.format(CdeStats.getInstance().numberOfExistingCdes));
    logger.info("#    - Number of CDEs successfully transformed to CEDAR fields: "
        + countFormat.format(CdeStats.getInstance().numberOfCdesProcessedOk)
        + " (" + GeneralUtil.calculatePercentage(CdeStats.getInstance().numberOfCdesProcessedOk,
        CdeStats.getInstance().numberOfInputCdes) + ")");
    logger.info("#      - Number of CEDAR CDEs that were skipped: "
        + countFormat.format(CdeStats.getInstance().numberOfCdesSkipped)
        + " (" + GeneralUtil.calculatePercentage(CdeStats.getInstance().numberOfCdesSkipped,
        CdeStats.getInstance().numberOfInputCdes) + ")");
    logger.info("#      - Number of CEDAR CDEs that failed to process: "
        + countFormat.format(CdeStats.getInstance().numberOfCdesFailed)
        + " (" + GeneralUtil.calculatePercentage(CdeStats.getInstance().numberOfCdesFailed,
        CdeStats.getInstance().numberOfInputCdes) + ")");
    logger.info("#      - Breakdown of skipped CDEs: ");
    for (Map.Entry<String, Integer> entry : CdeStats.getInstance().getSkippedReasons().entrySet()) {
      logger.info("#        - " + entry.getKey() + ": " + entry.getValue());
    }
    if (CdeStats.getInstance().numberOfCdesFailed > 0) {
      logger.info("#      - Breakdown of failed CDEs: ");
      for (Map.Entry<String, Integer> entry : CdeStats.getInstance().getFailedReasons().entrySet()) {
        logger.info("#        - " + entry.getKey() + ": " + entry.getValue());
      }
    }
    logger.info("#    - Number of CEDAR CDE fields to be CREATED: "
        + countFormat.format(CdeStats.getInstance().numberOfCdesToBeCreated));
    logger.info("#    - Number of CEDAR CDE fields created successfully: " + countFormat.format(CdeStats.getInstance().numberOfCdesCreated));
    logger.info("#    - Number of CEDAR CDE fields to be RETIRED: "
        + countFormat.format(CdeStats.getInstance().numberOfCdesToBeRetired));
    logger.info("#    - Number of CEDAR CDE fields retired successfully: " + countFormat.format(CdeStats.getInstance().numberOfCdesRetired));
    logger.info("#  CEDARVS ONTOLOGY SUMMARY:");
    logger.info("#    - File path: " + ontologyFilePath);
    logger.info("#    - Number value sets: " + countFormat.format(ValueSetsOntologyManager.getValueSetsCount()));
    logger.info("#    - Number of values: " + countFormat.format(ValueSetsOntologyManager.getValuesCount()));
    logger.info("#####################################################################");
  }

}
