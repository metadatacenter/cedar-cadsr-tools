package org.metadatacenter.cadsr.ingestor.tools;

import com.google.common.base.Stopwatch;
import org.metadatacenter.cadsr.ingestor.util.CategoryUtil;
import org.metadatacenter.cadsr.ingestor.util.CdeUploadUtil;
import org.metadatacenter.cadsr.ingestor.util.CedarServerUtil;
import org.metadatacenter.cadsr.ingestor.util.CedarServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.metadatacenter.cadsr.ingestor.util.Constants.ATTACH_CATEGORIES_OPTION;
import static org.metadatacenter.cadsr.ingestor.util.Constants.CedarEnvironment;

public class CadsrUploaderTool {

  private static final Logger logger = LoggerFactory.getLogger(CadsrUploaderTool.class);

  private static final DecimalFormat countFormat = new DecimalFormat("#,###,###,###");

  public static void main(String[] args) {

    String inputSourceLocation = args[0];
    CedarEnvironment targetEnvironment = CedarServerUtil.toCedarEnvironment(args[1]);
    String folderId = args[2];
    String apiKey = args[3];
    boolean attachCategories = false;
    if ((args.length > 4) && (args[4] != null) && args[4].equals(ATTACH_CATEGORIES_OPTION)) {
      attachCategories = true;
    }

    // Read the categoryIds from CEDAR to be able to link CDEs to them
    Map<String, String> categoryIdsToCedarCategoryIds = null;
    if (attachCategories) {
      try {
        categoryIdsToCedarCategoryIds =
            CedarServices.getCedarCategoryIds(targetEnvironment, apiKey);
      } catch (IOException e) {
        logger.error(e.getMessage());
      }
    }

    final Stopwatch stopwatch = Stopwatch.createStarted();

    int totalCdes = 0;
    boolean success = false;
    try {
      File inputSource = new File(inputSourceLocation);
      if (inputSource.isDirectory()) {
        CdeUploadUtil.uploadCdeFromDirectory(inputSource, folderId, attachCategories, categoryIdsToCedarCategoryIds, targetEnvironment, apiKey);
      } else {
        CdeUploadUtil.uploadCdeFromFile(inputSource, folderId, attachCategories, categoryIdsToCedarCategoryIds, targetEnvironment, apiKey);
      }
      success = true;
    } catch (Exception e) {
      logger.error(e.toString());
      success = false;
    } finally {
      CdeUploadUtil.storeOntologyInTempDir();
      printSummary(stopwatch, totalCdes, success);
    }
  }

  private static void printSummary(Stopwatch stopwatch, int totalCdes, boolean success) {
    logger.info("----------------------------------------------------------");
    if (success) {
      logger.info("UPLOAD SUCCESS");
    } else {
      logger.info("UPLOAD FAILED (see error.log for details)");
    }
    logger.info("----------------------------------------------------------");
    logger.info("Total number of generated CDEs: " + countFormat.format(totalCdes));
    long elapsedTimeInSeconds = stopwatch.elapsed(TimeUnit.SECONDS);
    long hours = elapsedTimeInSeconds / 3600;
    long minutes = (elapsedTimeInSeconds % 3600) / 60;
    long seconds = (elapsedTimeInSeconds % 60);
    logger.info("Total time: " + String.format("%02d:%02d:%02d", hours, minutes, seconds));
    logger.info("Finished at: " + LocalDateTime.now());
    logger.info("----------------------------------------------------------");
  }
}
