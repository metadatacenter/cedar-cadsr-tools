package org.metadatacenter.cadsr.ingestor.tools.cde.config;

import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;
import org.metadatacenter.cadsr.ingestor.util.Constants;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarServer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The updater runs unattended against a CEDAR deployment, so its arguments are checked before it touches anything:
 * where to write, what to update, and where the categories and CDEs come from, either local archives or NCI's FTP
 * server with all three of its credentials.
 */
class ConfigSettingsParserTest {

  private static final String[] REQUIRED = {"-k", "key", "-f", "https://repo.metadatacenter.orgx/folders/f", "-s", "local"};

  @Test
  void localArchivesNeedNothingMore() throws Exception {
    ConfigSettings settings = ConfigSettingsParser.parseOrRefuse(with("-t", "-G", "categories.zip", "-c", "-E",
        "cdes.zip", "-x", "exec", "-o", "onto"));

    assertTrue(settings.getUpdateCategories());
    assertTrue(settings.getUpdateCdes());
    assertFalse(settings.getDeleteCategories());
    assertEquals("key", settings.getCadsrAdminApikey());
    assertEquals("https://repo.metadatacenter.orgx/folders/f", settings.getCedarCdeFolderId());
    assertEquals(CedarServer.LOCAL, settings.getCedarServer());
    assertEquals("categories.zip", settings.getCategoriesFilePath());
    assertEquals("cdes.zip", settings.getCdesFilePath());
    assertEquals("exec", settings.getExecutionFolder());
    assertEquals("onto", settings.getOntologyOutputFolderPath());
    assertNull(settings.getFtpHost());
  }

  @Test
  void theFoldersDefaultWhenNotGiven() throws Exception {
    ConfigSettings settings = ConfigSettingsParser.parseOrRefuse(with("-d"));
    assertTrue(settings.getDeleteCategories());
    assertFalse(settings.getUpdateCategories());
    assertFalse(settings.getUpdateCdes());
    assertEquals(Constants.EXECUTION_FOLDER, settings.getExecutionFolder());
    assertEquals(Constants.ONTOLOGY_FOLDER, settings.getOntologyOutputFolderPath());
  }

  @Test
  void theFtpServerNeedsHostUserAndPassword() throws Exception {
    ConfigSettings settings = ConfigSettingsParser.parseOrRefuse(with("-t", "-g", "cats", "-c", "-e", "cdes",
        "-h", "ftp.example.org", "-u", "user", "-p", "password"));
    assertEquals("cats", settings.getFtpCategoriesFolder());
    assertEquals("cdes", settings.getFtpCdesFolder());
    assertEquals("ftp.example.org", settings.getFtpHost());
    assertEquals("user", settings.getFtpUser());
    assertEquals("password", settings.getFtpPassword());

    assertThrows(ParseException.class, () -> ConfigSettingsParser.parseOrRefuse(with("-t", "-g", "cats",
        "-h", "ftp.example.org", "-u", "user")), "a missing password is refused");
  }

  @Test
  void anUnrunnableListIsRefused() {
    assertThrows(ParseException.class, () -> ConfigSettingsParser.parseOrRefuse(new String[]{"-k", "key"}),
        "the folder and the server are required");
    assertThrows(ParseException.class, () -> ConfigSettingsParser.parseOrRefuse(with("-t")),
        "updating categories needs a source of them");
    assertThrows(ParseException.class, () -> ConfigSettingsParser.parseOrRefuse(with("-c")),
        "updating CDEs needs a source of them");
    assertThrows(ParseException.class, () -> ConfigSettingsParser.parseOrRefuse(new String[]{"-k", "key", "-f",
        "f", "-s", "test"}), "an unknown server is refused");
    assertThrows(ParseException.class, () -> ConfigSettingsParser.parseOrRefuse(with("-t", "-g", "cats", "-G",
        "categories.zip")), "the FTP folder and the local archive are alternatives");
  }

  private static String[] with(String... more) {
    String[] args = new String[REQUIRED.length + more.length];
    System.arraycopy(REQUIRED, 0, args, 0, REQUIRED.length);
    System.arraycopy(more, 0, args, REQUIRED.length, more.length);
    return args;
  }
}
