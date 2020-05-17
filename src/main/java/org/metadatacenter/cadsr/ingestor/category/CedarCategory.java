package org.metadatacenter.cadsr.ingestor.category;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"@id", "schema:identifier", "schema:name", "schema:description", "parentCategoryId", "children"})
public class CedarCategory {

  @JsonProperty("@id")
  private String ldId;
  @JsonProperty("schema:identifier")
  private String id;
  @JsonProperty("schema:name")
  private String name;
  @JsonProperty("schema:description")
  private String description;
  private String parentCategoryId;
  private List<CedarCategory> children;

  public CedarCategory() {
  }

  public CedarCategory(String ldId, String id, String name, String description, String parentCategoryId, List<CedarCategory> children) {
    this.ldId = ldId;
    this.id = id;
    this.name = name;
    this.description = description;
    this.parentCategoryId = parentCategoryId;
    this.children = children;
  }

  public String getLdId() {
    return ldId;
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

  public String getParentCategoryId() {
    return parentCategoryId;
  }

  public List<CedarCategory> getChildren() {
    return children;
  }

  @Override
  public String toString() {
    return "CedarCategory{" +
        "ldId='" + ldId + '\'' +
        ", id='" + id + '\'' +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", parentCategoryId='" + parentCategoryId + '\'' +
        ", children=" + children +
        '}';
  }
}
