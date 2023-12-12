package io.github.johannesbuchholz.clihats.processor;

import io.github.johannesbuchholz.clihats.processor.annotations.Argument;
import io.github.johannesbuchholz.clihats.processor.annotations.Command;
import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;
import io.github.johannesbuchholz.clihats.processor.execution.CliHats;
import io.github.johannesbuchholz.clihats.processor.subjects.MyClass;
import io.github.johannesbuchholz.clihats.processor.subjects.MyClassListMapper;
import io.github.johannesbuchholz.clihats.processor.subjects.MyListMapper;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * These tests rely on sequential execution.
 */
@CommandLineInterface
public class ProcessorTest {

    @Command(name = "print-all", cli = ProcessorTest.class, description = "prints all input arguments to console")
    public static void somethingForTheSecondCli(
            @Argument(type = Argument.Type.OPERAND) String s1,
            @Argument(type = Argument.Type.OPERAND) String s2,
            @Argument(name = {"-f", "--flag"}, flagValue = "True") Boolean b1,
            @Argument(name = {"-t", "--time"}, defaultValue = "1970-01-01") LocalDate ld,
            @Argument(name = {"-s", "--something", "--smthng", "--whatever-one-wants"}) String s
    ) {
        result.put("print-all", s1, s2, b1, ld, s);
    }

    @Command(name = "list", cli = ProcessorTest.class, description = "parses into list separated by ','")
    public static void list(@Argument(name = "-l", mapper = MyListMapper.class) List<String> l) {
        result.put("list", l);
    }

    @Command(name = "list-x", cli = ProcessorTest.class, description = "parses into list separated by ','")
    public static void listOfX(@Argument(name = "-l", mapper = MyClassListMapper.class) List<MyClass> lst) {
        result.put("list-x", lst);
    }

    /**
     * This is the first method that is called by {@link ProcessorTest}, when invoked with the right arguments.
     * This is another line of text. One will never know.
     */
    @Command(name = "command1", cli = ProcessorTest.class)
    public static void myMethod1(
            @Argument(type = Argument.Type.OPERAND) String arg1,
            @Argument(name = {"--a2", "--aa2"}) Integer arg2,
            @Argument(name = "--a3", defaultValue = "/my/default/path") Path arg3
    ) {
        result.put("command1", arg1, arg2, arg3);
    }

    @Command(name = "command2", cli = ProcessorTest.class)
    public static void myMethod2(
            @Argument(name = "-r", necessity = Argument.Necessity.REQUIRED) String r,
            @Argument(name = "--opt", flagValue = "Option-On", description = "This is a lengthy description for a string argument.") String arg1
    ) {
        result.put("command2", r, arg1);
    }

    private static final ReusableTestResult result = new ReusableTestResult();

    @Before
    public void setup() {
        result.clear();
    }

    @Test
    public void testCliInterfaceImpl_command1() {
        // given
        String[] args = {"command1", "--a3", "some/path/blubb", "some-input", "--aa2", "789"};

        // when
        CliHats.get(ProcessorTest.class).execute(args);

        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("command1",  "some-input", 789, Path.of("some/path/blubb"));
        assertEquals(expected, result.getAndClear());
    }

    @Test
    public void testCliInterfaceImpl_command1_with_default() {
        // given
        String[] args = {"command1", "some-input", "--aa2", "789"};

        // when
        CliHats.get(ProcessorTest.class).execute(args);

        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("command1",  "some-input", 789, Path.of("/my/default/path"));
        assertEquals(expected, result.getAndClear());
    }

    @Test
    public void testCliInterfaceImpl_command2() {
        // given
        String[] args = {"command2", "--opt", "-r", "required-input"};

        // when
        CliHats.get(ProcessorTest.class).execute(args);

        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("command2", "required-input", "Option-On");
        assertEquals(expected, result.getAndClear());
    }

    @Test
    public void testCliInterfaceImpl_command2_with_default() {
        // given
        String[] args = {"command2", "-r", "my-required-input"};

        // when
        CliHats.get(ProcessorTest.class).execute(args);

        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("command2", "my-required-input", null);
        assertEquals(expected, result.getAndClear());
    }

    @Test
    public void testCliInterface2Impl_printall() {
        // given
        String[] args = {"print-all", "vanilla", "input", "-f", "-t", "2022-01-01", "--something", "optional-input"};

        // when
        CliHats.get(ProcessorTest.class).execute(args);

        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("print-all",
                "vanilla", "input", true, LocalDate.parse("2022-01-01"), "optional-input"
        );
        assertEquals(expected, result.getAndClear());
    }

    @Test
    public void testCliInterface2Impl_printall_defaults() {
        // given
        String[] args = {"print-all", "input", "vanilla"};

        // when
        CliHats.get(ProcessorTest.class).execute(args);

        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("print-all",
                "input", "vanilla", null, LocalDate.parse("1970-01-01"), null
        );
        assertEquals(expected, result.getAndClear());
    }

    @Test
    public void testCliInterface2_mapToListOfString() {
        // given
        String[] args = {"list", "-l", "one, two, three, and another"};

        // when
        CliHats.get(ProcessorTest.class).execute(args);

        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("list", List.of("one", "two", "three", "and another"));
        assertEquals(expected, result.getAndClear());
    }

    @Test
    public void testCliInterface2_mapToListOfX() {
        // given
        String[] args = {"list-x", "-l", "one, two, three, and another"};

        // when
        CliHats.get(ProcessorTest.class).execute(args);

        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("list-x",
                List.of(
                        new MyClass("one"),
                        new MyClass("two"),
                        new MyClass("three"),
                        new MyClass("and another")
                )
        );
        assertEquals(expected, result.getAndClear());
    }

}
