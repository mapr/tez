package org.apache.tez.ui.util;

public class CmdConsOut {
    public int exitCode;
    public String consoleOut;
    public String reqLine[];
    public String errorOut;
    @Override
    public String toString() {
        return "exitCode: " + exitCode + " " + consoleOut;
    }
}