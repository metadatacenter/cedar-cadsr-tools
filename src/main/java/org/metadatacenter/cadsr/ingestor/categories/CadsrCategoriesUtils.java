package org.metadatacenter.cadsr.ingestor.categories;

import org.metadatacenter.cadsr.CLASSIFICATIONSLISTCATEGORIES;
import org.metadatacenter.cadsr.CLASSIFICATIONSLISTITEM;
import org.metadatacenter.cadsr.ingestor.Constants;
import org.metadatacenter.cadsr.ingestor.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CadsrCategoriesUtils {

  private static final Logger logger = LoggerFactory.getLogger(CadsrCategoriesUtils.class);

  public static CLASSIFICATIONSLISTCATEGORIES getClassificationsList(InputStream is) throws JAXBException,
      IOException {
    // Note that CLASSIFICATIONSLISTCATEGORIES is a class name that we created to diferentiate the categories extracted
    // from the categories file that the NCI sent us from the CLASSIFICATIONSLIST elements extracted from the original
    // caDSR CDEs XML file
    JAXBContext jaxbContext = JAXBContext.newInstance(CLASSIFICATIONSLISTCATEGORIES.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    InputStream cleanIs = Util.processInvalidXMLCharacters(is);
    return (CLASSIFICATIONSLISTCATEGORIES) jaxbUnmarshaller.unmarshal(new InputStreamReader(cleanIs, Constants.CHARSET));
  }

  public static List<Category> classificationsListToCategoryList(CLASSIFICATIONSLISTCATEGORIES rootClc) {

    List<Category> categories = new ArrayList<>();

    for (CLASSIFICATIONSLISTCATEGORIES.CLASSIFICATIONSLISTITEM classificationListItem : rootClc.getCLASSIFICATIONSLISTITEM()) {

      for (CLASSIFICATIONSLISTCATEGORIES.CLASSIFICATIONSLISTITEM.ClassificationsList.ClassificationsListITEM cli : classificationListItem.getClassificationsList().getClassificationsListITEM()) {

        // Level 1 (root categories)
        String name1 = cli.getClassificationScheme().getContextName();
        String id1 = "CTX-" + cli.getClassificationScheme().getContextName() + "v"
            + cli.getClassificationScheme().getContextVersion();
        String description1 = null;
        Category category1 = new Category(id1, name1, description1, Constants.ROOT_CATEGORY_KEY);
        if (!categories.contains(category1)) {
          categories.add(category1);
        }

        // Level 2
        String name2 = cli.getClassificationScheme().getPreferredName();
        String id2 = "CS-" + cli.getClassificationScheme().getPublicId() + "v" + cli.getClassificationScheme().getVersion();
        String description2 = null;
        Category category2 = new Category(id2, name2, description2, id1);
        if (!categories.contains(category2)) {
          categories.add(category2);
        }

        // Level 3
        String name3 = cli.getClassificationSchemeItemName();
        String id3 = "CSI-" + cli.getCsiPublicId() + "v" + cli.getCsiVersion();
        String description3 = null;
        Category category3 = new Category(id3, name3, description3, id2);
        if (!categories.contains(category3)) {
          categories.add(category3);
        }

      }
    }
    return categories;
  }


}
