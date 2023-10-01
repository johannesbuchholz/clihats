package io.github.johannesbuchholz.clihats.processor.subjects;

import io.github.johannesbuchholz.clihats.processor.GlobalTestResult;
import io.github.johannesbuchholz.clihats.processor.annotations.Argument;
import io.github.johannesbuchholz.clihats.processor.annotations.Command;
import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;


@CommandLineInterface
public class CliTestingNaming {

    @Command(cli = CliTestingNaming.class)
    public static void methodWithoutCommandName(
            @Argument(name = "-a") String a
    ) {
        GlobalTestResult.setSuccess("method-without-command-name", a);
    }

    @Command(name = "command-with-name", cli = CliTestingNaming.class)
    public static void methodWithName(
            @Argument String valuedWithoutName
    ) {
        GlobalTestResult.setSuccess("command-with-name", valuedWithoutName);
    }

    @Command(name = "command-with-name-2", cli = CliTestingNaming.class)
    public static void methodWithName2(
            @Argument(flagValue = "flag-value") String flagWithoutName
    ) {
        GlobalTestResult.setSuccess("command-with-name-2", flagWithoutName);
    }

    @Command(cli = CliTestingNaming.class)
    public static void allTogether(
            @Argument String valuedWithoutName,
            @Argument(flagValue = "flag-value") String flagWithoutName
    ) {
        GlobalTestResult.setSuccess("all-together", valuedWithoutName, flagWithoutName);
    }

}
