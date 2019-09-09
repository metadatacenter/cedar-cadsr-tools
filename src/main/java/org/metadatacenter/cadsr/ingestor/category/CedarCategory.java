package org.metadatacenter.cadsr.ingestor.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder({"schema:identifier", "schema:name","schema:description", "children"})
public class CedarCategory {

  @JsonProperty("schema:identifier")
  private String id;
  @JsonProperty("schema:name")
  private String name;
  @JsonProperty("schema:description")
  private String description;
  private List<CedarCategory> children;

  public CedarCategory() {
  }

  public CedarCategory(String id, String name, String description, List<CedarCategory> children) {
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

  public List<CedarCategory> getChildren() {
    return children;
  }

  public void setChildren(List<CedarCategory> children) {
    this.children = children;
  }

}
