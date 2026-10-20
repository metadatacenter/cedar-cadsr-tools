package org.metadatacenter.cadsr.ingestor.cde.action;

import org.junit.jupiter.api.Test;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarServer;
import org.metadatacenter.cadsr.ingestor.util.StubCedar;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * After writing the value-sets ontology, the updater asks the resource server to load it, and reports any outcome
 * but a completed load before going on to create the CDEs. A server that refuses the request is such an outcome.
 */
class LoadValueSetsOntologyActionTest {

  @Test
  void aRefusedLoadIsReportedRatherThanEndingTheRun() throws Exception {
    try (StubCedar cedar = StubCedar.start()) {
      cedar.answer("POST", "/resource/command/load-valuesets-ontology", 503, "{}");

      String status = new LoadValueSetsOntologyAction().execute(CedarServer.LOCAL, "key");

      assertTrue(status.startsWith("Error calling load value sets ontology endpoint"), status);
    }
  }
}
