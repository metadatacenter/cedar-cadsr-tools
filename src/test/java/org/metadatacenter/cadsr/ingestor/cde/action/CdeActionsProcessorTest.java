package org.metadatacenter.cadsr.ingestor.cde.action;

import org.junit.jupiter.api.Test;
import org.metadatacenter.cadsr.ingestor.cde.CdeSummary;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The processor applies the CDE actions the updater worked out: it creates the new CDEs, records what CEDAR called
 * each, and then applies the actions for CDEs the file no longer lists. Either kind can come without the other.
 */
class CdeActionsProcessorTest {

  @Test
  void createdCdesAreRecordedUnderTheirKeys() throws Exception {
    Map<String, Object> cde = new HashMap<>(Map.of("schema:identifier", "2188100", "pav:version", "1.0.0"));
    CreateCdeAction create = new CreateCdeAction(cde, "hash-1", "folder", List.of(), List.of()) {
      @Override
      public String execute(CedarServer cedarEnvironment, String apiKey) {
        return "cedar-2188100";
      }
    };
    Map<String, CdeSummary> cdes = new HashMap<>();

    new CdeActionsProcessor(null, List.of(create), List.of(), cdes, CedarServer.LOCAL, "key").executeCdeActions();

    assertEquals("cedar-2188100", cdes.get("2188100V1.0.0").getCedarId());
    assertEquals("hash-1", cdes.get("2188100V1.0.0").getHashCode());
  }

  @Test
  void aRunWithNothingToCreateStillAppliesTheOtherActions() throws Exception {
    List<String> applied = new ArrayList<>();
    UpdateOrDeleteCdeAction stale = new UpdateOrDeleteCdeAction("cedar-1", "9999999", "1.0.0", "h") {
      @Override
      public String execute(CedarServer cedarEnvironment, String apiKey) {
        applied.add(getCedarId());
        return getCedarId();
      }
    };

    new CdeActionsProcessor(null, List.of(), List.of(stale), new HashMap<>(), CedarServer.LOCAL, "key")
        .executeCdeActions();

    assertEquals(List.of("cedar-1"), applied);
  }

  @Test
  void aMissingOntologyActionIsReported() {
    assertEquals("Missing load value sets ontology action",
        new CdeActionsProcessor(null, List.of(), List.of(), new HashMap<>(), CedarServer.LOCAL, "key")
            .executeLoadValueSetsOntologyAction());
  }
}
