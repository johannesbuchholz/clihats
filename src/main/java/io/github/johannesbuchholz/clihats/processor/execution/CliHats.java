package io.github.johannesbuchholz.clihats.processor.execution;

import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;
import io.github.johannesbuchholz.clihats.processor.exceptions.ConfigurationException;

import java.util.Objects;
import java.util.Optional;

/**
 * Entry point for retrieving command-line interfaces via the {@link #get(Class)} method.
 */
public class CliHats {

    private static AbstractCommanderProvider commanderProvider = null;

    static {
        try {
            AbstractCommanderProvider.initializeImplementation();
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("Could not prepare CliHats: Could not initialize class: " + e.getMessage(), e);
        }
    }

    /**
     * Provides a command-line interface instance created from commands that were linked to the specified class.
     *
     * @param commandLineInterface the class annotated with {@link CommandLineInterface}
     * @return a command-line interface instance containing all commands associated to the provided commandLineInterface.
     */
    public static Cli get(Class<?> commandLineInterface) {
        return Optional.ofNullable(commanderProvider)
                .orElseThrow(() -> new ConfigurationException("CliHats could not provide a command-line interface. Make sure to declare at least one CommandLineInterface."))
                .getCli(commandLineInterface)
                .orElseThrow(() -> new ConfigurationException("Unknown command-line interface %s.", commandLineInterface.getCanonicalName()));
    }

    /**
     * May only be called once.
     *
     * @throws IllegalStateException if another AbstractCommanderProvider has already been registered.
     */
    static synchronized void registerCommanderProvider(AbstractCommanderProvider commanderProvider) throws IllegalStateException {
        Objects.requireNonNull(commanderProvider, "Can not register null.");
        if (CliHats.commanderProvider != null) {
            throw new IllegalStateException(
                    String.format("Trying to register AbstractCommanderProvider but there has been one registered already:\nTried to register %s.\nAlready registered %s.",
                            commanderProvider, commanderProvider));
        }
        CliHats.commanderProvider = commanderProvider;
    }

}
