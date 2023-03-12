package io.github.johannesbuchholz.clihats.processor.mapper.defaults;

import io.github.johannesbuchholz.clihats.processor.mapper.AbstractValueMapper;

/**
 * Returns the input as is.
 */
public class NoMapper extends AbstractValueMapper<String> {

    @Override
    public String map(String stringValue) {
        return stringValue;
    }
}
