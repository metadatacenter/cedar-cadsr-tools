package org.metadatacenter.cadsr.ingestor.cde.action;

import org.metadatacenter.cadsr.ingestor.util.CdeUtil;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DeleteCdeAction implements CdeAction {

  private static final Logger logger = LoggerFactory.getLogger(DeleteCdeAction.class);

  public DeleteCdeAction() { }

  @Override
  public String execute(CedarEnvironment cedarEnvironment, String apiKey) throws IOException {
    // TODO: Confirm with Denise that it's fine to delete CDEs before implementing this method
    return null;
  }
}
