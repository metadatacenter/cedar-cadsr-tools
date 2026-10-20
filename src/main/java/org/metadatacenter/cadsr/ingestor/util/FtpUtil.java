package org.metadatacenter.cadsr.ingestor.util;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FtpUtil {

  private static final Logger logger = LoggerFactory.getLogger(FtpUtil.class);

  /**
   * Downloads the most recently modified file in a folder of an FTP server. The server is a host name, optionally
   * followed by a colon and a port. A refused login or a missing folder is refused rather than answered with the
   * newest file of another folder, and subfolders are not candidates.
   */
  public static File downloadMostRecentFile(String server, String user, String password,
                                            String workingDirectory, String destinationFolder) throws IOException {

    String host = server;
    int port = FTP.DEFAULT_PORT;
    int colon = server.lastIndexOf(':');
    if (colon > 0 && server.indexOf(':') == colon && server.substring(colon + 1).matches("\\d+")) {
      host = server.substring(0, colon);
      port = Integer.parseInt(server.substring(colon + 1));
    }

    FTPClient ftpClient = new FTPClient();
    try {

      ftpClient.connect(host, port);
      if (!ftpClient.login(user, password)) {
        throw new IOException("The FTP server refused the login of " + user + ": " + ftpClient.getReplyString().trim());
      }
      ftpClient.enterLocalPassiveMode();
      ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

      if (!ftpClient.changeWorkingDirectory(workingDirectory)) {
        throw new IOException("The FTP server has no folder " + workingDirectory + ": "
            + ftpClient.getReplyString().trim());
      }

      List<FTPFile> filesList = new ArrayList<>(Arrays.stream(ftpClient.listFiles()).filter(FTPFile::isFile).toList());

      if (filesList.size() > 0) {
        filesList.sort(Comparator.comparing(FTPFile::getTimestamp).reversed());
        FTPFile mostRecentFile = filesList.get(0);

        // Create the path to the destination file if it does not exist
        File directory = new File(destinationFolder);
        if (!directory.exists()){
          directory.mkdirs();
        }

        File destinationFile = new File(destinationFolder + "/" + mostRecentFile.getName());
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(destinationFile));
        logger.info("Downloading file: " + mostRecentFile.getName() + " (size: " + mostRecentFile.getSize() / 1024 + " KB)");
        logger.info("Destination path: " + destinationFile.getAbsolutePath());
        CountingOutputStream cos = new CountingOutputStream(outputStream){
          double lastProgressShown = -1;
          protected void beforeWrite(int n){
            super.beforeWrite(n);
            double progressPercentage = Math.round(((double) getCount() * 100) / (double) mostRecentFile.getSize());
            if ( progressPercentage != lastProgressShown && progressPercentage % 10 == 0) {
              logger.info("Download progress: " + (int) progressPercentage + "%");
              lastProgressShown = progressPercentage;
            }
          }
        };
        boolean success = ftpClient.retrieveFile(mostRecentFile.getName(), cos);
        outputStream.close();

        if (success) {
          logger.info("The file has been downloaded successfully");
          return destinationFile;
        }
        else {
          throw new InternalError("Error downloading file: " + destinationFile.getAbsolutePath());
        }
      } else {
        throw new InternalError("The FTP folder is empty: " + workingDirectory);
      }
    } finally {
      try {
        if (ftpClient.isConnected()) {
          ftpClient.logout();
          ftpClient.disconnect();
        }
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }
}
