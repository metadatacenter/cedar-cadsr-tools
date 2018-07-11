package org.metadatacenter.cadsr.ingestor.valuesets;

import com.google.common.collect.Lists;
import org.metadatacenter.cadsr.DataElement;
import org.metadatacenter.cadsr.DataElementsList;
import org.metadatacenter.cadsr.PermissibleValues;
import org.metadatacenter.cadsr.PermissibleValuesITEM;
import org.metadatacenter.cadsr.ingestor.CadsrUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.metadatacenter.cadsr.ingestor.Constants.CDE_STATUS_RELEASED;
import static org.metadatacenter.cadsr.ingestor.Constants.CDE_VALUESETS_ONTOLOGY_IRI;
import static org.metadatacenter.cadsr.ingestor.Constants.MIN_BP_VALUESET_SIZE;

public class ValueSetsUtils {

  private static final Logger logger = LoggerFactory.getLogger(ValueSetsUtils.class);

  static {

  }

  public static OWLOntology getValueSetsAsOntology(InputStream is, OWLOntologyManager manager) {

    OWLOntology ontology = null;
    try {
      // Create the ontology that will contain the value sets
      ontology = manager.createOntology(IRI.create(CDE_VALUESETS_ONTOLOGY_IRI));
      DataElementsList del = CadsrUtils.getDataElementLists(is);
      for (DataElement dataElement : del.getDataElement()) {

        PermissibleValues values = dataElement.getVALUEDOMAIN().getPermissibleValues();
        int numValues = values.getPermissibleValuesITEM().size();
        if (numValues > MIN_BP_VALUESET_SIZE && dataElement.getWORKFLOWSTATUS().getContent().equals
            (CDE_STATUS_RELEASED)) {

          // In order to get access to objects that represent entities we need a data factory
          OWLDataFactory factory = manager.getOWLDataFactory();

          // Create the class that will represent the value set
          OWLClass valueSetClass = dataElementToOWLClass(dataElement, factory);

          // Values of the value set
          for (PermissibleValuesITEM item : values.getPermissibleValuesITEM()) {
            // Create a class that will represent a value of the value set
            OWLClass valueClass = permissibleValuesItemToOWLClass(item, factory);
            // Create subclass axiom
            OWLAxiom axiom = factory.getOWLSubClassOfAxiom(valueClass, valueSetClass);
            // Add axiom to the ontology
            manager.addAxiom(ontology, axiom);
          }
        }
      }

    } catch (JAXBException e) {
      logger.error("Error while parsing source document: " + e);
    } catch (ClassCastException e) {
      logger.error("Source document is not a list of data elements: " + e);
    } catch (UnsupportedEncodingException e) {
      logger.error("Unsupported encoding: " + e);
    } catch (OWLOntologyCreationException e) {
      logger.error("Error while creating ontology: " + e);
    }
    return ontology;
  }

  public static OWLClass dataElementToOWLClass(DataElement dataElement, OWLDataFactory factory) {

    String id = dataElement.getPUBLICID().getContent();
    String prefName = dataElement.getPREFERREDNAME().getContent();
    String longName = dataElement.getLONGNAME().getContent();
    String prefDefinition = dataElement.getPREFERREDDEFINITION().getContent();
    String publicId = dataElement.getPUBLICID().getContent();

    // Create a class
    OWLClass c = factory.getOWLClass(IRI.create(CDE_VALUESETS_ONTOLOGY_IRI, "#" + prefName));

    return c;
  }

  public static OWLClass permissibleValuesItemToOWLClass(PermissibleValuesITEM item, OWLDataFactory factory) {

    String prefName = item.getVALIDVALUE().getContent();
    OWLClass c = factory.getOWLClass(IRI.create(CDE_VALUESETS_ONTOLOGY_IRI, "#" + prefName));

    return c;

  }


  /**
   * Create OWLOntologyManager.
   *
   * @return An OWLOntologyManager.
   */
  public static OWLOntologyManager createOntologyManager() {
    OWLOntologyManager m = OWLManager.createOWLOntologyManager();
    return m;
  }


}
