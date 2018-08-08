package org.metadatacenter.cadsr.ingestor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.metadatacenter.cadsr.DataElement;
import org.metadatacenter.cadsr.PermissibleValues;
import org.metadatacenter.cadsr.PermissibleValuesITEM;
import org.metadatacenter.cadsr.ingestor.exception.DuplicatedAxiomException;
import org.metadatacenter.cadsr.ingestor.exception.InvalidIdentifierException;
import org.metadatacenter.cadsr.ingestor.exception.UnknownSeparatorException;
import org.metadatacenter.cadsr.ingestor.exception.UnsupportedDataElementException;
import org.metadatacenter.model.ModelNodeNames;
import org.metadatacenter.model.ModelNodeValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.metadatacenter.cadsr.ingestor.Constants.*;

public class PermissibleValuesHandler implements ModelHandler {

  private static final Logger logger = LoggerFactory.getLogger(PermissibleValuesHandler.class);

  private static final String ENUMERATED = "Enumerated";
  private static final String NON_ENUMERATED = "NonEnumerated";

  private final List<Map<String, Object>> ontologies = Lists.newArrayList();
  private final List<Map<String, Object>> valueSets = Lists.newArrayList();
  private final List<Map<String, Object>> classes = Lists.newArrayList();
  private final List<Map<String, Object>> branches = Lists.newArrayList();

  public PermissibleValuesHandler handle(DataElement dataElement) throws UnsupportedDataElementException,
      UnknownSeparatorException {
    String valueDomainType = dataElement.getVALUEDOMAIN().getValueDomainType().getContent();
    if (ENUMERATED.equals(valueDomainType)) {
      handleEnumeratedType(dataElement);
    } else if (NON_ENUMERATED.equals(valueDomainType)) {
      handleNonEnumeratedType(dataElement);
    } else {
      String reason = String.format("Value domain is not either enumerated or non-enumerated = %s (Unknown)",
          valueDomainType);
      throw new UnsupportedDataElementException(dataElement, reason);
    }
    return this;
  }

  private void handleEnumeratedType(DataElement dataElement) throws UnsupportedDataElementException,
      UnknownSeparatorException {

    PermissibleValues permissibleValues = dataElement.getVALUEDOMAIN().getPermissibleValues();
    Set<Value> values = getValues(permissibleValues, dataElement);

    if (values.size() <= MAX_ENUMERATED_TERMS) {
      String valueDomainId = dataElement.getVALUEDOMAIN().getPublicId().getContent();
      String valueDomainVersion = dataElement.getVALUEDOMAIN().getVersion().getContent();
      String valueSetId = null;
      try {
        valueSetId = ValueSetsUtil.generateValueSetId(valueDomainId, valueDomainVersion);
      } catch (InvalidIdentifierException e) {
        logger.error(e.getMessage());
      }
      setListOfClasses(valueSetId, values);
    } else {
      try {
        ValueSetsOntologyManager.addValueSetToOntology(dataElement, values);
      } catch (DuplicatedAxiomException e) {
        logger.error(e.getMessage());
      }
      setValueSet(dataElement, values.size());
    }
  }

  private Set<Value> getValues(PermissibleValues permissibleValues, DataElement dataElement) throws
      UnsupportedDataElementException, UnknownSeparatorException {
    Set<Value> termSet = Sets.newHashSet();
    for (PermissibleValuesITEM permissibleItem : permissibleValues.getPermissibleValuesITEM()) {
      String conceptUri = null;
      if (isNullConcept(permissibleItem)) {
        // use “VM” plus the Value Meaning PublicID and Version
        String publicId = permissibleItem.getVMPUBLICID().getContent();
        String version = permissibleItem.getVMVERSION().getContent();
        conceptUri = CDE_VALUESETS_ONTOLOGY_IRI + "/VM" + publicId + "v" + version;
      } else if (isComplexConcept(permissibleItem)) {
        // Use the rightmost concept (last) in the list of concepts
        conceptUri = NCIT_IRI + extractLastConcept(permissibleItem.getMEANINGCONCEPTS().getContent());
      } else { // regular concept
        conceptUri = NCIT_IRI + permissibleItem.getMEANINGCONCEPTS().getContent();
      }
      // checkConceptNotNull(permissibleItem, dataElement);
      // checkComplexConcept(permissibleItem, dataElement);
      termSet.add(new Value(
          permissibleItem.getVMPUBLICID().getContent(),
          permissibleItem.getVMVERSION().getContent(),
          permissibleItem.getVALIDVALUE().getContent(),
          permissibleItem.getVALUEMEANING().getContent(),
          conceptUri,
          permissibleItem.getMEANINGDESCRIPTION().getContent(),
          permissibleItem.getPVBEGINDATE().getContent(),
          permissibleItem.getPVENDDATE().getContent()
      ));
    }
    //checkPermissibleValueSize(termSet, dataElement);
    return termSet;
  }

