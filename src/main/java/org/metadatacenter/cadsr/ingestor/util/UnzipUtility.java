package org.metadatacenter.cadsr.ingestor.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This utility extracts files and directories of a standard zip file to
 * a destination directory.
 * @author www.codejava.net. Modified by the CEDAR team.
 *
 */
public class UnzipUtility {
  /**
   * Size of the buffer to read/write data
   */
  private static final int BUFFER_SIZE = 4096;
  /**
   * Extracts a zip file specified by the zipFilePath to a directory specified by
   * destDirectory (will be created if does not exists). An entry whose name would place it outside that directory
   * is refused, and nothing after it is extracted.
   * @param zipFilePath
   * @param destDirectory
   * @throws IOException
   */
  public static void unzip(String zipFilePath, String destDirectory) throws IOException {
    File destDir = new File(destDirectory);
    if (!destDir.exists()) {
      destDir.mkdirs();
    }
    Path destPath = destDir.toPath().toAbsolutePath().normalize();
    try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
      ZipEntry entry = zipIn.getNextEntry();
      // iterates over entries in the zip file
      while (entry != null) {
        Path entryPath = destPath.resolve(entry.getName()).normalize();
        if (!entryPath.startsWith(destPath)) {
          throw new IOException("Zip entry is outside the destination directory: " + entry.getName());
        }
        if (!entry.isDirectory()) {
          // if the entry is a file, extracts it, creating any directory the archive does not list
          entryPath.getParent().toFile().mkdirs();
          extractFile(zipIn, entryPath.toString());
        } else {
          // if the entry is a directory, make the directory
          entryPath.toFile().mkdirs();
        }
        zipIn.closeEntry();
        entry = zipIn.getNextEntry();
      }
    }
  }

  /**
   * Extracts a zip entry (file entry)
   * @param zipIn
   * @param filePath
   * @throws IOException
   */
  private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
      byte[] bytesIn = new byte[BUFFER_SIZE];
      int read = 0;
      while ((read = zipIn.read(bytesIn)) != -1) {
        bos.write(bytesIn, 0, read);
      }
    }
  }
}
