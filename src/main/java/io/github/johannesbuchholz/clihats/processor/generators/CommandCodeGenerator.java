package io.github.johannesbuchholz.clihats.processor.generators;

import io.github.johannesbuchholz.clihats.core.execution.Command;
import io.github.johannesbuchholz.clihats.core.execution.Instruction;
import io.github.johannesbuchholz.clihats.processor.CommandLineInterfaceProcessor;
import io.github.johannesbuchholz.clihats.processor.annotations.Option;
import io.github.johannesbuchholz.clihats.processor.exceptions.ConfigurationException;
import io.github.johannesbuchholz.clihats.processor.model.CommandDto;
import io.github.johannesbuchholz.clihats.processor.model.ExtendedSnippetCodeData;
import io.github.johannesbuchholz.clihats.processor.model.OptionAnnotationDto;
import io.github.johannesbuchholz.clihats.processor.model.SnippetCodeData;
import io.github.johannesbuchholz.clihats.processor.util.JavadocUtils;
import io.github.johannesbuchholz.clihats.processor.util.ProcessingUtils;
import io.github.johannesbuchholz.clihats.processor.util.TextUtils;
import io.github.johannesbuchholz.clihats.processor.util.visitors.ArrayOfSimpleAnnotationValueVisitor;
import io.github.johannesbuchholz.clihats.processor.util.visitors.EnumAnnotationValueVisitor;
import io.github.johannesbuchholz.clihats.processor.util.visitors.SimpleValueAnnotationValueVisitor;
import io.github.johannesbuchholz.clihats.processor.util.visitors.TypeAnnotationValueVisitor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.*;
import java.util.stream.Collectors;

public class CommandCodeGenerator {

    private static final String INSTRUCTION_PARAMETER_NAME = "args";

    private final ProcessingEnvironment processingEnvironment;
    private final String name;
    private final String description;
    private final ExecutableElement annotatedMethod;

    private final String originIdentifier;

    /*
    Might contain null entries. A null entry corresponds to a method parameter that is not annotated with @Option.
     */
    private final List<OptionCodeGenerator> optionCodeGenerators;

    public CommandCodeGenerator(ProcessingEnvironment processingEnvironment, CommandDto commandDto) throws ConfigurationException {
        this.processingEnvironment = processingEnvironment;

        annotatedMethod = commandDto.getAnnotatedMethod();
        name = commandDto.getName();
        description = commandDto.getDescription();

        List<OptionAnnotationDto> optionAnnotationDtos = extractOptionDtos(commandDto.getAnnotatedMethod(), processingEnvironment);
        originIdentifier = ProcessingUtils.generateOriginIdentifier(annotatedMethod);
        validateMethod(originIdentifier, annotatedMethod, optionAnnotationDtos);

        optionCodeGenerators = gatherOptionCodeGenerators(
                optionAnnotationDtos,
                annotatedMethod.getParameters(),
                originIdentifier,
                processingEnvironment
        );

    }

    private static List<OptionAnnotationDto> extractOptionDtos(ExecutableElement annotatedMethod, ProcessingEnvironment processingEnvironment) {
        String docComment = processingEnvironment.getElementUtils().getDocComment(annotatedMethod);
        final Map<String, String> paramDescriptionByName = new HashMap<>();
        if (docComment != null)
            paramDescriptionByName.putAll(JavadocUtils.extractParamDoc(docComment));

        return annotatedMethod.getParameters().stream()
                .map(ve -> mapToOptionDto(ve, paramDescriptionByName, processingEnvironment))
                .collect(Collectors.toList());
    }

    /**
     * Returns {@code null} if the given method parameter does not possess an annotation of type {@link Option}.
     */
    private static OptionAnnotationDto mapToOptionDto(VariableElement methodParameter, Map<String, String> paramDescriptionByName, ProcessingEnvironment processingEnvironment) {
        return methodParameter.getAnnotationMirrors().stream()
                .filter(am -> processingEnvironment.getTypeUtils().isSameType(am.getAnnotationType(), CommandLineInterfaceProcessor.optionAnnotationType.asType()))
                .findFirst()
                .map(annotationMirror -> mapToOptionDto(
                        annotationMirror,
                        paramDescriptionByName.getOrDefault(methodParameter.getSimpleName().toString(), ""),
                        processingEnvironment))
                .orElse(null);
    }

