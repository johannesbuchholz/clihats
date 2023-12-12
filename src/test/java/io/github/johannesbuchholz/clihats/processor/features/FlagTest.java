package io.github.johannesbuchholz.clihats.processor.features;

import io.github.johannesbuchholz.clihats.processor.ReusableTestResult;
import io.github.johannesbuchholz.clihats.processor.annotations.Argument;
import io.github.johannesbuchholz.clihats.processor.annotations.Command;
import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;
import io.github.johannesbuchholz.clihats.processor.execution.CliHats;
import io.github.johannesbuchholz.clihats.processor.subjects.MyClass;
import io.github.johannesbuchholz.clihats.processor.subjects.MyClassMapper;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@CommandLineInterface
public class FlagTest {

    @Command(name = "flag-parser", cli = FlagTest.class)
    public static void instructionFlagParser(
            @Argument(name = "--fnd", flagValue = "value") String flagNoDefault,
            @Argument(name = "--fd", flagValue = "my-value") String flagWithValue,
            @Argument(name = "-fdd", flagValue = "my-value", defaultValue = "my-default") String flagWithCustomDefaultValue,
            @Argument(name = {"--fa" , "--fa1", "--fa2", "--fa3"}, flagValue = "value") String flagWithAlias,
            @Argument(name = "--fm", flagValue = "some-value", mapper = MyClassMapper.class) MyClass flagWithMapper
    ) {
        result.put("flag-parser", flagNoDefault, flagWithCustomDefaultValue, flagWithValue, flagWithAlias, flagWithMapper);
    }

    private static final ReusableTestResult result = new ReusableTestResult();

    @Before
    public void setup() {
        result.clear();
    }

    @Test
    public void testFlag_expectNullAsDefaultValues_andDeclaredDefaultValues() {
        // given
        String[] args = {"flag-parser"};
        // when
        CliHats.get(FlagTest.class).execute(args);
        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("flag-parser", null, "my-default", null, null, null);
        assertEquals(expected, result.getAndClear());
    }

    @Test
    public void testFlag_expectFlagStringOnFlagWithoutDeclaredDefaultValue() {
        // given
        String[] args = {"flag-parser", "--fnd"};
        // when
        CliHats.get(FlagTest.class).execute(args);
        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("flag-parser", "value", "my-default", null, null, null);
        assertEquals(expected, result.getAndClear());
    }

    @Test
    public void testFlag_expectDeclaredFlagValue() {
        // given
        String[] args = {"flag-parser", "--fd"};
        // when
        CliHats.get(FlagTest.class).execute(args);
        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("flag-parser", null, "my-default", "my-value", null, null);
        assertEquals(expected, result.getAndClear());
    }

    @Test
    public void testFlag_expectMatchOnAliases() {
        // given
        String[][] args = {{"flag-parser", "--fa"},
                {"flag-parser", "--fa1"},
                {"flag-parser", "--fa2"},
                {"flag-parser", "--fa3"}};
        for (String[] argArray : args) {
            // when
            CliHats.get(FlagTest.class).execute(argArray);
            // then
            ReusableTestResult.Result expected = ReusableTestResult.getExpected("flag-parser", null, "my-default", null, "value", null);
            assertEquals(expected, result.getAndClear());
        }
    }

    @Test
    public void testFlag_expectMappedValue() {
        // given
        String[] args = {"flag-parser", "--fm"};
        // when
        CliHats.get(FlagTest.class).execute(args);
        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("flag-parser", null, "my-default", null, null, new MyClass("some-value"));
        assertEquals(expected, result.getAndClear());
    }

}
