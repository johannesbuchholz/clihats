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

public class OperandParserCodeGenerator extends AbstractArgumentParserCodeGenerator {

    private final int operandIndex;
    private final List<String> names;

    public OperandParserCodeGenerator(ArgumentDto argumentInputs, TargetParameter targetParameter, int operandIndex) {
        super(argumentInputs.getNecessity(), argumentInputs.getDescription(), argumentInputs.getDefaultValue(), argumentInputs.getMapper(), targetParameter);
        this.operandIndex = operandIndex;
        this.names = argumentInputs.getName();
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
                        "%s.operand(%s)%s%s%s%s%s%s",
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

    private SnippetCodeData generateMapperCode() {
        SnippetCodeData valueMapperCode = generateValueMapperCode(targetParameter.getTypeElement());
        if (valueMapperCode.isEmpty())
            return SnippetCodeData.empty();
        return SnippetCodeData.from(
                String.format(".withMapper(%s)", valueMapperCode.getCodeSnippet()),
                valueMapperCode.getImportPackages());
    }

    String generateName() {
        String name;
        if (!names.isEmpty()) {
            name = names.get(0);
        } else {
            name = targetParameter.getName();
        }
        return ".withDisplayName(" + TextUtils.quote(TextUtils.toUpperCaseString(name)) + ")";
    }

}
