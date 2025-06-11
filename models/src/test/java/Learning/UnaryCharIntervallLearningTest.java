package Learning;

import automata.sfa.SFA;
import learning.sfa.EDSM;
import learning.sfa.RPNI;
import org.junit.Test;
import org.sat4j.specs.TimeoutException;
import org.sosy_lab.java_smt.api.BooleanFormula;
import theory.BooleanAlgebra;
import theory.LRATuples.LRATuple;
import theory.LRATuples.RationalTupleCompAlgebra;
import theory.characters.CharPred;
import theory.intervals.UnaryCharIntervalSolver;
import utilities.PosNegSamples;
import utilities.exceptions.DeterminismViolationException;
import utilities.parsing.LRATupleParser;
import utilities.parsing.StringToUnicodeWordParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static java.lang.String.format;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UnaryCharIntervallLearningTest {

    @Test
    public void FirstTest() throws IOException, DeterminismViolationException, TimeoutException {

        //lratuple.printSamples();
        PosNegSamples<Character> samples =  PosNegSamples.readSamplesFromJsonFile("src/test/resources/IntervalProblems/characterSamples.json", new StringToUnicodeWordParser());

        BooleanAlgebra<CharPred, Character> algebra = new UnaryCharIntervalSolver();
        SFA<CharPred,Character> res = EDSM.run(samples.getPositiveSamples(), samples.getNegativeSamples(), algebra);

        res = res.mkTotal(algebra);

        //res.createDotFile("ASD", "/home/julian/");


        for (List<Character> e : samples.getPositiveSamples()) {
            assertTrue(format("The following sample should be accepted but is rejected: %s", e.toString()), res.accepts(e, algebra));
        }
        for (List<Character> e : samples.getNegativeSamples()) {
            assertFalse(res.accepts(e, algebra));
        }
    }

    @Test
    public void TestAllProblemsInFolderWithEDSM() throws IOException, DeterminismViolationException, TimeoutException {
        File folder = new File("src/test/resources/IntervalProblems/");
        File[] listOfFiles = folder.listFiles();

        assert listOfFiles != null;
        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                PosNegSamples<Character> samples =  PosNegSamples.readSamplesFromJsonFile("src/test/resources/IntervalProblems/characterSamples.json", new StringToUnicodeWordParser());

                BooleanAlgebra<CharPred, Character> algebra = new UnaryCharIntervalSolver();
                SFA<CharPred,Character> res = EDSM.run(samples.getPositiveSamples(), samples.getNegativeSamples(), algebra);

                for (List<Character> e : samples.getPositiveSamples()) {
                    assertTrue(format("The following sample should be accepted but is rejected: %s", e), res.accepts(e, algebra));
                }

                for (List<Character> e : samples.getNegativeSamples()) {
                    assertFalse(format("The following sample should NOT be accepted but is accepted: %s", e), res.accepts(e, algebra));
                }

                //res.createDotFile("ASD", "/home/julian/");

                //res.createDotFile(file.getName().substring(0, file.getName().length() - 4), "/home/julian/");
                //SFA<BooleanFormula, LRATuple> a = SFA.MkPTA(lratuple.getPositiveSamples(), lratuple.getNegativeSamples(), algebra);
                //a.createDotFile(file.getName().substring(0, file.getName().length() - 4)+"_PTA", "/home/julian/");
            }
        }
    }


    @Test
    public void TestAllProblemsInFolderWithRPNI() throws IOException, DeterminismViolationException, TimeoutException {
        File folder = new File("src/test/resources/IntervalProblems/");
        File[] listOfFiles = folder.listFiles();

        assert listOfFiles != null;
        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                PosNegSamples<Character> samples =  PosNegSamples.readSamplesFromJsonFile("src/test/resources/IntervalProblems/characterSamples.json", new StringToUnicodeWordParser());

                BooleanAlgebra<CharPred, Character> algebra = new UnaryCharIntervalSolver();
                SFA<CharPred,Character> res = RPNI.run(samples.getPositiveSamples(), samples.getNegativeSamples(), algebra);

                for (List<Character> e : samples.getPositiveSamples()) {
                    assertTrue(format("The following sample should be accepted but is rejected: %s", e), res.accepts(e, algebra));
                }

                for (List<Character> e : samples.getNegativeSamples()) {
                    assertFalse(format("The following sample should NOT be accepted but is accepted: %s", e), res.accepts(e, algebra));
                }

                //res.createDotFile("ASD", "/home/julian/");

                //res.createDotFile(file.getName().substring(0, file.getName().length() - 4), "/home/julian/");
                //SFA<BooleanFormula, LRATuple> a = SFA.MkPTA(lratuple.getPositiveSamples(), lratuple.getNegativeSamples(), algebra);
                //a.createDotFile(file.getName().substring(0, file.getName().length() - 4)+"_PTA", "/home/julian/");
            }
        }
    }
}
