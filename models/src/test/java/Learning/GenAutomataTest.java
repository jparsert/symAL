package Learning;

import automata.sfa.SFA;
import learning.sfa.EDSM;
import learning.sfa.GenAutomata;
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
    public void GeneralisePredicatesTest() throws IOException, DeterminismViolationException, TimeoutException {

        //lratuple.printSamples();
        PosNegSamples<Character> samples =  PosNegSamples.readSamplesFromJsonFile("src/test/resources/IntervallProblems/killerExample.json", new StringToUnicodeWordParser());

        BooleanAlgebra<CharPred, Character> algebra = new UnaryCharIntervalSolver();
        SFA<CharPred,Character> res = EDSM.run(samples.getPositiveSamples(), samples.getNegativeSamples(), algebra);

        //res.createDotFile("RES", "/home/julian/");

        res = res.mkTotal(algebra);

        for (List<Character> e : samples.getPositiveSamples()) {
            assertTrue(format("The following sample should be accepted but is rejected: %s", e.toString()), res.accepts(e, algebra));
        }
        for (List<Character> e : samples.getNegativeSamples()) {
            assertFalse(format("The following sample should be rejected but is accepted: %s", e.toString()), res.accepts(e, algebra));
        }


        //res.createDotFile("RES1", "/home/julian/");

        res = GenAutomata.generaliseTransitions(res, algebra);

        //res.createDotFile("RES2", "/home/julian/");

        for (List<Character> e : samples.getPositiveSamples()) {
            assertTrue(format("The following sample should be accepted but is rejected: %s", e.toString()), res.accepts(e, algebra));
        }
        for (List<Character> e : samples.getNegativeSamples()) {
            assertFalse(format("The following sample should be rejected but is accepted: %s", e.toString()), res.accepts(e, algebra));
        }
    }
}
