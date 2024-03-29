package org.metadatacenter.cadsr.ingestor.form.handler;

import org.metadatacenter.cadsr.form.schema.Form;
import org.metadatacenter.cadsr.form.schema.Module;
import org.metadatacenter.cadsr.form.schema.Question;
import org.metadatacenter.cadsr.form.schema.ValidValue;
import org.metadatacenter.cadsr.ingestor.form.FormParseReporter;
import org.metadatacenter.cadsr.ingestor.form.FormUtil;
import org.metadatacenter.cadsr.ingestor.util.CedarFieldUtil;
import org.metadatacenter.cadsr.ingestor.util.CedarServices;
import org.metadatacenter.cadsr.ingestor.util.Constants.CedarServer;
import org.metadatacenter.cadsr.ingestor.util.GeneralUtil;
import org.metadatacenter.model.ModelNodeNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static org.metadatacenter.cadsr.ingestor.util.Constants.LARGE_PAGE_SIZE;
import static org.metadatacenter.cadsr.ingestor.util.Constants.MOVE_ACTIONS_THRESHOLD;
import static org.metadatacenter.model.ModelNodeNames.*;

public class TemplateFieldsHandler implements ModelHandler {

  private static final Logger logger = LoggerFactory.getLogger(TemplateFieldsHandler.class);
  private CedarServer cedarServer;
  private String apiKey;
  private List<Map<String, Object>> fields = new ArrayList<>();
  private String reportId;

  public TemplateFieldsHandler(CedarServer cedarServer, String apiKey, String reportId) {
    this.cedarServer = cedarServer;
    this.apiKey = apiKey;
    this.reportId = reportId;
  }

  public TemplateFieldsHandler handle(Form form) throws IOException {
    List<Module> modules = form.getModule();
    sortModules(modules);
    for (Module module : modules) {
      handleModule(module);
    }
    return this;
  }

  private void handleModule(Module module) throws IOException {
    handleModuleInfo(module);
    List<Question> questions = module.getQuestion();
    sortQuestions(questions);
    for (Question question : questions) {
      handleQuestion(question);
    }
  }

  private void handleModuleInfo(Module module) {
    if (!GeneralUtil.isNullOrEmpty(module.getLongName())) {
      FormParseReporter.getInstance().addMessage(reportId, "Added section: " + module.getLongName());
      Map<String, Object> sectionBreak = CedarFieldUtil.generateDefaultSectionBreak(module.getLongName(), "",
          cedarServer);
      fields.add(sectionBreak);
    }
  }

  private void handleQuestion(Question question) throws IOException {
    if (question.getDataElement() != null) {
      Optional<Map<String, Object>> result =
          CedarServices.searchCdeByPublicIdAndVersion(question.getDataElement().getPublicID(),
              question.getDataElement().getVersion(), cedarServer, apiKey);
      if (result.isPresent()) {
        Map<String, Object> cde = result.get();
        cde = customizeCde(cde, question);
        fields.add(cde);
        FormParseReporter.getInstance().addMessage(reportId, "Added CDE-based field. (PublicId: "
            + question.getDataElement().getPublicID() + "; Version: " + question.getDataElement().getVersion() + ")");
      } else {
        FormParseReporter.getInstance().addMessage(reportId, "CDE-based field not found in CEDAR. Ignored. (PublicId: "
            + question.getDataElement().getPublicID() + "; Version: " + question.getDataElement().getVersion() + ")");
        logger.warn("CDE not found: PublicId: " + question.getDataElement().getPublicID() + " ; Version: " + question.getDataElement().getVersion());
      }
    } else {
      FormParseReporter.getInstance().addMessage(reportId, "Field does not have an associated CDE. Ignored. (PublicId: "
          + question.getPublicID() + ")");
      logger.info("The question does not have an associated data element. Question publicId: " + question.getPublicID());
    }
  }

  private Map<String, Object> customizeCde(Map<String, Object> cde, Question question) {
    cde = customizeCdePrefLabel(cde, question.getQuestionText());

    if (question.getValidValue().size() > 0) {
      try {
        cde = customizeCdeValues(cde, question.getValidValue());
      } catch (IOException | RuntimeException e) {
        FormParseReporter.getInstance().addMessage(reportId, "Error customizing values: " + e.getMessage());
        logger.error(e.getMessage());
      }
    }

    return cde;
  }

