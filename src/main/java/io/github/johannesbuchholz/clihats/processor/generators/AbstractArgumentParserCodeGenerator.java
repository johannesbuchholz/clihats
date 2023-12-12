package io.github.johannesbuchholz.clihats.processor.generators;

import io.github.johannesbuchholz.clihats.processor.CommandLineInterfaceProcessor;
import io.github.johannesbuchholz.clihats.processor.annotations.Argument;
import io.github.johannesbuchholz.clihats.processor.exceptions.ConfigurationException;
import io.github.johannesbuchholz.clihats.processor.mapper.DefaultMapperRegistry;
import io.github.johannesbuchholz.clihats.processor.model.SnippetCodeData;
import io.github.johannesbuchholz.clihats.processor.model.TargetParameter;
import io.github.johannesbuchholz.clihats.processor.util.ProcessingUtils;
import io.github.johannesbuchholz.clihats.processor.util.TextUtils;
import io.github.johannesbuchholz.clihats.processor.util.UserInputPrompter;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

public abstract class AbstractArgumentParserCodeGenerator implements ArgumentParserCodeGenerator {

    final Argument.Necessity necessity;
    final String description;
    final String defaultValue;
    final TypeElement mapperElement;
    final TargetParameter targetParameter;

    AbstractArgumentParserCodeGenerator(Argument.Necessity necessity, String description, String defaultValue, TypeElement mapperElement, TargetParameter targetParameter) {
        this.necessity = necessity;
        this.description = description;
        this.defaultValue = defaultValue;
        this.mapperElement = mapperElement;
        this.targetParameter = targetParameter;
    }

    public abstract SnippetCodeData generateParserCode();

    SnippetCodeData generatePromptCode() {
        String promptText = TextUtils.quote(TextUtils.uppercaseFirst(targetParameter.getName()) + ":\\n");
        String userInputSupplierCode;
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

    /**
     * @return The code defining a {@link io.github.johannesbuchholz.clihats.core.execution.parser.ValueMapper}
     * returning the specified target type.
     */
    SnippetCodeData generateValueMapperCode(TypeElement targetTypeElement) {
        SnippetCodeData codeData;
        if (mapperElement.asType().equals(CommandLineInterfaceProcessor.identityMapperType.asType())) {
            // here if automatic mapper type deduction applies
            if (targetTypeElement.asType().equals(CommandLineInterfaceProcessor.stringType.asType())) {
                // here if target type is java.lang.String: nothing to do
                return SnippetCodeData.empty();
            } else if (targetTypeElement.getKind() == ElementKind.ENUM) {
                codeData = DefaultMapperRegistry.getEnumMapperCodeFor(targetTypeElement);
            } else {
                // here if a target type could match an available default mapper
                codeData = DefaultMapperRegistry.getMapperCodeFor(targetTypeElement)
                        .orElseThrow(() -> new ConfigurationException(String.format("Unable to deduce mapper type for non-default target type: %s is not among %s", targetTypeElement, DefaultMapperRegistry.SUPPORTED_TARGET_TYPES)));
            }
        } else {
            // here if using mapper from configuration input
            codeData = SnippetCodeData.from(String.format("new %s()", mapperElement.getSimpleName()), ProcessingUtils.getPackageStrings(mapperElement));
        }
        return codeData;
    }

    String generateRequiredCode() {
        if (necessity == Argument.Necessity.REQUIRED)
            return ".withRequired(true)";
        return "";
    }

    String generateDescriptionCode() {
        if (description.isEmpty())
            return "";
        return ".withDescription(" + TextUtils.quote(description) + ")";
    }

    String generateDefaultValueCode() {
        if (isNotUsingCustomDefaultValue())
            return "";
        return ".withDefault(" + TextUtils.quote(defaultValue) + ")";
    }

    boolean isNotUsingCustomDefaultValue() {
        return necessity != Argument.Necessity.OPTIONAL || defaultValue.isEmpty();
    }

}
