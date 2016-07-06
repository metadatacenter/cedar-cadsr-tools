package org.metadatacenter.ingestor.cadsr;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import java.io.File;
import java.io.IOException;

public class CDEXMLInstance2CEDARCDEInstance
{
  public static void main(String[] argc) throws IOException, JAXBException, DatatypeConfigurationException
  {
    JAXBContext jaxbContext = JAXBContext.newInstance(DataElementsList.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    ObjectMapper mapper = new ObjectMapper();
    File xmlFile = new File("src/main/resources/xml/xml_cde_201510293457_1_UTF8_short.xml");

    DataElementsList dataElementsList = ((DataElementsList)jaxbUnmarshaller.unmarshal(xmlFile));
    dataElementsList.toString();
  }
}
