package io.github.johannesbuchholz.clihats.processor.mapper;

import io.github.johannesbuchholz.clihats.core.execution.parser.ValueMapper;

/**
 * Implementing classes are able to map a String value to some other type.
 * @param <T> the type after the mapping is applied
 */
public abstract class AbstractValueMapper<T> implements ValueMapper<T> {

    public AbstractValueMapper() {}

    /**
     * @implNote Implementations should be able to handle {@code null} as input.
     */
    abstract public T map(String stringValue);

}
