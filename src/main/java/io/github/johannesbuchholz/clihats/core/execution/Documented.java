package io.github.johannesbuchholz.clihats.core.execution;

/**
 * Implementing classes are able to provide a help-string or description.
 */
@FunctionalInterface
public interface Documented {

    /**
     * @return the documentation of this.
     */
    String getDoc();

}
