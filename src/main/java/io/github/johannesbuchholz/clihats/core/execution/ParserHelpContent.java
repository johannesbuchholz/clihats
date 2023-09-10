package io.github.johannesbuchholz.clihats.core.execution;

import io.github.johannesbuchholz.clihats.core.text.TextCell;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ParserHelpContent {

    private static final int MIN_DESCRIPTION_COL_WIDTH = 16;

    private final Collection<String> primaryNames;
    private final Collection<String> secondaryNames;
    private final List<String> additionalInfo;
    // nullable
    private final String description;
    private final String synopsisSnippet;

    public ParserHelpContent(Collection<String> primaryNames, Collection<String> secondaryNames, List<String> additionalInfo, String description, String synopsisSnippet) {
        this.primaryNames = Objects.requireNonNull(primaryNames);
        this.secondaryNames = Objects.requireNonNull(secondaryNames);
        this.additionalInfo = Objects.requireNonNull(additionalInfo);
        this.description = description;
        this.synopsisSnippet = synopsisSnippet;
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
                .map(String::trim)
                .map(desc -> TextCell.getNew(descriptionColWidth).paragraph(desc))
                .orElse(TextCell.getNew());
        return new TextCell[] {nameCol, aliasesCell, valueDescCell, paragraphCell};
    }

    public String getSynopsisSnippet() {
        return synopsisSnippet;
    }

    private String getFormattedIndicators() {
        String joinedValueDesc = additionalInfo.stream().filter(Objects::nonNull).sorted().collect(Collectors.joining(", "));
        if (joinedValueDesc.isEmpty())
            return null;
        else
            return "(" + joinedValueDesc + ")";
    }

}

