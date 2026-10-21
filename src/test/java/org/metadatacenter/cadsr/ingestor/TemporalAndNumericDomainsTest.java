package org.metadatacenter.cadsr.ingestor;

import org.junit.jupiter.api.Test;
import org.metadatacenter.cadsr.cde.schema.DataElement;
import org.metadatacenter.cadsr.ingestor.cde.handler.UIHandler;
import org.metadatacenter.cadsr.ingestor.cde.handler.ValueConstraintsHandler;
import org.metadatacenter.cadsr.ingestor.exception.UnsupportedDataElementException;
import org.metadatacenter.cadsr.ingestor.util.CdeUtil;
import org.metadatacenter.model.ModelNodeValues;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A CDE's value domain names a datatype and, for dates and times, a display format. A temporal datatype becomes a
 * field of date, time or date-time type whose granularity follows the display format, and a numeric datatype
 * becomes a number field of the matching XSD type, bounded by the domain's limits or by the datatype's own. Each
 * case varies one sample: a date for the temporal cases and an integer for the numeric ones.
 */
class TemporalAndNumericDomainsTest {

  @Test
  void aDatesGranularityFollowsItsDisplayFormat() throws Exception {
    assertEquals(ModelNodeValues.TEMPORAL_GRANULARITY_YEAR, ui(temporal("DATE", "YYYY")).getTemporalGranularity());
    assertEquals(ModelNodeValues.TEMPORAL_GRANULARITY_MONTH, ui(temporal("DATE", "MM/YYYY")).getTemporalGranularity());
    assertEquals(ModelNodeValues.TEMPORAL_GRANULARITY_DAY, ui(temporal("DATE", "DD-MON-YYYY")).getTemporalGranularity());
    assertEquals(ModelNodeValues.TEMPORAL_GRANULARITY_DAY, ui(temporal("DATE", "an unknown format")).getTemporalGranularity(),
        "an unknown format falls back to the day");
    assertEquals("xsd:date", temporalType(temporal("DATE", "YYYY")));
  }

  @Test
  void aTimesGranularityAndClockFollowItsDisplayFormat() throws Exception {
    UIHandler hours = ui(temporal("TIME", "hh"));
    assertEquals(ModelNodeValues.TEMPORAL_GRANULARITY_HOUR, hours.getTemporalGranularity());
    assertEquals(ModelNodeValues.TIME_FORMAT_24H, hours.getInputTimeFormat());
    assertEquals(false, hours.getTimezoneEnabled());
    UIHandler minutes = ui(temporal("TIME", "TIME (HR(24):MN)"));
    assertEquals(ModelNodeValues.TEMPORAL_GRANULARITY_MINUTE, minutes.getTemporalGranularity());
    assertEquals(ModelNodeValues.TIME_FORMAT_24H, minutes.getInputTimeFormat());
    assertEquals(ModelNodeValues.TEMPORAL_GRANULARITY_SECOND, ui(temporal("TIME", "hh:mm:ss")).getTemporalGranularity());
    assertEquals(ModelNodeValues.TEMPORAL_GRANULARITY_SECOND, ui(temporal("TIME", "an unknown format")).getTemporalGranularity());
    assertEquals("xsd:time", temporalType(temporal("TIME", "hh")));
  }

  @Test
  void aDateTimeDefaultsToSecondsOnA24HourClock() throws Exception {
    UIHandler dateTime = ui(temporal("DATETIME", "an unknown format"));
    assertEquals(ModelNodeValues.TEMPORAL_GRANULARITY_SECOND, dateTime.getTemporalGranularity());
    assertEquals(ModelNodeValues.TIME_FORMAT_24H, dateTime.getInputTimeFormat());
    assertEquals(false, dateTime.getTimezoneEnabled());
    assertEquals("xsd:dateTime", temporalType(temporal("DATE/TIME", "an unknown format")));
  }

  @Test
  void aNumbersTypeAndBoundsFollowItsDatatype() throws Exception {
    Map<String, Object> bytes = numeric("java.lang.Byte", null, null);
    assertEquals("xsd:byte", bytes.get("numberType"));
    assertEquals((byte) -128, bytes.get("minValue"), "a byte is bounded by its range");
    assertEquals((byte) 127, bytes.get("maxValue"));

    Map<String, Object> octets = numeric("UMLOctetv1.0", null, null);
    assertEquals("xsd:int", octets.get("numberType"));
    assertEquals(0, octets.get("minValue"));
    assertEquals(255, octets.get("maxValue"));

    assertEquals((short) -5, numeric("java.lang.Short", "-5", null).get("minValue"));
    assertEquals(9_000_000_000L, numeric("java.lang.Long", null, "9000000000").get("maxValue"));
    assertEquals(2.5f, numeric("java.lang.Float", null, "2.5").get("maxValue"));
    Map<String, Object> doubles = numeric("java.lang.Double", "0.5", null);
    assertEquals("xsd:double", doubles.get("numberType"));
    assertEquals(0.5d, doubles.get("minValue"));
    assertEquals(2, doubles.get("decimalPlace"));
    assertEquals("mg", doubles.get("unitOfMeasure"));
  }

  @Test
  void anUnknownDatatypeIsUnsupported() {
    assertThrows(UnsupportedDataElementException.class, () -> numeric("NOT_A_CADSR_DATATYPE", null, null));
  }

  private static UIHandler ui(DataElement element) throws Exception {
    return new UIHandler().handle(element);
  }

  private static Object temporalType(DataElement element) throws Exception {
    return constraints(element).get("temporalType");
  }

  private static Map<String, Object> numeric(String datatype, String min, String max) throws Exception {
    String xml = sample("cde-sample-3017383.xml")
        .replace("<Datatype>java.lang.Integer</Datatype>", "<Datatype>" + datatype + "</Datatype>")
        .replace("<DecimalPlace NULL=\"TRUE\"/>", "<DecimalPlace>2</DecimalPlace>")
        .replace("<UnitOfMeasure NULL=\"TRUE\"/>", "<UnitOfMeasure>mg</UnitOfMeasure>");
    if (min != null) {
      xml = xml.replace("<MinimumValue NULL=\"TRUE\"/>", "<MinimumValue>" + min + "</MinimumValue>");
    }
    if (max != null) {
      xml = xml.replace("<MaximumValue NULL=\"TRUE\"/>", "<MaximumValue>" + max + "</MaximumValue>");
    }
    return constraints(element(xml));
  }

  private static DataElement temporal(String datatype, String displayFormat) throws Exception {
    return element(sample("cde-sample-2001826.xml")
        .replace("<Datatype>DATE</Datatype>", "<Datatype>" + datatype + "</Datatype>")
        .replace("<DisplayFormat>MM/DD/YYYY</DisplayFormat>", "<DisplayFormat>" + displayFormat + "</DisplayFormat>"));
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> constraints(DataElement element) throws Exception {
    Map<String, Object> field = new HashMap<>();
    field.put("_valueConstraints", new HashMap<String, Object>());
    new ValueConstraintsHandler().handle(element).apply(field);
    return (Map<String, Object>) field.get("_valueConstraints");
  }

  private static String sample(String name) throws Exception {
    try (InputStream in = TemporalAndNumericDomainsTest.class.getResourceAsStream("/" + name)) {
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private static DataElement element(String xml) throws Exception {
    return CdeUtil.getDataElement(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
  }
}
