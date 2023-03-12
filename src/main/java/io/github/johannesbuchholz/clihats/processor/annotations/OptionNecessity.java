package io.github.johannesbuchholz.clihats.processor.annotations;

/**
 * Determines how clihats reacts if this option is missing.
 */
public enum OptionNecessity {

    /**
     * Indicates that the annotated parameter is not required. Clihats will set the annotated parameter to null if a
     * call to the associated method does not contain this option.
     */
    OPTIONAL,
    /**
     * Indicates that the annotated parameter is mandatory. Clihats will throw an exception if a call to the associated
     * method does not contain this option.
     */
    REQUIRED,
    /**
     * Indicates that the annotated parameter is mandatory. Clihats will ask for console input if a call to the
     * associated method does not contain this option.
     */
    PROMPT,
    /**
     * Indicates that the annotated parameter is mandatory. Clihats will ask for console input if a call to the
     * associated method does not contain this option. The actual typed console input is not displayed.
     */
    MASKED_PROMPT

}
