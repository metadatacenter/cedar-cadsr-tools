package org.metadatacenter.cadsr.ingestor.cde.handler;

import com.google.common.collect.Maps;
import org.metadatacenter.cadsr.cde.schema.DataElement;
import org.metadatacenter.cadsr.ingestor.cde.CadsrDatatypes;
import org.metadatacenter.cadsr.ingestor.exception.UnsupportedDataElementException;
import org.metadatacenter.model.ModelNodeNames;
import org.metadatacenter.model.ModelNodeValues;

import java.util.Collections;
import java.util.Map;

public class InputTypeHandler implements ModelHandler {

  private static final String ENUMERATED = "Enumerated";
  private static final String NON_ENUMERATED = "NonEnumerated";

  private static final int MAX_LENGTH_FOR_TEXTFIELD = 255;

  private final Map<String, Object> inputType = Maps.newHashMap();

  public InputTypeHandler handle(DataElement dataElement) throws UnsupportedDataElementException {
    String valueDomainType = dataElement.getVALUEDOMAIN().getValueDomainType().getContent();
    if (ENUMERATED.equals(valueDomainType)) {
      handleEnumeratedType(dataElement);
    } else if (NON_ENUMERATED.equals(valueDomainType)) {
      handleNonEnumeratedType(dataElement);
    } else {
      String reason = String.format("Value domain is not either enumerated or non-enumerated = %s (Unknown)",
          valueDomainType);
      throw new UnsupportedDataElementException(dataElement, reason);
    }
    return this;
  }

  private void handleEnumeratedType(DataElement dataElement) throws UnsupportedDataElementException {
    String datatype = dataElement.getVALUEDOMAIN().getDatatype().getContent();
    if (CadsrDatatypes.STRING_LIST.contains(datatype)) {
      inputType.put(ModelNodeNames.UI_FIELD_INPUT_TYPE, ModelNodeValues.TEXT_FIELD);
    } else {
      String reason = String.format("An enumerated %s is not supported (Unsupported)", datatype);
      throw new UnsupportedDataElementException(dataElement, reason);
    }
  }

  private void handleNonEnumeratedType(DataElement dataElement) throws UnsupportedDataElementException {
    String datatype = dataElement.getVALUEDOMAIN().getDatatype().getContent();
    if (CadsrDatatypes.STRING_LIST.contains(datatype)) {
      inputType.put(ModelNodeNames.UI_FIELD_INPUT_TYPE, ModelNodeValues.TEXT_FIELD);
    } else if (CadsrDatatypes.NUMERIC_LIST.contains(datatype)) {
      inputType.put(ModelNodeNames.UI_FIELD_INPUT_TYPE, ModelNodeValues.NUMERIC);
    } else if (CadsrDatatypes.DATE_LIST.contains(datatype)) {
      inputType.put(ModelNodeNames.UI_FIELD_INPUT_TYPE, ModelNodeValues.DATE);
    } else {
      String reason = String.format("A non-enumerated %s is not supported (Unsupported)", datatype);
      throw new UnsupportedDataElementException(dataElement, reason);
    }
  }

  public Map<String, Object> getInputType() {
    return Collections.unmodifiableMap(inputType);
  }

  @Override
  public void apply(final Map<String, Object> fieldObject) {
    fieldObject.put(ModelNodeNames.UI, getInputType());
  }
}
