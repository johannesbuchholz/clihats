package io.github.johannesbuchholz.clihats.core.execution;

import io.github.johannesbuchholz.clihats.core.exceptions.parsing.ValueExtractionException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class InputParser {

    private final int parserCount;
    private final List<AbstractParser> abstractParsers;

    InputParser(List<AbstractParser> abstractParsers) {
        parserCount = abstractParsers.size();
        this.abstractParsers = abstractParsers;
    }

    ParsingResult parse(String[] inputArgs) {
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
        Arrays.stream(args)
                .filter(Objects::nonNull)
                .map(InputArgument::getValue)
                .forEach(parsingResultBuilder::putUnknown);

        return parsingResultBuilder.build();
    }
    
    /**
     * @param tokens Parser tokens for this round.
     * @param args Array of input arguments. May contain null entries indicating that the respective element has already been parsed.
     * @param parsingResultBuilder The current parsing result builder to put values into.
     */
    private void parseNextRound(Collection<ParserToken> tokens, InputArgument[] args, ParsingResult.Builder parsingResultBuilder) {
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
                ArgumentParsingResult result = null;
                try {
                    result = token.abstractParser.parse(args, i);
                } catch (ValueExtractionException e) {
                    parsingResultBuilder.putError(e);
                    usedParsers.add(token);
                }
                if (result != null && result.isPresent()) {
                    // parser could extract a value from the current arg
                    noneFound = false;
                    parsingResultBuilder.putArg(token.targetPosition, result.getValue());
                    usedParsers.add(token);
                }
            }
            usedParsers.forEach(remainingTokens::remove);
            if (noneFound && currentArg.isBreakSequence()) {
                // current arg is unknown to the parsers of this round.
                // Treat not yet parsed arguments prior to the break-sequence as unknown and remove them from following parsing rounds
                for (int j = 0; j < i; j++) {
                    InputArgument arg = args[j];
                    if (arg != null) {
                        parsingResultBuilder.putUnknown(arg.getValue());
                        args[j] = null;
                    }
                }
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
    private void putDefaultValues(Collection<ParserToken> tokens, ParsingResult.Builder parsingResultBuilder) {
        for (ParserToken token : tokens) {
            ArgumentParsingResult defaultResult;
            try {
                defaultResult = token.abstractParser.defaultValue();
            } catch (ValueExtractionException e) {
                parsingResultBuilder.putError(e);
                continue;
            }
            if (defaultResult.isPresent())
                parsingResultBuilder.putArg(token.targetPosition, defaultResult.getValue());
            else
                // parser does not have a default value
                parsingResultBuilder.putMissing(token.abstractParser);
        }

    }

    /**
     * Stores one {@link AbstractParser} and the argument position that parser parses into.
     */
    private static class ParserToken {
        private final int targetPosition;
        private final AbstractParser abstractParser;

        private ParserToken(int targetPosition, AbstractParser abstractParser) {
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
