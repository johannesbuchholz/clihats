package io.github.johannesbuchholz.clihats.processor.generators;

import io.github.johannesbuchholz.clihats.core.execution.parser.ArgumentParsers;
import io.github.johannesbuchholz.clihats.processor.model.ArgumentDto;
import io.github.johannesbuchholz.clihats.processor.model.SnippetCodeData;
import io.github.johannesbuchholz.clihats.processor.model.TargetParameter;
import io.github.johannesbuchholz.clihats.processor.util.ProcessingUtils;
import io.github.johannesbuchholz.clihats.processor.util.TextUtils;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArrayOperandParserCodeGenerator extends AbstractArgumentParserCodeGenerator {

    private final List<String> names;
    private final int operandIndex;
    private final DeclaredType componentType;

    public ArrayOperandParserCodeGenerator(ArgumentDto argumentInputs, TargetParameter targetParameter, int operandIndex, DeclaredType componentType) {
        super(argumentInputs.getNecessity(), argumentInputs.getDescription(), argumentInputs.getDefaultValue(), argumentInputs.getMapper(), targetParameter);
        names = argumentInputs.getName();
        this.operandIndex = operandIndex;
        this.componentType = componentType;
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

    private String generateName() {
        String displayName;
        if (!names.isEmpty()) {
            displayName = names.get(0);
        } else {
            displayName = targetParameter.getName();
        }
        return ".withDisplayName(" + TextUtils.quote(TextUtils.toUpperCaseString(displayName)) + ")";
    }

    private SnippetCodeData generateMapperCode() {
        SnippetCodeData valueMapperCode = generateValueMapperCode((TypeElement) componentType.asElement());
        if (valueMapperCode.isEmpty())
            return SnippetCodeData.empty();
        Set<String> imports = new HashSet<>(valueMapperCode.getImportPackages());
        imports.addAll(ProcessingUtils.getPackageStrings((TypeElement) componentType.asElement()));
        return SnippetCodeData.from(
                String.format(".withMapper(%s, %s.class)", valueMapperCode.getCodeSnippet(), componentType.asElement().getSimpleName()),
                imports);
    }

    @Override
    String generateDefaultValueCode() {
        if (isNotUsingCustomDefaultValue())
            return "";
        return ".withDefault(new String[]{" + TextUtils.quote(defaultValue) + "})";
    }

}
