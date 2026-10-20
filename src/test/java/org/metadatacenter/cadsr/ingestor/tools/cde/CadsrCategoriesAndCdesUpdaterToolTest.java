package org.metadatacenter.cadsr.ingestor.tools.cde;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.xml.bind.JAXBContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.metadatacenter.cadsr.category.schema.CSI;
import org.metadatacenter.cadsr.category.schema.ClassificationScheme;
import org.metadatacenter.cadsr.category.schema.Classifications;
import org.metadatacenter.cadsr.category.schema.Context;
import org.metadatacenter.cadsr.cde.schema.DataElement;
import org.metadatacenter.cadsr.ingestor.util.CdeUtil;
import org.metadatacenter.cadsr.ingestor.util.StubCedar;
import org.metadatacenter.cadsr.ingestor.util.StubFtp;
import org.metadatacenter.cadsr.ingestor.util.StubCedar.Reply;
import org.metadatacenter.cadsr.ingestor.util.StubCedar.Request;
import org.metadatacenter.constant.OntologyAndValueSetConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.metadatacenter.cadsr.ingestor.util.Constants.CADSR_CATEGORY_SCHEMA_ORG_ID;

/**
 * One run of the updater, from NCI's two archives to CEDAR. CEDAR already holds part of the category tree and some
 * of the CDEs, and the run must bring both in line with the archives: create the categories that are missing,
 * parents before children, rename the one that changed and delete the one the archive no longer has; create each
 * new CDE in the CDE folder with its hash and attach it to its categories; leave an unchanged CDE alone but attach
 * it to a category it lacks; and load the value sets the CDEs draw on.
 * <p>
 * The stub answers for CEDAR and keeps the category tree as the run changes it, since the run reads it again after
 * creating categories.
 */
class CadsrCategoriesAndCdesUpdaterToolTest {

  private static final ObjectMapper JSON = new ObjectMapper();
  private static final String KEY = "cadsr-admin-key";
  private static final String FOLDER_UUID = "0f2a3b4c-1111-2222-3333-444455556666";
  private static final String CHANGED_CDE = "https://repo.metadatacenter.orgx/template-fields/eeeeeeee-1111-2222-3333-444455556666";

  private static final String CIP = CADSR_CATEGORY_SCHEMA_ORG_ID + "/CIP_V2.31";
  private static final String NCIA = CIP + "/2857181_V1";
  private static final String RIDER = NCIA + "/2812022_V1";
  private static final String RIDER_SUB = RIDER + "/9000001_V1";
  private static final String GONE = CADSR_CATEGORY_SCHEMA_ORG_ID + "/GONE_V1";
  private static final String NCI_STANDARDS = CADSR_CATEGORY_SCHEMA_ORG_ID + "/NCIStandards_V1";
  private static final String PRO = NCI_STANDARDS + "/5675693_V1";
  private static final String MSAS = PRO + "/5840828_V1";

  private StubCedar cedar;
  /** CEDAR's categories, by CEDAR identifier, as the run leaves them. */
  private final Map<String, ObjectNode> categories = new LinkedHashMap<>();

  @BeforeEach
  void startCedar() throws Exception {
    cedar = StubCedar.start();
    category("root", "ROOT", "Root", null);
    category("cedar-cadsr", CADSR_CATEGORY_SCHEMA_ORG_ID, "NCI caDSR", "root");
    category("cedar-cip", CIP, "CIP", "cedar-cadsr");
    category("cedar-ncia", NCIA, "NCIA Projects (former name)", "cedar-cip");
    category("cedar-gone", GONE, "Gone", "cedar-cadsr");
    serveTheCategoryTree();
  }

  @AfterEach
  void stopCedar() {
    cedar.close();
  }

