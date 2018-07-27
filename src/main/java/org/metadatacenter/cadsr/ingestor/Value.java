package org.metadatacenter.cadsr.ingestor;

import java.util.Objects;

public class Value {
  private final String id; // VMPUBLICID
  private final String version; // VMVERSION
  private final String dbLabel; // VALIDVALUE
  private final String displayLabel; // VALUEMEANING
  private final String relatedTermUri; // MEANINGCONCEPTS
  private final String description; // MEANINGDESCRIPTION
  private final String beginDate; // PVBEGINDATE
  private final String endDate; // PVENDDATE


  public Value(String id, String version, String dbLabel, String displayLabel, String relatedTermUri, String
      description, String beginDate, String endDate) {
    if (id != null && id.length() == 0) {
      id = null;
    }
    if (version != null && version.length() == 0) {
      version = null;
    }
    if (dbLabel != null && dbLabel.length() == 0) {
      dbLabel = null;
    }
    if (displayLabel != null && displayLabel.length() == 0) {
      displayLabel = null;
    }
    if (relatedTermUri != null && relatedTermUri.length() == 0) {
      relatedTermUri = null;
    }
    if (description != null && description.length() == 0) {
      description = null;
    }
    if (beginDate != null && beginDate.length() == 0) {
      beginDate = null;
    }
    if (endDate != null && endDate.length() == 0) {
      endDate = null;
    }
    this.id = id;
    this.version = version;
    this.dbLabel = dbLabel;
    this.relatedTermUri = relatedTermUri;
    this.displayLabel = displayLabel;
    this.description = description;
    this.beginDate = beginDate;
    this.endDate = endDate;
  }

  public String getId() {
    return id;
  }

  public String getVersion() {
    return version;
  }

  public String getDbLabel() {
    return dbLabel;
  }

  public String getDisplayLabel() {
    return displayLabel;
  }

  public String getRelatedTermUri() {
    return relatedTermUri;
  }

  public String getDescription() {
    return description;
  }

  public String getBeginDate() {
    return beginDate;
  }

  public String getEndDate() {
    return endDate;
  }

  @Override
  public String toString() {
    return "Value{" +
        "id='" + id + '\'' +
        ", version='" + version + '\'' +
        ", dbLabel='" + dbLabel + '\'' +
        ", displayLabel='" + displayLabel + '\'' +
        ", relatedTermUri='" + relatedTermUri + '\'' +
        ", description='" + description + '\'' +
        ", beginDate='" + beginDate + '\'' +
        ", endDate='" + endDate + '\'' +
        '}';
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, version, dbLabel, displayLabel,
        relatedTermUri, description, beginDate, endDate);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Value)) {
      return false;
    }
    Value other = (Value) obj;
    return Objects.equals(id, other.id)
        && Objects.equals(version, other.version)
        && Objects.equals(dbLabel, other.dbLabel)
        && Objects.equals(displayLabel, other.displayLabel)
        && Objects.equals(relatedTermUri, other.relatedTermUri)
        && Objects.equals(description, other.description)
        && Objects.equals(beginDate, other.beginDate)
        && Objects.equals(endDate, other.endDate);
  }
}