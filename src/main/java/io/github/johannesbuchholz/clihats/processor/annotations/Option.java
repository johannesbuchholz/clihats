package io.github.johannesbuchholz.clihats.processor.annotations;

import io.github.johannesbuchholz.clihats.core.execution.parser.FlagOptionParser;
import io.github.johannesbuchholz.clihats.core.execution.parser.OperandParser;
import io.github.johannesbuchholz.clihats.core.execution.parser.ValuedOptionParser;
import io.github.johannesbuchholz.clihats.processor.mapper.AbstractValueMapper;
import io.github.johannesbuchholz.clihats.processor.mapper.defaults.NoMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Determines how input arguments are parsed and assigned to the parameters of the annotated method.
 * <p>Set exactly on of the following parameters: {@link #position()} or {@link #name()}</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface Option {

    /**
     * The position of the option in the arg array to be parsed starting at position 0.
     * <p>If set, a positional option declaration is assumed.</p>
     * @see OperandParser
     */
    int position() default -1;

    /**
     * Name and aliases of the option.
     * @see FlagOptionParser
     * @see ValuedOptionParser
     */
    String[] name() default {};

    /**
     * If non-empty, indicates that this option represents a flag-type option.
     * <p>This flag-option will return the specified value if present and {@code null} or a specified default value otherwise.</p>
     * @see FlagOptionParser
     */
    String flagValue() default "";

    /**
     * Determines behaviour in case this option is missing from the original input options.
     * <p>Ignored if {@link #flagValue()} is set.</p>
     */
    OptionNecessity necessity() default OptionNecessity.OPTIONAL;

    /**
     * Returns the specified value whenever this option is missing.
     * <p>Ignored if {@link #necessity()} is set to {@link OptionNecessity#REQUIRED}.</p>
     */
    String defaultValue() default "";

    /**
     * A custom option mapper that is applied to parsed values.
     */
    Class<? extends AbstractValueMapper<?>> mapper() default NoMapper.class;

    /**
     * The description of this option.
     */
    String description() default "";
}
