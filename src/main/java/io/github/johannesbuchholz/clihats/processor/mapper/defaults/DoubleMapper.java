package io.github.johannesbuchholz.clihats.processor.mapper.defaults;

import io.github.johannesbuchholz.clihats.processor.mapper.AbstractValueMapper;

public class DoubleMapper extends AbstractValueMapper<Double> {

    @Override
    public Double map(String stringValue) {
        return Double.parseDouble(stringValue);
    }

}
