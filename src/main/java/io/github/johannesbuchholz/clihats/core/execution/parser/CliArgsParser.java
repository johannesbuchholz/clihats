package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.execution.InputArgument;

import java.util.*;
import java.util.stream.Collectors;

public class CliArgsParser implements ArgsParser {

    private final List<ParserToken<AbstractArgumentParser<?>>> optionParsers;
    private final List<ParserToken<AbstractArgumentParser<?>>> operandParsers;

    public CliArgsParser(List<AbstractArgumentParser<?>> abstractParsers) {
        List<ParserToken<AbstractArgumentParser<?>>> optionParsers = new ArrayList<>();
        List<ParserToken<AbstractArgumentParser<?>>> operandParsers = new ArrayList<>();
        int targetPosition = 0;
        for (AbstractArgumentParser<?> abstractParser : abstractParsers) {
            if (abstractParser instanceof AbstractOptionParser) {
                optionParsers.add(new ParserToken<>(targetPosition, abstractParser));
            } else if (abstractParser instanceof AbstractOperandParser) {
                operandParsers.add(new ParserToken<>(targetPosition, abstractParser));
            } else {
                throw new IllegalArgumentException("Encountered parser of unknown type " + abstractParser.getClass());
            }
            targetPosition++;
        }
        this.optionParsers = optionParsers;
        this.operandParsers = operandParsers;
    }

    @Override
    public Object[] parse(InputArgument[] args) throws ArgumentParsingException {
        Object[] parsedValues = new Object[optionParsers.size() + operandParsers.size()];

        // parse options
        parseNextRound(optionParsers, args, parsedValues);
        args = Arrays.stream(args).filter(Objects::nonNull).toArray(InputArgument[]::new);

        // parse operands
        parseNextRound(operandParsers, args, parsedValues);

        // mark remaining args as unknown
        List<InputArgument> unknownInputArguments = Arrays.stream(args)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!unknownInputArguments.isEmpty())
            throw new UnknownArgumentException(unknownInputArguments);

        return parsedValues;
    }

    // TODO: Simplify by now relying on separation of parser types
    /**
     * @param tokens Parser tokens for this round.
     * @param args Array of input arguments. May contain null entries indicating that the respective element has already been parsed.
     * @param parsedValues The current parsing result builder to put values into.
     */
    private void parseNextRound(List<ParserToken<AbstractArgumentParser<?>>> tokens, InputArgument[] args, Object[] parsedValues) throws ArgumentParsingException {
        Set<ParserToken<?>> remainingTokens = new HashSet<>(tokens);
        List<ParserToken<?>> usedParsers = new ArrayList<>(tokens.size());

        for (int i = 0; i < args.length; i++) {
            boolean noneFound = true;
            InputArgument currentArg = args[i];
            if (currentArg == null)
                // skip already parsed args
                continue;
            for (ParserToken<?> token : remainingTokens) {
                if (args[i] == null)
                    // argument position has already been parsed by previous parsers
                    break;
                ArgumentParsingResult<?> result = token.parser.parse(args, i);
                if (result.isPresent()) {
                    // parser could extract a value from the current arg
                    noneFound = false;
                    parsedValues[token.targetPosition] = result.getValue();
                    usedParsers.add(token);
                }
            }
            usedParsers.forEach(remainingTokens::remove);
            if (noneFound && currentArg.isBreakSequence()) {
                // current arg is unknown to the parsers of this round.
                // Treat not yet parsed arguments prior to the break-sequence as unknown
                List<InputArgument> unknownInputArguments = Arrays.stream(args).limit(i).filter(Objects::nonNull).collect(Collectors.toList());
                if (!unknownInputArguments.isEmpty())
                    throw new UnknownArgumentException(unknownInputArguments);
                // remove break-sequence arg
                args[i] = null;
                break;
            }
        }
        putDefaultValues(remainingTokens, parsedValues);
    }

    /**
     * Apply default values for specified parsers.
     */
    private void putDefaultValues(Collection<ParserToken<?>> tokens, Object[] parsingResultBuilder) throws ArgumentParsingException {
        for (ParserToken<?> token : tokens) {
            ArgumentParsingResult<?> defaultResult;
            defaultResult = token.parser.defaultValue();
            if (defaultResult.isPresent()) {
                parsingResultBuilder[token.targetPosition] = defaultResult.getValue();
            } else {
                // parser does not have a default value
                throw new MissingArgumentException(token.parser);
            }
        }
    }

    /**
     * Stores one {@link AbstractArgumentParser} and the argument position that parser parses into.
     * @param <T> the stored parser type
     */
    private static class ParserToken<T extends AbstractArgumentParser<?>> {
        private final int targetPosition;
        private final T parser;

        private ParserToken(int targetPosition, T parser) {
            this.targetPosition = targetPosition;
            this.parser = parser;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ParserToken<?> that = (ParserToken<?>) o;
            return targetPosition == that.targetPosition;
        }

        @Override
        public int hashCode() {
            return Objects.hash(targetPosition);
        }

        @Override
        public String toString() {
            return targetPosition + " <- " + parser.toString();
        }
    }

}
