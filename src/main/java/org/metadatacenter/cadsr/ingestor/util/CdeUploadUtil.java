package org.metadatacenter.cadsr.ingestor.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import org.metadatacenter.cadsr.ingestor.cde.ValueSetsOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.metadatacenter.cadsr.ingestor.util.Constants.CDE_CATEGORY_IDS_FIELD;
import static org.metadatacenter.cadsr.ingestor.util.Constants.CDE_VALUESETS_ONTOLOGY_NAME;

public class CdeUploadUtil {

  private static ObjectMapper objectMapper = new ObjectMapper();
  private static final Logger logger = LoggerFactory.getLogger(CdeUploadUtil.class);

  public static int uploadCdeFromDirectory(File inputDir, boolean attachCategories, Map<String, String> categoryIdsToCedarCategoryIds, String templateFieldsEndpoint,
                                            String attachCategoryEndpoint, String apiKey) throws IOException {
    int totalCdes = 0;
    for (final File inputFile : inputDir.listFiles()) {
      totalCdes += uploadCdeFromFile(inputFile, attachCategories, categoryIdsToCedarCategoryIds, templateFieldsEndpoint, attachCategoryEndpoint, apiKey);
    }
    return totalCdes;
  }

  public static int uploadCdeFromFile(File inputFile, boolean attachCategories, Map<String, String> categoryIdsToCedarCategoryIds, String templateFieldsEndpoint,
                                      String attachCategoryEndpoint, String apiKey) throws IOException {

    logger.info("Processing input file at " + inputFile.getAbsolutePath());
    Collection<Map<String, Object>> fieldMaps =
        CdeUtil.getFieldMapsFromInputStream(new FileInputStream(inputFile));

    int totalFields = fieldMaps.size();
    if (totalFields > 0) {
      int counter = 0;
      for (Map<String, Object> fieldMap : fieldMaps) {
        uploadCde(fieldMap, attachCategories, categoryIdsToCedarCategoryIds, templateFieldsEndpoint, attachCategoryEndpoint, apiKey);
      }
      logger.info(String.format("Uploading CDEs (%d/%d)", counter, totalFields));
    }
    return totalFields;
  }

  public static String uploadCde(Map<String, Object> fieldMap,
                               boolean attachCategories,
                               Map<String, String> categoryIdsToCedarCategoryIds,
                               String templateFieldsEndpoint,
                               String attachCategoryEndpoint,
                               String apiKey) {

    HttpURLConnection conn = null;
    String cedarCdeId = null;
    try {

      // Extract the categories from the map. They are not part of the CEDAR model so we don't want to post them
      List<String> categoryIds = (List) fieldMap.get(CDE_CATEGORY_IDS_FIELD);
      fieldMap.remove(CDE_CATEGORY_IDS_FIELD);

      String payload = objectMapper.writeValueAsString(fieldMap);
      conn = ConnectionUtil.createAndOpenConnection("POST", templateFieldsEndpoint, apiKey);
      OutputStream os = conn.getOutputStream();
      os.write(payload.getBytes());
      os.flush();
      int responseCode = conn.getResponseCode();
      if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
        ConnectionUtil.logErrorMessageAndThrowException("Error uploading CDE", conn);
      } else {
        // Read the CDE @id
        String response = ConnectionUtil.readResponseMessage(conn.getInputStream());
        cedarCdeId = JsonUtil.extractJsonFieldValue(response, "@id");
        if (attachCategories) {
          CedarServices.attachCdeToCategories(cedarCdeId, categoryIds, categoryIdsToCedarCategoryIds, attachCategoryEndpoint, apiKey);
        }
      }

    } catch (Exception e) {
      logger.error(e.toString());
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
    return cedarCdeId;
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
