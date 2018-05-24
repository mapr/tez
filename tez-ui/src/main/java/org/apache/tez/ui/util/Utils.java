package org.apache.tez.ui.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Utils {

  private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

  private Utils() throws IllegalAccessException{
    throw new IllegalAccessException("Utility class can't be instantiated");
  }

  public static CmdConsOut execCmd(String cmd[]) {

    CmdConsOut tStatus = new CmdConsOut();
    // String array is not required so set some default value
    tStatus.reqLine = new String[1];
    tStatus.reqLine[0] = "noLine";
    int exitCode = -1;

    StringBuffer stringBuffer = new StringBuffer();
    try {
      Process p = Runtime.getRuntime().exec(cmd);
      InputStreamReader isr = new InputStreamReader(p.getInputStream());
      BufferedReader br = new BufferedReader(isr);
      String line = null;
      while ((line = br.readLine()) != null) {
        if(line.contains("Warning: Permanently added") && line.contains("(RSA) to the list of known hosts"))
          continue;
        stringBuffer.append(line + "\n");
      }
      br.close();
      exitCode = p.waitFor();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      tStatus.exitCode = exitCode;
      tStatus.consoleOut = stringBuffer.toString();
    }

    if (tStatus.exitCode == 0) {
      LOGGER.debug("Command " + cmd + " successful.\n" + tStatus.consoleOut);
    } else {
      LOGGER.debug("Command " + cmd + " failed.\n" + tStatus.consoleOut);
    }
    return tStatus;
  }
}
