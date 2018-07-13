package org.metadatacenter.cadsr.ingestor;

import java.util.Objects;

public class Term {
  final String conceptId;
  final String prefLabel;
  final String label;

  public Term(String conceptId, String prefLabel, String label) {
    this.conceptId = conceptId;
    this.prefLabel = prefLabel;
    this.label = label;
  }

  @Override
  public int hashCode() {
    return Objects.hash(conceptId, prefLabel, label);
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
        && Objects.equals(prefLabel, other.prefLabel)
        && Objects.equals(label, other.label);
  }
}