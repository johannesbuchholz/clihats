package io.github.johannesbuchholz.clihats.core.exceptions.parsing;

/**
 * Thrown during argument parsing if a value could not be determined.
 */
public class ValueExtractionException extends Exception {

    ValueExtractionException(String message) {
        super(message);
    }

    public ValueExtractionException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
    
}