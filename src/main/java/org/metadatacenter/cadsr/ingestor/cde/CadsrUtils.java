package org.metadatacenter.cadsr.ingestor.cde;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.metadatacenter.cadsr.cde.schema.DataElement;
import org.metadatacenter.cadsr.cde.schema.DataElementsList;
import org.metadatacenter.cadsr.ingestor.Constants;
import org.metadatacenter.cadsr.ingestor.Util;
import org.metadatacenter.cadsr.ingestor.exception.UnknownSeparatorException;
import org.metadatacenter.cadsr.ingestor.exception.UnsupportedDataElementException;
import org.metadatacenter.model.ModelNodeNames;
import org.metadatacenter.model.ModelNodeValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CadsrUtils {

  private static final Logger logger = LoggerFactory.getLogger(CadsrUtils.class);

  private static final String CEDAR_SCHEMA_VERSION = "1.5.0";

  public static Collection<Map<String, Object>> getFieldMapsFromDataElements(DataElementsList del) {
    final List<Map<String, Object>> fieldMaps = Lists.newArrayList();
    for (DataElement dataElement : del.getDataElement()) {
      try {
        final Map<String, Object> field = Maps.newHashMap();
        parseDataElement(dataElement, field);
        fieldMaps.add(field);
      } catch (UnsupportedDataElementException e) {
        logger.warn(e.getMessage());
      } catch (UnknownSeparatorException e) {
        logger.error(e.getMessage());
      }
    }
    return fieldMaps;
  }

  public static Collection<Map<String, Object>> getFieldMapsFromInputStream(InputStream is) {
    final List<Map<String, Object>> fieldMaps = Lists.newArrayList();
    try {
      DataElementsList del = getDataElementLists(is);
      fieldMaps.addAll(getFieldMapsFromDataElements(del));
    } catch (ClassCastException e) {
      logger.error("Source document is not a list of data elements: " + e);
    } catch (UnsupportedEncodingException e) {
      logger.error("Unsupported encoding: " + e);
    } catch (JAXBException | IOException e) {
      logger.error("Error while parsing source document: " + e);
    }
    return fieldMaps;
  }

  public static DataElementsList getDataElementLists(InputStream is) throws JAXBException,
      IOException {
    JAXBContext jaxbContext = JAXBContext.newInstance(DataElementsList.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    InputStream cleanIs = Util.processInvalidXMLCharacters(is);
    return (DataElementsList) jaxbUnmarshaller.unmarshal(new InputStreamReader(cleanIs, Constants.CHARSET));
  }

  public static Map<String, Object> getFieldMapFromDataElement(DataElement de) {
    final Map<String, Object> fieldMap = Maps.newHashMap();
    try {
      parseDataElement(de, fieldMap);
    } catch (UnsupportedDataElementException e) {
      logger.warn(e.getMessage());
    } catch (UnknownSeparatorException e) {
      logger.error(e.getMessage());
    }
    return fieldMap;
  }

  public static Map<String, Object> getFieldMapFromInputStream(InputStream is) {
    final Map<String, Object> fieldMap = Maps.newHashMap();
    try {
      DataElement dataElement = getDataElement(is);
      fieldMap.putAll(getFieldMapFromDataElement(dataElement));
    } catch (JAXBException e) {
      logger.error("Error while parsing source document: " + e);
    } catch (ClassCastException e) {
      logger.error("Source document is not a data element: " + e);
    } catch (UnsupportedEncodingException e) {
      logger.error("Unsupported encoding: " + e);
    }
    return fieldMap;
  }

  public static DataElement getDataElement(InputStream is) throws JAXBException, UnsupportedEncodingException {
    JAXBContext jaxbContext = JAXBContext.newInstance(DataElement.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    return (DataElement) jaxbUnmarshaller.unmarshal(new InputStreamReader(is, Constants.CHARSET));
  }

  private static void parseDataElement(DataElement dataElement, final Map<String, Object> fieldMap) throws
      UnsupportedDataElementException, UnknownSeparatorException {
    createEmptyField(fieldMap);
    setFieldIdentifier(fieldMap, dataElement.getPUBLICID().getContent());
    setFieldName(fieldMap, dataElement.getLONGNAME().getContent());
    setFieldDescription(fieldMap, dataElement.getPREFERREDDEFINITION().getContent());
    setFieldQuestions(fieldMap, dataElement, new UserQuestionsHandler());
    setInputType(fieldMap, dataElement, new InputTypeHandler());
    setPermissibleValues(fieldMap, dataElement, new PermissibleValuesHandler());
    setValueConstraints(fieldMap, dataElement, new ValueConstraintsHandler());
    setProperties(fieldMap, dataElement, new PropertiesHandler());
    setVersion(fieldMap, dataElement, new VersionHandler());
  }

  private static void setFieldIdentifier(final Map<String, Object> fieldMap, String content) {
    fieldMap.put(ModelNodeNames.SCHEMA_ORG_IDENTIFIER, content);
  }

  private static void setFieldName(final Map<String, Object> fieldMap, String content) {
    fieldMap.put(ModelNodeNames.SCHEMA_ORG_NAME, content);
    fieldMap.put(ModelNodeNames.JSON_SCHEMA_TITLE, asJsonSchemaTitle(content));
    fieldMap.put(ModelNodeNames.JSON_SCHEMA_DESCRIPTION, asJsonSchemaDescription(content));
  }

  private static Object asJsonSchemaTitle(String content) {
    return String.format("The '%s' field schema", content);
  }

  private static Object asJsonSchemaDescription(String content) {
    return String.format("The '%s' field schema auto-generated by the CEDAR/CDE Tool", content);
  }

  private static void setFieldDescription(final Map<String, Object> fieldMap, String content) {
    fieldMap.put(ModelNodeNames.SCHEMA_ORG_DESCRIPTION, content);
  }

  private static void setFieldQuestions(final Map<String, Object> fieldMap, DataElement dataElement, UserQuestionsHandler
      userQuestionsHandler) throws UnsupportedDataElementException {
    userQuestionsHandler.handle(dataElement).apply(fieldMap);
  }

  private static void setInputType(final Map<String, Object> fieldMap, DataElement dataElement, InputTypeHandler
      inputTypeHandler) throws UnsupportedDataElementException {
    inputTypeHandler.handle(dataElement).apply(fieldMap);
  }

  private static void setProperties(Map<String, Object> fieldMap, DataElement dataElement, PropertiesHandler
      propertiesHandler) throws UnsupportedDataElementException {
    propertiesHandler.handle(dataElement).apply(fieldMap);
  }

  private static void setPermissibleValues(Map<String, Object> fieldMap, DataElement dataElement, PermissibleValuesHandler
      permissibleValuesHandler) throws UnsupportedDataElementException, UnknownSeparatorException {
    permissibleValuesHandler.handle(dataElement).apply(fieldMap);
  }

  private static void setValueConstraints(Map<String, Object> fieldMap, DataElement dataElement, ValueConstraintsHandler
      valueConstraintsHandler) throws UnsupportedDataElementException {
    valueConstraintsHandler.handle(dataElement).apply(fieldMap);
  }

  private static void setVersion(Map<String, Object> fieldMap, DataElement dataElement, VersionHandler
      versionHandler) throws UnsupportedDataElementException {
    versionHandler.handle(dataElement).apply(fieldMap);
  }

  private static void createEmptyField(final Map<String, Object> fieldMap) {
    fieldMap.put(ModelNodeNames.JSON_SCHEMA_SCHEMA, ModelNodeValues.JSON_SCHEMA_IRI);
    fieldMap.put(ModelNodeNames.JSON_LD_ID, null);
    fieldMap.put(ModelNodeNames.JSON_LD_TYPE, "https://schema.metadatacenter.org/core/TemplateField");
    fieldMap.put(ModelNodeNames.JSON_LD_CONTEXT, setDefaultContext());
    fieldMap.put(ModelNodeNames.JSON_SCHEMA_TYPE, ModelNodeValues.OBJECT);
    fieldMap.put(ModelNodeNames.JSON_SCHEMA_TITLE, "");
    fieldMap.put(ModelNodeNames.JSON_SCHEMA_DESCRIPTION, "");
    fieldMap.put(ModelNodeNames.UI, setDefaultUi());
    fieldMap.put(ModelNodeNames.VALUE_CONSTRAINTS, setDefaultValueConstraints());
    fieldMap.put(ModelNodeNames.JSON_SCHEMA_PROPERTIES, null);
    fieldMap.put(ModelNodeNames.SCHEMA_ORG_NAME, "");
    fieldMap.put(ModelNodeNames.SCHEMA_ORG_DESCRIPTION, "");
    fieldMap.put(ModelNodeNames.PAV_CREATED_ON, null);
    fieldMap.put(ModelNodeNames.PAV_CREATED_BY, null);
    fieldMap.put(ModelNodeNames.PAV_LAST_UPDATED_ON, null);
    fieldMap.put(ModelNodeNames.OSLC_MODIFIED_BY, null);
    fieldMap.put(ModelNodeNames.SCHEMA_ORG_SCHEMA_VERSION, CEDAR_SCHEMA_VERSION);
    fieldMap.put(ModelNodeNames.JSON_SCHEMA_ADDITIONAL_PROPERTIES, ModelNodeValues.FALSE);
  }

  private static Map<String, Object> setDefaultContext() {
    Map<String, Object> context = Maps.newHashMap();
    context.put(ModelNodeNames.XSD, ModelNodeValues.XSD_IRI);
    context.put(ModelNodeNames.PAV, ModelNodeValues.PAV_IRI);
    context.put(ModelNodeNames.OSLC, ModelNodeValues.OSLC_IRI);
    context.put(ModelNodeNames.SCHEMA, ModelNodeValues.SCHEMA_IRI);
    context.put(ModelNodeNames.BIBO, ModelNodeValues.BIBO_IRI);
    context.put(ModelNodeNames.SKOS, ModelNodeValues.SKOS_IRI);
    context.put(ModelNodeNames.SCHEMA_ORG_NAME, setAtTypeString());
    context.put(ModelNodeNames.SCHEMA_ORG_DESCRIPTION, setAtTypeString());
    context.put(ModelNodeNames.SKOS_PREFLABEL, setAtTypeString());
    context.put(ModelNodeNames.SKOS_ALTLABEL, setAtTypeString());
    context.put(ModelNodeNames.PAV_CREATED_ON, setAtTypeDateTime());
    context.put(ModelNodeNames.PAV_CREATED_BY, setAtTypeId());
    context.put(ModelNodeNames.PAV_LAST_UPDATED_ON, setAtTypeDateTime());
    context.put(ModelNodeNames.OSLC_MODIFIED_BY, setAtTypeId());
    return context;
  }

  private static Map<String, Object> setAtTypeId() {
    Map<String, Object> typeId = Maps.newHashMap();
    typeId.put(ModelNodeNames.JSON_LD_TYPE, ModelNodeValues.LD_ID);
    return typeId;
  }

  private static Map<String, Object> setAtTypeString() {
    Map<String, Object> typeString = Maps.newHashMap();
    typeString.put(ModelNodeNames.JSON_LD_TYPE, ModelNodeValues.XSD_STRING);
    return typeString;
  }

  private static Map<String, Object> setAtTypeDateTime() {
    Map<String, Object> typeDateTime = Maps.newHashMap();
    typeDateTime.put(ModelNodeNames.JSON_LD_TYPE, ModelNodeValues.XSD_DATETIME);
    return typeDateTime;
  }

  private static Object setDefaultUi() {
    Map<String, Object> inputType = Maps.newHashMap();
    inputType.put(ModelNodeNames.UI_FIELD_INPUT_TYPE, null);
    return inputType;
  }

  private static Map<String, Object> setDefaultValueConstraints() {
    Map<String, Object> valueConstraints = Maps.newHashMap();
    valueConstraints.put(ModelNodeNames.VALUE_CONSTRAINTS_REQUIRED_VALUE, ModelNodeValues.FALSE);
    valueConstraints.put(ModelNodeNames.VALUE_CONSTRAINTS_MULTIPLE_CHOICE, ModelNodeValues.FALSE);
    return valueConstraints;
  }
}
