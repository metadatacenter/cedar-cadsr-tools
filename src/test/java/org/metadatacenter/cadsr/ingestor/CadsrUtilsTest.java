package org.metadatacenter.cadsr.ingestor;

import org.junit.Ignore;
import org.junit.Test;
import org.metadatacenter.cadsr.DataElement;
import org.metadatacenter.model.ModelNodeNames;
import org.metadatacenter.model.ModelNodeValues;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;


public class CadsrUtilsTest {

  @Ignore
  @Test
  public void shouldProduceFieldMap_ENUMERATED_CHARACTER() throws JAXBException, IOException {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-5873923.xml");
    Map<String, Object> fieldMap = CadsrUtils.getFieldMapFromDataElement(dataElement);
    // Assert
    assertThat(fieldMap.get(ModelNodeNames._SCHEMA), is(ModelNodeValues.JSON_SCHEMA_IRI));
    assertThat(fieldMap.get(ModelNodeNames.LD_ID), is(nullValue()));
    assertThat(fieldMap.get(ModelNodeNames.LD_TYPE), is("https://schema.metadatacenter.org/core/TemplateField"));
    assertThat(fieldMap.get(ModelNodeNames.TYPE), is(ModelNodeValues.OBJECT));
    assertThat(getInputType(fieldMap), is(ModelNodeValues.TEXT_FIELD));
    assertThat(getClassesConstraintMap(fieldMap).size(), is(2));
    assertThat(getClassConstraint(fieldMap, 0), is("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C49488"));
    assertThat(getClassConstraint(fieldMap, 1), is("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C49487"));
    assertThat(getProperties(fieldMap).get(ModelNodeNames.LD_TYPE), is(not(nullValue())));
    assertThat(getProperties(fieldMap).get(ModelNodeNames.LD_ID), is(not(nullValue())));
    assertThat(getProperties(fieldMap).get(ModelNodeNames.LD_VALUE), is(nullValue()));
    assertThat(getProperties(fieldMap).get(ModelNodeNames.RDFS_LABEL), is(not(nullValue())));
    assertThat(getRequiredList(fieldMap).size(), is(1));
    assertThat(getRequired(fieldMap, 0), is(ModelNodeNames.LD_ID));
  }

  @Ignore
  @Test
  public void shouldProduceFieldMap_NON_ENUMERATED_CHARACTER() throws JAXBException, IOException {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-2182451.xml");
    Map<String, Object> fieldMap = CadsrUtils.getFieldMapFromDataElement(dataElement);
    // Assert
    assertThat(fieldMap.get(ModelNodeNames._SCHEMA), is(ModelNodeValues.JSON_SCHEMA_IRI));
    assertThat(fieldMap.get(ModelNodeNames.LD_ID), is(nullValue()));
    assertThat(fieldMap.get(ModelNodeNames.LD_TYPE), is("https://schema.metadatacenter.org/core/TemplateField"));
    assertThat(fieldMap.get(ModelNodeNames.TYPE), is(ModelNodeValues.OBJECT));
    assertThat(getInputType(fieldMap), is(ModelNodeValues.TEXT_AREA));
    assertThat(getProperties(fieldMap).get(ModelNodeNames.LD_TYPE), is(not(nullValue())));
    assertThat(getProperties(fieldMap).get(ModelNodeNames.LD_ID), is(nullValue()));
    assertThat(getProperties(fieldMap).get(ModelNodeNames.LD_VALUE), is(not(nullValue())));
    assertThat(getProperties(fieldMap).get(ModelNodeNames.RDFS_LABEL), is(nullValue()));
    assertThat(getRequiredList(fieldMap).size(), is(1));
    assertThat(getRequired(fieldMap, 0), is(ModelNodeNames.LD_VALUE));
  }

