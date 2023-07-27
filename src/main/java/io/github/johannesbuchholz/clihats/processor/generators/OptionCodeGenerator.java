package io.github.johannesbuchholz.clihats.processor.generators;

import io.github.johannesbuchholz.clihats.core.execution.ValueMapper;
import io.github.johannesbuchholz.clihats.core.execution.parser.Parsers;
import io.github.johannesbuchholz.clihats.processor.CommandLineInterfaceProcessor;
import io.github.johannesbuchholz.clihats.processor.annotations.OptionNecessity;
import io.github.johannesbuchholz.clihats.processor.exceptions.ConfigurationException;
import io.github.johannesbuchholz.clihats.processor.logging.Logging;
import io.github.johannesbuchholz.clihats.processor.mapper.DefaultMapperRegistry;
import io.github.johannesbuchholz.clihats.processor.model.OptionAnnotationDto;
import io.github.johannesbuchholz.clihats.processor.model.SnippetCodeData;
import io.github.johannesbuchholz.clihats.processor.util.ProcessingUtils;
import io.github.johannesbuchholz.clihats.processor.util.TextUtils;
import io.github.johannesbuchholz.clihats.processor.util.UserInputPrompter;
import org.slf4j.Logger;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.*;

public class OptionCodeGenerator {

    private static final Logger log = Logging.getCliHatsLogger();

    protected enum ParserType {POSITIONAL, VALUED, FLAG}

    private final VariableElement targetVariableElement;
    
    private final OptionAnnotationDto optionInputs;
    
    private final TypeElement mapperType;
    private final ParserType parserType;

    private final ProcessingEnvironment processingEnvironment;

    public OptionCodeGenerator(String originIdentifier, OptionAnnotationDto optionInputs, VariableElement targetVariableElement, ProcessingEnvironment processingEnvironment) throws ConfigurationException {
        this.processingEnvironment = processingEnvironment;
        this.targetVariableElement = targetVariableElement;
        this.optionInputs = optionInputs;

        String parameterOrigin = String.format("Parameter %s in %s", targetVariableElement.getSimpleName(), originIdentifier);
        mapperType = getMapperTypeAndVerify(
                optionInputs.getMapper(),
                targetVariableElement,
                processingEnvironment,
                parameterOrigin
        );
        parserType = verifyAndDetermineType(
                optionInputs.getPosition(),
                optionInputs.getName(),
                optionInputs.getFlagValue(),
                optionInputs.getDefaultValue(),
                ProcessingUtils.getEnumFromTypeElement(EnumSet.allOf(OptionNecessity.class), optionInputs.getNecessity(), processingEnvironment),
                parameterOrigin
        );
    }

    /**
     * Assumes mapperType actually represents {@link ValueMapper}.
     * Returns {@code null} if targetVariableElement represents a String.
     */
    private static TypeElement getMapperTypeAndVerify(TypeElement mapperTypeInput, VariableElement targetVariableElement, ProcessingEnvironment processingEnvironment, String originIdentifier) {
        if (ProcessingUtils.isSameType(targetVariableElement.asType(), String.class, processingEnvironment))
            return null;
        // ----- get
        TypeElement targetTypeElement = (TypeElement) processingEnvironment.getTypeUtils().asElement(targetVariableElement.asType());
        TypeElement mapperType;
        if (processingEnvironment.getTypeUtils().isSameType(mapperTypeInput.asType(), CommandLineInterfaceProcessor.noMapperType.asType()))
            mapperType = DefaultMapperRegistry.getForType(targetTypeElement, processingEnvironment.getElementUtils());
        else
            mapperType = mapperTypeInput;
        // ----- verify
        // mapper type is public
        if (!mapperType.getModifiers().contains(Modifier.PUBLIC)) {
            throw new ConfigurationException("Can not process option: %s: Mapper %s is not public", originIdentifier, mapperType.getQualifiedName());
        }
        // no-args constructor exists
        if (ProcessingUtils.getPublicNoArgsConstructor(mapperType).isEmpty())
            throw new ConfigurationException("Can not process option: %s: Mapper %s does not possess a public no-args constructor", originIdentifier, mapperType.getQualifiedName());
        // types match
        DeclaredType declaredMapperType = ProcessingUtils.getMatchingSuperClass(mapperType, CommandLineInterfaceProcessor.abstractValueMapperType, processingEnvironment)
                .orElseThrow(() -> new IllegalStateException("Programming error: Mapper type should implement ValueMapper..."));
        TypeMirror mapperTargetType = declaredMapperType.getTypeArguments().get(0);
        if (!processingEnvironment.getTypeUtils().isSameType(mapperTargetType, targetVariableElement.asType()))
            throw new ConfigurationException("Can not process option: %s: Mapper type %s does not match target parameter type %s", originIdentifier, mapperTargetType, targetVariableElement.asType());
        return mapperType;
    }

