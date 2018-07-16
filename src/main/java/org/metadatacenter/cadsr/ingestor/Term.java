package org.metadatacenter.cadsr.ingestor;

import java.util.Objects;

public class Term {
  final String conceptId;
  final String dbLabel;
  final String uiLabel;
  final String description;

  public Term(String conceptId, String dbLabel, String displayLabel, String description) {
    this.conceptId = conceptId;
    this.dbLabel = dbLabel;
    this.uiLabel = displayLabel;
    this.description = description;
  }

  @Override
  public int hashCode() {
    return Objects.hash(conceptId, dbLabel, uiLabel, description);
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
    return Objects.equals(conceptId, other.conceptId)
        && Objects.equals(dbLabel, other.dbLabel)
        && Objects.equals(uiLabel, other.uiLabel)
        && Objects.equals(description, other.description);
  }
}