package org.metadatacenter.cadsr.ingestor.cde.action;

import org.metadatacenter.cadsr.ingestor.util.CedarServices;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LoadValueSetsOntologyAction implements CdeAction {

  private static final Logger logger = LoggerFactory.getLogger(LoadValueSetsOntologyAction.class);

  public LoadValueSetsOntologyAction() {
  }

  public static Logger getLogger() {
    return logger;
  }

  @Override
  public String execute(CedarServer cedarEnvironment, String apiKey) {
    try {
      CedarServices.loadValueSetsOntology(cedarEnvironment, apiKey);
      return "Done";
    } catch (IOException e) {
      String message = "Error calling load value sets ontology endpoint: " + e.getMessage();
      logger.warn(message);
      return message;
    }
  }
}
