package io.github.johannesbuchholz.clihats.processor.generators;

import io.github.johannesbuchholz.clihats.core.execution.parser.ArgumentParsers;
import io.github.johannesbuchholz.clihats.processor.annotations.Argument;
import io.github.johannesbuchholz.clihats.processor.exceptions.ArgumentConfigurationException;
import io.github.johannesbuchholz.clihats.processor.model.ArgumentDto;
import io.github.johannesbuchholz.clihats.processor.model.SnippetCodeData;
import io.github.johannesbuchholz.clihats.processor.util.ProcessingUtils;
import io.github.johannesbuchholz.clihats.processor.util.TextUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.HashSet;
import java.util.Set;

public class ArrayOperandParserCodeGenerator extends AbstractArgumentParserCodeGenerator {

    private final int operandIndex;
    private final TypeMirror arrayComponentType;

    public ArrayOperandParserCodeGenerator(ArgumentDto argumentInputs, VariableElement targetVariableElement, int operandIndex, ProcessingEnvironment processingEnvironment) throws ArgumentConfigurationException {
        super(
                argumentInputs,
                targetVariableElement,
                deduceMapperTypeAndVerify(argumentInputs.getMapper(), targetVariableElement.asType(), processingEnvironment),
                processingEnvironment);
        this.operandIndex = operandIndex;
        arrayComponentType = deduceArrayComponentTypeAndVerify(targetVariableElement);
    }

    /**
     * The provided mapper must not map to an array but to the type the array consists of. Hence, we call the super
     * implementation but with the array content type.
     * This is OK:
     * Mapper: String -> Path
     * Method parameter: Path[]
     */
    static TypeElement deduceMapperTypeAndVerify(TypeElement mapperTypeInput, TypeMirror targetVariableType, ProcessingEnvironment processingEnvironment) throws ArgumentConfigurationException {
        return AbstractArgumentParserCodeGenerator.deduceMapperTypeAndVerify(mapperTypeInput, verifyAndGetArrayComponentType(targetVariableType), processingEnvironment);
    }

    private static TypeMirror deduceArrayComponentTypeAndVerify(VariableElement targetVariableElement) throws ArgumentConfigurationException {
        TypeMirror targetType = targetVariableElement.asType();
        if (ProcessingUtils.hasGenericTypeParameter(targetType))
            throw new ArgumentConfigurationException(String.format("Configuration %s can not be used on parameters of array type containing generic types", Argument.Type.ARRAY_OPERAND));
        if (targetType.getKind() != TypeKind.ARRAY)
            throw new ArgumentConfigurationException(String.format("Target type %s is not of type array", targetType));

        TypeMirror componentType = ((ArrayType) targetType).getComponentType();
        if (componentType.getKind() == TypeKind.ARRAY)
            throw new ArgumentConfigurationException(String.format("Target type %s is an array of depth greater than one", targetType));
        return componentType;
    }

    private static TypeMirror verifyAndGetArrayComponentType(TypeMirror arrayType) throws ArgumentConfigurationException {
        if (arrayType.getKind() != TypeKind.ARRAY)
            throw new ArgumentConfigurationException(String.format("Found configuration %s but target type is not of type array: %s", Argument.Type.ARRAY_OPERAND, arrayType));
        TypeMirror componentType = ((ArrayType) arrayType).getComponentType();
        if (componentType.getKind() == TypeKind.ARRAY)
            throw new ArgumentConfigurationException(String.format("Target type %s is an array of depth greater than one", arrayType));
        return componentType;
    }

    @Override
    public SnippetCodeData generateParserCode() {
        SnippetCodeData mapperSnippetCodeData = generateMapperCode();
        SnippetCodeData promptSnippetCodeData = generatePromptCode();
        Set<String> imports = new HashSet<>(ProcessingUtils.getPackageStrings(ArgumentParsers.class));
        imports.addAll(promptSnippetCodeData.getImportPackages());
        imports.addAll(mapperSnippetCodeData.getImportPackages());
        return SnippetCodeData.from(
                String.format(
                        "%s.arrayOperand(%s)%s%s%s%s%s%s",
                        ArgumentParsers.class.getSimpleName(),
                        operandIndex,
                        generateName(),
                        generateRequiredCode(),
                        generateDefaultValueCode(),
                        mapperSnippetCodeData.getCodeSnippet(),
                        promptSnippetCodeData.getCodeSnippet(),
                        generateDescriptionCode()
                ),
                imports
        );
    }

    String generateName() {
        String name;
        if (!argumentInputs.getName().isEmpty()) {
            name = argumentInputs.getName().get(0);
        } else {
            name = targetVariableElement.getSimpleName().toString();
        }
        return ".withDisplayName(" + TextUtils.quote(TextUtils.toUpperCaseString(name)) + ")";
    }

    @Override
    SnippetCodeData generateMapperCode() {
        if (mapperType == null)
            return SnippetCodeData.empty();
        Element arrayComponentTypeElement = processingEnvironment.getTypeUtils().asElement(arrayComponentType);
        Set<String> imports = new HashSet<>(ProcessingUtils.getPackageStrings(mapperType));
        imports.addAll(ProcessingUtils.getPackageStrings(arrayComponentTypeElement));
        return SnippetCodeData.from(String.format(".withMapper(new %s(), %s.class)", mapperType.getSimpleName(), arrayComponentTypeElement.getSimpleName()), imports);
    }

    @Override
    String generateDefaultValueCode() {
        String defaultValueInput = argumentInputs.getDefaultValue();
        if (isNotUsingCustomDefaultValue())
            return "";
        return ".withDefault(new String[]{" + TextUtils.quote(defaultValueInput) + "})";
    }

}
