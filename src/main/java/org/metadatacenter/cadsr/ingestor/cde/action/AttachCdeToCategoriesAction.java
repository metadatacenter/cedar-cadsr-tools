package org.metadatacenter.cadsr.ingestor.cde.action;

import org.metadatacenter.cadsr.ingestor.util.CedarServices;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AttachCdeToCategoriesAction implements CdeAction {

  private static final Logger logger = LoggerFactory.getLogger(AttachCdeToCategoriesAction.class);

  private String cedarCdeId;
  private List<String> cedarCategoryIds;

  public AttachCdeToCategoriesAction(String cedarCdeId, List<String> cedarCategoryIds) {
    this.cedarCdeId = cedarCdeId;
    this.cedarCategoryIds = cedarCategoryIds;
  }

  public String getCedarCdeId() {
    return cedarCdeId;
  }

  public List<String> getCedarCategoryIds() {
    return cedarCategoryIds;
  }

  @Override
  public String execute(CedarServer cedarEnvironment, String apiKey) {
    logger.info("Attaching CDE to categories: " + cedarCdeId);
    CedarServices.attachCdeToCategories(cedarCdeId, cedarCategoryIds, false, cedarEnvironment, apiKey);
    return cedarCdeId;
  }
}
