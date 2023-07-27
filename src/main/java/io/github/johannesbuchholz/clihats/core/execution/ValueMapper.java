package io.github.johannesbuchholz.clihats.core.execution;

import io.github.johannesbuchholz.clihats.core.exceptions.parsing.ValueExtractionException;

/**
 * Implementing classes are able to map a String value to some other type.
 * @param <T> the type after mapping is applied.
 */
@FunctionalInterface
public interface ValueMapper<T> {

    T map(String stringValue);
    
    default T mapWithThrows(String stringValue) throws ValueExtractionException {
        if (stringValue == null)
            return null;
        try {
            return map(stringValue);
        } catch (Exception e) {
            throw new ValueExtractionException(e);
        }
    }

}
