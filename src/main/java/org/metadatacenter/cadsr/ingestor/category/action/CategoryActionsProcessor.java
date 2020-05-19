package org.metadatacenter.cadsr.ingestor.category.action;

import org.metadatacenter.cadsr.ingestor.category.CategoryTreeNode;
import org.metadatacenter.cadsr.ingestor.util.CategoryUtil;
import org.metadatacenter.cadsr.ingestor.util.CedarServices;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.metadatacenter.cadsr.ingestor.util.Constants.ROOT_CATEGORY_KEY;

public class CategoryActionsProcessor {

  private static final Logger logger = LoggerFactory.getLogger(CategoryActionsProcessor.class);

  private List<CreateCategoryAction> createCategoryActions;
  private List<UpdateCategoryAction> updateCategoryActions;
  private List<DeleteCategoryAction> deleteCategoryActions;
  private CedarEnvironment cedarEnvironment;
  private String apiKey;

  public CategoryActionsProcessor(List<CreateCategoryAction> createCategoryActions,
                                  List<UpdateCategoryAction> updateCategoryActions,
                                  List<DeleteCategoryAction> deleteCategoryActions, CedarEnvironment cedarEnvironment
      , String apiKey) throws IOException {
    this.createCategoryActions = createCategoryActions;
    this.updateCategoryActions = updateCategoryActions;
    this.deleteCategoryActions = deleteCategoryActions;
    this.cedarEnvironment = cedarEnvironment;
    this.apiKey = apiKey;
    // Prepare actions for execution.
    //prepareUpdateActions();
    //prepareDeleteActions();
    prepareCreateActions();
  }

  public void executeCategoryActions() throws IOException {
    if (updateCategoryActions.size() > 0) {
      executeUpdateActions();
    }
    if (deleteCategoryActions.size() > 0) {
      executeDeleteActions();
    }
    if (createCategoryActions.size() > 0) {
      executeCreateActions();
    }
  }

  public void logActionsSummary() {
    logger.info("Processing category actions: ");
    logger.info(" - " + createCategoryActions.size() + " Create actions");
    logger.info(" - " + deleteCategoryActions.size() + " Delete actions");
    logger.info(" - " + updateCategoryActions.size() + " Update actions");
    if (createCategoryActions.size() > 0) {
      logger.info("Categories to Create:");
      for (CreateCategoryAction action : createCategoryActions) {
        logger.info(action.getCategory().toString());
      }
    }
    if (deleteCategoryActions.size() > 0) {
      logger.info("Categories to Delete:");
      for (DeleteCategoryAction action : deleteCategoryActions) {
        logger.info(action.getCategoryCedarId());
      }
    }
    if (updateCategoryActions.size() > 0) {
      logger.info("Categories to Update:");
      for (UpdateCategoryAction action : updateCategoryActions) {
        logger.info(action.getCategory().toString());
      }
    }
  }

  /*** Private methods to execute actions ***/

  private void executeCreateActions() throws IOException {
    logger.info("Executing category Create actions");
    for (CreateCategoryAction createCategoryAction : createCategoryActions) {
      createCategoryAction.execute(cedarEnvironment, apiKey);
    }
  }

  private void executeDeleteActions() throws IOException {
    logger.info("Executing category Delete actions");
    for (DeleteCategoryAction deleteCategoryAction : deleteCategoryActions) {
      deleteCategoryAction.execute(cedarEnvironment, apiKey);
    }
  }

  private void executeUpdateActions() throws IOException {
    logger.info("Executing category Update actions");
    for (UpdateCategoryAction updateCategoryAction : updateCategoryActions) {
      updateCategoryAction.execute(cedarEnvironment, apiKey);
    }
  }

  /*** Private methods to prepare actions for execution ***/

  private void prepareDeleteActions() {
  }

  private void prepareCreateActions() throws IOException {
    List<CreateCategoryAction> processedCreateCategoryActions = new ArrayList<>();
    List<CategoryTreeNode> nodesToBeCreated = new ArrayList<>();
    Map<String, String> uniqueIdToParentCedarIdMap = new HashMap<>(); // We'll needed later to retrieve the parent id
    for (CreateCategoryAction createAction : createCategoryActions) {
      nodesToBeCreated.add(createAction.getCategory());
      uniqueIdToParentCedarIdMap.put(createAction.getCategory().getUniqueId(), createAction.getParentCategoryCedarId());
    }
    List<CategoryTreeNode> connectedCategoryTreeNodes = connectCategoryTreeNodes(nodesToBeCreated);
    String rootCategoryId = CedarServices.getRootCategoryId(cedarEnvironment, apiKey);

    for (CategoryTreeNode categoryTreeNode : connectedCategoryTreeNodes) {
      // Set the root category id if needed
      if (categoryTreeNode.getParentUniqueId().equals(ROOT_CATEGORY_KEY)) {
        categoryTreeNode.setParentUniqueId(rootCategoryId);
      }
      processedCreateCategoryActions.add(new CreateCategoryAction(categoryTreeNode, uniqueIdToParentCedarIdMap.get(categoryTreeNode.getUniqueId())));
    }
    createCategoryActions = processedCreateCategoryActions;
  }


  //private void prepareUpdateActions() { }

  /**
   * Organize the CategoryTreeNodes as a tree in order to be able to create them in the right order. When we
   * translated independent categories to categoryTreeNodes, we didn't fill out the children field. Therefore, these
   * nodes are disconnected. We now connect them via the children attribute so that they can be created in the right
   * order.
   */
  private List<CategoryTreeNode> connectCategoryTreeNodes(List<CategoryTreeNode> allNodes) {
    List<CategoryTreeNode> connectedCategoryTreeNodes = new ArrayList<>();
    List<CategoryTreeNode> topLevelNodes = getTopLevelNodes(allNodes);

    for (CategoryTreeNode topLevelNode : topLevelNodes) {
      connectedCategoryTreeNodes.add(fillOutChildren(topLevelNode, allNodes));
    }
    return connectedCategoryTreeNodes;
  }

  private List<CategoryTreeNode> getTopLevelNodes(List<CategoryTreeNode> nodes) {
    List<CategoryTreeNode> topLevelNodes = new ArrayList<>();

    List<String> nodeUniqueIds = new ArrayList<>();
    for (CategoryTreeNode node : nodes) {
      nodeUniqueIds.add(node.getUniqueId());
    }

    for (CategoryTreeNode node : nodes) {
      // It's a top level node, does not have parents in the lists of given nodes
      if (!nodeUniqueIds.contains(node.getParentUniqueId())) {
        topLevelNodes.add(node);
      }
    }
    return topLevelNodes;
  }

  private CategoryTreeNode fillOutChildren(CategoryTreeNode node, List<CategoryTreeNode> nodes) {
    List<CategoryTreeNode> directChildren = getDirectChildren(node.getUniqueId(), nodes);
    for (CategoryTreeNode child : directChildren) {
      fillOutChildren(child, nodes);
    }
    node.setChildren(directChildren);
    return node;
  }

  private List<CategoryTreeNode> getDirectChildren(String nodeUniqueId, List<CategoryTreeNode> nodes) {
    List<CategoryTreeNode> children = new ArrayList<>();
    for (CategoryTreeNode node : nodes) {
      if (node.getParentUniqueId().equals(nodeUniqueId)) {
        children.add(node);
      }
    }
    return children;
  }
}
