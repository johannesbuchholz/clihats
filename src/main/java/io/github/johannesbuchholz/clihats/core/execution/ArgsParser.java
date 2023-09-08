package io.github.johannesbuchholz.clihats.core.execution;

import io.github.johannesbuchholz.clihats.core.execution.exception.ArgumentParsingException;

public interface ArgsParser {

    Object[] parse(InputArgument[] args) throws ArgumentParsingException;

}
