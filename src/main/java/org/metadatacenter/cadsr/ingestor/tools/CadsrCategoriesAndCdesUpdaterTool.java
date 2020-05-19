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
import org.metadatacenter.cadsr.ingestor.util.*;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarEnvironment;
import org.metadatacenter.cadsr.ingestor.cde.CdeSummary;
import org.metadatacenter.cadsr.ingestor.cde.ValueSetsOntologyManager;
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
    //final String EXISTING_CATEGORIES_MAP_FILE_PATH = EXECUTION_RESOURCES_PATH + "/categoriesMap.json";
    final String EXISTING_CDES_MAP_FILE_PATH = EXECUTION_RESOURCES_PATH + "/cdesMap.json";

    final String categoriesFolderPath = EXECUTION_RESOURCES_PATH + "/categories";
    //final String categoriesZipFilePath = categoriesFolderPath + "/xml_cscsi_20204110528.zip";
    //final String categoriesZipFilePath = categoriesFolderPath + "/xml_cscsi_20204110528_modified.zip";
    //final String categoriesZipFilePath = categoriesFolderPath + "/xml_cscsi_20204110528_deleted.zip";
    final String categoriesZipFilePath = categoriesFolderPath + "/xml_cscsi_20204110528_deleted2.zip";
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
    boolean deleteCategoryTree = false;

    /*** OTHER CONSTANTS ***/

    try {

      if (deleteCategoryTree) {
        CedarServices.deleteCategoryTree(targetEnvironment, apiKey);
      }

      /*** Download and extract Categories ***/ // TODO: Read them using FTPClient once we have the FTP host
      FileUtils.deleteDirectory(new File(categoriesUnzippedFolderPath));
      UnzipUtility.unzip(categoriesZipFilePath, categoriesUnzippedFolderPath);

      /*** Download and extract CDEs ***/ // TODO: Read them using FTPClient once we have the FTP host
      FileUtils.deleteDirectory(new File(cdesUnzippedFolderPath));
      UnzipUtility.unzip(cdesZipFilePath, cdesUnzippedFolderPath);

      // Generate map with existing Categories based on the current Category Tree in CEDAR
      Map<String, CategorySummary> existingCategoriesMap = CategoryUtil.generateExistingCategoriesMap(targetEnvironment, apiKey);

      // Read map with existing CDEs, if it exists
      Map<String, CdeSummary> existingCdesMap = new HashMap<>();
      File existingCdesMapFile = new File(EXISTING_CDES_MAP_FILE_PATH);
      if (existingCdesMapFile.exists()) {
        existingCdesMap = objectMapper.readValue(existingCdesMapFile, new TypeReference<HashMap<String, CdeSummary>>() {});
      }

      /*** Read new XML caDSR classifications as Categories ***/
      File classificationsFile = (new File(categoriesUnzippedFolderPath)).listFiles()[0];
      Classifications classifications = CategoryUtil.getClassifications(new FileInputStream(classificationsFile));
      List<Category> currentCategories = CategoryUtil.classificationsToCategoriesList(classifications);

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
      List<CedarCategory> unchangedCategories = new ArrayList<>();
      Map<String, CategorySummary> existingCategoriesMapUpdated = new HashMap<>(existingCategoriesMap);
      for (Category currentCategory : currentCategories) {
        if (existingCategoriesMapUpdated.containsKey(currentCategory.getUniqueId())) {
          String existingCategoryCedarId = existingCategoriesMapUpdated.get(currentCategory.getUniqueId()).getCedarId();
          String newCategoryHash = CategoryUtil.generateCategoryHashCode(currentCategory);
          if (existingCategoriesMapUpdated.get(currentCategory.getUniqueId()).getHashCode().equals(newCategoryHash)) {
            unchangedCategories.add(currentCategory.toCedarCategory(existingCategoryCedarId, null));
          }
          else {
            updateCategoryActions.add(new UpdateCategoryAction(existingCategoryCedarId, currentCategory.toCategoryTreeNode()));
          }
        }
        else {
          String parentCategoryCedarId = null;
          // Note that here we access to the original (unmodified) map
          if (existingCategoriesMap.containsKey(currentCategory.getParentUniqueId())) {
            parentCategoryCedarId = existingCategoriesMap.get(currentCategory.getParentUniqueId()).getCedarId();
          }
          createCategoryActions.add(new CreateCategoryAction(currentCategory.toCategoryTreeNode(), parentCategoryCedarId));
        }
        // Update the map!
        existingCategoriesMapUpdated.remove(currentCategory.getUniqueId());
      }
      // The remaining categories will have to be deleted
      for (CategorySummary categorySummary : existingCategoriesMapUpdated.values()) {
        deleteCategoryActions.add(new DeleteCategoryAction(categorySummary.getCedarId()));
      }

      // Process actions
      CategoryActionsProcessor categoryActionsProcessor =
          new CategoryActionsProcessor(createCategoryActions, updateCategoryActions, deleteCategoryActions, targetEnvironment, apiKey);
      categoryActionsProcessor.logActionsSummary();
      categoryActionsProcessor.executeCategoryActions();

      /*** Upload JSON Category Tree to CEDAR ***/
//      List<CategoryTreeNode> categoryTreeNodes = CategoryUtil.classificationsToCategoryTree(classifications);
//      String rootCategoryId = CedarServices.getRootCategoryId(targetEnvironment, apiKey);
//      // Delete existing category tree
//      CedarServices.deleteCategoryTree(targetEnvironment, apiKey);
//      // Upload new tree
//      //CategoryUtil.uploadCategoriesFromFile(categoryTreeFile, rootCategoryId, targetEnvironment, apiKey);
//      for (CategoryTreeNode categoryTreeNode : categoryTreeNodes) {
//        CategoryUtil.uploadCategory(categoryTreeNode, rootCategoryId, targetEnvironment, apiKey);
//      }

      /*** Transform XML CDEs to CEDAR fields and upload them to CEDAR ***/

      // Read data elements from xml files
      List<DataElement> dataElements = new ArrayList<>();
      for (final File inputFile : new File(cdesUnzippedFolderPath).listFiles()) {
        logger.info("Processing CDEs file: " + inputFile.getAbsolutePath());
        DataElementsList dataElementList = CdeUtil.getDataElementLists(new FileInputStream(inputFile));
        dataElements.addAll(dataElementList.getDataElement());
      }

      // Delete fields in the CDE folder TODO: not working. It's not able to delete published fields
      //CedarServices.deleteAllFieldsInFolder(cedarFolderShortId, targetEnvironment, apiKey);

      // For each data element, transform it to a CEDAR field and upload it
      int count = 0;
      for (DataElement dataElement : dataElements) {
        Map<String, Object> fieldMap = CdeUtil.getFieldMapFromDataElement(dataElement);
        String templateFieldsEndpoint = CedarServerUtil.getTemplateFieldsEndpoint(cedarFolderShortId,
            targetEnvironment);
        String attachCategoriesEndpoint = CedarServerUtil.getAttachCategoriesEndpoint(targetEnvironment);

        String cdeId = CdeUtil.generateCdeId(dataElement.getPUBLICID().getContent(), dataElement.getVERSION().getContent());
        String cdeCedarId = CdeUploadUtil.uploadCde(fieldMap, true, categoryIdsToCedarCategoryIds, templateFieldsEndpoint,
            attachCategoriesEndpoint, apiKey);

        // Check if the CDE is new or not
        if (existingCdesMap.containsKey(cdeId)) {
          if (existingCdesMap.get(cdeId).getHashCode().equals(CdeUtil.generateCdeModifiedHashCode(dataElement))) {
            logger.info("****** THE CDE IS ALREADY THERE, nothing to do *******");
          }
          else {
            logger.info("****** THE CDE HAS BEEN UPDATED!, update it *******");
          }
        }
        else {
          logger.info("****** NEW CDE *******, create it");
        }


        logger.info("CDE uploaded: Id: " + cdeId + "; CEDAR Id: " + cdeCedarId);
        // Add to map of uploaded CDEs
        existingCdesMap.put(cdeId, new CdeSummary(cdeCedarId, CdeUtil.generateCdeModifiedHashCode(dataElement)));

        logger.info("Writing file");
        objectMapper.writeValue(new File(EXISTING_CDES_MAP_FILE_PATH), existingCdesMap);

        if (count++ == 5) {
          break;
        }
      }


      // Save ontology
      ValueSetsOntologyManager.saveOntology(new File(ontologyFilePath));

    } catch (IOException | JAXBException e) {
      e.printStackTrace();
    }

  }

}
