package org.metadatacenter.cadsr.ingestor;

import com.google.common.base.Strings;
import org.metadatacenter.cadsr.DataElement;
import org.metadatacenter.cadsr.VALUEDOMAIN;
import org.metadatacenter.model.ModelNodeNames;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Map;

public class ValueConstraintsHandler implements ModelHandler {

  private static final String ENUMERATED = "Enumerated";
  private static final String NON_ENUMERATED = "NonEnumerated";

  private static final String XSD_DECIMAL = "xsd:decimal";
  private static final String XSD_LONG = "xsd:long";
  private static final String XSD_INT = "xsd:int";
  private static final String XSD_DOUBLE = "xsd:double";

  private static final int NONE = -1;

  private int minLength = NONE;
  private int maxLength = NONE;
  private int decimalPlace = NONE;
  private Number minValue;
  private Number maxValue;
  private String unitOfMeasure;
  private String numberType;

  public ValueConstraintsHandler handle(DataElement dataElement) throws UnsupportedDataElementException {
    final VALUEDOMAIN valueDomain = dataElement.getVALUEDOMAIN();
    String valueDomainType = valueDomain.getValueDomainType().getContent();
    if (ENUMERATED.equals(valueDomainType) || NON_ENUMERATED.equals(valueDomainType)) {
      handleValueDomain(valueDomain);
    } else {
      String reason = String.format("Value domain is not either enumerated or non-enumerated = %s (Unknown)",
          valueDomainType);
      throw new UnsupportedDataElementException(dataElement, reason);
    }
    return this;
  }

  private void handleValueDomain(final VALUEDOMAIN valueDomain) throws UnsupportedDataElementException {
    String datatype = valueDomain.getDatatype().getContent();
    if (CadsrDatatypes.STRING_LIST.contains(datatype)) {
      handleStringValueConstraints(valueDomain);
    } else if (CadsrDatatypes.NUMERIC_LIST.contains(datatype)) {
      handleNumericValueConstraints(valueDomain);
    }
  }

  private void handleStringValueConstraints(final VALUEDOMAIN valueDomain) {
    minLength = getMinimumLength(valueDomain);
    maxLength = getMaximumLength(valueDomain);
  }

  private void handleNumericValueConstraints(final VALUEDOMAIN valueDomain) {
    numberType = getNumberType(valueDomain);
    minValue = getMinimumValue(valueDomain, numberType);
    maxValue = getMaximumValue(valueDomain, numberType);
    decimalPlace = getDecimalPlace(valueDomain);
    unitOfMeasure = getUnitOfMeasure(valueDomain);
  }

  private static int getMinimumLength(final VALUEDOMAIN valueDomain) {
    String value = valueDomain.getMinimumLength().getContent();
    if (!Strings.isNullOrEmpty(value)) {
      return Integer.parseInt(value);
    } else {
      return NONE;
    }
  }

  private static int getMaximumLength(final VALUEDOMAIN valueDomain) {
    String value = valueDomain.getMaximumLength().getContent();
    if (!Strings.isNullOrEmpty(value)) {
      return Integer.parseInt(value);
    } else {
      return NONE;
    }
  }

  @Nullable
  private static String getNumberType(VALUEDOMAIN valueDomain) {
    String numericDatatype = valueDomain.getDatatype().getContent();
    if (!Strings.isNullOrEmpty(numericDatatype)) {
      if (CadsrDatatypes.JAVA_LONG.equals(numericDatatype)) {
        return XSD_LONG;
      } else if (CadsrDatatypes.JAVA_INTEGER.equals(numericDatatype)) {
        return XSD_INT;
      } else if (CadsrDatatypes.JAVA_DOUBLE.equals(numericDatatype)) {
        return XSD_DOUBLE;
      } else {
        return XSD_DECIMAL;
      }
    } else {
      return null;
    }
  }

  @Nullable
  private static Number getMinimumValue(final VALUEDOMAIN valueDomain, String numberType) {
    String value = valueDomain.getMinimumValue().getContent();
    return getNumber(value, numberType);
  }

  @Nullable
  private static Number getMaximumValue(VALUEDOMAIN valueDomain, String numberType) {
    String value = valueDomain.getMaximumValue().getContent();
    return getNumber(value, numberType);
  }

  @Nullable
  private static Number getNumber(String numberValue, String numberType) {
    if (!Strings.isNullOrEmpty(numberValue)) {
      if (XSD_LONG.equals(numberType)) {
        return Long.valueOf(numberValue);
      } else if (XSD_INT.equals(numberType)) {
        return Integer.valueOf(numberValue);
      } else if (XSD_DOUBLE.equals(numberType)) {
        return Double.valueOf(numberValue);
      } else {
        return new BigDecimal(numberValue);
      }
    } else {
      return null;
    }
  }

  private static int getDecimalPlace(VALUEDOMAIN valueDomain) {
    String decimalPlace = valueDomain.getDecimalPlace().getContent();
    if (!Strings.isNullOrEmpty(decimalPlace)) {
      return Integer.parseInt(decimalPlace);
    } else {
      return NONE;
    }
  }

  @Nullable
  private static String getUnitOfMeasure(VALUEDOMAIN valueDomain) {
    String unitOfMeasure = valueDomain.getUnitOfMeasure().getContent();
    if (!Strings.isNullOrEmpty(unitOfMeasure)) {
      return unitOfMeasure;
    } else {
      return null;
    }
  }

  @Override
  public void apply(Map<String, Object> fieldObject) {
    Map<String, Object> valueConstraints = (Map<String, Object>) fieldObject.get(ModelNodeNames.VALUE_CONSTRAINTS);
    if (minLength != NONE) {
      valueConstraints.put(ModelNodeNames.MIN_LENGTH, minLength);
    }
    if (maxLength != NONE) {
      valueConstraints.put(ModelNodeNames.MAX_LENGTH, maxLength);
    }
    if (decimalPlace != NONE) {
      valueConstraints.put(ModelNodeNames.DECIMAL_PLACE, decimalPlace);
    }
    if (minValue != null) {
      valueConstraints.put(ModelNodeNames.MIN_NUMBER_VALUE, minValue);
    }
    if (maxValue != null) {
      valueConstraints.put(ModelNodeNames.MAX_NUMBER_VALUE, maxValue);
    }
    if (numberType != null) {
      valueConstraints.put(ModelNodeNames.NUMBER_TYPE, numberType);
    }
    if (unitOfMeasure != null) {
      valueConstraints.put(ModelNodeNames.UNIT_OF_MEASURE, unitOfMeasure);
    }
  }
}
