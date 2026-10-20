package org.metadatacenter.cadsr.ingestor.util;

import org.junit.jupiter.api.Test;
import org.metadatacenter.cadsr.category.schema.CSI;
import org.metadatacenter.cadsr.category.schema.ClassificationScheme;
import org.metadatacenter.cadsr.category.schema.Classifications;
import org.metadatacenter.cadsr.category.schema.Context;
import org.metadatacenter.cadsr.ingestor.category.Category;
import org.metadatacenter.cadsr.ingestor.category.CategoryTreeNode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.metadatacenter.cadsr.ingestor.util.Constants.CADSR_CATEGORY_SCHEMA_ORG_ID;

/**
 * The conversion of caDSR classifications into the category tree CEDAR imports: a context at the
 * root, its classification schemes below it, and their items below those, to any depth. Category
 * names must be unique among siblings, so siblings that share a name are told apart by their
 * versions.
 */
class CategoryUtilTest {

  @Test
  void classificationsBecomeANestedTree() {
    Classifications classifications = new Classifications();
    Context context = context("NCIP", "1");
    ClassificationScheme scheme = scheme("Disease", 123, "2.0", "Disease Classification");
    CSI cancer = item("Cancer", 5, "1");
    cancer.getCSI().add(item("Lung Cancer", 6, "1"));
    scheme.getCSI().add(cancer);
    context.getClassificationScheme().add(scheme);
    classifications.getContext().add(context);

    List<CategoryTreeNode> tree = CategoryUtil.classificationsToCdeCategoryTree(classifications);

    assertEquals(1, tree.size());
    CategoryTreeNode contextNode = tree.get(0);
    assertEquals("NCIP", contextNode.getName());
    assertEquals(CADSR_CATEGORY_SCHEMA_ORG_ID + "/NCIP_V1", contextNode.getUniqueId());

    CategoryTreeNode schemeNode = only(contextNode.getChildren());
    assertEquals("Disease Classification", schemeNode.getName(), "a scheme is named by its long name");
    assertEquals(contextNode.getUniqueId() + "/123_V2.0", schemeNode.getUniqueId());
    assertEquals("2.0", schemeNode.getVersion());

    CategoryTreeNode cancerNode = only(schemeNode.getChildren());
    assertEquals("Cancer", cancerNode.getName());
    assertEquals(schemeNode.getUniqueId() + "/5_V1", cancerNode.getUniqueId());
    assertEquals(cancerNode.getUniqueId(), only(cancerNode.getChildren()).getParentUniqueId());
    assertEquals("Lung Cancer", only(cancerNode.getChildren()).getName());
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

    CategoryTreeNode schemeNode = only(only(CategoryUtil.classificationsToCdeCategoryTree(classifications)).getChildren());

    assertEquals(List.of("Cancer 1", "Cancer 2"),
        schemeNode.getChildren().stream().map(CategoryTreeNode::getName).sorted().toList());
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

  private static CategoryTreeNode only(List<CategoryTreeNode> nodes) {
    assertEquals(1, nodes.size(), "expected one node: " + nodes);
    return nodes.get(0);
  }
}
