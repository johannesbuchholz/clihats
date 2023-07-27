package io.github.johannesbuchholz.clihats.core.execution.parser;

import org.junit.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParserIdCommonPartsTest {

    @Test
    public void commonPositions() {
        int commonPosition = 1234;
        AbstractOperandParser.OperandParserId id1 = new AbstractOperandParser.OperandParserId(commonPosition);
        AbstractOperandParser.OperandParserId id2 = new AbstractOperandParser.OperandParserId(commonPosition);

        Optional<String> opt = id1.hasCommonParts(id2);
        Optional<String> opt2 = id2.hasCommonParts(id1);
        assertEquals(opt, opt2);
        assertTrue(opt.isPresent());
        assertTrue(opt.get().contains(String.valueOf(commonPosition)));
    }

    @Test
    public void notCommonBetweenOperandsAndOptions() {
        AbstractOperandParser.OperandParserId idOperand = new AbstractOperandParser.OperandParserId(0);
        AbstractOptionParser.OptionParserName name = AbstractOptionParser.OptionParserName.of("-a");
        AbstractOptionParser.OptionParserId idOption = new AbstractOptionParser.OptionParserId(Set.of(name));

        Optional<String> opt = idOperand.hasCommonParts(idOption);
        Optional<String> opt2 = idOption.hasCommonParts(idOperand);
        assertEquals(opt, opt2);
        assertTrue(opt.isEmpty());
    }

    @Test
    public void commonOnNames() {
        AbstractOptionParser.OptionParserName nameA = AbstractOptionParser.OptionParserName.of("-a");
        AbstractOptionParser.OptionParserName nameB = AbstractOptionParser.OptionParserName.of("-b");
        AbstractOptionParser.OptionParserName nameC = AbstractOptionParser.OptionParserName.of("-c");
        String commonPartX = "-x";
        AbstractOptionParser.OptionParserName nameX = AbstractOptionParser.OptionParserName.of(commonPartX);
        AbstractOptionParser.OptionParserName nameY = AbstractOptionParser.OptionParserName.of("-y");
        String commonPartZ = "--zZz";
        AbstractOptionParser.OptionParserName nameZ = AbstractOptionParser.OptionParserName.of(commonPartZ);

        AbstractOptionParser.OptionParserId idOptionABC = new AbstractOptionParser.OptionParserId(Set.of(nameZ, nameA, nameB, nameC, nameX));
        AbstractOptionParser.OptionParserId idOptionXYZ = new AbstractOptionParser.OptionParserId(Set.of(nameX, nameY, nameZ));

        Optional<String> opt = idOptionABC.hasCommonParts(idOptionXYZ);
        Optional<String> opt2 = idOptionXYZ.hasCommonParts(idOptionABC);
        assertEquals(opt, opt2);
        assertTrue(opt.isPresent());
        assertTrue(opt.get().contains(commonPartX));
        assertTrue(opt.get().contains(commonPartZ));
    }

    @Test
    public void notCommonOnNames() {
        AbstractOptionParser.OptionParserName nameA = AbstractOptionParser.OptionParserName.of("-a");
        AbstractOptionParser.OptionParserName nameB = AbstractOptionParser.OptionParserName.of("-b");
        AbstractOptionParser.OptionParserName nameC = AbstractOptionParser.OptionParserName.of("-c");
        AbstractOptionParser.OptionParserName nameX = AbstractOptionParser.OptionParserName.of("-x");
        AbstractOptionParser.OptionParserName nameY = AbstractOptionParser.OptionParserName.of("-y");
        AbstractOptionParser.OptionParserName nameZ = AbstractOptionParser.OptionParserName.of("-z");

        AbstractOptionParser.OptionParserId idOptionABC = new AbstractOptionParser.OptionParserId(Set.of(nameA, nameB, nameC));
        AbstractOptionParser.OptionParserId idOptionXYZ = new AbstractOptionParser.OptionParserId(Set.of(nameX, nameY, nameZ));

        Optional<String> opt = idOptionABC.hasCommonParts(idOptionXYZ);
        Optional<String> opt2 = idOptionXYZ.hasCommonParts(idOptionABC);
        assertEquals(opt, opt2);
        assertTrue(opt.isEmpty());
    }



}
