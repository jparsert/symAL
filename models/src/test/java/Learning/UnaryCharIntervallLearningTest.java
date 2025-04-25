package Learning;

import automata.sfa.SFA;
import learning.sfa.EDSM;
import org.junit.Test;
import org.sat4j.specs.TimeoutException;
import theory.BooleanAlgebra;
import theory.characters.CharPred;
import theory.intervals.UnaryCharIntervalSolver;
import utilities.PosNegSamples;
import utilities.exceptions.DeterminismViolationException;
import utilities.parsing.StringToUnicodeWordParser;

import java.io.IOException;
import java.util.List;

import static java.lang.String.format;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UnaryCharIntervallLearningTest {

    @Test
    public void FirstTest() throws IOException, DeterminismViolationException, TimeoutException {

        //lratuple.printSamples();
        PosNegSamples<Character> samples =  PosNegSamples.readSamplesFromJsonFile("src/test/resources/killerExample.txt", new StringToUnicodeWordParser());

        BooleanAlgebra<CharPred, Character> algebra = new UnaryCharIntervalSolver();
        SFA<CharPred,Character> res = EDSM.run(samples.getPositiveSamples(), samples.getNegativeSamples(), algebra);


        for (List<Character> e : samples.getPositiveSamples()) {
            assertTrue(format("The following sample should be accepted but is rejected: %s", e.toString()), res.accepts(e, algebra));
        }
        for (List<Character> e : samples.getNegativeSamples()) {
            assertFalse(res.accepts(e, algebra));
        }
    }

}
