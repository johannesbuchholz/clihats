package io.github.johannesbuchholz.clihats.processor.generators;

import io.github.johannesbuchholz.clihats.core.execution.parser.ValueMapper;
import io.github.johannesbuchholz.clihats.processor.CommandLineInterfaceProcessor;
import io.github.johannesbuchholz.clihats.processor.annotations.Argument;
import io.github.johannesbuchholz.clihats.processor.exceptions.ArgumentConfigurationException;
import io.github.johannesbuchholz.clihats.processor.mapper.DefaultMapperRegistry;
import io.github.johannesbuchholz.clihats.processor.model.ArgumentDto;
import io.github.johannesbuchholz.clihats.processor.model.SnippetCodeData;
import io.github.johannesbuchholz.clihats.processor.util.ProcessingUtils;
import io.github.johannesbuchholz.clihats.processor.util.TextUtils;
import io.github.johannesbuchholz.clihats.processor.util.UserInputPrompter;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractArgumentParserCodeGenerator implements ArgumentParserCodeGenerator {

    final ArgumentDto argumentInputs;
    final VariableElement targetVariableElement;
    final TypeElement mapperType;
    final ProcessingEnvironment processingEnvironment;

    AbstractArgumentParserCodeGenerator(ArgumentDto argumentInputs, VariableElement targetVariableElement, TypeElement mapperType, ProcessingEnvironment processingEnvironment) {
        this.argumentInputs = argumentInputs;
        this.targetVariableElement = targetVariableElement;
        this.mapperType = mapperType;
        this.processingEnvironment = processingEnvironment;
    }

    public abstract SnippetCodeData generateParserCode();

    /**
     * Assumes mapperType actually represents {@link ValueMapper}.
     * Returns {@code null} if targetVariableElement represents a String.
     */
    static TypeElement deduceMapperTypeAndVerify(TypeElement mapperTypeInput, TypeMirror targetVariableType, ProcessingEnvironment processingEnvironment) throws ArgumentConfigurationException{
        TypeElement mapperType = getMapperType(mapperTypeInput, targetVariableType, processingEnvironment);
        verifyMapperType(mapperType, targetVariableType, processingEnvironment);
        if (CommandLineInterfaceProcessor.identityMapperType.equals(mapperType)) {
            return null;
        }
        return mapperType;
    }

    static TypeElement getMapperType(TypeElement mapperTypeInput, TypeMirror targetVariableType, ProcessingEnvironment processingEnvironment) throws ArgumentConfigurationException {
        TypeElement targetTypeElement = (TypeElement) processingEnvironment.getTypeUtils().asElement(targetVariableType);
        TypeElement mapperType;
        if (processingEnvironment.getTypeUtils().isSameType(mapperTypeInput.asType(), CommandLineInterfaceProcessor.identityMapperType.asType())) {
            if (targetTypeElement == null)
                throw new ArgumentConfigurationException("Target variable type has no corresponding element and no custom mapper has been defined: " + targetVariableType);
            mapperType = DefaultMapperRegistry.getForType(targetTypeElement, processingEnvironment.getElementUtils());
        } else {
            mapperType = mapperTypeInput;
        }
        return mapperType;
    }

    static void verifyMapperType(TypeElement mapperType, TypeMirror targetVariableType, ProcessingEnvironment processingEnvironment) throws ArgumentConfigurationException{
        // types match
        DeclaredType declaredMapperType = ProcessingUtils.getMatchingSuperClass(mapperType, CommandLineInterfaceProcessor.abstractValueMapperType, processingEnvironment)
                .orElseThrow(() -> new IllegalStateException("Programming error: Mapper type should implement ValueMapper"));
        TypeMirror mapperTargetType = declaredMapperType.getTypeArguments().get(0);
        if (!processingEnvironment.getTypeUtils().isSameType(mapperTargetType, targetVariableType))
            throw new ArgumentConfigurationException(String.format("Mapper type %s does not match target parameter type %s", mapperTargetType, targetVariableType));
        // ----- verify
        // mapper type is public
        if (!mapperType.getModifiers().contains(Modifier.PUBLIC)) {
            throw new ArgumentConfigurationException(String.format("Mapper %s is not public", mapperType.getQualifiedName()));
        }
        // no-args constructor exists
        if (ProcessingUtils.getPublicNoArgsConstructor(mapperType).isEmpty()) {
            throw new ArgumentConfigurationException(String.format("Mapper %s does not possess a public no-args constructor", mapperType.getQualifiedName()));
        }
    }

    SnippetCodeData generatePromptCode() {
        String promptText = TextUtils.quote(TextUtils.uppercaseFirst(targetVariableElement.getSimpleName().toString()) + ":\\n");
        String userInputSupplierCode;
        Argument.Necessity necessity = ProcessingUtils.getEnumFromTypeElement(EnumSet.allOf(Argument.Necessity.class), argumentInputs.getNecessity(), processingEnvironment);
        switch (necessity) {
            case PROMPT:
                userInputSupplierCode = String.format("() -> %s.getNew().prompt(%s)", UserInputPrompter.class.getSimpleName(), promptText);
                break;
            case MASKED_PROMPT:
                userInputSupplierCode = String.format("() -> %s.getNew().promptMasked(%s)", UserInputPrompter.class.getSimpleName(), promptText);
                break;
            default:
                return SnippetCodeData.empty();
        }
        return SnippetCodeData.from(
                String.format(".withDefault(%s)", userInputSupplierCode),
                ProcessingUtils.getPackageStrings(UserInputPrompter.class)
        );
    }

    SnippetCodeData generateMapperCode() {
        if (mapperType == null)
            return SnippetCodeData.empty();
        Set<String> imports = new HashSet<>(ProcessingUtils.getPackageStrings(mapperType));
        return SnippetCodeData.from(String.format(".withMapper(new %s())", mapperType.getSimpleName()), imports);
    }

    String generateRequiredCode() {
        Argument.Necessity necessity = ProcessingUtils.getEnumFromTypeElement(EnumSet.allOf(Argument.Necessity.class), argumentInputs.getNecessity(), processingEnvironment);
        if (necessity == Argument.Necessity.REQUIRED)
            return ".withRequired(true)";
        return "";
    }

    String generateDescriptionCode() {
        String descriptionInput = argumentInputs.getDescription();
        if (descriptionInput.isEmpty())
            return "";
        return ".withDescription(" + TextUtils.quote(descriptionInput) + ")";
    }

    String generateDefaultValueCode() {
        String defaultValueInput = argumentInputs.getDefaultValue();
        if (isNotUsingCustomDefaultValue())
            return "";
        return ".withDefault(" + TextUtils.quote(defaultValueInput) + ")";
    }

    boolean isNotUsingCustomDefaultValue() {
        Argument.Necessity necessity = ProcessingUtils.getEnumFromTypeElement(EnumSet.allOf(Argument.Necessity.class), argumentInputs.getNecessity(), processingEnvironment);
        return necessity != Argument.Necessity.OPTIONAL || argumentInputs.getDefaultValue().isEmpty();
    }

}