  private Map<String, Object> customizeCdePrefLabel(Map<String, Object> cde, String prefLabel) {
    String originalPrefLabel = (String) cde.get(SKOS_PREFLABEL);
    if (!GeneralUtil.isNullOrEmpty(prefLabel) && !prefLabel.equals(originalPrefLabel)) {
      logger.info("Replacing prefLabel: " + originalPrefLabel + " -> " + prefLabel);
      cde.replace(SKOS_PREFLABEL, prefLabel);
    }
    return cde;
  }

  // If the CDE is constrained to values from a BioPortal value set, customize those values using the information
  // from the form's xml
  private Map<String, Object> customizeCdeValues(Map<String, Object> cde, List<ValidValue> validValues) throws IOException, RuntimeException {

    if (cde.containsKey("_valueConstraints")
        && ((Map<String, Object>) cde.get("_valueConstraints")).containsKey("valueSets")
        && ((List) ((Map<String, Object>) cde.get("_valueConstraints")).get("valueSets")).size() > 0) {

      // 1. Retrieve all the values from BioPortal
      List<Map<String, String>> cdeValues = new ArrayList<>();
      List<Map<String, String>> values;
      int page = 1;
      do {
        Map<String, Object> result =
            CedarServices.integratedSearch((Map<String, Object>) cde.get("_valueConstraints"), page, LARGE_PAGE_SIZE,
                cedarServer, apiKey);
        values = result.containsKey("collection") ? (List<Map<String, String>>) result.get("collection") :
            new ArrayList<>();
        if (values.size() > 0) {
          cdeValues.addAll(values);
          page++;
        }
      } while (values.size() > 0);

      // If the displayOrder specified in the valid values matches the order of the values retrieved from BioPortal,
      // we won't need to generate move actions. In many cases, the valid values are ordered alphabetically, and we will
      // also sort them alphabetically in the UI, so we need to make that comparison. We cannot retrieve the values
      // alphabetically sorted by BioPortal, so we sort them here. We will compare their position to the valid values
      // display order later (see generateMoveActions method) to determine if it's necessary to generate move actions.
      cdeValues.sort((v1, v2) -> v1.get("notation").compareTo(v2.get("notation")));

      // Create Map with the values indexed by prefLabel to access them quickly. We save the position as a new
      // attribute. We'll use it to generate the move actions
      Map<String, Map<String, String>> cdeValuesMap = new HashMap<>();

      int position = 0;
      for (Map<String, String> cdeValue : cdeValues) {
        cdeValue.put("position", Integer.toString(position));
        if (cdeValue.get("notation") != null && cdeValue.get("notation").length() > 0) {
          cdeValuesMap.put(cdeValue.get("notation").toLowerCase(), cdeValue);
        }
        position++;
      }

      // 2. Compare (ignoring case) the retrieved values to the values in the form's XML to identify exclusions
      // Correspondences between CEDAR's CDE model and the form's xml validValue model:
      //  - notation <-> value
      Map<String, Map<String, String>> excludedCdeValuesMap = new HashMap<>(cdeValuesMap);
      for (ValidValue validValue : validValues) {
        String valueKey = validValue.getValue().toLowerCase();
        if (excludedCdeValuesMap.containsKey(valueKey)) {
          excludedCdeValuesMap.remove(valueKey);
          logger.info("Including value: " + valueKey);
        }
      }

      // The remaining values are the ones that we need to create exclusions for
      Map<String, String> valueSetConstraint = ((List<Map<String, String>>) ((Map<String, Object>) cde.get(
          "_valueConstraints")).get("valueSets")).get(0);
      String valueDomainUri = valueSetConstraint.get("uri");
      // We extracted the valueDomainUri from the original valueSet constraints definition because it's not returned
      // by the integrated search endpoint and we'll need it to generate the actions. This approach won't work if
      // the valueSets array contains more than one value set. However, we shouldn't run into any issues since we only
      // associate CDEs to one value set at maximum (i.e., only one element in the _valueConstraints.valueSets array).
      List<Map<String, Object>> deleteActions = generateDeleteActions(new ArrayList(excludedCdeValuesMap.values()),
          valueDomainUri);
      List<Map<String, Object>> moveActions = generateMoveActions(validValues, cdeValuesMap, valueDomainUri,
          MOVE_ACTIONS_THRESHOLD);

      List<Map<String, Object>> actions = new ArrayList<>();
      actions.addAll(deleteActions);
      actions.addAll(moveActions);

      ((Map<String, Object>) cde.get("_valueConstraints")).put("actions", actions);
    }
    return cde;
  }

