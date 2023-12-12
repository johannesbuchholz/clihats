package io.github.johannesbuchholz.clihats.processor.generators;

import io.github.johannesbuchholz.clihats.core.execution.Command;
import io.github.johannesbuchholz.clihats.core.execution.Instruction;
import io.github.johannesbuchholz.clihats.processor.CommandLineInterfaceProcessor;
import io.github.johannesbuchholz.clihats.processor.annotations.Argument;
import io.github.johannesbuchholz.clihats.processor.exceptions.ArgumentConfigurationException;
import io.github.johannesbuchholz.clihats.processor.exceptions.ConfigurationException;
import io.github.johannesbuchholz.clihats.processor.model.*;
import io.github.johannesbuchholz.clihats.processor.util.JavadocUtils;
import io.github.johannesbuchholz.clihats.processor.util.ProcessingUtils;
import io.github.johannesbuchholz.clihats.processor.util.TextUtils;
import io.github.johannesbuchholz.clihats.processor.util.visitors.ArrayOfSimpleAnnotationValueVisitor;
import io.github.johannesbuchholz.clihats.processor.util.visitors.EnumAnnotationValueVisitor;
import io.github.johannesbuchholz.clihats.processor.util.visitors.SimpleValueAnnotationValueVisitor;
import io.github.johannesbuchholz.clihats.processor.util.visitors.TypeAnnotationValueVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.*;
import java.util.stream.Collectors;

public class CommandCodeGenerator {

    private static final Logger log = LoggerFactory.getLogger(CommandCodeGenerator.class);
    private static final String INSTRUCTION_PARAMETER_NAME = "args";

    private final ProcessingEnvironment processingEnvironment;
    private final String name;
    private final String description;
    private final ExecutableElement annotatedMethod;

    private final String originIdentifier;

    private final List<ParameterCodeGeneratorPair> parameterCodeGeneratorPairs;

    public CommandCodeGenerator(ProcessingEnvironment processingEnvironment, CommandDto commandDto) throws ConfigurationException {
        this.processingEnvironment = processingEnvironment;

        annotatedMethod = commandDto.getAnnotatedMethod();
        name = commandDto.getName();
        description = commandDto.getDescription();

        List<ParameterArgumentPair> parameterArgumentPairs = extractArgumentDtos(commandDto.getAnnotatedMethod(), processingEnvironment);
        originIdentifier = ProcessingUtils.generateOriginIdentifier(annotatedMethod);
        validateMethod(originIdentifier, annotatedMethod);

        parameterCodeGeneratorPairs = gatherArgumentParserCodeGenerators(
                parameterArgumentPairs,
                originIdentifier,
                processingEnvironment
        );

    }

    private static List<ParameterArgumentPair> extractArgumentDtos(ExecutableElement annotatedMethod, ProcessingEnvironment processingEnvironment) {
        String docComment = processingEnvironment.getElementUtils().getDocComment(annotatedMethod);
        final Map<String, String> paramDescriptionByName = new HashMap<>();
        if (docComment != null)
            paramDescriptionByName.putAll(JavadocUtils.extractParamDoc(docComment));

        return annotatedMethod.getParameters().stream()
                .map(ve -> extractArgumentDto(ve, paramDescriptionByName.getOrDefault(ve.getSimpleName().toString(), ""), processingEnvironment)
                        .map(dto -> ParameterArgumentPair.pair(ve, dto))
                        .orElse(ParameterArgumentPair.unmanagedParameter(ve)))
                .collect(Collectors.toList());
    }

    /**
     * Returns {@code null} if the given method parameter does not possess an annotation of type {@link Argument}.
     */
    private static Optional<ArgumentDto> extractArgumentDto(VariableElement methodParameter, String paramJavaDoc, ProcessingEnvironment processingEnvironment) {
        Optional<ArgumentDto> argumentDto = methodParameter.getAnnotationMirrors().stream()
                .filter(am -> am.getAnnotationType().equals(CommandLineInterfaceProcessor.optionAnnotationType.asType()))
                .findFirst()
                .map(annotationMirror -> extractArgumentDto(annotationMirror, paramJavaDoc, processingEnvironment));
        argumentDto
                .map(CommandCodeGenerator::determineDubiousConfiguration)
                .ifPresent(dubiousMessages -> dubiousMessages.forEach(msg -> log.warn("Dubious argument configuration at {}: {}", methodParameter, msg)));
        return argumentDto;
    }