  @Test
  public void shouldProduceFieldMap_NON_ENUMERATED_DATE() throws JAXBException, IOException {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-2001826.xml");
    Map<String, Object> fieldMap = CadsrUtils.getFieldMapFromDataElement(dataElement);
    // Assert
    assertThat(fieldMap.get(ModelNodeNames._SCHEMA), is(ModelNodeValues.JSON_SCHEMA_IRI));
    assertThat(fieldMap.get(ModelNodeNames.LD_ID), is(nullValue()));
    assertThat(fieldMap.get(ModelNodeNames.LD_TYPE), is("https://schema.metadatacenter.org/core/TemplateField"));
    assertThat(fieldMap.get(ModelNodeNames.TYPE), is(ModelNodeValues.OBJECT));
    assertThat(getInputType(fieldMap), is(ModelNodeValues.DATE));
    assertThat(getProperties(fieldMap).get(ModelNodeNames.LD_TYPE), is(not(nullValue())));
    assertThat(getProperties(fieldMap).get(ModelNodeNames.LD_ID), is(nullValue()));
    assertThat(getProperties(fieldMap).get(ModelNodeNames.LD_VALUE), is(not(nullValue())));
    assertThat(getProperties(fieldMap).get(ModelNodeNames.RDFS_LABEL), is(nullValue()));
    assertThat(getRequiredList(fieldMap).size(), is(1));
    assertThat(getRequired(fieldMap, 0), is(ModelNodeNames.LD_VALUE));
  }

  @Test
  public void shouldProduceFieldMap_NON_ENUMERATED_NUMBER() throws JAXBException, IOException {
    DataElement dataElement = FileUtils.readDataElementResource("cde-sample-2002061.xml");
    Map<String, Object> fieldMap = CadsrUtils.getFieldMapFromDataElement(dataElement);
    // Assert
    assertThat(fieldMap.get(ModelNodeNames._SCHEMA), is(ModelNodeValues.JSON_SCHEMA_IRI));
    assertThat(fieldMap.get(ModelNodeNames.LD_ID), is(nullValue()));
    assertThat(fieldMap.get(ModelNodeNames.LD_TYPE), is("https://schema.metadatacenter.org/core/TemplateField"));
    assertThat(fieldMap.get(ModelNodeNames.TYPE), is(ModelNodeValues.OBJECT));
    assertThat(getInputType(fieldMap), is(ModelNodeValues.NUMERIC));
    assertThat(getProperties(fieldMap).get(ModelNodeNames.LD_TYPE), is(not(nullValue())));
    assertThat(getProperties(fieldMap).get(ModelNodeNames.LD_ID), is(nullValue()));
    assertThat(getProperties(fieldMap).get(ModelNodeNames.LD_VALUE), is(not(nullValue())));
    assertThat(getProperties(fieldMap).get(ModelNodeNames.RDFS_LABEL), is(nullValue()));
    assertThat(getRequiredList(fieldMap).size(), is(1));
    assertThat(getRequired(fieldMap, 0), is(ModelNodeNames.LD_VALUE));
  }

  private static String getInputType(Map<String, Object> fieldMap) {
    Map<String, Object> uiMap = (Map<String, Object>) fieldMap.get(ModelNodeNames.UI);
    return (String) uiMap.get(ModelNodeNames.INPUT_TYPE);
  }

  private List<Map<String, Object>> getClassesConstraintMap(Map<String, Object> fieldMap) {
    Map<String, Object> valueConstraints = (Map<String, Object>) fieldMap.get(ModelNodeNames.VALUE_CONSTRAINTS);
    return (List<Map<String, Object>>) valueConstraints.get(ModelNodeNames.CLASSES);
  }

  private String getClassConstraint(Map<String, Object> fieldMap, int i) {
    Map<String, Object> classConstraint = (Map<String, Object>) getClassesConstraintMap(fieldMap).get(i);
    return (String) classConstraint.get(ModelNodeNames.URI);
  }

  private Map<String, Object> getProperties(Map<String, Object> fieldMap) {
    return (Map<String, Object>) fieldMap.get(ModelNodeNames.PROPERTIES);
  }

  private List<String> getRequiredList(Map<String, Object> fieldMap) {
    return (List<String>) fieldMap.get(ModelNodeNames.REQUIRED);
  }

  private String getRequired(Map<String, Object> fieldMap, int i) {
    return getRequiredList(fieldMap).get(i);
  }
}
