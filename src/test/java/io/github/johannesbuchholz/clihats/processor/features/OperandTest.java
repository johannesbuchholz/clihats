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
public class OperandTest {

    @Command(name = "position-parser", cli = OperandTest.class)
    public static void instructionPositionParser(
            @Argument(type = Argument.Type.OPERAND) String positional,
            @Argument(name = "--sr1") String someRequired1,
            @Argument(name = "--sr2") String someRequired2,
            @Argument(type = Argument.Type.OPERAND, mapper = MyClassMapper.class) MyClass positionalWithMapper,
            @Argument(name = "--sd", defaultValue = "some-default") String someWithDefault
    ) {
        result.put("position-parser", positional, someRequired1, someRequired2, positionalWithMapper, someWithDefault);
    }

    @Command(name = "position-parser-with-default", cli = OperandTest.class)
    public static void instructionPositionParserWithDefault(
            @Argument(type = Argument.Type.OPERAND, defaultValue = "position-1-default") String positional,
            @Argument(name = "--sr1") String someRequired1,
            @Argument(name = "--sr2") String someRequired2,
            @Argument(type = Argument.Type.OPERAND, mapper = MyClassMapper.class, necessity = Argument.Necessity.REQUIRED) MyClass positionalWithMapper,
            @Argument(name = "--sd", defaultValue = "some-default") String someWithDefault
    ) {
        result.put("position-parser-with-default", positional, someRequired1, someRequired2, positionalWithMapper, someWithDefault);
    }

    private static final ReusableTestResult result = new ReusableTestResult();

    @Before
    public void setup() {
        result.clear();
    }

    @Test
    public void testPosition_positionsAreParsedIndependentlyFromOthers() {
        // given
        String[][] args = {
                {"position-parser", "positional0", "positional1", "--sr1", "my-value1", "--sr2", "my-value2"},
                {"position-parser", "positional0", "--sr1", "my-value1", "positional1", "--sr2", "my-value2"},
                {"position-parser", "positional0", "--sr1", "my-value1", "--sr2", "my-value2", "positional1"},
                {"position-parser", "--sr1", "my-value1", "positional0", "--sr2", "my-value2", "positional1"},
                {"position-parser", "--sr1", "my-value1", "--sr2", "my-value2", "positional0", "positional1"},
        };

        for (String[] argArray : args) {
            // when
            CliHats.get(OperandTest.class).execute(argArray);
            // then
            ReusableTestResult.Result expected = ReusableTestResult.getExpected("position-parser", "positional0", "my-value1", "my-value2", new MyClass("positional1"), "some-default");
            assertEquals(expected, result.getAndClear());
        }
    }

    @Test
    public void testPosition_positionsAreParsedAccordingToNecessity() {
        // given
        String[][] args = {
                {"position-parser-with-default", "positional0", "positional1", "--sr1", "my-value1", "--sr2", "my-value2"},
        };

        for (String[] argArray : args) {
            // when
            CliHats.get(OperandTest.class).execute(argArray);
            // then
            ReusableTestResult.Result expected = ReusableTestResult.getExpected("position-parser-with-default", "positional0", "my-value1", "my-value2", new MyClass("positional1"), "some-default");
            assertEquals(expected, result.getAndClear());
        }
    }

}
