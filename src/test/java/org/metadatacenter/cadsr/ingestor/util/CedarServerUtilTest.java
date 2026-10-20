package org.metadatacenter.cadsr.ingestor.util;

import org.junit.jupiter.api.Test;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarServer;
import org.metadatacenter.model.CedarResourceType;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The tools reach one of three CEDAR deployments, which an operator names or the host CEDAR runs on implies, and
 * build every request from that deployment's servers. A folder or field arrives as a full identifier, an encoded one,
 * or a bare UUID, and each request must address it the same way.
 */
class CedarServerUtilTest {

  private static final String FOLDER_UUID = "0f2a3b4c-1111-2222-3333-444455556666";
  private static final String FOLDER_ID = "https://repo.metadatacenter.orgx/folders/" + FOLDER_UUID;
  private static final String FIELD_UUID = "9e8d7c6b-1111-2222-3333-444455556666";

  @Test
  void aDeploymentIsNamedByOperatorOrImpliedByHost() {
    assertEquals(CedarServer.LOCAL, CedarServerUtil.toCedarServerFromServerName("local"));
    assertEquals(CedarServer.STAGING, CedarServerUtil.toCedarServerFromServerName("Staging"));
    assertEquals(CedarServer.PRODUCTION, CedarServerUtil.toCedarServerFromServerName("PRODUCTION"));
    assertThrows(IllegalArgumentException.class, () -> CedarServerUtil.toCedarServerFromServerName("test"));

    assertEquals(CedarServer.LOCAL, CedarServerUtil.toCedarServerFromHostName("metadatacenter.orgx"));
    assertEquals(CedarServer.STAGING, CedarServerUtil.toCedarServerFromHostName("staging.metadatacenter.org"));
    assertEquals(CedarServer.PRODUCTION, CedarServerUtil.toCedarServerFromHostName("metadatacenter.org"));
    assertThrows(IllegalArgumentException.class, () -> CedarServerUtil.toCedarServerFromHostName("example.org"));
  }

  @Test
  void eachDeploymentHasItsOwnServers() {
    assertEquals(Map.of(
            CedarServer.LOCAL, List.of("https://resource.metadatacenter.orgx", "https://repo.metadatacenter.orgx",
                "https://terminology.metadatacenter.orgx"),
            CedarServer.STAGING, List.of("https://resource.staging.metadatacenter.org",
                "https://repo.staging.metadatacenter.org", "https://terminology.staging.metadatacenter.org"),
            CedarServer.PRODUCTION, List.of("https://resource.metadatacenter.org", "https://repo.metadatacenter.org",
                "https://terminology.metadatacenter.org")),
        Map.of(CedarServer.LOCAL, servers(CedarServer.LOCAL), CedarServer.STAGING, servers(CedarServer.STAGING),
            CedarServer.PRODUCTION, servers(CedarServer.PRODUCTION)));
  }

  @Test
  void aFolderIsAddressedTheSameHoweverItArrives() throws Exception {
    String encoded = java.net.URLEncoder.encode(FOLDER_ID, java.nio.charset.StandardCharsets.UTF_8);
    for (String folder : List.of(FOLDER_ID, encoded, FOLDER_UUID)) {
      assertEquals("https://resource.metadatacenter.orgx/template-fields?folder_id=folders%2F" + FOLDER_UUID,
          CedarServerUtil.getTemplateFieldsEndpoint(folder, CedarServer.LOCAL), folder);
      assertEquals("https://resource.metadatacenter.orgx/templates?folder_id=folders%2F" + FOLDER_UUID,
          CedarServerUtil.getTemplatesEndpoint(folder, CedarServer.LOCAL), folder);
      assertEquals("https://resource.metadatacenter.orgx/folders/" + FOLDER_UUID + "/contents",
          CedarServerUtil.getFolderContentsEndPoint(folder, CedarServer.LOCAL), folder);
    }
  }

  @Test
  void theCdeExtractAsksForTheFieldsTheUpdaterCompares() throws Exception {
    List<String> fields = List.of("schema:identifier", "pav:version", "sourceHash");
    String base = "https://resource.metadatacenter.orgx/folders/" + FOLDER_UUID + "/contents-extract?resource_types=field";
    assertEquals(base + "&field_names=schema:identifier,pav:version,sourceHash,categories",
        CedarServerUtil.getCdesInFolderExtractEndPoint(FOLDER_ID, fields, true, CedarServer.LOCAL));
    assertEquals(base + "&field_names=schema:identifier,pav:version,sourceHash",
        CedarServerUtil.getCdesInFolderExtractEndPoint(FOLDER_ID, fields, false, CedarServer.LOCAL));
    assertEquals(base, CedarServerUtil.getCdesInFolderExtractEndPoint(FOLDER_ID, List.of(), false, CedarServer.LOCAL));
    assertEquals(List.of("schema:identifier", "pav:version", "sourceHash"), fields, "the caller's list is left alone");
  }

