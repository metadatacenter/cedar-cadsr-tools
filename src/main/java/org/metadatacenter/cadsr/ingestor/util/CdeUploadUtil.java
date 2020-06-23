package org.metadatacenter.cadsr.ingestor.util;

import com.google.common.io.Files;
import org.metadatacenter.cadsr.cde.schema.DataElement;
import org.metadatacenter.cadsr.cde.schema.DataElementsList;
import org.metadatacenter.cadsr.ingestor.cde.ValueSetsOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarEnvironment;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static org.metadatacenter.cadsr.ingestor.util.Constants.CDE_VALUESETS_ONTOLOGY_NAME;

public class CdeUploadUtil {

  private static final Logger logger = LoggerFactory.getLogger(CdeUploadUtil.class);

  public static void uploadCdeFromDirectory(File inputDir, String cedarFolderId,
                                            boolean attachCategories, Map<String, Set<String>> categoryCadsrIdsToCedarCategoryIds,
                                            CedarEnvironment cedarEnvironment, String apiKey)
      throws IOException, JAXBException {

    for (final File inputFile : inputDir.listFiles()) {
      uploadCdeFromFile(inputFile, cedarFolderId, attachCategories, categoryCadsrIdsToCedarCategoryIds, cedarEnvironment, apiKey);
    }
  }

  public static void uploadCdeFromFile(File inputFile, String cedarFolderId,
                                       boolean attachCategories, Map<String, Set<String>> categoryCadsrIdsToCedarCategoryIds,
                                       CedarEnvironment cedarEnvironment, String apiKey) throws IOException, JAXBException {

    logger.info("Processing input file at " + inputFile.getAbsolutePath());

    DataElementsList dataElementsList = CdeUtil.getDataElementLists(new FileInputStream(inputFile));

    for (DataElement dataElement : dataElementsList.getDataElement()) {
      String hashCode = CdeUtil.generateCdeHashCode(dataElement);
      Optional<List<String>> cedarCategoryIds = Optional.empty();
      Map<String, Object> fieldMap = CdeUtil.getFieldMapFromDataElement(dataElement);
      if (attachCategories) {
        cedarCategoryIds = Optional.of(CategoryUtil.extractCategoryCedarIdsFromCdeField(fieldMap, categoryCadsrIdsToCedarCategoryIds));
      }
      CedarServices.createCde(fieldMap, hashCode, cedarFolderId, cedarCategoryIds, cedarEnvironment, apiKey);
    }
  }

  private static boolean multiplesOfAHundred(int counter) {
    return counter != 0 && counter % 100 == 0;
  }

  public static void storeOntologyInTempDir() {
    File outputTempDir = Files.createTempDir();
    File outputOntologyFile = new File(outputTempDir, CDE_VALUESETS_ONTOLOGY_NAME);
    logger.info("Storing the generated value set ontology at " + outputOntologyFile);
    ValueSetsOntologyManager.saveOntology(outputOntologyFile);
  }

}