    private static OptionAnnotationDto mapToOptionDto(AnnotationMirror optionMirror, String javadocParamDescription, ProcessingEnvironment processingEnvironment) {
        Map<String, ? extends AnnotationValue> valuesByFieldName = processingEnvironment.getElementUtils().getElementValuesWithDefaults(optionMirror)
                .entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().getSimpleName().toString(), Map.Entry::getValue));
        return mapToOptionDto(valuesByFieldName, javadocParamDescription, processingEnvironment);
    }

    private static OptionAnnotationDto mapToOptionDto(Map<String, ? extends AnnotationValue> valuesByFieldName, String javadocParamDescription, ProcessingEnvironment processingEnvironment) {
        String descriptionFromAnnotation = valuesByFieldName.get(OptionAnnotationDto.DESCRIPTION_FIELD_NAME).accept(new SimpleValueAnnotationValueVisitor<>(String.class), null);
        return new OptionAnnotationDto(
                valuesByFieldName.get(OptionAnnotationDto.POSITION_FIELD_NAME).accept(new SimpleValueAnnotationValueVisitor<>(Integer.class), null),
                valuesByFieldName.get(OptionAnnotationDto.NAME_FIELD_NAME).accept(new ArrayOfSimpleAnnotationValueVisitor<>(String.class), null),
                valuesByFieldName.get(OptionAnnotationDto.FLAG_FIELD_NAME).accept(new SimpleValueAnnotationValueVisitor<>(String.class), null),
                valuesByFieldName.get(OptionAnnotationDto.DEFAULT_FIELD_NAME).accept(new SimpleValueAnnotationValueVisitor<>(String.class), null),
                valuesByFieldName.get(OptionAnnotationDto.MAPPER_FIELD_NAME).accept(new TypeAnnotationValueVisitor(), processingEnvironment.getTypeUtils()),
                valuesByFieldName.get(OptionAnnotationDto.NECESSITY_FIELD_NAME).accept(new EnumAnnotationValueVisitor(), null),
                descriptionFromAnnotation.isEmpty() ? javadocParamDescription : descriptionFromAnnotation
        );
    }

    private static void validateMethod(String originIdentifier, ExecutableElement annotatedMethod, List<OptionAnnotationDto> options) throws ConfigurationException {
        List<String> errorMessages = new ArrayList<>();
        if (annotatedMethod.getReturnType().getKind() != TypeKind.VOID)
            errorMessages.add("Method must have return type kind void but has return type kind " + annotatedMethod.getReturnType().getKind());
        if (!annotatedMethod.getModifiers().containsAll(List.of(Modifier.PUBLIC, Modifier.STATIC)))
            errorMessages.add("Annotated method is not public or not static: " + annotatedMethod.getModifiers());
        if (annotatedMethod.getParameters().size() != options.size())
            errorMessages.add("Method parameter count and declared option count differs: " +
                    "parameters " + annotatedMethod.getParameters().size() + ", declared: " + options.size()
            );

        List<VariableElement> primitiveParameters = annotatedMethod.getParameters().stream().filter(ve -> ve.asType().getKind().isPrimitive()).collect(Collectors.toList());
        if (!primitiveParameters.isEmpty())
            errorMessages.add("Method contains primitive parameters " + primitiveParameters);

        Element parentElement = annotatedMethod.getEnclosingElement();
        if (parentElement.getKind() != ElementKind.CLASS)
            errorMessages.add("Enclosing element is not of Kind Class: " + parentElement.getKind());

        if (!errorMessages.isEmpty())
            throw new ConfigurationException("Can not process annotated method at %s:\n%s",
                    originIdentifier,
                    TextUtils.indentEveryLine(String.join("\n", errorMessages), "    > ")
            );
    }

    private static List<OptionCodeGenerator> gatherOptionCodeGenerators(List<OptionAnnotationDto> options, List<? extends VariableElement> targetTypes, String originIdentifier, ProcessingEnvironment processingEnvironment) throws ConfigurationException {
        List<OptionCodeGenerator> optionCodeGeneratorList = new ArrayList<>(options.size());
        for (int i = 0; i < options.size(); i++) {
            OptionAnnotationDto optionAnnotationDto = options.get(i);
            if (optionAnnotationDto == null)
                optionCodeGeneratorList.add(null);
            else
                optionCodeGeneratorList.add(new OptionCodeGenerator(originIdentifier, optionAnnotationDto, targetTypes.get(i), processingEnvironment));
        }
        return optionCodeGeneratorList;
    }

    /**
     * Code and import for a {@link Command} object.
     */
    public ExtendedSnippetCodeData generateCommandCode() {
        SnippetCodeData instructionSnippetCodeData = generateInstructionCode();

        Set<String> imports = new HashSet<>(instructionSnippetCodeData.getImportPackages());
        imports.addAll(ProcessingUtils.getPackageStrings(Command.class));

        StringBuilder commandCodeSb = new StringBuilder()
                .append("Command.forName(").append(TextUtils.quote(generateActualCommandName())).append(")")
                .append(CommanderProviderCodeGenerator.NEW_LINE_INDENT_DOUBLE).append(".withInstruction(").append(instructionSnippetCodeData.getCodeSnippet()).append(")");
        String actualDescription = generateActualDescription();
        if (!actualDescription.isBlank())
            commandCodeSb
                    .append(CommanderProviderCodeGenerator.NEW_LINE_INDENT_DOUBLE).append(".withDescription(").append(TextUtils.quote(actualDescription)).append(")");
        if (isAnyOptionCodeGeneratorPresent()) {
            commandCodeSb
                    .append(CommanderProviderCodeGenerator.NEW_LINE_INDENT_DOUBLE).append(".withParsers(");
            List<String> parserCodeStrings = new ArrayList<>();
            optionCodeGenerators.stream()
                    .filter(Objects::nonNull)
                    .forEach(codeGenerator -> {
                        SnippetCodeData parserSnippetCodeData = codeGenerator.generateParserCode();
                        imports.addAll(parserSnippetCodeData.getImportPackages());
                        parserCodeStrings.add(TextUtils.indentEveryLine(parserSnippetCodeData.getCodeSnippet(), CommanderProviderCodeGenerator.LINE_INDENT_DOUBLE));
                    });
            commandCodeSb
                    .append(CommanderProviderCodeGenerator.NEW_LINE_INDENT_DOUBLE)
                    .append(String.join("," + CommanderProviderCodeGenerator.NEW_LINE_INDENT_DOUBLE, parserCodeStrings))
                    .append(")");
        }

        return ExtendedSnippetCodeData.from(commandCodeSb.toString(), imports)
                .setBaggage(Set.of(generateSuppressWarningAnnotation()));
    }

    private String generateActualCommandName() {
        if (name.isBlank())
            return TextUtils.toHyphenString(annotatedMethod.getSimpleName().toString());
        else
            return name;
    }

    private String generateActualDescription() {
        String actualDescription = description;
        if (description.isBlank()) {
            String docComment = Objects.requireNonNullElse(processingEnvironment.getElementUtils().getDocComment(annotatedMethod), "");
            actualDescription = JavadocUtils.getText(docComment);
        }
        return TextUtils.normalizeString(actualDescription);
    }

    /**
     * Code and imports for an {@link Instruction} lambda for the command delegating to the user-annotated
     * method that actually performs the command logic.
     * <p>
     *     args -> SomeClass.myMethod1((String) args[0], null, (Path) args[1])
     * </p>
     */
    private SnippetCodeData generateInstructionCode() {
        SnippetCodeData methodCallParametersCode = getMethodCallParameters();
        Set<String> imports = new HashSet<>(methodCallParametersCode.getImportPackages());

        TypeElement enclosingType = (TypeElement) annotatedMethod.getEnclosingElement();
        imports.addAll(ProcessingUtils.getPackageStrings(enclosingType));

        String code = String.format("%s -> %s.%s(%s)",
                INSTRUCTION_PARAMETER_NAME,
                enclosingType.getSimpleName(),
                annotatedMethod.getSimpleName(),
                methodCallParametersCode.getCodeSnippet()
        );

        return SnippetCodeData.from(code, imports);
    }

    /**
     * (String) args[0], (Integer) args[1], args[2]
     */
    private SnippetCodeData getMethodCallParameters() {
        List<SnippetCodeData> parameterCodeSnippets = annotatedMethod.getParameters().stream()
                .map(variableElement -> mapToTypeString(variableElement.asType()))
                .collect(Collectors.toList());

        Set<String> imports = new HashSet<>();
        List<String> parameterStrings = new ArrayList<>();
        String pattern = "(%s) " + INSTRUCTION_PARAMETER_NAME + "[%s]";
        int optionParserIndex = 0;
        for (int i = 0; i < parameterCodeSnippets.size(); i++) {
            if (optionCodeGenerators.get(i) == null) {
                parameterStrings.add("null");
            } else {
                SnippetCodeData codeSnippet = parameterCodeSnippets.get(i);
                parameterStrings.add(String.format(pattern, codeSnippet.getCodeSnippet(), optionParserIndex++));
                imports.addAll(codeSnippet.getImportPackages());
            }
        }
        return SnippetCodeData.from(String.join(", ", parameterStrings), imports);
    }

    /**
     * @return {@code MyCustomClass} or {@code List<Path>} or {@code List<List<Path>} or {@code String[][]} with according imports.
     */
    private SnippetCodeData mapToTypeString(TypeMirror methodParameter) {
        TypeMirror methodParameterType = methodParameter;
        // determine if parameter is of type array and get dimension
        int arrayDepth = 0;
        while (methodParameterType.getKind() == TypeKind.ARRAY) {
            arrayDepth++;
            methodParameterType = ((ArrayType) methodParameterType).getComponentType();
        }

        // get nested generic types
        List<? extends TypeMirror> typeArguments;
        if (methodParameter.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredParameterType = (DeclaredType) methodParameterType;
            typeArguments = declaredParameterType.getTypeArguments();
        } else {
            typeArguments = List.of();
        }

        // map current type
        Element methodParameterAsElement = processingEnvironment.getTypeUtils().asElement(methodParameterType);
        if (methodParameterAsElement == null)
            throw new ConfigurationException("Can not process method parameter type '%s' on %s. The parameter does not seem to correspond to a DeclaredType or VariableElement. This might be an issue when using wildcards.", methodParameter, originIdentifier);
        Set<String> imports = new HashSet<>(ProcessingUtils.getPackageStrings(methodParameterAsElement));
        StringBuilder sb = new StringBuilder(methodParameterAsElement.getSimpleName());

        // map nested types recursively and collect
        if (!typeArguments.isEmpty()) {
            List<String> subtypes = new ArrayList<>(typeArguments.size());
            for (TypeMirror tm : typeArguments) {
               SnippetCodeData snippetCodeData = mapToTypeString(tm);
               subtypes.add(snippetCodeData.getCodeSnippet());
               imports.addAll(snippetCodeData.getImportPackages());
            }
            sb.append("<").append(String.join(", ", subtypes)).append(">");
        }

        sb.append("[]".repeat(arrayDepth));
        return SnippetCodeData.from(sb.toString(), imports);
    }

    private SnippetCodeData generateSuppressWarningAnnotation() {
        if (annotatedMethodContainsAnyGenericTypeParameter())
            return SnippetCodeData.from("@SuppressWarnings(\"unchecked\")");
        else
            return SnippetCodeData.empty();
    }

    private boolean annotatedMethodContainsAnyGenericTypeParameter() {
        return annotatedMethod.getParameters().stream()
                .anyMatch(param -> {
                    TypeMirror paramType = param.asType();
                    if (paramType.getKind() == TypeKind.DECLARED)
                        return !((DeclaredType) paramType).getTypeArguments().isEmpty();
                    else
                        return false;
                });
    }

    private boolean isAnyOptionCodeGeneratorPresent() {
        return optionCodeGenerators.stream().anyMatch(Objects::nonNull);
    }

}
