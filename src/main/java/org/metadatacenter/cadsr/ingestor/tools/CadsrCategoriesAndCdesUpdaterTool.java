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
import org.metadatacenter.cadsr.ingestor.tools.config.ConfigSettings;
import org.metadatacenter.cadsr.ingestor.tools.config.ConfigSettingsParser;
import org.metadatacenter.cadsr.ingestor.util.*;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarEnvironment;
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

  public static void main(String[] args) {

    final Stopwatch stopwatch = Stopwatch.createStarted();
    final LocalDateTime startTime = LocalDateTime.now();
    ConfigSettings settings = ConfigSettingsParser.parse(args);

    try {
      logger.info("#####################################################################");
      logger.info("# Execution started at " + startTime);
      logger.info("# Execution settings:");
      logger.info("#   - Command-line arguments: ");
      for (String argGroup : GeneralUtil.commandLineArgumentsGrouped(args, "-p", "--ftp-password", "-k", "apikey")) {
        logger.info("#     " + argGroup);
      }
      logger.info("#   - Update categories? " + (settings.getUpdateCategories()? "Yes" : "No"));
      logger.info("#   - Delete existing categories before updating them? " + (settings.getDeleteCategories()? "Yes" : "No"));
      logger.info("#   - Update CDEs? " + (settings.getUpdateCdes()? "Yes" : "No"));
      logger.info("#   - CEDAR folder short Id: " + settings.getCedarCdeFolderShortId());
      logger.info("#   - CEDAR environment: " + settings.getCedarEnvironment().name());
      logger.info("#   - CEDAR caDSR Admin api key: " + "******");
      logger.info("#   - Local execution folder: " + settings.getExecutionFolder());
      logger.info("#   - Local categories file: " + (settings.getCategoriesFilePath() != null ? settings.getCategoriesFilePath() : "Not provided"));
      logger.info("#   - Local CDEs file: " + (settings.getCdesFilePath() != null ? settings.getCdesFilePath() : "Not provided"));
      logger.info("#   - caDSR FTP Host: " + (settings.getFtpHost() != null ? settings.getFtpHost() : "Not provided"));
      logger.info("#   - caDSR FTP User: " + (settings.getFtpUser() != null ? settings.getFtpUser() : "Not provided"));
      logger.info("#   - caDSR FTP Password: " + (settings.getFtpPassword() != null ? "********" : "Not provided"));
      logger.info("#   - caDSR FTP Categories folder: " + (settings.getFtpCategoriesFolder() != null ? settings.getFtpCategoriesFolder() : "Not provided"));
      logger.info("#   - caDSR FTP CDEs folder: " + (settings.getFtpCdesFolder() != null ? settings.getFtpCdesFolder() : "Not provided"));
      logger.info("#   - Ontology destination folder: " + settings.getOntologyOutputFolderPath());
      logger.info("#####################################################################\n");

      if (settings.getUpdateCategories()) {

        /*** UPDATE CATEGORIES ***/
        logger.info("#########################################");
        logger.info("#      Updating caDSR Categories...     #");
        logger.info("#########################################");

        // Delete categories temporal folder if it exists
        FileUtils.deleteDirectory(new File(settings.getExecutionFolder()));

        String categoriesOutputFolder = settings.getExecutionFolder() + "/" + Constants.CATEGORIES_FOLDER;
        String unzippedCategoriesFolder = categoriesOutputFolder + "/" + Constants.UNZIPPED_FOLDER;

        if (settings.getCategoriesFilePath() != null) { // Read categories from file
          UnzipUtility.unzip(settings.getCategoriesFilePath(), unzippedCategoriesFolder);
        } else { // Download most recent categories from the NCI FTP servers
          logger.info("Downloading most recent categories");
          File categoriesZipFile = FtpUtil.downloadMostRecentFile(settings.getFtpHost(), settings.getFtpUser(), settings.getFtpPassword(), settings.getFtpCategoriesFolder(), categoriesOutputFolder);
          UnzipUtility.unzip(categoriesZipFile.getAbsolutePath(), unzippedCategoriesFolder);
        }

        if (settings.getDeleteCategories()) {
          logger.info("Deleting all existing caDSR categories in CEDAR.");
          CategoryUtil.deleteAllNciCadsrCategories(settings.getCedarEnvironment(), settings.getCadsrAdminApikey());
          CategoryStats.resetStats();
        }

        File classificationsFile = (new File(unzippedCategoriesFolder)).listFiles()[0];
        Classifications newClassifications = CategoryUtil.getClassifications(new FileInputStream(classificationsFile));
        List<Category> newCategories = CategoryUtil.classificationsToCategoriesList(newClassifications);
        logger.info("Finished downloading categories. " + newCategories.size() + " categories found.");

        updateCategories(newCategories, settings.getCedarEnvironment(), settings.getCadsrAdminApikey());
      }

      String ontologyFilePath = null;
      if (settings.getUpdateCdes()) {

        /*** UPDATE CDEs and CDE-Category relations ***/
        logger.info("#########################################");
        logger.info("#            Updating CDEs...           #");
        logger.info("#########################################");

        // Delete CDEs temporal folder if it exists
        FileUtils.deleteDirectory(new File(settings.getExecutionFolder()));

        String cdesOutputFolder = settings.getExecutionFolder() + "/" + Constants.CDES_FOLDER;
        String unzippedCdesFolder = cdesOutputFolder + "/" + Constants.UNZIPPED_FOLDER;

        if (settings.getCdesFilePath() != null) {
          UnzipUtility.unzip(settings.getCdesFilePath(), unzippedCdesFolder);
        } else { // read CDEs from file
          logger.info("Downloading most recent CDEs");
          File cdesZipFile = FtpUtil.downloadMostRecentFile(settings.getFtpHost(), settings.getFtpUser(), settings.getFtpPassword(), settings.getFtpCdesFolder(), cdesOutputFolder);
          UnzipUtility.unzip(cdesZipFile.getAbsolutePath(), unzippedCdesFolder);
        }

        List<DataElement> newDataElements = new ArrayList<>();
        for (final File inputFile : new File(unzippedCdesFolder).listFiles()) {
          logger.info("Processing CDEs file: " + inputFile.getAbsolutePath());
          DataElementsList newDataElementList = CdeUtil.getDataElementLists(new FileInputStream(inputFile));
          newDataElements.addAll(newDataElementList.getDataElement());
        }

        ontologyFilePath = settings.getOntologyOutputFolderPath() + "/" + Constants.ONTOLOGY_FILE;
        updateCDEs(newDataElements, settings.getCedarCdeFolderShortId(), ontologyFilePath, settings.getCedarEnvironment(), settings.getCadsrAdminApikey());

        // Remove temporal files
        logger.info("Deleting temporal execution folder: " + new File(settings.getExecutionFolder()).getAbsolutePath());
        FileUtils.deleteDirectory(new File(settings.getExecutionFolder()));

      }

      printSummary(stopwatch, startTime, settings.getUpdateCategories(), settings.getUpdateCdes(), ontologyFilePath);

    } catch (IOException | JAXBException e) {
      logger.error("Error: " + e.getMessage());
      e.printStackTrace();
    }
    finally {
      try {
        FileUtils.deleteDirectory(new File(settings.getExecutionFolder()));
      } catch (IOException e) {
        logger.error("Error deleting execution folder: " + e.getMessage());
      }
    }
  }

  private static void updateCategories(List<Category> newCategories, CedarEnvironment cedarEnvironment, String apiKey) throws IOException {

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
    logger.info("Number of CDEs retrieved from CEDAR: " + cdeSummaries.size() + ".");
    CdeStats.getInstance().numberOfExistingCdes = cdeSummaries.size();

    // Create CDE Map (key: cdeId (PublicId + "V" + Version); Value: CdeSummary)
    Map<String, CdeSummary> existingCdesMap = new HashMap<>();
    for (CdeSummary cdeSummary : cdeSummaries) {
      String cdeMapKey = CdeUtil.generateCdeUniqueId(cdeSummary.getId(), cdeSummary.getVersion());
      existingCdesMap.put(cdeMapKey, cdeSummary);
    }

    // Read the categoryIds from CEDAR to be able to link CDEs to them
    Map<String, String> categoryUniqueIdsToCedarCategoryIds =
        CedarServices.getCategoryUniqueIdsToCedarCategoryIdsMap(cedarEnvironment, apiKey);
    Map<String, Set<String>> categoryCadsrIdsToCedarCategoryIds =
        CategoryUtil.generateCategoryCadsrIdsToCedarCategoryIdsMap(categoryUniqueIdsToCedarCategoryIds);

    // Check CDE changes
    logger.info("Checking CDEs changes and generating actions.");
    List<CreateCdeAction> createCdeActions = new ArrayList<>();
    List<RetireCdeAction> retireCdeActions = new ArrayList<>();
    Map<String, CdeSummary> cdesToDeleteMap = new HashMap<>(existingCdesMap);
    int count = 1;
    for (DataElement newDataElement : newDataElements) {

      logger.info("Processing CDE " + count + "/" + newDataElements.size());
      Map<String, Object> newCdeFieldMap = CdeUtil.getFieldMapFromDataElement(newDataElement);
      CdeStats.getInstance().numberOfInputCdes++;
      if (newCdeFieldMap != null) { // If the cde is not supported, newCdeFieldMap will be null
        CdeStats.getInstance().numberOfCdesProcessedOk++;
        String newCdeHashCode = CdeUtil.generateCdeHashCode(newDataElement);
        String newCdeUniqueId = CdeUtil.generateCdeUniqueId(newDataElement);
        List<String> categoryCedarIds = CategoryUtil.extractCategoryCedarIdsFromCdeField(newCdeFieldMap,
            categoryCadsrIdsToCedarCategoryIds);
        List<String> categoryCadsrIds = CategoryUtil.extractCategoryCadsrIdsFromCdeField(newCdeFieldMap);
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
              categoryCedarIds, categoryCadsrIds));
        }
        // Update the map to remove the cdes that have been visited.
        cdesToDeleteMap.remove(newCdeUniqueId);
      }

      // Stop when reaching limit
      if (count++ == Constants.MAX_CDES_TO_PROCESS) {
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

  private static void printSummary(Stopwatch stopwatch, LocalDateTime startTime, boolean showCategoriesSummary, boolean showCdesSummary, String ontologyFilePath) {
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

    if (showCategoriesSummary) {
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
    }
    if (showCdesSummary) {
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
      logger.info("#    - Location: " + ontologyFilePath);
      logger.info("#    - Number value sets: " + countFormat.format(ValueSetsOntologyManager.getValueSetsCount()));
      logger.info("#    - Number of values: " + countFormat.format(ValueSetsOntologyManager.getValuesCount()));
    }
    logger.info("#####################################################################");
  }

}
