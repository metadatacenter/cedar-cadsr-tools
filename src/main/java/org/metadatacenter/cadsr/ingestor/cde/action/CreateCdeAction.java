package org.metadatacenter.cadsr.ingestor.cde.action;

import org.metadatacenter.cadsr.ingestor.util.CdeUtil;
import org.metadatacenter.cadsr.ingestor.util.CedarServices;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarEnvironment;
import org.metadatacenter.model.ModelNodeNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CreateCdeAction implements CdeAction {

  private static final Logger logger = LoggerFactory.getLogger(CreateCdeAction.class);

  private Map<String, Object> cdeFieldMap;
  private String hashCode;
  private String cedarFolderShortId;
  private List<String> cedarCategoryIds;
  private List<String> categoryIds;

  public CreateCdeAction(Map<String, Object> cdeFieldMap, String hashCode, String cedarFolderShortId,
                         List<String> cedarCategoryIds, List<String> categoryIds) {
    this.cdeFieldMap = cdeFieldMap;
    this.hashCode = hashCode;
    this.cedarFolderShortId = cedarFolderShortId;
    this.cedarCategoryIds = cedarCategoryIds;
    this.categoryIds = categoryIds;
  }

  public static Logger getLogger() {
    return logger;
  }

  public Map<String, Object> getCdeFieldMap() {
    return cdeFieldMap;
  }

  public String getHashCode() {
    return hashCode;
  }

  public String getCedarFolderShortId() {
    return cedarFolderShortId;
  }

  public List<String> getCedarCategoryIds() {
    return cedarCategoryIds;
  }

  public List<String> getCategoryIds() {
    return categoryIds;
  }

  @Override
  public String execute(CedarEnvironment cedarEnvironment, String apiKey) {
    logger.info("-----------------------------------------");
    logger.info("Creating CDE: " + CdeUtil.generateCdeUniqueId(cdeFieldMap) + " (" + cdeFieldMap.get(ModelNodeNames.SCHEMA_ORG_NAME).toString() + ")");
    Optional<List<String>> cedarCatIds = Optional.empty();
    Optional<List<String>> catIds = Optional.empty();
    if (cedarCategoryIds != null && cedarCategoryIds.size() > 0) {
      cedarCatIds = Optional.of(cedarCategoryIds);
    }
    if (categoryIds != null && categoryIds.size() > 0) {
      catIds = Optional.of(categoryIds);
    }
    return CedarServices.createCde(cdeFieldMap, hashCode, cedarFolderShortId, cedarCatIds, catIds, cedarEnvironment, apiKey);
  }
}
