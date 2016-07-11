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
      DATAELEMENTCONCEPT cadsrDATAELEMENTCONCEPT = cadsrDataElement.getDATAELEMENTCONCEPT();
      DataElementConcept cedarDataElementConcept = new DataElementConcept();

      //DEC Public ID
      String cadsrDECPublicID = cadsrDATAELEMENTCONCEPT.getPublicId().getContent();
      System.out.println(cadsrDECPublicID);
      PublicID_ cedarDECPublicID = new PublicID_();
      cedarDECPublicID.setValue(cadsrDECPublicID);
      cedarDataElementConcept.setPublicID(cedarDECPublicID);

      //DEC Preferred Name
      String cadsrDECPreferredName = cadsrDATAELEMENTCONCEPT.getPreferredName().getContent();
      System.out.println(cadsrDECPreferredName);
      org.metadatacenter.ingestor.cedar.PreferredName_ cedarDECPreferredName = new PreferredName_();
      cedarDECPreferredName.setValue(cadsrDECPreferredName);
      cedarDataElementConcept.setPreferredName(cedarDECPreferredName);


      //DEC Preferred Definition
      String cadsrDECPreferredDefinition = cadsrDATAELEMENTCONCEPT.getPreferredDefinition().getContent();
      System.out.println(cadsrDECPreferredDefinition);
      org.metadatacenter.ingestor.cedar.PreferredDefinition_ cedarDECPreferredDefinition = new PreferredDefinition_();
      cedarDECPreferredDefinition.setValue(cadsrPreferredDefinition);
      cedarDataElementConcept.setPreferredDefinition(cedarDECPreferredDefinition);

      //DEC Long Name
      String cadsrDECLongName = cadsrDATAELEMENTCONCEPT.getLongName().getContent();
      System.out.println(cadsrDECLongName);
      org.metadatacenter.ingestor.cedar.LongName_ cedarDECLongName = new LongName_();
      cedarDECLongName.setValue(cadsrDECLongName);
      cedarDataElementConcept.setLongName(cedarDECLongName);

      //DEC Version
      String cadsrDECVersion = cadsrDATAELEMENTCONCEPT.getVersion().getContent();
      System.out.println(cadsrDECVersion);
      org.metadatacenter.ingestor.cedar.Version_ cedarDECVersion = new Version_();
      cedarDECVersion.setValue(cadsrDECVersion);
      cedarDataElementConcept.setVersion(cedarDECVersion);

      //DEC Workflow Status
      String cadsrDECWorkflowStatus = cadsrDATAELEMENTCONCEPT.getWorkflowStatus().getContent();
      System.out.println(cadsrDECWorkflowStatus);
      org.metadatacenter.ingestor.cedar.WorkflowStatus_ cedarDECWorkflowStatus = new WorkflowStatus_();
      cedarDECWorkflowStatus.setValue(cadsrDECWorkflowStatus);
      cedarDataElementConcept.setWorkflowStatus(cedarDECWorkflowStatus);

      //DEC Context Name
      String cadsrDECContextName = cadsrDATAELEMENTCONCEPT.getContextName().getContent();
      System.out.println(cadsrDECContextName);
      org.metadatacenter.ingestor.cedar.ContextName_ cedarDECContextName = new ContextName_();
      cedarDECContextName.setValue(cadsrContextName);
      cedarDataElementConcept.setContextName(cedarDECContextName);

      //DEC Context Version
      String cadsrDECContextVersion = cadsrDATAELEMENTCONCEPT.getContextVersion().getContent();
      System.out.println(cadsrDECContextVersion);
      org.metadatacenter.ingestor.cedar.ContextVersion_ cedarDECContextVersion = new ContextVersion_();
      cedarContextVersion.setValue(cadsrContextVersion);
      cedarDataElementConcept.setContextVersion(cedarDECContextVersion);

      /* ******************************************************* */

      //DEC conceptual domain
      System.out.println("**DEC Conceptual Domain**");
      ConceptualDomain cadsrDATAELEMENTCONCEPTDECConceptualDomain = cadsrDATAELEMENTCONCEPT.getConceptualDomain();
      org.metadatacenter.ingestor.cedar.ConceptualDomain cedarDECConceptualDomain = new org.metadatacenter.ingestor.cedar.ConceptualDomain();


      //DEC CD public id
      String cadsrDECcdPublicID = cadsrDATAELEMENTCONCEPTDECConceptualDomain.getPublicId().getContent();
      System.out.println(cadsrDECcdPublicID);
      org.metadatacenter.ingestor.cedar.PublicID__ cedarDECcdPublicID = new org.metadatacenter.ingestor.cedar.PublicID__();
      cedarDECcdPublicID.setValue(cadsrDECcdPublicID);
      cedarDECConceptualDomain.setPublicID(cedarDECcdPublicID);

      //DEC CD context name
      String cadsrDECcdContextName = cadsrDATAELEMENTCONCEPTDECConceptualDomain.getContextName().getContent();
      System.out.println(cadsrDECcdContextName);
      org.metadatacenter.ingestor.cedar.ContextName__ cedarDECcdContextName = new org.metadatacenter.ingestor.cedar.ContextName__();
      cedarDECcdContextName.setValue(cadsrContextName);
      cedarDECConceptualDomain.setContextName(cedarDECcdContextName);

      //DEC CD context version
      String cadsrDECcdContextVersion = cadsrDATAELEMENTCONCEPTDECConceptualDomain.getContextVersion().getContent();
      System.out.println(cadsrDECcdContextVersion);
      org.metadatacenter.ingestor.cedar.ContextVersion__ cedarDECcdContextVersion = new org.metadatacenter.ingestor.cedar.ContextVersion__();
      cedarDECcdContextVersion.setValue(cadsrContextVersion);
      cedarDECConceptualDomain.setContextVersion(cedarDECcdContextVersion);

      //DEC CD preferred name
      String cadsrDECcdPreferredName = cadsrDATAELEMENTCONCEPTDECConceptualDomain.getPreferredName().getContent();
      System.out.println(cadsrDECcdPreferredName);
      org.metadatacenter.ingestor.cedar.PreferredName__ cedarDECcdPreferredName = new org.metadatacenter.ingestor.cedar.PreferredName__();
      cedarDECcdPreferredName.setValue(cadsrDECcdPreferredName);
      cedarDECConceptualDomain.setPreferredName(cedarDECcdPreferredName);

      //DEC CD version
      String cadsrDECcdVersion = cadsrDATAELEMENTCONCEPTDECConceptualDomain.getVersion().getContent();
      System.out.println(cadsrDECcdVersion);
      org.metadatacenter.ingestor.cedar.Version__ cedarDECcdVersion = new org.metadatacenter.ingestor.cedar.Version__();
      cedarDECcdContextVersion.setValue(cadsrDECcdContextVersion);
      cedarDECConceptualDomain.setVersion(cedarDECcdVersion);

      //DEC CD long name
      String cadsrDECcdLongName = cadsrDATAELEMENTCONCEPTDECConceptualDomain.getLongName().getContent();
      System.out.println(cadsrDECcdLongName);
      org.metadatacenter.ingestor.cedar.LongName__ cedarDECcdLongName = new org.metadatacenter.ingestor.cedar.LongName__();
      cedarDECcdLongName.setValue(cadsrDECcdLongName);
      cedarDECConceptualDomain.setLongName(cedarDECcdLongName);


      //wire DEC conceptual domain to DEC
      cedarDataElementConcept.setConceptualDomain(cedarDECConceptualDomain);

      /* ******************************************************* */

      //object class
      System.out.println("**ObjectClass**");
      ObjectClass cadsrDATAELEMENTCONCEPTObjectClass = cadsrDATAELEMENTCONCEPT.getObjectClass();
      org.metadatacenter.ingestor.cedar.ObjectClass cedarObjectClass = new org.metadatacenter.ingestor.cedar.ObjectClass();

      //object class public id
      String cadsrObjClassPublicID = cadsrDATAELEMENTCONCEPTObjectClass.getPublicId().getContent();
      System.out.println(cadsrObjClassPublicID);
      org.metadatacenter.ingestor.cedar.PublicID___ cedarObjClassPublicID = new org.metadatacenter.ingestor.cedar.PublicID___();
      cedarObjClassPublicID.setValue(cadsrObjClassPublicID);
      cedarObjectClass.setPublicID(cedarObjClassPublicID);

      //object class context name
      String cadsrObjClassContextName = cadsrDATAELEMENTCONCEPTObjectClass.getContextName().getContent();
      System.out.println(cadsrObjClassContextName);
      org.metadatacenter.ingestor.cedar.ContextName___ cedarObjClassContextName = new org.metadatacenter.ingestor.cedar.ContextName___();
      cedarObjClassContextName.setValue(cadsrObjClassContextName);
      cedarObjectClass.setContextName(cedarObjClassContextName);

      //object class context version
      String cadsrObjClassContextVersion = cadsrDATAELEMENTCONCEPTObjectClass.getContextVersion().getContent();
      System.out.println(cadsrObjClassContextVersion);
      org.metadatacenter.ingestor.cedar.ContextVersion___ cedarObjClassContextVersion = new org.metadatacenter.ingestor.cedar.ContextVersion___();
      cedarObjClassContextVersion.setValue(cadsrObjClassContextVersion);
      cedarObjectClass.setContextVersion(cedarObjClassContextVersion);

      //object class preferred name
      String cadsrObjClassPreferredName = cadsrDATAELEMENTCONCEPTObjectClass.getPreferredName().getContent();
      System.out.println(cadsrObjClassPreferredName);
      org.metadatacenter.ingestor.cedar.PreferredName___ cedarObjClassPreferredName = new org.metadatacenter.ingestor.cedar.PreferredName___();
      cedarObjClassPreferredName.setValue(cadsrObjClassPreferredName);
      cedarObjectClass.setPreferredName(cedarObjClassPreferredName);

      //object class version
      String cadsrObjClassVersion = cadsrDATAELEMENTCONCEPTObjectClass.getVersion().getContent();
      System.out.println(cadsrObjClassVersion);
      org.metadatacenter.ingestor.cedar.Version___ cedarObjClassVersion = new org.metadatacenter.ingestor.cedar.Version___();
      cedarObjClassVersion.setValue(cadsrObjClassVersion);
      cedarObjectClass.setVersion(cedarObjClassVersion);

      //object class long name
      String cadsrObjClassLongName = cadsrDATAELEMENTCONCEPTObjectClass.getLongName().getContent();
      System.out.println(cadsrObjClassLongName);
      org.metadatacenter.ingestor.cedar.LongName___ cedarObjClassLongName = new org.metadatacenter.ingestor.cedar.LongName___();
      cedarObjClassLongName.setValue(cadsrObjClassLongName);
      cedarObjectClass.setLongName(cedarObjClassLongName);

      /* ******************************************************* */

      //object class concept details list
      List<ConceptDetailsITEM> cadsrDATAELEMENTCONCEPTObjectClassConceptDetailsITEM = cadsrDATAELEMENTCONCEPTObjectClass.getConceptDetails().getConceptDetailsITEM();
      org.metadatacenter.ingestor.cedar.ConceptDetails cedarObjClassConceptDetails = new ConceptDetails();
      List<ConceptDetailsItem> cedarObjClassConceptDetailsList = new ArrayList<ConceptDetailsItem>();

      if (!cadsrDATAELEMENTCONCEPTObjectClassConceptDetailsITEM.isEmpty()) {
        for (ConceptDetailsITEM val : cadsrDATAELEMENTCONCEPTObjectClassConceptDetailsITEM) {
          org.metadatacenter.ingestor.cedar.ConceptDetailsItem cedarObjClassConceptDetailsItem = new ConceptDetailsItem();

          System.out.println("object class concept details list item: ");

          //obj class concept details preferred name
          String cadsrObjClassConceptDetailsItemPreferredName = val.getPREFERREDNAME().getContent();
          System.out.println(cadsrObjClassConceptDetailsItemPreferredName);
          org.metadatacenter.ingestor.cedar.PreferredName____ cedarObjClassConceptDetailsItemPreferredName = new PreferredName____();
          cedarObjClassConceptDetailsItemPreferredName.setValue(cadsrObjClassConceptDetailsItemPreferredName);
          cedarObjClassConceptDetailsItem.setPreferredName(cedarObjClassConceptDetailsItemPreferredName);

          //obj class concept details long name
          String cadsrObjClassConceptDetailsItemLongName = val.getLONGNAME().getContent();
          System.out.println(cadsrObjClassConceptDetailsItemLongName);
          org.metadatacenter.ingestor.cedar.LongName____ cedarObjClassConceptDetailsItemLongName = new LongName____();
          cedarObjClassConceptDetailsItemLongName.setValue(cadsrObjClassConceptDetailsItemLongName);
          cedarObjClassConceptDetailsItem.setLongName(cedarObjClassConceptDetailsItemLongName);

          //obj class concept details concept id
          String cadsrObjClassConceptDetailsItemConID = val.getCONID().getContent();
          System.out.println(cadsrObjClassConceptDetailsItemConID);
          org.metadatacenter.ingestor.cedar.ConceptID cedarObjClassConceptDetailsItemConID = new ConceptID();
          cedarObjClassConceptDetailsItemConID.setValue(cadsrObjClassConceptDetailsItemConID);
          cedarObjClassConceptDetailsItem.setConceptID(cedarObjClassConceptDetailsItemConID);

          //obj class concept details definition source
          String cadsrObjClassConceptDetailsItemDefSource = val.getDEFINITIONSOURCE().getContent();
          System.out.println(cadsrObjClassConceptDetailsItemDefSource);
          org.metadatacenter.ingestor.cedar.DefinitionSource cedarObjClassConceptDetailsItemDefSource = new DefinitionSource();
          cedarObjClassConceptDetailsItemDefSource.setValue(cadsrObjClassConceptDetailsItemDefSource);
          cedarObjClassConceptDetailsItem.setDefinitionSource(cedarObjClassConceptDetailsItemDefSource);

          //obj class concept details origin
          String cadsrObjClassConceptDetailsItemOrigin = val.getORIGIN().getContent();
          System.out.println(cadsrObjClassConceptDetailsItemOrigin);
          org.metadatacenter.ingestor.cedar.Origin_ cedarObjClassConceptDetailsItemOrigin = new Origin_();
          cedarObjClassConceptDetailsItemOrigin.setValue(cadsrObjClassConceptDetailsItemOrigin);
          cedarObjClassConceptDetailsItem.setOrigin(cedarObjClassConceptDetailsItemOrigin);

          //obj class concept details EVS Source
          String cadsrObjClassConceptDetailsItemEVS = val.getEVSSOURCE().getContent();
          System.out.println(cadsrObjClassConceptDetailsItemEVS);
          org.metadatacenter.ingestor.cedar.EVSSource cedarObjClassConceptDetailsItemEVS = new EVSSource();
          cedarObjClassConceptDetailsItemEVS.setValue(cadsrObjClassConceptDetailsItemEVS);
          cedarObjClassConceptDetailsItem.setEVSSource(cedarObjClassConceptDetailsItemEVS);

          //obj class concept details primary flag indicator
          String cadsrObjClassConceptDetailsItemPrimaryFlag = val.getPRIMARYFLAGIND().getContent();
          System.out.println(cadsrObjClassConceptDetailsItemPrimaryFlag);
          org.metadatacenter.ingestor.cedar.PrimaryFlag cedarObjClassConceptDetailsItemPrimaryFlag = new PrimaryFlag();
          cedarObjClassConceptDetailsItemPrimaryFlag.setValue(cadsrObjClassConceptDetailsItemPrimaryFlag);
          cedarObjClassConceptDetailsItem.setPrimaryFlag(cedarObjClassConceptDetailsItemPrimaryFlag);

          //obj class concept details display order
          String cadsrObjClassConceptDetailsItemDisplayOrder = val.getDISPLAYORDER().getContent();
          System.out.println(cadsrObjClassConceptDetailsItemDisplayOrder);
          org.metadatacenter.ingestor.cedar.DisplayOrder cedarObjClassConceptDetailsItemDisplayOrder = new DisplayOrder();
          cedarObjClassConceptDetailsItemDisplayOrder.setValue(cadsrObjClassConceptDetailsItemDisplayOrder);
          cedarObjClassConceptDetailsItem.setDisplayOrder(cedarObjClassConceptDetailsItemDisplayOrder);

          cedarObjClassConceptDetailsList.add(cedarObjClassConceptDetailsItem);
        }
      }

      cedarObjClassConceptDetails.setConceptDetailsItem(cedarObjClassConceptDetailsList);

      cedarObjectClass.setConceptDetails(cedarObjClassConceptDetails);

      //TODO figure out empty case (null?)

      cedarDataElementConcept.setObjectClass(cedarObjectClass);

      /* ******************************************************* */

      //property
      System.out.println("**Property**");
      Property cadsrDATAELEMENTCONCEPTProperty = cadsrDATAELEMENTCONCEPT.getProperty();

      org.metadatacenter.ingestor.cedar.Property cedarProperty = new org.metadatacenter.ingestor.cedar.Property();

      //property public id
      String cadsrPropertyPublicID = cadsrDATAELEMENTCONCEPTProperty.getPublicId().getContent();
      System.out.println(cadsrPropertyPublicID);
      org.metadatacenter.ingestor.cedar.PublicID____ cedarPropertyPublicID = new org.metadatacenter.ingestor.cedar.PublicID____();
      cedarPropertyPublicID.setValue(cadsrPropertyPublicID);
      cedarProperty.setPublicID(cedarPropertyPublicID);

      //property context name
      String cadsrPropertyContextName = cadsrDATAELEMENTCONCEPTProperty.getContextName().getContent();
      System.out.println(cadsrPropertyContextName);
      org.metadatacenter.ingestor.cedar.ContextName____ cedarPropertyContextName = new org.metadatacenter.ingestor.cedar.ContextName____();
      cedarPropertyContextName.setValue(cadsrPropertyContextName);
      cedarProperty.setContextName(cedarPropertyContextName);

      //property context version
      String cadsrPropertyContextVersion = cadsrDATAELEMENTCONCEPTProperty.getContextVersion().getContent();
      System.out.println(cadsrPropertyContextVersion);
      org.metadatacenter.ingestor.cedar.ContextVersion____ cedarPropertyContextVersion = new org.metadatacenter.ingestor.cedar.ContextVersion____();
      cedarPropertyContextVersion.setValue(cadsrPropertyContextVersion);
      cedarProperty.setContextVersion(cedarPropertyContextVersion);

      //property preferred name
      String cadsrPropertyPreferredName = cadsrDATAELEMENTCONCEPTProperty.getPreferredName().getContent();
      System.out.println(cadsrPropertyPreferredName);
      org.metadatacenter.ingestor.cedar.PreferredName_____ cedarPropertyPreferredName = new org.metadatacenter.ingestor.cedar.PreferredName_____();
      cedarPropertyPreferredName.setValue(cadsrPropertyPreferredName);
      cedarProperty.setPreferredName(cedarPropertyPreferredName);

      //property version
      String cadsrPropertyVersion = cadsrDATAELEMENTCONCEPTProperty.getVersion().getContent();
      System.out.println(cadsrPropertyVersion);
      org.metadatacenter.ingestor.cedar.Version____ cedarPropertyVersion = new org.metadatacenter.ingestor.cedar.Version____();
      cedarPropertyVersion.setValue(cadsrPropertyVersion);
      cedarProperty.setVersion(cedarPropertyVersion);

      //property long name
      String cadsrPropertyLongName = cadsrDATAELEMENTCONCEPTProperty.getLongName().getContent();
      System.out.println(cadsrPropertyLongName);
      org.metadatacenter.ingestor.cedar.LongName_____ cedarPropertyLongName = new org.metadatacenter.ingestor.cedar.LongName_____();
      cedarPropertyLongName.setValue(cadsrPropertyLongName);
      cedarProperty.setLongName(cedarPropertyLongName);

      /* ******************************************************* */

      //property concept details list
      List<ConceptDetailsITEM> cadsrDATAELEMENTCONCEPTPropertyConceptDetailsITEM = cadsrDATAELEMENTCONCEPTProperty.getConceptDetails().getConceptDetailsITEM();
      org.metadatacenter.ingestor.cedar.ConceptDetails_ cedarPropertyConceptDetails = new ConceptDetails_();
      List<ConceptDetailsItem_> cedarPropertyConceptDetailsList = new ArrayList<ConceptDetailsItem_>();

      if (!cadsrDATAELEMENTCONCEPTPropertyConceptDetailsITEM.isEmpty()) {
        for (ConceptDetailsITEM val : cadsrDATAELEMENTCONCEPTPropertyConceptDetailsITEM) {
          org.metadatacenter.ingestor.cedar.ConceptDetailsItem_ cedarPropertyConceptDetailsItem = new ConceptDetailsItem_();

          System.out.println("property concept details list item: ");

          //property concept details preferred name
          String cadsrPropertyConceptDetailsItemPreferredName = val.getPREFERREDNAME().getContent();
          System.out.println(cadsrPropertyConceptDetailsItemPreferredName);
          org.metadatacenter.ingestor.cedar.PreferredName______ cedarPropertyConceptDetailsItemPreferredName = new PreferredName______();
          cedarPropertyConceptDetailsItemPreferredName.setValue(cadsrPropertyConceptDetailsItemPreferredName);
          cedarPropertyConceptDetailsItem.setPreferredName(cedarPropertyConceptDetailsItemPreferredName);

          //property concept details long name
          String cadsrPropertyConceptDetailsItemLongName = val.getLONGNAME().getContent();
          System.out.println(cadsrPropertyConceptDetailsItemLongName);
          org.metadatacenter.ingestor.cedar.LongName______ cedarPropertyConceptDetailsItemLongName = new LongName______();
          cedarPropertyConceptDetailsItemLongName.setValue(cadsrPropertyConceptDetailsItemLongName);
          cedarPropertyConceptDetailsItem.setLongName(cedarPropertyConceptDetailsItemLongName);

          //property concept details concept id
          String cadsrPropertyConceptDetailsItemConID = val.getCONID().getContent();
          System.out.println(cadsrPropertyConceptDetailsItemConID);
          org.metadatacenter.ingestor.cedar.ConceptID_ cedarPropertyConceptDetailsItemConID = new ConceptID_();
          cedarPropertyConceptDetailsItemConID.setValue(cadsrPropertyConceptDetailsItemConID);
          cedarPropertyConceptDetailsItem.setConceptID(cedarPropertyConceptDetailsItemConID);

          //property concept details definition source
          String cadsrPropertyConceptDetailsItemDefSource = val.getDEFINITIONSOURCE().getContent();
          System.out.println(cadsrPropertyConceptDetailsItemDefSource);
          org.metadatacenter.ingestor.cedar.DefinitionSource_ cedarPropertyConceptDetailsItemDefSource = new DefinitionSource_();
          cedarPropertyConceptDetailsItemDefSource.setValue(cadsrPropertyConceptDetailsItemDefSource);
          cedarPropertyConceptDetailsItem.setDefinitionSource(cedarPropertyConceptDetailsItemDefSource);

          //property concept details origin
          String cadsrPropertyConceptDetailsItemOrigin = val.getORIGIN().getContent();
          System.out.println(cadsrPropertyConceptDetailsItemOrigin);
          org.metadatacenter.ingestor.cedar.Origin__ cedarPropertyConceptDetailsItemOrigin = new Origin__();
          cedarPropertyConceptDetailsItemOrigin.setValue(cadsrPropertyConceptDetailsItemOrigin);
          cedarPropertyConceptDetailsItem.setOrigin(cedarPropertyConceptDetailsItemOrigin);

          //property concept details EVS Source
          String cadsrPropertyConceptDetailsItemEVS = val.getEVSSOURCE().getContent();
          System.out.println(cadsrPropertyConceptDetailsItemEVS);
          org.metadatacenter.ingestor.cedar.EVSSource_ cedarPropertyConceptDetailsItemEVS = new EVSSource_();
          cedarPropertyConceptDetailsItemEVS.setValue(cadsrPropertyConceptDetailsItemEVS);
          cedarPropertyConceptDetailsItem.setEVSSource(cedarPropertyConceptDetailsItemEVS);

          //property concept details primary flag indicator
          String cadsrPropertyConceptDetailsItemPrimaryFlag = val.getPRIMARYFLAGIND().getContent();
          System.out.println(cadsrPropertyConceptDetailsItemPrimaryFlag);
          org.metadatacenter.ingestor.cedar.PrimaryFlag_ cedarPropertyConceptDetailsItemPrimaryFlag = new PrimaryFlag_();
          cedarPropertyConceptDetailsItemPrimaryFlag.setValue(cadsrPropertyConceptDetailsItemPrimaryFlag);
          cedarPropertyConceptDetailsItem.setPrimaryFlag(cedarPropertyConceptDetailsItemPrimaryFlag);

          //property concept details display order
          String cadsrPropertyConceptDetailsItemDisplayOrder = val.getDISPLAYORDER().getContent();
          System.out.println(cadsrPropertyConceptDetailsItemDisplayOrder);
          org.metadatacenter.ingestor.cedar.DisplayOrder_ cedarPropertyConceptDetailsItemDisplayOrder = new DisplayOrder_();
          cedarPropertyConceptDetailsItemDisplayOrder.setValue(cadsrPropertyConceptDetailsItemDisplayOrder);
          cedarPropertyConceptDetailsItem.setDisplayOrder(cedarPropertyConceptDetailsItemDisplayOrder);

          cedarPropertyConceptDetailsList.add(cedarPropertyConceptDetailsItem);

        }
      }

      cedarPropertyConceptDetails.setConceptDetailsItem(cedarPropertyConceptDetailsList);

      cedarProperty.setConceptDetails(cedarPropertyConceptDetails);

      cedarDataElementConcept.setProperty(cedarProperty);

      /* ******************************************************* */

      //object class qualifier
      String cadsrObjectClassQualifier = cadsrDATAELEMENTCONCEPT.getObjectClassQualifier().getContent();
      org.metadatacenter.ingestor.cedar.ObjectClassQualifier cedarObjectClassQualifier = new org.metadatacenter.ingestor.cedar.ObjectClassQualifier();
      if (cadsrObjectClassQualifier.isEmpty()) {
        cadsrObjectClassQualifier = cadsrDATAELEMENTCONCEPT.getObjectClassQualifier().getNULL();
        if (cadsrObjectClassQualifier.equals("TRUE")) {
          cadsrObjectClassQualifier = "NULL";
        }
      }
      System.out.println(cadsrObjectClassQualifier);
      cedarObjectClassQualifier.setValue(cadsrObjectClassQualifier);
      cedarDataElementConcept.setObjectClassQualifier(cedarObjectClassQualifier);

      /* ******************************************************* */

      //property qualifier
      String cadsrPropertyQualifier = cadsrDATAELEMENTCONCEPT.getPropertyQualifier().getContent();
      org.metadatacenter.ingestor.cedar.PropertyQualifier cedarPropertyQualifier = new org.metadatacenter.ingestor.cedar.PropertyQualifier();
      if (cadsrPropertyQualifier.isEmpty()) {
        cadsrPropertyQualifier = cadsrDATAELEMENTCONCEPT.getPropertyQualifier().getNULL();
        if (cadsrPropertyQualifier.equals("TRUE")) {
          cadsrPropertyQualifier = "NULL";
        }
      }
      System.out.println(cadsrPropertyQualifier);
      cedarPropertyQualifier.setValue(cadsrPropertyQualifier);
      cedarDataElementConcept.setPropertyQualifier(cedarPropertyQualifier);

      /* ******************************************************* */

      //origin
      String cadsrDECOrigin = cadsrDATAELEMENTCONCEPT.getOrigin().getContent();
      org.metadatacenter.ingestor.cedar.Origin___ cedarDECOrigin = new org.metadatacenter.ingestor.cedar.Origin___();
      if (cadsrDECOrigin.isEmpty()) {
        cadsrDECOrigin = cadsrDATAELEMENTCONCEPT.getOrigin().getNULL();
        if (cadsrDECOrigin.equals("TRUE")) {
          cadsrDECOrigin = "NULL";
        }
      }
      System.out.println(cadsrDECOrigin);
      cedarDECOrigin.setValue(cadsrDECOrigin);
      cedarDataElementConcept.setOrigin(cedarDECOrigin);



      // wire cedar DEC to data element
      //TODO check DEC is complete
      cedarDataElement.setDataElementConcept(cedarDataElementConcept);


      /* ******************************************************* */
      /* ******************************************************* */


      //value domain
      System.out.println("**Value Domain**");
      VALUEDOMAIN cadsrVALUEDOMAIN = cadsrDataElement.getVALUEDOMAIN();
      org.metadatacenter.ingestor.cedar.ValueDomain cedarValueDomain = new org.metadatacenter.ingestor.cedar.ValueDomain();

      //value domain public id
      String cadsrValueDomainPublicID = cadsrVALUEDOMAIN.getPublicId().getContent();
      System.out.println(cadsrValueDomainPublicID);
      org.metadatacenter.ingestor.cedar.PublicID_____ cedarValueDomainPublicID = new org.metadatacenter.ingestor.cedar.PublicID_____();
      cedarValueDomainPublicID.setValue(cadsrValueDomainPublicID);
      cedarValueDomain.setPublicID(cedarValueDomainPublicID);

      //value domain preferred name
      String cadsrValueDomainPreferredName = cadsrVALUEDOMAIN.getPreferredName().getContent();
      System.out.println(cadsrValueDomainPreferredName);
      org.metadatacenter.ingestor.cedar.PreferredName_______ cedarValueDomainPreferredName = new org.metadatacenter.ingestor.cedar.PreferredName_______();
      cedarValueDomainPreferredName.setValue(cadsrValueDomainPreferredName);
      cedarValueDomain.setPreferredName(cedarValueDomainPreferredName);

      //value domain preferred definition
      String cadsrValueDomainPreferredDefinition = cadsrVALUEDOMAIN.getPreferredDefinition().getContent();
      System.out.println(cadsrValueDomainPreferredDefinition);
      org.metadatacenter.ingestor.cedar.PreferredDefinition__ cedarValueDomainPreferredDefinition = new org.metadatacenter.ingestor.cedar.PreferredDefinition__();
      cedarValueDomainPreferredDefinition.setValue(cadsrValueDomainPreferredDefinition);
      cedarValueDomain.setPreferredDefinition(cedarValueDomainPreferredDefinition);

      //value domain long name
      String cadsrValueDomainLongName = cadsrVALUEDOMAIN.getLongName().getContent();
      System.out.println(cadsrValueDomainLongName);
      org.metadatacenter.ingestor.cedar.LongName_______ cedarValueDomainLongName = new org.metadatacenter.ingestor.cedar.LongName_______();
      cedarValueDomainLongName.setValue(cadsrValueDomainLongName);
      cedarValueDomain.setLongName(cedarValueDomainLongName);

      //value domain version
      String cadsrValueDomainVersion = cadsrVALUEDOMAIN.getVersion().getContent();
      System.out.println(cadsrValueDomainVersion);
      org.metadatacenter.ingestor.cedar.Version_____ cedarValueDomainVersion = new org.metadatacenter.ingestor.cedar.Version_____();
      cedarValueDomainVersion.setValue(cadsrValueDomainVersion);
      cedarValueDomain.setVersion(cedarValueDomainVersion);

      //value domain workflow status
      String cadsrValueDomainWorkflowStatus = cadsrVALUEDOMAIN.getWorkflowStatus().getContent();
      System.out.println(cadsrValueDomainWorkflowStatus);
      org.metadatacenter.ingestor.cedar.WorkflowStatus__ cedarValueDomainWorkflowStatus = new org.metadatacenter.ingestor.cedar.WorkflowStatus__();
      cedarValueDomainWorkflowStatus.setValue(cadsrValueDomainWorkflowStatus);
      cedarValueDomain.setWorkflowStatus(cedarValueDomainWorkflowStatus);

      //value domain context name
      String cadsrValueDomainContextName = cadsrVALUEDOMAIN.getContextName().getContent();
      System.out.println(cadsrValueDomainContextName);
      org.metadatacenter.ingestor.cedar.ContextName_____ cedarValueDomainContextName = new org.metadatacenter.ingestor.cedar.ContextName_____();
      cedarValueDomainContextName.setValue(cadsrValueDomainContextName);
      cedarValueDomain.setContextName(cedarValueDomainContextName);

      //value domain context version
      String cadsrValueDomainContextVersion = cadsrVALUEDOMAIN.getContextVersion().getContent();
      System.out.println(cadsrValueDomainContextVersion);
      org.metadatacenter.ingestor.cedar.ContextVersion_____ cedarValueDomainContextVersion = new org.metadatacenter.ingestor.cedar.ContextVersion_____();
      cedarValueDomainContextVersion.setValue(cadsrValueDomainContextVersion);
      cedarValueDomain.setContextVersion(cedarValueDomainContextVersion);

      /* ******************************************************* */

      //value domain conceptual domain
      System.out.println("**VD Conceptual Domain**");
      ConceptualDomain cadsrVDConceptualDomain = cadsrVALUEDOMAIN.getConceptualDomain();
      org.metadatacenter.ingestor.cedar.ConceptualDomain_ cedarVDConceptualDomain = new org.metadatacenter.ingestor.cedar.ConceptualDomain_();

      //value domain conceptual domain public id
      String cadsrVDConceptualDomainPublicID = cadsrVDConceptualDomain.getPublicId().getContent();
      System.out.println(cadsrVDConceptualDomainPublicID);

      //value domain conceptual domain context name
      String cadsrVDConceptualDomainContextName = cadsrVDConceptualDomain.getContextName().getContent();
      System.out.println(cadsrVDConceptualDomainContextName);

      //value domain conceptual domain context version
      String cadsrVDConceptualDomainContextVersion = cadsrVDConceptualDomain.getContextVersion().getContent();
      System.out.println(cadsrVDConceptualDomainContextVersion);

      //value domain conceptual domain preferred name
      String cadsrVDConceptualDomainPreferredName = cadsrVDConceptualDomain.getPreferredName().getContent();
      System.out.println(cadsrVDConceptualDomainPreferredName);

      //value domain conceptual domain version
      String cadsrVDConceptualDomainVersion = cadsrVDConceptualDomain.getVersion().getContent();
      System.out.println(cadsrVDConceptualDomainVersion);

      //value domain conceptual domain long name
      String cadsrVDConceptualDomainLongName = cadsrVDConceptualDomain.getLongName().getContent();
      System.out.println(cadsrVDConceptualDomainLongName);

      //TODO return here

      cedarValueDomain.setConceptualDomain(cedarVDConceptualDomain);

      System.out.println("**Value Domain cont**");
      System.out.println(cadsrVALUEDOMAIN.getDatatype().getContent());
      System.out.println(cadsrVALUEDOMAIN.getValueDomainType().getContent());

      //unit of measure
      String unitOfMeasure = cadsrVALUEDOMAIN.getUnitOfMeasure().getContent();
      if (unitOfMeasure.isEmpty()) {
        unitOfMeasure = cadsrVALUEDOMAIN.getUnitOfMeasure().getNULL();
        if (unitOfMeasure.equals("TRUE")) {
          unitOfMeasure = "NULL";
        }
      }
      System.out.println(unitOfMeasure);

      //display format
      String displayFormat = cadsrVALUEDOMAIN.getDisplayFormat().getContent();
      if (displayFormat.isEmpty()) {
        displayFormat = cadsrVALUEDOMAIN.getDisplayFormat().getNULL();
        if (displayFormat.equals("TRUE")) {
          displayFormat = "NULL";
        }
      }
      System.out.println(displayFormat);

      //max and min length
      System.out.println(cadsrVALUEDOMAIN.getMaximumLength().getContent());
      System.out.println(cadsrVALUEDOMAIN.getMinimumLength().getContent());

      //decimal place
      String decimalPlace = cadsrVALUEDOMAIN.getDecimalPlace().getContent();
      if (decimalPlace.isEmpty()) {
        decimalPlace = cadsrVALUEDOMAIN.getDecimalPlace().getNULL();
        if (decimalPlace.equals("TRUE")) {
          decimalPlace = "NULL";
        }
      }
      System.out.println(decimalPlace);

      //character set name
      String characterSetName = cadsrVALUEDOMAIN.getCharacterSetName().getContent();
      if (characterSetName.isEmpty()) {
        characterSetName = cadsrVALUEDOMAIN.getCharacterSetName().getNULL();
        if (characterSetName.equals("TRUE")) {
          characterSetName = "NULL";
        }
      }
      System.out.println(characterSetName);

      //max value
      String maximumValue = cadsrVALUEDOMAIN.getMaximumValue().getContent();
      if (maximumValue.isEmpty()) {
        maximumValue = cadsrVALUEDOMAIN.getMaximumValue().getNULL();
        if (maximumValue.equals("TRUE")) {
          maximumValue = "NULL";
        }
      }
      System.out.println(maximumValue);

      //min value
      String minimumValue = cadsrVALUEDOMAIN.getMinimumValue().getContent();
      if (minimumValue.isEmpty()) {
        minimumValue = cadsrVALUEDOMAIN.getMinimumValue().getNULL();
        if (minimumValue.equals("TRUE")) {
          minimumValue = "NULL";
        }
      }
      System.out.println(minimumValue);

      //origin
      String origin = cadsrVALUEDOMAIN.getOrigin().getContent();
      if (origin.isEmpty()) {
        origin = cadsrVALUEDOMAIN.getOrigin().getNULL();
        if (origin.equals("TRUE")) {
          origin = "NULL";
        }
      }
      System.out.println(origin);

      //representation
      System.out.println("**Representation**");
      Representation cadsrVALUEDOMAINRepresentation = cadsrVALUEDOMAIN.getRepresentation();
      System.out.println(cadsrVALUEDOMAINRepresentation.getPublicId().getContent());
      System.out.println(cadsrVALUEDOMAINRepresentation.getContextName().getContent());
      System.out.println(cadsrVALUEDOMAINRepresentation.getContextVersion().getContent());
      System.out.println(cadsrVALUEDOMAINRepresentation.getPreferredName().getContent());
      System.out.println(cadsrVALUEDOMAINRepresentation.getVersion().getContent());
      System.out.println(cadsrVALUEDOMAINRepresentation.getLongName().getContent());

      /* TODO:
       * does every value have to be checked for null?
       * next, wire all these values to xml classes*/

      //representation concept details list
      List<ConceptDetailsITEM> cadsrVALUEDOMAINRepresentationConceptDetailsITEM = cadsrVALUEDOMAINRepresentation.getConceptDetails().getConceptDetailsITEM();
      if (!cadsrVALUEDOMAINRepresentationConceptDetailsITEM.isEmpty()) {
        for (ConceptDetailsITEM val : cadsrVALUEDOMAINRepresentationConceptDetailsITEM) {
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
      List<PermissibleValuesITEM> permissibleValuesITEMList = cadsrVALUEDOMAIN.getPermissibleValues().getPermissibleValuesITEM();
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
      List<ValueDomainConceptsITEM> valueDomainConceptsITEMList = cadsrVALUEDOMAIN.getValueDomainConcepts().getValueDomainConceptsITEM();
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
      CLASSIFICATIONSLIST cadsrCLASSIFICATIONSLIST = cadsrDataElement.getCLASSIFICATIONSLIST();
      List<CLASSIFICATIONSLISTITEM> cadsrCLASSIFICATIONSLISTITEM = cadsrCLASSIFICATIONSLIST.getCLASSIFICATIONSLISTITEM();
      if (!cadsrCLASSIFICATIONSLISTITEM.isEmpty()) {
        for (CLASSIFICATIONSLISTITEM val : cadsrCLASSIFICATIONSLISTITEM) {
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
      ALTERNATENAMELIST cadsrALTERNATENAMELIST = cadsrDataElement.getALTERNATENAMELIST();
      List<ALTERNATENAMELISTITEM> cadsrALTERNATENAMELISTITEM = cadsrALTERNATENAMELIST.getALTERNATENAMELISTITEM();
      if (!cadsrALTERNATENAMELISTITEM.isEmpty()) {
        for (ALTERNATENAMELISTITEM val : cadsrALTERNATENAMELISTITEM) {
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
      DATAELEMENTDERIVATION cadsrDATAELEMENTDERIVATION = cadsrDataElement.getDATAELEMENTDERIVATION();

      //derivation type
      String derivationType = cadsrDATAELEMENTDERIVATION.getDerivationType().getContent();
      if (derivationType.isEmpty()) {
        derivationType = cadsrDATAELEMENTDERIVATION.getDerivationType().getNULL();
        if (derivationType.equals("TRUE")) {
          derivationType = "NULL";
        }
      }
      System.out.println(derivationType);

      //derivation type description
      String derivationTypeDescription = cadsrDATAELEMENTDERIVATION.getDerivationTypeDescription().getContent();
      if (derivationTypeDescription.isEmpty()) {
        derivationTypeDescription = cadsrDATAELEMENTDERIVATION.getDerivationTypeDescription().getNULL();
        if (derivationTypeDescription.equals("TRUE")) {
          derivationTypeDescription = "NULL";
        }
      }
      System.out.println(derivationTypeDescription);

      //methods
      String methods = cadsrDATAELEMENTDERIVATION.getMethods().getContent();
      if (methods.isEmpty()) {
        methods = cadsrDATAELEMENTDERIVATION.getMethods().getNULL();
        if (methods.equals("TRUE")) {
          methods = "NULL";
        }
      }
      System.out.println(methods);

      //rule
      String rule = cadsrDATAELEMENTDERIVATION.getRule().getContent();
      if (rule.isEmpty()) {
        rule = cadsrDATAELEMENTDERIVATION.getRule().getNULL();
        if (rule.equals("TRUE")) {
          rule = "NULL";
        }
      }
      System.out.println(rule);

      //concatenation character
      String concatenationCharacter = cadsrDATAELEMENTDERIVATION.getConcatenationCharacter().getContent();
      if (concatenationCharacter.isEmpty()) {
        concatenationCharacter = cadsrDATAELEMENTDERIVATION.getConcatenationCharacter().getNULL();
        if (concatenationCharacter.equals("TRUE")) {
          concatenationCharacter = "NULL";
        }
      }
      System.out.println(concatenationCharacter);

      //component data elements
      System.out.println("**Component Data Elements**");
      List<ComponentDataElementsListITEM> componenentDataElementsList = cadsrDATAELEMENTDERIVATION.getComponentDataElementsList().getComponentDataElementsListITEM();
      String componenentDataElementsListNULLval = null;
      if (componenentDataElementsList.isEmpty()) {
         componenentDataElementsListNULLval = cadsrDATAELEMENTDERIVATION.getComponentDataElementsList().getNULL();
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
