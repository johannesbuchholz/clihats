package io.github.johannesbuchholz.clihats.processor.annotations;

import io.github.johannesbuchholz.clihats.processor.execution.CliHats;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a command line interface upon the annotated class, representing a collection of referencing commands.
 * <p>To obtain this command line interface, call {@link CliHats#get(Class)}</p>
 * <p>The actual annotated class only acts as an anchor for commands to be registered. It does not add meaning to
 * the annotated class beyond borrowing its qualified name.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface CommandLineInterface {

    String name() default "";

    /**
     * Description of this command line interface displayed in generated help strings. This overrides javadoc parsing.
     */
    String description() default "";

}