    private static ParserType verifyAndDetermineType(Integer positionInput, List<String> nameInput, String flagInput, String defaultValueInput, OptionNecessity necessity, String originIdentifier) throws ConfigurationException {
        if (positionInput > -1 && !flagInput.isEmpty())
            throw new ConfigurationException("Can not process option: %s: position and flag can not be set simultaneously", originIdentifier);
        if (positionInput > -1 && nameInput.size() > 0)
            throw new ConfigurationException("Can not process option: %s: position and name can not be set simultaneously", originIdentifier);
        if (!defaultValueInput.isEmpty() && necessity != OptionNecessity.OPTIONAL)
            throw new ConfigurationException("Can not process option: %s: Non-empty default value while necessity is not OPTIONAL", originIdentifier);
        if (positionInput > -1) {
            return ParserType.POSITIONAL;
        } else if (!flagInput.isEmpty()) {
            if (necessity != OptionNecessity.OPTIONAL)
                log.warn("Ignoring necessity {} for flag option: {}", necessity, originIdentifier);
            return ParserType.FLAG;
        }
        return ParserType.VALUED;
    }

    public SnippetCodeData generateParserCode() {
        switch (parserType) {
            case POSITIONAL:
                return generatePositionalParserCode();
            case FLAG:
                return generateFlagParserCode();
            case VALUED:
                return generateValuedParserCode();
            default:
                throw new IllegalStateException(String.format("Unknown option parser type: %s is not in %s", parserType, Arrays.toString(ParserType.values())));
        }
    }

    /**
     * OptionParsers.positional(1)
     *     .withMapper(X::new, X.class)
     */
    private SnippetCodeData generatePositionalParserCode() {
        SnippetCodeData mapperSnippetCodeData = generateMapperCode();
        SnippetCodeData promptSnippetCodeData = generatePromptCode();
        Set<String> imports = new HashSet<>(ProcessingUtils.getPackageStrings(Parsers.class));
        imports.addAll(promptSnippetCodeData.getImportPackages());
        imports.addAll(mapperSnippetCodeData.getImportPackages());
        return SnippetCodeData.from(
                String.format(
                        "%s.positional(%s)%s%s%s%s%s",
                        Parsers.class.getSimpleName(),
                        optionInputs.getPosition(),
                        generateRequiredCode(),
                        generateDefaultValueCode(),
                        mapperSnippetCodeData.getCodeSnippet(),
                        promptSnippetCodeData.getCodeSnippet(),
                        generateDescriptionCode()
                        ),
                imports
        );
    }

    /**
     * OptionParsers.flag("--opt")
     *     .withFlagValue("my/option/path")
     *     .withDefaultValue("my/default")
     *     .withAliases("-bli", "-bla", "--blubber")
     *     .withMapper(Path::of, Path.class)
     */
    private SnippetCodeData generateFlagParserCode() {
        SnippetCodeData mapperSnippetCodeData = generateMapperCode();
        Set<String> imports = new HashSet<>(ProcessingUtils.getPackageStrings(Parsers.class));
        imports.addAll(mapperSnippetCodeData.getImportPackages());
        return SnippetCodeData.from(
                String.format(
                        "%s.flag(%s)%s%s%s%s%s",
                        Parsers.class.getSimpleName(),
                        TextUtils.quote(generateNames()),
                        generateFlagValueCode(),
                        generateDefaultValueCode(),
                        mapperSnippetCodeData.getCodeSnippet(),
                        generateDescriptionCode()
                        ),
                imports
        );
    }

