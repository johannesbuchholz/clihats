package io.github.johannesbuchholz.clihats.processor.subjects;

import io.github.johannesbuchholz.clihats.processor.GlobalTestResult;
import io.github.johannesbuchholz.clihats.processor.annotations.Argument;
import io.github.johannesbuchholz.clihats.processor.annotations.Command;
import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;
import io.github.johannesbuchholz.clihats.processor.subjects.misc.MyClass;
import io.github.johannesbuchholz.clihats.processor.subjects.misc.MyClassListMapper;
import io.github.johannesbuchholz.clihats.processor.subjects.misc.MyListMapper;

import java.time.LocalDate;
import java.util.List;

@CommandLineInterface
public class Cli2 {

    @Command(name = "print-all", cli = Cli2.class, description = "prints all input arguments to console")
    public static void somethingForTheSecondCli(
            @Argument(type = Argument.Type.OPERAND) String s1,
            @Argument(type = Argument.Type.OPERAND) String s2,
            @Argument(name = {"-f", "--flag"}, flagValue = "True") Boolean b1,
            @Argument(name = {"-t", "--time"}, defaultValue = "1970-01-01") LocalDate ld,
            @Argument(name = {"-s", "--something", "--smthng", "--whatever-one-wants"}) String s
    ) {
        GlobalTestResult.setSuccess("print-all", s1, s2, b1, ld, s);
    }

    @Command(name = "run", cli = Cli2.class, description = "calls the runner")
    public static void runner() {
        GlobalTestResult.setSuccess("run");
    }

    @Command(name = "repeat", cli = Cli2.class, description = "repeats printing according to the given count")
    public static void repeat(@Argument(type = Argument.Type.OPERAND) Integer i) {
        if (i < 0)
            throw new IllegalArgumentException("Given argument must be non-negative but was " + i);
        for (int n = 0; n < i; n++) {
            System.out.println("count up to i:" + n);
        }
        GlobalTestResult.setSuccess("repeat", i);
    }

    @Command(name = "list", cli = Cli2.class, description = "parses into list separated by ','")
    public static void list(@Argument(name = "-l", mapper = MyListMapper.class) List<String> l) {
        GlobalTestResult.setSuccess("list", l);
    }

    @Command(name = "list-x", cli = Cli2.class, description = "parses into list separated by ','")
    public static void listOfX(@Argument(name = "-l", mapper = MyClassListMapper.class) List<MyClass> lst) {
        GlobalTestResult.setSuccess("list-x", lst);
    }

}
