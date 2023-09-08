package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.execution.InputArgument;
import io.github.johannesbuchholz.clihats.core.execution.parser.exception.ArgumentParsingException;

public interface ArgsParser {

    Object[] parse(InputArgument[] args) throws ArgumentParsingException;

}
