package io.github.johannesbuchholz.clihats.processor.features;

import io.github.johannesbuchholz.clihats.processor.ReusableTestResult;
import io.github.johannesbuchholz.clihats.processor.annotations.Argument;
import io.github.johannesbuchholz.clihats.processor.annotations.Command;
import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;
import io.github.johannesbuchholz.clihats.processor.execution.CliHats;
import io.github.johannesbuchholz.clihats.processor.mapper.AbstractValueMapper;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

@CommandLineInterface
public class MapperTypesTest {

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

    public static class MyExtendingCustomClass extends MyCustomClass {
        public MyExtendingCustomClass(String s) {
            super(s);
        }
    }

    public static class MyExtendingCustomClassMapper extends AbstractValueMapper<MyExtendingCustomClass> {
        @Override
        public MyExtendingCustomClass map(String stringValue) {
            return new MyExtendingCustomClass(stringValue);
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
        result.put("run-nested-lists", customClass);
    }

    @Command
    public static void runWithExtendingMapperType(
            @Argument(mapper = MyExtendingCustomClassMapper.class) MyCustomClass customClass
    ) {
        result.put("run-with-extending-mapper-type", customClass);
    }

    @Command
    public static void runNestedArrays(
            @Argument(mapper = StringArrayMapper.class) MyCustomClass[][] myCustomClasses
    ) {
        result.put("run-nested-arrays", (Object) myCustomClasses);
    }

    @Command
    public static void runCrazy(
            @Argument(mapper = CrazyMapper.class) Function<MyCustomClass, Function<MyCustomClass[][], List<String>>> crazyStuff
    ) {
        result.put("run-crazy", crazyStuff);
    }

    private static final ReusableTestResult result = new ReusableTestResult();

    @Before
    public void setup() {
        result.clear();
    }

    @Test
    public void call_withMapperReturningExtendingTypeOfTargetParameter() {
        // given
        String s = "longerstringvalue";
        String[] args = {"run-with-extending-mapper-type", "-c", s};
        // when
        CliHats.get(MapperTypesTest.class).execute(args);
        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("run-with-extending-mapper-type", new MyExtendingCustomClass(s));
        assertEquals(expected, result.getAndClear());
    }

    @Test
    public void call_withNestedLists() {
        // given
        String s = "longerstringvalue";
        String[] args = {"run-nested-lists", "-c", s};
        // when
        CliHats.get(MapperTypesTest.class).execute(args);
        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("run-nested-lists", List.of(List.of(new MapperTypesTest.MyCustomClass(s))));
        assertEquals(expected, result.getAndClear());
    }

    @Test
    public void call_withNestedArrays() {
        // given
        String s = "longerstringvalue";
        String[] args = {"run-nested-arrays", "-m", s};
        // when
        CliHats.get(MapperTypesTest.class).execute(args);
        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("run-nested-arrays", new Object[] { new MapperTypesTest.MyCustomClass[][] {{ new MapperTypesTest.MyCustomClass(s) }} });
        assertEquals(expected, result.getAndClear());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void call_withCrazyType() {
        // given
        String s = "longerstringvalue";
        String[] args = {"run-crazy", "-c", s};
        // when
        CliHats.get(MapperTypesTest.class).execute(args);
        // then
        CrazyMapper crazyMapper = new CrazyMapper();
        Function<MyCustomClass, Function<MyCustomClass[][], List<String>>> expectedMethod = crazyMapper.map(s);

        String sExample = "some-other-string";
        MapperTypesTest.MyCustomClass exampleInputObject = new MapperTypesTest.MyCustomClass(sExample);
        MapperTypesTest.MyCustomClass[][] otherExampleInputObjects = new MapperTypesTest.MyCustomClass[][]{
                new MapperTypesTest.MyCustomClass[] { new MapperTypesTest.MyCustomClass("00"), new MapperTypesTest.MyCustomClass("01")},
                new MapperTypesTest.MyCustomClass[] { new MapperTypesTest.MyCustomClass("10"), new MapperTypesTest.MyCustomClass("11")}
        };
        List<String> expectedMethodResult = expectedMethod.apply(exampleInputObject).apply(otherExampleInputObjects);

        Object[] actualArgs = result.getAndClear().getArgs();
        assertEquals(1, actualArgs.length);
        assertEquals(
                expectedMethodResult,
                ((Function<MapperTypesTest.MyCustomClass, Function<MapperTypesTest.MyCustomClass[][], List<String>>>) actualArgs[0])
                        .apply(exampleInputObject)
                        .apply(otherExampleInputObjects)
        );
    }

}
