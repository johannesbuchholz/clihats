package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.execution.InputArgument;

import java.util.Collection;

/**
 * Thrown when an input argument could not be parsed by any parser.
 */
public class UnknownArgumentException extends ArgumentParsingException {

    UnknownArgumentException(Collection<InputArgument> inputArguments) {
        super("Unknown input arguments " + inputArguments);
    }

}
