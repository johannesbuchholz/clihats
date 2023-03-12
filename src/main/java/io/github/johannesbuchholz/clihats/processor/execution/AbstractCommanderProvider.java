package io.github.johannesbuchholz.clihats.processor.execution;

import io.github.johannesbuchholz.clihats.core.execution.Commander;
import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractCommanderProvider {

    public static final String IMPL_CLASS_NAME = "CommanderProvider";
    public static final String IMPL_ABSTRACT_METHOD_NAME = "supplyCommanderByCli";
    public static final String IMPL_REGISTER_METHOD_NAME = "registerForClihats";

    /**
     * Load generated implementation in order to execute static initializers.
     */
    static void initializeImplementation() throws ClassNotFoundException {
        ClassLoader classLoader = CliHats.class.getClassLoader();
        Class<?> aClass = classLoader.loadClass(AbstractCommanderProvider.class.getPackage().getName() + "." + IMPL_CLASS_NAME);
        Class.forName(aClass.getCanonicalName(), true, classLoader);
    }

    /**
     * Makes this available to Clihats.
     */
    protected void registerForClihats() {
        CliHats.registerCommanderProvider(this);
    }

    /**
     * @return Commander by qualified name of the associated class annotated with {@link CommandLineInterface}.
     */
    abstract Map<String, Commander> supplyCommanderByCli();

    Optional<Cli> getCli(Class<?> cliAnnotatedClass) {
        Commander commander = supplyCommanderByCli().get(cliAnnotatedClass.getCanonicalName());
        return Optional.ofNullable(commander).map(Cli::getNew);
    }

    @Override
    public String toString() {
        return String.format("%s=[%s]",
                this.getClass().getSimpleName(),
                supplyCommanderByCli()
                        .entrySet()
                        .stream()
                        .map(entry -> entry.getKey() + " -> " + entry.getValue())
                        .sorted()
                        .collect(Collectors.joining(", "))
        );
    }

}
