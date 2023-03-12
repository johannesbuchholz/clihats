package io.github.johannesbuchholz.clihats.processor.mapper.defaults;

import io.github.johannesbuchholz.clihats.processor.mapper.AbstractValueMapper;

import java.time.LocalDateTime;

public class LocalDateTimeMapper extends AbstractValueMapper<LocalDateTime> {

    @Override
    public LocalDateTime map(String stringValue) {
        return LocalDateTime.parse(stringValue);
    }

}
