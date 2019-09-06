package org.metadatacenter.cadsr.ingestor.category;

import org.metadatacenter.cadsr.ingestor.Constants;

import java.util.Objects;

public class Category {

  private String id;
  private String uniqueId; // Unique identifier. It will be used to build the tree
  private String name;
  private String description;
  private String parentId;

  public Category(String id, String uniqueId, String name, String description, String parentId) {
    this.id = id;
    this.uniqueId = uniqueId;
    this.name = name;
    this.description = description;
    this.parentId = parentId;
  }

  public String getId() {
    return id;
  }

  public String getUniqueId() {
    return uniqueId;
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
    return Objects.equals(getId(), category.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }

  @Override
  public String toString() {
    return "Category{" +
        "id='" + id + '\'' +
        ", uniqueId='" + uniqueId + '\'' +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", parentId='" + parentId + '\'' +
        '}';
  }

}
