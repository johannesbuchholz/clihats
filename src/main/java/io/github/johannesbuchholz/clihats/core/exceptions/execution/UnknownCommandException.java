package io.github.johannesbuchholz.clihats.core.exceptions.execution;

import io.github.johannesbuchholz.clihats.core.execution.Commander;
import io.github.johannesbuchholz.clihats.util.TextUtils;

public class UnknownCommandException extends CommanderExecutionException {

    private static String generateErrorMessage(String[] inputArgs) {
        return String.format("Could not find command for input arguments %s", TextUtils.indentEveryLine(String.join(" ", inputArgs)));
    }

    public UnknownCommandException(Commander failingCommander, String[] inputArgs) {
        super(failingCommander, generateErrorMessage(inputArgs));
    }

}
