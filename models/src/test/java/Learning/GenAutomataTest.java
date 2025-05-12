package Learning;

import automata.sfa.SFA;
import learning.sfa.EDSM;
import org.junit.Test;
import org.sat4j.specs.TimeoutException;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import theory.BooleanAlgebra;
import theory.characters.CharPred;
import theory.intervals.UnaryCharIntervalSolver;
import utilities.PosNegSamples;
import utilities.exceptions.DeterminismViolationException;
import utilities.parsing.StringToUnicodeWordParser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static java.lang.String.format;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GenAutomataTest {


    @Test
    public void GeneralisePredicatesTest() throws IOException, InvalidConfigurationException, DeterminismViolationException, TimeoutException {

        //lratuple.printSamples();
        PosNegSamples<Character> samples =  PosNegSamples.readSamplesFromJsonFile("src/test/resources/killerExample.txt", new StringToUnicodeWordParser());

        BooleanAlgebra<CharPred, Character> algebra = new UnaryCharIntervalSolver();
        SFA<CharPred,Character> res = EDSM.run(samples.getPositiveSamples(), samples.getNegativeSamples(), algebra);

        res.createDotFile("RES", "/home/julian/");

        for (List<Character> e : samples.getPositiveSamples()) {
            assertTrue(format("The following sample should be accepted but is rejected: %s", e.toString()), res.accepts(e, algebra));
        }
        for (List<Character> e : samples.getNegativeSamples()) {
            assertFalse(res.accepts(e, algebra));
        }
    }
}
