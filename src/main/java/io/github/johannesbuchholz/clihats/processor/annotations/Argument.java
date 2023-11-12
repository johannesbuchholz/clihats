package io.github.johannesbuchholz.clihats.processor.annotations;

import io.github.johannesbuchholz.clihats.core.execution.parser.FlagOptionParser;
import io.github.johannesbuchholz.clihats.processor.mapper.AbstractValueMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Determines how input arguments are parsed and passed to the parameters of the annotated method.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface Argument {

    /**
     * The type of the command line argument from which a value is obtained.
     */
    Type type() default Type.OPTION;

    /**
     * The name of this argument.
     * <p>
     *     If left empty, defaults to the names deduced from the annotated method parameter name in the following way:
     *     <p>
     *          Assume parameter name {@code myParameter}
     *     <ul>
     *         <li>For an option type argument: {@code {"-m", "--my-parameter}}</li>
     *         <li>For an operand type argument: {@code MY_PARAMETER}</li>
     *     </ul>
     *     </p>
     * </p>
     */
    String[] name() default {};

    /**
     * If non-empty, indicates that this option represents a flag-type option.
     * <p>
     *     If set to a non-empty String, CliHats will return the specified value whenever one of the names from
     *     {@link #name()} is among the provided command line arguments.
     * </p>
     * <p>Ignored if {@link #type()} is not set to {@link Type#OPTION}.</p>
     * @see FlagOptionParser
     */
    String flagValue() default "";

    /**
     * Determines behaviour whenever a suiting value is not among the command line arguments.
     * <p>Ignored if {@link #flagValue()} is non-empty.</p>
     */
    Necessity necessity() default Necessity.OPTIONAL;

    /**
     * Returns the specified value whenever the provided command line arguments do not contain a suiting value.
     * <p>Ignored if {@link #necessity()} is not set to {@link Necessity#OPTIONAL}.</p>
     */
    String defaultValue() default "";

    /**
     * A custom option mapper that is applied to parsed values.
     */
    Class<? extends AbstractValueMapper<?>> mapper() default AbstractValueMapper.IdentityMapper.class;

    /**
     * The description of this option.
     */
    String description() default "";

    /**
     * Determines how CliHats reacts if an argument value is not provided.
     */
    enum Necessity {

        /**
         * Indicates that the annotated parameter is not required. If set, CliHats passes a default value to the annotated
         * parameter whenever the provided command line arguments do not contain a value for this argument.
         */
        OPTIONAL,

        /**
         * Indicates that the annotated parameter is mandatory. If set, CliHats aborts processing whenever the provided
         * command line arguments do not contain a value for this argument.
         */
        REQUIRED,

        /**
         * Indicates that the annotated parameter is mandatory. If set, CliHats will ask for console input whenever the
         * provided command line arguments do not contain a value for this argument.
         */
        PROMPT,

        /**
         * Indicates that the annotated parameter is mandatory. If set, CliHats will ask for console input whenever the
         * provided command line arguments do not contain a value for this argument.
         * The actual typed console input is not displayed.
         */
        MASKED_PROMPT

    }

    /**
     * Denotes the type of the command line argument to watch out for.
     * <p>
     * The available types follow the
     * <a href=https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/V1_chap12.html>POSIX utility guideline</a>
     * which mainly differentiates command line argument into "options" and "operands".
     * </p>
     */
    enum Type {

        /**
         * Denotes that the annotated method parameter obtains its value from an option command line argument.
         * The value taken into account is determined by the names specified in {@link Argument#name()}.
         */
        OPTION,

        /**
         * Denotes that the annotated method parameter obtains its value from a single operand command line argument.
         * The value is taken from the operand array index equal to the position of this annotation among the other
         * operand typed arguments.
         */
        OPERAND,

        /**
         * Denotes that the annotated method parameter obtains its value as an array consisting of all operand
         * command line arguments.
         * <p>The annotated method parameter must be an array.</p>
         */
        ARRAY_OPERAND

    }

}
