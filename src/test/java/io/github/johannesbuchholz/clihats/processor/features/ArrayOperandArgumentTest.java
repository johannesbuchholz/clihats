package io.github.johannesbuchholz.clihats.processor.features;

import io.github.johannesbuchholz.clihats.processor.GlobalTestResult;
import io.github.johannesbuchholz.clihats.processor.subjects.Cli4;
import io.github.johannesbuchholz.clihats.processor.subjects.CliTestInvoker;
import org.junit.Test;

import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

public class ArrayOperandArgumentTest {

    @Test
    public void operand_expectAllOperandsToBeParsed() {
        // given
        String[] args = {"array-operand", "25", "/a/b/c/t.txt", "x/y/z/", "path/to/nowhere/file.exe"};
        // when
        CliTestInvoker.testGeneratedCli(Cli4.class, args);
        // then
        Path[] expectedPaths = new Path[]{Path.of("/a/b/c/t.txt"), Path.of("x/y/z/"), Path.of("path/to/nowhere/file.exe")};
        GlobalTestResult expected = GlobalTestResult.constructSuccess("array-operand", 25, expectedPaths);
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

    @Test
    public void operand_expectNullArray() {
        // given
        String[] args = {"array-operand", "25"};
        // when
        CliTestInvoker.testGeneratedCli(Cli4.class, args);
        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("array-operand", 25, null);
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

    @Test
    public void operand_expectDefaultArray() {
        // given
        String[] args = {"array-operand-default", "25"};
        // when
        CliTestInvoker.testGeneratedCli(Cli4.class, args);
        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("array-operand-default", 25, new Path[] {Path.of("/the/default/way")});
        GlobalTestResult actual = GlobalTestResult.waitForResult();

        assertEquals(expected, actual);
    }

    @Test
    public void operand_expectMappedStrings() {
        // given
        String[] args = {"array-operand-mapper", "some", "strings", "blubb"};
        // when
        CliTestInvoker.testGeneratedCli(Cli4.class, args);
        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("array-operand-mapper", (Object) new String[] {"SOME", "STRINGS", "BLUBB"});
        GlobalTestResult actual = GlobalTestResult.waitForResult();

        assertEquals(expected, actual);
    }

    @Test
    public void operand_expectAllRemainingArgumentsTakenByFirstArrayOperand() {
        // given
        String[] args = {"array-operand-multi", "some", "strings", "blubb"};
        // when
        CliTestInvoker.testGeneratedCli(Cli4.class, args);
        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("array-operand-multi", "some", new String[] {"strings", "blubb"}, null, null);
        GlobalTestResult actual = GlobalTestResult.waitForResult();

        assertEquals(expected, actual);
    }

}
