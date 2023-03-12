package io.github.johannesbuchholz.clihats.processor.mapper.defaults;

import io.github.johannesbuchholz.clihats.processor.mapper.AbstractValueMapper;

import java.time.LocalDate;

public class LocalDateMapper extends AbstractValueMapper<LocalDate> {

    @Override
    public LocalDate map(String stringValue) {
        return LocalDate.parse(stringValue);
    }

}
