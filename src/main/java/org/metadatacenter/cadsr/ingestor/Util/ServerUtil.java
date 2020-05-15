package org.metadatacenter.cadsr.ingestor.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.metadatacenter.cadsr.ingestor.Util.Constants.*;

public class ServerUtil {

  private static final Logger logger = LoggerFactory.getLogger(ServerUtil.class);

  public static CedarEnvironment toCedarEnvironment(String targetEnvironment) {
    if (targetEnvironment.compareToIgnoreCase(CedarEnvironment.LOCAL.name()) == 0) {
      return CedarEnvironment.LOCAL;
    }
    else if (targetEnvironment.compareToIgnoreCase(CedarEnvironment.STAGING.name()) == 0) {
      return CedarEnvironment.STAGING;
    }
    else if (targetEnvironment.compareToIgnoreCase(CedarEnvironment.PRODUCTION.name()) == 0) {
      return CedarEnvironment.PRODUCTION;
    }
    else {
      throw new IllegalArgumentException("Invalid target environment: " +
          targetEnvironment + ". Allowed values: local, staging, production");
    }
  }

  public static String getResourceServerUrl(CedarEnvironment targetServer) {
    if (Constants.CedarEnvironment.LOCAL.equals(targetServer)) {
      return LOCAL_RESOURCE_SERVER_URL;
    } else if (Constants.CedarEnvironment.STAGING.equals(targetServer)) {
      return STAGING_RESOURCE_SERVER_URL;
    } else if (Constants.CedarEnvironment.PRODUCTION.equals(targetServer)) {
      return PRODUCTION_RESOURCE_SERVER_URL;
    }
    throw new RuntimeException("Invalid target environment");
  }

  public static String getRepoServerUrl(CedarEnvironment targetServer) {
    if (Constants.CedarEnvironment.LOCAL.equals(targetServer)) {
      return LOCAL_REPO_SERVER_URL;
    } else if (Constants.CedarEnvironment.STAGING.equals(targetServer)) {
      return STAGING_REPO_SERVER_URL;
    } else if (Constants.CedarEnvironment.PRODUCTION.equals(targetServer)) {
      return PRODUCTION_REPO_SERVER_URL;
    }
    throw new RuntimeException("Invalid target environment");
  }

  public static String getCategoriesRestEndpoint(CedarEnvironment targetEnvironment) {
    String serverUrl = getResourceServerUrl(targetEnvironment);
    return serverUrl + "/categories";
  }

  public static String getRootCategoryRestEndpoint(CedarEnvironment targetEnvironment) {
    String serverUrl = getResourceServerUrl(targetEnvironment);
    return serverUrl + "/categories/root";
  }

  public static String getCategoryTreeEndpoint(CedarEnvironment targetEnvironment) {
    String serverUrl = getResourceServerUrl(targetEnvironment);
    return serverUrl + "/categories/tree";
  }

  public static String getAttachCategoryEndpoint(CedarEnvironment targetEnvironment) {
    String serverUrl = getResourceServerUrl(targetEnvironment);
    return serverUrl + "/command/attach-category";
  }

  public static String getAttachCategoriesEndpoint(CedarEnvironment targetEnvironment) {
    String serverUrl = getResourceServerUrl(targetEnvironment);
    return serverUrl + "/command/attach-categories";
  }

}
