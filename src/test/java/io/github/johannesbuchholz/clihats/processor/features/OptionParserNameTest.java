package io.github.johannesbuchholz.clihats.processor.features;

import io.github.johannesbuchholz.clihats.core.execution.CliException;
import io.github.johannesbuchholz.clihats.core.execution.exception.InvalidInputArgumentException;
import io.github.johannesbuchholz.clihats.processor.ReusableTestResult;
import io.github.johannesbuchholz.clihats.processor.annotations.Argument;
import io.github.johannesbuchholz.clihats.processor.annotations.Command;
import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;
import io.github.johannesbuchholz.clihats.processor.execution.CliHats;
import io.github.johannesbuchholz.clihats.processor.subjects.MyClass;
import io.github.johannesbuchholz.clihats.processor.subjects.MyClassListMapper;
import io.github.johannesbuchholz.clihats.processor.subjects.MyClassMapper;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

@CommandLineInterface
public class OptionParserNameTest {

    @Command(name = "name-parser", cli = OptionParserNameTest.class)
    public static void instructionNameParser(
            @Argument(name = "--nnd") String nameNoDefault,
            @Argument(name = "--nd", defaultValue = "my-default") String nameWithDefault,
            @Argument(name = "--nr", necessity = Argument.Necessity.REQUIRED) String nameRequired,
            @Argument(name = {"--na", "--na1", "--na2", "--na3"}) String nameWithAliases,
            @Argument(name = "--nm", mapper = MyClassMapper.class) MyClass nameWithMapper,
            @Argument(name = "--nml", mapper = MyClassListMapper.class) List<MyClass> nameWithList
    ) {
        result.put("name-parser", nameNoDefault, nameWithDefault, nameRequired, nameWithAliases, nameWithMapper, nameWithList);
    }

    private static final ReusableTestResult result = new ReusableTestResult();

    @Before
    public void setup() {
        result.clear();
    }
    
    @Test
    public void testName_expectDefaultValuesExceptRequired() {
        // given
        String[] args = {"name-parser", "--nr", "my-value"};
        // when
        CliHats.get(OptionParserNameTest.class).execute(args);
        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("name-parser", null, "my-default", "my-value", null, null, null);
        assertEquals(expected, result.getAndClear());
    }

    @Test
    public void testName_expectGivenValue() {
        // given
        String[] args = {"name-parser", "--nr", "my-value", "--nnd", "given-value"};
        // when
        CliHats.get(OptionParserNameTest.class).execute(args);
        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("name-parser", "given-value", "my-default", "my-value", null, null, null);
        assertEquals(expected, result.getAndClear());
    }

    @Test
    public void testName_expectGivenValueInsteadOfCustomDefault() {
        // given
        String[] args = {"name-parser", "--nd", "value-instead-of-default", "--nr", "my-value"};
        // when
        CliHats.get(OptionParserNameTest.class).execute(args);
        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("name-parser", null, "value-instead-of-default", "my-value", null, null, null);
        assertEquals(expected, result.getAndClear());
    }

    @Test
    public void testName_expectErrorOnMissingRequiredArgument() {
        // given
        String[] args = {"name-parser"};
        // when
        // then
        List<String> expectedKeywords = List.of("missing", "required", "-nr");
        CliException actual = assertThrows(CliException.class, () -> CliHats.get(OptionParserNameTest.class).executeWithThrows(args));
        Throwable cause = actual.getCause();
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
            CliHats.get(OptionParserNameTest.class).execute(argArray);
            // then
            ReusableTestResult.Result expected = ReusableTestResult.getExpected("name-parser", null, "my-default", "my-value", "alias-value", null, null);
            assertEquals(expected, result.getAndClear());
        }
    }

    @Test
    public void testName_expectMappedValue() {
        // given
        String[] args = {"name-parser", "--nm", "mapper-value", "--nr", "my-value"};
        // when
        CliHats.get(OptionParserNameTest.class).execute(args);
        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("name-parser", null, "my-default", "my-value", null, new MyClass("mapper-value"), null);
        assertEquals(expected, result.getAndClear());
    }

    @Test
    public void testName_expectMappedListValue() {
        // given
        String[] args = {"name-parser", "--nml", "mapper-value-one, mapper-value-two", "--nr", "my-value"};
        // when
        CliHats.get(OptionParserNameTest.class).execute(args);
        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected(
                "name-parser", null, "my-default", "my-value", null, null, List.of(new MyClass("mapper-value-one"), new MyClass("mapper-value-two"))
        );
        assertEquals(expected, result.getAndClear());
    }

}
