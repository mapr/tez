/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tez.conftool;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 CLI manager to configure Tez components.
 */
public final class ConfCli {
  private ConfCli() {
  }

  private static final Options CMD_LINE_OPTIONS = new Options();
  private static final HelpFormatter HELP_FORMATTER = new HelpFormatter();
  private static final String HELP = "help";
  private static final String TOOL_NAME = "conftool";
  private static final String PATH = "path";
  private static final String SECURITY = "security";

  static {
    OptionBuilder.hasArg(false);
    OptionBuilder.withDescription("Print help information");
    CMD_LINE_OPTIONS.addOption(OptionBuilder.create(HELP));

    OptionBuilder.hasArg();
    OptionBuilder.withArgName("true or false for security");
    OptionBuilder.withDescription("Shows current status of security");
    CMD_LINE_OPTIONS.addOption(OptionBuilder.create(SECURITY));

    OptionBuilder.hasArg();
    OptionBuilder.withArgName("path to xml file");
    OptionBuilder
        .withDescription("Path to xml file to configure tez-site.xml.");
    CMD_LINE_OPTIONS.addOption(OptionBuilder.create(PATH));
  }

  /**
   CLI entry point. Handle args as array of options with option values.
   */
  public static void main(String[] args)
      throws IOException, ParserConfigurationException, SAXException,
      TransformerException {
    CommandLineParser cmdParser = new GnuParser();
    CommandLine line;
    try {
      line = cmdParser.parse(CMD_LINE_OPTIONS, args);
    } catch (ParseException e) {
      printHelp();
      throw new IllegalArgumentException(
          TOOL_NAME + ": Parsing failed.  Reason: " + e.getLocalizedMessage());
    }
    if (line == null) {
      throw new IllegalArgumentException(
          TOOL_NAME + ": parsing failed.  Reason: unknown");
    }
    if (line.hasOption(HELP)) {
      printHelp();
    } else if (line.hasOption(PATH)) {
      String pathToXmlFile = line.getOptionValue(PATH);
      if (line.hasOption(SECURITY)) {
        configureSecurity(pathToXmlFile, getSecurity(line));
      }
    } else {
      printHelp();
    }
  }

  /**
   CLI help, returns list of possible options.
   */
  private static void printHelp() {
    HELP_FORMATTER.printHelp(TOOL_NAME, CMD_LINE_OPTIONS);
  }

  /**
   Configuration of security properties based on CLI arguments.
   */
  private static void configureSecurity(String pathToTezSite, Security security)
      throws IOException, ParserConfigurationException, SAXException,
      TransformerException {
    ConfTool.setEncryption(pathToTezSite, security);
  }

  private static boolean isTrueOrFalseOrCustom(String value) {
    for (Security security : Security.values()) {
      if (security.value().equalsIgnoreCase(value.trim())) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasValidSecurityOptions(CommandLine line) {
    return line.hasOption(SECURITY) && isTrueOrFalseOrCustom(
        line.getOptionValue(SECURITY));
  }

  private static Security getSecurity(CommandLine line) {
    if (hasValidSecurityOptions(line)) {
      return Security.parse(line.getOptionValue(SECURITY));
    } else {
      printHelp();
      throw new IllegalArgumentException(
          "Incorrect security configuration options");
    }
  }
}
