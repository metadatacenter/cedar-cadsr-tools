package org.metadatacenter.cadsr.ingestor.tools.form.config;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigSettingsParser {

  private static final Logger logger = LoggerFactory.getLogger(ConfigSettingsParser.class);

  public static ConfigSettings parse(String[] args) {
    try {
      return parseOrRefuse(args);
    } catch (ParseException e) {
      logger.error("Error parsing command-line arguments: " + e.getMessage());
      logger.info("Please, follow the instructions below:");
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp( "mvn exec:java@cedar-cadsr-form-import -Dexec.args=\"[options]\"\nOptions:", options());
      System.exit(1);
      return null;
    }
  }

  /** Reads the arguments into settings, and refuses with a {@link ParseException} a list without the form's path. */
  static ConfigSettings parseOrRefuse(String[] args) throws ParseException {
    ConfigSettings settings = new ConfigSettings();
    CommandLine cmd = new DefaultParser().parse(options(), args);
    if (cmd.hasOption("f")) {
      settings.setFormFilePath(cmd.getOptionValue("f"));
    }
    return settings;
  }

  private static Options options() {
    Options options = new Options();
    options.addRequiredOption("f", "form", true, "[REQUIRED] Path of the .xml file with the caDSR Form to be imported into CEDAR");
    return options;
  }

}
