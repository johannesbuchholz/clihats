package io.github.johannesbuchholz.clihats.processor.features;

import io.github.johannesbuchholz.clihats.processor.GlobalTestResult;
import io.github.johannesbuchholz.clihats.processor.subjects.CliTestInvoker;
import io.github.johannesbuchholz.clihats.processor.subjects.CliWithCustomMapper;
import org.junit.Test;

import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class MapperWithGenericTypesTest {

    @Test
    public void call_withNestedLists() {
        // given
        String s = "longerstringvalue";
        String[] args = {"run-nested-lists", "-c", s};
        // when
        CliTestInvoker.testGeneratedCliWithThrows(CliWithCustomMapper.class, args);
        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("run-nested-lists", List.of(List.of(new CliWithCustomMapper.MyCustomClass(s))));
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

    @Test
    public void call_withNestedArrays() {
        // given
        String s = "longerstringvalue";
        String[] args = {"run-nested-arrays", "-m", s};
        // when
        CliTestInvoker.testGeneratedCliWithThrows(CliWithCustomMapper.class, args);
        // then
        Object[] expectedArgs = new Object[] { new CliWithCustomMapper.MyCustomClass[][] {{ new CliWithCustomMapper.MyCustomClass(s) }} };
        Object[] actualArgs = GlobalTestResult.waitForResult().getArgs();
        assertArrayEquals(expectedArgs, actualArgs);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void call_withCrazyType() {
        // given
        String s = "longerstringvalue";
        String[] args = {"run-crazy", "-c", s};
        // when
        CliTestInvoker.testGeneratedCliWithThrows(CliWithCustomMapper.class, args);
        // then
        String sExample = "some-other-string";
        CliWithCustomMapper.MyCustomClass exampleInputObject = new CliWithCustomMapper.MyCustomClass(sExample);
        CliWithCustomMapper.MyCustomClass[][] otherExampleInputObjects = new CliWithCustomMapper.MyCustomClass[][]{
                new CliWithCustomMapper.MyCustomClass[] { new CliWithCustomMapper.MyCustomClass("00"), new CliWithCustomMapper.MyCustomClass("01")},
                new CliWithCustomMapper.MyCustomClass[] { new CliWithCustomMapper.MyCustomClass("10"), new CliWithCustomMapper.MyCustomClass("11")}
        };
        List<String> expectedMethodReturnValue = List.of(s, sExample, "00", "01", "10", "11");

        Object[] actualArgs = GlobalTestResult.waitForResult().getArgs();
        assertEquals(1, actualArgs.length);
        assertEquals(
                expectedMethodReturnValue,
                ((Function<CliWithCustomMapper.MyCustomClass, Function<CliWithCustomMapper.MyCustomClass[][], List<String>>>) actualArgs[0])
                        .apply(exampleInputObject)
                        .apply(otherExampleInputObjects)
        );
    }

}
