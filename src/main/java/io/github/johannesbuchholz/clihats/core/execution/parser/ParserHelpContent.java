package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.execution.Command;
import io.github.johannesbuchholz.clihats.core.text.TextCell;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

// TODO: Add synopsis part.
public class ParserHelpContent {

    private static final int MIN_DESCRIPTION_COL_WIDTH = 16;

    private final Collection<String> primaryNames;
    private final Collection<String> secondaryNames;
    private final List<String> valueDescriptions;
    private final String description;

    public static ParserHelpContent empty() {
        return new ParserHelpContent(null, null, null, null);
    }

    public ParserHelpContent(Collection<String> primaryNames, Collection<String> secondaryNames, List<String> indicators, String description) {
        this.primaryNames = primaryNames;
        this.secondaryNames = secondaryNames;
        this.valueDescriptions = indicators;
        this.description = description;
    }

    public TextCell[] asTextCells() {
        TextCell nameCol = TextCell.getNew(primaryNames);
        TextCell aliasesCell = TextCell.getNew(secondaryNames);
        TextCell valueDescCell = TextCell.getNew(getFormattedIndicators());
        int descriptionColWidth = Math.max(
                MIN_DESCRIPTION_COL_WIDTH,
                Command.COMMAND_DESCRIPTION_WIDTH - (nameCol.getWidth() + aliasesCell.getWidth() + valueDescCell.getWidth())
        );
        TextCell paragraphCell = Optional.ofNullable(description)
                .map(desc -> TextCell.getNew(descriptionColWidth).paragraph(desc))
                .orElse(TextCell.getNew("<No description>"));
        return new TextCell[] {nameCol, aliasesCell, valueDescCell, paragraphCell};
    }

    private String getFormattedIndicators() {
        String joinedValueDesc = valueDescriptions.stream().filter(Objects::nonNull).sorted().collect(Collectors.joining(", "));
        if (joinedValueDesc.isEmpty())
            return null;
        else
            return "(" + joinedValueDesc + ")";
    }

}

