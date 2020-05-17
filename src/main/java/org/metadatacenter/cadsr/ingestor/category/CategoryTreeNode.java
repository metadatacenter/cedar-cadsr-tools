package org.metadatacenter.cadsr.ingestor.category;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder({"id", "cadsrId", "name","description", "children", "parentId"})
public class CategoryTreeNode {

  private String id;
  private String cadsrId;
  private String name;
  private String description;
  private List<CategoryTreeNode> children;
  private String parentId;

  public CategoryTreeNode() {
  }

  public CategoryTreeNode(String id, String cadsrId, String name, String description, List<CategoryTreeNode> children, String parentId) {
    this.id = id;
    this.cadsrId = cadsrId;
    this.name = name;
    this.description = description;
    this.children = children;
    this.parentId = parentId;
  }

  public String getId() {
    return id;
  }

  public String getCadsrId() {
    return cadsrId;
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

  public String getParentId() { return parentId; }
}
