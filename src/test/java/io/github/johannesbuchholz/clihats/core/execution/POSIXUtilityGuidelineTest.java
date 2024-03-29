package io.github.johannesbuchholz.clihats.core.execution;

import io.github.johannesbuchholz.clihats.core.execution.exception.CommandExecutionException;
import io.github.johannesbuchholz.clihats.core.execution.parser.AbstractOperandParser;
import io.github.johannesbuchholz.clihats.core.execution.parser.AbstractOptionParser;
import io.github.johannesbuchholz.clihats.core.execution.parser.ArgumentParsers;
import io.github.johannesbuchholz.clihats.core.execution.parser.exception.MissingValueException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * POSIX <a href="https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/V1_chap12.html">Guidelines</a>.
 */
public class POSIXUtilityGuidelineTest {

    @Test
    public void guideline_3_optionNamesOneCharOnly_positive() {
        // given
        List<String> validNames = List.of("-a","-b","-c","-d","-e","-f","-g","-h","-i","-j",
                "-k","-l","-m","-n","-o","-p","-q","-r","-s","-t","-u","-v","-w","-x","-y","-z",
                "-A","-B","-C","-D","-E","-F","-G","-H","-I","-J",
                "-K","-L","-M","-N","-O","-P","-Q","-R","-S","-T","-U","-V","-W","-X","-Y","-Z",
                "-1","-2","-3","-4","-5","-6","-7","-8","-9", "-0",
                "-ß", "-ä", "-ö", "-ü",
                "-Ä", "-Ö", "-Ü");

        // when
        List<? extends AbstractOptionParser<?>> valued = validNames.stream()
                .map(ArgumentParsers::valuedOption)
                .collect(Collectors.toList());
        List<? extends AbstractOptionParser<?>> flag = validNames.stream()
                .map(ArgumentParsers::flagOption)
                .collect(Collectors.toList());

        // then
        assertEquals(validNames.size(), valued.size());
        assertEquals(validNames.size(), flag.size());
    }

    @Test
    public void guideline_3_optionNamesOneCharOnly_negative() {
        // given
        List<String> invalidNames = List.of("-?", "-!", "-\\", "-+", "-'", "-\"", "-~",
                "-.", "-,", "-;", "-<", "->", "-`", "-@", "-[", "-]", "-*", "-|",
                "-(", "-)", "-{", "-}", "-=", "-§", "-%", "-&", "-#", "-^", "-°");

        // when
        List<? extends AbstractOptionParser<?>> valued = invalidNames.stream()
                .map(ArgumentParsers::valuedOption)
                .collect(Collectors.toList());
        List<? extends AbstractOptionParser<?>> flag = invalidNames.stream()
                .map(ArgumentParsers::flagOption)
                .collect(Collectors.toList());

        // then
        assertEquals(invalidNames.size(), valued.size());
        assertEquals(invalidNames.size(), flag.size());
        assertTrue(valued.stream().allMatch(parser -> parser.getNames().stream().noneMatch(AbstractOptionParser.OptionParserName::isPOSIXConformOptionName)));
        assertTrue(flag.stream().allMatch(parser -> parser.getNames().stream().noneMatch(AbstractOptionParser.OptionParserName::isPOSIXConformOptionName)));
    }

    @Test
    public void guideline_4_optionsWithPrefixOnly_negative() {
        // given
        List<String> invalidNames = List.of("a", "+a", "abc", "_a");

        // when
        List<Exception> valued = new ArrayList<>();
        List<Exception> flag = new ArrayList<>();
        for (String name : invalidNames) {
            try {
                ArgumentParsers.valuedOption(name);
            } catch (IllegalArgumentException e) {
                valued.add(e);
            }
            try {
                ArgumentParsers.flagOption(name);
            } catch (IllegalArgumentException e) {
                flag.add(e);
            }
        }

        // then
        assertEquals(invalidNames.size(), valued.size());
        assertEquals(invalidNames.size(), flag.size());
    }

