package org.metadatacenter.cadsr.ingestor.cde.handler;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.metadatacenter.cadsr.cde.schema.CLASSIFICATIONSLIST;
import org.metadatacenter.cadsr.cde.schema.DataElement;
import org.metadatacenter.cadsr.cde.schema.REFERENCEDOCUMENTSLIST;
import org.metadatacenter.model.ModelNodeNames;

import java.util.Map;
import java.util.Set;

public class CategoriesHandler implements ModelHandler {

//  private static final String PREFERRED_QUESTION_TEXT = "Preferred Question Text";
//  private static final String ALTERNATE_QUESTION_TEXT = "Alternate Question Text";

  private String preferredQuestion;
  private Set<String> alternateQuestions = Sets.newHashSet();

  public CategoriesHandler handle(DataElement dataElement) {

//    final CLASSIFICATIONSLIST classificationsList = dataElement.getCLASSIFICATIONSLIST();
//
//    if (classificationsList != null) {
//      classificationsList.getCLASSIFICATIONSLISTITEM().stream().forEach(item -> {
//        item.
//
//
//
//      });
//    }
//
//
//    final REFERENCEDOCUMENTSLIST referenceDocumentList = dataElement.getREFERENCEDOCUMENTSLIST();
//    if (referenceDocumentList != null) {
//      referenceDocumentList.getREFERENCEDOCUMENTSLISTITEM().stream().forEach(item -> {
//        String documentType = item.getDocumentType().getContent();
//        if (PREFERRED_QUESTION_TEXT.equals(documentType)) {
//          preferredQuestion = item.getDocumentText().getContent();
//        } else if (ALTERNATE_QUESTION_TEXT.equals(documentType)) {
//          alternateQuestions.add(item.getDocumentText().getContent());
//        }
//      });
//    }
//    return this;
    return null;
  }

  @Override
  public void apply(Map<String, Object> fieldObject) {
    if (!Strings.isNullOrEmpty(preferredQuestion)) {
      fieldObject.put(ModelNodeNames.SKOS_PREFLABEL, preferredQuestion);
    }
    if (!alternateQuestions.isEmpty()) {
      fieldObject.put(ModelNodeNames.SKOS_ALTLABEL, alternateQuestions);
    }
  }
}

