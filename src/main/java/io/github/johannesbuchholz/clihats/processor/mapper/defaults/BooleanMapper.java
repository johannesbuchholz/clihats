package io.github.johannesbuchholz.clihats.processor.mapper.defaults;

import io.github.johannesbuchholz.clihats.processor.mapper.AbstractValueMapper;

public class BooleanMapper extends AbstractValueMapper<Boolean> {

    @Override
    public Boolean map(String stringValue) {
        return Boolean.parseBoolean(stringValue);
    }

}
