package io.github.johannesbuchholz.clihats.processor.mapper.defaults;

import io.github.johannesbuchholz.clihats.processor.mapper.AbstractValueMapper;

import java.math.BigDecimal;

public class BigDecimalMapper extends AbstractValueMapper<BigDecimal> {

    @Override
    public BigDecimal map(String stringValue) {
        return BigDecimal.valueOf(Double.parseDouble(stringValue));
    }

}
