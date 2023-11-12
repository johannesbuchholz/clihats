package io.github.johannesbuchholz.clihats.processor.subjects;

import io.github.johannesbuchholz.clihats.processor.GlobalTestResult;
import io.github.johannesbuchholz.clihats.processor.annotations.Argument;
import io.github.johannesbuchholz.clihats.processor.annotations.Command;
import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;
import io.github.johannesbuchholz.clihats.processor.mapper.AbstractValueMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLineInterface
public class CliWithCustomMapper {

    public static class MyCustomClass {
        private final String s;
        public MyCustomClass(String s) {
            this.s = s;
        }
        public String getS() {
            return s;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MyCustomClass that = (MyCustomClass) o;
            return Objects.equals(s, that.s);
        }
        @Override
        public int hashCode() {
            return Objects.hash(s);
        }
    }

    public static class MyCustomMapper extends AbstractValueMapper<List<List<MyCustomClass>>> {
        @Override
        public List<List<MyCustomClass>> map(String stringValue) {
            return List.of(List.of(new MyCustomClass(stringValue)));
        }
    }

    public static class StringArrayMapper extends AbstractValueMapper<MyCustomClass[][]> {
        @Override
        public MyCustomClass[][] map(String stringValue) {
            return new MyCustomClass[][]{{ new MyCustomClass(stringValue) }};
        }
    }

    public static class CrazyMapper extends AbstractValueMapper<Function<MyCustomClass, Function<MyCustomClass[][], List<String>>>> {
        @Override
        public Function<MyCustomClass, Function<MyCustomClass[][], List<String>>> map(String stringValue) {
            return myCustomClass -> (myCustomClasses -> Stream.concat(
                    Stream.of(stringValue, myCustomClass.getS()),
                    Arrays.stream(myCustomClasses).flatMap(Arrays::stream).map(MyCustomClass::getS)
            ).collect(Collectors.toList()));
        }
    }

    @Command
    public static void runNestedLists(
            @Argument(mapper = MyCustomMapper.class) List<List<MyCustomClass>> customClass
    ) {
        GlobalTestResult.setSuccess("run-nested-lists", customClass);
    }

    @Command
    public static void runNestedArrays(
            @Argument(mapper = StringArrayMapper.class) MyCustomClass[][] myCustomClasses
    ) {
        GlobalTestResult.setSuccess("run-nested-arrays", (Object) myCustomClasses);
    }

    @Command
    public static void runCrazy(
            @Argument(mapper = CrazyMapper.class) Function<MyCustomClass, Function<MyCustomClass[][], List<String>>> crazyStuff
    ) {
        GlobalTestResult.setSuccess("run-crazy", crazyStuff);
    }

}