    private static List<String> determineDubiousConfiguration(ArgumentDto argumentDto) {
        List<String> dubiousConfigurations = new ArrayList<>();
        Argument.Type type = argumentDto.getType();
        if (type != Argument.Type.OPTION && !argumentDto.getFlagValue().isEmpty())
            dubiousConfigurations.add("Encountered flag value on an argument that is not an option");

        Argument.Necessity necessity = argumentDto.getNecessity();
        if (!argumentDto.getDefaultValue().isEmpty() && necessity != Argument.Necessity.OPTIONAL)
            dubiousConfigurations.add("Encountered non-empty default value while necessity is not optional");
        if (!argumentDto.getFlagValue().isEmpty()) {
            if (necessity != Argument.Necessity.OPTIONAL)
                dubiousConfigurations.add("Encountered optional necessity for flag option");
        }
        return dubiousConfigurations;
    }

    private static ArgumentDto extractArgumentDto(AnnotationMirror optionMirror, String javadocParamDescription, ProcessingEnvironment processingEnvironment) {
        Map<String, ? extends AnnotationValue> valuesByFieldName = processingEnvironment.getElementUtils().getElementValuesWithDefaults(optionMirror)
                .entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().getSimpleName().toString(), Map.Entry::getValue));
        return extractArgumentDto(valuesByFieldName, javadocParamDescription, processingEnvironment);
    }

    private static ArgumentDto extractArgumentDto(Map<String, ? extends AnnotationValue> valuesByFieldName, String javadocParamDescription, ProcessingEnvironment processingEnvironment) {
        VariableElement typeVariableElement = valuesByFieldName.get(ArgumentDto.TYPE_FIELD_NAME).accept(new EnumAnnotationValueVisitor(), null);
        List<String> name = valuesByFieldName.get(ArgumentDto.NAME_FIELD_NAME).accept(new ArrayOfSimpleAnnotationValueVisitor<>(String.class), null);
        String flagValue = valuesByFieldName.get(ArgumentDto.FLAG_FIELD_NAME).accept(new SimpleValueAnnotationValueVisitor<>(String.class), null);
        String defaultValue = valuesByFieldName.get(ArgumentDto.DEFAULT_FIELD_NAME).accept(new SimpleValueAnnotationValueVisitor<>(String.class), null);
        TypeElement mapper = valuesByFieldName.get(ArgumentDto.MAPPER_FIELD_NAME).accept(new TypeAnnotationValueVisitor(), processingEnvironment.getTypeUtils());
        VariableElement necessityVariableElement = valuesByFieldName.get(ArgumentDto.NECESSITY_FIELD_NAME).accept(new EnumAnnotationValueVisitor(), null);
        String descriptionFromAnnotation = valuesByFieldName.get(ArgumentDto.DESCRIPTION_FIELD_NAME).accept(new SimpleValueAnnotationValueVisitor<>(String.class), null);
        return new ArgumentDto(
                ProcessingUtils.getEnumFromTypeElement(Argument.Type.class, typeVariableElement, processingEnvironment),
                name,
                flagValue,
                defaultValue,
                mapper,
                ProcessingUtils.getEnumFromTypeElement(Argument.Necessity.class, necessityVariableElement, processingEnvironment),
                descriptionFromAnnotation.isEmpty() ? javadocParamDescription : descriptionFromAnnotation
        );
    }

    private static void validateMethod(String originIdentifier, ExecutableElement annotatedMethod) throws ConfigurationException {
        List<String> errorMessages = new ArrayList<>();
        if (annotatedMethod.getReturnType().getKind() != TypeKind.VOID)
            errorMessages.add("Method must have return type kind void but has return type kind " + annotatedMethod.getReturnType().getKind());
        if (!annotatedMethod.getModifiers().containsAll(List.of(Modifier.PUBLIC, Modifier.STATIC)))
            errorMessages.add("Annotated method is not public or not static: " + annotatedMethod.getModifiers());

        List<VariableElement> primitiveParameters = annotatedMethod.getParameters().stream().filter(ve -> ve.asType().getKind().isPrimitive()).collect(Collectors.toList());
        if (!primitiveParameters.isEmpty())
            errorMessages.add("Method contains primitive parameters " + primitiveParameters);

        Element parentElement = annotatedMethod.getEnclosingElement();
        if (parentElement.getKind() != ElementKind.CLASS)
            errorMessages.add("Enclosing element is not of Kind Class: " + parentElement.getKind());

        if (!errorMessages.isEmpty())
            throw new ConfigurationException("Can not process method %s at %s:\n%s",
                    annotatedMethod,
                    originIdentifier,
                    TextUtils.indentEveryLine(String.join("\n", errorMessages), "    > ")
            );
    }

    private static List<ParameterCodeGeneratorPair> gatherArgumentParserCodeGenerators(
            List<ParameterArgumentPair> parameterArgumentPairs,
            String originIdentifier,
            ProcessingEnvironment processingEnvironment
    ) throws ConfigurationException {
        ArgumentParserCodeGeneratorFactory generatorFactory = ArgumentParserCodeGeneratorFactory.getNew(processingEnvironment);
        try {
            return generatorFactory.createParserCodeGenerators(parameterArgumentPairs);
        } catch (ArgumentConfigurationException e) {
            throw new ConfigurationException(String.format("Can not process argument at %s: %s", originIdentifier, e.getMessage()), e);
        }
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
        if (isAnyArgumentParserCodeGeneratorPresent()) {
            commandCodeSb
                    .append(CommanderProviderCodeGenerator.NEW_LINE_INDENT_DOUBLE).append(".withParsers(")
                    .append(CommanderProviderCodeGenerator.NEW_LINE_INDENT_DOUBLE);
            List<String> parserCodeStrings = new ArrayList<>();
            parameterCodeGeneratorPairs.stream()
                    .filter(ParameterCodeGeneratorPair::isHasCodeGenerator)
                    .map(pair -> pair.getArgumentParserCodeGenerator().generateParserCode())
                    .forEach(snippet -> {
                        imports.addAll(snippet.getImportPackages());
                        parserCodeStrings.add(TextUtils.indentEveryLine(snippet.getCodeSnippet(), CommanderProviderCodeGenerator.LINE_INDENT_DOUBLE));
                    });
            commandCodeSb
                    .append(String.join("," + CommanderProviderCodeGenerator.NEW_LINE_INDENT_DOUBLE, parserCodeStrings))
                    .append(")");
        }

        return ExtendedSnippetCodeData.from(commandCodeSb.toString(), imports)
                .setBaggage(Set.of(generateSuppressWarningAnnotation()));
    }

    private String generateActualCommandName() {
        if (name.isBlank()){
            return TextUtils.toHyphenString(annotatedMethod.getSimpleName().toString());
        }
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
     * (String) args[0], (Integer) args[1], null
     */
    private SnippetCodeData getMethodCallParameters() {
        Set<String> imports = new HashSet<>();
        List<String> parameterStrings = new ArrayList<>();
        String pattern = "(%s) " + INSTRUCTION_PARAMETER_NAME + "[%s]";
        int argIndex = 0;
        for (ParameterCodeGeneratorPair pair : parameterCodeGeneratorPairs) {
            if (pair.isHasCodeGenerator()) {
                // here if method parameter has been annotated
                SnippetCodeData codeSnippet = mapToTypeString(pair.getTargetParameter().asType());
                parameterStrings.add(String.format(pattern, codeSnippet.getCodeSnippet(), argIndex++));
                imports.addAll(codeSnippet.getImportPackages());
            } else {
                parameterStrings.add("null");
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
        TypeElement methodParameterAsElement = (TypeElement) processingEnvironment.getTypeUtils().asElement(methodParameterType);
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
                .map(Element::asType)
                .anyMatch(ProcessingUtils::hasGenericTypeParameter);
    }

    private boolean isAnyArgumentParserCodeGeneratorPresent() {
        return !parameterCodeGeneratorPairs.isEmpty();
    }

}
