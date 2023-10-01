package io.github.johannesbuchholz.clihats.processor.exceptions;

/**
 * Thrown when annotation processing detects incorrectly configured
 * {@link io.github.johannesbuchholz.clihats.processor.annotations.Argument}.
 */
public class ArgumentConfigurationException extends Exception {

    public ArgumentConfigurationException(String message) {
        super(message);
    }

}
