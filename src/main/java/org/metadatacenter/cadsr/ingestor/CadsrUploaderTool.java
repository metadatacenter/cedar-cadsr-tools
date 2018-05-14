package org.metadatacenter.cadsr.ingestor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import static java.lang.String.format;

public class CadsrUploaderTool {

  private static final Logger logger = LoggerFactory.getLogger(CadsrUploaderTool.class);

  public static void main(String[] args) {

    String cdeSourceLocation = args[0];
    String endpoint = args[1];
    String apiKey = args[2];

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
          logger.info(format("Uploading resource (%d/%d)", counter++, totalCdes));
          post(endpoint, apiKey, fieldJson);
        } catch (Exception e) {
          logger.warn(e.getMessage());
        }
      }
    } catch (FileNotFoundException e) {
      logger.error(e.getMessage());
    }
  }

  private static void post(String endpoint, String apiKey, String payload) {
    HttpURLConnection conn = null;
    try {
      URL url = new URL(endpoint);
      conn = (HttpURLConnection) url.openConnection();
      conn.setDoOutput(true);
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("Authorization", apiKey);
      OutputStream os = conn.getOutputStream();
      os.write(payload.getBytes());
      os.flush();
      if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
        throw new RuntimeException("Failed : HTTP error code : "
            + conn.getResponseCode());
      }
    } catch (MalformedURLException e) {
      logger.error(e.getMessage());
    } catch (IOException e) {
      logger.error(e.getMessage());
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }
}
