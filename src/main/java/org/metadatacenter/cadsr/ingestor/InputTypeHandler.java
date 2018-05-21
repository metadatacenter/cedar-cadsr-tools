package org.metadatacenter.cadsr.ingestor;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.metadatacenter.cadsr.DataElement;
import org.metadatacenter.cadsr.VALUEDOMAIN;
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
      checkUnsupportedFeatures(dataElement);
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
      inputType.put(ModelNodeNames.INPUT_TYPE, ModelNodeValues.TEXT_FIELD);
    } else {
      String reason = String.format("An enumerated %s is not supported (Unsupported)", datatype);
      throw new UnsupportedDataElementException(dataElement, reason);
    }
  }

  private void handleNonEnumeratedType(DataElement dataElement) throws UnsupportedDataElementException {
    String datatype = dataElement.getVALUEDOMAIN().getDatatype().getContent();
    if (CadsrDatatypes.STRING_LIST.contains(datatype)) {
      VALUEDOMAIN valueDomain = dataElement.getVALUEDOMAIN();
      int maxLength = getMaxLength(valueDomain);
      if (maxLength >= MAX_LENGTH_FOR_TEXTFIELD) {
        inputType.put(ModelNodeNames.INPUT_TYPE, ModelNodeValues.TEXT_AREA);
      } else {
        inputType.put(ModelNodeNames.INPUT_TYPE, ModelNodeValues.TEXT_FIELD);
      }
    } else if (CadsrDatatypes.NUMERIC_LIST.contains(datatype)) {
      inputType.put(ModelNodeNames.INPUT_TYPE, ModelNodeValues.NUMERIC);
    } else if (CadsrDatatypes.DATE_LIST.contains(datatype)) {
      inputType.put(ModelNodeNames.INPUT_TYPE, ModelNodeValues.DATE);
    } else {
      String reason = String.format("A non-enumerated %s is not supported (Unsupported)", datatype);
      throw new UnsupportedDataElementException(dataElement, reason);
    }
  }

  private void checkUnsupportedFeatures(DataElement dataElement) throws UnsupportedDataElementException {
    String minValue = dataElement.getVALUEDOMAIN().getMinimumValue().getContent();
    if (!Strings.isNullOrEmpty(minValue)) {
      String reason = String.format("A value constraint of 'minimum value' is not supported (Unsupported)");
      throw new UnsupportedDataElementException(dataElement, reason);
    }
    String maxValue = dataElement.getVALUEDOMAIN().getMaximumValue().getContent();
    if (!Strings.isNullOrEmpty(maxValue)) {
      String reason = String.format("A value constraint of 'maximum value' is not supported (Unsupported)");
      throw new UnsupportedDataElementException(dataElement, reason);
    }
    String decimalPlace = dataElement.getVALUEDOMAIN().getDecimalPlace().getContent();
    if (!Strings.isNullOrEmpty(decimalPlace)) {
      String reason = String.format("A value constraint of 'decimal place' is not supported (Unsupported)");
      throw new UnsupportedDataElementException(dataElement, reason);
    }
    String unitOfMeasure = dataElement.getVALUEDOMAIN().getUnitOfMeasure().getContent();
    if (!Strings.isNullOrEmpty(unitOfMeasure)) {
      String reason = String.format("A value constraint of 'unit of measure' is not supported (Unsupported)");
      throw new UnsupportedDataElementException(dataElement, reason);
    }
  }

  private static int getMaxLength(VALUEDOMAIN valueDomain) {
    int maxLength = 0;
    String value = valueDomain.getMaximumLength().getContent();
    if (!Strings.isNullOrEmpty(value)) {
      maxLength = Integer.parseInt(value);
    }
    return maxLength;
  }

  public Map<String, Object> getInputType() {
    return Collections.unmodifiableMap(inputType);
  }

  @Override
  public void apply(final Map<String, Object> fieldObject) {
    fieldObject.put(ModelNodeNames.UI, getInputType());
  }
}