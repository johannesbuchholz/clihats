package io.github.johannesbuchholz.clihats.processor.generators;

import io.github.johannesbuchholz.clihats.core.execution.parser.ArgumentParsers;
import io.github.johannesbuchholz.clihats.processor.model.ArgumentDto;
import io.github.johannesbuchholz.clihats.processor.model.SnippetCodeData;
import io.github.johannesbuchholz.clihats.processor.model.TargetParameter;
import io.github.johannesbuchholz.clihats.processor.util.ProcessingUtils;
import io.github.johannesbuchholz.clihats.processor.util.TextUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValuedOptionParserCodeGenerator extends AbstractArgumentParserCodeGenerator {

    private final List<String> names;

    public ValuedOptionParserCodeGenerator(ArgumentDto argumentInputs, TargetParameter targetParameter) {
        super(argumentInputs.getNecessity(), argumentInputs.getDescription(), argumentInputs.getDefaultValue(), argumentInputs.getMapper(), targetParameter);
        names = argumentInputs.getName();
    }

    @Override
    public SnippetCodeData generateParserCode() {
        SnippetCodeData mapperSnippetCodeData = generateMapperCode();
        SnippetCodeData promptSnippetCodeData = generatePromptCode();
        Set<String> imports = new HashSet<>(ProcessingUtils.getPackageStrings(ArgumentParsers.class));
        imports.addAll(mapperSnippetCodeData.getImportPackages());
        imports.addAll(promptSnippetCodeData.getImportPackages());
        return SnippetCodeData.from(
                String.format(
                        "%s.valuedOption(%s)%s%s%s%s%s",
                        ArgumentParsers.class.getSimpleName(),
                        generateNames(),
                        generateRequiredCode(),
                        generateDefaultValueCode(),
                        generateDescriptionCode(),
                        mapperSnippetCodeData.getCodeSnippet(),
                        promptSnippetCodeData.getCodeSnippet()
                ),
                imports
        );
    }

    private SnippetCodeData generateMapperCode() {
        SnippetCodeData valueMapperCode = generateValueMapperCode(targetParameter.getTypeElement());
        if (valueMapperCode.isEmpty())
            return SnippetCodeData.empty();
        return SnippetCodeData.from(
                String.format(".withMapper(%s)", valueMapperCode.getCodeSnippet()),
                valueMapperCode.getImportPackages());
    }

    private String generateNames() {
        Stream<String> nameStream;
        if (!names.isEmpty()) {
            nameStream = names.stream();
        } else {
            String variableName = targetParameter.getName();
            nameStream = Stream.of("-" + variableName.charAt(0), "--" + TextUtils.toHyphenString(variableName));
        }
        return nameStream.map(TextUtils::quote).collect(Collectors.joining(", "));
    }

}
