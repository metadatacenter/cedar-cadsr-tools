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
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;
import static org.metadatacenter.cadsr.ingestor.Constants.CDES_FOLDER;
import static org.metadatacenter.cadsr.ingestor.Constants.CDE_VALUESETS_ONTOLOGY_FOLDER;
import static org.metadatacenter.cadsr.ingestor.Constants.CDE_VALUESETS_ONTOLOGY_NAME;

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
          String outputFolderPath = outputDirectory + "/" + CDES_FOLDER + "/";
          File outputFolder = new File(outputFolderPath);
          if (!outputFolder.exists()) {
            outputFolder.mkdir();
          }
          String outputFilePath = outputFolderPath + UUID.randomUUID() + ".json";
          Files.write(Paths.get(outputFilePath), fieldJson.getBytes());
          if (counter % 100 == 0) {
            logger.info(format("Writing resource (%d/%d)", counter++, totalCdes));
          }
        } catch (JsonProcessingException e) {
          logger.error(e.toString());
        } catch (IOException e) {
          logger.error(e.toString());
        }
      }
      File ontologyFile = new File(outputDirectory + "/" +
          CDE_VALUESETS_ONTOLOGY_FOLDER + "/" + CDE_VALUESETS_ONTOLOGY_NAME);
      // Save value sets ontology
      logger.info("Saving ontology - " + ontologyFile.getAbsolutePath());
      ValueSetsOntologyManager.saveOntology(ontologyFile);
    } catch (FileNotFoundException e) {
      logger.error(e.toString());
    }
  }
}
