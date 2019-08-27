package org.metadatacenter.cadsr.ingestor.categories;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Category {

  @JsonProperty("schema:identifier")
  private String id;
  private String name;
  private String description;
  private String parentId;

  public Category(String id, String name, String description, String parentId) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.parentId = parentId;
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

  public String getParentId() {
    return parentId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Category category = (Category) o;
    return Objects.equals(getId(), category.getId()) &&
        Objects.equals(getParentId(), category.getParentId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getParentId());
  }

}
