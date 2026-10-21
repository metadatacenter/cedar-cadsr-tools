package org.metadatacenter.cadsr.ingestor.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.metadatacenter.cadsr.ingestor.category.CategoryTreeNode;
import org.metadatacenter.cadsr.ingestor.category.CedarCategory;
import org.metadatacenter.cadsr.ingestor.cde.CdeSummary;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarServer;
import org.metadatacenter.cadsr.ingestor.util.StubCedar.Reply;
import org.metadatacenter.cadsr.ingestor.util.StubCedar.Request;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The calls the tools make to CEDAR on the caDSR administrator's behalf: reading and changing the category tree,
 * creating CDEs as fields and attaching them to categories, finding a CDE a form names, loading the value-sets
 * ontology, and creating a template from a form. Each request must carry the API key and a UTF-8 body, and each
 * refusal must reach the operator with the server's explanation.
 */
@Tag("integration")
class CedarServicesTest {

  private static final String KEY = "cadsr-admin-key";
  private static final String FOLDER = "https://repo.metadatacenter.orgx/folders/0f2a3b4c-1111-2222-3333-444455556666";
  private static final ObjectMapper JSON = new ObjectMapper();

  private StubCedar cedar;

  @BeforeEach
  void startCedar() throws Exception {
    cedar = StubCedar.start();
  }

  @AfterEach
  void stopCedar() {
    cedar.close();
  }

  /** A category is created under its parent, and its children under it with the identifier CEDAR gave it. */
  @Test
  void aCategoryIsCreatedWithItsChildren() throws Exception {
    List<String> created = new ArrayList<>();
    cedar.answer("POST", "/resource/categories", request -> {
      created.add(read(request).get("schema:identifier").asText());
      return new Reply(201, "{\"@id\": \"cedar-" + created.size() + "\"}");
    });
    CategoryTreeNode child = node("ctx/cs", "Scheme °C", List.of());
    CategoryTreeNode parent = node("ctx", "Context", List.of(child));

    assertEquals("cedar-1", CedarServices.createCategory(parent, "cadsr-root", CedarServer.LOCAL, KEY));

    List<Request> posts = cedar.requests("POST", "/resource/categories");
    assertEquals(List.of("ctx", "ctx/cs"), created);
    assertEquals("cadsr-root", read(posts.get(0)).get("parentCategoryId").asText());
    assertEquals("cedar-1", read(posts.get(1)).get("parentCategoryId").asText(), "a child goes under its parent");
    assertEquals("Scheme °C", read(posts.get(1)).get("schema:name").asText(), "the body is UTF-8");
    assertEquals("apiKey " + KEY, posts.get(0).authorization());
  }

  @Test
  void aRefusedCategoryReportsWhy() {
    cedar.answer("POST", "/resource/categories", 400, "{\"message\": \"name taken\"}");
    RuntimeException refusal = assertThrows(RuntimeException.class,
        () -> CedarServices.createCategory(node("ctx", "Context", List.of()), "root", CedarServer.LOCAL, KEY));
    assertTrue(refusal.getMessage().contains("name taken"), refusal.getMessage());
  }

  @Test
  void aCategoryIsUpdatedAndDeletedByItsCedarIdentifier() throws Exception {
    String id = "https://repo.metadatacenter.orgx/categories/c1";
    String path = "/resource/categories/" + java.net.URLEncoder.encode(id, java.nio.charset.StandardCharsets.UTF_8);
    cedar.answer("PUT", path, 200, "{}").answer("DELETE", path, 204, null);

    CedarServices.updateCategory(id, node("ctx", "Renamed", List.of()), CedarServer.LOCAL, KEY);
    CedarServices.deleteCategory(id, CedarServer.LOCAL, KEY);

    JsonNode update = read(only(cedar.requests("PUT", path)));
    assertEquals("Renamed", update.get("schema:name").asText());
    assertEquals("ctx", update.get("schema:identifier").asText());
    assertEquals(1, cedar.requests("DELETE", path).size());

    cedar.answer("DELETE", path, 404, "{\"message\": \"no such category\"}");
    InternalError refusal = assertThrows(InternalError.class, () -> CedarServices.deleteCategory(id, CedarServer.LOCAL, KEY));
    assertTrue(refusal.getMessage().contains("no such category"), refusal.getMessage());
    cedar.answer("PUT", path, 403, "{\"message\": \"not yours\"}");
    assertThrows(RuntimeException.class,
        () -> CedarServices.updateCategory(id, node("ctx", "Renamed", List.of()), CedarServer.LOCAL, KEY));
  }

