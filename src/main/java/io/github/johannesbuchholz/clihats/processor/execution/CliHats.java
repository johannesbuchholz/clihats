package io.github.johannesbuchholz.clihats.processor.execution;

import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;
import io.github.johannesbuchholz.clihats.processor.exceptions.ConfigurationException;

/**
 * Entry point for retrieving command-line interfaces via the {@link #get(Class)} method.
 */
public class CliHats {

    private final static AbstractCommanderProvider commanderProvider;

    static {
        try {
            commanderProvider = AbstractCommanderProvider.instantiateImplementation();
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException(String.format("Could not prepare CliHats: Could not initialize class %s: %s", AbstractCommanderProvider.class.getSimpleName(), e.getMessage()), e);
        }
    }

    /**
     * Provides a command-line interface instance created from commands that were linked to the specified class.
     *
     * @param commandLineInterface the class annotated with {@link CommandLineInterface}
     * @return a command-line interface instance containing all commands associated to the provided commandLineInterface.
     */
    public static Cli get(Class<?> commandLineInterface) {
        return commanderProvider
                .getCli(commandLineInterface)
                .orElseThrow(() -> new ConfigurationException("Unknown command-line interface %s.", commandLineInterface.getCanonicalName()));
    }

}