  private void setListOfClasses(String valueSetId, Set<Value> values) {
    for (Value value : values) {
      Map<String, Object> cls = Maps.newHashMap();
      cls.put(ModelNodeNames.URI, ValueSetsUtil.generateValueId(valueSetId, value));
      cls.put(ModelNodeNames.LABEL, value.getDisplayLabel());
      cls.put(ModelNodeNames.PREF_LABEL, value.getDbLabel());
      cls.put(ModelNodeNames.TYPE, ModelNodeValues.ONTOLOGY_CLASS);
      cls.put(ModelNodeNames.SOURCE, NCIT_ONTOLOGY_LABEL);
      classes.add(cls);
    }
  }

  private void setValueSet(DataElement dataElement, int size) {
    Map<String, Object> valueSet = Maps.newHashMap();
    valueSet.put(ModelNodeNames.NAME, dataElement.getVALUEDOMAIN().getLongName().getContent());
    valueSet.put(ModelNodeNames.VS_COLLECTION, CDE_VALUESETS_ONTOLOGY_ID);
    valueSet.put(ModelNodeNames.URI, ValueSetsUtil.generateValueSetIRI(dataElement));
    valueSet.put(ModelNodeNames.NUM_TERMS, size);
    valueSets.add(valueSet);
  }

  private static void checkConceptNotNull(PermissibleValuesITEM permissibleItem, DataElement dataElement) throws
      UnsupportedDataElementException {
    String conceptId = permissibleItem.getMEANINGCONCEPTS().getContent();
    if ("".equals(conceptId)) {
      String reason = String.format("Controlled term for value '%s' is null (NullValue)",
          permissibleItem.getVALUEMEANING().getContent());
      throw new UnsupportedDataElementException(dataElement, reason);
    }
  }

  private static boolean isNullConcept(PermissibleValuesITEM permissibleItem) {
    String conceptId = permissibleItem.getMEANINGCONCEPTS().getContent();
    if (conceptId == null || conceptId.length() == 0) {
      return true;
    } else {
      return false;
    }
  }

  private static void checkComplexConcept(PermissibleValuesITEM permissibleItem, DataElement dataElement) throws
      UnsupportedDataElementException {
    String conceptId = permissibleItem.getMEANINGCONCEPTS().getContent();
    if (conceptId.contains(",") || conceptId.contains(":")) {
      String reason = String.format("Controlled term for value '%s' is a complex concept [%s] " +
          "(Unsupported)", permissibleItem.getVALUEMEANING().getContent(), conceptId);
      throw new UnsupportedDataElementException(dataElement, reason);
    }
  }

  private static boolean isComplexConcept(PermissibleValuesITEM permissibleItem) {
    String conceptId = permissibleItem.getMEANINGCONCEPTS().getContent();
    if (conceptId.contains(",") || conceptId.contains(":")) {
      return true;
    } else {
      return false;
    }
  }

  private static String extractLastConcept(String complexConcept) throws UnknownSeparatorException {
    String lastConcept = null;
    int indexColon = complexConcept.lastIndexOf(":");
    int indexComma = complexConcept.lastIndexOf(",");
    if (indexColon > indexComma) {
      lastConcept = complexConcept.substring(indexColon + 1);
    }
    else if (indexColon < indexComma) {
      lastConcept = complexConcept.substring(indexComma + 1);
    }
    else {
      throw new UnknownSeparatorException("Found a complex concept with an unknown separator: " + complexConcept);
    }
    return lastConcept;
  }

//  private static void checkPermissibleValueSize(Set<Term> termSet, DataElement dataElement) throws
//      UnsupportedDataElementException {
//    int termSize = termSet.size();
//    if (termSize > MAX_ENUMERATED_TERMS) {
//      String reason = String.format("Controlled terms selection is too large = %d (Unsupported)", termSize);
//      throw new UnsupportedDataElementException(dataElement, reason);
//    }
//  }

  private void handleNonEnumeratedType(DataElement dataElement) {
    // Does nothing
  }

  public List<Map<String, Object>> getOntologies() {
    return Collections.unmodifiableList(ontologies);
  }

  public List<Map<String, Object>> getValueSets() {
    return Collections.unmodifiableList(valueSets);
  }

  public List<Map<String, Object>> getClasses() {
    return Collections.unmodifiableList(classes);
  }

  public List<Map<String, Object>> getBranches() {
    return Collections.unmodifiableList(branches);
  }

  @Override
  public void apply(Map<String, Object> fieldObject) {
    Map<String, Object> valueConstraints = (Map<String, Object>) fieldObject.get(ModelNodeNames.VALUE_CONSTRAINTS);
    valueConstraints.put(ModelNodeNames.ONTOLOGIES, getOntologies());
    valueConstraints.put(ModelNodeNames.VALUE_SETS, getValueSets());
    valueConstraints.put(ModelNodeNames.CLASSES, getClasses());
    valueConstraints.put(ModelNodeNames.BRANCHES, getBranches());
  }

}
