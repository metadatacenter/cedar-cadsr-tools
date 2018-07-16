package org.metadatacenter.cadsr.ingestor;

import org.metadatacenter.cadsr.DataElement;

import static org.metadatacenter.cadsr.ingestor.Constants.CDE_VALUESETS_ONTOLOGY_IRI;

public class ValueSetsUtil {

  public static String generateValueSetIRI(DataElement dataElement) {
    String id = dataElement.getPUBLICID().getContent();
    String version = dataElement.getVERSION().getContent();
    String iri = CDE_VALUESETS_ONTOLOGY_IRI + "#VS_DE" + id + "v" + version;
    return iri;
  }

}
