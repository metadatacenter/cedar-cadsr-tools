package org.metadatacenter.cadsr.ingestor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.metadatacenter.cadsr.DataElement;
import org.metadatacenter.cadsr.ingestor.exception.UnsupportedDataElementException;
import org.metadatacenter.model.ModelNodeNames;
import org.metadatacenter.model.ModelNodeValues;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PropertiesHandler implements ModelHandler {

  private static final String ENUMERATED = "Enumerated";
  private static final String NON_ENUMERATED = "NonEnumerated";

  private final Map<String, Object> typeProperty = Maps.newHashMap();
  private final Map<String, Object> valueProperty = Maps.newHashMap();
  private final Map<String, Object> idProperty = Maps.newHashMap();
  private final Map<String, Object> rdfsLabelProperty = Maps.newHashMap();

  public PropertiesHandler handle(DataElement dataElement) throws UnsupportedDataElementException {
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
      typeProperty.put(ModelNodeNames.LD_TYPE, setOneOfStringOrArray());
      idProperty.put(ModelNodeNames.LD_ID, setUriString());
      rdfsLabelProperty.put(ModelNodeNames.RDFS_LABEL, setTypeStringOrNull());
    } else {
      String reason = String.format("An enumerated %s is not supported (Unsupported)", datatype);
      throw new UnsupportedDataElementException(dataElement, reason);
    }
  }

  private void handleNonEnumeratedType(DataElement dataElement) throws UnsupportedDataElementException {
    String datatype = dataElement.getVALUEDOMAIN().getDatatype().getContent();
    if (CadsrDatatypes.STRING_LIST.contains(datatype)) {
      typeProperty.put(ModelNodeNames.LD_TYPE, setOneOfStringOrArray());
      valueProperty.put(ModelNodeNames.LD_VALUE, setTypeStringOrNull());
    } else if (CadsrDatatypes.NUMERIC_LIST.contains(datatype)) {
      typeProperty.put(ModelNodeNames.LD_TYPE, setUriString());
      valueProperty.put(ModelNodeNames.LD_VALUE, setTypeStringOrNull());
    } else if (CadsrDatatypes.DATE_LIST.contains(datatype)) {
      typeProperty.put(ModelNodeNames.LD_TYPE, setUriString());
      valueProperty.put(ModelNodeNames.LD_VALUE, setTypeStringOrNull());
    } else {
      String reason = String.format("A non-enumerated %s is not supported (Unsupported)", datatype);
      throw new UnsupportedDataElementException(dataElement, reason);
    }
  }

  private static Map<String, Object> setOneOfStringOrArray() {
    Map<String, Object> oneOf = Maps.newHashMap();
    List<Map<String, Object>> listOfStringAndArray = Lists.newArrayList();
    listOfStringAndArray.add(setUriString());
    listOfStringAndArray.add(setUriStringArray());
    oneOf.put(ModelNodeNames.ONE_OF, listOfStringAndArray);
    return oneOf;
  }

  private static Map<String, Object> setUriString() {
    Map<String, Object> uriString = Maps.newHashMap();
    uriString.put(ModelNodeNames.TYPE, ModelNodeValues.STRING);
    uriString.put(ModelNodeNames.FORMAT, ModelNodeValues.URI);
    return uriString;
  }

  private static Map<String, Object> setUriStringArray() {
    Map<String, Object> uriStringArray = Maps.newHashMap();
    uriStringArray.put(ModelNodeNames.TYPE, ModelNodeValues.ARRAY);
    uriStringArray.put(ModelNodeNames.MIN_ITEMS, 1);
    uriStringArray.put(ModelNodeNames.ITEMS, setUriString());
    uriStringArray.put(ModelNodeNames.UNIQUE_ITEMS, ModelNodeValues.TRUE);
    return uriStringArray;
  }

  private Object setTypeStringOrNull() {
    Map<String, Object> typeStringOrNull = Maps.newHashMap();
    typeStringOrNull.put(ModelNodeNames.TYPE,
        Lists.newArrayList(ModelNodeValues.STRING, ModelNodeValues.NULL));
    return typeStringOrNull;
  }

  @Nullable
  public Map<String, Object> getTypeProperty() {
    Map<String, Object> toReturn = null;
    if (!typeProperty.isEmpty()) {
      toReturn = Collections.unmodifiableMap(typeProperty);
    }
    return toReturn;
  }

  @Nullable
  public Map<String, Object> getValueProperty() {
    Map<String, Object> toReturn = null;
    if (!valueProperty.isEmpty()) {
      toReturn = Collections.unmodifiableMap(valueProperty);
    }
    return toReturn;
  }

  @Nullable
  public Map<String, Object> getIdProperty() {
    Map<String, Object> toReturn = null;
    if (!idProperty.isEmpty()) {
      toReturn = Collections.unmodifiableMap(idProperty);
    }
    return toReturn;
  }

  @Nullable
  public Map<String, Object> getRdfsLabelProperty() {
    Map<String, Object> toReturn = null;
    if (!rdfsLabelProperty.isEmpty()) {
      toReturn = Collections.unmodifiableMap(rdfsLabelProperty);
    }
    return toReturn;
  }

  @Override
  public void apply(final Map<String, Object> fieldObject) {
    fieldObject.put(ModelNodeNames.PROPERTIES, setPropertiesObject());
    fieldObject.put(ModelNodeNames.REQUIRED, setRequiredList());
  }

  private Map<String, Object> setPropertiesObject() {
    Map<String, Object> properties = Maps.newHashMap();
    putIfNotNull(properties, getTypeProperty());
    putIfNotNull(properties, getValueProperty());
    putIfNotNull(properties, getIdProperty());
    putIfNotNull(properties, getRdfsLabelProperty());
    return properties;
  }

  private static void putIfNotNull(final Map<String, Object> properties, Object property) {
    if (property != null) {
      properties.putAll((Map<String, Object>) property);
    }
  }

  private List<String> setRequiredList() {
    List<String> requiredList = Lists.newArrayList();
    addIfNotNull(requiredList, ModelNodeNames.LD_VALUE, getValueProperty());
    return requiredList;
  }

  private static void addIfNotNull(final List<String> list, String item, Object value) {
    if (value != null) {
      list.add(item);
    }
  }
}
