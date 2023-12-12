package io.github.johannesbuchholz.clihats.processor.subjects;


import io.github.johannesbuchholz.clihats.processor.mapper.AbstractValueMapper;

public class MyClassMapper extends AbstractValueMapper<MyClass> {

    @Override
    public MyClass map(String stringValue) {
        return new MyClass(stringValue);
    }

}