  @Test
  void theCategoryTreeIsReadAsCategoriesAndAsIdentifiers() throws Exception {
    cedar.answer("GET", "/resource/categories/tree", 200, """
        {"@id": "root", "schema:identifier": "ROOT", "schema:name": "Root", "children": [
          {"@id": "cedar-cadsr", "schema:identifier": "CADSR-CATEGORY-ID", "schema:name": "caDSR",
           "parentCategoryId": "root", "children": []}]}""");

    CedarCategory tree = CedarServices.getCedarCategoryTree(CedarServer.LOCAL, KEY);
    assertEquals("cedar-cadsr", only(tree.getChildren()).getCedarId());
    assertEquals("root", only(tree.getChildren()).getParentCategoryCedarId());
    assertEquals(Map.of("ROOT", "root", "CADSR-CATEGORY-ID", "cedar-cadsr"),
        CedarServices.getCategoryUniqueIdsToCedarCategoryIdsMap(CedarServer.LOCAL, KEY));

    cedar.answer("GET", "/resource/categories/tree", 500, "{\"message\": \"down\"}");
    assertThrows(RuntimeException.class, () -> CedarServices.getCedarCategoryTree(CedarServer.LOCAL, KEY));
  }

  /**
   * A CDE is created as a field in the CDE folder, with the hash the updater compares sent as a header. The
   * category identifiers the field map carries are not part of the CEDAR model and stay out of the body.
   */
  @Test
  void aCdeIsCreatedWithItsHashAndWithoutItsCategories() throws Exception {
    cedar.answer("POST", "/resource/template-fields", 201, "{\"@id\": \"cedar-field\"}");
    Map<String, Object> cde = cdeMap("2188100", "1.0.0");
    cde.put("schema:name", "Temperature °C");
    cde.put(Constants.CDE_CATEGORY_IDS_FIELD, List.of("CADSR-CATEGORY-ID/x"));

    assertEquals("cedar-field", CedarServices.createCde(cde, "hash-1", FOLDER, CedarServer.LOCAL, KEY));

    Request post = only(cedar.requests("POST", "/resource/template-fields"));
    assertEquals("folder_id=folders%2F0f2a3b4c-1111-2222-3333-444455556666", post.query());
    assertEquals("hash-1", post.sourceHash());
    assertFalse(read(post).has(Constants.CDE_CATEGORY_IDS_FIELD));
    assertEquals("Temperature °C", read(post).get("schema:name").asText());

    cedar.answer("POST", "/resource/template-fields", 400, "{\"message\": \"invalid field\"}");
    assertNull(CedarServices.createCde(cdeMap("2188100", "1.0.0"), "hash-1", FOLDER, CedarServer.LOCAL, KEY),
        "a refused CDE is logged and skipped, and the run goes on");
  }

  /** The updater reads every CDE in the folder, a page of a hundred at a time, until it has the total. */
  @Test
  void theCdesInTheFolderAreReadPageByPage() throws Exception {
    cedar.answer("GET", "/resource/folders/", request -> {
      int offset = Integer.parseInt(request.query().replaceAll(".*offset=(\\d+).*", "$1"));
      int count = offset == 0 ? 100 : 20;
      StringBuilder resources = new StringBuilder();
      for (int i = 0; i < count; i++) {
        resources.append(i == 0 ? "" : ",").append("{\"@id\": \"f").append(offset + i)
            .append("\", \"schema:identifier\": \"").append(offset + i).append("\", \"pav:version\": \"1.0.0\",")
            .append(" \"sourceHash\": \"h\", \"categories\": [\"c\"]}");
      }
      return new Reply(200, "{\"totalCount\": 120, \"resources\": [" + resources + "]}");
    });

    List<CdeSummary> summaries = CedarServices.findCdeSummariesInFolder(FOLDER,
        List.of("schema:identifier", "pav:version", "sourceHash"), true, CedarServer.LOCAL, KEY);

    assertEquals(120, summaries.size());
    assertEquals("f119", summaries.get(119).getCedarId());
    assertEquals(List.of("c"), summaries.get(0).getCategoryCedarIds());
    List<Request> reads = cedar.requests("GET", "/resource/folders/0f2a3b4c-1111-2222-3333-444455556666/contents-extract");
    assertEquals(2, reads.size());
    assertTrue(reads.get(1).query().endsWith("&offset=100&limit=100"), reads.get(1).query());
  }

