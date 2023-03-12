package io.github.johannesbuchholz.clihats.processor.mapper.defaults;

import io.github.johannesbuchholz.clihats.processor.mapper.AbstractValueMapper;

public class IntegerMapper extends AbstractValueMapper<Integer> {

    @Override
    public Integer map(String stringValue) {
        return Integer.parseInt(stringValue);
    }

}
