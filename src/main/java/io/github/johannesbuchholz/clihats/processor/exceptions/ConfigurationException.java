package io.github.johannesbuchholz.clihats.processor.exceptions;

/**
 * Thrown when annotation processing detects incorrectly set annotations or annotation values.
 */
public class ConfigurationException extends RuntimeException {

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationException(String template, Object... args) {
        super(String.format(template, args));
    }

}
