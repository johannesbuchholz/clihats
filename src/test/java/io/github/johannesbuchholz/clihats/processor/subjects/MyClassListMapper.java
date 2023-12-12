package io.github.johannesbuchholz.clihats.processor.subjects;

import io.github.johannesbuchholz.clihats.processor.mapper.AbstractValueMapper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MyClassListMapper extends AbstractValueMapper<List<MyClass>> {

    @Override
    public List<MyClass> map(String stringValue) {
        if (stringValue == null)
            return null;
        return Arrays.stream(stringValue.split(","))
                .map(String::trim)
                .map(MyClass::new)
                .collect(Collectors.toList());
    }

}
