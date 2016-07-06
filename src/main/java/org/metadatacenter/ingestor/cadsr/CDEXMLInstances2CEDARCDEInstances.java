package org.metadatacenter.ingestor.cadsr;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.metadatacenter.ingestor.cedar.CDE;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import java.io.File;
import java.io.IOException;

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
    for (DataElement dataElement : dataElementsList.dataElement) {

      System.out.println("Processing CDE....");

      // Create a CDE Java object (which we will serialize as a CEDAR template instance) for each DataElement
      CDE cde = new CDE();

      // Transfer the content of each DataElement to a CDE

      // Specify a temporary file to store a CDE template instance
      File cdeFile = File.createTempFile("CDE", "json");

      System.out.println("Writing CDE to " + cdeFile.getAbsolutePath());

      // Serialize the CDE instance
      mapper.writeValue(cdeFile, cde);
    }
  }
}
