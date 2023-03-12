package io.github.johannesbuchholz.clihats.processor.features;

import io.github.johannesbuchholz.clihats.processor.GlobalTestResult;
import io.github.johannesbuchholz.clihats.processor.subjects.Cli3;
import io.github.johannesbuchholz.clihats.processor.subjects.CliTestInvoker;
import io.github.johannesbuchholz.clihats.processor.subjects.misc.MyClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PositionalTest {

    @Test
    public void testPosition_positionsAreParsedIndependentlyFromOthers() {
        // given
        String[][] args = {
                {"position-parser", "positional0", "positional1", "-sr1", "my-value1", "-sr2", "my-value2"},
                {"position-parser", "positional0", "-sr1", "my-value1", "positional1", "-sr2", "my-value2"},
                {"position-parser", "positional0", "-sr1", "my-value1", "-sr2", "my-value2", "positional1"},
                {"position-parser", "-sr1", "my-value1", "positional0", "-sr2", "my-value2", "positional1"},
                {"position-parser", "-sr1", "my-value1", "-sr2", "my-value2", "positional0", "positional1"},
        };

        for (String[] argArray : args) {
            // when
            CliTestInvoker.testGeneratedCliWithThrows(Cli3.class, argArray);
            // then
            GlobalTestResult expected = GlobalTestResult.constructSuccess("position-parser", "positional1", "my-value1", "my-value2", new MyClass("positional0"), "some-default");
            assertEquals(expected, GlobalTestResult.waitForResult());
        }
    }

    @Test
    public void testPosition_positionsAreParsedAccordingToNecessity() {
        // given
        String[][] args = {
                {"position-parser-with-default", "positional0", "positional1", "-sr1", "my-value1", "-sr2", "my-value2"},
        };

        for (String[] argArray : args) {
            // when
            CliTestInvoker.testGeneratedCliWithThrows(Cli3.class, argArray);
            // then
            GlobalTestResult expected = GlobalTestResult.constructSuccess("position-parser-with-default", "positional1", "my-value1", "my-value2", new MyClass("positional0"), "some-default");
            assertEquals(expected, GlobalTestResult.waitForResult());
        }
    }

}
