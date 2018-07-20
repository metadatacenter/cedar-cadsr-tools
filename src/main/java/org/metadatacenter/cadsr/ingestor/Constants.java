package org.metadatacenter.cadsr.ingestor;

public final class Constants {

  public static final String NCIT_ONTOLOGY_IRI = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#";
  public static final String NCIT_ONTOLOGY_LABEL = "NCIT";

  // Maximum number of enumerated values, or minimum number of values for the value set to be defined as a BioPortal
  // value set. A value of 0 means that all value sets will be created as BioPortal value sets.
  public static final int MAX_ENUMERATED_TERMS = 0;

  public static final String CDE_VALUESETS_ONTOLOGY_ID = "CADSR-VS";
  public static final String CDE_VALUESETS_ONTOLOGY_IRI = "https://cadsr.nci.nih.gov/metadata/" + CDE_VALUESETS_ONTOLOGY_ID;
  public static final String CDES_FOLDER = "cdes";
  public static final String CDE_VALUESETS_ONTOLOGY_FOLDER = "ontology";
  public static final String CDE_VALUESETS_ONTOLOGY_NAME = "cdesvs.owl";
  //public static final String CDE_STATUS_RELEASED = "RELEASED";

}
