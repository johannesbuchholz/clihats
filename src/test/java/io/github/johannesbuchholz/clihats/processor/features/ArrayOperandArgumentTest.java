package io.github.johannesbuchholz.clihats.processor.features;

import io.github.johannesbuchholz.clihats.processor.ReusableTestResult;
import io.github.johannesbuchholz.clihats.processor.annotations.Argument;
import io.github.johannesbuchholz.clihats.processor.annotations.Command;
import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;
import io.github.johannesbuchholz.clihats.processor.execution.CliHats;
import io.github.johannesbuchholz.clihats.processor.mapper.AbstractValueMapper;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

@CommandLineInterface
public class ArrayOperandArgumentTest {

    @Command
    public static void arrayOperand(
            @Argument(type = Argument.Type.OPERAND) Integer number,
            @Argument(type = Argument.Type.ARRAY_OPERAND) Path[] paths
    ) {
        result.put("array-operand", number, paths);
    }

    @Command
    public static void arrayOperandDefault(
            @Argument(type = Argument.Type.OPERAND) Integer number,
            @Argument(type = Argument.Type.ARRAY_OPERAND, defaultValue = "/the/default/way") Path[] paths
    ) {
        result.put("array-operand-default", number, paths);
    }

    @Command
    public static void arrayOperandMapper(
            @Argument(type = Argument.Type.ARRAY_OPERAND, mapper = ArrayOperandArgumentTest.UppercaseMapper.class) String[] strings
    ) {
        Object[] args = {strings};
        result.put("array-operand-mapper", args);
    }

    public static class UppercaseMapper extends AbstractValueMapper<String> {
        @Override
        public String map(String stringValue) {
            return stringValue.toUpperCase();
        }
    }

    @Command
    public static void arrayOperandMulti(
            @Argument(type = Argument.Type.OPERAND) String string,
            @Argument(type = Argument.Type.ARRAY_OPERAND) String[] strings,
            @Argument(type = Argument.Type.OPERAND) Path path,
            @Argument(type = Argument.Type.ARRAY_OPERAND) Path[] paths
    ) {
        Object[] args = {string, strings, path, paths};
        result.put("array-operand-multi", args);
    }

    private static final ReusableTestResult result = new ReusableTestResult();

    @Before
    public void setup() {
        result.clear();
    }

    @Test
    public void operand_expectAllOperandsToBeParsed() {
        // given
        String[] args = {"array-operand", "25", "/a/b/c/t.txt", "x/y/z/", "path/to/nowhere/file.exe"};
        // when
        CliHats.get(ArrayOperandArgumentTest.class).execute(args);
        // then
        Path[] expectedPaths = new Path[]{Path.of("/a/b/c/t.txt"), Path.of("x/y/z/"), Path.of("path/to/nowhere/file.exe")};
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("array-operand", 25, expectedPaths);
        assertEquals(expected, result.getAndClear());
    }

    @Test
    public void operand_expectNullArray() {
        // given
        String[] args = {"array-operand", "25"};
        // when
        CliHats.get(ArrayOperandArgumentTest.class).execute(args);
        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("array-operand", 25, null);
        assertEquals(expected, result.getAndClear());
    }

    @Test
    public void operand_expectDefaultArray() {
        // given
        String[] args = {"array-operand-default", "25"};
        // when
        CliHats.get(ArrayOperandArgumentTest.class).execute(args);
        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("array-operand-default", 25, new Path[] {Path.of("/the/default/way")});
        assertEquals(expected, result.getAndClear());
    }

    @Test
    public void operand_expectMappedStrings() {
        // given
        String[] args = {"array-operand-mapper", "some", "strings", "blubb"};
        // when
        CliHats.get(ArrayOperandArgumentTest.class).execute(args);
        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("array-operand-mapper", (Object) new String[] {"SOME", "STRINGS", "BLUBB"});
        assertEquals(expected, result.getAndClear());
    }

    @Test
    public void operand_expectAllRemainingArgumentsTakenByFirstArrayOperand() {
        // given
        String[] args = {"array-operand-multi", "some", "strings", "blubb"};
        // when
        CliHats.get(ArrayOperandArgumentTest.class).execute(args);
        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("array-operand-multi", "some", new String[] {"strings", "blubb"}, null, null);
        assertEquals(expected, result.getAndClear());
    }

}
