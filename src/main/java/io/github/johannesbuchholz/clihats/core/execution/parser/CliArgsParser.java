package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.execution.InputArgument;

import java.util.*;
import java.util.stream.Collectors;

public class CliArgsParser implements ArgsParser {

    private final List<ParserToken<AbstractOptionParser<?>>> optionParsers;
    private final List<ParserToken<AbstractOperandParser<?>>> operandParsers;

    public CliArgsParser(List<AbstractArgumentParser<?>> abstractParsers) {
        List<ParserToken<AbstractOptionParser<?>>> optionParsers = new ArrayList<>();
        List<ParserToken<AbstractOperandParser<?>>> operandParsers = new ArrayList<>();
        int targetPosition = 0;
        for (AbstractArgumentParser<?> abstractParser : abstractParsers) {
            if (abstractParser instanceof AbstractOptionParser) {
                optionParsers.add(new ParserToken<>(targetPosition, (AbstractOptionParser<?>) abstractParser));
            } else if (abstractParser instanceof AbstractOperandParser) {
                operandParsers.add(new ParserToken<>(targetPosition, (AbstractOperandParser<?>)abstractParser));
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
        parseOptions(optionParsers, args, parsedValues);
        args = Arrays.stream(args).filter(Objects::nonNull).toArray(InputArgument[]::new);
        // parse operands
        parseOperands(operandParsers, args, parsedValues);

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
    private void parseOptions(Collection<ParserToken<AbstractOptionParser<?>>> tokens, InputArgument[] args, Object[] parsedValues) throws ArgumentParsingException {
        Set<ParserToken<?>> remainingTokens = new HashSet<>(tokens);
        int i = -1;
        while (++i < args.length) {
            Collection<ParserToken<?>> usedTokens = parseArgument(args, i, remainingTokens, parsedValues);
            usedTokens.forEach(remainingTokens::remove);
            if (usedTokens.isEmpty() && args[i] != null && args[i].isBreakSequence()) {
                // remove break-sequence arg
                args[i] = null;
                break;
            }
        }
        putDefaultValues(remainingTokens, parsedValues);
    }

    /**
     * @param tokens Parser tokens for this round.
     * @param args Array of input arguments. May contain null entries indicating that the respective element has already been parsed.
     * @param parsedValues The current parsing result builder to put values into.
     */
    private void parseOperands(Collection<ParserToken<AbstractOperandParser<?>>> tokens, InputArgument[] args, Object[] parsedValues) throws ArgumentParsingException {
        Set<ParserToken<?>> remainingTokens = new HashSet<>(tokens);
        int i = -1;
        while (++i < args.length) {
            parseArgument(args, i, remainingTokens, parsedValues)
                    .forEach(remainingTokens::remove);
        }
        putDefaultValues(remainingTokens, parsedValues);
    }

    /**
     * @return A collection of parsers that found a value
     */
    private Collection<ParserToken<?>> parseArgument(InputArgument[] args, int index, Collection<ParserToken<?>> tokens, Object[] parsedValues) throws ArgumentParsingException {
        Iterator<ParserToken<?>> tokenIterator = tokens.iterator();
        List<ParserToken<?>> usedParsers = new ArrayList<>(tokens.size());
        while (args[index] != null && tokenIterator.hasNext()) {
            ParserToken<?> token = tokenIterator.next();
            ArgumentParsingResult<?> result = token.parser.parse(args, index);
            if (result.isPresent()) {
                // parser could extract a value from the current arg
                parsedValues[token.targetPosition] = result.getValue();
                usedParsers.add(token);
            }
        }
        return usedParsers;
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