  @Test
  void aRunBringsCategoriesAndCdesInLineWithTheArchives(@TempDir Path dir) throws Exception {
    Path categoriesZip = zip(dir.resolve("categories.zip"), "categories.xml", classificationsXml());
    Path cdesZip = zip(dir.resolve("cdes.zip"), "cdes.xml",
        cdeListXml("2182451", "5873923", "3017383", "2752716", "2001826"));
    Path ontologyDir = Files.createDirectories(dir.resolve("ontology"));
    Path executionDir = dir.resolve("execution");

    String unchangedHash = CdeUtil.generateCdeHashCode(sample("5873923"));
    List<String> createdCdes = serveTheCdeSide(List.of(
        summary("https://repo.metadatacenter.orgx/template-fields/aaaaaaaa-1111-2222-3333-444455556666", "5873923",
            unchangedHash),
        summary(CHANGED_CDE, "3017383", "a-hash-the-file-no-longer-gives"),
        summary("https://repo.metadatacenter.orgx/template-fields/ffffffff-1111-2222-3333-444455556666", "9999999",
            "h")));

    CadsrCategoriesAndCdesUpdaterTool.main(new String[]{
        "-k", KEY, "-f", "https://repo.metadatacenter.orgx/folders/" + FOLDER_UUID, "-s", "local",
        "-t", "-G", categoriesZip.toString(), "-c", "-E", cdesZip.toString(),
        "-x", executionDir.toString(), "-o", ontologyDir.toString()});

    // Categories: the missing ones exist under the right parents, the renamed one has its new name, the gone one is gone.
    assertEquals(cedarId(NCIA), parentOf(RIDER), "a new category goes under the existing one it belongs to");
    assertEquals(cedarId(RIDER), parentOf(RIDER_SUB), "a new category goes under the new one it belongs to");
    assertEquals("cedar-cadsr", parentOf(NCI_STANDARDS), "a new context goes under caDSR's root");
    assertEquals(cedarId(NCI_STANDARDS), parentOf(PRO));
    assertEquals(cedarId(PRO), parentOf(MSAS));
    assertEquals("NCIA Projects", categories.get("cedar-ncia").get("schema:name").asText());
    assertFalse(categories.containsKey("cedar-gone"));
    assertEquals("cedar-cip", cedarId(CIP), "an unchanged category keeps its identifier");
    assertEquals(1, cedar.requests("PUT", "/resource/categories/").size(), "only the changed category is updated");

    // CDEs: each new one is created in the CDE folder with its hash; the existing ones are not created again.
    assertTrue(createdCdes.contains("2182451"), "created: " + createdCdes);
    assertFalse(createdCdes.contains("5873923") || createdCdes.contains("3017383"), "created: " + createdCdes);
    for (Request create : cedar.requests("POST", "/resource/template-fields")) {
      assertEquals("folder_id=folders%2F" + FOLDER_UUID, create.query());
      assertEquals("apiKey " + KEY, create.authorization());
      String publicId = read(create).get("schema:identifier").asText();
      assertEquals(CdeUtil.generateCdeHashCode(sample(publicId)), create.sourceHash(), publicId);
      assertFalse(read(create).has("categoryIds"), publicId);
    }
    assertEquals(1, cedar.requests("GET", "/resource/template-fields/eeeeeeee").size(),
        "a CDE that changed without a new version is read back to report the difference");

    // Attachments: the new CDE when it is created, the unchanged one when the relations are reviewed.
    Map<String, Set<String>> attached = new LinkedHashMap<>();
    for (Request attach : cedar.requests("POST", "/resource/command/attach-categories")) {
      Set<String> ids = new HashSet<>();
      read(attach).get("categoryIds").forEach(id -> ids.add(id.asText()));
      attached.put(read(attach).get("artifactId").asText(), ids);
    }
    assertEquals(Set.of(cedarId(RIDER)), attached.get("cedar-cde-2182451"));
    assertEquals(Set.of(cedarId(MSAS)), attached.get("https://repo.metadatacenter.orgx/template-fields/aaaaaaaa-1111-2222-3333-444455556666"));

    // The value sets are written out and loaded, and the execution folder is removed.
    assertTrue(Files.size(ontologyDir.resolve(OntologyAndValueSetConstants.CADSR_VALUE_SETS_ONTOLOGY_FILE)) > 0);
    assertEquals(1, cedar.requests("POST", "/resource/command/load-valuesets-ontology").size());
    assertFalse(Files.exists(executionDir));
  }

