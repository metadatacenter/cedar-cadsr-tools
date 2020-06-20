package org.metadatacenter.cadsr.ingestor.tools;

import com.google.common.base.Stopwatch;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarEnvironment;
import org.metadatacenter.cadsr.ingestor.util.CedarServerUtil;
import org.metadatacenter.cadsr.ingestor.util.CategoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class CadsrCategoriesUploaderTool_Deprecated {

  private static final Logger logger = LoggerFactory.getLogger(CadsrCategoriesUploaderTool_Deprecated.class);

  public static void main(String[] args) {

    String inputSourceLocation = args[0];
    String cedarRootCategoryId = args[1];
    CedarEnvironment targetEnvironment = CedarServerUtil.toCedarEnvironment(args[2]);
    String apiKey = args[3];

    final Stopwatch stopwatch = Stopwatch.createStarted();

    boolean success = false;
    try {
      File inputSource = new File(inputSourceLocation);
      if (inputSource.isDirectory()) {
        CategoryUtil.uploadCategoriesFromDirectory(inputSource, cedarRootCategoryId, targetEnvironment, apiKey);
      } else {
        CategoryUtil.uploadCategoriesFromFile(inputSource, cedarRootCategoryId, targetEnvironment, apiKey);
      }
      success = true;
    } catch (Exception e) {
      logger.error(e.toString());
      success = false;
    } finally {
      printSummary(stopwatch, success);
    }
  }

  private static void printSummary(Stopwatch stopwatch, boolean success) {
    logger.info("----------------------------------------------------------");
    if (success) {
      logger.info("UPLOAD-CATEGORIES SUCCESS");
    } else {
      logger.info("UPLOAD-CATEGORIES FAILED (see error.log for details)");
    }
    logger.info("----------------------------------------------------------");
    long elapsedTimeInSeconds = stopwatch.elapsed(TimeUnit.SECONDS);
    long hours = elapsedTimeInSeconds / 3600;
    long minutes = (elapsedTimeInSeconds % 3600) / 60;
    long seconds = (elapsedTimeInSeconds % 60);
    logger.info("Total time: " + String.format("%02d:%02d:%02d", hours, minutes, seconds));
    logger.info("Finished at: " + LocalDateTime.now());
    logger.info("----------------------------------------------------------");
  }

}
