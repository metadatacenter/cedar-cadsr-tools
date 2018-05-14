package org.metadatacenter.cadsr.ingestor;

import org.junit.Before;
import org.junit.Test;
import org.metadatacenter.cadsr.DataElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ValueConstraintsHandlerTest {

  private static final Logger logger = LoggerFactory.getLogger(InputTypeHandlerTest.class);

  private ValueConstraintsHandler handler;

  @Before
  public void init() {
    handler = new ValueConstraintsHandler();
  }

  @Test
  public void shouldNotProduceVCs_NON_ENUMERATED_DATE() throws JAXBException, IOException {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-2001826.xml");
    try {
      assertNonEnumeratedType(dataElement);
    } catch (UnsupportedDataElementException e) {
      logger.warn(e.getMessage());
    }
  }

  @Test
  public void shouldNotProduceVCs_NON_ENUMERATED_JAVA_DATE() throws JAXBException, IOException {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-2513819.xml");
    try {
      assertNonEnumeratedType(dataElement);
    } catch (UnsupportedDataElementException e) {
      logger.warn(e.getMessage());
    }
  }

  @Test
  public void shouldProduceVCs_ENUMERATED_CHARACTER() throws JAXBException, IOException {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-5873923.xml");
    try {
      ValueConstraintsHandler newHandler = handler.handle(dataElement);
      List<Map<String, Object>> ontologies = newHandler.getOntologies();
      List<Map<String, Object>> valueSets = newHandler.getValueSets();
      List<Map<String, Object>> branches = newHandler.getBranches();
      List<Map<String, Object>> classes = newHandler.getClasses();
      // Assert
      assertThat(ontologies.isEmpty(), is(true));
      assertThat(valueSets.isEmpty(), is(true));
      assertThat(branches.isEmpty(), is(true));
      assertThat(classes.isEmpty(), is(false));
      assertThat(classes.size(), is(2));
      assertThat(classes.get(0).get("uri"), is("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C49488"));
      assertThat(classes.get(0).get("label"), is("Yes"));
      assertThat(classes.get(0).get("prefLabel"), is("Yes"));
      assertThat(classes.get(1).get("uri"), is("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C49487"));
      assertThat(classes.get(1).get("label"), is("No"));
      assertThat(classes.get(1).get("prefLabel"), is("No"));
    } catch (UnsupportedDataElementException e) {
      logger.warn(e.getMessage());
    }
  }

  @Test
  public void shouldNotProduceVCs_NON_ENUMERATED_JAVA_STRING() throws JAXBException, IOException {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-2608127.xml");
    try {
      assertNonEnumeratedType(dataElement);
    } catch (UnsupportedDataElementException e) {
      logger.warn(e.getMessage());
    }
  }

  @Test(expected = UnsupportedDataElementException.class)
  public void shouldThrowException_ENUMERATED_ALPHANUMERIC_PARTIALLY_ANNOTATED() throws Exception {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-3245384.xml");
    try {
      ValueConstraintsHandler newHandler = handler.handle(dataElement);
    } catch (UnsupportedDataElementException e) {
      String message = e.getMessage();
      assertThat(message, is("Failed to convert 'Orthodontist Model Use Use Frequency' (ID: 3245384) - Reason: " +
          "Controlled term for value 'OCCASIONALLY' is null (NullValue)"));
      throw e;
    }
  }

  @Test
  public void shouldNotProduceVCs_NON_ENUMERATED_ISO21090CD() throws JAXBException, IOException {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-3177059.xml");
    try {
      assertNonEnumeratedType(dataElement);
    } catch (UnsupportedDataElementException e) {
      logger.warn(e.getMessage());
    }
  }

  @Test
  public void shouldNotProduceVCs_NON_ENUMERATED_NUMBER() throws JAXBException, IOException {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-2002061.xml");
    try {
      assertNonEnumeratedType(dataElement);
    } catch (UnsupportedDataElementException e) {
      logger.warn(e.getMessage());
    }
  }

  @Test
  public void shouldNotProduceVCs_NON_ENUMERATED_JAVA_LONG() throws JAXBException, IOException {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-2608393.xml");
    try {
      assertNonEnumeratedType(dataElement);
    } catch (UnsupportedDataElementException e) {
      logger.warn(e.getMessage());
    }
  }

  @Test
  public void shouldNotProduceVCs_NON_ENUMERATED_JAVA_INTEGER() throws JAXBException, IOException {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-2513896.xml");
    try {
      assertNonEnumeratedType(dataElement);
    } catch (UnsupportedDataElementException e) {
      logger.warn(e.getMessage());
    }
  }

  @Test
  public void shouldNotProduceVCs_NON_ENUMERATED_JAVA_DOUBLE() throws JAXBException, IOException {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-2513777.xml");
    try {
      assertNonEnumeratedType(dataElement);
    } catch (UnsupportedDataElementException e) {
      logger.warn(e.getMessage());
    }
  }

  private void assertNonEnumeratedType(DataElement dataElement) throws UnsupportedDataElementException {
    ValueConstraintsHandler newHandler = handler.handle(dataElement);
    List<Map<String, Object>> ontologies = newHandler.getOntologies();
    List<Map<String, Object>> valueSets = newHandler.getValueSets();
    List<Map<String, Object>> branches = newHandler.getBranches();
    List<Map<String, Object>> classes = newHandler.getClasses();
    // Assert
    assertThat(ontologies.isEmpty(), is(true));
    assertThat(valueSets.isEmpty(), is(true));
    assertThat(branches.isEmpty(), is(true));
    assertThat(classes.isEmpty(), is(true));
  }
}
