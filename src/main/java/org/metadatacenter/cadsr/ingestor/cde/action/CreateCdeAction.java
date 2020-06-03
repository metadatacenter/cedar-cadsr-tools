package org.metadatacenter.cadsr.ingestor.cde.action;

import org.metadatacenter.cadsr.ingestor.util.CdeUtil;
import org.metadatacenter.cadsr.ingestor.util.CedarServices;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarEnvironment;
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

  public CreateCdeAction(Map<String, Object> cdeFieldMap, String hashCode, String cedarFolderShortId,
                         List<String> cedarCategoryIds) {
    this.cdeFieldMap = cdeFieldMap;
    this.hashCode = hashCode;
    this.cedarFolderShortId = cedarFolderShortId;
    this.cedarCategoryIds = cedarCategoryIds;
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

  @Override
  public String execute(CedarEnvironment cedarEnvironment, String apiKey) {
    logger.info("Creating CDE: " + CdeUtil.generateCdeUniqueId(cdeFieldMap));
    Optional<List<String>> cedarCatIds = Optional.empty();
    if (cedarCategoryIds != null && cedarCategoryIds.size() > 0) {
      cedarCatIds = Optional.of(cedarCategoryIds);
    }
    String createdCdeCedarId = CedarServices.createCde(cdeFieldMap, hashCode, cedarFolderShortId, cedarCatIds, cedarEnvironment, apiKey);
    logger.info("CDE created: " + CdeUtil.generateCdeUniqueId(cdeFieldMap) + "; CEDAR Id: " + createdCdeCedarId);
    return createdCdeCedarId;
  }
}