  /**
   * A form names a CDE by public identifier and version. A search finds the fields that mention the identifier, and
   * the one with that identifier and version, stored in the CDE folder, is the CDE.
   */
  @Test
  void aCdeIsFoundByIdentifierAndVersionInTheCdeFolder() throws Exception {
    String elsewhere = "https://repo.metadatacenter.orgx/template-fields/aaaaaaaa-1111-2222-3333-444455556666";
    String wanted = "https://repo.metadatacenter.orgx/template-fields/bbbbbbbb-1111-2222-3333-444455556666";
    cedar.answer("GET", "/resource/search", 200, "{\"totalCount\": 3, \"resources\": ["
        + "{\"@id\": \"x\", \"schema:identifier\": \"2001039\", \"pav:version\": \"2.0.0\"},"
        + "{\"@id\": \"" + elsewhere + "\", \"schema:identifier\": \"2001039\", \"pav:version\": \"1.0.0\"},"
        + "{\"@id\": \"" + wanted + "\", \"schema:identifier\": \"2001039\", \"pav:version\": \"1.0.0\"}]}");
    // The field's own route is added before its report's, which shares its prefix, so the report's wins for it.
    cedar.answer("GET", "/resource/template-fields/aaaaaaaa", 200, "{\"parentPath\": \"/Users/someone\"}")
        .answer("GET", "/resource/template-fields/bbbbbbbb", 200,
            "{\"@id\": \"" + wanted + "\", \"schema:name\": \"Wanted\"}")
        .answer("GET", "/resource/template-fields/bbbbbbbb-1111-2222-3333-444455556666/report", 200,
            "{\"parentPath\": \"/Shared/CDE\"}");

    Optional<Map<String, Object>> found = CedarServices.searchCdeByPublicIdAndVersion("2001039", "1",
        CedarServer.LOCAL, KEY);

    assertEquals("Wanted", found.orElseThrow().get("schema:name"));
    assertEquals("q=2001039&resource_types=field", only(cedar.requests("GET", "/resource/search")).query());
    assertTrue(CedarServices.searchCdeByPublicIdAndVersion("2001039", "3", CedarServer.LOCAL, KEY).isEmpty(),
        "no field carries that version");

    cedar.answer("GET", "/resource/template-fields/cccccccc", 200, "{}");
    assertThrows(IllegalArgumentException.class, () -> CedarServices.isFieldInPath(
        "https://repo.metadatacenter.orgx/template-fields/cccccccc-1111-2222-3333-444455556666", "/Shared/CDE",
        CedarServer.LOCAL, KEY),
        "a report without a path is refused");
    cedar.answer("GET", "/resource/search", 503, "{\"message\": \"search is down\"}");
    InternalError refusal = assertThrows(InternalError.class,
        () -> CedarServices.searchCdeByPublicIdAndVersion("2001039", "1", CedarServer.LOCAL, KEY));
    assertTrue(refusal.getMessage().contains("search is down"), refusal.getMessage());
  }

  @Test
  void aFieldThatCannotBeReadReportsWhy() {
    cedar.answer("GET", "/resource/template-fields/", 404, "{\"message\": \"no such field\"}");
    String field = "https://repo.metadatacenter.orgx/template-fields/dddddddd-1111-2222-3333-444455556666";
    InternalError refusal = assertThrows(InternalError.class,
        () -> CedarServices.getFieldById(field, CedarServer.LOCAL, KEY));
    assertTrue(refusal.getMessage().contains("no such field"), refusal.getMessage());
    assertThrows(InternalError.class, () -> CedarServices.getFieldReport(field, CedarServer.LOCAL, KEY));
  }

  @Test
  void aCdeIsAttachedToEveryCategoryInOneCall() throws Exception {
    cedar.answer("POST", "/resource/command/attach-categories", 200, "{}");

    CedarServices.attachCdeToCategories("cedar-field", List.of("c1", "c2"), false, CedarServer.LOCAL, KEY);

    JsonNode body = read(only(cedar.requests("POST", "/resource/command/attach-categories")));
    assertEquals("cedar-field", body.get("artifactId").asText());
    assertEquals(List.of("c1", "c2"), List.of(body.get("categoryIds").get(0).asText(), body.get("categoryIds").get(1).asText()));
    assertThrows(IllegalArgumentException.class,
        () -> CedarServices.attachCdeToCategories("cedar-field", List.of(), false, CedarServer.LOCAL, KEY));
    cedar.answer("POST", "/resource/command/attach-categories", 404, "{\"message\": \"no such category\"}");
    assertThrows(RuntimeException.class,
        () -> CedarServices.attachCdeToCategories("cedar-field", List.of("c9"), true, CedarServer.LOCAL, KEY));
  }

