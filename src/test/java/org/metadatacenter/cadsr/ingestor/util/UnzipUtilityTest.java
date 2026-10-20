package org.metadatacenter.cadsr.ingestor.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The updater unpacks the categories and CDEs archives NCI publishes, or that an operator names, into its execution
 * folder. Everything in an archive must land inside that folder.
 */
class UnzipUtilityTest {

  @Test
  void filesAndDirectoriesAreExtracted(@TempDir Path dir) throws Exception {
    Path zip = dir.resolve("cdes.zip");
    try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip.toFile()))) {
      out.putNextEntry(new ZipEntry("listed/"));
      out.closeEntry();
      add(out, "top.xml", "<top/>");
      add(out, "unlisted/nested.xml", "<nested/>");
    }
    Path target = dir.resolve("unzipped");

    UnzipUtility.unzip(zip.toString(), target.toString());

    assertTrue(Files.isDirectory(target.resolve("listed")));
    assertEquals("<top/>", Files.readString(target.resolve("top.xml")));
    assertEquals("<nested/>", Files.readString(target.resolve("unlisted/nested.xml")),
        "a file is extracted even when the archive does not list its directory");
  }

  @Test
  void anEntryThatClimbsOutOfTheTargetIsRefused(@TempDir Path dir) throws Exception {
    Path zip = dir.resolve("hostile.zip");
    try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip.toFile()))) {
      add(out, "../escaped.xml", "<escaped/>");
    }
    Path target = dir.resolve("unzipped");

    assertThrows(IOException.class, () -> UnzipUtility.unzip(zip.toString(), target.toString()));
    assertFalse(Files.exists(dir.resolve("escaped.xml")));
  }

  private static void add(ZipOutputStream out, String name, String content) throws IOException {
    out.putNextEntry(new ZipEntry(name));
    out.write(content.getBytes(StandardCharsets.UTF_8));
    out.closeEntry();
  }
}
