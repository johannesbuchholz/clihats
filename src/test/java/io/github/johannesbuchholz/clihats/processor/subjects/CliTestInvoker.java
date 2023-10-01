package io.github.johannesbuchholz.clihats.processor.subjects;

import io.github.johannesbuchholz.clihats.core.execution.CliException;
import io.github.johannesbuchholz.clihats.processor.GlobalTestResult;
import io.github.johannesbuchholz.clihats.processor.execution.CliHats;

public class CliTestInvoker {

    public static void testGeneratedCliThrowing(Class<?> cliClassToRun, String[] args) throws CliException {
        CliHats.get(cliClassToRun).executeWithThrows(args);
    }

    public static void testGeneratedCli(Class<?> cliClassToRun, String[] args) {
        try {
            CliHats.get(cliClassToRun).executeWithThrows(args);
        } catch (CliException e) {
            GlobalTestResult.setError(e);
        }
    }

}
