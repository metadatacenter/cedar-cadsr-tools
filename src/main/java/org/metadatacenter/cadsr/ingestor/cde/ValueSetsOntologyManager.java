package org.metadatacenter.cadsr.ingestor.cde;

import org.metadatacenter.cadsr.cde.schema.DataElement;
import org.metadatacenter.cadsr.ingestor.exception.DuplicatedAxiomException;
import org.metadatacenter.cadsr.ingestor.exception.InvalidIdentifierException;
import org.metadatacenter.cadsr.ingestor.util.GeneralUtil;
import org.metadatacenter.cadsr.ingestor.util.ValueSetUtil;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.semanticweb.owlapi.vocab.SKOSVocabulary;
import org.semanticweb.owlapi.vocab.XSDVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Set;

import static org.metadatacenter.cadsr.ingestor.util.Constants.CDE_VALUESETS_ONTOLOGY_IRI;
import static org.metadatacenter.cadsr.ingestor.util.Constants.DUBLINCORE_IDENTIFIER_IRI;
import static org.metadatacenter.cadsr.ingestor.util.Constants.DUBLINCORE_VERSION_IRI;
import static org.metadatacenter.cadsr.ingestor.util.Constants.SCHEMAORG_ENDTIME_IRI;
import static org.metadatacenter.cadsr.ingestor.util.Constants.SCHEMAORG_STARTTIME_IRI;
import static org.metadatacenter.cadsr.ingestor.util.Constants.SKOS_NOTATION_IRI;

public class ValueSetsOntologyManager
{
  private static OWLOntologyManager manager;
  private static OWLOntology ontology;
  private static OWLDataFactory owlDataFactory;

  private static int valueSetsCount = 0;
  private static int valuesCount = 0;

  private static IRI IDENTIFIER_IRI = IRI.create(DUBLINCORE_IDENTIFIER_IRI);
  private static IRI VERSION_IRI = IRI.create(DUBLINCORE_VERSION_IRI);
  private static IRI NOTATION_IRI = IRI.create(SKOS_NOTATION_IRI);
  private static IRI RELATED_MATCH_IRI = SKOSVocabulary.RELATEDMATCH.getIRI();
  private static IRI COMMENT_IRI = OWLRDFVocabulary.RDFS_COMMENT.getIRI();
  private static IRI LABEL_IRI = OWLRDFVocabulary.RDFS_LABEL.getIRI();
  private static IRI START_TIME_IRI = IRI.create(SCHEMAORG_STARTTIME_IRI);
  private static IRI END_TIME_IRI = IRI.create(SCHEMAORG_ENDTIME_IRI);

  private static final Logger logger = LoggerFactory.getLogger(ValueSetsOntologyManager.class);

  static {
    manager = OWLManager.createOWLOntologyManager();
    owlDataFactory = manager.getOWLDataFactory();
    // Create the ontology that will contain the value sets. The root classes will represent value sets. All
    // subclasses of a rootclass will represent the values of the value set.
    try {
      ontology = manager.createOntology(IRI.create(CDE_VALUESETS_ONTOLOGY_IRI));
    } catch (OWLOntologyCreationException e) {
      logger.error("Error while creating ontology: " + e);
    }
  }


