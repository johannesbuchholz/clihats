package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.execution.InputArgument;

public interface ArgsParser {

    Object[] parse(InputArgument[] args) throws ArgumentParsingException;

}
