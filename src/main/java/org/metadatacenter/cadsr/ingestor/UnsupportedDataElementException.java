package org.metadatacenter.cadsr.ingestor;

import org.metadatacenter.cadsr.DataElement;

import static com.google.common.base.Preconditions.checkNotNull;

public class UnsupportedDataElementException extends Exception {

  private final DataElement dataElement;
  private final String reason;

  public UnsupportedDataElementException(DataElement dataElement, String reason) {
    this.dataElement = checkNotNull(dataElement);
    this.reason = checkNotNull(reason);
  }

  public DataElement getDataElement() {
    return dataElement;
  }

  public String getReason() {
    return reason;
  }

  @Override
  public String getMessage() {
    String cdeId = dataElement.getPUBLICID().getContent();
    String cdeName = dataElement.getLONGNAME().getContent();
    return null;
    //return String.format("Failed to convert '%s' (ID: %s) - Reason: %s", cdeName, cdeId, reason);
  }
}
