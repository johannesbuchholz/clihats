package io.github.johannesbuchholz.clihats.processor.util;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;

public class ProcessingLogger {

    public static final String LOG_LEVEL_COMPILE_ARG_NAME = "clihats.logLevel";
    private static final String PREFIX = "(clihats) ";

    private enum Level {
        DEBUG, INFO, WARN, OFF;

        private static Level from(String value) {
            if (value == null) {
                return ProcessingLogger.Level.INFO;
            }

            try {
                return ProcessingLogger.Level.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ProcessingLogger.Level.INFO;
            }
        }
    }

    private final Messager messager;
    private final Level level;

    public static ProcessingLogger from(ProcessingEnvironment processingEnv) {
        return new ProcessingLogger(processingEnv);
    }

    private ProcessingLogger(ProcessingEnvironment processingEnv) {
        this.messager = processingEnv.getMessager();
        this.level = ProcessingLogger.Level.from(processingEnv.getOptions().get(LOG_LEVEL_COMPILE_ARG_NAME));
    }

    public void warn(String message, Object... args) {
        if (level.ordinal() <= ProcessingLogger.Level.WARN.ordinal()) {
            messager.printMessage(Diagnostic.Kind.WARNING, format(message, args));
        }
    }

    private static String format(String message, Object[] args) {
        return PREFIX + TextUtils.format(message, args);
    }

    public void info(String message, Object... args) {
        if (level.ordinal() <= ProcessingLogger.Level.INFO.ordinal()) {
            messager.printMessage(Diagnostic.Kind.NOTE, format(message, args));
        }
    }

    public void debug(String message, Object... args) {
        if (isDebugEnabled()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "(DEBUG) " + format(message, args));
        }
    }

    public boolean isDebugEnabled() {
        return level.ordinal() <= ProcessingLogger.Level.DEBUG.ordinal();
    }

}
