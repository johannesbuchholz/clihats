package io.github.johannesbuchholz.clihats.processor.features;

import io.github.johannesbuchholz.clihats.processor.GlobalTestResult;
import io.github.johannesbuchholz.clihats.processor.subjects.Cli3;
import io.github.johannesbuchholz.clihats.processor.subjects.CliTestInvoker;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NonParameterOptionTest {

    @Test
    public void testNonOptions_ExpectAllNull() {
        // given
        String[] args = {"parser-with-non-option-parameters"};
        // when
        CliTestInvoker.testGeneratedCli(Cli3.class, args);
        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("parser-with-non-option-parameters", null, null, null, null, null, null, null);
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

    @Test
    public void testNonOptions_ExpectNonOptionNull_ExpectRemainingNonNull() {
        // given
        String[] args = {"parser-with-non-option-parameters", "-f", "42", "-o", "12345.6789"};
        // when
        CliTestInvoker.testGeneratedCli(Cli3.class, args);
        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("parser-with-non-option-parameters", null, 42, null, null, 12345.6789, true, null);
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

}