    @Test
    public void guideline_5_multiplePOSIXConformOptionFollowedByValue_onlyOneValuedOption_expectSuccess() throws CommandExecutionException {
        // given
        AbstractOptionParser<?> flagA = ArgumentParsers.flagOption("-a");
        AbstractOptionParser<?> flagB = ArgumentParsers.flagOption("-b");
        AbstractOptionParser<?> value = ArgumentParsers.valuedOption("-v");
        TestInstruction instruction = new TestInstruction();
        Command command = Command.forName("do-it")
                .withParsers(flagA, flagB, value)
                .withInstruction(instruction);
        List<String> validArgs = List.of("-abv", "-bav", "-avb", "-vab", "-vba", "-bva", "-bav");
        String argValue = "5";

        // when
        for (String arg : validArgs) {
            command.execute(new String[]{arg, argValue});
        }

        // then
        List<Object[]> calledArguments = instruction.getCalledArguments();
        assertEquals(validArgs.size(), calledArguments.size());
        assertTrue(calledArguments.stream().allMatch(values -> Arrays.equals(new String[]{"", "", argValue}, values)));
    }

    @Test
    public void guideline_5_multiplePOSIXConformOptionFollowedByValue_multipleValuedOption_expectFailure() {
        // given
        AbstractOptionParser<?> flag = ArgumentParsers.flagOption("-a");
        AbstractOptionParser<?> valueA = ArgumentParsers.valuedOption("-v");
        AbstractOptionParser<?> valueB = ArgumentParsers.valuedOption("-w");
        TestInstruction instruction = new TestInstruction();
        Command command = Command.forName("do-it")
                .withParsers(flag, valueA, valueB)
                .withInstruction(instruction);
        List<String> invalidArgs = List.of("-vw", "-avw", "-vaw", "-vwa", "-wva", "-wav", "-awv");
        String argValue = "5";

        // when
        List<CommandExecutionException> exceptions = new ArrayList<>();
        for (String arg : invalidArgs) {
            CommandExecutionException e = assertThrows(CommandExecutionException.class, () -> command.execute(new String[]{arg, argValue, argValue}));
            exceptions.add(e);
        }

        //then
        assertTrue(instruction.getCalledArguments().isEmpty());
        assertEquals(invalidArgs.size(), exceptions.size());
        assertTrue(exceptions.stream().allMatch(e -> MissingValueException.class.equals(e.getCause().getClass())));
    }

    @Test
    public void guideline_6_valuedOptionsArgumentsAreSeparateArguments_positive() throws CommandExecutionException {
        // given
        AbstractOptionParser<?> valued = ArgumentParsers.valuedOption("-v");
        TestInstruction instruction = new TestInstruction();
        Command command = Command.forName("do-it")
                .withParsers(valued)
                .withInstruction(instruction);
        String[] validArguments = new String[] {"-v", "42"};

        // when
        command.execute(validArguments);

        // then
        List<Object[]> calledArguments = instruction.getCalledArguments();
        assertEquals(1, calledArguments.size());
        assertArrayEquals(new String[] {"42"}, calledArguments.get(0));
    }

    @Test
    public void guideline_6_valuedOptionsArgumentsAreSeparateArguments_negative() {
        // given
        AbstractOptionParser<?> valued = ArgumentParsers.valuedOption("-v");
        TestInstruction instruction = new TestInstruction();
        Command command = Command.forName("do-it")
                .withParsers(valued)
                .withInstruction(instruction);
        String[] invalidArguments = new String[] {"-v42"};

        // when
        // then
        CliException actualException = assertThrows(CommandExecutionException.class, () ->  command.execute(invalidArguments));
        assertTrue(actualException.getMessage().contains(valued.toString()));
        assertEquals(MissingValueException.class, actualException.getCause().getClass());
    }

    @Test
    public void guideline_7_valuedOptionsArgumentsAreMandatory() {
        // given
        AbstractOptionParser<?> valued = ArgumentParsers.valuedOption("-v");
        TestInstruction instruction = new TestInstruction();
        Command command = Command.forName("do-it")
                .withParsers(valued)
                .withInstruction(instruction);
        String[] invalidArguments = new String[] {"-v"};

        // when
        // then
        CliException actualException = assertThrows(CommandExecutionException.class, () ->  command.execute(invalidArguments));
        assertTrue(actualException.getMessage().contains(valued.toString()));
        assertEquals(MissingValueException.class, actualException.getCause().getClass());
    }