  @Test
  void theValueSetsOntologyIsLoadedAndItsStatusRead() throws Exception {
    cedar.answer("POST", "/resource/command/load-valuesets-ontology", 200, "{}")
        .answer("GET", "/resource/command/load-valuesets-ontology-status", 200, "{\"importStatus\": \"COMPLETE\"}");

    CedarServices.loadValueSetsOntology(CedarServer.LOCAL, KEY);
    assertEquals("COMPLETE", CedarServices.loadValueSetsOntologyStatus(CedarServer.LOCAL, KEY));

    cedar.answer("POST", "/resource/command/load-valuesets-ontology", 500, "{}")
        .answer("GET", "/resource/command/load-valuesets-ontology-status", 500, "{}");
    assertThrows(InternalError.class, () -> CedarServices.loadValueSetsOntology(CedarServer.LOCAL, KEY));
    assertThrows(InternalError.class, () -> CedarServices.loadValueSetsOntologyStatus(CedarServer.LOCAL, KEY));
  }

  /** The values of a CDE's value set are read a page at a time through the terminology server. */
  @Test
  void aValueSetIsSearchedThroughTheTerminologyServer() throws Exception {
    cedar.answer("POST", "/terminology/bioportal/integrated-search", 200,
        "{\"collection\": [{\"notation\": \"Y\"}], \"page\": 1}");
    Map<String, Object> constraints = Map.of("valueSets", List.of(Map.of("uri", "https://cadsr.example/VD1")));

    Map<String, Object> result = CedarServices.integratedSearch(constraints, 2, 50, CedarServer.LOCAL, KEY);

    assertEquals(List.of(Map.of("notation", "Y")), result.get("collection"));
    JsonNode body = read(only(cedar.requests("POST", "/terminology/bioportal/integrated-search")));
    assertEquals(2, body.get("page").asInt());
    assertEquals(50, body.get("pageSize").asInt());
    assertEquals("https://cadsr.example/VD1",
        body.get("parameterObject").get("valueConstraints").get("valueSets").get(0).get("uri").asText());

    cedar.answer("POST", "/terminology/bioportal/integrated-search", 502, "{}");
    assertThrows(RuntimeException.class,
        () -> CedarServices.integratedSearch(constraints, 1, 50, CedarServer.LOCAL, KEY));
  }

  /** The impex server creates the template it translated from a form in the folder the user chose. */
  @Test
  void aTemplateIsCreatedInTheChosenFolder() throws Exception {
    cedar.answer("POST", "/resource/templates", 201, "{\"@id\": \"cedar-template\"}");
    Map<String, Object> template = Map.of("schema:name", "Form é");

    assertEquals("cedar-template", CedarServices.createTemplate(template, FOLDER, CedarServer.LOCAL, KEY));

    Request post = only(cedar.requests("POST", "/resource/templates"));
    assertEquals("folder_id=folders%2F0f2a3b4c-1111-2222-3333-444455556666", post.query());
    assertEquals("Form é", read(post).get("schema:name").asText());
    cedar.answer("POST", "/resource/templates", 400, "{\"message\": \"invalid template\"}");
    RuntimeException refusal = assertThrows(RuntimeException.class,
        () -> CedarServices.createTemplate(template, FOLDER, CedarServer.LOCAL, KEY));
    assertTrue(refusal.getMessage().contains("invalid template"), refusal.getMessage());
  }

  private static CategoryTreeNode node(String uniqueId, String name, List<CategoryTreeNode> children) {
    return new CategoryTreeNode(uniqueId, null, name, name + " description", children, "parent", "1");
  }

  private static Map<String, Object> cdeMap(String publicId, String version) {
    Map<String, Object> cde = new HashMap<>();
    cde.put("schema:identifier", publicId);
    cde.put("pav:version", version);
    cde.put("schema:name", "CDE " + publicId);
    return cde;
  }

  private static JsonNode read(Request request) {
    try {
      return JSON.readTree(request.body());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static <T> T only(List<T> items) {
    assertEquals(1, items.size(), "expected one: " + items);
    return items.get(0);
  }
}
