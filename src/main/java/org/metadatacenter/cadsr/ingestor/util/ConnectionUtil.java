package org.metadatacenter.cadsr.ingestor.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public class ConnectionUtil {

  private static final Logger logger = LoggerFactory.getLogger(ConnectionUtil.class);

  //@formatter:off
  private static TrustManager[] trustAllCerts = new TrustManager[1];
  static {
    trustAllCerts[0] = new X509TrustManager() {
      public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
      public void checkClientTrusted(X509Certificate[] certs, String authType) {}
      public void checkServerTrusted(X509Certificate[] certs, String authType) {}
    };
  }
  //@formatter:on

  public static String readResponseMessage(InputStream is) {
    StringBuffer sb = new StringBuffer();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
      String messageLine;
      while ((messageLine = br.readLine()) != null) {
        sb.append(messageLine);
      }
    } catch (IOException e) {
      logger.error(e.getMessage());
    }
    return sb.toString();
  }

  public static HttpURLConnection createAndOpenConnection(String requestMethod, String endpoint, String apiKey) throws IOException {
    ignoreSSLCheckingByAcceptingAnyCertificates();
    try {
      URL url = new URL(endpoint);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod(requestMethod);
      conn.setDoOutput(true);
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("Authorization", apiKey);
      return conn;
    } catch (MalformedURLException e) {
      logger.error(e.getMessage());
      throw new RuntimeException(e.getMessage());
    }
  }

  private static void ignoreSSLCheckingByAcceptingAnyCertificates() {
    try {
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
      HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> 1 == 1);
    } catch (KeyManagementException | NoSuchAlgorithmException e) {
      logger.error(e.getMessage());
      throw new RuntimeException(e.getMessage());
    }
  }

  public static void logErrorMessageAndThrowException(String message, final HttpURLConnection conn) {
    String response = ConnectionUtil.readResponseMessage(conn.getErrorStream());
    logger.error(response);
    throw new RuntimeException(message + "\nError message: " + response);
  }

//  private static void logResponseMessage(final HttpURLConnection conn) throws IOException {
//    String response = ConnectionUtil.readResponseMessage(conn.getInputStream());
//    String message = createMessageBasedOnFieldNameAndId(response);
//    logger.debug("POST 200 OK: " + message);
//  }
//
//  private static String createMessageBasedOnFieldNameAndId(String response) throws IOException {
//    JsonNode responseNode = objectMapper.readTree(response);
//    String fieldName = responseNode.get("schema:name").asText();
//    String fieldId = responseNode.get("@id").asText();
//    return String.format("%s (ID: %s)", fieldName, fieldId);
//  }

}
