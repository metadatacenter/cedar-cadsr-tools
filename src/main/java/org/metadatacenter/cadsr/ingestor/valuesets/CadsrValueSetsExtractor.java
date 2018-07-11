package org.metadatacenter.cadsr.ingestor.valuesets;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static org.metadatacenter.cadsr.ingestor.Constants.CDE_VALUESETS_ONTOLOGY_NAME;

public class CadsrValueSetsExtractor {

  private static final Logger logger = LoggerFactory.getLogger(CadsrValueSetsExtractor.class);

  public static void main(String[] args) {

    String cdeSourceLocation = args[0];
    String outputDirectory = args[1];

    try {
      File sourceFile = new File(cdeSourceLocation);
      logger.info("Extracting value sets...");

      OWLOntologyManager ontologyManager = ValueSetsUtils.createOntologyManager();
      OWLOntology ontology = ValueSetsUtils.getValueSetsAsOntology
          (new FileInputStream(sourceFile), ontologyManager);

      File ontologyFile = new File(outputDirectory + "/" + CDE_VALUESETS_ONTOLOGY_NAME);

      ontologyManager.saveOntology(ontology, IRI.create(ontologyFile.toURI()));

    } catch (FileNotFoundException e) {
      logger.error(e.getMessage());
    } catch (OWLOntologyStorageException e) {
      logger.error("Error while saving ontology", e);
    }
  }
}
