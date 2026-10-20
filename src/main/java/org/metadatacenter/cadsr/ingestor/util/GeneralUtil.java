package org.metadatacenter.cadsr.ingestor.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GeneralUtil {

  private static final Logger logger = LoggerFactory.getLogger(CdeUtil.class);

  public static String getSha1(String input) {
    MessageDigest mDigest = null;
    try {
      mDigest = MessageDigest.getInstance("SHA1");
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    byte[] result = mDigest.digest(input.getBytes());
    StringBuilder sb = new StringBuilder();
    for (byte b : result) {
      sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
    }
    return sb.toString();
  }

  public static String getValueOrNull(String input) {
    if (input != null && !input.trim().isEmpty()) {
      return input;
    } else {
      return null;
    }
  }

  public static File checkDirectoryExists(String directory) {
    File outputDir = new File(directory);
    if (!outputDir.exists()) {
      outputDir.mkdir();
    }
    return outputDir;
  }

  /**
   * Replaces each character the XML 1.0 standard does not allow with a space. The input is read as UTF-8 and the
   * output is written as UTF-8, whatever the platform's default charset, and a character outside the Basic
   * Multilingual Plane is kept whole rather than judged one half at a time.
   */
  public static InputStream processInvalidXMLCharacters(InputStream in) throws IOException {
    String text = new String(in.readAllBytes(), Constants.CHARSET);
    StringBuilder out = new StringBuilder(text.length());
    text.codePoints().forEach(codePoint -> {
      if (isValidXMLCharacter(codePoint)) {
        out.appendCodePoint(codePoint);
      } else {
        out.append(' ');
      }
    });
    return new ByteArrayInputStream(out.toString().getBytes(Constants.CHARSET));
  }

  /**
   * Check if the XML character is valid as specified by the XML 1.0 standard. For reference, please see
   * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the standard</a>.
   */
  private static boolean isValidXMLCharacter(int codePoint) {
    return codePoint == 0x9 ||
        codePoint == 0xA ||
        codePoint == 0xD ||
        (codePoint >= 0x20 && codePoint <= 0xD7FF) ||
        (codePoint >= 0xE000 && codePoint <= 0xFFFD) ||
        (codePoint >= 0x10000 && codePoint <= 0x10FFFF);
  }

  public static String encodeIfNeeded(String uri) throws UnsupportedEncodingException {
    String decodedUri = URLDecoder.decode(uri, "UTF-8");
    // It is necessary to encode it
    if (uri.compareTo(decodedUri) == 0) {
      return URLEncoder.encode(uri, "UTF-8");
    }
    // If was already encoded
    else {
      return uri;
    }
  }

  public static void logErrorMessage(final HttpURLConnection conn) {
    String response = ConnectionUtil.readResponseMessage(conn.getErrorStream());
    logger.error(response);
    throw new RuntimeException(response);
  }

  public static String calculatePercentage(int amount, int total) {
    final DecimalFormat percentFormat = new DecimalFormat("###.##%");
    return percentFormat.format(((float) amount / (float) total));
  }

  /**
   * Groups command-line arguments for logging, each option with its value, and hides the value of each secret option,
   * whether it follows the option or is attached to it ({@code -kVALUE}, {@code --apikey=VALUE}).
   */
  public static List<String> commandLineArgumentsGrouped(String[] args, String passwordShortOption, String passwordLongOption, String apikeyShortOption, String apikeyLongOption) {
    List<String> shortSecretOptions = List.of(passwordShortOption, apikeyShortOption);
    List<String> longSecretOptions = List.of(passwordLongOption, apikeyLongOption);
    List<String> argGroups = new ArrayList<>();
    String group = "";
    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      if (arg.startsWith("-") && group.length() > 0) {
        argGroups.add(group);
        group = "";
      }
      String attachedSecretOption = attachedSecretOption(arg, shortSecretOptions, longSecretOptions);
      if (shortSecretOptions.contains(arg) || longSecretOptions.contains(arg)) {
        argGroups.add(arg + " ********"); // hide password or apikey
        i++;
      } else if (attachedSecretOption != null) {
        argGroups.add(attachedSecretOption + " ********");
      } else if (arg.startsWith("-")) {
        group = arg;
      } else {
        argGroups.add(group.isEmpty() ? arg : group + " " + arg);
        group = "";
      }
    }
    if (group.length() > 0) {
      argGroups.add(group);
    }
    return argGroups;
  }

  private static String attachedSecretOption(String arg, List<String> shortSecretOptions, List<String> longSecretOptions) {
    for (String option : longSecretOptions) {
      if (arg.startsWith(option + "=")) {
        return option;
      }
    }
    for (String option : shortSecretOptions) {
      if (!arg.startsWith("--") && arg.startsWith(option) && arg.length() > option.length()) {
        return option;
      }
    }
    return null;
  }

  public static String convertMapToJson(Map map) {
    ObjectMapper objectMapper = new ObjectMapper();
    String json = null;
    try {
      json = objectMapper.writeValueAsString(map);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return json;
  }

  public static boolean isNullOrEmpty(String value) {
    if (value == null || value.trim().length() == 0) {
      return true;
    }
    return false;
  }

  public static String getOptionalValue(String content) {
    return content != null ? content : "";
  }

  public static <T> List<List<T>> getBatches(List<T> collection, int batchSize){
    int i = 0;
    List<List<T>> batches = new ArrayList<>();
    while(i<collection.size()){
      int nextInc = Math.min(collection.size()-i,batchSize);
      List<T> batch = collection.subList(i,i+nextInc);
      batches.add(batch);
      i = i + nextInc;
    }

    return batches;
  }

}
