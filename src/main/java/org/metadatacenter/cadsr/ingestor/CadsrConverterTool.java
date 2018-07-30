package org.metadatacenter.cadsr.ingestor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;
import static org.metadatacenter.cadsr.ingestor.Constants.*;

public class CadsrConverterTool {

  private static final Logger logger = LoggerFactory.getLogger(CadsrConverterTool.class);
  private static final boolean SAVE_FIELDS = false;
  private static final boolean SAVE_VALUESETS_ONTOLOGY = true;

  public static void main(String[] args) {

    String cdeSourceFolder = args[0];
    String outputDirectory = args[1];

    try {
      for (final File fileEntry : new File(cdeSourceFolder).listFiles()) {
        logger.info("Processing file: " + fileEntry.getName());
        if (fileEntry.isDirectory()) {
          // Do nothing
        } else {
          Collection<Map<String, Object>> fieldMaps = CadsrUtils.getFieldMapsFromInputStream(new FileInputStream(fileEntry));
          int totalCdes = fieldMaps.size();
          logger.info("Total number of CDEs: " + totalCdes);

          if (SAVE_FIELDS) {
            int count = 0;
            for (Map<String, Object> fieldMap : fieldMaps) {
              try {
                // TODO: remove the following line (it is used to save only fields with value sets)
                if (((List) ((Map) fieldMap.get("_valueConstraints")).get("valueSets")).size() > 0) {
                  String fieldJson = new ObjectMapper().writeValueAsString(fieldMap);
                  String outputFolderPath = outputDirectory + "/" + CDES_FOLDER + "/";
                  File outputFolder = new File(outputFolderPath);
                  if (!outputFolder.exists()) {
                    outputFolder.mkdir();
                  }
                  String outputFilePath = outputFolderPath + UUID.randomUUID() + ".json";
                  Files.write(Paths.get(outputFilePath), fieldJson.getBytes());
                  if (count % 100 == 0) {
                    logger.info(format("Writing field (%d/%d)", count++, totalCdes));
                  }
                  count++;
//                  if (count == 2) {
//                    System.exit(0);
//                  }
                }
              } catch (JsonProcessingException e) {
                logger.error(e.toString());
              } catch (IOException e) {
                logger.error(e.toString());
              }
            }
          }
        }
      }
      if (SAVE_VALUESETS_ONTOLOGY) {
        File ontologyFile = new File(outputDirectory + "/" +
            CDE_VALUESETS_ONTOLOGY_FOLDER + "/" + CDE_VALUESETS_ONTOLOGY_NAME);
        // Save value sets ontology
        logger.info("Saving ontology - " + ontologyFile.getAbsolutePath());
        ValueSetsOntologyManager.saveOntology(ontologyFile);
      }
    } catch (FileNotFoundException e) {
      logger.error(e.toString());
    }
  }
}

