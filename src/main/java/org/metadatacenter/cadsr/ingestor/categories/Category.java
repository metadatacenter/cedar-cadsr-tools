package org.metadatacenter.cadsr.ingestor.categories;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Category {

  @JsonProperty("schema:identifier")
  private String id;
  @JsonProperty("schema:name")
  private String name;
  @JsonProperty("schema:description")
  private String description;
  private String parentCategoryId;

  public Category() {}

  public Category(String id, String name, String description, String parentCategoryId) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.parentCategoryId = parentCategoryId;
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

  public void setParentCategoryId(String parentCategoryId) {
    this.parentCategoryId = parentCategoryId;
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
        Objects.equals(getParentCategoryId(), category.getParentCategoryId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getParentCategoryId());
  }

  @Override
  public String toString() {
    return "Category{" +
        "id='" + id + '\'' +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", parentCategoryId='" + parentCategoryId + '\'' +
        '}';
  }
}
