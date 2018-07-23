package org.metadatacenter.cadsr.ingestor;

import java.util.Objects;

public class Term {
  final String termId;
  final String termVersion;
  final String dbLabel;
  final String displayLabel;
  final String relatedTermId;
  final String description;

  public Term(String termId, String termVersion, String dbLabel, String displayLabel, String relatedTermId, String description) {
    this.termId = termId;
    this.termVersion = termVersion;
    this.dbLabel = dbLabel;
    this.relatedTermId = relatedTermId;
    this.displayLabel = displayLabel;
    this.description = description;
  }

  @Override
  public int hashCode() {
    return Objects.hash(termId, termVersion, dbLabel, displayLabel, relatedTermId, description);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Term)) {
      return false;
    }
    Term other = (Term) obj;
    return Objects.equals(termId, other.termId)
        && Objects.equals(termVersion, other.termVersion)
        && Objects.equals(dbLabel, other.dbLabel)
        && Objects.equals(displayLabel, other.displayLabel)
        && Objects.equals(relatedTermId, other.relatedTermId)
        && Objects.equals(description, other.description);
  }
}