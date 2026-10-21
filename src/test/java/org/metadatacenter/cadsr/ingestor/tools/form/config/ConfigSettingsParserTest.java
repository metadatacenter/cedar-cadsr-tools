package org.metadatacenter.cadsr.ingestor.tools.form.config;

import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** The form import reads one caDSR form, whose path it must be given. */
class ConfigSettingsParserTest {

  @Test
  void theFormsPathIsRequired() throws Exception {
    assertEquals("form.xml", ConfigSettingsParser.parseOrRefuse(new String[]{"-f", "form.xml"}).getFormFilePath());
    assertEquals("form.xml", ConfigSettingsParser.parseOrRefuse(new String[]{"--form", "form.xml"}).getFormFilePath());
    assertThrows(ParseException.class, () -> ConfigSettingsParser.parseOrRefuse(new String[]{}));
  }
}
