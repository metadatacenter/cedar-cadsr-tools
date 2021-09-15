package org.metadatacenter.cadsr.ingestor.cde.action;

import org.metadatacenter.cadsr.ingestor.cde.CdeStats;
import org.metadatacenter.cadsr.ingestor.cde.CdeSummary;
import org.metadatacenter.cadsr.ingestor.util.CdeUtil;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CdeActionsProcessor {

  private static final Logger logger = LoggerFactory.getLogger(CdeActionsProcessor.class);

  private List<CreateCdeAction> createCdeActions;
  private List<UpdateOrDeleteCdeAction> updateOrDeleteCdeActions;
  private Map<String, CdeSummary> cdesMap; // To store the final CDEs so that they can saved to a file
  private CedarServer cedarEnvironment;
  private String apiKey;
  private int count;

  public CdeActionsProcessor(List<CreateCdeAction> createCdeActions, List<UpdateOrDeleteCdeAction> updateOrDeleteCdeActions,
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

  private void executeCreateActions() {
    final int NUM_THREADS = 10;
    logger.info("Executing CDE Create actions");
    ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
    for (CreateCdeAction createCdeAction : createCdeActions) {
      Runnable worker = new ActionsRunnable(this, createCdeAction);
      executor.execute(worker);
    }
    executor.shutdown();
    try {
      executor.awaitTermination(1, TimeUnit.MINUTES);
      // all tasks have finished or the time has been reached.
    } catch (InterruptedException e) {
      logger.error("Error while waiting for all tasks to be finished", e);
    }
  }

  private void executeUpdateOrDeleteActions() {
    logger.info("Executing CDE Update/Delete actions");
    for (UpdateOrDeleteCdeAction updateOrDeleteCdeAction : updateOrDeleteCdeActions) {
      updateOrDeleteCdeAction.execute(cedarEnvironment, apiKey);
    }
  }

  /*** Runnable class to execute actions in multiple threads ***/

  public static class ActionsRunnable implements Runnable {
    private CdeActionsProcessor processor;
    private CreateCdeAction createCdeAction;

    ActionsRunnable(CdeActionsProcessor processor, CreateCdeAction createCdeAction) {
      this.processor = processor;
      this.createCdeAction = createCdeAction;
    }

    @Override
    public void run() {
      logger.info("Progress: " + processor.count++ + "/" + processor.createCdeActions.size());
      String createdCdeCedarId = createCdeAction.execute(processor.cedarEnvironment, processor.apiKey);
      String createdCdeUniqueId = CdeUtil.generateCdeUniqueId(createCdeAction.getCdeFieldMap());
      CdeSummary cdeSummary = new CdeSummary(createdCdeCedarId, null, null, createCdeAction.getHashCode(),
          new ArrayList<>());
      processor.cdesMap.put(createdCdeUniqueId, cdeSummary);
    }
  }
}
