package org.metadatacenter.cadsr.ingestor.cde.handler;

import org.metadatacenter.cadsr.cde.schema.CLASSIFICATIONSLIST;
import org.metadatacenter.cadsr.cde.schema.DataElement;
import org.metadatacenter.cadsr.ingestor.util.Constants;
import org.metadatacenter.cadsr.ingestor.util.CategoryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CategoriesHandler implements ModelHandler {

  private List<String> categoryIds = new ArrayList<>();

  public CategoriesHandler handle(DataElement dataElement) {

    final CLASSIFICATIONSLIST classificationsList = dataElement.getCLASSIFICATIONSLIST();
    if (classificationsList != null) {
      classificationsList.getCLASSIFICATIONSLISTITEM().stream().forEach(item -> {

        String ctxName =  item.getClassificationScheme().getContextName().getContent();
        String ctxVersion = item.getClassificationScheme().getContextVersion().getContent();
        String ctxLocalId = CategoryUtil.generateCategoryLocalId(ctxName, Optional.empty(), ctxVersion);

        String csName = item.getClassificationScheme().getPreferredName().getContent();
        String csPublicId = item.getClassificationScheme().getPublicId().getContent();
        String csVersion = item.getClassificationScheme().getVersion().getContent();
        String csLocalId = CategoryUtil.generateCategoryLocalId(csName, Optional.of(csPublicId), csVersion);

        String csiName = item.getClassificationSchemeItemName().getContent();
        String csiPublicId = item.getCsiPublicId().getContent();
        String csiVersion= item.getCsiVersion().getContent();
        String categoryId = CategoryUtil.generateCategoryLocalId(csiName,
            Optional.of(csiPublicId), csiVersion);

        String cadsrCategoryId = CategoryUtil.generateCadsrCategoryId(categoryId, Optional.of(ctxLocalId), Optional.of(csLocalId));

        if (!categoryIds.contains(cadsrCategoryId)) {
          categoryIds.add(cadsrCategoryId);
        }

      });
    }
    return this;
  }

  @Override
  public void apply(Map<String, Object> fieldObject) {
    fieldObject.put(Constants.CDE_CATEGORY_IDS_FIELD, categoryIds);
  }
}

