package org.metadatacenter.cadsr.ingestor.category.action;

import org.metadatacenter.cadsr.ingestor.util.Constants.CedarEnvironment;

import java.io.IOException;

/* Based on the Command design pattern */
public interface CategoryAction {
  public void execute(CedarEnvironment environment, String apiKey) throws IOException;
}
