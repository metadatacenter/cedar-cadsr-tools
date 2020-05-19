package org.metadatacenter.cadsr.ingestor.category;

import com.fasterxml.jackson.annotation.JsonProperty;

// Used to call the Cedar endpoint to update a category
public class BasicCedarCategory {

  @JsonProperty("schema:identifier")
  private String id; // Unique identifier (see details in Category.java)
  @JsonProperty("schema:name")
  private String name;
  @JsonProperty("schema:description")
  private String description;

  public BasicCedarCategory(String id, String name, String description) {
    this.id = id;
    this.name = name;
    this.description = description;
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

  public void setName(String name) {
    this.name = name;
  }
}


