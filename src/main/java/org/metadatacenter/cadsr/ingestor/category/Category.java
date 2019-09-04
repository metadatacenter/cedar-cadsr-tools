package org.metadatacenter.cadsr.ingestor.category;

import java.util.List;
import java.util.Objects;

public class Category {

  private String id;
  private String name;
  private String description;
  private List<String> path; // Ids from the root to the current node, including the id of the current node.

  public Category(String id, String name, String description, List<String> path) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.path = path;
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

  public List<String> getPath() {
    return path;
  }

  public List<String> getParentPath() {
    return getPath().subList(0, getPath().size()-1);
  }

//  public String getParentCategoryId() {
//    // The id of the parent is the second to last path component
//    return this.path.get(this.path.size() - 2);
//  }

  public boolean isRoot() {
    return (this.path.size() == 2);
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
    return Objects.equals(getPath(), category.getPath());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getPath());
  }

  @Override
  public String toString() {
    return "Category{" +
        "id='" + id + '\'' +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", path=" + path +
        '}';
  }
}
