package org.metadatacenter.cadsr.ingestor.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import org.metadatacenter.cadsr.ingestor.cde.ValueSetsOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarEnvironment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.metadatacenter.cadsr.ingestor.util.Constants.CDE_VALUESETS_ONTOLOGY_NAME;

public class CdeUploadUtil {

  private static final Logger logger = LoggerFactory.getLogger(CdeUploadUtil.class);

  public static void uploadCdeFromDirectory(File inputDir, String cedarFolderShortId,
                                            boolean attachCategories, Map<String, String> categoryIdsToCedarCategoryIds, CedarEnvironment cedarEnvironment, String apiKey) throws IOException {

    for (final File inputFile : inputDir.listFiles()) {
      uploadCdeFromFile(inputFile, cedarFolderShortId, attachCategories, categoryIdsToCedarCategoryIds, cedarEnvironment, apiKey);
    }
  }

  public static void uploadCdeFromFile(File inputFile, String cedarFolderShortId,
                                       boolean attachCategories, Map<String, String> categoryIdsToCedarCategoryIds,
                                       CedarEnvironment cedarEnvironment, String apiKey) throws IOException {

    logger.info("Processing input file at " + inputFile.getAbsolutePath());
    Collection<Map<String, Object>> fieldMaps =
        CdeUtil.getFieldMapsFromInputStream(new FileInputStream(inputFile));

    int totalFields = fieldMaps.size();
    if (totalFields > 0) {
      for (Map<String, Object> fieldMap : fieldMaps) {
        Optional<List<String>> cedarCategoryIds = Optional.empty();
        if (attachCategories) {
          cedarCategoryIds = Optional.of(CategoryUtil.extractCategoryCedarIdsFromCdeField(fieldMap, categoryIdsToCedarCategoryIds));
        }
        CedarServices.createCde(fieldMap, cedarFolderShortId, cedarCategoryIds, cedarEnvironment, apiKey);
      }
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
