package io.github.johannesbuchholz.clihats.processor.subjects;

import io.github.johannesbuchholz.clihats.processor.mapper.AbstractValueMapper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MyListMapper extends AbstractValueMapper<List<String>> {

    @Override
    public List<String> map(String stringValue) {
        if (stringValue == null)
            return null;
        return Arrays.stream(stringValue.split(",")).map(String::trim).collect(Collectors.toList());
    }

}
