package io.github.johannesbuchholz.clihats.processor.features;

import io.github.johannesbuchholz.clihats.core.execution.InvalidInputArgumentException;
import io.github.johannesbuchholz.clihats.processor.GlobalTestResult;
import io.github.johannesbuchholz.clihats.processor.subjects.Cli3;
import io.github.johannesbuchholz.clihats.processor.subjects.CliTestInvoker;
import io.github.johannesbuchholz.clihats.processor.subjects.misc.MyClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class OptionNameTest {

    @Test
    public void testName_expectDefaultValuesExceptRequired() {
        // given
        String[] args = {"name-parser", "--nr", "my-value"};
        // when
        CliTestInvoker.testGeneratedCliWithThrows(Cli3.class, args);
        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("name-parser", null, "my-default", "my-value", null, null, null);
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

    @Test
    public void testName_expectGivenValue() {
        // given
        String[] args = {"name-parser", "--nr", "my-value", "--nnd", "given-value"};
        // when
        CliTestInvoker.testGeneratedCliWithThrows(Cli3.class, args);
        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("name-parser", "given-value", "my-default", "my-value", null, null, null);
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

    @Test
    public void testName_expectGivenValueInsteadOfCustomDefault() {
        // given
        String[] args = {"name-parser", "--nd", "value-instead-of-default", "--nr", "my-value"};
        // when
        CliTestInvoker.testGeneratedCliWithThrows(Cli3.class, args);
        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("name-parser", null, "value-instead-of-default", "my-value", null, null, null);
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

    @Test
    public void testName_expectErrorOnMissingRequiredArgument() {
        // given
        String[] args = {"name-parser"};
        // when
        CliTestInvoker.testGeneratedCliWithThrows(Cli3.class, args);
        // then
        GlobalTestResult globalTestResult = GlobalTestResult.waitForResult();
        Throwable encounteredException = globalTestResult.getException();
        List<String> expectedKeywords = List.of("missing", "required", "-nr");

        assertNotNull(encounteredException);

        Throwable cause = encounteredException.getCause();
        assertEquals(InvalidInputArgumentException.class, cause.getClass());
        assertTrue(expectedKeywords.stream().allMatch(key -> cause.getMessage().toLowerCase().contains(key.toLowerCase())));
    }

    @Test
    public void testName_expectMatchOnAliases() {
        // given
        String[][] args = {
                {"name-parser", "--na", "alias-value", "--nr", "my-value"},
                {"name-parser", "--na1", "alias-value", "--nr", "my-value"},
                {"name-parser", "--na2", "alias-value", "--nr", "my-value"},
                {"name-parser", "--na3", "alias-value", "--nr", "my-value"},
        };

        for (String[] argArray : args) {
            // when
            CliTestInvoker.testGeneratedCliWithThrows(Cli3.class, argArray);
            // then
            GlobalTestResult expected = GlobalTestResult.constructSuccess("name-parser", null, "my-default", "my-value", "alias-value", null, null);
            assertEquals(expected, GlobalTestResult.waitForResult());
        }
    }

    @Test
    public void testName_expectMappedValue() {
        // given
        String[] args = {"name-parser", "--nm", "mapper-value", "--nr", "my-value"};
        // when
        CliTestInvoker.testGeneratedCliWithThrows(Cli3.class, args);
        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("name-parser", null, "my-default", "my-value", null, new MyClass("mapper-value"), null);
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

    @Test
    public void testName_expectMappedListValue() {
        // given
        String[] args = {"name-parser", "--nml", "mapper-value-one, mapper-value-two", "--nr", "my-value"};
        // when
        CliTestInvoker.testGeneratedCliWithThrows(Cli3.class, args);
        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess(
                "name-parser", null, "my-default", "my-value", null, null, List.of(new MyClass("mapper-value-one"), new MyClass("mapper-value-two"))
        );
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

}
