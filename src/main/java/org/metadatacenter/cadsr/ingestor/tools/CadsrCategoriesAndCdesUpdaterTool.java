package org.metadatacenter.cadsr.ingestor.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.metadatacenter.cadsr.cde.schema.DataElement;
import org.metadatacenter.cadsr.cde.schema.DataElementsList;
import org.metadatacenter.cadsr.ingestor.Util.*;
import org.metadatacenter.cadsr.ingestor.Util.Constants.CedarEnvironment;
import org.metadatacenter.cadsr.ingestor.cde.ValueSetsOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CadsrCategoriesAndCdesUpdaterTool {

  private static final Logger logger = LoggerFactory.getLogger(CadsrCategoriesAndCdesUpdaterTool.class);

  public static void main(String[] args) {

    /*** INPUTS ***/
    final String EXECUTION_RESOURCES_PATH = "/Users/marcosmr/Development/DEV_EXECUTIONS/2020" +
        "-05_cdes_upload_production_process";
    final String PREVIOUS_CDES_MAP_FILE_PATH = EXECUTION_RESOURCES_PATH + "/cdesMap.json";
    //private static final String PREVIOUS_CATEGORIES_MAP_FILE_PATH = EXECUTION_RESOURCES_PATH + "/categoriesMap.json";

    final String categoriesFolderPath = EXECUTION_RESOURCES_PATH + "/categories";
    final String categoriesZipFilePath = categoriesFolderPath + "/xml_cscsi_20204110528.zip";
    final String categoriesUnzippedFolderPath = categoriesFolderPath + "/unzipped";

    final String cdesFolderPath = EXECUTION_RESOURCES_PATH + "/cdes";
    final String cdesZipFilePath = cdesFolderPath + "/xml_cde_20205110558_lite.zip";
    final String cdesUnzippedFolderPath = cdesFolderPath + "/unzipped";

    // Id of the CEDAR folder where the contents will be uploaded
    final String cedarFolderShortId = "196dc02b-3aa5-4693-8c20-9b4e3b3fad20";

    // caDSR user's api key
    final String apiKey = "apiKey 58c4f22b9ea1548047682f3112f2f1bcedcb5e40443ddb5e6a11bda0629c2f20"; // In my local system I'm using the cedar-admin api key
    final CedarEnvironment targetEnvironment = CedarEnvironment.LOCAL;

    boolean reuploadCategories = false;

    /*** OUTPUTS ***/
    final String categoryTreeFolderPath = categoriesFolderPath + "/tree";
    final String ontologyFilePath = EXECUTION_RESOURCES_PATH + "/ontology/cadsr-vs.owl";


    /*** OTHER CONSTANTS ***/

    try {

      /*** Download and extract Categories ***/ // TODO: Read them using FTPClient once we have the FTP host
      FileUtils.deleteDirectory(new File(categoriesUnzippedFolderPath));
      UnzipUtility.unzip(categoriesZipFilePath, categoriesUnzippedFolderPath);

      /*** Download and extract CDEs ***/ // TODO: Read them using FTPClient once we have the FTP host
      FileUtils.deleteDirectory(new File(cdesUnzippedFolderPath));
      UnzipUtility.unzip(cdesZipFilePath, cdesUnzippedFolderPath);

      /*** Transform XML caDSR classifications to a CEDAR JSON category tree ***/
      File classificationsFile = (new File(categoriesUnzippedFolderPath)).listFiles()[0];
      File categoryTreeFolder = GeneralUtil.checkDirectoryExists(categoryTreeFolderPath);
      File categoryTreeFile = CategoryUtil.convertCdeCategoriesFromFile(classificationsFile, categoryTreeFolder);

      /*** Upload JSON Category Tree to CEDAR ***/
      if (reuploadCategories) {
        String rootCategoryId = CedarServices.getRootCategoryId(targetEnvironment, apiKey);
        // Delete existing category tree
        CedarServices.deleteCategoryTree(targetEnvironment, apiKey);
        CategoryUtil.uploadCategoriesFromFile(categoryTreeFile, rootCategoryId, targetEnvironment, apiKey);
      }

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
//      int count = 0;
//      for (DataElement dataElement : dataElements) {
//        Map<String, Object> fieldMap = CdeUtil.getFieldMapFromDataElement(dataElement);
//        String templateFieldsEndpoint = CedarServerUtil.getTemplateFieldsEndpoint(cedarFolderShortId, targetEnvironment);
//        String attachCategoriesEndpoint = CedarServerUtil.getAttachCategoriesEndpoint(targetEnvironment);
//        logger.info("Uploading CDE");
//        CdeUploadUtil.uploadCde(fieldMap, false, templateFieldsEndpoint, attachCategoriesEndpoint, apiKey);
//        if (count++ == 5) {
//          break;
//        }
//      }

      // Save ontology
      //ValueSetsOntologyManager.saveOntology(new File(ontologyFilePath));

//
//      String categoryEndPoint = CadsrCategoriesUtils.getRestEndpoint(resourceServerUrl);
//      CadsrCategoriesUtils.uploadCategoriesFromFile(categoryTreeFile, )

//      public static int uploadCategoriesFromFile(File inputFile, String cedarRootCategoryId, String endpoint,
//          String apiKey) throws IOException {

//      String inputSourceLocation = args[0];
//      String cedarRootCategoryId = args[1];
//      String targetServer = args[2];
//      String apiKey = args[3];

      /* 2. Generate CEDAR CDE Actions */

      // Read data elements from xml files
//      List<DataElement> dataElements = new ArrayList<>();
//      for (final File inputFile : new File(cdesUnzippedFilePath).listFiles()) {
//        logger.info("Processing CDEs file: " + inputFile.getAbsolutePath());
//        DataElementsList dataElementList = CadsrUtils.getDataElementLists(new FileInputStream(inputFile));
//        dataElements.addAll(dataElementList.getDataElement());
//      }
//
//      // Transform dataElements to CEDAR fields and upload them to CEDAR When the data element is transformed to a CEDAR field, the
//      // corresponding value set is added to the ontology
//      int count = 0;
//      for (DataElement dataElement : dataElements) {
//        Map<String, Object> fieldMap = CadsrUtils.getFieldMapFromDataElement(dataElement);
//        System.out.println(count++);
//      }

//      // Save ontology to file
//      ValueSetsOntologyManager.saveOntology(new File(ONTOLOGY_FILE_PATH));
//      // TODO: Move ontology to a public url where BioPortal can read it

      /*** Download and read categories and CDEs ***/
//    String classificationsFolderPath = "/Users/marcosmr/Desktop/tmp/2020-05_cdes_upload_production_process
//    /classifications/";
//    String classificationsZipFilePath = classificationsFolderPath + "/xml_cscsi_20204110528.zip";
//    String classifications
//      UnzipUtility.unzip(classificationsZipFilePath, classificationsFolderPath);
//      Classifications classifications = CadsrCategoriesUtils.getClassifications(new FileInputStream
//      (classificationsFile));
//      List<CategoryTreeNode> categoryTree = CadsrCategoriesUtils.classificationsToCategoryTree(classifications);
//    } catch (JAXBException e) {
//      e.printStackTrace();
    } catch (IOException | JAXBException e) {
      e.printStackTrace();
    }

  }

}
