package org.metadatacenter.cadsr.ingestor.tools;

import com.google.common.base.Stopwatch;
import org.metadatacenter.cadsr.ingestor.Util.Constants.CedarEnvironment;
import org.metadatacenter.cadsr.ingestor.Util.ServerUtil;
import org.metadatacenter.cadsr.ingestor.Util.CadsrCategoriesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class CadsrCategoriesUploaderTool {

  private static final Logger logger = LoggerFactory.getLogger(CadsrCategoriesUploaderTool.class);

  public static void main(String[] args) {

    String inputSourceLocation = args[0];
    String cedarRootCategoryId = args[1];
    CedarEnvironment targetEnvironment = ServerUtil.toCedarEnvironment(args[2]);
    String apiKey = args[3];

    final Stopwatch stopwatch = Stopwatch.createStarted();

    int totalCategories = 0;
    boolean success = false;
    try {
      File inputSource = new File(inputSourceLocation);

      String endpoint = ServerUtil.getCategoriesRestEndpoint(targetEnvironment);

      if (inputSource.isDirectory()) {
        totalCategories = CadsrCategoriesUtil.uploadCategoriesFromDirectory(inputSource, cedarRootCategoryId, endpoint, apiKey);
      } else {
        totalCategories = CadsrCategoriesUtil.uploadCategoriesFromFile(inputSource, cedarRootCategoryId, endpoint, apiKey);
      }
      success = true;
    } catch (Exception e) {
      logger.error(e.toString());
      success = false;
    } finally {
      printSummary(stopwatch, totalCategories, success);
    }
  }

  private static void printSummary(Stopwatch stopwatch, int totalCdes, boolean success) {
    logger.info("----------------------------------------------------------");
    if (success) {
      logger.info("UPLOAD-CATEGORIES SUCCESS");
    } else {
      logger.info("UPLOAD-CATEGORIES FAILED (see error.log for details)");
    }
    logger.info("----------------------------------------------------------");
    //logger.info("Total number categories uploaded: " + countFormat.format(totalCdes));
    long elapsedTimeInSeconds = stopwatch.elapsed(TimeUnit.SECONDS);
    long hours = elapsedTimeInSeconds / 3600;
    long minutes = (elapsedTimeInSeconds % 3600) / 60;
    long seconds = (elapsedTimeInSeconds % 60);
    logger.info("Total time: " + String.format("%02d:%02d:%02d", hours, minutes, seconds));
    logger.info("Finished at: " + LocalDateTime.now());
    logger.info("----------------------------------------------------------");
  }

}
