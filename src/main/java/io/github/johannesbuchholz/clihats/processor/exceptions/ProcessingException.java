package io.github.johannesbuchholz.clihats.processor.exceptions;

/**
 * Indicates annotation processing ultimately failed.
 */
public class ProcessingException extends RuntimeException {

    public ProcessingException(Throwable e, String template, Object... args) {
        super(String.format(template, args), e);
    }

}
