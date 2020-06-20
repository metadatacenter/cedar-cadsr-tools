package org.metadatacenter.cadsr.ingestor.tools.config;

import org.metadatacenter.cadsr.ingestor.util.Constants.CedarEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigSettings {

  private static final Logger logger = LoggerFactory.getLogger(ConfigSettings.class);

  private boolean updateCategories;
  private boolean deleteCategories;
  private boolean updateCdes;
  private String cedarCdeFolderShortId;
  private CedarEnvironment cedarEnvironment;
  private String cadsrAdminApikey;
  private String executionFolder;
  private String ftpHost;
  private String ftpUser;
  private String ftpPassword;
  private String ftpCategoriesFolder;
  private String ftpCdesFolder;
  private String categoriesFilePath;
  private String cdesFilePath;
  private String ontologyOutputFile;

  public ConfigSettings() {
  }

  public static Logger getLogger() {
    return logger;
  }

  public boolean getUpdateCategories() {
    return updateCategories;
  }

  public void setUpdateCategories(boolean updateCategories) {
    this.updateCategories = updateCategories;
  }

  public boolean getDeleteCategories() {
    return deleteCategories;
  }

  public void setDeleteCategories(boolean deleteCategories) {
    this.deleteCategories = deleteCategories;
  }

  public boolean getUpdateCdes() {
    return updateCdes;
  }

  public void setUpdateCdes(boolean updateCdes) {
    this.updateCdes = updateCdes;
  }

  public String getCedarCdeFolderShortId() {
    return cedarCdeFolderShortId;
  }

  public void setCedarCdeFolderShortId(String cedarCdeFolderShortId) {
    this.cedarCdeFolderShortId = cedarCdeFolderShortId;
  }

  public CedarEnvironment getCedarEnvironment() {
    return cedarEnvironment;
  }

  public void setCedarEnvironment(CedarEnvironment cedarEnvironment) {
    this.cedarEnvironment = cedarEnvironment;
  }

  public String getCadsrAdminApikey() {
    return cadsrAdminApikey;
  }

  public void setCadsrAdminApikey(String cadsrAdminApikey) {
    this.cadsrAdminApikey = cadsrAdminApikey;
  }

  public String getExecutionFolder() {
    return executionFolder;
  }

  public void setExecutionFolder(String executionFolder) {
    this.executionFolder = executionFolder;
  }

  public String getFtpHost() {
    return ftpHost;
  }

  public void setFtpHost(String ftpHost) {
    this.ftpHost = ftpHost;
  }

  public String getFtpUser() {
    return ftpUser;
  }

  public void setFtpUser(String ftpUser) {
    this.ftpUser = ftpUser;
  }

  public String getFtpPassword() {
    return ftpPassword;
  }

  public void setFtpPassword(String ftpPassword) {
    this.ftpPassword = ftpPassword;
  }

  public String getFtpCategoriesFolder() {
    return ftpCategoriesFolder;
  }

  public void setFtpCategoriesFolder(String ftpCategoriesFolder) {
    this.ftpCategoriesFolder = ftpCategoriesFolder;
  }

  public String getFtpCdesFolder() {
    return ftpCdesFolder;
  }

  public void setFtpCdesFolder(String ftpCdesFolder) {
    this.ftpCdesFolder = ftpCdesFolder;
  }

  public String getCategoriesFilePath() {
    return categoriesFilePath;
  }

  public void setCategoriesFilePath(String categoriesFilePath) {
    this.categoriesFilePath = categoriesFilePath;
  }

  public String getCdesFilePath() {
    return cdesFilePath;
  }

  public void setCdesFilePath(String cdesFilePath) {
    this.cdesFilePath = cdesFilePath;
  }

  public String getOntologyOutputFile() {
    return ontologyOutputFile;
  }

  public void setOntologyOutputFile(String ontologyOutputFile) {
    this.ontologyOutputFile = ontologyOutputFile;
  }
}
