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

import com.google.common.annotations.VisibleForTesting;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;

/**
 *  Helper for configuring Tez via tez-site.xml.
 */

final class ConfTool {
  private ConfTool() {
  }

  public static final String KEEP_ALIVE =
      "tez.runtime.shuffle.keep-alive.enabled";
  public static final String SSL = "tez.runtime.shuffle.ssl.enable";

  private static final Logger LOG = Logger.getLogger(ConfTool.class);
  private static final String NAME = "name";
  private static final String VALUE = "value";
  private static final String PROPERTY = "property";
  private static final String CONFIGURATION = "configuration";
  private static final String TRUE = "true";
  private static final String EMPTY = "";

  /**
   * Configures Shuffle SSL encryption for Tez.
   *
   * @param pathToTezSite tez-site location
   * @param security true if Mapr Sasl security is enabled on the cluster
   * @throws TransformerException
   * @throws IOException
   * @throws SAXException
   * @throws ParserConfigurationException
   */

  static void setEncryption(String pathToTezSite, Security security)
      throws TransformerException, IOException, SAXException,
      ParserConfigurationException {
    Document doc = readDocument(pathToTezSite);
    LOG.info(
        String.format("Reading tez-site.xml from path : %s", pathToTezSite));
    switch (security) {
    case CUSTOM:
      return;
    case MAPRSASL:
      set(doc, SSL, TRUE);
      set(doc, KEEP_ALIVE, TRUE);
      break;
    case NONE:
      removeProperty(doc, SSL);
      removeProperty(doc, KEEP_ALIVE);
      break;
    default:
      return;
    }
    saveToFile(doc, pathToTezSite);
  }

  @VisibleForTesting
  static void set(Document doc, String property, String value) {
    if (propertyExists(doc, property)) {
      LOG.info(String.format("Property %s exists in xml file", property));
      setProperty(doc, property, value);
    } else {
      LOG.info(
          String.format("Property %s does not exist in xml file", property));
      addProperty(doc, property, value);
    }
  }

  private static void addProperty(Document doc, String property, String value) {
    LOG.info(String
        .format("Adding property to tez-site.xml: %s = %s", property, value));
    Element element = doc.createElement(PROPERTY);
    addName(doc, element, property);
    addValue(doc, element, value);
    getConfigurationNode(doc).appendChild(element);
  }

  static void removeProperty(Document doc, String property) {
    LOG.info(
        String.format("Removing property from tez-site.xml: %s", property));
    Node configuration = getConfigurationNode(doc);
    NodeList childNodes = configuration.getChildNodes();
    int length = childNodes.getLength();
    for (int i = 0; i <= length - 1; i++) {
      Node node = childNodes.item(i);
      NodeList nameValueDesc = node.getChildNodes();
      int childLength = nameValueDesc.getLength();
      for (int j = 0; j <= childLength - 1; j++) {
        Node childNode = nameValueDesc.item(j);
        if (NAME.equals(childNode.getNodeName()) && property
            .equals(childNode.getTextContent())) {
          //Remove the new line text node that stands after node we need to remove.
          //Without this step removing will produce empty line.
          if (node.getNextSibling() != null
              && node.getNextSibling().getNodeType() == Node.TEXT_NODE && node
              .getNextSibling().getNodeValue().trim().isEmpty()) {
            configuration.removeChild(node.getNextSibling());
          }
          configuration.removeChild(node);
          return;
        }
      }
    }
  }

  private static void setProperty(Document doc, String property, String value) {
    LOG.info(String
        .format("Setting value to existing property in xml file: %s = %s",
            property, value));
    Node configuration = getConfigurationNode(doc);
    NodeList properties = configuration.getChildNodes();
    int length = properties.getLength();
    for (int i = 0; i <= length - 1; i++) {
      Node node = properties.item(i);
      NodeList nameValueDesc = node.getChildNodes();
      int childLength = nameValueDesc.getLength();
      for (int j = 0; j <= childLength - 1; j++) {
        Node childNode = nameValueDesc.item(j);
        if (NAME.equals(childNode.getNodeName()) && property
            .equals(childNode.getTextContent())) {
          writeValue(nameValueDesc, value);
        }
      }
    }
  }

