package SFA;

import automata.sfa.SFA;
import learning.sfa.LearningWithIndEx;
import learning.sfa.RPNI;
import org.junit.Test;
import org.sat4j.specs.TimeoutException;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.java_smt.api.BooleanFormula;
import theory.BooleanAlgebra;
import theory.LRATuples.LRATuple;
import theory.LRATuples.RationalTupleCompAlgebra;
import theory.characters.CharPred;
import theory.intervals.UnaryCharIntervalSolver;
import utilities.LearningSamples;
import utilities.Pair;
import utilities.exceptions.DeterminismViolationException;
import utilities.parsing.LRATupleParser;
import utilities.parsing.StringToUnicodeWordParser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MKPTATest {

    @Test
    public void testMKPTAIntervalFromJsonTest() throws IOException, DeterminismViolationException, TimeoutException {
        File folder = new File("src/test/resources/IntervalProblemsInductive");
        List<File> listOfFiles = new java.util.ArrayList<>(List.of(Objects.requireNonNull(folder.listFiles())));

        folder = new File("src/test/resources/IntervalProblems");
        listOfFiles.addAll(List.of(Objects.requireNonNull(folder.listFiles())));

        int files = 0;

        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                files += 1;

                LearningSamples<Character> samples =  LearningSamples.readSamplesFromJsonFile(file.getPath(), new StringToUnicodeWordParser());
                BooleanAlgebra<CharPred, Character> algebra = new UnaryCharIntervalSolver();
                SFA<CharPred, Character> res = SFA.MkPTA(samples.getPositiveSamples(), samples.getNegativeSamples(),  algebra);

                for (List<Character> e : samples.getPositiveSamples()) {
                    assertTrue(format("The following sample should be accepted but is rejected: %s", e), res.accepts(e, algebra));
                }

                for (List<Character> e : samples.getNegativeSamples()) {
                    assertFalse(format("The following sample should NOT be accepted but is accepted: %s", e), res.accepts(e, algebra));
                }
            }
        }
        assert(files > 0);
    }

    @Test
    public void testMKPTARatTuplesTest() throws IOException, DeterminismViolationException, TimeoutException, InvalidConfigurationException {
        File folder = new File("src/test/resources/RatTuples");
        File[] listOfFiles = folder.listFiles();

        assert listOfFiles != null;
        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(".txt")) {
                LearningSamples<LRATuple> lratuple = LearningSamples.readSamplesFromFile(file.getPath(), new LRATupleParser());

                BooleanAlgebra<BooleanFormula, LRATuple> algebra = new RationalTupleCompAlgebra(2);

                SFA<BooleanFormula, LRATuple> res = SFA.MkPTA(lratuple.getPositiveSamples(), lratuple.getNegativeSamples(), algebra);

                for (List<LRATuple> e : lratuple.getPositiveSamples()) {
                    assertTrue(format("The following sample should be accepted but is rejected: %s", e), res.accepts(e, algebra));
                }

                for (List<LRATuple> e : lratuple.getNegativeSamples()) {
                    assertFalse(format("The following sample should NOT be accepted but is accepted: %s", e), res.accepts(e, algebra));
                }
            }
        }
    }
}
