package org.metadatacenter.cadsr.ingestor;

import org.junit.Before;
import org.junit.Test;
import org.metadatacenter.cadsr.DataElement;
import org.metadatacenter.model.ModelNodeNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class InputTypeHandlerTest {

  private static final Logger logger = LoggerFactory.getLogger(InputTypeHandlerTest.class);

  private InputTypeHandler handler;

  @Before
  public void init() {
    handler = new InputTypeHandler();
  }

  @Test
  public void shouldDefineDateField_DATE() throws JAXBException, IOException {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-2001826.xml");
    try {
      Map<String, Object> inputType = handler.handle(dataElement).getInputType();
      // Assert
      assertThat(inputType.get(ModelNodeNames.INPUT_TYPE).toString(), is(equalTo("date")));
    } catch (UnsupportedDataElementException e) {
      logger.warn(e.getMessage());
    }
  }

  @Test
  public void shouldDefineDateField_JAVA_DATE() throws JAXBException, IOException {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-2513819.xml");
    try {
      Map<String, Object> inputType = handler.handle(dataElement).getInputType();
      // Assert
      assertThat(inputType.get(ModelNodeNames.INPUT_TYPE).toString(), is(equalTo("date")));
    } catch (UnsupportedDataElementException e) {
      logger.warn(e.getMessage());
    }
  }

  @Test
  public void shouldDefineTextField_CHARACTER() throws JAXBException, IOException {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-2001831.xml");
    try {
      Map<String, Object> inputType = handler.handle(dataElement).getInputType();
      // Assert
      assertThat(inputType.get(ModelNodeNames.INPUT_TYPE).toString(), is(equalTo("textfield")));
    } catch (UnsupportedDataElementException e) {
      logger.warn(e.getMessage());
    }
  }

  @Test
  public void shouldDefineTextField_JAVA_STRING() throws JAXBException, IOException {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-2608127.xml");
    try {
      Map<String, Object> inputType = handler.handle(dataElement).getInputType();
      // Assert
      assertThat(inputType.get(ModelNodeNames.INPUT_TYPE).toString(), is(equalTo("textfield")));
    } catch (UnsupportedDataElementException e) {
      logger.warn(e.getMessage());
    }
  }

  @Test
  public void shouldDefineTextField_ALPHANUMERIC() throws JAXBException, IOException {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-3245384.xml");
    try {
      Map<String, Object> inputType = handler.handle(dataElement).getInputType();
      // Assert
      assertThat(inputType.get(ModelNodeNames.INPUT_TYPE).toString(), is(equalTo("textfield")));
    } catch (UnsupportedDataElementException e) {
      logger.warn(e.getMessage());
    }
  }

  @Test
  public void shouldDefineTextField_ISO21090CD() throws JAXBException, IOException {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-3177059.xml");
    try {
      Map<String, Object> inputType = handler.handle(dataElement).getInputType();
      // Assert
      assertThat(inputType.get(ModelNodeNames.INPUT_TYPE).toString(), is(equalTo("textfield")));
    } catch (UnsupportedDataElementException e) {
      logger.warn(e.getMessage());
    }
  }

  @Test
  public void shouldDefineNumericField_NUMBER() throws JAXBException, IOException {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-2002061.xml");
    try {
      Map<String, Object> inputType = handler.handle(dataElement).getInputType();
      // Assert
      assertThat(inputType.get(ModelNodeNames.INPUT_TYPE).toString(), is(equalTo("numeric")));
    } catch (UnsupportedDataElementException e) {
      logger.warn(e.getMessage());
    }
  }

  @Test
  public void shouldDefineNumericField_JAVA_LONG() throws JAXBException, IOException {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-2608393.xml");
    try {
      Map<String, Object> inputType = handler.handle(dataElement).getInputType();
      // Assert
      assertThat(inputType.get(ModelNodeNames.INPUT_TYPE).toString(), is(equalTo("numeric")));
    } catch (UnsupportedDataElementException e) {
      logger.warn(e.getMessage());
    }
  }

  @Test
  public void shouldDefineNumericField_JAVA_INTEGER() throws JAXBException, IOException {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-2513896.xml");
    try {
      Map<String, Object> inputType = handler.handle(dataElement).getInputType();
      // Assert
      assertThat(inputType.get(ModelNodeNames.INPUT_TYPE).toString(), is(equalTo("numeric")));
    } catch (UnsupportedDataElementException e) {
      logger.warn(e.getMessage());
    }
  }

  @Test
  public void shouldDefineNumericField_JAVA_DOUBLE() throws JAXBException, IOException {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-2513777.xml");
    try {
      Map<String, Object> inputType = handler.handle(dataElement).getInputType();
      // Assert
      assertThat(inputType.get(ModelNodeNames.INPUT_TYPE).toString(), is(equalTo("numeric")));
    } catch (UnsupportedDataElementException e) {
      logger.warn(e.getMessage());
    }
  }

  @Test
  public void shouldDefineTextArea_CHARACTER() throws JAXBException, IOException {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-2182451.xml");
    try {
      handler = handler.handle(dataElement);
      Map<String, Object> inputType = handler.getInputType();
      // Assert
      assertThat(inputType.get(ModelNodeNames.INPUT_TYPE).toString(), is(equalTo("textarea")));
    } catch (UnsupportedDataElementException e) {
      logger.warn(e.getMessage());
    }
  }
}
