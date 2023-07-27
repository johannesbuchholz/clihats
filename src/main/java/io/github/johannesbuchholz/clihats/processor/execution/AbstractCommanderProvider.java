package io.github.johannesbuchholz.clihats.processor.execution;

import io.github.johannesbuchholz.clihats.core.execution.Commander;
import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractCommanderProvider {

    public static String getAbstractMethodName() {
        return "supplyCommanderByCli";
    }

    public static String getImplementationPackageName() {
        return AbstractCommanderProvider.class.getPackage().getName();
    }

    public static String getImplementationSimpleName() {
        return "CommanderProvider";
    }

    public static String getImplementationQualifiedName() {
        return getImplementationPackageName() + "." + getImplementationSimpleName();
    }

    static AbstractCommanderProvider instantiateImplementation() throws ClassNotFoundException {
        Class<? extends AbstractCommanderProvider> abstractCommanderProviderType = AbstractCommanderProvider.class.getClassLoader()
                .loadClass(getImplementationQualifiedName())
                .asSubclass(AbstractCommanderProvider.class);
        try {
            return (AbstractCommanderProvider) MethodHandles.publicLookup()
                    .findConstructor(abstractCommanderProviderType, MethodType.methodType(void.class))
                    .invoke();
        } catch (Throwable e) {
            throw new IllegalStateException("Could not instantiate implementation: " + e.getMessage(), e);
        }
    }

    public AbstractCommanderProvider() {}

    /**
     * @return Mapping of qualified names of classes annotated with {@link CommandLineInterface} to Commander objects.
     */
    abstract Map<String, Commander> supplyCommanderByCli();

    Optional<Cli> getCli(Class<?> cliAnnotatedClass) {
        Commander commander = supplyCommanderByCli().get(cliAnnotatedClass.getCanonicalName());
        return Optional.ofNullable(commander).map(Cli::new);
    }

    @Override
    public String toString() {
        return String.format("%s=[%s]",
                this.getClass().getSimpleName(),
                supplyCommanderByCli().entrySet().stream()
                        .map(entry -> entry.getKey() + " -> " + entry.getValue())
                        .sorted()
                        .collect(Collectors.joining(", "))
        );
    }

}
