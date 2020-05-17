package org.metadatacenter.cadsr.ingestor.cde;

public class CdeSummary {

  private String cedarId;
  private String hashCode;

  public CdeSummary() {
  }

  public CdeSummary(String cedarId, String hashCode) {
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
