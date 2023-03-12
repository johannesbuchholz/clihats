package io.github.johannesbuchholz.clihats.core.execution;

import io.github.johannesbuchholz.clihats.core.execution.parser.OptionParsingResult;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * When running {@link Command#execute(String[])}, the array of String values is parsed during multiple parsing rounds.
 * <p>
 *     Each parser belongs to exactly one parsing round. Each round, all remaining input arguments are passed to each
 *     parser associated with the current round. If some parser finds its matching argument, both the parser and the
 *     argument are no longer considered for succeeding rounds.
 * </p>
 */
class InputParser {

    private final int parserCount;
    private final Map<Integer, List<ParserToken>> parserTokensByRoundWeight;
    private final List<Integer> sortedWeights;

    InputParser(List<AbstractOptionParser<?>> abstractOptionParsers) {
        parserCount = abstractOptionParsers.size();
        parserTokensByRoundWeight = getParserTokensByRoundWeight(abstractOptionParsers);
        sortedWeights = parserTokensByRoundWeight.keySet().stream().sorted().collect(Collectors.toList());
    }

    ParsingResult parse(String[] inputArgs) {
        // init
        ParsingResult.Builder parsingResultBuilder = ParsingResult.builder(parserCount);
        List<String> remainingInputArgs = new LinkedList<>(Arrays.asList(inputArgs));

        // parse round-wise
        for (int roundWeight : sortedWeights)
            parseNextRound(parserTokensByRoundWeight.get(roundWeight), remainingInputArgs, parsingResultBuilder);

        // check for unparsed string args
        remainingInputArgs.forEach(parsingResultBuilder::putUnknown);
        return parsingResultBuilder.build();
    }

    /**
     * @param parserTokens         parsers for this round
     * @param remainingInputArgs   list of input arguments that remained from the previous round
     * @param parsingResultBuilder the current parsing result builder to put values into.
     */
    private void parseNextRound(List<ParserToken> parserTokens, List<String> remainingInputArgs, ParsingResult.Builder parsingResultBuilder) {
        List<String> usedArguments = new ArrayList<>(remainingInputArgs.size());
        for (ParserToken parserToken : parserTokens) {
            OptionParsingResult optionParsingResult = parserToken.abstractOptionParser.parse(remainingInputArgs);
            if (optionParsingResult.isError())
                parsingResultBuilder.putError(optionParsingResult.getCause());
            else if (optionParsingResult.isFound())
                parsingResultBuilder.putArg(parserToken.targetPosition, optionParsingResult.getValue());
            else
                parsingResultBuilder.putMissing(parserToken.abstractOptionParser);
            usedArguments.addAll(optionParsingResult.getParsedInputArguments());
        }
        remainingInputArgs.removeAll(usedArguments);
    }

    /**
     * parsing order -> parser token
     * <p>
     * A map is needed to pop positions from the list of parsers.
     */
    private Map<Integer, List<ParserToken>> getParserTokensByRoundWeight(List<AbstractOptionParser<?>> abstractOptionParsers) {
        return IntStream.range(0, abstractOptionParsers.size())
                .mapToObj(i -> new ParserToken(i, abstractOptionParsers.get(i)))
                .collect(Collectors.groupingBy(parserToken -> parserToken.abstractOptionParser.getParsingOrderWeight()));
    }

    /**
     * Stores one {@link AbstractOptionParser} and the argument position that parser parses into.
     */
    private static class ParserToken {
        private final int targetPosition;
        private final AbstractOptionParser<?> abstractOptionParser;

        private ParserToken(int targetPosition, AbstractOptionParser<?> abstractOptionParser) {
            this.targetPosition = targetPosition;
            this.abstractOptionParser = abstractOptionParser;
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
            return targetPosition + " <- " + abstractOptionParser.toString();
        }
    }

}
