package io.github.johannesbuchholz.clihats.processor.features;

import io.github.johannesbuchholz.clihats.core.execution.CliException;
import io.github.johannesbuchholz.clihats.core.execution.exception.InvalidInputArgumentException;
import io.github.johannesbuchholz.clihats.core.execution.parser.exception.ValueMappingException;
import io.github.johannesbuchholz.clihats.processor.ReusableTestResult;
import io.github.johannesbuchholz.clihats.processor.annotations.Argument;
import io.github.johannesbuchholz.clihats.processor.annotations.Command;
import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;
import io.github.johannesbuchholz.clihats.processor.execution.CliHats;
import org.junit.Before;
import org.junit.Test;

import static io.github.johannesbuchholz.clihats.processor.annotations.Argument.Type.ARRAY_OPERAND;
import static org.junit.Assert.*;

@CommandLineInterface
public class EnumMapperTest {

    public enum MY_ENUM {A, B, BLUBB}

    @Command
    public static void run(
            @Argument MY_ENUM myEnum,
            @Argument(type = ARRAY_OPERAND) MY_ENUM[] myEnumArray) {
        result.put("run", myEnum, myEnumArray);
    }

    private static final ReusableTestResult result = new ReusableTestResult();

    @Before
    public void setup() {
        result.clear();
    }

    @Test
    public void testEnumMapper_expectAllInputArgumentsToBeMapped() {
        // given
        String[] args = {"run", "--my-enum", "B", "BLUBB", "A", "B"};
        // when
        CliHats.get(EnumMapperTest.class).execute(args);
        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("run", MY_ENUM.B, new MY_ENUM[]{MY_ENUM.BLUBB, MY_ENUM.A, MY_ENUM.B});
        assertEquals(expected, result.getAndClear());
    }

    @Test
    public void testEnumMapper_expectParsingExceptionForArgumentX() {
        // given
        String[] args = {"run", "--my-enum", "X", "BLUBB", "A", "B"};
        // when
        // then
        CliException actualException = assertThrows(CliException.class, () -> CliHats.get(EnumMapperTest.class).executeWithThrows(args));

        Throwable cause1 = actualException.getCause();
        assertEquals(InvalidInputArgumentException.class, cause1.getClass());
        Throwable cause2 = cause1.getCause();
        assertEquals(ValueMappingException.class, cause2.getClass());
        assertTrue(actualException.getMessage().contains("X"));
    }

}
