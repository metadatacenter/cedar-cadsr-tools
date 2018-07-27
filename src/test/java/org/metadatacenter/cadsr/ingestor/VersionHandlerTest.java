package org.metadatacenter.cadsr.ingestor;

import org.junit.Before;
import org.junit.Test;
import org.metadatacenter.cadsr.DataElement;
import org.metadatacenter.cadsr.ingestor.exception.UnsupportedDataElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class VersionHandlerTest {

  private static final Logger logger = LoggerFactory.getLogger(VersionHandlerTest.class);

  private VersionHandler handler;

  @Before
  public void init() {
    handler = new VersionHandler();
  }

  @Test
  public void shouldProduceVersioning_RELEASED() throws JAXBException, IOException {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-2001826.xml");
    try {
      VersionHandler newHandler = handler.handle(dataElement);
      String status = newHandler.getStatus();
      String version = newHandler.getVersion();
      // Assert
      assertThat(status, is("bibo:published"));
      assertThat(version, is("3"));
    } catch (UnsupportedDataElementException e) {
      logger.warn(e.getMessage());
    }
  }

  @Test(expected = UnsupportedDataElementException.class)
  public void shouldNotProduceVersioning_APPRVD_FOR_TRIAL_USE() throws Exception {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-2626966.xml");
    try {
      VersionHandler newHandler = handler.handle(dataElement);
    } catch (UnsupportedDataElementException e) {
      String message = e.getMessage();
      assertThat(message, is("Failed to convert 'Fluorescence in situ Hybridization Cytogenetic Analysis Cell " +
          "Assessment Percentage Value' (ID: 2626966) - Reason: The data element status of 'APPRVD FOR TRIAL USE' is " +
          "not supported (Unsupported)"));
      throw e;
    }
  }

  @Test(expected = UnsupportedDataElementException.class)
  public void shouldNotProduceVersioning_DRAFT_MOD() throws Exception {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-2953241.xml");
    try {
      VersionHandler newHandler = handler.handle(dataElement);
    } catch (UnsupportedDataElementException e) {
      String message = e.getMessage();
      assertThat(message, is("Failed to convert 'Hematopoietic Cell Transplantation Recipient Research Data Shared " +
          "Consent Date' (ID: 2953241) - Reason: The data element status of 'DRAFT MOD' is not supported " +
          "(Unsupported)"));
      throw e;
    }
  }

  @Test(expected = UnsupportedDataElementException.class)
  public void shouldNotProduceVersioning_DRAFT_NEW() throws Exception {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-5286461.xml");
    try {
      VersionHandler newHandler = handler.handle(dataElement);
    } catch (UnsupportedDataElementException e) {
      String message = e.getMessage();
      assertThat(message, is("Failed to convert 'Medical Research Council Breathlessness Score Have to Stop for " +
          "Breath After Walking About 100 Yards on the Level Indicator' (ID: 5286461) - Reason: The data element " +
          "status of 'DRAFT NEW' is not supported (Unsupported)"));
      throw e;
    }
  }
}
