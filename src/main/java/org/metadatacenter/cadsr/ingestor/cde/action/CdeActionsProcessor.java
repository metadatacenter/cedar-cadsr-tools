package org.metadatacenter.cadsr.ingestor.cde.action;

import org.metadatacenter.cadsr.ingestor.cde.CdeSummary;
import org.metadatacenter.cadsr.ingestor.util.CdeUtil;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CdeActionsProcessor {

  private static final Logger logger = LoggerFactory.getLogger(CdeActionsProcessor.class);

  private List<CreateCdeAction> createCdeActions;
  private List<UpdateCdeAction> updateCdeActions;
  private List<DeleteCdeAction> deleteCdeActions;
  private Map<String, CdeSummary> cdesMap; // To store the final CDEs so that they can saved to a file
  private CedarEnvironment cedarEnvironment;
  private String apiKey;

  public CdeActionsProcessor(List<CreateCdeAction> createCdeActions, List<UpdateCdeAction> updateCdeActions,
                             List<DeleteCdeAction> deleteCdeActions, Map<String, CdeSummary> cdesMap,
                             CedarEnvironment cedarEnvironment, String apiKey) {
    this.createCdeActions = createCdeActions;
    this.updateCdeActions = updateCdeActions;
    this.deleteCdeActions = deleteCdeActions;
    this.cdesMap = cdesMap;
    this.cedarEnvironment = cedarEnvironment;
    this.apiKey = apiKey;
  }

  public Map<String, CdeSummary> getCdesMap() {
    return cdesMap;
  }

  public void executeCdeActions() throws IOException {
    logger.info("Applying CDE actions: ");
    if (updateCdeActions.size() > 0) {
      executeUpdateActions();
    }
    if (deleteCdeActions.size() > 0) {
      executeDeleteActions();
    }
    if (createCdeActions.size() > 0) {
      executeCreateActions();
    }
    logger.info("Finished applying CDE actions.");
  }

  public void logActionsSummary() {
    logger.info("CDE actions: ");
    logger.info(" - " + createCdeActions.size() + " Create actions");
    logger.info(" - " + deleteCdeActions.size() + " Delete actions");
    logger.info(" - " + updateCdeActions.size() + " Update actions");
    if (createCdeActions.size() > 0) {
      logger.info("CDEs to Create:");
      for (CreateCdeAction action : createCdeActions) {
        logger.info(CdeUtil.generateCdeUniqueId(action.getCdeFieldMap()));
      }
    }
    if (deleteCdeActions.size() > 0) {
      logger.info("CDEs to Delete:");
      for (DeleteCdeAction action : deleteCdeActions) {
        //logger.info(action.getCategoryCedarId());
      }
    }
    if (updateCdeActions.size() > 0) {
      logger.info("CDEs to Update:");
      for (UpdateCdeAction action : updateCdeActions) {
        //logger.info(action.getCategory().toString());
      }
    }
  }

  /*** Private methods to execute actions ***/

  private void executeCreateActions() {
    logger.info("Executing CDE Create actions");
    for (CreateCdeAction createCdeAction : createCdeActions) {
      String createdCdeCedarId = createCdeAction.execute(cedarEnvironment, apiKey);
      String createdCdeUniqueId = CdeUtil.generateCdeUniqueId(createCdeAction.getCdeFieldMap());
      CdeSummary cdeSummary = new CdeSummary(createdCdeCedarId, null, null, createCdeAction.getHashCode(),
          new ArrayList<>());
      cdesMap.put(createdCdeUniqueId, cdeSummary);
    }
  }

  private void executeDeleteActions() throws IOException {
    logger.info("Executing CDE Delete actions");
    for (DeleteCdeAction deleteCdeAction : deleteCdeActions) {
      deleteCdeAction.execute(cedarEnvironment, apiKey);
    }
  }

  private void executeUpdateActions() {
    logger.info("Executing CDE Update actions");
    for (UpdateCdeAction updateCdeAction : updateCdeActions) {
      updateCdeAction.execute(cedarEnvironment, apiKey);
    }
  }

}
