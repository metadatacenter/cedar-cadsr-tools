package org.metadatacenter.cadsr.ingestor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.metadatacenter.cadsr.ingestor.Constants.CDE_VALUESETS_ONTOLOGY_NAME;

public class CadsrConverterTool {

  private static final Logger logger = LoggerFactory.getLogger(CadsrConverterTool.class);

  private static final DecimalFormat countFormat = new DecimalFormat("#,###,###,###");

  public static void main(String[] args) {

    String inputSourceLocation = args[0];
    String outputTargetLocation = args[1];

    final Stopwatch stopwatch = Stopwatch.createStarted();

    int totalCdes = 0;
    boolean success = false;
    try {
      File inputSource = new File(inputSourceLocation);
      File outputDir = checkOutputDirectoryExists(outputTargetLocation);
      if (inputSource.isDirectory()) {
        totalCdes = convertCdeFromDirectory(inputSource, outputDir);
      } else {
        totalCdes = convertCdeFromFile(inputSource, outputDir);
      }
      success = true;
    } catch (Exception e) {
      logger.error(e.toString());
      success = false;
    } finally {
      storeOntologyInTempDir();
      printSummary(stopwatch, totalCdes, success);
    }
  }

  public static File checkOutputDirectoryExists(String outputDirectory) throws IOException {
    File outputDir = new File(outputDirectory);
    if (!outputDir.exists()) {
      outputDir.mkdir();
    }
    return outputDir;
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
    File outputSubDir = createDirectoryBasedOnInputFileName(inputFile, outputDir);
    Collection<Map<String, Object>> fieldMaps = CadsrUtils.getFieldMapsFromInputStream(new FileInputStream(inputFile));
    int totalFields = fieldMaps.size();
    if (totalFields > 0) {
      int counter = 0;
      for (Map<String, Object> fieldMap : fieldMaps) {
        try {
          String fieldJson = new ObjectMapper().writeValueAsString(fieldMap);
          Files.write(fieldJson.getBytes(), new File(outputSubDir, UUID.randomUUID() + ".json"));
          if (multiplesOfAHundred(counter)) {
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

  private static File createDirectoryBasedOnInputFileName(File sourceFile, File outputDir) throws IOException {
    String outputLocation = outputDir.getAbsolutePath() + "/" + Files.getNameWithoutExtension(sourceFile.getName());
    return checkOutputDirectoryExists(outputLocation);
  }

  private static boolean multiplesOfAHundred(int counter) {
    return counter != 0 && counter % 100 == 0;
  }

  private static void storeOntologyInTempDir() {
    File outputTempDir = Files.createTempDir();
    File outputOntologyFile = new File(outputTempDir, CDE_VALUESETS_ONTOLOGY_NAME);
    logger.info("Storing the generated value set ontology at " + outputOntologyFile);
    ValueSetsOntologyManager.saveOntology(outputOntologyFile);
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

