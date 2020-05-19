package org.metadatacenter.cadsr.ingestor.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.metadatacenter.cadsr.ingestor.category.BasicCedarCategory;
import org.metadatacenter.cadsr.ingestor.category.CategoryTreeNode;
import org.metadatacenter.cadsr.ingestor.category.CedarCategory;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarEnvironment;
import org.metadatacenter.rest.assertion.noun.CedarParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.metadatacenter.cadsr.ingestor.util.Constants.*;
import static org.metadatacenter.model.ModelNodeNames.JSON_LD_ID;

public class CedarServices {

  private static final Logger logger = LoggerFactory.getLogger(CdeUploadUtil.class);
  private static ObjectMapper objectMapper = new ObjectMapper();

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
      String message = "Error deleting category (" + categoryId + "): "+
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
   * @param category
   * @param cedarParentCategoryId
   * @param environment
   * @param apiKey
   */
  public static void createCategory(CategoryTreeNode category, String cedarParentCategoryId,
                                    CedarEnvironment environment, String apiKey) {

    CedarCategory cedarCategory =
        new CedarCategory(null, category.getUniqueId(), category.getName(), category.getDescription(),
            cedarParentCategoryId, null);

    HttpURLConnection conn = null;
    try {
      Thread.sleep(50);
      logger.info("Trying to upload: " + category.getUniqueId());
      String payload = objectMapper.writeValueAsString(cedarCategory);
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

  public static Map<String, String> getCedarCategoryIds(Constants.CedarEnvironment targetEnvironment, String apiKey) throws IOException {
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

  public static void updateCategory(String categoryCedarId, BasicCedarCategory category, CedarEnvironment environment,
                                    String apiKey) {
    HttpURLConnection conn = null;
    try {
      logger.info("Trying to upload: " + category.getId());
      String payload = objectMapper.writeValueAsString(category);
      String url = CedarServerUtil.getCategoryRestEndpoint(categoryCedarId, environment);
      conn = ConnectionUtil.createAndOpenConnection("PUT", url, apiKey);
      OutputStream os = conn.getOutputStream();
      os.write(payload.getBytes());
      os.flush();
      int responseCode = conn.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_OK) {
        logger.error("Error updating category: " + category.getId());
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

  /**
   *
   * Attach a CDE to multiple categories (making multiple REST calls)
   *
   * @param cedarCdeId: CEDAR CDE id
   * @param categoryIds: list of cadsr category Ids (not CEDAR  ids)
   */
  public static void attachCdeToCategoriesMultipleCalls(String cedarCdeId, List<String> categoryIds, String endpoint,
                                                        Map<String, String> categoryIdsToCedarCategoryIds,
                                                        String apiKey) {

    for (String categoryId : categoryIds) {

      if (categoryIdsToCedarCategoryIds.containsKey(categoryId)) {

        String cedarCategoryId = categoryIdsToCedarCategoryIds.get(categoryId);

        ObjectNode node = objectMapper.createObjectNode();
        node.put(CEDAR_CATEGORY_ATTACH_ARTIFACT_ID, cedarCdeId);
        node.put(CEDAR_CATEGORY_ATTACH_CATEGORY_ID, cedarCategoryId);

        HttpURLConnection conn = null;
        try {
          String payload = objectMapper.writeValueAsString(node);
          logger.info("Attaching CDE to category: " + payload);
          conn = ConnectionUtil.createAndOpenConnection("POST", endpoint, apiKey);
          OutputStream os = conn.getOutputStream();
          os.write(payload.getBytes());
          os.flush();
          int responseCode = conn.getResponseCode();
          if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
            GeneralUtil.logErrorMessage(conn);
          } else {
            GeneralUtil.logResponseMessage(conn);
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
      }
      else {
        logger.error("Could not find CEDAR Id in map for category id: " + categoryId);
      }
    }
  }

  /**
   * Attach a CDE to multiple categories (making a single REST call)
   *
   * @param cedarCdeId:  CEDAR CDE id
   * @param categoryIds: list of cadsr category Ids (not CEDAR ids)
   */
  public static void attachCdeToCategories(String cedarCdeId, List<String> categoryIds,
                                           Map<String, String> categoryIdsToCedarCategoryIds,
                                           String endpoint,
                                           String apiKey) {

    List<String> cedarCategoryIds = new ArrayList<>();
    for (String categoryId : categoryIds) {
      if (categoryIdsToCedarCategoryIds.containsKey(categoryId)) {
        cedarCategoryIds.add(categoryIdsToCedarCategoryIds.get(categoryId));
      }
      else {
        logger.error("Could not find CEDAR Id in map for category id: " + categoryId);
      }
    }

    logger.info("Attaching CDE to the following categories: " + String.join(", ", cedarCategoryIds));

    if (!cedarCategoryIds.isEmpty()) {

      ObjectNode node = objectMapper.createObjectNode();
      node.put(CEDAR_CATEGORY_ATTACH_ARTIFACT_ID, cedarCdeId);
      ArrayNode array = objectMapper.valueToTree(cedarCategoryIds);
      node.putArray(CEDAR_CATEGORY_ATTACH_CATEGORY_IDS).addAll(array);

      HttpURLConnection conn = null;
      try {
        String payload = objectMapper.writeValueAsString(node);
        logger.info("Attaching CDE to category: " + payload);
        conn = ConnectionUtil.createAndOpenConnection("POST", endpoint, apiKey);
        OutputStream os = conn.getOutputStream();
        os.write(payload.getBytes());
        os.flush();
        int responseCode = conn.getResponseCode();
        if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
          GeneralUtil.logErrorMessage(conn);
        } else {
          GeneralUtil.logResponseMessage(conn);
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

    }
  }




}
