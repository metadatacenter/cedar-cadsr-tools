package org.metadatacenter.cadsr.ingestor.tools;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.metadatacenter.cadsr.category.schema.Classifications;
import org.metadatacenter.cadsr.cde.schema.DataElement;
import org.metadatacenter.cadsr.cde.schema.DataElementsList;
import org.metadatacenter.cadsr.ingestor.category.Category;
import org.metadatacenter.cadsr.ingestor.category.CategorySummary;
import org.metadatacenter.cadsr.ingestor.category.CedarCategory;
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
import org.metadatacenter.cadsr.ingestor.util.CategoryUtil;
import org.metadatacenter.cadsr.ingestor.util.CdeUtil;
import org.metadatacenter.cadsr.ingestor.util.CedarServices;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarEnvironment;
import org.metadatacenter.cadsr.ingestor.util.UnzipUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class CadsrCategoriesAndCdesUpdaterTool {

  private static ObjectMapper objectMapper = new ObjectMapper();
  private static final Logger logger = LoggerFactory.getLogger(CadsrCategoriesAndCdesUpdaterTool.class);

  public static void main(String[] args) {

    /*** INPUTS ***/
    final String EXECUTION_RESOURCES_PATH = "/Users/marcosmr/Development/DEV_EXECUTIONS/2020" +
        "-05_cdes_upload_production_process";
    final String EXISTING_CDES_MAP_FILE_PATH = EXECUTION_RESOURCES_PATH + "/cdesMap.json";

    final String categoriesFolderPath = EXECUTION_RESOURCES_PATH + "/categories";
    final String categoriesZipFilePath = categoriesFolderPath + "/xml_cscsi_20204110528.zip";
//    final String categoriesZipFilePath = categoriesFolderPath + "/xml_cscsi_20204110528_modified.zip";
//    final String categoriesZipFilePath = categoriesFolderPath + "/xml_cscsi_20204110528_deleted.zip";
//    final String categoriesZipFilePath = categoriesFolderPath + "/xml_cscsi_20204110528_deleted2.zip";
    final String categoriesUnzippedFolderPath = categoriesFolderPath + "/unzipped";

    final String cdesFolderPath = EXECUTION_RESOURCES_PATH + "/cdes";
    //final String cdesZipFilePath = cdesFolderPath + "/xml_cde_20205110558.zip";
    final String cdesZipFilePath = cdesFolderPath + "/xml_cde_20205110558_lite.zip";
    //final String cdesZipFilePath = cdesFolderPath + "/xml_cde_20205110558_lite_modified.zip";
    final String cdesUnzippedFolderPath = cdesFolderPath + "/unzipped";

    // Id of the CEDAR folder where the contents will be uploaded
    final String cedarFolderShortId = "1b21338a-10ab-4015-94be-353f947b8a6d";

    // caDSR user's api key
    final String apiKey = "apiKey 58c4f22b9ea1548047682f3112f2f1bcedcb5e40443ddb5e6a11bda0629c2f20"; // In my local
    // system I'm using the cedar-admin api key
    final CedarEnvironment targetEnvironment = CedarEnvironment.LOCAL;

    /*** OUTPUTS ***/
    //final String categoryTreeFolderPath = categoriesFolderPath + "/tree";
    final String ontologyFilePath = EXECUTION_RESOURCES_PATH + "/ontology/cadsr-vs.owl";

    /*** TEMPORAL VARIABLES TODO: REMOVE ***/
    final int MAX_CDES_TO_PROCESS = 5;
    //boolean deleteCategoryTree = false;

    try {

      /* Categories */

      // Generate map with existing Categories based on the current Category Tree in CEDAR
      Map<String, CategorySummary> existingCategoriesMap =
          Collections.unmodifiableMap(CategoryUtil.generateExistingCategoriesMap(targetEnvironment, apiKey));


//      if (deleteCategoryTree) {
//        CedarServices.deleteCategoryTree(targetEnvironment, apiKey);
//      }

      /*** Download and extract new Categories ***/ // TODO: Read them using FTPClient once we have the FTP host
      FileUtils.deleteDirectory(new File(categoriesUnzippedFolderPath));
      UnzipUtility.unzip(categoriesZipFilePath, categoriesUnzippedFolderPath);

      /*** Read new XML caDSR classifications as Categories ***/
      File classificationsFile = (new File(categoriesUnzippedFolderPath)).listFiles()[0];
      Classifications newClassifications = CategoryUtil.getClassifications(new FileInputStream(classificationsFile));
      List<Category> newCategories = CategoryUtil.classificationsToCategoriesList(newClassifications);

      // Read the categoryIds from CEDAR to be able to link CDEs to them
      Map<String, String> categoryIdsToCedarCategoryIds = null;
      try {
        categoryIdsToCedarCategoryIds =
            CedarServices.getCedarCategoryIds(targetEnvironment, apiKey);
      } catch (IOException e) {
        logger.error(e.getMessage());
      }

      /*** Check category changes ***/
      List<CreateCategoryAction> createCategoryActions = new ArrayList<>();
      List<UpdateCategoryAction> updateCategoryActions = new ArrayList<>();
      List<DeleteCategoryAction> deleteCategoryActions = new ArrayList<>();
      //List<CedarCategory> unchangedCategories = new ArrayList<>();
      Map<String, CategorySummary> categoriesToDeleteMap = new HashMap<>(existingCategoriesMap);
      for (Category newCategory : newCategories) {
        if (existingCategoriesMap.containsKey(newCategory.getUniqueId())) {
          String existingCategoryCedarId = existingCategoriesMap.get(newCategory.getUniqueId()).getCedarId();
          String newCategoryHash = CategoryUtil.generateCategoryHashCode(newCategory);
          if (existingCategoriesMap.get(newCategory.getUniqueId()).getHashCode().equals(newCategoryHash)) {
            // The category exists in CEDAR and it didn't change. Do nothing.
            //unchangedCategories.add(currentCategory.toCedarCategory(existingCategoryCedarId, null));
          } else {
            // The category exists in CEDAR and it changed. We'll need to update it.
            updateCategoryActions.add(new UpdateCategoryAction(existingCategoryCedarId,
                newCategory.toCategoryTreeNode()));
          }
        } else {
          // The category is not in CEDAR yet. We'll need to create it. We'll see if its parent is in CEDAR. The fact
          // that the parent category is not in CEDAR means that the parent will have to be created as well. At some
          // point, the top of the hierarchy of categories to be created will point to a parent CEDAR category
          String parentCategoryCedarId = null;
          if (existingCategoriesMap.containsKey(newCategory.getParentUniqueId())) {
            parentCategoryCedarId = existingCategoriesMap.get(newCategory.getParentUniqueId()).getCedarId();
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
              targetEnvironment, apiKey);
      categoryActionsProcessor.logActionsSummary();
      categoryActionsProcessor.executeCategoryActions();

      /* CDEs */

      // Read map with existing CDEs, if it exists. TODO: generate the map based on a CEDAR call
      Map<String, CdeSummary> existingCdesMap = new HashMap<>(); // Map cdeId (PublicId + "V" + Version)  -> CdeSummary
      File existingCdesMapFile = new File(EXISTING_CDES_MAP_FILE_PATH);
      if (existingCdesMapFile.exists()) {
        existingCdesMap = Collections.unmodifiableMap(objectMapper.readValue(existingCdesMapFile, new TypeReference<HashMap<String, CdeSummary>>() {}));
      }

      /*** Download and extract new CDEs ***/ // TODO: Read them using FTPClient once we have the FTP host
      FileUtils.deleteDirectory(new File(cdesUnzippedFolderPath));
      UnzipUtility.unzip(cdesZipFilePath, cdesUnzippedFolderPath);

      // Transform XML CDEs to CEDAR fields and upload them to CEDAR

      // Read data elements from xml files
      List<DataElement> newDataElements = new ArrayList<>();
      for (final File inputFile : new File(cdesUnzippedFolderPath).listFiles()) {
        logger.info("Processing CDEs file: " + inputFile.getAbsolutePath());
        DataElementsList newDataElementList = CdeUtil.getDataElementLists(new FileInputStream(inputFile));
        newDataElements.addAll(newDataElementList.getDataElement());
      }

      /* Check CDE changes */
      List<CreateCdeAction> createCdeActions = new ArrayList<>();
      List<UpdateCdeAction> updateCdeActions = new ArrayList<>();
      List<DeleteCdeAction> deleteCdeActions = new ArrayList<>();
      Map<String, CdeSummary> cdesToDeleteMap = new HashMap<>(existingCdesMap);
      int count = 0;
      for (DataElement newDataElement : newDataElements) {
        Map<String, Object> newCdeFieldMap = CdeUtil.getFieldMapFromDataElement(newDataElement);
        String newCdeHashCode = CdeUtil.generateCdeModifiedHashCode(newDataElement);
        String newCdeUniqueId = CdeUtil.generateCdeUniqueId(newCdeFieldMap);
        List<String> categoryCedarIds = CategoryUtil.extractCategoryCedarIdsFromCdeField(newCdeFieldMap,
            categoryIdsToCedarCategoryIds);
        // Check if the CDE is new or not
        if (existingCdesMap.containsKey(newCdeUniqueId)) {
          if (existingCdesMap.get(newCdeUniqueId).getHashCode().equals(newCdeHashCode)) {
            // The CDE exists in CEDAR and it didn't change. Do nothing.
          } else {
            // TODO: The CDE exists in CEDAR and it changed. We'll have to update it.
            updateCdeActions.add(new UpdateCdeAction());
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
      // TODO: The remaining CDEs in the map are not part of the new caDSR file. We'll delete them from CEDAR.
      for (CdeSummary cdeSummary : cdesToDeleteMap.values()) {
        deleteCdeActions.add(new DeleteCdeAction());
      }

      // Process CDE actions
      CdeActionsProcessor cdeActionsProcessor =
          new CdeActionsProcessor(createCdeActions, updateCdeActions, deleteCdeActions,
              new HashMap<>(existingCdesMap), targetEnvironment, apiKey);
      cdeActionsProcessor.logActionsSummary();
      cdeActionsProcessor.executeCdeActions();

      Map<String, CdeSummary> finalCdesMap = cdeActionsProcessor.getCdesMap();

      logger.info("Writing map of final CDEs to a file: " + EXISTING_CDES_MAP_FILE_PATH);
      objectMapper.writeValue(new File(EXISTING_CDES_MAP_FILE_PATH), finalCdesMap);

      // Save ontology
      ValueSetsOntologyManager.saveOntology(new File(ontologyFilePath));

    } catch (IOException | JAXBException e) {
      e.printStackTrace();
    }

  }

}
