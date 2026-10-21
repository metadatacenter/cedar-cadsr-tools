package org.metadatacenter.cadsr.ingestor.form;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.metadatacenter.cadsr.form.schema.Form;
import org.metadatacenter.cadsr.form.schema.Module;
import org.metadatacenter.cadsr.form.schema.Question;
import org.metadatacenter.cadsr.ingestor.util.StubCedar;
import org.metadatacenter.cadsr.ingestor.util.StubCedar.Reply;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A caDSR form becomes a CEDAR template: each module a section break, and each question whose CDE CEDAR holds the
 * field that CDE became, in the form's order. A question's wording replaces the CDE's label, and when the CDE draws
 * its values from a value set, the form's own list of answers becomes actions on that set: a value the form leaves
 * out is deleted, and a value the form places elsewhere is moved there. A question whose CDE CEDAR lacks is left out
 * and reported.
 * <p>
 * The example form has two modules and nine questions. CEDAR here holds the CDEs of two of them: the patient's
 * initials, a plain text field, and the patient's gender, whose value set has a value the form does not offer.
 */
@Tag("integration")
class FormParserTest {

  private static final ObjectMapper JSON = new ObjectMapper();
  private static final String INITIALS = "https://repo.metadatacenter.orgx/template-fields/11111111-1111-2222-3333-444455556666";
  private static final String GENDER = "https://repo.metadatacenter.orgx/template-fields/22222222-1111-2222-3333-444455556666";
  private static final String GENDER_VALUES = "https://cadsr.nci.nih.gov/metadata/CADSR-VS/VD62";

  private StubCedar cedar;

  @BeforeEach
  void startCedar() throws Exception {
    cedar = StubCedar.start();
    cedar.answer("GET", "/resource/search", request -> {
      if (request.query().startsWith("q=2001039&")) {
        return new Reply(200, found(INITIALS, "2001039", "4.0.0"));
      }
      if (request.query().startsWith("q=62&")) {
        return new Reply(200, found(GENDER, "62", "6.0.0"));
      }
      return new Reply(200, "{\"totalCount\": 0, \"resources\": []}");
    });
    cedar.answer("GET", "/resource/template-fields/11111111", 200, """
        {"@id": "%s", "schema:name": "Person Initials (2001039)", "schema:description": "Initials",
         "skos:prefLabel": "Initials", "_ui": {"inputType": "textfield"}}""".formatted(INITIALS));
    cedar.answer("GET", "/resource/template-fields/22222222", 200, """
        {"@id": "%s", "schema:name": "Person Gender v6.0 (62)", "schema:description": "Gender",
         "skos:prefLabel": "Gender", "_ui": {"inputType": "textfield"},
         "_valueConstraints": {"valueSets": [{"uri": "%s", "name": "Gender"}]}}""".formatted(GENDER, GENDER_VALUES));
    cedar.answer("GET", "/resource/template-fields/11111111-1111-2222-3333-444455556666/report", 200,
        "{\"parentPath\": \"/Shared/CDE\"}");
    cedar.answer("GET", "/resource/template-fields/22222222-1111-2222-3333-444455556666/report", 200,
        "{\"parentPath\": \"/Shared/CDE\"}");
    cedar.answer("POST", "/terminology/bioportal/integrated-search", request -> {
      int page = read(request.body()).get("page").asInt();
      String values = page > 1 ? "" : String.join(",", value("UNKNOWN"), value("FEMALE"), value("NOT REPORTED"),
          value("MALE"));
      return new Reply(200, "{\"collection\": [" + values + "]}");
    });
  }

  @AfterEach
  void stopCedar() {
    cedar.close();
  }

