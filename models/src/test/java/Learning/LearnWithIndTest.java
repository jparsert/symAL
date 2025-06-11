package Learning;

import automata.sfa.SFA;
import learning.sfa.LearningWithIndEx;
import org.junit.Test;
import org.sat4j.specs.TimeoutException;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import theory.BooleanAlgebra;
import theory.characters.CharPred;
import theory.intervals.UnaryCharIntervalSolver;
import utilities.LearningSamples;
import utilities.Pair;
import utilities.exceptions.DeterminismViolationException;
import utilities.parsing.StringToUnicodeWordParser;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.lang.String.format;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LearnWithIndTest {



    @Test
    public void RPNIIndCharIntervalTest() throws IOException, InvalidConfigurationException, DeterminismViolationException, TimeoutException {
        File folder = new File("src/test/resources/IntervalProblemsInductive");
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;

        int files = 0;

        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                files += 1;
                LearningSamples<Character> samples =  LearningSamples.readSamplesFromJsonFile(file.getPath(), new StringToUnicodeWordParser());

                assert(!samples.getImplicationSamples().isEmpty());

                BooleanAlgebra<CharPred, Character> algebra = new UnaryCharIntervalSolver();

                SFA<CharPred, Character> res = LearningWithIndEx.RPNIInductiveFixedPoint(samples, algebra);

                for (List<Character> e : samples.getPositiveSamples()) {
                    assertTrue(format("The following sample should be accepted but is rejected: %s", e), res.accepts(e, algebra));
                }

                for (List<Character> e : samples.getNegativeSamples()) {
                    assertFalse(format("The following sample should NOT be accepted but is accepted: %s", e), res.accepts(e, algebra));
                }

                for (Pair<List<Character>, List<Character>> e : samples.getImplicationSamples()) {
                    if (res.accepts(e.getFirst(), algebra)) {
                        assertTrue(format("The following Implication Sample should be accepted but is rejected: %s", e), res.accepts(e.getSecond(), algebra));
                    }
                }


            }
        }
        assert(files > 0);
    }



    @Test
    public void EDSMIndCharIntervalTest() throws IOException, InvalidConfigurationException, DeterminismViolationException, TimeoutException {
        File folder = new File("src/test/resources/IntervalProblemsInductive");
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;

        int files = 0;

        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                files += 1;
                LearningSamples<Character> samples =  LearningSamples.readSamplesFromJsonFile(file.getPath(), new StringToUnicodeWordParser());

                assert(!samples.getImplicationSamples().isEmpty());

                BooleanAlgebra<CharPred, Character> algebra = new UnaryCharIntervalSolver();

                SFA<CharPred, Character> res = LearningWithIndEx.EDSMInductiveFixedPoint(samples, algebra);

                for (List<Character> e : samples.getPositiveSamples()) {
                    assertTrue(format("The following sample should be accepted but is rejected: %s", e), res.accepts(e, algebra));
                }

                for (List<Character> e : samples.getNegativeSamples()) {
                    assertFalse(format("The following sample should NOT be accepted but is accepted: %s", e), res.accepts(e, algebra));
                }

                for (Pair<List<Character>, List<Character>> e : samples.getImplicationSamples()) {
                    if (res.accepts(e.getFirst(), algebra)) {
                        assertTrue(format("The following Implication Sample should be accepted but is rejected: %s", e), res.accepts(e.getSecond(), algebra));
                    }
                }

                res = res.mkTotal(algebra);
                //res.createDotFile("ASD", "/home/julian/");

            }
        }
        assert(files > 0);
    }


}
