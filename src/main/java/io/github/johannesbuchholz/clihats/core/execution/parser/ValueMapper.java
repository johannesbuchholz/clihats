package io.github.johannesbuchholz.clihats.core.execution.parser;

/**
 * Implementing classes are able to map a String value to some other type.
 * @param <T> the type of the mapping result.
 */
@FunctionalInterface
public interface ValueMapper<T> {

    /**
     * Transforms a non-null String value to a corresponding representation of this mappers type.
     * @param stringValue A non-null string value.
     * @return An object corresponding to the specified string value.
     */
    T map(String stringValue);

}
