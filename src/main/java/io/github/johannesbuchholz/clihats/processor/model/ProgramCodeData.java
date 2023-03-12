package io.github.johannesbuchholz.clihats.processor.model;

public class ProgramCodeData {

    private final String classFileContent;
    private final String qualifiedClassName;

    public ProgramCodeData(String classFileContent, String qualifiedClassName) {
        this.classFileContent = classFileContent;
        this.qualifiedClassName = qualifiedClassName;
    }

    public String getClassFileContent() {
        return classFileContent;
    }

    public String getQualifiedClassName() {
        return qualifiedClassName;
    }

}
