package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.execution.InputArgument;
import io.github.johannesbuchholz.clihats.core.execution.ParsingResult;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InputParser {

    private final int parserCount;
    private final List<AbstractParser<?>> abstractParsers;

    public InputParser(List<AbstractParser<?>> abstractParsers) {
        parserCount = abstractParsers.size();
        this.abstractParsers = abstractParsers;
    }

    public ParsingResult parse(String[] inputArgs) throws ArgumentParsingException {
        ParsingResult.Builder parsingResultBuilder = ParsingResult.builder(parserCount);

        // collect tokens
        Map<Integer, List<ParserToken>> tokensByPriority = IntStream.range(0, abstractParsers.size())
                .mapToObj(i -> new ParserToken(i, abstractParsers.get(i)))
                .collect(Collectors.groupingBy(token -> token.abstractParser.getParsingPriority()));
        // parse options
        InputArgument[] args = Arrays.stream(inputArgs).map(InputArgument::of).toArray(InputArgument[]::new);
        List<Integer> ascendingPriorities = tokensByPriority.keySet().stream().sorted().collect(Collectors.toList());
        for (int priority : ascendingPriorities) {
            parseNextRound(tokensByPriority.get(priority), args, parsingResultBuilder);
            args = Arrays.stream(args).filter(Objects::nonNull).toArray(InputArgument[]::new);
        }
        // mark remaining args as unknown
        List<InputArgument> unknownInputArguments = Arrays.stream(args)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!unknownInputArguments.isEmpty())
            throw new UnknownArgumentException(unknownInputArguments);

        return parsingResultBuilder.build();
    }
    
    /**
     * @param tokens Parser tokens for this round.
     * @param args Array of input arguments. May contain null entries indicating that the respective element has already been parsed.
     * @param parsingResultBuilder The current parsing result builder to put values into.
     */
    private void parseNextRound(Collection<ParserToken> tokens, InputArgument[] args, ParsingResult.Builder parsingResultBuilder) throws ArgumentParsingException {
        Set<ParserToken> remainingTokens = new HashSet<>(tokens);
        List<ParserToken> usedParsers = new ArrayList<>(tokens.size());

        for (int i = 0; i < args.length; i++) {
            boolean noneFound = true;
            InputArgument currentArg = args[i];
            if (currentArg == null)
                // skip already parsed args
                continue;
            for (ParserToken token : remainingTokens) {
                if (args[i] == null)
                    // argument position has already been parsed by previous parsers
                    break;
                ArgumentParsingResult<?> result = token.abstractParser.parse(args, i);
                if (result.isPresent()) {
                    // parser could extract a value from the current arg
                    noneFound = false;
                    parsingResultBuilder.putArg(token.targetPosition, result.getValue());
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
        putDefaultValues(remainingTokens, parsingResultBuilder);
    }

    /**
     * Apply default values for specified parsers.
     */
    private void putDefaultValues(Collection<ParserToken> tokens, ParsingResult.Builder parsingResultBuilder) throws ArgumentParsingException {
        for (ParserToken token : tokens) {
            ArgumentParsingResult<?> defaultResult;
            defaultResult = token.abstractParser.defaultValue();
            if (defaultResult.isPresent()) {
                parsingResultBuilder.putArg(token.targetPosition, defaultResult.getValue());
            } else {
                // parser does not have a default value
                throw new MissingArgumentException(token.abstractParser);
            }
        }
    }

    /**
     * Stores one {@link AbstractParser} and the argument position that parser parses into.
     */
    private static class ParserToken {
        private final int targetPosition;
        private final AbstractParser<?> abstractParser;

        private ParserToken(int targetPosition, AbstractParser<?> abstractParser) {
            this.targetPosition = targetPosition;
            this.abstractParser = abstractParser;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ParserToken that = (ParserToken) o;
            return targetPosition == that.targetPosition;
        }

        @Override
        public int hashCode() {
            return Objects.hash(targetPosition);
        }

        @Override
        public String toString() {
            return targetPosition + " <- " + abstractParser.toString();
        }
    }

}
