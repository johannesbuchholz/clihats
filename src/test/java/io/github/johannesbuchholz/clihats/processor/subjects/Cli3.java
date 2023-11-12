package io.github.johannesbuchholz.clihats.processor.subjects;

import io.github.johannesbuchholz.clihats.processor.GlobalTestResult;
import io.github.johannesbuchholz.clihats.processor.annotations.Argument;
import io.github.johannesbuchholz.clihats.processor.annotations.Command;
import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;
import io.github.johannesbuchholz.clihats.processor.subjects.misc.MyClass;
import io.github.johannesbuchholz.clihats.processor.subjects.misc.MyClassListMapper;
import io.github.johannesbuchholz.clihats.processor.subjects.misc.MyClassMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@CommandLineInterface
public class Cli3 {

    @Command(name = "flag-parser", cli = Cli3.class)
    public static void instructionFlagParser(
            @Argument(name = "--fnd", flagValue = "value") String flagNoDefault,
            @Argument(name = "--fd", flagValue = "my-value") String flagWithValue,
            @Argument(name = "-fdd", flagValue = "my-value", defaultValue = "my-default") String flagWithCustomDefaultValue,
            @Argument(name = {"--fa" , "--fa1", "--fa2", "--fa3"}, flagValue = "value") String flagWithAlias,
            @Argument(name = "--fm", flagValue = "some-value", mapper = MyClassMapper.class) MyClass flagWithMapper
    ) {
        GlobalTestResult.setSuccess("flag-parser", flagNoDefault, flagWithCustomDefaultValue, flagWithValue, flagWithAlias, flagWithMapper);
    }

    @Command(name = "name-parser", cli = Cli3.class)
    public static void instructionNameParser(
           @Argument(name = "--nnd") String nameNoDefault,
           @Argument(name = "--nd", defaultValue = "my-default") String nameWithDefault,
           @Argument(name = "--nr", necessity = Argument.Necessity.REQUIRED) String nameRequired,
           @Argument(name = {"--na", "--na1", "--na2", "--na3"}) String nameWithAliases,
           @Argument(name = "--nm", mapper = MyClassMapper.class) MyClass nameWithMapper,
           @Argument(name = "--nml", mapper = MyClassListMapper.class) List<MyClass> nameWithList
    ) {
        GlobalTestResult.setSuccess("name-parser", nameNoDefault, nameWithDefault, nameRequired, nameWithAliases, nameWithMapper, nameWithList);
    }

    @Command(name = "position-parser", cli = Cli3.class)
    public static void instructionPositionParser(
            @Argument(type = Argument.Type.OPERAND) String positional,
            @Argument(name = "--sr1") String someRequired1,
            @Argument(name = "--sr2") String someRequired2,
            @Argument(type = Argument.Type.OPERAND, mapper = MyClassMapper.class) MyClass positionalWithMapper,
            @Argument(name = "--sd", defaultValue = "some-default") String someWithDefault
    ) {
        GlobalTestResult.setSuccess("position-parser", positional, someRequired1, someRequired2, positionalWithMapper, someWithDefault);
    }

    @Command(name = "position-parser-with-default", cli = Cli3.class)
    public static void instructionPositionParserWithDefault(
            @Argument(type = Argument.Type.OPERAND, defaultValue = "position-1-default") String positional,
            @Argument(name = "--sr1") String someRequired1,
            @Argument(name = "--sr2") String someRequired2,
            @Argument(type = Argument.Type.OPERAND, mapper = MyClassMapper.class, necessity = Argument.Necessity.REQUIRED) MyClass positionalWithMapper,
            @Argument(name = "--sd", defaultValue = "some-default") String someWithDefault
    ) {
        GlobalTestResult.setSuccess("position-parser-with-default", positional, someRequired1, someRequired2, positionalWithMapper, someWithDefault);
    }

    @Command(name = "name-parser-with-prompt", cli = Cli3.class)
    public static void instructionNameParserWithPrompt(
            @Argument(name = "-u", necessity = Argument.Necessity.PROMPT) String user,
            @Argument(name = "-p", necessity = Argument.Necessity.MASKED_PROMPT) String password
    ) {
        GlobalTestResult.setSuccess("name-parser-with-prompt", user, password);
    }

    @Command(name = "positional-parser-with-prompt", cli = Cli3.class)
    public static void instructionPositionParserWithPrompt(
            @Argument(type = Argument.Type.OPERAND, necessity = Argument.Necessity.PROMPT) String user,
            @Argument(type = Argument.Type.OPERAND, necessity = Argument.Necessity.MASKED_PROMPT) String password
    ) {
        GlobalTestResult.setSuccess("positional-parser-with-prompt", user, password);
    }

    @Command(name = "parser-with-non-option-parameters", cli = Cli3.class)
    public static void instructionWithNonOptionParameters(
            LocalDateTime nonOption0,
            @Argument(type = Argument.Type.OPERAND) Integer zero,
            String nonOption1,
            BigDecimal nonOption2,
            @Argument(name = "-o") Double o,
            @Argument(name = "-f", flagValue = "True") Boolean flag,
            Integer nonOption3
    ) {
        GlobalTestResult.setSuccess("parser-with-non-option-parameters", nonOption0, zero, nonOption1, nonOption2, o, flag, nonOption3);
    }

}
