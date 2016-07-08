package org.metadatacenter.ingestor.cadsr;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class CDEXMLInstances2CEDARCDEInstances
{
  public static void main(String[] argc) throws IOException, JAXBException, DatatypeConfigurationException
  {
    ObjectMapper mapper = new ObjectMapper();

    // Create JAXB XML unmarshaller
    JAXBContext jaxbContext = JAXBContext.newInstance(DataElementsList.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

    // Specify the XML file containing caDSR data elements
    File xmlFile = new File("src/main/resources/xml/xml_cde_201510293457_1_UTF8_short.xml");

    // Read a list of DataElement objects from an XML file containing a list of CDEs
    DataElementsList dataElementsList = ((DataElementsList)jaxbUnmarshaller.unmarshal(xmlFile));

    // Process each DataElement
    for (org.metadatacenter.ingestor.cadsr.DataElement cadsrDataElement : dataElementsList.dataElement) {

      System.out.println("Processing DataElement....");

      // Create a DataElement Java object (which we will serialize as a CEDAR template instance) for each DataElement
      org.metadatacenter.ingestor.cedar.DataElement cedarDataElement = new org.metadatacenter.ingestor.cedar.DataElement();

      // Transfer the content of each DataElement to a ElementData
      System.out.println("list XML CDE");

      System.out.println("**Data Element Details**");

      String cadsrPublicID = cadsrDataElement.getPUBLICID().getContent();
      System.out.println(cadsrPublicID);

      String cadsrLongName = cadsrDataElement.getLONGNAME().getContent();
      System.out.println(cadsrLongName);

      String cadsrPreferredName = cadsrDataElement.getPREFERREDNAME().getContent();
      System.out.println(cadsrPreferredName);

      String cadsrPreferredDefinition = cadsrDataElement.getPREFERREDDEFINITION().getContent();
      System.out.println(cadsrPreferredDefinition);

      String cadsrVersion = cadsrDataElement.getVERSION().getContent();
      System.out.println(cadsrVersion);

      System.out.println("**Data Element Concept**");
      DATAELEMENTCONCEPT cadsrDataElementDATAELEMENTCONCEPT = cadsrDataElement.getDATAELEMENTCONCEPT();
      System.out.println(cadsrDataElementDATAELEMENTCONCEPT.getPublicId().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPT.getPreferredName().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPT.getPreferredDefinition().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPT.getLongName().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPT.getVersion().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPT.getWorkflowStatus().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPT.getContextName().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPT.getContextVersion().getContent());

      System.out.println("**DEC Conceptual Domain**");
      ConceptualDomain cadsrDataElementDATAELEMENTCONCEPTDECConceptualDomain = cadsrDataElementDATAELEMENTCONCEPT.getConceptualDomain();
      System.out.println(cadsrDataElementDATAELEMENTCONCEPTDECConceptualDomain.getPublicId().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPTDECConceptualDomain.getContextName().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPTDECConceptualDomain.getContextVersion().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPTDECConceptualDomain.getPreferredName().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPTDECConceptualDomain.getVersion().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPTDECConceptualDomain.getLongName().getContent());

      System.out.println("**ObjectClass**");
      ObjectClass cadsrDataElementDATAELEMENTCONCEPTObjectClass = cadsrDataElementDATAELEMENTCONCEPT.getObjectClass();
      System.out.println(cadsrDataElementDATAELEMENTCONCEPTObjectClass.getPublicId().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPTObjectClass.getContextName().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPTObjectClass.getContextVersion().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPTObjectClass.getPreferredName().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPTObjectClass.getVersion().getContent());
      System.out.println("\t" + cadsrDataElementDATAELEMENTCONCEPTObjectClass.getLongName().getContent());

      List<ConceptDetailsITEM> cadsrDataElementDATAELEMENTCONCEPTObjectClassConceptDetailsITEM = cadsrDataElementDATAELEMENTCONCEPTObjectClass.getConceptDetails().getConceptDetailsITEM();
      if (!cadsrDataElementDATAELEMENTCONCEPTObjectClassConceptDetailsITEM.isEmpty()) {
        for (ConceptDetailsITEM val : cadsrDataElementDATAELEMENTCONCEPTObjectClassConceptDetailsITEM) {
          System.out.println("object class concept details list item: ");
          System.out.println(val.getPREFERREDNAME().getContent());
          System.out.println(val.getLONGNAME().getContent());
          System.out.println(val.getCONID().getContent());
          System.out.println(val.getDEFINITIONSOURCE().getContent());
          System.out.println(val.getDEFINITIONSOURCE().getContent());
          System.out.println(val.getORIGIN().getContent());
          System.out.println(val.getEVSSOURCE().getContent());
          System.out.println(val.getPRIMARYFLAGIND().getContent());
          System.out.println(val.getDISPLAYORDER().getContent());
        }
      }

      System.out.println("**Property**");
      ObjectClass cadsrDataElementDATAELEMENTCONCEPTProperty = cadsrDataElementDATAELEMENTCONCEPT.getObjectClass();
      System.out.println(cadsrDataElementDATAELEMENTCONCEPTProperty.getPublicId().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPTProperty.getContextName().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPTProperty.getContextVersion().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPTProperty.getPreferredName().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPTProperty.getVersion().getContent());
      System.out.println("\t" + cadsrDataElementDATAELEMENTCONCEPTProperty.getLongName().getContent());

      List<ConceptDetailsITEM> cadsrDataElementDATAELEMENTCONCEPTPropertyConceptDetailsITEM = cadsrDataElementDATAELEMENTCONCEPTProperty.getConceptDetails().getConceptDetailsITEM();
      if (!cadsrDataElementDATAELEMENTCONCEPTObjectClassConceptDetailsITEM.isEmpty()) {
        for (ConceptDetailsITEM val : cadsrDataElementDATAELEMENTCONCEPTObjectClassConceptDetailsITEM) {
          System.out.println("property concept details list item: ");
          System.out.println(val.getPREFERREDNAME().getContent());
          System.out.println(val.getLONGNAME().getContent());
          System.out.println(val.getCONID().getContent());
          System.out.println(val.getDEFINITIONSOURCE().getContent());
          System.out.println(val.getDEFINITIONSOURCE().getContent());
          System.out.println(val.getORIGIN().getContent());
          System.out.println(val.getEVSSOURCE().getContent());
          System.out.println(val.getPRIMARYFLAGIND().getContent());
          System.out.println(val.getDISPLAYORDER().getContent());
        }
      }

      String objectClassQualifier = cadsrDataElementDATAELEMENTCONCEPT.getObjectClassQualifier().getContent();
      if (objectClassQualifier.isEmpty()) {
        objectClassQualifier = cadsrDataElementDATAELEMENTCONCEPT.getObjectClassQualifier().getNULL();
      }
      System.out.println(objectClassQualifier);

      String PropertyQualifier = cadsrDataElementDATAELEMENTCONCEPT.getPropertyQualifier().getContent();
      String Origin = cadsrDataElementDATAELEMENTCONCEPT.getOrigin().getContent();

      System.out.println("**Value Domain**");

      VALUEDOMAIN cadsrDataElementVALUEDOMAIN = cadsrDataElement.getVALUEDOMAIN();
      System.out.println(cadsrDataElementVALUEDOMAIN.getPublicId().getContent());
      System.out.println(cadsrDataElementVALUEDOMAIN.getPreferredName().getContent());
      System.out.println(cadsrDataElementVALUEDOMAIN.getPreferredDefinition().getContent());
      System.out.println(cadsrDataElementVALUEDOMAIN.getLongName().getContent());
      System.out.println(cadsrDataElementVALUEDOMAIN.getVersion().getContent());
      System.out.println(cadsrDataElementVALUEDOMAIN.getWorkflowStatus().getContent());
      System.out.println(cadsrDataElementVALUEDOMAIN.getContextName().getContent());
      System.out.println(cadsrDataElementVALUEDOMAIN.getContextVersion().getContent());

      System.out.println("**VD Conceptual Domain**");
      ConceptualDomain cadsrDataElementDATAELEMENTCONCEPTVDConceptualDomain = cadsrDataElementVALUEDOMAIN.getConceptualDomain();
      System.out.println(cadsrDataElementDATAELEMENTCONCEPTVDConceptualDomain.getPublicId().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPTVDConceptualDomain.getContextName().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPTVDConceptualDomain.getContextVersion().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPTVDConceptualDomain.getPreferredName().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPTVDConceptualDomain.getVersion().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPTVDConceptualDomain.getLongName().getContent());

      System.out.println("**Value Domain cont**");
      System.out.println(cadsrDataElementVALUEDOMAIN.getDatatype().getContent());
      System.out.println(cadsrDataElementVALUEDOMAIN.getValueDomainType().getContent());

      String unitOfMeasure = cadsrDataElementVALUEDOMAIN.getUnitOfMeasure().getContent();
      if (unitOfMeasure.isEmpty()) {
        unitOfMeasure = cadsrDataElementVALUEDOMAIN.getUnitOfMeasure().getNULL();
      }
      System.out.println(unitOfMeasure);

      String displayFormat = cadsrDataElementVALUEDOMAIN.getDisplayFormat().getContent();
      if (displayFormat.isEmpty()) {
        displayFormat = cadsrDataElementVALUEDOMAIN.getDisplayFormat().getNULL();
      }
      System.out.println(displayFormat);

      System.out.println(cadsrDataElementVALUEDOMAIN.getMaximumLength().getContent());
      System.out.println(cadsrDataElementVALUEDOMAIN.getMinimumLength().getContent());

      String decimalPlace = cadsrDataElementVALUEDOMAIN.getDecimalPlace().getContent();
      if (decimalPlace.isEmpty()) {
        decimalPlace = cadsrDataElementVALUEDOMAIN.getDecimalPlace().getNULL();
      }
      System.out.println(decimalPlace);

      String characterSetName = cadsrDataElementVALUEDOMAIN.getCharacterSetName().getContent();
      if (characterSetName.isEmpty()) {
        characterSetName = cadsrDataElementVALUEDOMAIN.getCharacterSetName().getNULL();
      }
      System.out.println(characterSetName);

      String maximumValue = cadsrDataElementVALUEDOMAIN.getMaximumValue().getContent();
      if (maximumValue.isEmpty()) {
        maximumValue = cadsrDataElementVALUEDOMAIN.getMaximumValue().getNULL();
      }
      System.out.println(maximumValue);

      String minimumValue = cadsrDataElementVALUEDOMAIN.getMinimumValue().getContent();
      if (minimumValue.isEmpty()) {
        minimumValue = cadsrDataElementVALUEDOMAIN.getMinimumValue().getNULL();
      }
      System.out.println(minimumValue);

      String origin = cadsrDataElementVALUEDOMAIN.getOrigin().getContent();
      if (origin.isEmpty()) {
        origin = cadsrDataElementVALUEDOMAIN.getOrigin().getNULL();
      }
      System.out.println(origin);

      Representation cadsrDataElementVALUEDOMAINRepresentation = cadsrDataElementVALUEDOMAIN.getRepresentation();
      System.out.println(cadsrDataElementVALUEDOMAINRepresentation.getPublicId().getContent());
      System.out.println(cadsrDataElementVALUEDOMAINRepresentation.getContextName().getContent());
      System.out.println(cadsrDataElementVALUEDOMAINRepresentation.getContextVersion().getContent());
      System.out.println(cadsrDataElementVALUEDOMAINRepresentation.getPreferredName().getContent());
      System.out.println(cadsrDataElementVALUEDOMAINRepresentation.getVersion().getContent());
      System.out.println(cadsrDataElementVALUEDOMAINRepresentation.getLongName().getContent());

      /* TODO: continue printing out the Data element, left off at representation concept details list
       * use other concept domain again
        * does every value have to be checked for null?
        * next, will wire all these values to xml classes*/

      ALTERNATENAMELIST cadsrDataElementALTERNATENAMELIST = cadsrDataElement.getALTERNATENAMELIST();
      List<ALTERNATENAMELISTITEM> cadsrDataElementALTERNATENAMELISTITEM = cadsrDataElementALTERNATENAMELIST.getALTERNATENAMELISTITEM();
      if (!cadsrDataElementALTERNATENAMELISTITEM.isEmpty()) {
        for (ALTERNATENAMELISTITEM val : cadsrDataElementALTERNATENAMELISTITEM) {
          System.out.println("alternate list item: ");
          System.out.println(val.getContextName().getContent());
          System.out.println(val.getContextVersion().getContent());
          System.out.println(val.getAlternateName().getContent());
          System.out.println(val.getAlternateNameType().getContent());
          System.out.println(val.getLanguage().getContent());
        }
      }
      //System.out.println(cadsrDataElementALTERNATENAMELIST);
      //System.out.println(cadsrDataElementALTERNATENAMELISTITEM);


      // Specify a temporary file to store a DataElement template instance
      File cdeFile = File.createTempFile("DataElement", ".json");

      System.out.println("Writing DataElement to " + cdeFile.getAbsolutePath());

      // Serialize the CDE instance
      mapper.writeValue(cdeFile, cedarDataElement);
    }
  }
}
