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

  public static OWLOntologyManager manager;
  private static OWLOntology ontology;
  private static final Logger logger = LoggerFactory.getLogger(ValueSetsOntologyManager.class);

  static {
     manager = OWLManager.createOWLOntologyManager();
    // Create the ontology that will contain the value sets. The root classes will represent value sets. All
    // subclasses of a rootclass will represent the values of the value set.
    try {
      ontology = manager.createOntology(IRI.create(CDE_VALUESETS_ONTOLOGY_IRI));
    } catch (OWLOntologyCreationException e) {
      logger.error("Error while creating ontology: " + e);
    }
  }

  public static void addValueSetToOntology(DataElement dataElement, Set<Term> values) {
    // In order to get access to objects that represent entities we need a data factory
    OWLDataFactory factory = manager.getOWLDataFactory();

    // Create the class that will represent the value set
    OWLClass valueSetClass = dataElementToOWLClass(dataElement, factory);

    // Values of the value set
    for (Term value : values) {
      ontology = addPermissibleValuesToOntology(value, valueSetClass, ontology, factory, manager);
    }
  }

  public static OWLClass dataElementToOWLClass(DataElement dataElement, OWLDataFactory factory) {
    return factory.getOWLClass(IRI.create(ValueSetsUtil.generateValueSetIRI(dataElement)));
  }

  public static OWLOntology addPermissibleValuesToOntology(Term value, OWLClass valueSetClass,
                                                           OWLOntology ontology, OWLDataFactory factory,
                                                           OWLOntologyManager manager) {
    // Create the class that represents the value
    OWLClass valueClass = factory.getOWLClass(IRI.create(NCIT_ONTOLOGY_IRI + value.conceptId));
    // Create subclass axiom
    OWLAxiom axiom = factory.getOWLSubClassOfAxiom(valueClass, valueSetClass);
    // Add subclass axiom to the ontology
    manager.addAxiom(ontology, axiom);

    // Add annotation properties to the value
    ontology = addAnnotationAxiomToClass(OWLRDFVocabulary.RDFS_LABEL.getIRI(), value.uiLabel, valueClass,
        ontology, factory, manager);
    ontology = addAnnotationAxiomToClass(SKOSVocabulary.HIDDENLABEL.getIRI(), value.dbLabel, valueClass, ontology,
        factory, manager);
    ontology = addAnnotationAxiomToClass(OWLRDFVocabulary.RDFS_COMMENT.getIRI(), value.description, valueClass,
        ontology, factory, manager);

    return ontology;
  }

  public static OWLOntology addAnnotationAxiomToClass(IRI propertyIRI, String annotationText, OWLClass c,
                                                      OWLOntology ontology,
                                                      OWLDataFactory factory, OWLOntologyManager manager) {
    OWLAnnotationProperty p = factory.getOWLAnnotationProperty(propertyIRI);
    OWLAnnotationValue v = factory.getOWLLiteral(annotationText);
    OWLAnnotation valueMeaningAnnotation = factory.getOWLAnnotation(p, v);
    OWLAnnotationAssertionAxiom ax = factory.getOWLAnnotationAssertionAxiom(c.getIRI(), valueMeaningAnnotation);
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