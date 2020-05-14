package org.metadatacenter.cadsr.ingestor.tools;

import com.google.common.base.Stopwatch;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.metadatacenter.cadsr.category.schema.Classifications;
import org.metadatacenter.cadsr.cde.schema.DataElement;
import org.metadatacenter.cadsr.cde.schema.DataElementsList;
import org.metadatacenter.cadsr.ingestor.UnzipUtility;
import org.metadatacenter.cadsr.ingestor.Util;
import org.metadatacenter.cadsr.ingestor.category.CadsrCategoriesUtils;
import org.metadatacenter.cadsr.ingestor.category.CategoryTreeNode;
import org.metadatacenter.cadsr.ingestor.cde.CadsrUtils;
import org.metadatacenter.cadsr.ingestor.cde.ValueSetsOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
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

    final String cedarCadsrUserApiKey = "";


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
      File categoryTreeFolder = Util.checkDirectoryExists(categoryTreeFolderPath);
      CadsrCategoriesUtils.convertCdeCategoriesFromFile(classificationsFile, categoryTreeFolder);

      /*** Upload JSON Category Tree to CEDAR ***/
      String inputSourceLocation = args[0];
      String cedarRootCategoryId = args[1];
      String targetServer = args[2];
      String apiKey = args[3];

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
//
//
//
//
//        System.out.println(count++);
//      }
//
//
//
//
//
//      // Save ontology to file
//      ValueSetsOntologyManager.saveOntology(new File(ONTOLOGY_FILE_PATH));
//      // TODO: Move ontology to a public url where BioPortal can read it


      /*** Download and read categories and CDEs ***/
//    String classificationsFolderPath = "/Users/marcosmr/Desktop/tmp/2020-05_cdes_upload_production_process
//    /classifications/";
//    String classificationsZipFilePath = classificationsFolderPath + "/xml_cscsi_20204110528.zip";
//    String classifications
//
//
//
//
//      UnzipUtility.unzip(classificationsZipFilePath, classificationsFolderPath);
//      Classifications classifications = CadsrCategoriesUtils.getClassifications(new FileInputStream
//      (classificationsFile));
//      List<CategoryTreeNode> categoryTree = CadsrCategoriesUtils.classificationsToCategoryTree(classifications);
//    } catch (JAXBException e) {
//      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }


//    String cdesFilePath = "/Users/marcosmr/Dropbox/01.INVESTIGACION/\\[2020\\]/2020\\ -\\ NCI\\ CDEs\\ " +
//        "Project/2020-05\\ Issue\\ 9\\ -\\ Production\\ Process\\ for\\ Updating\\ CDEs/CDEs\\ and\\ " +
//        "Classifications/cdes/xml_cde_20205110558";


  }

}
