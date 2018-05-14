package org.metadatacenter.cadsr.ingestor;

import java.util.Map;

public interface ModelHandler {

  void apply(final Map<String, Object> fieldObject);
}
