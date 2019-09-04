package org.metadatacenter.cadsr.ingestor.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder({"schema:identifier", "schema:name","schema:description", "children"})
public class CategoryTreeNode {

  @JsonProperty("schema:identifier")
  private String id;
  @JsonProperty("schema:name")
  private String name;
  @JsonProperty("schema:description")
  private String description;
  private List<CategoryTreeNode> children;

  public CategoryTreeNode() {
  }

  public CategoryTreeNode(String id, String name, String description, List<CategoryTreeNode> children) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.children = children;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public List<CategoryTreeNode> getChildren() {
    return children;
  }

  public void setChildren(List<CategoryTreeNode> children) {
    this.children = children;
  }

  //  @Override
//  public boolean equals(Object o) {
//    if (this == o) {
//      return true;
//    }
//    if (o == null || getClass() != o.getClass()) {
//      return false;
//    }
//    CategoryTreeNode category = (CategoryTreeNode) o;
//    return Objects.equals(getId(), category.getId()) &&
//        Objects.equals(getParentCategoryId(), category.getParentCategoryId());
//  }
//
//  @Override
//  public int hashCode() {
//    return Objects.hash(getId(), getParentCategoryId());
//  }
//
//  @Override
//  public String toString() {
//    return "Category{" +
//        "id='" + id + '\'' +
//        ", name='" + name + '\'' +
//        ", description='" + description + '\'' +
//        ", parentCategoryId='" + parentCategoryId + '\'' +
//        '}';
//  }
}
