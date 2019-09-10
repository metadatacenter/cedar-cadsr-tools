package org.metadatacenter.cadsr.ingestor.cde;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.google.common.io.Files;
import org.metadatacenter.cadsr.ingestor.Constants;
import org.metadatacenter.cadsr.ingestor.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.metadatacenter.cadsr.ingestor.Constants.CDE_VALUESETS_ONTOLOGY_NAME;

public class CadsrTransformerTool {

  private static final Logger logger = LoggerFactory.getLogger(CadsrTransformerTool.class);
  private static final DecimalFormat countFormat = new DecimalFormat("#,###,###,###");
  private static final ObjectMapper mapper = new ObjectMapper();

  public static void main(String[] args) {

    String inputSourceLocation = args[0];
    String outputTargetLocation = args[1];

    final Stopwatch stopwatch = Stopwatch.createStarted();

    int totalCdes = 0;
    boolean success = false;
    try {
      File inputSource = new File(inputSourceLocation);
      File outputDir = Util.checkDirectoryExists(outputTargetLocation);
      if (inputSource.isDirectory()) {
        totalCdes = convertCdeFromDirectory(inputSource, outputDir);
      } else {
        totalCdes = convertCdeFromFile(inputSource, outputDir);
      }
      storeOntology(outputDir);
      success = true;
    } catch (Exception e) {
      logger.error(e.toString());
      success = false;
    } finally {
      printSummary(stopwatch, totalCdes, success);
    }
  }

  private static int convertCdeFromDirectory(File inputDir, File outputDir) throws IOException {
    int totalCdes = 0;
    for (final File inputFile : inputDir.listFiles()) {
      totalCdes += convertCdeFromFile(inputFile, outputDir);
    }
    return totalCdes;
  }

  public static int convertCdeFromFile(File inputFile, File outputDir) throws IOException {
    logger.info("Processing input file at " + inputFile.getAbsolutePath());
    File outputSubDir = Util.createDirectoryBasedOnInputFileName(inputFile, outputDir);
    Collection<Map<String, Object>> fieldMaps = CadsrUtils.getFieldMapsFromInputStream(new FileInputStream(inputFile));
    int totalFields = fieldMaps.size();
    if (totalFields > 0) {
      int counter = 0;
      for (Map<String, Object> fieldMap : fieldMaps) {
        try {
          // Save categories to a different folder
          String uuid = UUID.randomUUID().toString();
          if (fieldMap.containsKey(Constants.CDE_CATEGORY_IDS_FIELD)) {
            List<String> categoryIds = (List) fieldMap.get(Constants.CDE_CATEGORY_IDS_FIELD);
            if (categoryIds.size() > 0) {
              fieldMap.remove(Constants.CDE_CATEGORY_IDS_FIELD);
              String categoryIdsJson = mapper.writeValueAsString(categoryIds);
              String outputCategoriesLocation = outputSubDir.getAbsolutePath() + "/" + Constants.CATEGORIES_FOLDER_NAME;
              Util.checkDirectoryExists(outputCategoriesLocation);
              Files.write(categoryIdsJson.getBytes(), new File(outputCategoriesLocation, uuid + Constants.CATEGORIES_FILE_NAME_SUFFIX + ".json"));
            }
          }
          String fieldJson = mapper.writeValueAsString(fieldMap);
          Files.write(fieldJson.getBytes(), new File(outputSubDir, uuid + ".json"));
          if (Util.multiplesOfAHundred(counter)) {
            logger.info(String.format("Generating CDEs (%d/%d)", counter, totalFields));
          }
          counter++;
        } catch (JsonProcessingException e) {
          logger.error(e.toString());
        } catch (IOException e) {
          logger.error(e.toString());
        }
      }
      logger.info(String.format("Generating CDEs (%d/%d)", counter, totalFields));
    }
    return totalFields;
  }

  private static void storeOntology(File outputDir) {
    File outputOntologyFile = new File(outputDir, CDE_VALUESETS_ONTOLOGY_NAME);
    logger.info("Storing the generated value set ontology at " + outputOntologyFile);
    ValueSetsOntologyManager.saveOntology(outputOntologyFile);
  }

  private static void printSummary(Stopwatch stopwatch, int totalCdes, boolean success) {
    logger.info("----------------------------------------------------------");
    if (success) {
      logger.info("TRANSFORMATION SUCCESS");
    } else {
      logger.info("TRANSFORMATION FAILED (see error.log for details)");
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