  /** Without local archives, the run fetches the newest archive of each kind from NCI's FTP server. */
  @Test
  void aRunFetchesTheNewestArchivesFromNcisFtpServer(@TempDir Path dir) throws Exception {
    List<String> createdCdes = serveTheCdeSide(List.of());
    Path executionDir = dir.resolve("execution");
    try (StubFtp ftp = new StubFtp("cadsr", "secret")) {
      ftp.file("categories", "categories-2026-01.zip", new byte[0], LocalDate.of(2026, 1, 1))
          .file("categories", "categories-2026-09.zip", zipBytes("categories.xml", classificationsXml()),
              LocalDate.of(2026, 9, 1))
          .file("cdes", "cdes-2026-09.zip", zipBytes("cdes.xml", cdeListXml("2182451")), LocalDate.of(2026, 9, 1));

      CadsrCategoriesAndCdesUpdaterTool.main(new String[]{
          "-k", KEY, "-f", "https://repo.metadatacenter.orgx/folders/" + FOLDER_UUID, "-s", "local",
          "-t", "-g", "categories", "-c", "-e", "cdes", "-h", ftp.address(), "-u", "cadsr", "-p", "secret",
          "-x", executionDir.toString(), "-o", Files.createDirectories(dir.resolve("ontology")).toString()});
    }

    assertEquals(cedarId(NCIA), parentOf(RIDER), "the categories came from the newest archive");
    assertEquals(List.of("2182451"), createdCdes);
    assertFalse(Files.exists(executionDir));
  }

  /** The delete option removes every caDSR category from CEDAR, and keeps caDSR's root for the next run. */
  @Test
  void deletingCategoriesKeepsCaDsrsRoot() {
    CadsrCategoriesAndCdesUpdaterTool.main(new String[]{
        "-k", KEY, "-f", "https://repo.metadatacenter.orgx/folders/" + FOLDER_UUID, "-s", "local", "-d"});

    assertEquals(List.of("root", "cedar-cadsr"), List.copyOf(categories.keySet()));
  }

  private List<String> serveTheCdeSide(List<ObjectNode> existingCdes) {
    serveTheCdeFolder(existingCdes);
    List<String> createdCdes = new ArrayList<>();
    cedar.answer("POST", "/resource/template-fields", request -> {
      createdCdes.add(read(request).get("schema:identifier").asText());
      return new Reply(201, "{\"@id\": \"cedar-cde-" + read(request).get("schema:identifier").asText() + "\"}");
    });
    cedar.answer("GET", "/resource/template-fields/eeeeeeee", 200,
        "{\"@id\": \"" + CHANGED_CDE + "\", \"schema:identifier\": \"3017383\", \"schema:name\": \"Old name\"}");
    cedar.answer("POST", "/resource/command/attach-categories", 200, "{}");
    cedar.answer("POST", "/resource/command/load-valuesets-ontology", 200, "{}");
    cedar.answer("GET", "/resource/command/load-valuesets-ontology-status", 200, "{\"importStatus\": \"COMPLETE\"}");
    return createdCdes;
  }

  private void category(String cedarId, String identifier, String name, String parent) {
    ObjectNode node = JSON.createObjectNode();
    node.put("@id", cedarId);
    node.put("schema:identifier", identifier);
    node.put("schema:name", name);
    node.put("schema:description", name);
    node.put("parentCategoryId", parent);
    categories.put(cedarId, node);
  }

  private void serveTheCategoryTree() {
    cedar.answer("GET", "/resource/categories/tree", request -> new Reply(200, tree("root").toString()));
    cedar.answer("POST", "/resource/categories", request -> {
      JsonNode body = read(request);
      String cedarId = "cedar-new-" + categories.size();
      category(cedarId, body.get("schema:identifier").asText(), body.get("schema:name").asText(),
          body.get("parentCategoryId").asText());
      return new Reply(201, "{\"@id\": \"" + cedarId + "\"}");
    });
    cedar.answer("PUT", "/resource/categories/", request -> {
      ObjectNode category = categories.get(idInPath(request));
      category.put("schema:name", read(request).get("schema:name").asText());
      category.put("schema:description", read(request).get("schema:description").asText());
      return new Reply(200, category.toString());
    });
    cedar.answer("DELETE", "/resource/categories/", request -> {
      categories.remove(idInPath(request));
      return new Reply(204, null);
    });
  }

  private ObjectNode tree(String cedarId) {
    ObjectNode node = categories.get(cedarId).deepCopy();
    ArrayNode children = node.putArray("children");
    for (ObjectNode category : categories.values()) {
      if (cedarId.equals(category.path("parentCategoryId").asText(null))) {
        children.add(tree(category.get("@id").asText()));
      }
    }
    return node;
  }

