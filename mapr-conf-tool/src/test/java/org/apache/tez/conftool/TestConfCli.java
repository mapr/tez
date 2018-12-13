/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tez.conftool;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

public class TestConfCli {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void printHelpTest()
      throws ParserConfigurationException, TransformerException, SAXException, IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
    ConfCli.main(new String[] { "--help" });
    String output = baos.toString();

    Assert.assertTrue(output.contains("Print help information"));
  }

  @Test
  public void testNoArguments()
      throws ParserConfigurationException, TransformerException, SAXException, IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
    ConfCli.main(new String[] { "" });
    String output = baos.toString();

    Assert.assertTrue(output.contains("Print help information"));
  }

  @Test
  public void testNullArguments()
      throws ParserConfigurationException, TransformerException, SAXException, IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
    ConfCli.main(null);
    String output = baos.toString();

    Assert.assertTrue(output.contains("Print help information"));
  }

  @Test
  public void testParsingError()
      throws ParserConfigurationException, TransformerException, SAXException, IOException {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Parsing failed");
    ConfCli.main(new String[] { "--lhjlk", "-/--l)9" });
  }

  @Test
  public void testSecurityTrue()
      throws ParserConfigurationException, TransformerException, SAXException, IOException {
    URL url = Thread.currentThread().getContextClassLoader().getResource("tez-site-01.xml");
    String pathToTezSite = url.getPath();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
    ConfCli.main(new String[] { "--path", pathToTezSite, "--security", "true" });
    String output = baos.toString();

    Assert.assertFalse(output.contains("Print help information"));
  }

  @Test
  public void testSecurityFalse()
      throws ParserConfigurationException, TransformerException, SAXException, IOException {
    URL url = Thread.currentThread().getContextClassLoader().getResource("tez-site-01.xml");
    String pathToTezSite = url.getPath();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
    ConfCli.main(new String[] { "--path", pathToTezSite, "--security", "false" });
    String output = baos.toString();

    Assert.assertFalse(output.contains("Print help information"));
  }

  @Test
  public void testSecurityCustom()
      throws ParserConfigurationException, TransformerException, SAXException, IOException {
    URL url = Thread.currentThread().getContextClassLoader().getResource("tez-site-01.xml");
    String pathToTezSite = url.getPath();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
    ConfCli.main(new String[] { "--path", pathToTezSite, "--security", "custom" });
    String output = baos.toString();

    Assert.assertFalse(output.contains("Print help information"));
  }

  @Test
  public void testSecurityIllegalArgument()
      throws ParserConfigurationException, TransformerException, SAXException, IOException {
    URL url = Thread.currentThread().getContextClassLoader().getResource("tez-site-01.xml");
    String pathToTezSite = url.getPath();
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Incorrect security configuration options");
    ConfCli.main(new String[] { "--path", pathToTezSite, "--security", "wrong" });
  }
}
