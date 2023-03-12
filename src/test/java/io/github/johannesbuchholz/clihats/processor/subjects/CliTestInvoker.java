package io.github.johannesbuchholz.clihats.processor.subjects;

import io.github.johannesbuchholz.clihats.core.exceptions.execution.CliException;
import io.github.johannesbuchholz.clihats.processor.GlobalTestResult;
import io.github.johannesbuchholz.clihats.processor.execution.CliHats;

public class CliTestInvoker {

    public static void testGeneratedCli(Class<?> cliClassToRun, String[] args) {
        CliHats.get(cliClassToRun).execute(args);
    }

    public static void testGeneratedCliWithThrows(Class<?> cliClassToRun, String[] args) {
        try {
            CliHats.get(cliClassToRun).executeWithThrows(args);
        } catch (CliException e) {
            GlobalTestResult.setError(e);
        }
    }

}
