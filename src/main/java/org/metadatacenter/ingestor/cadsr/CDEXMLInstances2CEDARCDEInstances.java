package org.metadatacenter.ingestor.cadsr;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.ObjectUtils;
import org.metadatacenter.ingestor.cedar.*;
import org.metadatacenter.ingestor.cedar.ConceptDetails;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

      //data element details
      System.out.println("**Data Element Details**");

      //wire data element from xml to json

      //DE public ID
      String cadsrPublicID = cadsrDataElement.getPUBLICID().getContent();
      System.out.println(cadsrPublicID);
      PublicID cedarPublicID = new org.metadatacenter.ingestor.cedar.PublicID();
      cedarPublicID.setValue(cadsrPublicID);
      cedarDataElement.setPublicID(cedarPublicID);

      //DE Long Name
      String cadsrLongName = cadsrDataElement.getLONGNAME().getContent();
      System.out.println(cadsrLongName);
      org.metadatacenter.ingestor.cedar.LongName cedarLongName = new org.metadatacenter.ingestor.cedar.LongName();
      cedarLongName.setValue(cadsrLongName);
      cedarDataElement.setLongName(cedarLongName);

      //DE Preferred Name
      String cadsrPreferredName = cadsrDataElement.getPREFERREDNAME().getContent();
      System.out.println(cadsrPreferredName);
      org.metadatacenter.ingestor.cedar.PreferredName cedarPreferredName = new org.metadatacenter.ingestor.cedar.PreferredName();
      cedarPreferredName.setValue(cadsrPreferredName);
      cedarDataElement.setPreferredName(cedarPreferredName);

      //DE Preferred Definition
      String cadsrPreferredDefinition = cadsrDataElement.getPREFERREDDEFINITION().getContent();
      System.out.println(cadsrPreferredDefinition);
      org.metadatacenter.ingestor.cedar.PreferredDefinition cedarPreferredDefinition = new org.metadatacenter.ingestor.cedar.PreferredDefinition();
      cedarPreferredDefinition.setValue(cadsrPreferredDefinition);
      cedarDataElement.setPreferredDefinition(cedarPreferredDefinition);

      //DE Version
      String cadsrVersion = cadsrDataElement.getVERSION().getContent();
      System.out.println(cadsrVersion);
      org.metadatacenter.ingestor.cedar.Version cedarVersion = new org.metadatacenter.ingestor.cedar.Version();
      cedarVersion.setValue(cadsrVersion);
      cedarDataElement.setVersion(cedarVersion);

      //DE Workflow Status
      String cadsrWorkflowStatus = cadsrDataElement.getWORKFLOWSTATUS().getContent();
      System.out.println(cadsrWorkflowStatus);
      org.metadatacenter.ingestor.cedar.WorkflowStatus cedarWorkflowStatus = new org.metadatacenter.ingestor.cedar.WorkflowStatus();
      cedarWorkflowStatus.setValue(cadsrWorkflowStatus);
      cedarDataElement.setWorkflowStatus(cedarWorkflowStatus);

      //DE Context Name
      String cadsrContextName = cadsrDataElement.getCONTEXTNAME().getContent();
      System.out.println(cadsrContextName);
      org.metadatacenter.ingestor.cedar.ContextName cedarContextName = new org.metadatacenter.ingestor.cedar.ContextName();
      cedarContextName.setValue(cadsrContextName);
      cedarDataElement.setContextName(cedarContextName);

      //DE Context Version
      String cadsrContextVersion = cadsrDataElement.getCONTEXTVERSION().getContent();
      System.out.println(cadsrContextVersion);
      org.metadatacenter.ingestor.cedar.ContextVersion cedarContextVersion = new org.metadatacenter.ingestor.cedar.ContextVersion();
      cedarContextVersion.setValue(cadsrContextVersion);
      cedarDataElement.setContextVersion(cedarContextVersion);

      //DE Origin
      String cadsrOrigin = cadsrDataElement.getORIGIN().getContent();
      System.out.println(cadsrOrigin);
      org.metadatacenter.ingestor.cedar.Origin cedarOrigin = new org.metadatacenter.ingestor.cedar.Origin();
      cedarOrigin.setValue(cadsrOrigin);
      cedarDataElement.setOrigin(cedarOrigin);

      //DE Registration Status
      String cadsrRegistrationStatus = cadsrDataElement.getREGISTRATIONSTATUS().getContent();
      System.out.println(cadsrRegistrationStatus);
      org.metadatacenter.ingestor.cedar.RegistrationStatus cedarRegistrationStatus = new org.metadatacenter.ingestor.cedar.RegistrationStatus();
      cedarRegistrationStatus.setValue(cadsrRegistrationStatus);
      cedarDataElement.setRegistrationStatus(cedarRegistrationStatus);

      /* ******************************************************* */
      /* ******************************************************* */


      // build Data element concept from xml to json
      System.out.println("**Data Element Concept**");
      DATAELEMENTCONCEPT cadsrDataElementDATAELEMENTCONCEPT = cadsrDataElement.getDATAELEMENTCONCEPT();
      DataElementConcept cedarDataElementConcept = new DataElementConcept();

      //DEC Public ID
      String cadsrDECPublicID = cadsrDataElementDATAELEMENTCONCEPT.getPublicId().getContent();
      System.out.println(cadsrDECPublicID);
      PublicID_ cedarDECPublicID = new PublicID_();
      cedarDECPublicID.setValue(cadsrDECPublicID);
      cedarDataElementConcept.setPublicID(cedarDECPublicID);

      //DEC Preferred Name
      String cadsrDECPreferredName = cadsrDataElementDATAELEMENTCONCEPT.getPreferredName().getContent();
      System.out.println(cadsrDECPreferredName);
      org.metadatacenter.ingestor.cedar.PreferredName_ cedarDECPreferredName = new PreferredName_();
      cedarDECPreferredName.setValue(cadsrDECPreferredName);
      cedarDataElementConcept.setPreferredName(cedarDECPreferredName);


      //DEC Preferred Definition
      String cadsrDECPreferredDefinition = cadsrDataElementDATAELEMENTCONCEPT.getPreferredDefinition().getContent();
      System.out.println(cadsrDECPreferredDefinition);
      org.metadatacenter.ingestor.cedar.PreferredDefinition_ cedarDECPreferredDefinition = new PreferredDefinition_();
      cedarDECPreferredDefinition.setValue(cadsrPreferredDefinition);
      cedarDataElementConcept.setPreferredDefinition(cedarDECPreferredDefinition);

      //DEC Long Name
      String cadsrDECLongName = cadsrDataElementDATAELEMENTCONCEPT.getLongName().getContent();
      System.out.println(cadsrDECLongName);
      org.metadatacenter.ingestor.cedar.LongName_ cedarDECLongName = new LongName_();
      cedarDECLongName.setValue(cadsrDECLongName);
      cedarDataElementConcept.setLongName(cedarDECLongName);

      //DEC Version
      String cadsrDECVersion = cadsrDataElementDATAELEMENTCONCEPT.getVersion().getContent();
      System.out.println(cadsrDECVersion);
      org.metadatacenter.ingestor.cedar.Version_ cedarDECVersion = new Version_();
      cedarDECVersion.setValue(cadsrDECVersion);
      cedarDataElementConcept.setVersion(cedarDECVersion);

      //DEC Workflow Status
      String cadsrDECWorkflowStatus = cadsrDataElementDATAELEMENTCONCEPT.getWorkflowStatus().getContent();
      System.out.println(cadsrDECWorkflowStatus);
      org.metadatacenter.ingestor.cedar.WorkflowStatus_ cedarDECWorkflowStatus = new WorkflowStatus_();
      cedarDECWorkflowStatus.setValue(cadsrDECWorkflowStatus);
      cedarDataElementConcept.setWorkflowStatus(cedarDECWorkflowStatus);

      //DEC Context Name
      String cadsrDECContextName = cadsrDataElementDATAELEMENTCONCEPT.getContextName().getContent();
      System.out.println(cadsrDECContextName);
      org.metadatacenter.ingestor.cedar.ContextName_ cedarDECContextName = new ContextName_();
      cedarDECContextName.setValue(cadsrContextName);
      cedarDataElementConcept.setContextName(cedarDECContextName);

      //DEC Context Version
      String cadsrDECContextVersion = cadsrDataElementDATAELEMENTCONCEPT.getContextVersion().getContent();
      System.out.println(cadsrDECContextVersion);
      org.metadatacenter.ingestor.cedar.ContextVersion_ cedarDECContextVersion = new ContextVersion_();
      cedarContextVersion.setValue(cadsrContextVersion);
      cedarDataElementConcept.setContextVersion(cedarDECContextVersion);

      /* ******************************************************* */

      //DEC conceptual domain
      System.out.println("**DEC Conceptual Domain**");
      ConceptualDomain cadsrDataElementDATAELEMENTCONCEPTDECConceptualDomain = cadsrDataElementDATAELEMENTCONCEPT.getConceptualDomain();
      org.metadatacenter.ingestor.cedar.ConceptualDomain cedarDECConceptualDomain = new org.metadatacenter.ingestor.cedar.ConceptualDomain();

      //DEC CD public id
      String cadsrDECcdPublicID = cadsrDataElementDATAELEMENTCONCEPTDECConceptualDomain.getPublicId().getContent();
      System.out.println(cadsrDECcdPublicID);

      //DEC CD context name
      String cadsrDECcdContextName = cadsrDataElementDATAELEMENTCONCEPTDECConceptualDomain.getContextName().getContent();
      System.out.println(cadsrDECcdContextName);

      //DEC CD context version
      String cadsrDECcdContextVersion = cadsrDataElementDATAELEMENTCONCEPTDECConceptualDomain.getContextVersion().getContent();
      System.out.println(cadsrDECcdContextVersion);

      //DEC CD preferred name
      String cadsrDECcdPreferredName = cadsrDataElementDATAELEMENTCONCEPTDECConceptualDomain.getPreferredName().getContent();
      System.out.println(cadsrDECcdPreferredName);

      //DEC CD version
      String cadsrDECcdVersion = cadsrDataElementDATAELEMENTCONCEPTDECConceptualDomain.getVersion().getContent();
      System.out.println(cadsrDECcdVersion);

      //DEC CD long name
      String cadsrDECcdLongName = cadsrDataElementDATAELEMENTCONCEPTDECConceptualDomain.getLongName().getContent();
      System.out.println(cadsrDECcdLongName);


      //wire DEC conceptual domain to DEC
      cedarDataElementConcept.setConceptualDomain(cedarDECConceptualDomain);

      /* ******************************************************* */

      //object class
      System.out.println("**ObjectClass**");
      ObjectClass cadsrDataElementDATAELEMENTCONCEPTObjectClass = cadsrDataElementDATAELEMENTCONCEPT.getObjectClass();
      org.metadatacenter.ingestor.cedar.ObjectClass objectClass = new org.metadatacenter.ingestor.cedar.ObjectClass();

      //object class public id
      String cadsrObjClassPublicID = cadsrDataElementDATAELEMENTCONCEPTObjectClass.getPublicId().getContent();
      System.out.println(cadsrObjClassPublicID);

      //object class context name
      String cadsrObjClassContextName = cadsrDataElementDATAELEMENTCONCEPTObjectClass.getContextName().getContent();
      System.out.println(cadsrObjClassContextName);

      //object class context version
      String cadsrObjClassContextVersion = cadsrDataElementDATAELEMENTCONCEPTObjectClass.getContextVersion().getContent();
      System.out.println(cadsrObjClassContextVersion);

      //object class preferred name
      String cadsrObjClassPreferredName = cadsrDataElementDATAELEMENTCONCEPTObjectClass.getPreferredName().getContent();
      System.out.println(cadsrObjClassPreferredName);

      //object class version
      String cadsrObjClassVersion = cadsrDataElementDATAELEMENTCONCEPTObjectClass.getVersion().getContent();
      System.out.println(cadsrObjClassVersion);

      //object class long name
      String cadsrObjClassLongName = cadsrDataElementDATAELEMENTCONCEPTObjectClass.getLongName().getContent();
      System.out.println(cadsrObjClassLongName);

      /* ******************************************************* */

      //object class concept details list
      List<ConceptDetailsITEM> cadsrDataElementDATAELEMENTCONCEPTObjectClassConceptDetailsITEM = cadsrDataElementDATAELEMENTCONCEPTObjectClass.getConceptDetails().getConceptDetailsITEM();
      org.metadatacenter.ingestor.cedar.ConceptDetails cedarObjClassConceptDetails = new ConceptDetails();
      List<ConceptDetailsItem> cedarConceptDetailsList = new ArrayList<ConceptDetailsItem>();

      if (!cadsrDataElementDATAELEMENTCONCEPTObjectClassConceptDetailsITEM.isEmpty()) {
        for (ConceptDetailsITEM val : cadsrDataElementDATAELEMENTCONCEPTObjectClassConceptDetailsITEM) {
          org.metadatacenter.ingestor.cedar.ConceptDetailsItem cedarObjClassConceptDetailsItem = new ConceptDetailsItem();

          System.out.println("object class concept details list item: ");

          String cadsrObjClassConceptDetailsItemPreferredName = val.getPREFERREDNAME().getContent();
          System.out.println(cadsrObjClassConceptDetailsItemPreferredName);
          org.metadatacenter.ingestor.cedar.PreferredName____ cedarObjClassConceptDetailsItemPreferredName = new PreferredName____();
          cedarObjClassConceptDetailsItemPreferredName.setValue(cadsrObjClassConceptDetailsItemPreferredName);
          cedarObjClassConceptDetailsItem.setPreferredName(cedarObjClassConceptDetailsItemPreferredName);


          cedarConceptDetailsList.add(cedarObjClassConceptDetailsItem);

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

      cedarObjClassConceptDetails.setConceptDetailsItem(cedarConceptDetailsList);

      objectClass.setConceptDetails(cedarObjClassConceptDetails);

      //TODO wire the rest of object class concept details back together, figure out empty case (null?)

      cedarDataElementConcept.setObjectClass(objectClass);

      /* ******************************************************* */

      //property
      System.out.println("**Property**");
      Property cadsrDataElementDATAELEMENTCONCEPTProperty = cadsrDataElementDATAELEMENTCONCEPT.getProperty();
      System.out.println(cadsrDataElementDATAELEMENTCONCEPTProperty.getPublicId().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPTProperty.getContextName().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPTProperty.getContextVersion().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPTProperty.getPreferredName().getContent());
      System.out.println(cadsrDataElementDATAELEMENTCONCEPTProperty.getVersion().getContent());
      System.out.println("\t" + cadsrDataElementDATAELEMENTCONCEPTProperty.getLongName().getContent());

      //property concept details list
      List<ConceptDetailsITEM> cadsrDataElementDATAELEMENTCONCEPTPropertyConceptDetailsITEM = cadsrDataElementDATAELEMENTCONCEPTProperty.getConceptDetails().getConceptDetailsITEM();
      if (!cadsrDataElementDATAELEMENTCONCEPTPropertyConceptDetailsITEM.isEmpty()) {
        for (ConceptDetailsITEM val : cadsrDataElementDATAELEMENTCONCEPTPropertyConceptDetailsITEM) {
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

      //object class qualifier
      String objectClassQualifier = cadsrDataElementDATAELEMENTCONCEPT.getObjectClassQualifier().getContent();
      if (objectClassQualifier.isEmpty()) {
        objectClassQualifier = cadsrDataElementDATAELEMENTCONCEPT.getObjectClassQualifier().getNULL();
        if (objectClassQualifier.equals("TRUE")) {
          objectClassQualifier = "NULL";
        }
      }
      System.out.println(objectClassQualifier);

      //property qualifier
      String PropertyQualifier = cadsrDataElementDATAELEMENTCONCEPT.getPropertyQualifier().getContent();
      if (PropertyQualifier.isEmpty()) {
        PropertyQualifier = cadsrDataElementDATAELEMENTCONCEPT.getPropertyQualifier().getNULL();
        if (PropertyQualifier.equals("TRUE")) {
          PropertyQualifier = "NULL";
        }
      }
      System.out.println(PropertyQualifier);

      //origin
      String Origin = cadsrDataElementDATAELEMENTCONCEPT.getOrigin().getContent();
      if (Origin.isEmpty()) {
        Origin = cadsrDataElementDATAELEMENTCONCEPT.getOrigin().getNULL();
        if (Origin.equals("TRUE")) {
          Origin = "NULL";
        }
      }
      System.out.println(Origin);


      // wire cedar DEC to data element
      //TODO check DEC is complete
      cedarDataElement.setDataElementConcept(cedarDataElementConcept);


      /* ******************************************************* */
      /* ******************************************************* */


      //value domain
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

      //value domain concept details list
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

      //unit of measure
      String unitOfMeasure = cadsrDataElementVALUEDOMAIN.getUnitOfMeasure().getContent();
      if (unitOfMeasure.isEmpty()) {
        unitOfMeasure = cadsrDataElementVALUEDOMAIN.getUnitOfMeasure().getNULL();
        if (unitOfMeasure.equals("TRUE")) {
          unitOfMeasure = "NULL";
        }
      }
      System.out.println(unitOfMeasure);

      //display format
      String displayFormat = cadsrDataElementVALUEDOMAIN.getDisplayFormat().getContent();
      if (displayFormat.isEmpty()) {
        displayFormat = cadsrDataElementVALUEDOMAIN.getDisplayFormat().getNULL();
        if (displayFormat.equals("TRUE")) {
          displayFormat = "NULL";
        }
      }
      System.out.println(displayFormat);

      //max and min length
      System.out.println(cadsrDataElementVALUEDOMAIN.getMaximumLength().getContent());
      System.out.println(cadsrDataElementVALUEDOMAIN.getMinimumLength().getContent());

      //decimal place
      String decimalPlace = cadsrDataElementVALUEDOMAIN.getDecimalPlace().getContent();
      if (decimalPlace.isEmpty()) {
        decimalPlace = cadsrDataElementVALUEDOMAIN.getDecimalPlace().getNULL();
        if (decimalPlace.equals("TRUE")) {
          decimalPlace = "NULL";
        }
      }
      System.out.println(decimalPlace);

      //character set name
      String characterSetName = cadsrDataElementVALUEDOMAIN.getCharacterSetName().getContent();
      if (characterSetName.isEmpty()) {
        characterSetName = cadsrDataElementVALUEDOMAIN.getCharacterSetName().getNULL();
        if (characterSetName.equals("TRUE")) {
          characterSetName = "NULL";
        }
      }
      System.out.println(characterSetName);

      //max value
      String maximumValue = cadsrDataElementVALUEDOMAIN.getMaximumValue().getContent();
      if (maximumValue.isEmpty()) {
        maximumValue = cadsrDataElementVALUEDOMAIN.getMaximumValue().getNULL();
        if (maximumValue.equals("TRUE")) {
          maximumValue = "NULL";
        }
      }
      System.out.println(maximumValue);

      //min value
      String minimumValue = cadsrDataElementVALUEDOMAIN.getMinimumValue().getContent();
      if (minimumValue.isEmpty()) {
        minimumValue = cadsrDataElementVALUEDOMAIN.getMinimumValue().getNULL();
        if (minimumValue.equals("TRUE")) {
          minimumValue = "NULL";
        }
      }
      System.out.println(minimumValue);

      //origin
      String origin = cadsrDataElementVALUEDOMAIN.getOrigin().getContent();
      if (origin.isEmpty()) {
        origin = cadsrDataElementVALUEDOMAIN.getOrigin().getNULL();
        if (origin.equals("TRUE")) {
          origin = "NULL";
        }
      }
      System.out.println(origin);

      //representation
      System.out.println("**Representation**");
      Representation cadsrDataElementVALUEDOMAINRepresentation = cadsrDataElementVALUEDOMAIN.getRepresentation();
      System.out.println(cadsrDataElementVALUEDOMAINRepresentation.getPublicId().getContent());
      System.out.println(cadsrDataElementVALUEDOMAINRepresentation.getContextName().getContent());
      System.out.println(cadsrDataElementVALUEDOMAINRepresentation.getContextVersion().getContent());
      System.out.println(cadsrDataElementVALUEDOMAINRepresentation.getPreferredName().getContent());
      System.out.println(cadsrDataElementVALUEDOMAINRepresentation.getVersion().getContent());
      System.out.println(cadsrDataElementVALUEDOMAINRepresentation.getLongName().getContent());

      /* TODO:
       * does every value have to be checked for null?
       * next, wire all these values to xml classes*/

      //representation concept details list
      List<ConceptDetailsITEM> cadsrDataElementVALUEDOMAINRepresentationConceptDetailsITEM = cadsrDataElementVALUEDOMAINRepresentation.getConceptDetails().getConceptDetailsITEM();
      if (!cadsrDataElementVALUEDOMAINRepresentationConceptDetailsITEM.isEmpty()) {
        for (ConceptDetailsITEM val : cadsrDataElementVALUEDOMAINRepresentationConceptDetailsITEM) {
          System.out.println("representation concept details list item: ");
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

      //permissible values
      System.out.println("**Permissible Values**");
      List<PermissibleValuesITEM> permissibleValuesITEMList = cadsrDataElementVALUEDOMAIN.getPermissibleValues().getPermissibleValuesITEM();
      if (!permissibleValuesITEMList.isEmpty()) {
        for (PermissibleValuesITEM val : permissibleValuesITEMList) {
          System.out.println("permissible values list item: ");
          System.out.println(val.getVALIDVALUE().getContent());
          System.out.println(val.getVALUEMEANING().getContent());
          System.out.println(val.getMEANINGDESCRIPTION().getContent());
          System.out.println(val.getMEANINGCONCEPTS().getContent());
          System.out.println(val.getPVBEGINDATE().getContent());

          String pvEndDate = val.getPVENDDATE().getContent();
          if (pvEndDate.isEmpty()) {
            pvEndDate = val.getPVENDDATE().getNULL();
            if (pvEndDate.equals("TRUE")) {
              pvEndDate = "NULL";
            }
          }
          System.out.println(pvEndDate);

          System.out.println(val.getVMPUBLICID().getContent());
          System.out.println(val.getVMVERSION().getContent());
        }
      }

      // value domain concepts
      System.out.println("**Value Domain Concepts**");
      List<ValueDomainConceptsITEM> valueDomainConceptsITEMList = cadsrDataElementVALUEDOMAIN.getValueDomainConcepts().getValueDomainConceptsITEM();
      if (!valueDomainConceptsITEMList.isEmpty()) {
        for (ValueDomainConceptsITEM val : valueDomainConceptsITEMList) {
          System.out.println("value domain concepts list item: ");
          System.out.println(val.getPREFERREDNAME().getContent());
          System.out.println(val.getLONGNAME().getContent());
          System.out.println(val.getCONID().getContent());
          System.out.println(val.getDEFINITIONSOURCE().getContent());
          System.out.println(val.getORIGIN().getContent());
          System.out.println(val.getEVSSOURCE().getContent());
          System.out.println(val.getPRIMARYFLAGIND().getContent());
          System.out.println(val.getDISPLAYORDER().getContent());
        }
      }

      //reference documents
      System.out.println("**Reference Documents**");
      List<REFERENCEDOCUMENTSLISTITEM> referencedocumentslistitemList = cadsrDataElement.getREFERENCEDOCUMENTSLIST().getREFERENCEDOCUMENTSLISTITEM();
      if (!referencedocumentslistitemList.isEmpty()) {
        for (REFERENCEDOCUMENTSLISTITEM val : referencedocumentslistitemList) {
          System.out.println("reference documents list item: ");
          System.out.println(val.getName().getContent());
          //organization name
          String organizationName = val.getOrganizationName().getContent();
          if (organizationName.isEmpty()) {
            organizationName = val.getOrganizationName().getNULL();
            if (organizationName.equals("TRUE")) {
              organizationName = "NULL";
            }
          }
          System.out.println(organizationName);

          System.out.println(val.getDocumentType().getContent());
          System.out.println(val.getDocumentText().getContent());
          //url
          String url = val.getURL().getContent();
          if (url.isEmpty()) {
            url = val.getURL().getNULL();
            if (url.equals("TRUE")) {
              url = "NULL";
            }
          }
          System.out.println(url);

          System.out.println(val.getLanguage().getContent());
          //display order
          String displayOrder = val.getDisplayOrder().getContent();
          if (displayOrder.isEmpty()) {
            displayOrder = val.getDisplayOrder().getNULL();
            if (displayOrder.equals("TRUE")) {
              displayOrder = "NULL";
            }
          }
          System.out.println(displayOrder);
        }
      }

      //classification items
      System.out.println("**Classifications**");
      CLASSIFICATIONSLIST cadsrDataElementCLASSIFICATIONSLIST = cadsrDataElement.getCLASSIFICATIONSLIST();
      List<CLASSIFICATIONSLISTITEM> cadsrDataElementCLASSIFICATIONSLISTCLASSIFICATIONSLISTITEM = cadsrDataElementCLASSIFICATIONSLIST.getCLASSIFICATIONSLISTITEM();
      if (!cadsrDataElementCLASSIFICATIONSLISTCLASSIFICATIONSLISTITEM.isEmpty()) {
        for (CLASSIFICATIONSLISTITEM val : cadsrDataElementCLASSIFICATIONSLISTCLASSIFICATIONSLISTITEM) {
          System.out.println("classifications list item: ");
          System.out.println(" \t classification scheme: ");
          ClassificationScheme valClassificationScheme = val.getClassificationScheme();
          System.out.println(valClassificationScheme.getPublicId().getContent());
          System.out.println(valClassificationScheme.getContextName().getContent());
          System.out.println(valClassificationScheme.getContextVersion().getContent());
          System.out.println(valClassificationScheme.getPreferredName().getContent());
          System.out.println(valClassificationScheme.getVersion().getContent());
          System.out.println("classifications list item (cont): ");
          System.out.println(val.getClassificationSchemeItemName().getContent());
          System.out.println(val.getClassificationSchemeItemType().getContent());
          System.out.println(val.getCsiPublicId().getContent());
          System.out.println(val.getCsiVersion().getContent());
        }
      }

      //alternate names
      System.out.println("**Alternate Names**");
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

      //data element derivation
      System.out.println("**Data Element Derivation**");
      DATAELEMENTDERIVATION cadsrDataElementDATAELEMENTDERIVATION = cadsrDataElement.getDATAELEMENTDERIVATION();

      //derivation type
      String derivationType = cadsrDataElementDATAELEMENTDERIVATION.getDerivationType().getContent();
      if (derivationType.isEmpty()) {
        derivationType = cadsrDataElementDATAELEMENTDERIVATION.getDerivationType().getNULL();
        if (derivationType.equals("TRUE")) {
          derivationType = "NULL";
        }
      }
      System.out.println(derivationType);

      //derivation type description
      String derivationTypeDescription = cadsrDataElementDATAELEMENTDERIVATION.getDerivationTypeDescription().getContent();
      if (derivationTypeDescription.isEmpty()) {
        derivationTypeDescription = cadsrDataElementDATAELEMENTDERIVATION.getDerivationTypeDescription().getNULL();
        if (derivationTypeDescription.equals("TRUE")) {
          derivationTypeDescription = "NULL";
        }
      }
      System.out.println(derivationTypeDescription);

      //methods
      String methods = cadsrDataElementDATAELEMENTDERIVATION.getMethods().getContent();
      if (methods.isEmpty()) {
        methods = cadsrDataElementDATAELEMENTDERIVATION.getMethods().getNULL();
        if (methods.equals("TRUE")) {
          methods = "NULL";
        }
      }
      System.out.println(methods);

      //rule
      String rule = cadsrDataElementDATAELEMENTDERIVATION.getRule().getContent();
      if (rule.isEmpty()) {
        rule = cadsrDataElementDATAELEMENTDERIVATION.getRule().getNULL();
        if (rule.equals("TRUE")) {
          rule = "NULL";
        }
      }
      System.out.println(rule);

      //concatenation character
      String concatenationCharacter = cadsrDataElementDATAELEMENTDERIVATION.getConcatenationCharacter().getContent();
      if (concatenationCharacter.isEmpty()) {
        concatenationCharacter = cadsrDataElementDATAELEMENTDERIVATION.getConcatenationCharacter().getNULL();
        if (concatenationCharacter.equals("TRUE")) {
          concatenationCharacter = "NULL";
        }
      }
      System.out.println(concatenationCharacter);

      //component data elements
      System.out.println("**Component Data Elements**");
      List<ComponentDataElementsListITEM> componenentDataElementsList = cadsrDataElementDATAELEMENTDERIVATION.getComponentDataElementsList().getComponentDataElementsListITEM();
      String componenentDataElementsListNULLval = null;
      if (componenentDataElementsList.isEmpty()) {
         componenentDataElementsListNULLval = cadsrDataElementDATAELEMENTDERIVATION.getComponentDataElementsList().getNULL();
        if (componenentDataElementsListNULLval.equals("TRUE")) {
          componenentDataElementsListNULLval = "NULL";
        }
        System.out.println(componenentDataElementsListNULLval);
      } else {
        for (ComponentDataElementsListITEM val : componenentDataElementsList) {
          System.out.println("component data elements list item: ");
          System.out.println(val.getPublicId().getContent());
          System.out.println(val.getLongName().getContent());
          System.out.println(val.getPreferredName().getContent());
          System.out.println(val.getPreferredDefinition().getContent());
          System.out.println(val.getVersion().getContent());
          System.out.println(val.getWorkflowStatus().getContent());
          System.out.println(val.getContextName().getContent());
          System.out.println(val.getDisplayOrder().getContent());
        }
      }


      // Specify a temporary file to store a DataElement template instance
      File cdeFile = File.createTempFile("DataElement", ".json");

      System.out.println("Writing DataElement to " + cdeFile.getAbsolutePath());

      // Serialize the CDE instance
      mapper.writeValue(cdeFile, cedarDataElement);
    }
  }
}
