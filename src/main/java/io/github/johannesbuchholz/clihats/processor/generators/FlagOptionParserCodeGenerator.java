package io.github.johannesbuchholz.clihats.processor.generators;

import io.github.johannesbuchholz.clihats.core.execution.parser.ArgumentParsers;
import io.github.johannesbuchholz.clihats.processor.exceptions.ArgumentConfigurationException;
import io.github.johannesbuchholz.clihats.processor.model.ArgumentDto;
import io.github.johannesbuchholz.clihats.processor.model.SnippetCodeData;
import io.github.johannesbuchholz.clihats.processor.util.ProcessingUtils;
import io.github.johannesbuchholz.clihats.processor.util.TextUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.VariableElement;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FlagOptionParserCodeGenerator extends AbstractArgumentParserCodeGenerator {

    public FlagOptionParserCodeGenerator(ArgumentDto argumentInputs, VariableElement targetVariableElement, ProcessingEnvironment processingEnvironment) throws ArgumentConfigurationException {
        super(
                argumentInputs,
                targetVariableElement,
                deduceMapperTypeAndVerify(argumentInputs.getMapper(), targetVariableElement.asType(), processingEnvironment),
                processingEnvironment);
    }

    @Override
    public SnippetCodeData generateParserCode() {
        SnippetCodeData mapperSnippetCodeData = generateMapperCode();
        Set<String> imports = new HashSet<>(ProcessingUtils.getPackageStrings(ArgumentParsers.class));
        imports.addAll(mapperSnippetCodeData.getImportPackages());
        return SnippetCodeData.from(
                String.format(
                        "%s.flagOption(%s)%s%s%s%s",
                        ArgumentParsers.class.getSimpleName(),
                        generateNames(),
                        generateFlagValueCode(),
                        generateDefaultValueCode(),
                        mapperSnippetCodeData.getCodeSnippet(),
                        generateDescriptionCode()
                ),
                imports
        );
    }

    private String generateNames() {
        Stream<String> nameStream;
        if (!argumentInputs.getName().isEmpty()) {
            nameStream = argumentInputs.getName().stream();
        } else {
            String variableName = targetVariableElement.getSimpleName().toString();
            nameStream = Stream.of("-" + variableName.charAt(0), "--" + TextUtils.toHyphenString(variableName));
        }
        return nameStream.map(TextUtils::quote).collect(Collectors.joining(", "));
    }

    private String generateFlagValueCode() {
        String flagValueInput = argumentInputs.getFlagValue();
        if (flagValueInput.isEmpty())
            return "";
        return ".withFlagValue(" + TextUtils.quote(flagValueInput) + ")";
    }

}