  private List<Map<String, String>> generateDeleteActions(List<Map<String, String>> values, String valueDomainUri) {
    List<Map<String, String>> deleteActions = new ArrayList<>();
    for (Map<String, String> value : values) {
      Map<String, String> deleteAction = new HashMap<>();
      deleteAction.put("termUri", value.get("@id"));
      deleteAction.put("sourceUri", valueDomainUri);
      deleteAction.put("source", value.get("source").substring(value.get("source").lastIndexOf("/") + 1));
      deleteAction.put("type", "Value");
      deleteAction.put("action", "delete");
      deleteActions.add(deleteAction);
    }
    return deleteActions;
  }

  private List<Map<String, Object>> generateMoveActions(List<ValidValue> validValues,
                                                        Map<String, Map<String, String>> cdeValuesMap,
                                                        String valueDomainUri, int generationThreshold) {
    List<Map<String, Object>> moveActions = new ArrayList<>();
    for (ValidValue validValue : validValues) {
      if (validValue.getDisplayOrder() != null) {
        String valueKey = validValue.getValue().toLowerCase();
        if (cdeValuesMap.containsKey(valueKey)) {
          Map<String, String> cdeValue = cdeValuesMap.get(valueKey);

          int currentPosition = Integer.parseInt(cdeValue.get("position"));
          int targetPosition = Integer.parseInt(validValue.getDisplayOrder());

          if (targetPosition != currentPosition) {
            Map<String, Object> moveAction = new HashMap<>();
            moveAction.put("to", targetPosition + 1); // The order of our actions is 1-based
            moveAction.put("termUri", cdeValue.get("@id"));
            moveAction.put("sourceUri", valueDomainUri);
            moveAction.put("source", cdeValue.get("source").substring(cdeValue.get("source").lastIndexOf("/") + 1));
            moveAction.put("type", "Value");
            moveAction.put("action", "move");
            moveActions.add(moveAction);
          }
        }
      }
    }

    // To avoid generating a large number of move actions, which the current UI won't be able to handle at this point,
    // since it makes an extra call to BioPortal per action, and BioPortal returns 429s after some requests.
    if (moveActions.size() <= generationThreshold) {
      return moveActions;
    } else {
      return new ArrayList<>();
    }
  }

  private Map<String, Object> getUpdatedUi(String fieldName, String fieldDescription, Map<String, Object> templateMap) {
    Map<String, Object> ui = (Map<String, Object>) templateMap.get(ModelNodeNames.UI);
    // Update order
    ((List<String>) ui.get(ModelNodeNames.UI_ORDER)).add(fieldName);
    // Update property labels
    ((Map<String, String>) ui.get(ModelNodeNames.UI_PROPERTY_LABELS)).put(fieldName, fieldName);
    // Update property descriptions
    ((Map<String, String>) ui.get(ModelNodeNames.UI_PROPERTY_DESCRIPTIONS)).put(fieldName, fieldDescription);
    return ui;
  }

  private Map<String, Object> getUpdatedPropertiesContextProperties(String fieldName, Map<String, Object> templateMap) {
    Map<String, Object> properties = (Map<String, Object>) templateMap.get(JSON_SCHEMA_PROPERTIES);
    Map<String, Object> propertiesContext = (Map<String, Object>) properties.get(JSON_LD_CONTEXT);
    Map<String, Object> propertiesContextProperties = (Map<String, Object>) propertiesContext.get(JSON_SCHEMA_PROPERTIES);
    propertiesContextProperties.put(fieldName, new HashMap<String, List<String>>() {{
      put(JSON_SCHEMA_ENUM, List.of("https://schema.metadatacenter.org/properties/" + UUID.randomUUID()));
    }});
    return propertiesContextProperties;
  }

