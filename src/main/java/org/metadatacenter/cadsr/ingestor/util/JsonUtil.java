package org.metadatacenter.cadsr.ingestor.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonUtil {

  private static ObjectMapper objectMapper = new ObjectMapper();

  public static String extractJsonFieldValue(String json, String fieldName) throws IOException {
    JsonNode node = objectMapper.readTree(json);
    if (node.has(fieldName)) {
      return node.get(fieldName).asText();
    } else {
      throw new RuntimeException("Json field not found in object: " + fieldName);
    }
  }

  public static List<JsonNode> extractJsonFieldAsList(String json, String fieldName) throws IOException {
    JsonNode node = objectMapper.readTree(json);
    List items = new ArrayList();
    if (node.has(fieldName) && node.get(fieldName).isArray()) {
      for (final JsonNode itemNode : node.get(fieldName)) {
        items.add(itemNode);
      }
    } else {
      throw new RuntimeException("Json field not found or is not an array: " + fieldName);
    }
    return items;
  }

}
