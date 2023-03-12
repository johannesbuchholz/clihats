package io.github.johannesbuchholz.clihats.processor.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Contains additional code snippets as "baggage".
 * <p>Such baggage might consist of annotation-code like {@code @SuppressWarnings("unchecked")} that is not directly
 * associated with the actual code contained in this snippet but should be included on a higher level code part.</p>
 */
public class ExtendedSnippetCodeData extends SnippetCodeData {

    Set<SnippetCodeData> additionalSnippets = new HashSet<>();

    public static ExtendedSnippetCodeData from(String codeSnippet, Set<String> importPackages) {
        return new ExtendedSnippetCodeData(codeSnippet, importPackages);
    }

    ExtendedSnippetCodeData(String codeSnippet, Set<String> importPackages) {
        super(codeSnippet, importPackages);
    }

    public ExtendedSnippetCodeData setBaggage(Set<SnippetCodeData> snippetCodeData) {
        additionalSnippets = Collections.unmodifiableSet(Objects.requireNonNull(snippetCodeData));
        return this;
    }

    public Set<SnippetCodeData> getBaggage() {
        return additionalSnippets;
    }
}
