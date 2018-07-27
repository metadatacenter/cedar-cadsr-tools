package org.metadatacenter.cadsr.ingestor;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util {

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

}