  public static void addValueSetToOntology(DataElement dataElement, Set<Value> values) throws DuplicatedAxiomException {

    String valueDomainId = GeneralUtil.getValueOrNull(dataElement.getVALUEDOMAIN().getPublicId().getContent());
    String valueDomainVersion = GeneralUtil.getValueOrNull(dataElement.getVALUEDOMAIN().getVersion().getContent());
    String valueDomainPrefName =
        GeneralUtil.getValueOrNull(dataElement.getVALUEDOMAIN().getPreferredName().getContent());
    String valueDomainPrefDefinition = GeneralUtil.getValueOrNull(dataElement.getVALUEDOMAIN().getPreferredDefinition()
        .getContent());

    // Create the class that will represent the value set
    OWLClass valueSetClass = dataElementToOWLClass(valueDomainId, valueDomainVersion);

    // Check if the ontology already contains that class
    if (!ontology.containsClassInSignature(valueSetClass.getIRI())) {

      // Add annotation properties to the value set
      String valueSetId = null;
      try {
        valueSetId = ValueSetUtil.generateValueSetId(valueDomainId, valueDomainVersion);
      } catch (InvalidIdentifierException e) {
        logger.error(e.getMessage());
      }

      logger.info("Adding value set to the ontology. ValueSetId = " + valueSetId + ". Number of values: " + values.size());

      ontology = addAnnotationAxiomToClass(IRI.create(DUBLINCORE_IDENTIFIER_IRI), valueDomainId, valueSetClass,
        owlDataFactory.getOWLDatatype(XSDVocabulary.STRING.getIRI()));

      if (valueDomainVersion != null) {
        ontology = addAnnotationAxiomToClass(IRI.create(DUBLINCORE_VERSION_IRI), valueDomainVersion, valueSetClass,
          owlDataFactory.getOWLDatatype(XSDVocabulary.STRING.getIRI()));
      }
      // This is the label that will be shown in BP's ontology hierarchy
      ontology = addAnnotationAxiomToClass(OWLRDFVocabulary.RDFS_LABEL.getIRI(), valueSetId, valueSetClass,
        owlDataFactory.getOWLDatatype(XSDVocabulary.STRING.getIRI()));

      if (valueDomainPrefName != null) {
        ontology = addAnnotationAxiomToClass(SKOSVocabulary.ALTLABEL.getIRI(), valueDomainPrefName, valueSetClass,
          owlDataFactory.getOWLDatatype(XSDVocabulary.STRING.getIRI()));
      }
      if (valueDomainPrefDefinition != null) {
        ontology = addAnnotationAxiomToClass(OWLRDFVocabulary.RDFS_COMMENT.getIRI(), valueDomainPrefDefinition,
            valueSetClass, owlDataFactory.getOWLDatatype(XSDVocabulary.STRING.getIRI()));
      }

      // Values of the value set
      for (Value value : values) {
        ontology = addPermissibleValuesToOntology(valueSetId, value, valueSetClass);
        valuesCount++;
      }
      valueSetsCount++;
    } else {
      logger.warn("The value set has not been added to the ontology because it's already there. Class IRI: " + valueSetClass.getIRI());
    }
  }

  public static OWLClass dataElementToOWLClass(String valueSetId, String valueSetVersion) {
    return owlDataFactory.getOWLClass(IRI.create(ValueSetUtil.generateValueSetIRI(valueSetId, valueSetVersion)));
  }

