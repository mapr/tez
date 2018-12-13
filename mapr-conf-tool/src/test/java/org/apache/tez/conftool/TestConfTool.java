package org.apache.tez.conftool;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.URL;

public class TestConfTool {

  @Test
  public void testSetProperty() throws ParserConfigurationException, IOException, SAXException {
    URL url = Thread.currentThread().getContextClassLoader().getResource("tez-site-01.xml");
    String pathToTezSite = url.getPath();
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    Document doc = docBuilder.parse(pathToTezSite);
    String valueBeforeTest = ConfTool.getProperty(doc, "myProperty");
    ConfTool.set(doc, "myProperty", "myValue");

    Assert.assertNotEquals(valueBeforeTest, ConfTool.getProperty(doc, "myProperty"));
    Assert.assertEquals("myValue", ConfTool.getProperty(doc, "myProperty"));
  }

  @Test
  public void testRemoveProperty() throws ParserConfigurationException, IOException, SAXException {
    URL url = Thread.currentThread().getContextClassLoader().getResource("tez-site-01.xml");
    String pathToTezSite = url.getPath();
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    Document doc = docBuilder.parse(pathToTezSite);
    String valueBeforeTest = ConfTool.getProperty(doc, "myProperty");
    ConfTool.removeProperty(doc, "myProperty");

    Assert.assertNotEquals(valueBeforeTest, ConfTool.getProperty(doc, "myProperty"));
    Assert.assertEquals("", ConfTool.getProperty(doc, "myProperty"));
  }

  @Test
  public void testEncryptionEnable()
      throws IOException, ParserConfigurationException, SAXException, TransformerException {
    URL url = Thread.currentThread().getContextClassLoader().getResource("tez-site-01.xml");
    String pathToTezSite = url.getPath();
    ConfTool.setEncryption(pathToTezSite, Security.MAPRSASL);
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    Document doc = docBuilder.parse(pathToTezSite);

    Assert.assertEquals("true", ConfTool.getProperty(doc, ConfTool.SSL));
    Assert.assertEquals("true", ConfTool.getProperty(doc, ConfTool.KEEP_ALIVE));
  }

  @Test
  public void testEncryptionDisable()
      throws IOException, ParserConfigurationException, SAXException, TransformerException {
    URL url = Thread.currentThread().getContextClassLoader().getResource("tez-site-02.xml");
    String pathToTezSite = url.getPath();
    ConfTool.setEncryption(pathToTezSite,  Security.NONE);
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    Document doc = docBuilder.parse(pathToTezSite);

    Assert.assertEquals("", ConfTool.getProperty(doc, ConfTool.SSL));
    Assert.assertEquals("", ConfTool.getProperty(doc, ConfTool.KEEP_ALIVE));
  }

  @Test
  public void testEncryptionDisableIfNotPresent()
      throws IOException, ParserConfigurationException, SAXException, TransformerException {
    URL url = Thread.currentThread().getContextClassLoader().getResource("tez-site-01.xml");
    String pathToTezSite = url.getPath();
    ConfTool.setEncryption(pathToTezSite,  Security.NONE);
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    Document doc = docBuilder.parse(pathToTezSite);

    Assert.assertEquals("", ConfTool.getProperty(doc, ConfTool.SSL));
    Assert.assertEquals("", ConfTool.getProperty(doc, ConfTool.KEEP_ALIVE));
  }

  @Test
  public void testEncryptionCustom()
      throws IOException, ParserConfigurationException, SAXException, TransformerException {
    URL url = Thread.currentThread().getContextClassLoader().getResource("tez-site-01.xml");
    String pathToTezSite = url.getPath();
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    Document doc = docBuilder.parse(pathToTezSite);
    String sslBeforeTest = ConfTool.getProperty(doc, ConfTool.SSL);
    String keepAliveBeforeTest = ConfTool.getProperty(doc, ConfTool.KEEP_ALIVE);
    ConfTool.setEncryption(pathToTezSite,  Security.CUSTOM);
    doc = docBuilder.parse(pathToTezSite);

    Assert.assertEquals(sslBeforeTest, ConfTool.getProperty(doc, ConfTool.SSL));
    Assert.assertEquals(keepAliveBeforeTest, ConfTool.getProperty(doc, ConfTool.KEEP_ALIVE));
  }
}
