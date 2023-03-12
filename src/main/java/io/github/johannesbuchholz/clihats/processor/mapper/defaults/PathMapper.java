package io.github.johannesbuchholz.clihats.processor.mapper.defaults;

import io.github.johannesbuchholz.clihats.processor.mapper.AbstractValueMapper;

import java.nio.file.Path;

public class PathMapper extends AbstractValueMapper<Path> {

    @Override
    public Path map(String stringValue) {
        return Path.of(stringValue);
    }

}
