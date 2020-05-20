package org.metadatacenter.cadsr.ingestor.cde.action;

import org.metadatacenter.cadsr.ingestor.util.Constants.CedarEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateCdeAction implements CdeAction {

  private static final Logger logger = LoggerFactory.getLogger(UpdateCdeAction.class);

  public UpdateCdeAction() { }

  @Override
  public String execute(CedarEnvironment cedarEnvironment, String apiKey) {
    // TODO: Confirm with Denise that it's fine to delete CDEs before implementing this method
    return null;
  }
}
