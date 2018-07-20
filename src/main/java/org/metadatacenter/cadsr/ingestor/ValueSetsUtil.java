package org.metadatacenter.cadsr.ingestor;

import org.metadatacenter.cadsr.DataElement;

import static org.metadatacenter.cadsr.ingestor.Constants.CDE_VALUESETS_ONTOLOGY_IRI;

public class ValueSetsUtil {

  public static String generateValueSetIRI(DataElement dataElement) {
    String valueSetId = dataElement.getVALUEDOMAIN().getPublicId().getContent();
    String valueSetVersion = dataElement.getVALUEDOMAIN().getVersion().getContent();
    return generateValueSetIRI(valueSetId, valueSetVersion);
  }

  public static String generateValueSetIRI(String valueSetId, String valueSetVersion) {
    return CDE_VALUESETS_ONTOLOGY_IRI + "#" + generateValueSetId(valueSetId, valueSetVersion);
  }

  public static String generateValueSetId(String valueSetId, String valueSetVersion) {
    return "VS_VD" + valueSetId + "v" + valueSetVersion;
  }

}
