package org.metadatacenter.cadsr.ingestor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;

public class CadsrConverterTool {

  private static final Logger logger = LoggerFactory.getLogger(CadsrConverterTool.class);

  public static void main(String[] args) {

    String cdeSourceLocation = args[0];
    String outputDirectory = args[1];

    try {
      File sourceFile = new File(cdeSourceLocation);
      logger.info("Generating CEDAR fields...");
      Collection<Map<String, Object>> fieldMaps =
          CadsrUtils.getFieldMapsFromInputStream(new FileInputStream(sourceFile));
      int totalCdes = fieldMaps.size();
      int counter = 0;
      for (Map<String, Object> fieldMap : fieldMaps) {
        try {
          String fieldJson = new ObjectMapper().writeValueAsString(fieldMap);
          String outputPath = outputDirectory + "/" + UUID.randomUUID() + ".json";
          Files.write(Paths.get(outputPath), fieldJson.getBytes());
          logger.info(format("Writing resource (%d/%d)", counter++, totalCdes));
        } catch (Exception e) {
          logger.warn(e.getMessage());
        }
      }
    } catch (FileNotFoundException e) {
      logger.error(e.getMessage());
    }
  }
}
