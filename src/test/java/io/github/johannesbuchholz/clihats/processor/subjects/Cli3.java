package io.github.johannesbuchholz.clihats.processor.subjects;

import io.github.johannesbuchholz.clihats.processor.GlobalTestResult;
import io.github.johannesbuchholz.clihats.processor.annotations.Command;
import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;
import io.github.johannesbuchholz.clihats.processor.annotations.Option;
import io.github.johannesbuchholz.clihats.processor.annotations.OptionNecessity;
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
            @Option(name = "-a", flagValue = "value") String flagNoDefault,
            @Option(name = "-b", flagValue = "my-value") String flagWithValue,
            @Option(name = "-c", flagValue = "my-value", defaultValue = "my-default") String flagWithCustomDefaultValue,
            @Option(name = {"-a" , "--a1", "--a2", "-fa3"}, flagValue = "value") String flagWithAlias,
            @Option(name = "-fm", flagValue = "some-value", mapper = MyClassMapper.class) MyClass flagWithMapper
    ) {
        GlobalTestResult.setSuccess("flag-parser", flagNoDefault, flagWithCustomDefaultValue, flagWithValue, flagWithAlias, flagWithMapper);
    }

    @Command(name = "name-parser", cli = Cli3.class)
    public static void instructionNameParser(
           @Option(name = "-nnd") String nameNoDefault,
           @Option(name = "-nd", defaultValue = "my-default") String nameWithDefault,
           @Option(name = "-nr", necessity = OptionNecessity.REQUIRED) String nameRequired,
           @Option(name = {"-na", "-na1", "-na2", "-na3"}) String nameWithAliases,
           @Option(name = "-nm", mapper = MyClassMapper.class) MyClass nameWithMapper,
           @Option(name = "-nml", mapper = MyClassListMapper.class) List<MyClass> nameWithList
    ) {
        GlobalTestResult.setSuccess("name-parser", nameNoDefault, nameWithDefault, nameRequired, nameWithAliases, nameWithMapper, nameWithList);
    }

    @Command(name = "position-parser", cli = Cli3.class)
    public static void instructionPositionParser(
            @Option(position = 1) String positional,
            @Option(name = "-sr1") String someRequired1,
            @Option(name = "-sr2") String someRequired2,
            @Option(position = 0, mapper = MyClassMapper.class) MyClass positionalWithMapper,
            @Option(name = "-sd", defaultValue = "some-default") String someWithDefault
    ) {
        GlobalTestResult.setSuccess("position-parser", positional, someRequired1, someRequired2, positionalWithMapper, someWithDefault);
    }

    @Command(name = "position-parser-with-default", cli = Cli3.class)
    public static void instructionPositionParserWithDefault(
            @Option(position = 1, defaultValue = "position-1-default") String positional,
            @Option(name = "-sr1") String someRequired1,
            @Option(name = "-sr2") String someRequired2,
            @Option(position = 0, mapper = MyClassMapper.class, necessity = OptionNecessity.REQUIRED) MyClass positionalWithMapper,
            @Option(name = "-sd", defaultValue = "some-default") String someWithDefault
    ) {
        GlobalTestResult.setSuccess("position-parser-with-default", positional, someRequired1, someRequired2, positionalWithMapper, someWithDefault);
    }

    @Command(name = "name-parser-with-prompt", cli = Cli3.class)
    public static void instructionNameParserWithPrompt(
            @Option(name = "-u", necessity = OptionNecessity.PROMPT) String user,
            @Option(name = "-pw", necessity = OptionNecessity.MASKED_PROMPT) String password
    ) {
        GlobalTestResult.setSuccess("name-parser-with-prompt", user, password);
    }

    @Command(name = "positional-parser-with-prompt", cli = Cli3.class)
    public static void instructionPositionParserWithPrompt(
            @Option(position = 0, necessity = OptionNecessity.PROMPT) String user,
            @Option(position = 1, necessity = OptionNecessity.MASKED_PROMPT) String password
    ) {
        GlobalTestResult.setSuccess("positional-parser-with-prompt", user, password);
    }

    @Command(name = "parser-with-non-option-parameters", cli = Cli3.class)
    public static void instructionWithNonOptionParameters(
            LocalDateTime nonOption0,
            @Option(position = 0) Integer zero,
            String nonOption1,
            BigDecimal nonOption2,
            @Option(name = "-o") Double o,
            @Option(name = "-f", flagValue = "True") Boolean flag,
            Integer nonOption3
    ) {
        GlobalTestResult.setSuccess("parser-with-non-option-parameters", nonOption0, zero, nonOption1, nonOption2, o, flag, nonOption3);
    }

}
