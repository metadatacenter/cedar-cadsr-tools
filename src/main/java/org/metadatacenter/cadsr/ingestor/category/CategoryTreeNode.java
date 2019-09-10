package org.metadatacenter.cadsr.ingestor.category;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder({"id", "name","description", "children"})
public class CategoryTreeNode {

  private String id;
  private String name;
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

}
