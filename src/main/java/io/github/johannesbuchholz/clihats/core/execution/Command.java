package io.github.johannesbuchholz.clihats.core.execution;

import io.github.johannesbuchholz.clihats.core.execution.parser.AbstractParser;
import io.github.johannesbuchholz.clihats.core.execution.parser.ArgumentParsingException;
import io.github.johannesbuchholz.clihats.core.execution.parser.InputParser;
import io.github.johannesbuchholz.clihats.core.text.TextMatrix;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Wraps a particular action together with a list of options.
 * <p>
 *     When executed via {@link #execute(String[])}, the received arguments are parsed and passed as arguments
 *     to the specified {@link Instruction}.
 * </p>
 * @see Commander
 * @see AbstractParser
 */
public class Command {

    public static final int COMMAND_DESCRIPTION_WIDTH = 80;

    private final Instruction instruction;
    private final List<AbstractParser<?>> parsers;
    private final String description;

    private final String name;
    private final InputParser inputParser;

    /**
     * A new Command with the specified name. The name will be used to identify this Command and may not contain
     * whitespaces. Example of valid names:
     * <ul>
     *     <li>"run-everything"</li>
     *     <li>"get_first"</li>
     *     <li>"get#all"</li>
     * </ul>
     * The returned command does not possess any parsers and executes the empty Instruction received from
     * {@link Instruction#empty()}.
     * @param name the name of the new Command
     * @return a new command with the specified name.
     * @throws IllegalArgumentException if the command could not be created.
     * @throws NullPointerException if the specified name is null.
     */
    public static Command forName(String name) {
        if (name == null || name.isEmpty() || name.chars().anyMatch(Character::isSpaceChar))
            throw new IllegalArgumentException("Command name must not contain a space character but is '" + name + "'");
        return new Command(name, Instruction.empty(), List.of(), "");
    }

    private static List<AbstractParser<?>> validate(List<AbstractParser<?>> parsers) {
        List<AbstractParser<?>> processedParsers = new ArrayList<>(parsers.size());
        List<String> conflictsMessages = new ArrayList<>();
        for (AbstractParser<?> parser : parsers) {
            processedParsers.forEach(coherent -> coherent.getConflictMessage(parser).ifPresent(conflictsMessages::add));
            processedParsers.add(parser);
        }
        if (!conflictsMessages.isEmpty()) {
            throw new IllegalArgumentException(String.format("Invalid parsers:\n%s",
                    conflictsMessages.stream().map(s -> "    " + s).collect(Collectors.joining("\n")))
            );
        }
        return parsers;
    }

    private Command(String name, Instruction instruction, List<AbstractParser<?>> parsers, String description) {
        this.name = name;
        this.instruction = instruction;
        this.parsers = parsers;
        this.description = description;

        inputParser = new InputParser(validate(parsers));
    }

    // builder likes

    /**
     * Creates a new Command as a copy of this using the specified parsers.
     * @param parsers the parsers to set.
     * @return a new Command with the specified parsers.
     * @throws NullPointerException if the specified array is null.
     * @see AbstractParser
     */
    public Command withParsers(AbstractParser<?>... parsers) {
        return new Command(name, instruction, Arrays.asList(Objects.requireNonNull(parsers)), description);
    }

    /**
     * Creates a new Command as a copy of this using the specified description.
     * @param description the description to set.
     * @return a new Command with the specified description.
     * @throws NullPointerException if the specified description is null.
     */
    public Command withDescription(String description) {
        return new Command(name, instruction, parsers, Objects.requireNonNullElse(description, "").trim());
    }

    /**
     * Creates a new Command as a copy of this using the specified instruction.
     * @param instruction the instruction to set.
     * @return a new Command with the specified instruction.
     * @throws NullPointerException if the specified instruction is null.
     * @see Instruction
     */
    public Command withInstruction(Instruction instruction) {
        return new Command(name, Objects.requireNonNull(instruction), parsers, description);
    }

    // functionality

    /**
     * Parses the specified arguments and invokes the instruction of this command with the received arguments.
     * @param inputArgs the array of options to be parsed and sent to the instruction of this Command.
     * @throws CommandExecutionException if parsing of arguments or execution fails.
     */
    public void execute(String[] inputArgs) throws CommandExecutionException {
        Object[] parsedValues;
        try {
            parsedValues = inputParser.parse(inputArgs);
        } catch (ArgumentParsingException e) {
            throw new InvalidInputArgumentException(this, e);
        }
        try {
            instruction.execute(parsedValues);
        } catch (Exception e) {
            throw new ClientCodeExecutionException(this, e);
        }
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    protected Optional<String> conflictsWith(Command other) {
        String conflictMessage = null;
        if (this.equals(other)) {
            conflictMessage = String.format("Command %s is registered multiple times", this);
        }
        return Optional.ofNullable(conflictMessage);
    }

    private String generateHelpString() {
        String normalizedName = getName();
        TextMatrix matrixHeader = TextMatrix.empty()
                .row(COMMAND_DESCRIPTION_WIDTH, "Help for " + normalizedName);
        if (!description.isEmpty()) {
            matrixHeader
                    .row(COMMAND_DESCRIPTION_WIDTH, description);
        }
        // add option description
        TextMatrix matrixParsers = TextMatrix.empty();
        if (!parsers.isEmpty()) {
            matrixHeader
                    .row()
                    .row(COMMAND_DESCRIPTION_WIDTH, "Options:");
            parsers.stream()
                    .sorted(Comparator.comparing(AbstractParser::getId))
                    .map(p -> p.getHelpContent().asTextCells())
                    .forEach(matrixParsers::row);
        }
        return matrixHeader + "\n" + matrixParsers.removeEmptyCols().resizeColumnWidths();
    }

    public String getDoc() {
        return generateHelpString();
    }

    @Override
    public String toString() {
        return String.format("%s{name=%s}", this.getClass().getSimpleName(), getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Command command = (Command) o;
        return Objects.equals(name, command.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}
