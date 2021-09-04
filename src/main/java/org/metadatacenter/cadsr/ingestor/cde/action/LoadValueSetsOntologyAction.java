package org.metadatacenter.cadsr.ingestor.cde.action;

import org.metadatacenter.cadsr.ingestor.util.CedarServices;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LoadValueSetsOntologyAction implements CdeAction {

  private static final Logger logger = LoggerFactory.getLogger(LoadValueSetsOntologyAction.class);

  private final static int MAX_STATUS_CHECK_ATTEMPTS = 10;
  private final static long STATUS_CHECK_INITIAL_SLEEP_TIME = 5000;
  private final static long STATUS_CHECK_SLEEP_TIME = 3000;

  public LoadValueSetsOntologyAction() {
  }

  public static Logger getLogger() {
    return logger;
  }

  @Override
  public String execute(CedarServer cedarEnvironment, String apiKey) {
    try {
      CedarServices.loadValueSetsOntology(cedarEnvironment, apiKey);

      Thread.sleep(STATUS_CHECK_INITIAL_SLEEP_TIME);

      String status = CedarServices.loadValueSetsOntologyStatus(cedarEnvironment, apiKey);

      for (int statusCheckNumber = 1; statusCheckNumber < MAX_STATUS_CHECK_ATTEMPTS; statusCheckNumber++) {
        if (status.equals("COMPLETE") || status.equals("ERROR")) break;

        Thread.sleep(STATUS_CHECK_SLEEP_TIME);

        status = CedarServices.loadValueSetsOntologyStatus(cedarEnvironment, apiKey);
      }

      if (status.equals("COMPLETE"))
        return "Done";
      else if (status.equals("ERROR"))
        return "Error";
      else if (status.equals("NOT_YET_INITIATED"))
        return "Failed to start";
      else if (status.equals("IN_PROGRESS"))
        return "Failed to finish";
      else
        return "Error - unknown status";

    } catch (IOException | InterruptedException e) {
      String message = "Error calling load value sets ontology endpoint: " + e.getMessage();
      logger.warn(message);
      return message;
    }
  }
}
