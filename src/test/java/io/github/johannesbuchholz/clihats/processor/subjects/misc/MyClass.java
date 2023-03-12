package io.github.johannesbuchholz.clihats.processor.subjects.misc;

import java.util.Objects;

public class MyClass {

    private final Object o;

    public MyClass(Object o) {
        this.o = o;
    }

    @Override
    public String toString() {
        return "MyClass{" +
                "o=" + o +
                '}';
    }

    @Override
    public boolean equals(Object o1) {
        if (this == o1) return true;
        if (o1 == null || getClass() != o1.getClass()) return false;
        MyClass myClass = (MyClass) o1;
        return Objects.equals(o, myClass.o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(o);
    }

}
