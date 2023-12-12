package io.github.johannesbuchholz.clihats.processor.features;

import io.github.johannesbuchholz.clihats.processor.ReusableTestResult;
import io.github.johannesbuchholz.clihats.processor.annotations.Argument;
import io.github.johannesbuchholz.clihats.processor.annotations.Command;
import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;
import io.github.johannesbuchholz.clihats.processor.execution.CliHats;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

@CommandLineInterface
public class UnmanagedParameterTest {

    @Command(name = "parser-with-non-option-parameters", cli = UnmanagedParameterTest.class)
    public static void instructionWithUnmanagedParameters(
            LocalDateTime nonOption0,
            @Argument(type = Argument.Type.OPERAND) Integer zero,
            String nonOption1,
            BigDecimal nonOption2,
            @Argument(name = "-o") Double o,
            @Argument(name = "-f", flagValue = "True") Boolean flag,
            Integer nonOption3
    ) {
        result.put("parser-with-unmanaged-parameters", nonOption0, zero, nonOption1, nonOption2, o, flag, nonOption3);
    }

    private static final ReusableTestResult result = new ReusableTestResult();

    @Before
    public void setup() {
        result.clear();
    }

    @Test
    public void test_ExpectAllNull() {
        // given
        String[] args = {"parser-with-non-option-parameters"};
        // when
        CliHats.get(UnmanagedParameterTest.class).execute(args);
        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("parser-with-unmanaged-parameters", null, null, null, null, null, null, null);
        assertEquals(expected, result.getAndClear());
    }

    @Test
    public void test_ExpectUnmanagedNull_ExpectRemainingNonNull() {
        // given
        String[] args = {"parser-with-non-option-parameters", "-f", "42", "-o", "12345.6789"};
        // when
        CliHats.get(UnmanagedParameterTest.class).execute(args);
        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("parser-with-unmanaged-parameters", null, 42, null, null, 12345.6789, true, null);
        assertEquals(expected, result.getAndClear());
    }

}
