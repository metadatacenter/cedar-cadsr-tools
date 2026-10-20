package org.metadatacenter.cadsr.ingestor.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The helpers the caDSR import leans on throughout: reading the XML NCI publishes, hashing what it read, logging the
 * arguments an operator typed, and splitting work into batches.
 */
class GeneralUtilTest {

  /**
   * NCI's XML can hold characters XML 1.0 forbids, which the unmarshaller refuses, so each becomes a space before the
   * document is read. Every other character must survive the trip unchanged, a character outside the Basic
   * Multilingual Plane included.
   */
  @Test
  void onlyCharactersXmlForbidsAreReplaced() throws Exception {
    String text = "<a>café 🧬\u0001￾\tline\nend</a>";
    String cleaned = new String(GeneralUtil.processInvalidXMLCharacters(
        new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))).readAllBytes(), StandardCharsets.UTF_8);
    assertEquals("<a>café 🧬  \tline\nend</a>", cleaned);
  }

  @Test
  void aLoneSurrogateIsReplaced() throws Exception {
    String cleaned = new String(GeneralUtil.processInvalidXMLCharacters(
        new ByteArrayInputStream("a\uD83Eb".getBytes(StandardCharsets.UTF_8))).readAllBytes(), StandardCharsets.UTF_8);
    assertEquals(3, cleaned.length());
    assertTrue(cleaned.startsWith("a") && cleaned.endsWith("b"));
    assertFalse(Character.isSurrogate(cleaned.charAt(1)));
  }

  /** A value domain's optional parts arrive as empty or blank strings, or not at all, and each reads as absent. */
  @Test
  void aBlankOrMissingValueReadsAsAbsent() {
    assertNull(GeneralUtil.getValueOrNull(null));
    assertNull(GeneralUtil.getValueOrNull("  "));
    assertEquals("VD123", GeneralUtil.getValueOrNull("VD123"));
    assertTrue(GeneralUtil.isNullOrEmpty(null));
    assertTrue(GeneralUtil.isNullOrEmpty(" "));
    assertFalse(GeneralUtil.isNullOrEmpty("x"));
    assertEquals("", GeneralUtil.getOptionalValue(null));
    assertEquals("x", GeneralUtil.getOptionalValue("x"));
  }

  @Test
  void theHashIsSha1InHex() {
    assertEquals("a9993e364706816aba3e25717850c26c9cd0d89d", GeneralUtil.getSha1("abc"));
  }

  /** An identifier is placed in a URL path encoded once, whether or not it arrives encoded. */
  @Test
  void anIdentifierIsEncodedOnce() throws Exception {
    String id = "https://repo.metadatacenter.orgx/categories/1 2";
    String encoded = GeneralUtil.encodeIfNeeded(id);
    assertEquals("https%3A%2F%2Frepo.metadatacenter.orgx%2Fcategories%2F1+2", encoded);
    assertEquals(encoded, GeneralUtil.encodeIfNeeded(encoded));
  }

  /**
   * The updater logs the arguments it was started with. Each option appears with its value, and the values of the
   * password and the API key never appear, however the operator attached them.
   */
  @Test
  void loggedArgumentsHideEverySecretAndKeepEveryOption() {
    String[] args = {"-t", "-k", "secret-key", "-s", "local", "--ftp-password=secret-password", "-psecret", "-c"};
    List<String> groups = GeneralUtil.commandLineArgumentsGrouped(args, "-p", "--ftp-password", "-k", "--apikey");
    assertEquals(List.of("-t", "-k ********", "-s local", "--ftp-password ********", "-p ********", "-c"), groups);
    assertFalse(String.join(" ", groups).contains("secret"));
  }

  @Test
  void workIsSplitIntoBatchesOfTheGivenSize() {
    assertEquals(List.of(List.of(1, 2), List.of(3, 4), List.of(5)), GeneralUtil.getBatches(List.of(1, 2, 3, 4, 5), 2));
    assertEquals(List.of(), GeneralUtil.getBatches(List.of(), 2));
  }

  @Test
  void aShareIsFormattedAsAPercentage() {
    assertEquals("33.33%", GeneralUtil.calculatePercentage(1, 3));
    assertEquals("100%", GeneralUtil.calculatePercentage(4, 4));
  }

  @Test
  void aMapIsWrittenAsJson() {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("a", 1);
    map.put("b", List.of("c"));
    assertEquals("{\"a\":1,\"b\":[\"c\"]}", GeneralUtil.convertMapToJson(map));
  }

  @Test
  void aMissingDirectoryIsCreated(@TempDir Path dir) {
    File created = GeneralUtil.checkDirectoryExists(dir.resolve("execution").toString());
    assertTrue(created.isDirectory());
    assertEquals(created, GeneralUtil.checkDirectoryExists(created.getPath()), "an existing directory is kept");
  }
}