  @Test
  void aFormBecomesATemplateOfTheCdesCedarHolds() throws Exception {
    Form form;
    try (InputStream in = getClass().getResourceAsStream("/xml/examples/forms/form2.xml")) {
      form = FormUtil.getForm(in);
    }

    FormParseResult result = FormUtil.getTemplateMapFromForm(form, "form-parser-test");
    JsonNode template = JSON.valueToTree(result.getTemplateMap());

    assertEquals("2725838", template.get("schema:identifier").asText());
    assertEquals("Demo Enrollment (2725838)", template.get("schema:name").asText());
    assertEquals("1.0.0", template.get("pav:version").asText());
    assertEquals("bibo:published", template.get("bibo:status").asText(), "a released form becomes a published template");

    String initials = "Person Initials (2001039)";
    String gender = "Person Gender v6_0 (62)";
    assertEquals(List.of("DEMOGRAPHY", initials, gender, "HISTORY"), strings(template.get("_ui").get("order")),
        "modules and questions keep the form's order; a name loses the dots CEDAR refuses");
    List<String> required = strings(template.get("required"));
    assertTrue(required.containsAll(List.of(initials, gender)), "required: " + required);
    assertFalse(required.contains("DEMOGRAPHY"), "a section break holds no value");
    assertTrue(template.get("properties").get("@context").get("properties").has(gender));
    assertEquals("section-break", template.get("properties").get("HISTORY").get("_ui").get("inputType").asText());

    assertEquals(questionText(form, "2001039"), template.get("properties").get(initials).get("skos:prefLabel").asText(),
        "the question's wording becomes the field's label");

    JsonNode actions = template.get("properties").get(gender).get("_valueConstraints").get("actions");
    assertEquals(2, actions.size(), "actions: " + actions);
    assertEquals("delete", actions.get(0).get("action").asText());
    assertEquals(GENDER_VALUES + "/NOT REPORTED", actions.get(0).get("termUri").asText(),
        "the value the form leaves out is deleted");
    assertEquals("CADSR-VS", actions.get(0).get("source").asText());
    assertEquals("move", actions.get(1).get("action").asText());
    assertEquals(GENDER_VALUES + "/UNKNOWN", actions.get(1).get("termUri").asText());
    assertEquals(3, actions.get(1).get("to").asInt(), "UNKNOWN moves to the form's third place");
    assertEquals(2, cedar.requests("POST", "/terminology/bioportal/integrated-search").size(),
        "the value set is read until a page comes back empty");

    assertTrue(result.getReportMessages().containsAll(List.of(
        "Added section: DEMOGRAPHY",
        "Added CDE-based field. (PublicId: 62; Version: 6.0)",
        "CDE-based field not found in CEDAR. Ignored. (PublicId: 2171; Version: 4.0)",
        "Form translation complete")), "report: " + result.getReportMessages());
    for (StubCedar.Request request : cedar.requests()) {
      assertTrue(request.authorization().startsWith("apiKey ") && request.authorization().length() > 7, request.path());
    }
  }

  private static String found(String id, String publicId, String version) {
    return "{\"totalCount\": 1, \"resources\": [{\"@id\": \"" + id + "\", \"schema:identifier\": \"" + publicId
        + "\", \"pav:version\": \"" + version + "\"}]}";
  }

  private static String value(String notation) {
    return "{\"@id\": \"" + GENDER_VALUES + "/" + notation + "\", \"notation\": \"" + notation
        + "\", \"source\": \"https://data.bioontology.org/ontologies/CADSR-VS\"}";
  }

  private static String questionText(Form form, String dataElementPublicId) {
    for (Module module : form.getModule()) {
      for (Question question : module.getQuestion()) {
        if (question.getDataElement() != null && dataElementPublicId.equals(question.getDataElement().getPublicID())) {
          return question.getQuestionText();
        }
      }
    }
    throw new AssertionError("no question for " + dataElementPublicId);
  }

  private static List<String> strings(JsonNode array) {
    return java.util.stream.StreamSupport.stream(array.spliterator(), false).map(JsonNode::asText).toList();
  }

  private static JsonNode read(String body) {
    try {
      return JSON.readTree(body);
    } catch (java.io.IOException e) {
      throw new java.io.UncheckedIOException(e);
    }
  }
}
