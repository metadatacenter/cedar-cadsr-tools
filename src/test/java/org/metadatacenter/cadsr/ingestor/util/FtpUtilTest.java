package org.metadatacenter.cadsr.ingestor.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NCI publishes a new archive of categories and of CDEs into a folder of its FTP server from time to time, and the
 * updater fetches the newest file in the folder it is given. It must fetch from that folder only, so a login or a
 * folder the server refuses stops it.
 */
@Tag("integration")
class FtpUtilTest {

  private StubFtp ftp;

  @BeforeEach
  void startFtp() throws IOException {
    ftp = new StubFtp("cadsr", "secret")
        .file("cdes", "cdes-2026-01.zip", bytes("older"), LocalDate.of(2026, 1, 5))
        .file("cdes", "cdes-2026-09.zip", bytes("newest"), LocalDate.of(2026, 9, 1))
        .directory("cdes", "archive", LocalDate.of(2026, 10, 1))
        .file("categories", "categories-2026-09.zip", bytes("categories"), LocalDate.of(2026, 9, 2))
        .directory("empty", "only-a-folder", LocalDate.of(2026, 9, 3));
  }

  @AfterEach
  void stopFtp() throws IOException {
    ftp.close();
  }

  @Test
  void theNewestFileInTheFolderIsFetched(@TempDir Path dir) throws Exception {
    Path destination = dir.resolve("downloads/cdes");

    File file = FtpUtil.downloadMostRecentFile(ftp.address(), "cadsr", "secret", "cdes", destination.toString());

    assertEquals("cdes-2026-09.zip", file.getName(), "a newer folder is not a candidate");
    assertEquals(destination.toFile(), file.getParentFile(), "the destination folder is created");
    assertEquals("newest", Files.readString(file.toPath()));
  }

  @Test
  void aRefusedLoginOrFolderStopsTheDownload(@TempDir Path dir) {
    IOException login = assertThrows(IOException.class,
        () -> FtpUtil.downloadMostRecentFile(ftp.address(), "cadsr", "wrong", "cdes", dir.toString()));
    assertTrue(login.getMessage().contains("530"), login.getMessage());
    IOException folder = assertThrows(IOException.class,
        () -> FtpUtil.downloadMostRecentFile(ftp.address(), "cadsr", "secret", "cde", dir.toString()));
    assertTrue(folder.getMessage().contains("cde"), folder.getMessage());
  }

  @Test
  void aFolderWithoutFilesIsRefused(@TempDir Path dir) {
    InternalError empty = assertThrows(InternalError.class,
        () -> FtpUtil.downloadMostRecentFile(ftp.address(), "cadsr", "secret", "empty", dir.toString()));
    assertTrue(empty.getMessage().contains("empty"), empty.getMessage());
  }

  private static byte[] bytes(String content) {
    return content.getBytes(StandardCharsets.UTF_8);
  }
}
