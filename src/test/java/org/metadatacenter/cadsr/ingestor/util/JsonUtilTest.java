package org.metadatacenter.cadsr.ingestor.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** The tools read CEDAR's answers by picking single fields out of them, and refuse an answer that lacks one. */
class JsonUtilTest {

  private static final String ANSWER = "{\"@id\": \"cedar-1\", \"totalCount\": 2, \"resources\": [{\"a\": 1}, {\"a\": 2}]}";

  @Test
  void aFieldIsReadAsTextNumberOrList() throws Exception {
    assertEquals("cedar-1", JsonUtil.extractJsonFieldValueAsText(ANSWER, "@id"));
    assertEquals(2, JsonUtil.extractJsonFieldValueAsInt(ANSWER, "totalCount"));
    assertEquals(2, JsonUtil.extractJsonFieldValueAsNode(ANSWER, "resources").size());
    assertEquals(List.of(1, 2), JsonUtil.extractJsonFieldAsList(ANSWER, "resources").stream()
        .map(node -> node.get("a").asInt()).toList());
    assertEquals(Map.of("a", 1), JsonUtil.readJsonAsMap("{\"a\": 1}"));
  }

  @Test
  void aMissingFieldIsRefused() {
    assertThrows(RuntimeException.class, () -> JsonUtil.extractJsonFieldValueAsText(ANSWER, "missing"));
    assertThrows(RuntimeException.class, () -> JsonUtil.extractJsonFieldValueAsInt(ANSWER, "missing"));
    assertThrows(RuntimeException.class, () -> JsonUtil.extractJsonFieldValueAsNode(ANSWER, "missing"));
    assertThrows(RuntimeException.class, () -> JsonUtil.extractJsonFieldAsList(ANSWER, "@id"),
        "a field that is not a list is refused as a list");
  }
}
