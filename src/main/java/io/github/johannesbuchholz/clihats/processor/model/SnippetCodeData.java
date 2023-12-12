package io.github.johannesbuchholz.clihats.processor.model;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class SnippetCodeData {

    final String codeSnippet;
    final Set<String> importPackages;

    public static SnippetCodeData empty() {
        return new SnippetCodeData("", Set.of());
    }

    public static SnippetCodeData from(String codeSnippet) {
        return new SnippetCodeData(codeSnippet, Set.of());
    }

    public static SnippetCodeData from(String codeSnippet, Set<String> importPackages) {
        return new SnippetCodeData(codeSnippet, importPackages);
    }

    SnippetCodeData(String codeSnippet, Set<String> importPackages) {
        this.codeSnippet = Objects.requireNonNull(codeSnippet);
        this.importPackages = Objects.requireNonNull(importPackages);
    }

    public Set<String> getImportPackages() {
        return Collections.unmodifiableSet(importPackages);
    }

    public String getCodeSnippet() {
        return codeSnippet;
    }

    public boolean isEmpty() {
        return codeSnippet.isEmpty();
    }

    public boolean hasContent() {
        return !isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SnippetCodeData that = (SnippetCodeData) o;
        return codeSnippet.equals(that.codeSnippet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codeSnippet);
    }

    @Override
    public String toString() {
        return "SnippetCodeData{" +
                "codeSnippet='" + codeSnippet + '\'' +
                ", importPackages=" + importPackages +
                '}';
    }
}
