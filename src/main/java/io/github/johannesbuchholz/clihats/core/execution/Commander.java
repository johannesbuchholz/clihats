package io.github.johannesbuchholz.clihats.core.execution;

import io.github.johannesbuchholz.clihats.core.exceptions.CommanderCreationException;
import io.github.johannesbuchholz.clihats.core.exceptions.execution.CliHelpCallException;
import io.github.johannesbuchholz.clihats.core.exceptions.execution.CommandExecutionException;
import io.github.johannesbuchholz.clihats.core.exceptions.execution.CommanderExecutionException;
import io.github.johannesbuchholz.clihats.core.exceptions.execution.UnknownCommandException;
import io.github.johannesbuchholz.clihats.core.execution.text.TextCell;
import io.github.johannesbuchholz.clihats.core.execution.text.TextMatrix;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A collection of commands. This class is responsible for invoking and passing arguments to particular commands.
 * <p>
 * When executed via {@link #execute(String[])}, the received arguments are parsed to determine the matching command
 * which then is executed with the remaining arguments.
 * </p>
 *
 * @see Command
 */
public class Commander implements Documented {

    private static final int COMMANDER_DESCRIPTION_WIDTH = 100;
    private static final int COMMAND_NAME_WIDTH = 24;
    private static final int COMMAND_DESCRIPTION_WIDTH = 76;
    private final List<String> helpArgs = List.of("--help");
    private final String cliName;
    // FIXME: Make this a map form String[] to Command, to not use the concatenated name internally.
    private final Map<String, Command> commands;
    private final String description;

    private Commander(String cliName, Map<String, Command> commands, String description) {
        this.cliName = cliName;
        this.commands = commands;
        this.description = description;
    }

    /**
     * Creates a new Commander with the specified name.
     *
     * @param name the name of the commander.
     * @return a new Commander.
     */
    public static Commander forName(String name) {
        return new Commander(Objects.requireNonNull(name).trim(), Map.of(), "");
    }

    private static Map<String, Command> generateCommandMap(Command[] commands) {
        Map<String, Command> map = new HashMap<>(commands.length);
        for (Command c : commands) {
            if (c == null) {
                throw new IllegalArgumentException("Encountered null");
            }
            String key = c.getNormalizedName();
            if (map.containsKey(key)) {
                throw new IllegalArgumentException("Encountered multiple commands with identical name identifier: " + Arrays.toString(c.getNameIdentifier()));
            } else {
                map.put(key, c);
            }
        }
        return map;
    }

    /**
     * Creates a copy of this with the specified array of Commands.
     *
     * @param commands the new commands.
     * @return a new Commander with the specified Commands.
     * @throws CommanderCreationException if the commander could not be created.
     */
    public Commander withCommands(Command... commands) throws CommanderCreationException {
        checkForCommandConflicts(commands);
        return new Commander(cliName, generateCommandMap(commands), description);
    }

    /**
     * Creates a copy of this with the specified description.
     *
     * @param description the new description.
     * @return a new Commander with the specified description.
     * @throws CommanderCreationException if the commander could not be created.
     */
    public Commander withDescription(String description) {
        return new Commander(cliName, commands, Objects.requireNonNull(description).trim());
    }

    /**
     * Derives the matching command from the first given arguments and executes that command using the remaining
     * arguments.
     *
     * @throws CommanderExecutionException if the execution fails.
     * @throws CliHelpCallException        if the user input requests help.
     */
    public void execute(String[] inputArgs) throws CommanderExecutionException, CliHelpCallException {
        CommandSearchResult commandSearchResult = findCommand(inputArgs);
        if (isHelpCall(inputArgs)) {
            throwHelpException(commandSearchResult);
        } else if (commandSearchResult.isEmpty()) {
            throw new UnknownCommandException(this, inputArgs);
        }

        try {
            commandSearchResult.foundCommand.execute(Arrays.copyOfRange(inputArgs, commandSearchResult.offset, inputArgs.length));
        } catch (CommandExecutionException e) {
            throw new CommanderExecutionException(this, e);
        }
    }

    /**
     * true, iff the last input argument is among {@link #helpArgs}
     */
    private boolean isHelpCall(String[] inputArgs) {
        return inputArgs.length == 0 || helpArgs.contains(inputArgs[inputArgs.length - 1]);
    }

    private void checkForCommandConflicts(Command... commands) throws CommanderCreationException {
        List<Command> processedCommands = new ArrayList<>(commands.length);
        List<String> conflictMessages = new LinkedList<>();
        for (Command command : commands) {
            Stream.concat(
                    processedCommands.stream().flatMap(coherent -> coherent.conflictsWith(command).stream()),
                    command.conflictsWith(helpArgs).stream()
            ).forEach(conflictMessages::add);
            processedCommands.add(command);
        }
        if (!conflictMessages.isEmpty()) {
            throw new CommanderCreationException("Detected conflicts among commands:\n%s",
                    conflictMessages.stream().map(s -> "    " + s).collect(Collectors.joining("\n"))
            );
        }
    }

    private CommandSearchResult findCommand(String[] inputArgs) {
        Command foundCommand = null;
        int pointer = 0;
        while (foundCommand == null && pointer < inputArgs.length) {
            String nameCandidate = trimAndConcat(Arrays.copyOfRange(inputArgs, 0, ++pointer));
            foundCommand = commands.get(nameCandidate);
        }
        return new CommandSearchResult(foundCommand, pointer);
    }


    private String trimAndConcat(String[] stringParts) {
        return Arrays.stream(stringParts)
                .map(String::trim)
                .collect(Collectors.joining(" "));
    }

    private void throwHelpException(CommandSearchResult commandSearchResult) throws CliHelpCallException {
        String actualHelpString;
        if (commandSearchResult.isEmpty())
            actualHelpString = getDoc();
        else
            actualHelpString = commandSearchResult.foundCommand.getDoc();
        throw new CliHelpCallException(actualHelpString);
    }

    private String generateHelpString() {
        TextMatrix matrixHeader = TextMatrix.empty();
        // add description
        matrixHeader.row(TextCell.getNew("Help for " + cliName));
        if (!description.isBlank()) {
            matrixHeader.row(COMMANDER_DESCRIPTION_WIDTH, description);
        }
        matrixHeader.row();
        // add commands
        TextMatrix matrixCommands = TextMatrix.empty();
        if (!commands.isEmpty()) {
            matrixHeader.row(TextCell.getNew("Commands:"));
            commands.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> matrixCommands
                            .row(new int[]{COMMAND_NAME_WIDTH, COMMAND_DESCRIPTION_WIDTH}, entry.getKey(), entry.getValue().getDescription())
                    );
        }
        return matrixHeader + "\n" +
                matrixCommands.removeEmptyCols().resizeColumnWidths();
    }

    /**
     * @return the name of this Commander.
     */
    public String getName() {
        return cliName;
    }

    @Override
    public String getDoc() {
        return generateHelpString();
    }

    /**
     * @return value of {@link #getName()}.
     */
    @Override
    public String toString() {
        return String.format("%s={name=%s, commands=%s}",
                this.getClass().getSimpleName(), cliName, commands.keySet().stream().sorted().collect(Collectors.toList()));
    }

    private static class CommandSearchResult {

        private final Command foundCommand;
        private final int offset;

        public CommandSearchResult(Command foundCommand, int offset) {
            this.foundCommand = foundCommand;
            this.offset = offset;
        }

        public boolean isEmpty() {
            return foundCommand == null;
        }

    }
}
