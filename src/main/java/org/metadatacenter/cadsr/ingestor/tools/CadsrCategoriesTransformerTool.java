package org.metadatacenter.cadsr.ingestor.tools;

import com.google.common.base.Stopwatch;
import org.metadatacenter.cadsr.ingestor.Util.GeneralUtil;
import org.metadatacenter.cadsr.ingestor.Util.CategoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * This class reads a caDSR XML file with a <Classifications> root element and generates a JSON file with a tree of
 * categories.
 */
public class CadsrCategoriesTransformerTool {

  private static final Logger logger = LoggerFactory.getLogger(CadsrCategoriesTransformerTool.class);

  public static void main(String[] args) {

    String inputSourceLocation = args[0];
    String outputTargetLocation = args[1];

    final Stopwatch stopwatch = Stopwatch.createStarted();

    int totalCategories = 0;
    boolean success = false;
    try {
      File inputSource = new File(inputSourceLocation);
      File outputDir = GeneralUtil.checkDirectoryExists(outputTargetLocation);
      if (inputSource.isDirectory()) {
        CategoryUtil.convertCdeCategoriesFromDirectory(inputSource, outputDir);
      } else {
        CategoryUtil.convertCdeCategoriesFromFile(inputSource, outputDir);
      }
      success = true;
    } catch (Exception e) {
      logger.error(GeneralUtil.getStackTrace(e));
      success = false;
    } finally {
      printSummary(stopwatch, totalCategories, success);
    }
  }

  private static void printSummary(Stopwatch stopwatch, int totalCdes, boolean success) {
    final DecimalFormat countFormat = new DecimalFormat("#,###,###,###");
    logger.info("----------------------------------------------------------");
    if (success) {
      logger.info("TRANSFORM-CATEGORIES SUCCESS");
    } else {
      logger.info("TRANSFORM-CATEGORIES FAILED (see error.log for details)");
    }
    logger.info("----------------------------------------------------------");
    logger.info("Total number of generated categories: " + countFormat.format(totalCdes));
    long elapsedTimeInSeconds = stopwatch.elapsed(TimeUnit.SECONDS);
    long hours = elapsedTimeInSeconds / 3600;
    long minutes = (elapsedTimeInSeconds % 3600) / 60;
    long seconds = (elapsedTimeInSeconds % 60);
    logger.info("Total time: " + String.format("%02d:%02d:%02d", hours, minutes, seconds));
    logger.info("Finished at: " + LocalDateTime.now());
    logger.info("----------------------------------------------------------");
  }

}