  private void serveTheCdeFolder(List<ObjectNode> summaries) {
    cedar.answer("GET", "/resource/folders/" + FOLDER_UUID + "/contents-extract", request -> {
      ObjectNode page = JSON.createObjectNode();
      page.put("totalCount", summaries.size());
      page.putArray("resources").addAll(summaries);
      return new Reply(200, page.toString());
    });
  }

  private static ObjectNode summary(String cedarId, String publicId, String hash) {
    ObjectNode summary = JSON.createObjectNode();
    summary.put("@id", cedarId);
    summary.put("schema:identifier", publicId);
    summary.put("pav:version", "1.0.0");
    summary.put("sourceHash", hash);
    summary.putArray("categories");
    return summary;
  }

  private String cedarId(String identifier) {
    for (ObjectNode category : categories.values()) {
      if (category.get("schema:identifier").asText().equals(identifier)) {
        return category.get("@id").asText();
      }
    }
    throw new AssertionError("CEDAR has no category " + identifier + ": " + categories.keySet());
  }

  private String parentOf(String identifier) {
    return categories.get(cedarId(identifier)).get("parentCategoryId").asText();
  }

  private static String idInPath(Request request) {
    return URLDecoder.decode(request.path().substring("/resource/categories/".length()), StandardCharsets.UTF_8);
  }

  /** The categories the CDEs name: CIP's NCIA projects, one of them with a sub-project, and NCI Standards' MSAS. */
  private static String classificationsXml() throws Exception {
    Classifications classifications = new Classifications();
    CSI rider = item("Rider Project", 2812022);
    rider.getCSI().add(item("Rider Subproject", 9000001));
    classifications.getContext().add(context("CIP", "2.31", scheme("NCIA Projects", 2857181, rider)));
    classifications.getContext().add(context("NCI Standards", "1", scheme("PRO HRQoL", 5675693, item("MSAS", 5840828))));
    StringWriter xml = new StringWriter();
    JAXBContext.newInstance(Classifications.class).createMarshaller().marshal(classifications, xml);
    return xml.toString();
  }

  private static Context context(String name, String version, ClassificationScheme scheme) {
    Context context = new Context();
    context.setPreferredName(name);
    context.setVersion(new BigDecimal(version));
    context.getClassificationScheme().add(scheme);
    return context;
  }

  private static ClassificationScheme scheme(String name, int publicId, CSI item) {
    ClassificationScheme scheme = new ClassificationScheme();
    scheme.setPreferredName(name);
    scheme.setLongName(name);
    scheme.setPublicId(BigInteger.valueOf(publicId));
    scheme.setVersion(new BigDecimal("1"));
    scheme.getCSI().add(item);
    return scheme;
  }

  private static CSI item(String name, int publicId) {
    CSI item = new CSI();
    item.setClassificationSchemeItemName(name);
    item.setClassificationSchemeItemType("CATEGORY_TYPE");
    item.setPublicId(BigInteger.valueOf(publicId));
    item.setVersion(new BigDecimal("1"));
    return item;
  }

  private static String cdeListXml(String... publicIds) throws IOException {
    StringBuilder xml = new StringBuilder("<DataElementsList>");
    for (String publicId : publicIds) {
      try (InputStream in = CadsrCategoriesAndCdesUpdaterToolTest.class.getResourceAsStream("/cde-sample-" + publicId + ".xml")) {
        xml.append(new String(in.readAllBytes(), StandardCharsets.UTF_8));
      }
    }
    return xml.append("</DataElementsList>").toString();
  }

  private static DataElement sample(String publicId) throws Exception {
    try (InputStream in = CadsrCategoriesAndCdesUpdaterToolTest.class.getResourceAsStream("/cde-sample-" + publicId + ".xml")) {
      return CdeUtil.getDataElement(in);
    }
  }

  private static Path zip(Path zip, String entry, String content) throws IOException {
    Files.write(zip, zipBytes(entry, content));
    return zip;
  }

  private static byte[] zipBytes(String entry, String content) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (ZipOutputStream out = new ZipOutputStream(bytes)) {
      out.putNextEntry(new ZipEntry(entry));
      out.write(content.getBytes(StandardCharsets.UTF_8));
      out.closeEntry();
    }
    return bytes.toByteArray();
  }

  private static JsonNode read(Request request) {
    try {
      return JSON.readTree(request.body());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
