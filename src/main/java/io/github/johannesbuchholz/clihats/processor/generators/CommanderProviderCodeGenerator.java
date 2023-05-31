package io.github.johannesbuchholz.clihats.processor.generators;

import io.github.johannesbuchholz.clihats.processor.CommandLineInterfaceProcessor;
import io.github.johannesbuchholz.clihats.processor.execution.AbstractCommanderProvider;
import io.github.johannesbuchholz.clihats.processor.model.CommanderDto;
import io.github.johannesbuchholz.clihats.processor.model.ExtendedSnippetCodeData;
import io.github.johannesbuchholz.clihats.processor.model.ProgramCodeData;
import io.github.johannesbuchholz.clihats.processor.model.SnippetCodeData;
import io.github.johannesbuchholz.clihats.util.ProcessingUtils;
import io.github.johannesbuchholz.clihats.util.TextUtils;

import javax.annotation.processing.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CommanderProviderCodeGenerator {

    protected static final String LINE_INDENT = " ".repeat(4);
    protected static final String LINE_INDENT_DOUBLE = " ".repeat(8);
    protected static final String NEW_LINE_INDENT_DOUBLE = "\n" + LINE_INDENT_DOUBLE;

    private static final Set<String> MANDATORY_IMPORTS = ProcessingUtils.getPackageStrings(
            Generated.class, Override.class, Map.class
    );
    private static final String CLASS_CODE_TEMPLATE = "package %s;\n" +
            "\n" +
            "%s\n" +
            "\n" +
            "%s\n" +
            "public class %s extends %s {\n" +
            "\n" +
            "%s\n" +
            "\n" +
            "%s\n" +
            "\n" +
            "%s\n" +
            "\n" +
            "}";
    private static final String GENERATED_FIELD_NAME = "commanderByCliName";
    private static final String MAP_TYPE_STRING = "Map<String, Commander>";

    private final ProcessingEnvironment processingEnvironment;
    private final List<CommanderDto> commanderDtos;

    public CommanderProviderCodeGenerator(ProcessingEnvironment processingEnv, List<CommanderDto> commanderDtos) {
        this.processingEnvironment = processingEnv;
        this.commanderDtos = commanderDtos;
    }

    public ProgramCodeData generateCommanderProviderCode() {
        Map<String, ExtendedSnippetCodeData> commanderCodeSnippetsByCliName = commanderDtos.stream()
                .collect(Collectors.toMap(
                        commanderDto -> commanderDto.getAnnotatedInterface().getQualifiedName().toString(),
                        commanderDto -> new CommanderCodeGenerator(processingEnvironment, commanderDto).generateCommanderCode()
                ));
        SnippetCodeData fieldCodeData = generateFieldCode(commanderCodeSnippetsByCliName);

        Set<String> imports = MANDATORY_IMPORTS;
        imports.addAll(fieldCodeData.getImportPackages());

        String packageName = AbstractCommanderProvider.class.getPackage().getName();

        String classFileContent = String.format(
                CLASS_CODE_TEMPLATE,
                packageName,
                generateImportStringLines(imports, packageName),
                generateClassAnnotationCode(),
                AbstractCommanderProvider.IMPL_CLASS_NAME,
                AbstractCommanderProvider.class.getSimpleName(),
                TextUtils.indentEveryLine(generateStaticInitializer()),
                TextUtils.indentEveryLine(fieldCodeData.getCodeSnippet()),
                TextUtils.indentEveryLine(generateGetterCode())
        );

        return new ProgramCodeData(classFileContent, packageName + "." + AbstractCommanderProvider.IMPL_CLASS_NAME);
    }

    private String generateStaticInitializer() {
        return String.format("static {\n%s\n}",
                LINE_INDENT + String.format("new %s().%s();",
                        AbstractCommanderProvider.IMPL_CLASS_NAME, AbstractCommanderProvider.IMPL_REGISTER_METHOD_NAME));
    }

    private SnippetCodeData generateFieldCode(Map<String, ExtendedSnippetCodeData> commanderCodeSnippets) {
        StringBuilder sb = new StringBuilder("private final " + MAP_TYPE_STRING + " " + GENERATED_FIELD_NAME + " = Map.ofEntries(");
        HashSet<String> imports = new HashSet<>();
        List<String> commanderCodeStrings = new ArrayList<>();
        Set<String> requestedAnnotations = new HashSet<>();
        commanderCodeSnippets.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    ExtendedSnippetCodeData snippetCodeData = entry.getValue();
                    imports.addAll(snippetCodeData.getImportPackages());
                    String mapEntryString = "Map.entry("
                            + NEW_LINE_INDENT_DOUBLE + TextUtils.quote(entry.getKey()) + ", " +
                            "\n" + TextUtils.indentEveryLine(snippetCodeData.getCodeSnippet(), LINE_INDENT_DOUBLE) + ")";
                    commanderCodeStrings.add("\n" + TextUtils.indentEveryLine(mapEntryString, LINE_INDENT_DOUBLE));
                    snippetCodeData.getBaggage().forEach(snippet -> {
                        requestedAnnotations.add(snippet.getCodeSnippet());
                        imports.addAll(snippet.getImportPackages());
                    });
                });
        if (!commanderCodeStrings.isEmpty())
            sb.append(String.join(", " + NEW_LINE_INDENT_DOUBLE, commanderCodeStrings));
        sb.append(");");

        return SnippetCodeData.from(String.join("\n", requestedAnnotations) + "\n" + sb, imports);
    }

    private String generateGetterCode() {
        return "@Override\n" +
                "public " + MAP_TYPE_STRING + " " + AbstractCommanderProvider.IMPL_ABSTRACT_METHOD_NAME + "() {\n" +
                LINE_INDENT + "return " + GENERATED_FIELD_NAME + ";\n" +
                "}";
    }

    private String generateClassAnnotationCode() {
        return "@Generated(" + NEW_LINE_INDENT_DOUBLE +
                "value = \"" + CommandLineInterfaceProcessor.class.getCanonicalName() + "\"," + NEW_LINE_INDENT_DOUBLE +
                "date = \"" + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()) + "\"," + NEW_LINE_INDENT_DOUBLE +
                "comments = \"" + "Implementation version " + CommandLineInterfaceProcessor.IMPLEMENTATION_VERSION +
                ", " + "Java " + CommandLineInterfaceProcessor.JAVA_VERSION + " (" + CommandLineInterfaceProcessor.JAVA_VENDOR + ")" + "\"\n" +
                ")";
    }

    private String generateImportStringLines(Set<String> imports, String packageName) {
        return imports.stream().sorted()
                .filter(importString -> isNotInSamePackage(importString, packageName))
                .map(importString -> "import " + importString + ";")
                .collect(Collectors.joining("\n"));
    }

    private boolean isNotInSamePackage(String fullTypeName, String packageName) {
        int indexOfLastPart = fullTypeName.lastIndexOf(".");
        return !fullTypeName.substring(0, indexOfLastPart).equals(packageName);
    }

}
