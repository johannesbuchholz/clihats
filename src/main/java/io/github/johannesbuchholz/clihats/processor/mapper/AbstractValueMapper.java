package io.github.johannesbuchholz.clihats.processor.mapper;

import io.github.johannesbuchholz.clihats.core.execution.parser.ValueMapper;

public abstract class AbstractValueMapper<T> implements ValueMapper<T> {

    public AbstractValueMapper() {}

    abstract public T map(String stringValue);

    /**
     * Returns the input as is.
     */
    public static class IdentityMapper extends AbstractValueMapper<String> {
        @Override
        public String map(String stringValue) {
            return stringValue;
        }
    }

}
