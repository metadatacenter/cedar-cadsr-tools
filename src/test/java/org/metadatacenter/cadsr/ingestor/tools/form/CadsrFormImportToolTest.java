package org.metadatacenter.cadsr.ingestor.tools.form;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.metadatacenter.cadsr.ingestor.util.StubCedar;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The form import's command line reads one caDSR form and prints the template it becomes, as JSON, for an operator
 * to inspect or post. Here CEDAR holds none of the form's CDEs, so the template holds the form's two sections.
 */
@Tag("integration")
class CadsrFormImportToolTest {

  @Test
  void theTemplateIsPrinted() throws Exception {
    String printed;
    try (StubCedar cedar = StubCedar.start()) {
      cedar.answer("GET", "/resource/search", 200, "{\"totalCount\": 0, \"resources\": []}");
      String[] args = {"-f", example("form2.xml")};
      printed = captured(() -> CadsrFormImportTool.main(args));
    }

    // The log shares standard output, so the template is the one line that is a JSON object.
    String json = printed.lines().filter(line -> line.startsWith("{\"")).reduce((a, b) -> b).orElseThrow();
    JsonNode template = new ObjectMapper().readTree(json);
    assertEquals("2725838", template.get("schema:identifier").asText());
    assertEquals(2, template.get("_ui").get("order").size(), "the two sections, and no field: " + template.get("_ui"));
  }

  private static String example(String form) throws URISyntaxException {
    return Path.of(CadsrFormImportToolTest.class.getResource("/xml/examples/forms/" + form).toURI()).toString();
  }

  private static String captured(Runnable run) {
    PrintStream original = System.out;
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
    try {
      run.run();
    } finally {
      System.setOut(original);
    }
    return buffer.toString(StandardCharsets.UTF_8);
  }
}
