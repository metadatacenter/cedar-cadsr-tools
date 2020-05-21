package org.metadatacenter.cadsr.ingestor.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.metadatacenter.cadsr.ingestor.category.CategoryTreeNode;
import org.metadatacenter.cadsr.ingestor.category.CedarCategory;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarEnvironment;
import org.metadatacenter.server.neo4j.cypher.NodeProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.*;

import static org.metadatacenter.cadsr.ingestor.util.Constants.*;
import static org.metadatacenter.model.ModelNodeNames.*;

public class CedarServices {

  private static final Logger logger = LoggerFactory.getLogger(CdeUploadUtil.class);
  private static ObjectMapper objectMapper = new ObjectMapper();

  /*** Field services ***/

  public static void deleteAllFieldsInFolder(String folderShortId, CedarEnvironment environment, String apiKey) throws IOException {
    List<String> fieldIds = findFieldsInFolder(folderShortId, environment, apiKey);
    for (String fieldId : fieldIds) {
      deleteField(fieldId, environment, apiKey);
    }
  }

  // Returns the @ids of all the CEDAR fields in the given folder
  public static List<String> findFieldsInFolder(String folderShortId, CedarEnvironment environment, String apiKey) throws IOException {
    String endpoint = CedarServerUtil.getFolderContentsEndPoint(folderShortId, environment);
    HttpURLConnection connection = ConnectionUtil.createAndOpenConnection("GET", endpoint, apiKey);
    int responseCode = connection.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_OK) {
      String response = ConnectionUtil.readResponseMessage(connection.getInputStream());
      connection.disconnect();
      List<JsonNode> resources = JsonUtil.extractJsonFieldAsList(response, "resources");
      List<String> fieldIds = new ArrayList<>();
      for (JsonNode resource : resources) {
        if (resource.get("resourceType").asText().equals("field")) {
          fieldIds.add(resource.get(JSON_LD_ID).asText());
        }
      }
      return fieldIds;
    } else {
      logger.error("Error retrieving root category id");
      throw new InternalError("Error retrieving root category id");
    }
  }

  public static void deleteField(String fieldId, CedarEnvironment environment, String apiKey) throws IOException {
    String endpointDelete = CedarServerUtil.getTemplateFieldEndPoint(fieldId, environment);
    HttpURLConnection connection = ConnectionUtil.createAndOpenConnection("DELETE", endpointDelete, apiKey);
    int responseCode = connection.getResponseCode();
    if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
      String message = "Error deleting field: " + ConnectionUtil.readResponseMessage(connection.getInputStream());
      logger.error(message);
      throw new InternalError(message);
    }
    connection.disconnect();
  }


  /*** CDE Services ***/

  public static String createCde(Map<String, Object> cdeFieldMap, String cedarFolderShortId,
                                 Optional<List<String>> cedarCategoryIds, CedarEnvironment cedarEnvironment,
                                 String apiKey) {

    HttpURLConnection conn = null;
    String cedarCdeId = null;
    try {

      String templateFieldsEndpoint = CedarServerUtil.getTemplateFieldsEndpoint(cedarFolderShortId, cedarEnvironment);

      // Extract the categories from the map if they are still there. They are not part of the CEDAR model so we
      // don't want to post them
      if (cdeFieldMap.containsKey(CDE_CATEGORY_IDS_FIELD)) {
        cdeFieldMap.remove(CDE_CATEGORY_IDS_FIELD);
      }

      String payload = objectMapper.writeValueAsString(cdeFieldMap);
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
        cedarCdeId = JsonUtil.extractJsonFieldValue(response, JSON_LD_ID);

        // Optionally, attach CDE to categories
        if (cedarCategoryIds.isPresent()) {
          String attachCategoriesEndpoint = CedarServerUtil.getAttachCategoriesEndpoint(cedarEnvironment);
          CedarServices.attachCdeToCategories(cedarCdeId, cedarCategoryIds.get(), attachCategoriesEndpoint, apiKey);
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

  /*** Category Services ***/

  public static String getRootCategoryId(CedarEnvironment targetEnvironment, String apiKey) throws IOException {
    String endpoint = CedarServerUtil.getRootCategoryRestEndpoint(targetEnvironment);
    HttpURLConnection connection = ConnectionUtil.createAndOpenConnection("GET", endpoint, apiKey);
    int responseCode = connection.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_OK) {
      String response = ConnectionUtil.readResponseMessage(connection.getInputStream());
      connection.disconnect();
      return JsonUtil.extractJsonFieldValue(response, "@id");
    } else {
      logger.error("Error retrieving root category id");
      throw new InternalError("Error retrieving root category id");
    }
  }

  public static void deleteCategory(String categoryId, CedarEnvironment targetEnvironment, String apiKey) throws IOException {
    String endpoint = CedarServerUtil.getCategoryRestEndpoint(categoryId, targetEnvironment);
    HttpURLConnection connection = ConnectionUtil.createAndOpenConnection("DELETE", endpoint, apiKey);
    int responseCode = connection.getResponseCode();
    if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
      String message = "Error deleting category (" + categoryId + "): " +
          ConnectionUtil.readResponseMessage(connection.getInputStream());
      logger.error(message);
      throw new InternalError(message);
    }
    connection.disconnect();
  }


  // Deletes all the categories and their relationships to CDEs, except the root category
  public static void deleteCategoryTree(CedarEnvironment targetEnvironment, String apiKey) throws IOException {
    String endpoint = CedarServerUtil.getCategoryTreeEndpoint(targetEnvironment);
    HttpURLConnection connection = ConnectionUtil.createAndOpenConnection("DELETE", endpoint, apiKey);
    int responseCode = connection.getResponseCode();
    if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
      String message = "Error deleting category tree: " +
          ConnectionUtil.readResponseMessage(connection.getInputStream());
      logger.error(message);
      throw new InternalError(message);
    }
    connection.disconnect();
  }

  /**
   * Uploads a category to CEDAR, including its children
   *
   * @param category
   * @param cedarParentCategoryId
   * @param environment
   * @param apiKey
   */
  public static void createCategory(CategoryTreeNode category, String cedarParentCategoryId,
                                    CedarEnvironment environment, String apiKey) {

    Map<String, String> categoryFieldsMap = new HashMap<>();
    categoryFieldsMap.put(SCHEMA_ORG_IDENTIFIER, category.getUniqueId());
    categoryFieldsMap.put(SCHEMA_ORG_NAME, category.getName());
    categoryFieldsMap.put(SCHEMA_ORG_DESCRIPTION, category.getDescription());
    categoryFieldsMap.put(NodeProperty.PARENT_CATEGORY_ID.getValue(), cedarParentCategoryId);

    HttpURLConnection conn = null;
    try {
      Thread.sleep(50);
      logger.info("Trying to upload: " + category.getUniqueId());
      String payload = objectMapper.writeValueAsString(categoryFieldsMap);
      String url = CedarServerUtil.getCategoriesRestEndpoint(environment);
      conn = ConnectionUtil.createAndOpenConnection("POST", url, apiKey);
      OutputStream os = conn.getOutputStream();
      os.write(payload.getBytes());
      os.flush();
      int responseCode = conn.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_CREATED) {
        logger.error("Error creating category: " + category.getUniqueId());
        GeneralUtil.logErrorMessage(conn);
      } else {
        logger.info("Category created: " + payload);
        String response = ConnectionUtil.readResponseMessage(conn.getInputStream());
        String cedarCategoryId = JsonUtil.extractJsonFieldValue(response, "@id");
        //logger.info(String.format("Uploading categories (%d/%d)", counter, allCategories.size()));
        for (CategoryTreeNode categoryTreeNode : category.getChildren()) {
          createCategory(categoryTreeNode, cedarCategoryId, environment, apiKey);
        }
      }
    } catch (JsonProcessingException e) {
      logger.error(e.toString());
    } catch (IOException e) {
      logger.error(e.toString());
    } catch (InterruptedException e) {
      logger.error(e.getMessage());
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

  public static Map<String, String> getCategoryIdsToCedarCategoryIdsMap(Constants.CedarEnvironment targetEnvironment, String apiKey) throws IOException {
    String endpoint = CedarServerUtil.getCategoryTreeEndpoint(targetEnvironment);
    Map<String, String> categoryIdsMap = null;
    HttpURLConnection conn = null;
    try {
      conn = ConnectionUtil.createAndOpenConnection("GET", endpoint, apiKey);
      int responseCode = conn.getResponseCode();
      if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
        GeneralUtil.logErrorMessage(conn);
      } else {
        // Read the CDE @id
        String response = ConnectionUtil.readResponseMessage(conn.getInputStream());
        JsonNode categoryTree = objectMapper.readTree(response);
        categoryIdsMap = CategoryUtil.getCategoryIdsFromCategoryTree(categoryTree);
      }
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
    return categoryIdsMap;
  }

  public static CedarCategory getCedarCategoryTree(Constants.CedarEnvironment targetEnvironment, String apiKey) throws IOException {
    String endpoint = CedarServerUtil.getCategoryTreeEndpoint(targetEnvironment);
    HttpURLConnection conn = null;
    CedarCategory categoryTree = null;
    try {
      conn = ConnectionUtil.createAndOpenConnection("GET", endpoint, apiKey);
      int responseCode = conn.getResponseCode();
      if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
        GeneralUtil.logErrorMessage(conn);
      } else {
        String response = ConnectionUtil.readResponseMessage(conn.getInputStream());
        categoryTree = objectMapper.readValue(response, CedarCategory.class);
      }
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
    return categoryTree;
  }

  public static void updateCategory(String categoryCedarId, CategoryTreeNode category, CedarEnvironment environment,
                                    String apiKey) {

    Map<String, String> categoryFieldsMap = new HashMap<>();
    categoryFieldsMap.put(SCHEMA_ORG_IDENTIFIER, category.getUniqueId());
    categoryFieldsMap.put(SCHEMA_ORG_NAME, category.getName());
    categoryFieldsMap.put(SCHEMA_ORG_DESCRIPTION, category.getDescription());

    HttpURLConnection conn = null;
    try {
      String payload = objectMapper.writeValueAsString(categoryFieldsMap);
      String url = CedarServerUtil.getCategoryRestEndpoint(categoryCedarId, environment);
      conn = ConnectionUtil.createAndOpenConnection("PUT", url, apiKey);
      OutputStream os = conn.getOutputStream();
      os.write(payload.getBytes());
      os.flush();
      int responseCode = conn.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_OK) {
        logger.error("Error updating category: " + category.getUniqueId());
        GeneralUtil.logErrorMessage(conn);
      }
    } catch (JsonProcessingException e) {
      logger.error(e.toString());
    } catch (IOException e) {
      logger.error(e.toString());
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

  /*** CDE-Category Services ***/

  /**
   * Attach a CDE to multiple categories (making a single REST call)
   */
  public static void attachCdeToCategories(String cedarCdeId, List<String> cedarCategoryIds,
                                           String endpoint,
                                           String apiKey) {

    logger.info("Attaching CDE to the following categories: " + String.join(", ", cedarCategoryIds));

    if (!cedarCategoryIds.isEmpty()) {

      ObjectNode node = objectMapper.createObjectNode();
      node.put(CEDAR_CATEGORY_ATTACH_ARTIFACT_ID, cedarCdeId);
      ArrayNode array = objectMapper.valueToTree(cedarCategoryIds);
      node.putArray(CEDAR_CATEGORY_ATTACH_CATEGORY_IDS).addAll(array);

      HttpURLConnection conn = null;
      try {
        String payload = objectMapper.writeValueAsString(node);
        conn = ConnectionUtil.createAndOpenConnection("POST", endpoint, apiKey);
        OutputStream os = conn.getOutputStream();
        os.write(payload.getBytes());
        os.flush();
        int responseCode = conn.getResponseCode();
        if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
          GeneralUtil.logErrorMessage(conn);
        } else {
          logger.info("CDE attached successfully to categories");
        }
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      } catch (IOException e) {
        logger.error(e.toString());
      } finally {
        if (conn != null) {
          conn.disconnect();
        }
      }
    } else {
      String message = "No category ids provided";
      logger.error(message);
      throw new IllegalArgumentException(message);
    }
  }

}
