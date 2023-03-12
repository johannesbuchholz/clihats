package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.execution.Command;
import io.github.johannesbuchholz.clihats.core.execution.text.TextCell;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class OptionHelpContent {

    static final String REQUIRED_TEXT = "required";

    private static final int MIN_DESCRIPTION_COL_WIDTH = 16;

    private final String name;
    private final String[] aliases;
    private final String[] valueDescriptions;
    private final String description;

    public static OptionHelpContent empty() {
        return new OptionHelpContent(null, null, null, null);
    }

    public OptionHelpContent(String name, String[] aliases, String[] valueDescriptions, String description) {
        this.name = name;
        this.aliases = aliases;
        this.valueDescriptions = valueDescriptions;
        this.description = description;
    }

    public TextCell[] asTextCells() {
        TextCell nameCol = TextCell.getNew(name);
        TextCell aliasesCell = TextCell.getNew(aliases);
        TextCell valueDescCell = TextCell.getNew(getFormattedValueDescription());
        int descriptionColWidth = Math.max(
                MIN_DESCRIPTION_COL_WIDTH,
                Command.COMMAND_DESCRIPTION_WIDTH - (nameCol.getWidth() + aliasesCell.getWidth() + valueDescCell.getWidth())
        );
        TextCell paragraphCell = TextCell.getNew(descriptionColWidth).paragraph(description);
        return new TextCell[] {nameCol, aliasesCell, valueDescCell, paragraphCell};
    }

    private String getFormattedValueDescription() {
        String joinedValueDesc = Arrays.stream(valueDescriptions).filter(Objects::nonNull).sorted().collect(Collectors.joining(", "));
        if (joinedValueDesc.isEmpty())
            return null;
        else
            return "(" + joinedValueDesc + ")";
    }

}