  private List<String> getUpdatedPropertiesContextRequired(String fieldName, Map<String, Object> templateMap) {
    Map<String, Object> properties = (Map<String, Object>) templateMap.get(JSON_SCHEMA_PROPERTIES);
    Map<String, Object> propertiesContext = (Map<String, Object>) properties.get(JSON_LD_CONTEXT);
    List<String> propertiesContextRequired = (List<String>) propertiesContext.get(JSON_SCHEMA_REQUIRED);
    if (!propertiesContextRequired.contains(fieldName)) {
      propertiesContextRequired.add(fieldName);
    }
    return propertiesContextRequired;
  }

  private List<String> getUpdatedRequired(String fieldName, Map<String, Object> templateMap) {
    List<String> required = new ArrayList<>((List<String>) templateMap.get(JSON_SCHEMA_REQUIRED));
    if (!required.contains(fieldName)) {
      required.add(fieldName);
    }
    return required;
  }

  /**
   * Sort modules based on their displayOrder. We consider that null is greater than non-null (nullsLast policy)
   *
   * @param modules
   */
  private void sortModules(List<Module> modules) {
    modules.sort((m1, m2) -> {
      if (m1.getDisplayOrder() == null && m2.getDisplayOrder() == null) {
        return 0;
      } else if (m1.getDisplayOrder() == null) {
        return 1;
      } else if (m2.getDisplayOrder() == null) {
        return -1;
      }
      return Integer.compare(Integer.parseInt(m1.getDisplayOrder()), Integer.parseInt(m2.getDisplayOrder()));
    });
  }

  /**
   * Sort questions based on their displayOrder. We consider that null is greater than non-null (nullsLast policy)
   *
   * @param questions
   */
  private void sortQuestions(List<Question> questions) {
    questions.sort((q1, q2) -> {
      if (q1.getDisplayOrder() == null && q2.getDisplayOrder() == null) {
        return 0;
      } else if (q1.getDisplayOrder() == null) {
        return 1;
      } else if (q2.getDisplayOrder() == null) {
        return -1;
      }
      return Integer.compare(Integer.parseInt(q1.getDisplayOrder()), Integer.parseInt(q2.getDisplayOrder()));
    });
  }

  @Override
  public void apply(Map<String, Object> templateMap) { // Add all fields to the template
    for (Map<String, Object> field : fields) {
      String fieldName = FormUtil.preprocessFieldName((String) field.get(ModelNodeNames.SCHEMA_ORG_NAME));
      String fieldDescription = (String) field.get(ModelNodeNames.SCHEMA_ORG_DESCRIPTION);
      String fieldInputType = ((Map<String, String>) field.get(UI)).get(UI_FIELD_INPUT_TYPE);
      // Update _ui
      templateMap.replace(ModelNodeNames.UI, getUpdatedUi(fieldName, fieldDescription, templateMap));
      // Update properties
      ((Map<String, Object>) templateMap.get(JSON_SCHEMA_PROPERTIES)).put(fieldName, field);

      if (!fieldInputType.equals(FIELD_INPUT_TYPE_SECTION_BREAK)) { // If the field is not a section break
        // Update properties.@context.properties
        ((Map<String, Object>) ((Map<String, Object>) templateMap.get(JSON_SCHEMA_PROPERTIES)).get(JSON_LD_CONTEXT)).
            replace(JSON_SCHEMA_PROPERTIES, getUpdatedPropertiesContextProperties(fieldName, templateMap));
        // Update properties.@context.required
        ((Map<String, Object>) ((Map<String, Object>) templateMap.get(JSON_SCHEMA_PROPERTIES)).get(JSON_LD_CONTEXT)).
            replace(JSON_SCHEMA_REQUIRED, getUpdatedPropertiesContextRequired(fieldName, templateMap));
        // Update required
        templateMap.replace(JSON_SCHEMA_REQUIRED, getUpdatedRequired(fieldName, templateMap));
      }
    }
  }


}
