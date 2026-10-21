package org.metadatacenter.cadsr.ingestor.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.JAXBContext;
import org.junit.jupiter.api.Test;
import org.metadatacenter.cadsr.category.schema.CSI;
import org.metadatacenter.cadsr.category.schema.ClassificationScheme;
import org.metadatacenter.cadsr.category.schema.Classifications;
import org.metadatacenter.cadsr.category.schema.Context;
import org.metadatacenter.cadsr.ingestor.category.Category;
import org.metadatacenter.cadsr.ingestor.category.CategoryStats;
import org.metadatacenter.cadsr.ingestor.category.CategorySummary;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.metadatacenter.cadsr.ingestor.util.Constants.CADSR_CATEGORY_SCHEMA_ORG_ID;
import static org.metadatacenter.cadsr.ingestor.util.Constants.CDE_CATEGORY_IDS_FIELD;

/**
 * The conversion of caDSR classifications into the categories CEDAR imports: a context at the root, its
 * classification schemes below it, and their items below those, to any depth. Category names must be unique among
 * siblings, so siblings that share a name are told apart by their versions. A CDE names its categories by caDSR
 * identifier, which spells out the context and the scheme but not the items between, so a category is found by that
 * identifier and may stand for several categories in the tree.
 */
class CategoryUtilTest {

  @Test
  void classificationsBecomeCategoriesThatNameTheirParents() throws Exception {
    Classifications classifications = new Classifications();
    Context context = context("NCIP", "1");
    ClassificationScheme scheme = scheme("Disease", 123, "2.0", "Disease Classification");
    CSI cancer = item("Cancer", 5, "1");
    cancer.getCSI().add(item("Lung Cancer", 6, "1"));
    scheme.getCSI().add(cancer);
    context.getClassificationScheme().add(scheme);
    classifications.getContext().add(context);

    // The updater reads the classifications from NCI's XML.
    StringWriter xml = new StringWriter();
    JAXBContext.newInstance(Classifications.class).createMarshaller().marshal(classifications, xml);
    List<Category> categories = CategoryUtil.classificationsToCategoriesList(
        CategoryUtil.getClassifications(new ByteArrayInputStream(xml.toString().getBytes(StandardCharsets.UTF_8))));

    assertEquals(4, categories.size());
    Category contextCategory = named(categories, "NCIP");
    assertEquals(CADSR_CATEGORY_SCHEMA_ORG_ID + "/NCIP_V1", contextCategory.getUniqueId());
    assertEquals(CADSR_CATEGORY_SCHEMA_ORG_ID, contextCategory.getParentUniqueId());

    Category schemeCategory = named(categories, "Disease Classification");
    assertEquals(contextCategory.getUniqueId() + "/123_V2.0", schemeCategory.getUniqueId(),
        "a scheme is named by its long name and identified by its public id");
    assertEquals(contextCategory.getUniqueId(), schemeCategory.getParentUniqueId());
    assertEquals("2.0", schemeCategory.getVersion());

    Category cancerCategory = named(categories, "Cancer");
    Category lungCancerCategory = named(categories, "Lung Cancer");
    assertEquals(schemeCategory.getUniqueId() + "/5_V1", cancerCategory.getUniqueId());
    assertEquals(cancerCategory.getUniqueId(), lungCancerCategory.getParentUniqueId());
    assertEquals(CADSR_CATEGORY_SCHEMA_ORG_ID + "/NCIP_V1/123_V2.0/6_V1", lungCancerCategory.getCadsrId(),
        "a nested item's caDSR identifier names its context and scheme, and not the items between");
  }

