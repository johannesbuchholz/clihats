package io.github.johannesbuchholz.clihats.core.execution.parser;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParserIdSortingTest {

    @Test
    public void testSorting_POSIXCompliantOptionNamesFirst() {
        AbstractOptionParser.OptionParserName name1 = AbstractOptionParser.OptionParserName.of("-z");
        AbstractOptionParser.OptionParserName name2 = AbstractOptionParser.OptionParserName.of("--a");

        AbstractOptionParser.OptionParserId id1 = new AbstractOptionParser.OptionParserId(Set.of(name1));
        AbstractOptionParser.OptionParserId id2 = new AbstractOptionParser.OptionParserId(Set.of(name2));

        assertTrue(id1.compareTo(id2) < 0);
    }

    @Test
    public void testSorting_POSIXCompliantOptionNamesByValue() {
        AbstractOptionParser.OptionParserName name1 = AbstractOptionParser.OptionParserName.of("-z");
        AbstractOptionParser.OptionParserName name2 = AbstractOptionParser.OptionParserName.of("-a");

        AbstractOptionParser.OptionParserId id1 = new AbstractOptionParser.OptionParserId(Set.of(name1));
        AbstractOptionParser.OptionParserId id2 = new AbstractOptionParser.OptionParserId(Set.of(name2));

        assertTrue(id1.compareTo(id2) > 0);
    }

    @Test
    public void testSorting_NonPOSIXCompliantOptionNamesByValue() {
        AbstractOptionParser.OptionParserName name1 = AbstractOptionParser.OptionParserName.of("--z");
        AbstractOptionParser.OptionParserName name2 = AbstractOptionParser.OptionParserName.of("--a");

        AbstractOptionParser.OptionParserId id1 = new AbstractOptionParser.OptionParserId(Set.of(name1));
        AbstractOptionParser.OptionParserId id2 = new AbstractOptionParser.OptionParserId(Set.of(name2));

        assertTrue(id1.compareTo(id2) > 0);
    }

    @Test
    public void testSorting_NonPOSIXCompliantOptionNamesByValue_multipleNames() {
        AbstractOptionParser.OptionParserName nameA = AbstractOptionParser.OptionParserName.of("--a");
        AbstractOptionParser.OptionParserName nameB = AbstractOptionParser.OptionParserName.of("--b");
        AbstractOptionParser.OptionParserName nameZ = AbstractOptionParser.OptionParserName.of("--z");

        AbstractOptionParser.OptionParserId id1 = new AbstractOptionParser.OptionParserId(Set.of(nameB, nameA));
        AbstractOptionParser.OptionParserId id2 = new AbstractOptionParser.OptionParserId(Set.of(nameA, nameZ));

        assertTrue(id1.compareTo(id2) < 0);
    }

    @Test
    public void testSorting_POSIXCompliantOptionNamesEqual() {
        AbstractOptionParser.OptionParserName name1 = AbstractOptionParser.OptionParserName.of("-a");
        AbstractOptionParser.OptionParserName name2 = AbstractOptionParser.OptionParserName.of("-a");

        AbstractOptionParser.OptionParserId id1 = new AbstractOptionParser.OptionParserId(Set.of(name1));
        AbstractOptionParser.OptionParserId id2 = new AbstractOptionParser.OptionParserId(Set.of(name2));

        assertEquals(0, id1.compareTo(id2));
    }

    @Test
    public void testSorting_NonPOSIXCompliantOptionNamesEqual() {
        AbstractOptionParser.OptionParserName name1 = AbstractOptionParser.OptionParserName.of("--a");
        AbstractOptionParser.OptionParserName name2 = AbstractOptionParser.OptionParserName.of("--a");

        AbstractOptionParser.OptionParserId id1 = new AbstractOptionParser.OptionParserId(Set.of(name1));
        AbstractOptionParser.OptionParserId id2 = new AbstractOptionParser.OptionParserId(Set.of(name2));

        assertEquals(0, id1.compareTo(id2));
    }

    @Test
    public void testSorting_OperandsAfterOptions() {
        AbstractOptionParser.OptionParserName optionName = AbstractOptionParser.OptionParserName.of("-a");
        int operandPosition = 9999;

        AbstractOperandParser.OperandParserId idOperand = new AbstractOperandParser.OperandParserId(operandPosition);
        AbstractOptionParser.OptionParserId idOption = new AbstractOptionParser.OptionParserId(Set.of(optionName));

        assertTrue(idOperand.compareTo(idOption) > 0);
    }

    @Test
    public void testSorting_OperandsByPosition() {
        int operandPosition = 9999;
        int operandPosition2 = 1111;

        AbstractOperandParser.OperandParserId idOperand = new AbstractOperandParser.OperandParserId(operandPosition);
        AbstractOperandParser.OperandParserId idOperand2 = new AbstractOperandParser.OperandParserId(operandPosition2);

        assertTrue(idOperand.compareTo(idOperand2) > 0);
    }

}
