package org.metadatacenter.cadsr.ingestor.Util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import org.metadatacenter.cadsr.cde.schema.DataElement;
import org.metadatacenter.cadsr.cde.schema.DataElementsList;
import org.metadatacenter.cadsr.ingestor.cde.CadsrTransformationStats;
import org.metadatacenter.cadsr.ingestor.cde.ValueSetsOntologyManager;
import org.metadatacenter.cadsr.ingestor.cde.handler.*;
import org.metadatacenter.cadsr.ingestor.exception.UnknownSeparatorException;
import org.metadatacenter.cadsr.ingestor.exception.UnsupportedDataElementException;
import org.metadatacenter.model.ModelNodeNames;
import org.metadatacenter.model.ModelNodeValues;
import org.metadatacenter.cadsr.ingestor.Util.Constants.CedarEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.HttpURLConnection;
import java.util.*;

import static org.metadatacenter.cadsr.ingestor.Util.Constants.CDE_CATEGORY_IDS_FIELD;
import static org.metadatacenter.cadsr.ingestor.Util.Constants.CDE_VALUESETS_ONTOLOGY_NAME;
import static org.metadatacenter.model.ModelNodeNames.JSON_LD_ID;

public class CdeUploadUtil {

  private static ObjectMapper objectMapper = new ObjectMapper();
  private static final Logger logger = LoggerFactory.getLogger(CdeUploadUtil.class);

  public static int uploadCdeFromDirectory(File inputDir, boolean attachCategories, String templateFieldsEndpoint,
                                            String attachCategoryEndpoint, String apiKey) throws IOException {
    int totalCdes = 0;
    for (final File inputFile : inputDir.listFiles()) {
      totalCdes += uploadCdeFromFile(inputFile, attachCategories, templateFieldsEndpoint, attachCategoryEndpoint, apiKey);
    }
    return totalCdes;
  }

  public static int uploadCdeFromFile(File inputFile, boolean attachCategories, String templateFieldsEndpoint,
                                      String attachCategoryEndpoint, String apiKey) throws IOException {

    logger.info("Processing input file at " + inputFile.getAbsolutePath());
    Collection<Map<String, Object>> fieldMaps =
        CdeUtil.getFieldMapsFromInputStream(new FileInputStream(inputFile));

    int totalFields = fieldMaps.size();
    if (totalFields > 0) {
      int counter = 0;
      for (Map<String, Object> fieldMap : fieldMaps) {
        uploadCde(fieldMap, attachCategories, templateFieldsEndpoint, attachCategoryEndpoint, apiKey);
      }
      logger.info(String.format("Uploading CDEs (%d/%d)", counter, totalFields));
    }
    return totalFields;
  }

  public static void uploadCde(Map<String, Object> fieldMap,
                               boolean attachCategories,
                               String templateFieldsEndpoint,
                               String attachCategoryEndpoint, String apiKey) {

    HttpURLConnection conn = null;
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
        logErrorMessage(conn);
      } else {
        //logger.info("CDE uploaded: " + payload);
        if (attachCategories) {
          // Read the CDE @id
          String response = ConnectionUtil.readResponseMessage(conn.getInputStream());
          String cedarCdeId = JsonUtil.extractJsonFieldValue(response, "@id");
          CedarServices.attachCdeToCategories(cedarCdeId, categoryIds, attachCategoryEndpoint, apiKey);
        }
      }

    } catch (Exception e) {
      logger.error(e.toString());
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

  private static void logErrorMessage(final HttpURLConnection conn) {
    String response = ConnectionUtil.readResponseMessage(conn.getErrorStream());
    logger.error(response);
    throw new RuntimeException("Unable to upload CDE. Reason:\n" + response);
  }

  private static void logResponseMessage(final HttpURLConnection conn) throws IOException {
    String response = ConnectionUtil.readResponseMessage(conn.getInputStream());
    String message = createMessageBasedOnFieldNameAndId(response);
    logger.debug("POST 200 OK: " + message);
  }

  private static String createMessageBasedOnFieldNameAndId(String response) throws IOException {
    JsonNode responseNode = objectMapper.readTree(response);
    String fieldName = responseNode.get("schema:name").asText();
    String fieldId = responseNode.get("@id").asText();
    return String.format("%s (ID: %s)", fieldName, fieldId);
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