  private static Node getConfigurationNode(Document doc) {
    NodeList nodes = doc.getChildNodes();
    int length = nodes.getLength();
    for (int i = 0; i <= length - 1; i++) {
      Node node = nodes.item(i);
      if (CONFIGURATION.equals(node.getNodeName())) {
        return node;
      }
    }
    throw new IllegalArgumentException("No <configuration> tag");
  }

  private static void addName(Document doc, Node node, String property) {
    Element element = doc.createElement(NAME);
    element.appendChild(doc.createTextNode(property));
    node.appendChild(element);
  }

  private static void addValue(Document doc, Node property, String value) {
    Element name = doc.createElement(VALUE);
    name.appendChild(doc.createTextNode(value));
    property.appendChild(name);
  }

  private static boolean propertyExists(Document doc, String property) {
    LOG.info(String.format("Checking that property exists in tez-site.xml : %s",
        property));
    Node configuration = getConfigurationNode(doc);
    NodeList properties = configuration.getChildNodes();
    int length = properties.getLength();
    for (int i = 0; i <= length - 1; i++) {
      Node node = properties.item(i);
      NodeList nameValueDesc = node.getChildNodes();
      int childLength = nameValueDesc.getLength();
      for (int j = 0; j <= childLength - 1; j++) {
        Node childNode = nameValueDesc.item(j);
        if (NAME.equals(childNode.getNodeName()) && property
            .equals(childNode.getTextContent())) {
          return true;
        }
      }
    }
    return false;
  }

  @VisibleForTesting
  static String getProperty(Document doc, String property) {
    Node configuration = getConfigurationNode(doc);
    NodeList properties = configuration.getChildNodes();
    int length = properties.getLength();
    for (int i = 0; i <= length - 1; i++) {
      Node node = properties.item(i);
      NodeList nameValueDesc = node.getChildNodes();
      int childLength = nameValueDesc.getLength();
      for (int j = 0; j <= childLength - 1; j++) {
        Node childNode = nameValueDesc.item(j);
        if (NAME.equals(childNode.getNodeName()) && property
            .equals(childNode.getTextContent())) {
          return readValue(nameValueDesc);
        }
      }
    }
    return EMPTY;
  }

  private static String readValue(NodeList nameValueDesc) {
    int childLength = nameValueDesc.getLength();
    for (int j = 0; j <= childLength - 1; j++) {
      Node childNode = nameValueDesc.item(j);
      if (VALUE.equals(childNode.getNodeName())) {
        return childNode.getTextContent();
      }
    }
    return EMPTY;
  }

  private static void writeValue(NodeList nameValueDesc, String value) {
    int childLength = nameValueDesc.getLength();
    for (int j = 0; j <= childLength - 1; j++) {
      Node childNode = nameValueDesc.item(j);
      if (VALUE.equals(childNode.getNodeName())) {
        childNode.setTextContent(value);
      }
    }
  }

  private static Document readDocument(String pathToTezSite)
      throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    return docBuilder.parse(pathToTezSite);
  }

  private static void saveToFile(Document doc, String filepath)
      throws TransformerException {
    removeEmptyText(doc.getDocumentElement());
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer
        .setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    DOMSource source = new DOMSource(doc);
    StreamResult result = new StreamResult(new File(filepath));
    transformer.transform(source, result);
  }

  private static void removeEmptyText(Node node) {
    Node child = node.getFirstChild();
    while (child != null) {
      Node sibling = child.getNextSibling();
      if (child.getNodeType() == Node.TEXT_NODE) {
        if (child.getTextContent().trim().isEmpty())
          node.removeChild(child);
      } else {
        removeEmptyText(child);
      }
      child = sibling;
    }
  }
}
