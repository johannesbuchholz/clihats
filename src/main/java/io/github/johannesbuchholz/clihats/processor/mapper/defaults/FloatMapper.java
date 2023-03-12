package io.github.johannesbuchholz.clihats.processor.mapper.defaults;

import io.github.johannesbuchholz.clihats.processor.mapper.AbstractValueMapper;

public class FloatMapper extends AbstractValueMapper<Float> {

    @Override
    public Float map(String stringValue) {
        return Float.parseFloat(stringValue);
    }

}
