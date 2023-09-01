package io.github.johannesbuchholz.clihats.core.execution;

import io.github.johannesbuchholz.clihats.core.text.TextCell;
import io.github.johannesbuchholz.clihats.core.text.TextMatrix;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final Map<String, Command> commandsByName;
    private final String description;

    /**
     * Creates a new Commander with the specified name.
     *
     * @param name the name of the commander.
     * @return a new Commander.
     */
    public static Commander forName(String name) {
        return new Commander(Objects.requireNonNull(name).trim(), Map.of(), "");
    }

    private Commander(String cliName, Map<String, Command> commandsByName, String description) {
        this.cliName = cliName;
        this.commandsByName = commandsByName;
        this.description = description;
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
        Map<String, Command> commandMap = Arrays.stream(commands)
                .collect(Collectors.toUnmodifiableMap(Command::getName, Function.identity()));
        return new Commander(cliName, commandMap, description);
    }

    /**
     * Creates a copy of this with the specified description.
     *
     * @param description the new description.
     * @return a new Commander with the specified description.
     * @throws CommanderCreationException if the commander could not be created.
     */
    public Commander withDescription(String description) {
        return new Commander(cliName, commandsByName, Objects.requireNonNull(description).trim());
    }

    /**
     * Derives the matching command from the first given arguments and executes that command using the remaining
     * arguments.
     *
     * @throws CommanderExecutionException if the execution fails.
     * @throws CliHelpCallException        if the user input requests help.
     */
    public void execute(String[] inputArgs) throws CommanderExecutionException, CliHelpCallException {
        Optional<Command> commandSearchResult;
        if (inputArgs.length > 0) {
            commandSearchResult = getCommand(inputArgs[0]);
        } else {
            commandSearchResult = Optional.empty();
        }
        if (isHelpCall(inputArgs)) {
            String actualHelpString;
            if (commandSearchResult.isEmpty())
                actualHelpString = getDoc();
            else
                actualHelpString = commandSearchResult.get().getDoc();
            throw new CliHelpCallException(actualHelpString);
        }
        if (commandSearchResult.isEmpty())
            throw new UnknownCommandException(this, inputArgs[0]);

        try {
            commandSearchResult.get().execute(Arrays.copyOfRange(inputArgs, 1, inputArgs.length));
        } catch (CommandExecutionException e) {
            throw new CommanderExecutionException(this, e);
        }
    }

    /**
     * true, iff the last input argument is among {@link #helpArgs}
     */
    private boolean isHelpCall(String[] inputArgs) {
        return inputArgs.length == 0 || Arrays.stream(inputArgs).anyMatch(helpArgs::contains);
    }

    private void checkForCommandConflicts(Command... commands) throws CommanderCreationException {
        List<Command> processedCommands = new ArrayList<>(commands.length);
        List<String> conflictMessages = new LinkedList<>();
        for (Command command : commands) {
            processedCommands.forEach(coherent -> coherent.conflictsWith(command).ifPresent(conflictMessages::add));
            processedCommands.add(command);
        }
        if (!conflictMessages.isEmpty()) {
            throw new CommanderCreationException(String.format("Detected conflicts among commands:\n%s",
                    conflictMessages.stream().map(s -> "    " + s).collect(Collectors.joining("\n")))
            );
        }
    }

    private Optional<Command> getCommand(String commandName) {
       return Optional.ofNullable(commandsByName.get(commandName));
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
        if (!commandsByName.isEmpty()) {
            matrixHeader.row(TextCell.getNew("Commands:"));
            commandsByName.values().stream()
                    .sorted(Comparator.comparing(Command::getName))
                    .forEach(command ->
                            matrixCommands.row(new int[]{COMMAND_NAME_WIDTH, COMMAND_DESCRIPTION_WIDTH}, command.getName(), command.getDescription())
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
                this.getClass().getSimpleName(), cliName, commandsByName.keySet().stream().sorted().collect(Collectors.toList()));
    }

}
