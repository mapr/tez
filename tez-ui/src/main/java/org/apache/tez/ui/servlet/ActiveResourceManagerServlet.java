package org.apache.tez.ui.servlet;

import org.apache.tez.ui.util.CmdConsOut;
import org.apache.tez.ui.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ActiveResourceManagerServlet extends HttpServlet {

  private static final Logger LOGGER = LoggerFactory.getLogger(ActiveResourceManagerServlet.class);

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String activeResourceManagerUrl;
    String[] cmd = {
        "/bin/sh",
        "-c",
        "maprcli urls -name resourcemanager"
    };
    CmdConsOut cmdConsOut = Utils.execCmd(cmd);
    LOGGER.debug("Console out: " + cmdConsOut.consoleOut);
    String consoleOut = cmdConsOut.consoleOut;
    String[] split = consoleOut.split("\n");
    if(split.length == 2) {
      activeResourceManagerUrl = split[1];
    } else activeResourceManagerUrl = split[0];
    resp.setContentType("text/html;" + StandardCharsets.UTF_8.name());
    try(PrintWriter writer = resp.getWriter()) {
      writer.println(activeResourceManagerUrl);
    }
  }
}