  @Test
  void siblingsThatShareANameAreToldApartByVersion() {
    Classifications classifications = new Classifications();
    Context context = context("NCIP", "1");
    ClassificationScheme scheme = scheme("Disease", 123, "1", null);
    scheme.getCSI().add(item("Cancer", 5, "1"));
    scheme.getCSI().add(item("Cancer", 7, "2"));
    context.getClassificationScheme().add(scheme);
    classifications.getContext().add(context);

    List<Category> categories = CategoryUtil.classificationsToCategoriesList(classifications);
    String schemeUniqueId = named(categories, "Disease").getUniqueId();

    assertEquals(List.of("Cancer 1", "Cancer 2"), categories.stream()
        .filter(c -> c.getParentUniqueId().equals(schemeUniqueId)).map(Category::getName).sorted().toList());
  }

  /**
   * Suffixing can itself leave two siblings with one name, when they also share a version. A third
   * sibling of that name must rename each of them once, and no other category.
   */
  @Test
  void everyExistingSiblingOfTheNameIsRenamedOnce() {
    List<Category> categories = new ArrayList<>();
    categories.add(category("Cancer 1", "1", "p/a"));
    categories.add(category("Other", "1", "p/b"));
    categories.add(category("Cancer 1", "3", "p/c"));

    CategoryUtil.addCategoryToList(category("Cancer 1", "4", "p/d"), categories);

    Map<String, String> names = categories.stream()
        .collect(Collectors.toMap(Category::getUniqueId, Category::getName));
    assertEquals(Map.of("p/a", "Cancer 1 1", "p/b", "Other", "p/c", "Cancer 1 3", "p/d", "Cancer 1 4"), names);
    assertEquals(4, categories.size());
  }

  @Test
  void identifiersCarryNeitherSpacesNorSlashes() {
    assertEquals("LungCancer_V1", CategoryUtil.generateCategoryLocalId("Lung Cancer", Optional.empty(), "1"));
    assertEquals("ab_V2", CategoryUtil.generateCategoryLocalId("a/b", Optional.empty(), "2"));
    assertEquals("6_V1", CategoryUtil.generateCategoryLocalId("Lung Cancer", Optional.of("6"), "1"));
    assertEquals(CADSR_CATEGORY_SCHEMA_ORG_ID + "/ctx/cs/6_V1",
        CategoryUtil.generateCadsrCategoryId("6_V1", Optional.of("ctx"), Optional.of("cs")));
    assertEquals(CADSR_CATEGORY_SCHEMA_ORG_ID + "/ctx/6_V1",
        CategoryUtil.generateCadsrCategoryId("6_V1", Optional.of("ctx"), Optional.empty()));
  }

  /**
   * Categories that share a caDSR identifier, an item filed under two parents in one scheme, are each attached to
   * the CDEs that name that identifier.
   */
  @Test
  void aCaDsrIdentifierFindsEveryCategoryItStandsFor() {
    String scheme = CADSR_CATEGORY_SCHEMA_ORG_ID + "/ctx/cs";
    Map<String, String> uniqueIdsToCedarIds = Map.of(
        scheme + "/5_V1", "cedar-5",
        scheme + "/5_V1/6_V1", "cedar-6-under-5",
        scheme + "/7_V1/6_V1", "cedar-6-under-7");

    Map<String, Set<String>> cadsrIdsToCedarIds =
        CategoryUtil.generateCategoryCadsrIdsToCedarCategoryIdsMap(uniqueIdsToCedarIds);

    assertEquals(Map.of(
        scheme + "/5_V1", Set.of("cedar-5"),
        scheme + "/6_V1", Set.of("cedar-6-under-5", "cedar-6-under-7")), cadsrIdsToCedarIds);

    String unknown = scheme + "/9_V1";
    Map<String, Object> cde = new HashMap<>();
    cde.put(CDE_CATEGORY_IDS_FIELD, List.of(scheme + "/6_V1", unknown));
    assertEquals(Set.of("cedar-6-under-5", "cedar-6-under-7"),
        new HashSet<>(CategoryUtil.extractCategoryCedarIdsFromCdeField(cde, cadsrIdsToCedarIds)));
    assertTrue(CategoryStats.getInstance().idsOfCategoriesNotFoundInSource.contains(unknown),
        "a category the CDE names and the categories file lacks is reported");
    assertEquals(List.of(scheme + "/6_V1", unknown), CategoryUtil.extractCategoryCadsrIdsFromCdeField(cde));
    assertEquals(List.of(), CategoryUtil.extractCategoryCadsrIdsFromCdeField(new HashMap<>()));
  }

