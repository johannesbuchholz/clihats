package io.github.johannesbuchholz.clihats.processor.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Logging {

    static {
        putSystemPropertyIfAbsent("org.slf4j.simpleLogger.log.CliHats", "info");
        putSystemPropertyIfAbsent("org.slf4j.simpleLogger.showThreadName", "false");
        putSystemPropertyIfAbsent("org.slf4j.simpleLogger.levelInBrackets", "true");
        putSystemPropertyIfAbsent("org.slf4j.simpleLogger.logFile", "System.out");
    }

    private static final Logger logger = LoggerFactory.getLogger("CliHats");

    public static Logger getCliHatsLogger() {
        return logger;
    }

    private static void putSystemPropertyIfAbsent(String name, String value) {
        if(System.getProperty(name) == null)
            System.setProperty(name, value);
    }

}
