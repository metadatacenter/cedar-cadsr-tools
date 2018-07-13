package org.metadatacenter.cadsr.ingestor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.metadatacenter.cadsr.DataElement;
import org.metadatacenter.cadsr.PermissibleValues;
import org.metadatacenter.cadsr.PermissibleValuesITEM;
import org.metadatacenter.model.ModelNodeNames;
import org.metadatacenter.model.ModelNodeValues;

import java.util.*;

import static org.metadatacenter.cadsr.ingestor.Constants.MAX_ENUMERATED_TERMS;
import static org.metadatacenter.cadsr.ingestor.Constants.NCIT_ONTOLOGY_IRI;
import static org.metadatacenter.cadsr.ingestor.Constants.NCIT_ONTOLOGY_LABEL;

public class PermissibleValuesHandler implements ModelHandler {

  private static final String ENUMERATED = "Enumerated";
  private static final String NON_ENUMERATED = "NonEnumerated";

  private final List<Map<String, Object>> ontologies = Lists.newArrayList();
  private final List<Map<String, Object>> valueSets = Lists.newArrayList();
  private final List<Map<String, Object>> classes = Lists.newArrayList();
  private final List<Map<String, Object>> branches = Lists.newArrayList();

  public PermissibleValuesHandler handle(DataElement dataElement) throws UnsupportedDataElementException {
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

  private void handleEnumeratedType(DataElement dataElement) throws UnsupportedDataElementException {
    PermissibleValues permissibleValues = dataElement.getVALUEDOMAIN().getPermissibleValues();
    Set<Term> termSet = getTermSet(permissibleValues, dataElement);

    if (termSet.size() <= MAX_ENUMERATED_TERMS) {
      setListOfClasses(termSet);
    }
    else {
      ValueSetsOntologyManager.addValueSetToOntology(dataElement, termSet);
    }
  }

  private Set<Term> getTermSet(PermissibleValues permissibleValues, DataElement dataElement) throws
      UnsupportedDataElementException {
    Set<Term> termSet = Sets.newHashSet();
    for (PermissibleValuesITEM permissibleItem : permissibleValues.getPermissibleValuesITEM()) {
      checkConceptNotNull(permissibleItem, dataElement);
      checkComplexConcept(permissibleItem, dataElement);
      termSet.add(new Term(
          permissibleItem.getMEANINGCONCEPTS().getContent(),
          permissibleItem.getVALUEMEANING().getContent(),
          permissibleItem.getVALIDVALUE().getContent()
      ));
    }
    //checkPermissibleValueSize(termSet, dataElement);
    return termSet;
  }

  private void setListOfClasses(Set<Term> termSet) {
    for (Term term : termSet) {
      Map<String, Object> controlledTerm = Maps.newHashMap();
      controlledTerm.put(ModelNodeNames.URI, NCIT_ONTOLOGY_IRI + term.conceptId);
      controlledTerm.put(ModelNodeNames.LABEL, term.label);
      controlledTerm.put(ModelNodeNames.PREF_LABEL, term.prefLabel);
      controlledTerm.put(ModelNodeNames.TYPE, ModelNodeValues.ONTOLOGY_CLASS);
      controlledTerm.put(ModelNodeNames.SOURCE, NCIT_ONTOLOGY_LABEL);
      classes.add(controlledTerm);
    }
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

  private static void checkComplexConcept(PermissibleValuesITEM permissibleItem, DataElement dataElement) throws
      UnsupportedDataElementException {
    String conceptId = permissibleItem.getMEANINGCONCEPTS().getContent();
    if (conceptId.contains(",") || conceptId.contains(":")) {
      String reason = String.format("Controlled term for value '%s' is a complex concept [%s] " +
          "(Unsupported)", permissibleItem.getVALUEMEANING().getContent(), conceptId);
      throw new UnsupportedDataElementException(dataElement, reason);
    }
  }

  private static void checkPermissibleValueSize(Set<Term> termSet, DataElement dataElement) throws
      UnsupportedDataElementException {
    int termSize = termSet.size();
    if (termSize > MAX_ENUMERATED_TERMS) {
      String reason = String.format("Controlled terms selection is too large = %d (Unsupported)", termSize);
      throw new UnsupportedDataElementException(dataElement, reason);
    }
  }

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