    /**
     * OptionParsers.valued("-d")
     *     .withAliases("-a", "--blubb")
     *     .withDelimiter("=")
     *     .withDefault("-1")
     *     .withMapper(Integer::parseInt, Integer.class)
     */
    private SnippetCodeData generateValuedParserCode() {
        SnippetCodeData mapperSnippetCodeData = generateMapperCode();
        SnippetCodeData promptSnippetCodeData = generatePromptCode();
        Set<String> imports = new HashSet<>(ProcessingUtils.getPackageStrings(Parsers.class));
        imports.addAll(mapperSnippetCodeData.getImportPackages());
        imports.addAll(promptSnippetCodeData.getImportPackages());
        return SnippetCodeData.from(
                String.format(
                        "%s.valued(%s)%s%s%s%s%s%s",
                        Parsers.class.getSimpleName(),
                        TextUtils.quote(generateNames()),
                        generateRequiredCode(),
                        generateDefaultValueCode(),
                        generateDescriptionCode(),
                        mapperSnippetCodeData.getCodeSnippet(),
                        promptSnippetCodeData.getCodeSnippet()
                        ),
                imports
        );
    }

    private String generateNames() {
        if (!optionInputs.getName().isEmpty())
            return String.join(", ", optionInputs.getName());
        return "-" + targetVariableElement.getSimpleName().toString().charAt(0);
    }

    private SnippetCodeData generatePromptCode() {
        String promptText = TextUtils.quote(TextUtils.uppercaseFirst(targetVariableElement.getSimpleName().toString()) + ":\\n");
        String userInputSupplierCode;
        OptionNecessity optionNecessity = ProcessingUtils.getEnumFromTypeElement(EnumSet.allOf(OptionNecessity.class), optionInputs.getNecessity(), processingEnvironment);
        switch (optionNecessity) {
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
     * May be empty if no mapper is given.
     */
    private SnippetCodeData generateMapperCode() {
        if (mapperType == null)
            return SnippetCodeData.empty();
        Set<String> imports = new HashSet<>(ProcessingUtils.getPackageStrings(mapperType));
        return SnippetCodeData.from(String.format(".withMapper(new %s())", mapperType.getSimpleName()), imports);
    }

    private String generateRequiredCode() {
        OptionNecessity optionNecessity = ProcessingUtils.getEnumFromTypeElement(EnumSet.allOf(OptionNecessity.class), optionInputs.getNecessity(), processingEnvironment);
        if (optionNecessity == OptionNecessity.REQUIRED)
            return ".withRequired(true)";
        return "";
    }

    private String generateFlagValueCode() {
        String flagValueInput = optionInputs.getFlagValue();
        if (flagValueInput.isEmpty())
            return "";
        return ".withFlagValue(" + TextUtils.quote(flagValueInput) + ")";
    }

    private String generateDefaultValueCode() {
        String defaultValueInput = optionInputs.getDefaultValue();
        if (isNotUsingCustomDefaultValue())
            return "";
        return ".withDefault(" + TextUtils.quote(defaultValueInput) + ")";
    }
    private String generateDescriptionCode() {
        String descriptionInput = optionInputs.getDescription();
        if (descriptionInput.isEmpty())
            return "";
        return ".withDescription(" + TextUtils.quote(descriptionInput) + ")";
    }

    private boolean isNotUsingCustomDefaultValue() {
        OptionNecessity optionNecessity = ProcessingUtils.getEnumFromTypeElement(EnumSet.allOf(OptionNecessity.class), optionInputs.getNecessity(), processingEnvironment);
        return optionNecessity != OptionNecessity.OPTIONAL || optionInputs.getDefaultValue().isEmpty();
    }

}
