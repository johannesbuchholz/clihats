package io.github.johannesbuchholz.clihats.processor;

import io.github.johannesbuchholz.clihats.processor.subjects.Cli1;
import io.github.johannesbuchholz.clihats.processor.subjects.Cli2;
import io.github.johannesbuchholz.clihats.processor.subjects.CliTestInvoker;
import io.github.johannesbuchholz.clihats.processor.subjects.misc.MyClass;
import org.junit.Test;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * These tests rely on sequential execution.
 */
public class ProcessorTest {

    @Test
    public void testCliInterfaceImpl_command1() {
        // given
        String[] args = {"command1", "--a3", "some/path/blubb", "some-input", "--aa2", "789"};

        // when
        CliTestInvoker.testGeneratedCli(Cli1.class, args);

        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("command1",  "some-input", 789, Path.of("some/path/blubb"));
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

    @Test
    public void testCliInterfaceImpl_command1_with_default() {
        // given
        String[] args = {"command1", "some-input", "--aa2", "789"};

        // when
        CliTestInvoker.testGeneratedCli(Cli1.class, args);

        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("command1",  "some-input", 789, Path.of("/my/default/path"));
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

    @Test
    public void testCliInterfaceImpl_command2() {
        // given
        String[] args = {"command2", "--opt", "-r", "required-input"};

        // when
        CliTestInvoker.testGeneratedCli(Cli1.class, args);

        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("command2", "required-input", "Option-On");
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

    @Test
    public void testCliInterfaceImpl_command2_with_default() {
        // given
        String[] args = {"command2", "-r", "my-required-input"};

        // when
        CliTestInvoker.testGeneratedCli(Cli1.class, args);

        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("command2", "my-required-input", null);
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

    @Test
    public void testCliInterface2Impl_printall() {
        // given
        String[] args = {"print-all", "vanilla", "input", "-f", "-t", "2022-01-01", "--something", "optional-input"};

        // when
        CliTestInvoker.testGeneratedCli(Cli2.class, args);

        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("print-all",
                "vanilla", "input", true, LocalDate.parse("2022-01-01"), "optional-input"
        );
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

    @Test
    public void testCliInterface2Impl_printall_defaults() {
        // given
        String[] args = {"print-all", "input", "vanilla"};

        // when
        CliTestInvoker.testGeneratedCli(Cli2.class, args);

        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("print-all",
                "input", "vanilla", null, LocalDate.parse("1970-01-01"), null
        );
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

    @Test
    public void testCliInterface2_mapToListOfString() {
        // given
        String[] args = {"list", "-l", "one, two, three, and another"};

        // when
        CliTestInvoker.testGeneratedCli(Cli2.class, args);

        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("list", List.of("one", "two", "three", "and another"));
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

    @Test
    public void testCliInterface2_mapToListOfX() {
        // given
        String[] args = {"list-x", "-l", "one, two, three, and another"};

        // when
        CliTestInvoker.testGeneratedCli(Cli2.class, args);

        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("list-x",
                List.of(
                        new MyClass("one"),
                        new MyClass("two"),
                        new MyClass("three"),
                        new MyClass("and another")
                )
        );
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

}
