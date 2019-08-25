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

  public static InputStream stripNonValidXMLCharacters(InputStream in) throws IOException {
    StringBuffer out = new StringBuffer(); // Used to hold the output.
    Reader r = new InputStreamReader(in, "UTF-8");
    int intChar;
    while ((intChar = r.read()) != -1) {
      char inputChar = (char) intChar;
      if ((inputChar == 0x9) ||
          (inputChar == 0xA) ||
          (inputChar == 0xD) ||
          ((inputChar >= 0x20) && (inputChar <= 0xD7FF)) ||
          ((inputChar >= 0xE000) && (inputChar <= 0xFFFD)) ||
          ((inputChar >= 0x10000) && (inputChar <= 0x10FFFF)))
        out.append(inputChar);
      else {
        logger.info("Found invalid XML character: " + "0x" + Integer.toHexString(intChar));
      }
    }
    return new ByteArrayInputStream(out.toString().getBytes());
  }

}
