package io.github.johannesbuchholz.clihats.core.execution;

import io.github.johannesbuchholz.clihats.core.exceptions.CommandCreationException;
import io.github.johannesbuchholz.clihats.core.exceptions.execution.ClientCodeExecutionException;
import io.github.johannesbuchholz.clihats.core.exceptions.execution.CommandExecutionException;
import io.github.johannesbuchholz.clihats.core.exceptions.execution.ParsingException;
import io.github.johannesbuchholz.clihats.core.execution.text.TextMatrix;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Wraps a particular action together with a list of options.
 * <p>
 *     When executed via {@link #execute(String[])}, the received arguments are parsed and passed as arguments
 *     to the specified {@link Instruction}.
 * </p>
 * @see Commander
 * @see AbstractParser
 */
public class Command implements Documented {

    public static final int COMMAND_DESCRIPTION_WIDTH = 80;

    private final Instruction instruction;
    private final List<AbstractParser> parsers;
    private final String description;

    private final Name name;
    private final InputParser inputParser;

    /**
     * A new Command with the specified name. The name will be used to identify this Command and may contain
     * whitespaces. Example of valid names:
     * <ul>
     *     <li>"run-everything"</li>
     *     <li>"get first"</li>
     *     <li>"get all"</li>
     * </ul>
     * The returned command does not possess any parsers and executes the empty Instruction received from
     * {@link Instruction#empty()}.
     * @param name the name of the new Command
     * @return a new command with the specified name.
     * @throws CommandCreationException if the Command could not be created.
     * @throws NullPointerException if the specified name is null.
     */
    public static Command forName(String name) throws CommandCreationException {
        return new Command(Name.from(Objects.requireNonNull(name)), Instruction.empty(), List.of(), "");
    }

    private Command(Name name, Instruction instruction, List<AbstractParser> parsers, String description) throws CommandCreationException {
        this.name = name;
        this.instruction = instruction;
        this.parsers = parsers;
        this.description = description;

        checkForInternalParserConflicts(parsers);
        inputParser = new InputParser(parsers);
    }

    // builder likes

    /**
     * Creates a new Command as a copy of this using the specified parsers.
     * @param parsers the parsers to set.
     * @return a new Command with the specified parsers.
     * @throws NullPointerException if the specified array is null.
     * @see AbstractParser
     */
    public Command withParsers(AbstractParser... parsers) {
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
        ParsingResult parsingResult = inputParser.parse(inputArgs);
        if (!parsingResult.isValid()) {
            throw new ParsingException(this, parsingResult);
        }
        try {
            instruction.execute(parsingResult.getValues());
        } catch (Exception e) {
            throw new ClientCodeExecutionException(this, e);
        }
    }

    /**
     * Returns the identifying name of this command.
     */
    public Name getName() {
        return name;
    }

    /**
     * Returns the normalized name identifier received from {@link #getName()} as a String by joining with one
     * single whitespace.
     * <p>
     *     This method is used to provide a human readable representation of this command.
     * </p>
     */
    public String getDisplayName() {
        return name.toString();
    }

    public String getDescription() {
        return description;
    }

    protected List<String> conflictsWith(Command other) {
        List<String> conflictMessages = new ArrayList<>();
        if (this == other || this.equals(other)) {
            conflictMessages.add(String.format("Command %s is registered multiple times", this));
        }
        conflictMessages.addAll(conflictsWithParsersOf(other));
        return conflictMessages;
    }

    private String generateHelpString() {
        String normalizedName = getDisplayName();
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
                    .sorted(Comparator.comparing(AbstractParser::toString))
                    .map(p -> p.getHelpContent().asTextCells())
                    .forEach(matrixParsers::row);
        }
        return matrixHeader + "\n" + matrixParsers.removeEmptyCols().resizeColumnWidths();
    }

    private void checkForInternalParserConflicts(List<AbstractParser> parsers) throws CommandCreationException {
        List<AbstractParser> processedParsers = new ArrayList<>(parsers.size());
        List<String> conflictsMessages = new ArrayList<>();
        for (AbstractParser parser : parsers) {
            processedParsers.stream()
                    // TODO: Re-implement this method
                    .flatMap(coherent -> Stream.<String>of())
                    .forEach(conflictsMessages::add);
            processedParsers.add(parser);
        }
        if (!conflictsMessages.isEmpty()) {
            throw new CommandCreationException("Can not add parser to %s:\n%s",
                    this, conflictsMessages.stream().map(s -> "    " + s).collect(Collectors.joining("\n"))
            );
        }
    }

    /**
     * Returns a list of messages. One for each conflicting parser of this command with any parsers o of the specified command.
     * To prevent ambiguous command calls, a conflict arises whenever the other command possesses argument parsers with
     * names equals to any name part of this command.
     */
    private List<String> conflictsWithParsersOf(Command other) {
        return other.parsers.stream()
                .map(this::conflictsWith)
                .flatMap(List::stream)
                .map(conflictMsg -> String.format("Command %s conflicts with parsers of command %s: %s", this, other, conflictMsg))
                .collect(Collectors.toList());
    }

    private List<String> conflictsWith(AbstractParser parser) {
        // TODO: implement properly;
        return List.of();
    }

    @Override
    public String getDoc() {
        return generateHelpString();
    }

    @Override
    public String toString() {
        return String.format("%s{name=%s}", this.getClass().getSimpleName(), getDisplayName());
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

    public static class Name {

        private final String[] nameParts;

        static Name from(String rawName) {
            return new Name(rawName.trim().split("\\s+"));
        }

        static Name from(String[] nameParts) {
            return new Name(nameParts);
        }

        private Name(String[] nameParts) {
            this.nameParts = nameParts;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Name name = (Name) o;
            return Arrays.equals(nameParts, name.nameParts);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(nameParts);
        }

        @Override
        public String toString() {
            return String.join(" ", nameParts);
        }
    }

}