  public static OWLOntology addPermissibleValuesToOntology(String valueSetId, Value value, OWLClass valueSetClass)
      throws DuplicatedAxiomException {
    // Create the class that represents the value
    OWLClass valueClass = owlDataFactory.getOWLClass(ValueSetUtil.generateValueIRI(valueSetId, value));
    // Create subclass axiom
    OWLAxiom axiom = owlDataFactory.getOWLSubClassOfAxiom(valueClass, valueSetClass);
    // Add subclass axiom to the ontology
    manager.addAxiom(ontology, axiom);
    // Add annotation properties to the value
    if (value.getId() != null) {
      ontology = addAnnotationAxiomToClass(IDENTIFIER_IRI, value.getId(), valueClass,
        owlDataFactory.getOWLDatatype(XSDVocabulary.STRING.getIRI()));
    }
    if (value.getVersion() != null) {
      ontology = addAnnotationAxiomToClass(VERSION_IRI, value.getVersion(), valueClass,
        owlDataFactory.getOWLDatatype(XSDVocabulary.STRING.getIRI()));
    }
    if (value.getDbLabel() != null) {
      ontology = addAnnotationAxiomToClass(NOTATION_IRI, value.getDbLabel(), valueClass,
        owlDataFactory.getOWLDatatype(XSDVocabulary.STRING.getIRI()));
      //ontology = addAnnotationAxiomToClass(SKOSVocabulary.HIDDENLABEL.getIRI(), value.getDbLabel(), valueClass);
    }
    if (value.getDisplayLabel() != null) {
      ontology = addAnnotationAxiomToClass(LABEL_IRI, value.getDisplayLabel(), valueClass,
        owlDataFactory.getOWLDatatype(XSDVocabulary.STRING.getIRI()));
    }
    if (value.getDescription() != null) {
      ontology = addAnnotationAxiomToClass(COMMENT_IRI, value.getDescription(), valueClass,
        owlDataFactory.getOWLDatatype(XSDVocabulary.STRING.getIRI()));
    }
    if (value.getTermIri() != null) {
      ontology = addAnnotationAxiomToClass(RELATED_MATCH_IRI, value.getTermIri(), valueClass);
    }
    if (value.getBeginDate() != null) {
      ontology = addAnnotationAxiomToClass(START_TIME_IRI, value.getBeginDate(), valueClass,
        owlDataFactory.getOWLDatatype(XSDVocabulary.DATE.getIRI()));
    }
    if (value.getEndDate() != null) {
      ontology = addAnnotationAxiomToClass(END_TIME_IRI, value.getEndDate(), valueClass,
        owlDataFactory.getOWLDatatype(XSDVocabulary.DATE.getIRI()));
    }
    return ontology;
  }

  public static void saveOntology(File ontologyFile) {
    try {
      manager.saveOntology(ontology, IRI.create(ontologyFile.toURI()));
    } catch (OWLOntologyStorageException e) {
      logger.error("Error while saving the ontology: " + e);
    }
  }

  public static int getValueSetsCount() {
    return valueSetsCount;
  }

  public static int getValuesCount() {
    return valuesCount;
  }

  private static OWLOntology addAnnotationAxiomToClass(IRI propertyIRI, String annotationText, OWLClass c,
    OWLDatatype datatype) throws DuplicatedAxiomException {
    OWLAnnotationProperty p = owlDataFactory.getOWLAnnotationProperty(propertyIRI);
    OWLAnnotationValue v = owlDataFactory.getOWLLiteral(annotationText, datatype);
    OWLAnnotation valueMeaningAnnotation = owlDataFactory.getOWLAnnotation(p, v);
    OWLAnnotationAssertionAxiom ax = owlDataFactory.getOWLAnnotationAssertionAxiom(c.getIRI(), valueMeaningAnnotation);

    if (ontology.containsAxiom(ax)) {
      String message = "Duplicated annotation axiom: " + c.getIRI() + " - " + p.getIRI() + " - " + v.toString();
      throw new DuplicatedAxiomException(message);
    }
    manager.addAxiom(ontology, ax);
    return ontology;
  }

  private static OWLOntology addAnnotationAxiomToClass(IRI propertyIRI, String annotationIRI, OWLClass c)
    throws DuplicatedAxiomException {
    OWLAnnotationProperty p = owlDataFactory.getOWLAnnotationProperty(propertyIRI);
    OWLAnnotationValue v = IRI.create(annotationIRI);
    OWLAnnotation valueMeaningAnnotation = owlDataFactory.getOWLAnnotation(p, v);
    OWLAnnotationAssertionAxiom ax = owlDataFactory.getOWLAnnotationAssertionAxiom(c.getIRI(), valueMeaningAnnotation);

    if (ontology.containsAxiom(ax)) {
      String message = "Duplicated annotation axiom: " + c.getIRI() + " - " + p.getIRI() + " - " + v.toString();
      throw new DuplicatedAxiomException(message);
    }
    manager.addAxiom(ontology, ax);
    return ontology;
  }
}
