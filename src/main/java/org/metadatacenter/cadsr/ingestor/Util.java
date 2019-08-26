package org.metadatacenter.cadsr.ingestor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util {

  private static final Logger logger = LoggerFactory.getLogger(CadsrUtils.class);

  public static String getSha1(String input) {
    MessageDigest mDigest = null;
    try {
      mDigest = MessageDigest.getInstance("SHA1");
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    byte[] result = mDigest.digest(input.getBytes());
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < result.length; i++) {
      sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
    }
    return sb.toString();
  }

  public static String getValueOrNull(String input) {
    if (input != null & input.trim().length() > 0) {
      return input;
    }
    else {
      return null;
    }
  }

  public static InputStream processInvalidXMLCharacters(InputStream in) throws IOException {
    StringBuffer out = new StringBuffer(); // Used to hold the output.
    Reader r = new InputStreamReader(in, "UTF-8");
    int intChar;
    while ((intChar = r.read()) != -1) {
      char inputChar = (char) intChar;
      if (inputChar == 0x13) { // Replace <0x13> by "-"
        out.append("-");
      }
      else {
        out.append(inputChar);
      }
    }
    return new ByteArrayInputStream(out.toString().getBytes());
  }

}
