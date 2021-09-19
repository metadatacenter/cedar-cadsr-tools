package org.metadatacenter.cadsr.ingestor.cde.action;

import org.metadatacenter.cadsr.ingestor.cde.CdeStats;
import org.metadatacenter.cadsr.ingestor.cde.CdeSummary;
import org.metadatacenter.cadsr.ingestor.util.CdeUtil;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarServer;
import org.metadatacenter.cadsr.ingestor.util.GeneralUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CdeActionsProcessor {

  private static final Logger logger = LoggerFactory.getLogger(CdeActionsProcessor.class);

  private List<CreateCdeAction> createCdeActions;
  private List<UpdateOrDeleteCdeAction> updateOrDeleteCdeActions;
  private Map<String, CdeSummary> cdesMap; // To store the final CDEs so that they can saved to a file
  private CedarServer cedarEnvironment;
  private String apiKey;

  public CdeActionsProcessor(List<CreateCdeAction> createCdeActions,
                             List<UpdateOrDeleteCdeAction> updateOrDeleteCdeActions,
                             Map<String, CdeSummary> cdesMap, CedarServer cedarEnvironment, String apiKey) {
    this.createCdeActions = createCdeActions;
    this.updateOrDeleteCdeActions = updateOrDeleteCdeActions;
    this.cdesMap = cdesMap;
    this.cedarEnvironment = cedarEnvironment;
    this.apiKey = apiKey;
    // Save stats
    CdeStats.getInstance().numberOfCdesToBeCreated = createCdeActions.size();
    CdeStats.getInstance().numberOfCdesToBeUpdatedOrDeleted = updateOrDeleteCdeActions.size();
  }

  public Map<String, CdeSummary> getCdesMap() {
    return cdesMap;
  }

  public void executeCdeActions() throws IOException {
    logger.info("Applying CDE actions: ");
    if (createCdeActions.size() > 0) {
      executeCreateActions();
    }
    logger.info("Applying CDE actions: ");
    if (createCdeActions.size() > 0) {
      executeUpdateOrDeleteActions();
    }
    logger.info("Finished applying CDE actions.");
  }

  public void logActionsSummary() {
    logger.info("CDE actions: ");
    logger.info(" - " + createCdeActions.size() + " Create actions");
  }

  /*** Private methods to execute actions ***/

  /**
   * Creates CDEs and attaches them to the corresponding categories.
   * <p>
   * Note that internally, this method makes calls to two different endpoints, one to create a CDE, and another one
   * to attach the CDE to its categories once it has been created. Calling those two endpoints sequentially for a
   * particular CDE will cause a performance decrease. The reason is that, by default, changes in Elasticsearch
   * indexes are visible after 1 second. If there is no 1 second between the call to the first endpoint and the call
   * to the second endpoint, the second endpoint, which updates the cde document in the index to add the categories
   * will have to wait until the cde is indexed. In order to avoid a substantial decrease in performance, we first
   * execute a batch of CDE creations, and then attach the categories to those CDEs. We split the full list of
   * creation actions in batches and process them one by one.
   */
  private void executeCreateActions() {
    logger.info("Executing CDE Create actions");
    final int BATCH_SIZE = 100;
    int progressCount = 0;
    List<List<CreateCdeAction>> createCdeActionBatches = GeneralUtil.getBatches(createCdeActions, BATCH_SIZE);
    for (List<CreateCdeAction> batch : createCdeActionBatches) {
      List<AttachCdeToCategoriesAction> attachCdeToCategoriesActions = new ArrayList<>();
      for (CreateCdeAction createCdeAction : batch) {
        logger.info("Progress: " + progressCount++ + "/" + createCdeActions.size());
        String createdCdeCedarId = createCdeAction.execute(cedarEnvironment, apiKey);
        String createdCdeUniqueId = CdeUtil.generateCdeUniqueId(createCdeAction.getCdeFieldMap());
        CdeSummary cdeSummary = new CdeSummary(createdCdeCedarId, null, null, createCdeAction.getHashCode(),
            new ArrayList<>());
        cdesMap.put(createdCdeUniqueId, cdeSummary);
        if (createdCdeCedarId != null && createCdeAction.getCedarCategoryIds().size() > 0) {
          attachCdeToCategoriesActions.add(new AttachCdeToCategoriesAction(createdCdeCedarId,
              createCdeAction.getCedarCategoryIds()));
        }
      }
      logger.info("Executing CDE AttachToCategories actions");
      for (AttachCdeToCategoriesAction attachAction : attachCdeToCategoriesActions) {
        attachAction.execute(cedarEnvironment, apiKey);
      }
    }
  }

  private void executeUpdateOrDeleteActions() {
    logger.info("Executing CDE Update/Delete actions");
    for (UpdateOrDeleteCdeAction updateOrDeleteCdeAction : updateOrDeleteCdeActions) {
      updateOrDeleteCdeAction.execute(cedarEnvironment, apiKey);
    }
  }
}
