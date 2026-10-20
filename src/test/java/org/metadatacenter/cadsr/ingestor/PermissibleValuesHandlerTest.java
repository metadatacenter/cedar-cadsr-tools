package org.metadatacenter.cadsr.ingestor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.metadatacenter.cadsr.cde.schema.DataElement;
import org.metadatacenter.cadsr.ingestor.cde.CdeStats;
import org.metadatacenter.cadsr.ingestor.cde.ValueSetsOntologyManager;
import org.metadatacenter.cadsr.ingestor.util.CdeUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An enumerated CDE's permissible values become a value set in the caDSR value-sets ontology, each value linked to
 * the concept its meaning names. A meaning can name a list of concepts, separated by colons or commas, and the value
 * is then linked to the first. A CDE the import cannot represent is skipped and reported once, with its reason.
 * <p>
 * Each case edits a copy of sample 5873923, a yes-or-no CDE, so that its first value meaning names the case's
 * concepts, and gives the copy its own identifiers so that neither the parser's cache nor the ontology has seen it.
 */
class PermissibleValuesHandlerTest {

  @Test
  void aColonSeparatedConceptListLinksTheFirstConcept(@TempDir Path dir) throws Exception {
    DataElement element = variant("9873921", "9506061", "C99901:C99902", null);

    Map<String, Object> field = CdeUtil.getFieldMapFromDataElement(element);

    assertNotNull(field, "the CDE becomes a field");
    assertFalse(CdeStats.getInstance().failedIds.contains("9873921V1.0.0"));
    Path ontology = dir.resolve("values.owl");
    ValueSetsOntologyManager.saveOntology(ontology.toFile());
    String saved = Files.readString(ontology);
    assertTrue(saved.contains("C99901"), "the value is linked to the first concept");
    assertFalse(saved.contains("C99901:C99902"), "the list itself is not a concept");
  }

  @Test
  void aCommaSeparatedConceptListLinksTheFirstConcept(@TempDir Path dir) throws Exception {
    DataElement element = variant("9873922", "9506062", "C99903,C99904", null);

    assertNotNull(CdeUtil.getFieldMapFromDataElement(element));
    Path ontology = dir.resolve("values.owl");
    ValueSetsOntologyManager.saveOntology(ontology.toFile());
    String saved = Files.readString(ontology);
    assertTrue(saved.contains("C99903"));
    assertFalse(saved.contains("C99904"), "only the first concept is linked");
  }

  @Test
  void aCdeTheImportCannotRepresentIsSkippedAndReportedOnce() throws Exception {
    DataElement element = variant("9873923", "9506063", "C49487", "Mixed");

    assertNull(CdeUtil.getFieldMapFromDataElement(element));
    assertNull(CdeUtil.getFieldMapFromDataElement(element), "a second read skips it again");

    assertEquals(1, Collections.frequency(CdeStats.getInstance().skippedIds, "9873923V1.0.0"));
    assertFalse(CdeStats.getInstance().getSkippedReasons().isEmpty());
  }

  private static DataElement variant(String publicId, String valueDomainId, String concepts, String valueDomainType)
      throws Exception {
    String xml;
    try (InputStream in = PermissibleValuesHandlerTest.class.getResourceAsStream("/cde-sample-5873923.xml")) {
      xml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
    xml = xml.replace("<PUBLICID>5873923</PUBLICID>", "<PUBLICID>" + publicId + "</PUBLICID>")
        .replace("<PublicId>3506068</PublicId>", "<PublicId>" + valueDomainId + "</PublicId>")
        .replace("<MEANINGCONCEPTS>C49487</MEANINGCONCEPTS>", "<MEANINGCONCEPTS>" + concepts + "</MEANINGCONCEPTS>");
    if (valueDomainType != null) {
      xml = xml.replaceFirst("<ValueDomainType>[^<]*</ValueDomainType>",
          "<ValueDomainType>" + valueDomainType + "</ValueDomainType>");
    }
    return CdeUtil.getDataElement(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
  }
}
