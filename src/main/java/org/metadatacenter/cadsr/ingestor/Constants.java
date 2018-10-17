package org.metadatacenter.cadsr.ingestor;

public final class Constants {

  // Schema.org URIs
  public static final String SCHEMAORG_IRI = "https://schema.org/";
  public static final String SCHEMAORG_STARTTIME_IRI = SCHEMAORG_IRI + "startTime";
  public static final String SCHEMAORG_ENDTIME_IRI = SCHEMAORG_IRI + "endTime";
  // Dublic Core URIs
  public static final String DUBLINCORE_IRI = "http://purl.org/dc/terms/";
  public static final String DUBLINCORE_IDENTIFIER_IRI = DUBLINCORE_IRI + "identifier";
  public static final String DUBLINCORE_VERSION_IRI = DUBLINCORE_IRI + "hasVersion";
  // SKOS URIs
  public static final String SKOS_IRI = "http://www.w3.org/2004/02/skos/core#";
  public static final String SKOS_NOTATION_IRI = SKOS_IRI + "notation";

  // Maximum number of enumerated values, or minimum number of values for the value set to be defined as a BioPortal
  // value set. A value of 0 means that all value sets will be created as BioPortal value sets.
  public static final int MAX_ENUMERATED_TERMS = 0;

  public static final String CDE_VALUESETS_ONTOLOGY_ID = "CADSR-VS";
  public static final String CDE_VALUESETS_ONTOLOGY_IRI = "https://cadsr.nci.nih.gov/metadata/" + CDE_VALUESETS_ONTOLOGY_ID;
  public static final String CDE_VALUESETS_ONTOLOGY_NAME = "cadsr-vs.owl";
}