    @Test
    public void guideline_10_operandDelimiter_expectDelimiterParsedAsValueOfOption() throws CommandExecutionException {
        // given
        AbstractOptionParser<?> valuedV = ArgumentParsers.valuedOption("-v");
        AbstractOptionParser<?> valuedW = ArgumentParsers.valuedOption("-w");
        AbstractOperandParser<?> operand0 = ArgumentParsers.operand(0);
        TestInstruction instruction = new TestInstruction();
        Command command = Command.forName("do-it")
                .withParsers(valuedV, valuedW, operand0)
                .withInstruction(instruction);
        String[] args = new String[] {"-v", "--", "-w", "42", "blubb"};

        // when
        command.execute(args);

        // then
        assertEquals(1, instruction.getCalledArguments().size());
        String[] expectedValues = {"--", "42", "blubb"};
        assertArrayEquals(expectedValues, instruction.getCalledArguments().get(0));
    }

    @Test
    public void guideline_10_operandDelimiter_expectOptionAfterDelimiterParsedByOperand() throws CommandExecutionException {
        // given
        AbstractOptionParser<?> valuedV = ArgumentParsers.valuedOption("-v");
        AbstractOptionParser<?> valuedW = ArgumentParsers.valuedOption("-w");
        AbstractOperandParser<?> operand0 = ArgumentParsers.operand(0);
        TestInstruction instruction = new TestInstruction();
        Command command = Command.forName("do-it")
                .withParsers(valuedV, valuedW, operand0)
                .withInstruction(instruction);
        String[] args = new String[] {"-v", "42", "--", "-w"};

        // when
        command.execute(args);

        // then
        assertEquals(1, instruction.getCalledArguments().size());
        String[] expectedValues = {"42", null, "-w"};
        assertArrayEquals(expectedValues, instruction.getCalledArguments().get(0));
    }

    @Test
    public void guideline_10_operandDelimiter_expectEmptyOperands() throws CommandExecutionException {
        // given
        AbstractOptionParser<?> valuedV = ArgumentParsers.valuedOption("-v");
        AbstractOptionParser<?> valuedW = ArgumentParsers.valuedOption("-w");
        AbstractOperandParser<?> operand0 = ArgumentParsers.operand(0);
        TestInstruction instruction = new TestInstruction();
        Command command = Command.forName("do-it")
                .withParsers(valuedV, valuedW, operand0)
                .withInstruction(instruction);
        String[] args = new String[] {"-v", "42", "-w", "blubb", "--"};

        // when
        command.execute(args);

        // then
        assertEquals(1, instruction.getCalledArguments().size());
        String[] expectedValues = {"42", "blubb", null};
        assertArrayEquals(expectedValues, instruction.getCalledArguments().get(0));
    }

    @Test
    public void guideline_10_operandDelimiter_expectNoParsedValues() throws CommandExecutionException {
        // given
        AbstractOptionParser<?> valuedV = ArgumentParsers.valuedOption("-v");
        AbstractOptionParser<?> valuedW = ArgumentParsers.valuedOption("-w");
        AbstractOperandParser<?> operand0 = ArgumentParsers.operand(0);
        TestInstruction instruction = new TestInstruction();
        Command command = Command.forName("do-it")
                .withParsers(valuedV, valuedW, operand0)
                .withInstruction(instruction);
        String[] args = new String[] {"--"};

        // when
        command.execute(args);

        // then
        assertEquals(1, instruction.getCalledArguments().size());
        String[] expectedValues = {null, null, null};
        assertArrayEquals(expectedValues, instruction.getCalledArguments().get(0));
    }

    @Test
    public void guideline_10_operandDelimiter_expectTooManyOperands() {
        // given
        AbstractOptionParser<?> valuedV = ArgumentParsers.valuedOption("-v");
        AbstractOptionParser<?> valuedW = ArgumentParsers.valuedOption("-w");
        AbstractOperandParser<?> operand0 = ArgumentParsers.operand(0);
        TestInstruction instruction = new TestInstruction();
        Command command = Command.forName("do-it")
                .withParsers(valuedV, valuedW, operand0)
                .withInstruction(instruction);
        String[] args = new String[] {"--", "-v", "-w", "42"};

        // when
        // then
        CliException actualException = assertThrows(CommandExecutionException.class, () ->  command.execute(args));
        assertTrue(actualException.getMessage().contains("-w"));
        assertTrue(actualException.getMessage().contains("42"));
    }

