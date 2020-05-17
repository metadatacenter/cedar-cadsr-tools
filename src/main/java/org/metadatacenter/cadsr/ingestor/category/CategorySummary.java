package org.metadatacenter.cadsr.ingestor.category;

public class CategorySummary {

  private String cedarId;
  private String hashCode;

  public CategorySummary() {
  }

  public CategorySummary(String cedarId, String hashCode) {
    this.cedarId = cedarId;
    this.hashCode = hashCode;
  }

  public String getCedarId() {
    return cedarId;
  }

  public String getHashCode() {
    return hashCode;
  }
}