  /** The category tree CEDAR serves is read into a map from caDSR unique identifier to CEDAR identifier. */
  @Test
  void theCedarTreeIsReadIntoIdentifiers() throws Exception {
    JsonNode tree = new ObjectMapper().readTree("""
        {"@id": "root", "children": [
          {"@id": "cedar-cadsr", "schema:identifier": "CADSR-CATEGORY-ID", "children": [
            {"@id": "cedar-ctx", "schema:identifier": "CADSR-CATEGORY-ID/ctx", "children": []}]},
          {"@id": "cedar-other", "schema:identifier": "OTHER"}]}""");

    Map<String, String> ids = CategoryUtil.getCategoryUniqueIdsFromCategoryTree(tree);

    assertEquals(Map.of("CADSR-CATEGORY-ID", "cedar-cadsr", "CADSR-CATEGORY-ID/ctx", "cedar-ctx",
        "OTHER", "cedar-other"), ids);
  }

  /** The updater manages the categories below caDSR's root and leaves the root and everything else alone. */
  @Test
  void onlyCategoriesBelowTheCaDsrRootAreManaged() {
    CategorySummary summary = new CategorySummary("cedar", null, "hash");
    Map<String, CategorySummary> all = Map.of(
        CADSR_CATEGORY_SCHEMA_ORG_ID, summary,
        CADSR_CATEGORY_SCHEMA_ORG_ID + "/ctx", summary,
        "OTHER/ctx", summary);
    assertEquals(Set.of(CADSR_CATEGORY_SCHEMA_ORG_ID + "/ctx"), CategoryUtil.extractCdeCategoriesMap(all).keySet());
  }

  /** A category is updated when what CEDAR stores of it, its identifier, name and description, changes. */
  @Test
  void theHashFollowsWhatCedarStores() {
    Category category = category("Cancer", "1", "p/a");
    String hash = CategoryUtil.generateCategoryHashCode(category);
    assertEquals(hash, CategoryUtil.generateCategoryHashCode(category("Cancer", "1", "p/a")));
    category.setName("Cancers");
    assertNotEquals(hash, CategoryUtil.generateCategoryHashCode(category));
  }

  private static Context context(String name, String version) {
    Context context = new Context();
    context.setPreferredName(name);
    context.setVersion(new BigDecimal(version));
    return context;
  }

  private static ClassificationScheme scheme(String name, int publicId, String version, String longName) {
    ClassificationScheme scheme = new ClassificationScheme();
    scheme.setPreferredName(name);
    scheme.setPublicId(BigInteger.valueOf(publicId));
    scheme.setVersion(new BigDecimal(version));
    scheme.setLongName(longName);
    return scheme;
  }

  private static CSI item(String name, int publicId, String version) {
    CSI item = new CSI();
    item.setClassificationSchemeItemName(name);
    item.setClassificationSchemeItemType("DISEASE_TYPE");
    item.setPublicId(BigInteger.valueOf(publicId));
    item.setVersion(new BigDecimal(version));
    return item;
  }

  private static Category category(String name, String version, String uniqueId) {
    return new Category(null, version, name + "_V" + version, null, uniqueId, name, name, null, "p");
  }

  private static Category named(List<Category> categories, String name) {
    List<Category> matches = categories.stream().filter(c -> c.getName().equals(name)).toList();
    assertEquals(1, matches.size(), "expected one category named " + name + ": " + matches);
    return matches.get(0);
  }
}
