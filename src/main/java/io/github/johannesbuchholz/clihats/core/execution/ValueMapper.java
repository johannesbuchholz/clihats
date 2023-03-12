package io.github.johannesbuchholz.clihats.core.execution;

/**
 * Implementing classes are able to map a String value to some other type.
 * @param <T> the type after mapping is applied.
 */
@FunctionalInterface
public interface ValueMapper<T> {

    T map(String stringValue);

}
