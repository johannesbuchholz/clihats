package io.github.johannesbuchholz.clihats.processor.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates, that the annotated method is target of a command from a command line interface.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Command {

    /**
     * Name used for calling this command. Defaults to the "hyphenized" name of the annotated method.
     */
    String name() default "";

    /**
     * Description of this command displayed in help strings. This overrides javadoc parsing.
     */
    String description() default "";

    /**
     * The interfaces annotated with {@link CommandLineInterface} this command belongs to.
     */
    Class<?>[] cli() default {};

}
