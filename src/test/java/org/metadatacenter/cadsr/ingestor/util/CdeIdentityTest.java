package org.metadatacenter.cadsr.ingestor.util;

import org.junit.jupiter.api.Test;
import org.metadatacenter.cadsr.cde.schema.DataElement;
import org.metadatacenter.cadsr.cde.schema.DataElementsList;
import org.metadatacenter.cadsr.ingestor.cde.CdeStats;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The updater matches each CDE in NCI's file with the CDE CEDAR already holds by public identifier and version, and
 * tells whether it changed by a hash of the field it would create. A CDE read from the file and the same CDE read
 * back from CEDAR must therefore produce the same key and the same hash, or every run creates every CDE again.
 */
class CdeIdentityTest {

  private static final List<String> SAMPLES = List.of("5873923", "2182451", "3017383", "2752716", "2001826");

  @Test
  void everyCdeInTheFileBecomesAFieldOrIsReportedAsSkipped() throws Exception {
    DataElementsList list = CdeUtil.getDataElementLists(sampleList());
    assertEquals(SAMPLES.size(), list.getDataElement().size());

    Collection<Map<String, Object>> fields = CdeUtil.getFieldMapsFromInputStream(sampleList());
    List<String> fieldKeys = new ArrayList<>();
    for (Map<String, Object> field : fields) {
      fieldKeys.add(CdeUtil.generateCdeUniqueId(field));
    }
    for (DataElement element : list.getDataElement()) {
      String key = CdeUtil.generateCdeUniqueId(element);
      assertTrue(fieldKeys.contains(key) || CdeStats.getInstance().skippedIds.contains(key)
          || CdeStats.getInstance().failedIds.contains(key), key + " should be a field or be reported");
    }
    assertTrue(fieldKeys.contains("5873923V1.0.0"), "a field is keyed as its data element is: " + fieldKeys);
  }

  @Test
  void aVersionIsWrittenWithThreeParts() {
    assertEquals("1.0.0", CdeUtil.reformatVersioningNumber("1"));
    assertEquals("2.31.0", CdeUtil.reformatVersioningNumber("2.31"));
    assertEquals("1.2.3", CdeUtil.reformatVersioningNumber("1.2.3"));
    assertEquals("2188100V1.0.0", CdeUtil.generateCdeUniqueId("2188100", "1.0.0"));
  }

  /**
   * CEDAR adds an identifier, provenance and the categories the CDE is attached to. None of them is the CDE's own
   * content, so none of them changes its hash, and a change to its content does.
   */
  @Test
  void theHashIgnoresWhatCedarAdds() throws Exception {
    DataElement element = firstElement();
    String hash = CdeUtil.generateCdeHashCode(element);
    assertEquals(hash, CdeUtil.generateCdeHashCode(firstElement()), "the hash is stable across reads");

    Map<String, Object> stored = new HashMap<>(CdeUtil.getFieldMapFromDataElement(element));
    stored.put("@id", "https://repo.metadatacenter.orgx/template-fields/f");
    stored.put("pav:createdOn", "2026-10-19T00:00:00-07:00");
    stored.put("pav:createdBy", "https://metadatacenter.org/users/u");
    stored.put("pav:lastUpdatedOn", "2026-10-19T00:00:00-07:00");
    stored.put("oslc:modifiedBy", "https://metadatacenter.org/users/u");
    stored.put(Constants.CDE_CATEGORY_IDS_FIELD, List.of("c"));
    assertEquals(hash, CdeUtil.generateCdeHashCode(stored));

    Map<String, Object> changed = new HashMap<>(CdeUtil.getFieldMapFromDataElement(element));
    changed.put("schema:description", "A different definition");
    assertNotEquals(hash, CdeUtil.generateCdeHashCode(changed));
  }

  /**
   * The parser reads each CDE once and answers later reads from a cache. A caller that removes keys from the map it
   * was given, as hashing and creating a CDE both do, must not remove them from what a later read finds.
   */
  @Test
  void aCallersChangeDoesNotReachTheNextRead() throws Exception {
    DataElement element = firstElement();
    Map<String, Object> first = CdeUtil.getFieldMapFromDataElement(element);
    CdeUtil.generateCdeHashCode(first);
    first.remove(Constants.CDE_CATEGORY_IDS_FIELD);

    Map<String, Object> again = CdeUtil.getFieldMapFromDataElement(element);
    assertTrue(again.containsKey(Constants.CDE_CATEGORY_IDS_FIELD), "the categories survive: " + again.keySet());
    assertTrue(again.containsKey("@id"), "the identifier key survives: " + again.keySet());
  }

  private DataElement firstElement() throws Exception {
    try (InputStream in = getClass().getResourceAsStream("/cde-sample-" + SAMPLES.get(0) + ".xml")) {
      return CdeUtil.getDataElement(in);
    }
  }

  /** NCI publishes CDEs as lists. This one is assembled from the single-CDE samples. */
  private InputStream sampleList() throws Exception {
    StringBuilder xml = new StringBuilder("<DataElementsList>");
    for (String sample : SAMPLES) {
      try (InputStream in = getClass().getResourceAsStream("/cde-sample-" + sample + ".xml")) {
        xml.append(new String(in.readAllBytes(), StandardCharsets.UTF_8));
      }
    }
    return new ByteArrayInputStream(xml.append("</DataElementsList>").toString().getBytes(StandardCharsets.UTF_8));
  }
}