    @Test
    public void guideline_10_operandDelimiter_expectSecondDelimiterParsedAsValue() throws CommandExecutionException {
        // given
        AbstractOptionParser<?> valuedV = ArgumentParsers.valuedOption("-v");
        AbstractOptionParser<?> valuedW = ArgumentParsers.valuedOption("-w");
        AbstractOperandParser<?> operand0 = ArgumentParsers.operand(0);
        TestInstruction instruction = new TestInstruction();
        Command command = Command.forName("do-it")
                .withParsers(valuedV, valuedW, operand0)
                .withInstruction(instruction);
        String[] args = new String[] {"-v", "42", "--", "--"};

        // when
        command.execute(args);

        // then
        assertEquals(1, instruction.getCalledArguments().size());
        String[] expectedValues = {"42", null, "--"};
        assertArrayEquals(expectedValues, instruction.getCalledArguments().get(0));
    }

    @Test
    public void guideline_10_operandDelimiter_expectFirstDelimiterParsedAsValue() throws CommandExecutionException {
        // given
        AbstractOptionParser<?> valuedV = ArgumentParsers.valuedOption("-v");
        AbstractOptionParser<?> valuedW = ArgumentParsers.valuedOption("-w");
        AbstractOperandParser<?> operand0 = ArgumentParsers.operand(0);
        TestInstruction instruction = new TestInstruction();
        Command command = Command.forName("do-it")
                .withParsers(valuedV, valuedW, operand0)
                .withInstruction(instruction);
        String[] args = new String[] {"-v", "--", "--"};

        // when
        command.execute(args);

        // then
        assertEquals(1, instruction.getCalledArguments().size());
        String[] expectedValues = {"--", null, null};
        assertArrayEquals(expectedValues, instruction.getCalledArguments().get(0));
    }

    @Test
    public void guideline_11_optionOrderDoesNotMatter() throws CommandExecutionException {
        // given
        AbstractOptionParser<?> valued = ArgumentParsers.valuedOption("-v");
        AbstractOptionParser<?> flag = ArgumentParsers.flagOption("-f");
        TestInstruction instruction = new TestInstruction();
        Command command = Command.forName("do-it")
                .withParsers(valued, flag)
                .withInstruction(instruction);
        String[] argsOrdering1 = new String[] {"-v", "42", "-f"};
        String[] argsOrdering2 = new String[] {"-f", "-v", "42"};

        // when
        command.execute(argsOrdering1);
        command.execute(argsOrdering2);

        // then
        assertEquals(2, instruction.getCalledArguments().size());
        String[] expectedValues = {"42", ""};
        assertTrue(instruction.getCalledArguments().stream().allMatch(actualValues -> Arrays.equals(expectedValues, actualValues)));
    }

    @Test
    public void guideline_12_operandOrderMatters_forCliHats() throws CommandExecutionException {
        // given
        AbstractOperandParser<?> operand0 = ArgumentParsers.operand(0);
        AbstractOperandParser<?> operand1 = ArgumentParsers.operand(1);
        TestInstruction instruction = new TestInstruction();
        Command command = Command.forName("do-it")
                .withParsers(operand0, operand1)
                .withInstruction(instruction);
        String[] argsOrdering1 = new String[] {"42", "blubb"};
        String[] argsOrdering2 = new String[] {"blubb", "42"};

        // when
        command.execute(argsOrdering1);
        command.execute(argsOrdering2);

        // then
        assertEquals(2, instruction.getCalledArguments().size());
        String[] expectedValuesOrdering0 = {"42", "blubb"};
        String[] expectedValuesOrdering1 = {"blubb", "42"};
        assertArrayEquals(expectedValuesOrdering0, instruction.getCalledArguments().get(0));
        assertArrayEquals(expectedValuesOrdering1, instruction.getCalledArguments().get(1));
    }

    static class TestInstruction implements Instruction {

        private final List<Object[]> calledArguments = new ArrayList<>();

        @Override
        public void execute(Object[] args) {
            calledArguments.add(args);
        }

        public List<Object[]> getCalledArguments() {
            return Collections.unmodifiableList(calledArguments);
        }

    }

}