  @Test
  void aFieldAndItsReportAreAddressedByUuid() throws Exception {
    String fieldId = "https://repo.metadatacenter.org/template-fields/" + FIELD_UUID;
    assertEquals("https://resource.metadatacenter.org/template-fields/" + FIELD_UUID,
        CedarServerUtil.getTemplateFieldEndPoint(fieldId, CedarServer.PRODUCTION));
    assertEquals("https://resource.metadatacenter.org/template-fields/" + FIELD_UUID + "/report",
        CedarServerUtil.getTemplateFieldReportEndPoint(fieldId, CedarServer.PRODUCTION));
  }

  @Test
  void categoryAndCommandEndpointsSitOnTheResourceServer() throws Exception {
    String resource = "https://resource.staging.metadatacenter.org";
    CedarServer staging = CedarServer.STAGING;
    assertEquals(resource + "/categories", CedarServerUtil.getCategoriesRestEndpoint(staging));
    assertEquals(resource + "/categories/root", CedarServerUtil.getRootCategoryRestEndpoint(staging));
    assertEquals(resource + "/categories/tree", CedarServerUtil.getCategoryTreeEndpoint(staging));
    assertEquals(resource + "/categories/https%3A%2F%2Frepo.staging.metadatacenter.org%2Fcategories%2Fc1",
        CedarServerUtil.getCategoryRestEndpoint("https://repo.staging.metadatacenter.org/categories/c1", staging));
    assertEquals(resource + "/command/attach-category", CedarServerUtil.getAttachCategoryEndpoint(staging));
    assertEquals(resource + "/command/attach-categories", CedarServerUtil.getAttachCategoriesEndpoint(staging));
    assertEquals(resource + "/command/load-valuesets-ontology",
        CedarServerUtil.getLoadValueSetsOntologyEndpoint(staging));
    assertEquals(resource + "/command/load-valuesets-ontology-status",
        CedarServerUtil.getLoadValueSetsOntologyStatusEndpoint(staging));
    assertEquals("https://terminology.staging.metadatacenter.org/bioportal/integrated-search",
        CedarServerUtil.getIntegratedSearchEndpoint(staging));
  }

  @Test
  void aSearchNamesItsQueryAndResourceTypes() {
    String search = "https://resource.metadatacenter.orgx/search?";
    assertEquals(search + "q=2001039&resource_types=field",
        CedarServerUtil.getSearchEndPoint("2001039", List.of(CedarResourceType.FIELD), CedarServer.LOCAL));
    assertEquals(search + "resource_types=field,template",
        CedarServerUtil.getSearchEndPoint(null, List.of(CedarResourceType.FIELD, CedarResourceType.TEMPLATE),
            CedarServer.LOCAL));
    assertEquals(search + "q=x&", CedarServerUtil.getSearchEndPoint("x", List.of(), CedarServer.LOCAL));
  }

  /**
   * A form's module becomes a static section-break field, identified on the repository server of the deployment
   * the template is created in.
   */
  @Test
  void aSectionBreakIsAStaticFieldOfTheDeployment() {
    Map<String, Object> sectionBreak = CedarFieldUtil.generateDefaultSectionBreak("Demographics", "About you",
        CedarServer.STAGING);
    assertTrue(((String) sectionBreak.get("@id")).startsWith("https://repo.staging.metadatacenter.org/template-fields/"));
    assertEquals("https://schema.metadatacenter.org/core/StaticTemplateField", sectionBreak.get("@type"));
    assertEquals("Demographics", sectionBreak.get("schema:name"));
    assertEquals("About you", sectionBreak.get("schema:description"));
    assertEquals("'Demographics' field schema", sectionBreak.get("title"));
    assertEquals("section-break", ((Map<?, ?>) sectionBreak.get("_ui")).get("inputType"));
    assertEquals(Constants.CEDAR_SCHEMA_VERSION, sectionBreak.get("schema:schemaVersion"));
    assertTrue(sectionBreak.containsKey("pav:createdOn"), "the server fills in the provenance on creation");
  }

  private static List<String> servers(CedarServer server) {
    return List.of(CedarServerUtil.getResourceServerUrl(server), CedarServerUtil.getRepoServerUrl(server),
        CedarServerUtil.getTerminologyServerUrl(server));
  }
}
