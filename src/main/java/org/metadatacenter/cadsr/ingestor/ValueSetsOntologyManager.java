package org.metadatacenter.cadsr.ingestor;

import org.metadatacenter.cadsr.DataElement;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.semanticweb.owlapi.vocab.SKOSVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Set;

import static org.metadatacenter.cadsr.ingestor.Constants.*;

public class ValueSetsOntologyManager {

  private static OWLOntologyManager manager;
  private static OWLOntology ontology;
  private static OWLDataFactory owlDataFactory;
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

  public static void addValueSetToOntology(DataElement dataElement, Set<Term> values) {

    String valueDomainId = dataElement.getVALUEDOMAIN().getPublicId().getContent();
    String valueDomainVersion = dataElement.getVALUEDOMAIN().getVersion().getContent();

    // Create the class that will represent the value set
    OWLClass valueSetClass = dataElementToOWLClass(valueDomainId, valueDomainVersion);

    // Check if the ontology already contains that class
    if (!ontology.containsClassInSignature(valueSetClass.getIRI())) {

      String vsDescription = "Value set for ValueDomain ID=" + valueDomainId + " (version " + valueDomainVersion + ")";

      // Add annotation properties to the value set
      String valueSetId = ValueSetsUtil.generateValueSetId(valueDomainId, valueDomainVersion);
      ontology = addAnnotationAxiomToClass(OWLRDFVocabulary.RDFS_LABEL.getIRI(), valueSetId, valueSetClass);
      ontology = addAnnotationAxiomToClass(OWLRDFVocabulary.RDFS_COMMENT.getIRI(), vsDescription, valueSetClass);

      // Values of the value set
      for (Term value : values) {
        ontology = addPermissibleValuesToOntology(valueSetId, value, valueSetClass);
      }
    } else {
      logger.warn("The value set has not been added to the ontology because it's already there. Class IRI: " + valueSetClass.getIRI());
    }
  }

  public static OWLClass dataElementToOWLClass(String valueSetId, String valueSetVersion) {
    return owlDataFactory.getOWLClass(IRI.create(ValueSetsUtil.generateValueSetIRI(valueSetId, valueSetVersion)));
  }

  public static OWLOntology addPermissibleValuesToOntology(String valueSetId, Term value, OWLClass valueSetClass) {
    // Create the class that represents the value
    OWLClass valueClass = owlDataFactory.getOWLClass(ValueSetsUtil.generateValueIRI(valueSetId, value.termId, value.termVersion));
    // Create subclass axiom
    OWLAxiom axiom = owlDataFactory.getOWLSubClassOfAxiom(valueClass, valueSetClass);
    // Add subclass axiom to the ontology
    manager.addAxiom(ontology, axiom);
    // Add annotation properties to the value
    ontology = addAnnotationAxiomToClass(OWLRDFVocabulary.RDFS_LABEL.getIRI(), value.displayLabel, valueClass);
    ontology = addAnnotationAxiomToClass(SKOSVocabulary.HIDDENLABEL.getIRI(), value.dbLabel, valueClass);
    ontology = addAnnotationAxiomToClass(SKOSVocabulary.RELATEDMATCH.getIRI(), NCIT_ONTOLOGY_IRI + value.relatedTermId, valueClass);
    ontology = addAnnotationAxiomToClass(OWLRDFVocabulary.RDFS_COMMENT.getIRI(), value.description, valueClass);
    return ontology;
  }

  public static OWLOntology addAnnotationAxiomToClass(IRI propertyIRI, String annotationText, OWLClass c) {
    OWLAnnotationProperty p = owlDataFactory.getOWLAnnotationProperty(propertyIRI);
    OWLAnnotationValue v = owlDataFactory.getOWLLiteral(annotationText);
    OWLAnnotation valueMeaningAnnotation = owlDataFactory.getOWLAnnotation(p, v);
    OWLAnnotationAssertionAxiom ax = owlDataFactory.getOWLAnnotationAssertionAxiom(c.getIRI(), valueMeaningAnnotation);
    manager.addAxiom(ontology, ax);
    return ontology;
  }

  public static void saveOntology(File ontologyFile) {
    try {
      manager.saveOntology(ontology, IRI.create(ontologyFile.toURI()));
    } catch (OWLOntologyStorageException e) {
      logger.error("Error while saving the ontology: " + e);
    }
  }

}
