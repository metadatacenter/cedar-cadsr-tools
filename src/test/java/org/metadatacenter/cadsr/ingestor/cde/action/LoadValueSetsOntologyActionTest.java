package org.metadatacenter.cadsr.ingestor.cde.action;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarServer;
import org.metadatacenter.cadsr.ingestor.util.StubCedar;

import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * After writing the value-sets ontology, the updater asks the resource server to load it, checks the load's status
 * until it ends or the checks run out, and reports any outcome but a completed load before going on to create the
 * CDEs. The waits between checks are zero here.
 */
@Tag("integration")
class LoadValueSetsOntologyActionTest {

  private static final String LOAD = "/resource/command/load-valuesets-ontology";
  private static final String STATUS = "/resource/command/load-valuesets-ontology-status";

  @Test
  void aRefusedLoadIsReportedRatherThanEndingTheRun() throws Exception {
    try (StubCedar cedar = StubCedar.start()) {
      cedar.answer("POST", LOAD, 503, "{}");

      String status = action().execute(CedarServer.LOCAL, "key");

      assertTrue(status.startsWith("Error calling load value sets ontology endpoint"), status);
    }
  }

  @Test
  void aLoadStillInProgressIsCheckedAgainUntilItCompletes() throws Exception {
    assertEquals("Done", outcome(List.of("IN_PROGRESS", "IN_PROGRESS", "COMPLETE"), 3));
  }

  @Test
  void aLoadThatEndsInErrorIsReportedAsAnError() throws Exception {
    assertEquals("Error", outcome(List.of("IN_PROGRESS", "ERROR"), 2));
  }

  @Test
  void aLoadThatNeverFinishesIsReportedAfterTheLastCheck() throws Exception {
    assertEquals("Failed to finish", outcome(List.of("IN_PROGRESS"), LoadValueSetsOntologyAction.MAX_STATUS_CHECK_ATTEMPTS));
  }

  @Test
  void aLoadThatNeverStartsIsReportedAsSuch() throws Exception {
    assertEquals("Failed to start", outcome(List.of("NOT_YET_INITIATED"),
        LoadValueSetsOntologyAction.MAX_STATUS_CHECK_ATTEMPTS));
  }

  /** The action's result against a server whose status answers run through the list, repeating the last. */
  private static String outcome(List<String> statuses, int expectedChecks) throws Exception {
    try (StubCedar cedar = StubCedar.start()) {
      Iterator<String> answers = statuses.iterator();
      String[] last = {statuses.get(0)};
      cedar.answer("POST", LOAD, 200, "{}");
      cedar.answer("GET", STATUS, request -> {
        if (answers.hasNext()) {
          last[0] = answers.next();
        }
        return new StubCedar.Reply(200, "{\"importStatus\": \"" + last[0] + "\"}");
      });

      String status = action().execute(CedarServer.LOCAL, "key");

      assertEquals(1, cedar.requests("POST", LOAD).size());
      assertEquals(expectedChecks, cedar.requests("GET", STATUS).size(), "status checks");
      return status;
    }
  }

  private static LoadValueSetsOntologyAction action() {
    return new LoadValueSetsOntologyAction(0, 0);
  }
}
