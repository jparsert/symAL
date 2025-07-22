package Learning;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import automata.Move;
import automata.sfa.SFA;
import automata.sfa.SFAInputMove;
import learning.sfa.RPNI;
import org.junit.Test;
import org.sat4j.specs.TimeoutException;

import static java.lang.String.format;
import static org.junit.Assert.*;

import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.java_smt.api.BooleanFormula;
import theory.BooleanAlgebra;
import theory.LRATuples.LRATuple;
import theory.LRATuples.RationalTupleCompAlgebra;
import theory.characters.CharPred;
import theory.characters.CharTuplePred;
import theory.intervals.CharIntervalTupleSolver;
import theory.intervals.RealPred;
import theory.intervals.RealSolver;
import theory.intervals.UnaryCharIntervalSolver;
import utilities.exceptions.DeterminismViolationException;
import utilities.parsing.CharIntervalTupleParser;
import utilities.parsing.DoubleWordParser;
import utilities.parsing.LRATupleParser;
import utilities.LearningSamples;
import utilities.parsing.StringToUnicodeWordParser;


public class RPNITest {

    @Test
    public void AllRatTuplesInResources() throws FileNotFoundException, InvalidConfigurationException, DeterminismViolationException, TimeoutException {
        File folder = new File("src/test/resources/RatTuples");
        File[] listOfFiles = folder.listFiles();

        assert listOfFiles != null;
        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(".txt")) {
                LearningSamples<LRATuple> lratuple =  LearningSamples.readSamplesFromFile(file.getPath(), new LRATupleParser());

                BooleanAlgebra<BooleanFormula, LRATuple> algebra = new RationalTupleCompAlgebra(2);

                SFA<BooleanFormula,LRATuple> res = RPNI.run(lratuple.getPositiveSamples(), lratuple.getNegativeSamples(), algebra);

                for (List<LRATuple> e : lratuple.getPositiveSamples()) {
                    assertTrue(format("The following sample should be accepted but is rejected: %s", e), res.accepts(e, algebra));
                }

                for (List<LRATuple> e : lratuple.getNegativeSamples()) {
                    assertFalse(format("The following sample should NOT be accepted but is accepted: %s", e), res.accepts(e, algebra));
                }
            }
        }
    }

    @Test
    public void RPNICharIntervalTupleTest() throws IOException, InvalidConfigurationException, DeterminismViolationException, TimeoutException {
        File folder = new File("src/test/resources/CharIntervalTuples");
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;

        int files = 0;

        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                files += 1;
                LearningSamples<Character[]> samples =  LearningSamples.readSamplesFromJsonFile(file.getPath(), new CharIntervalTupleParser());

                assertTrue(samples.getDimension().isPresent());
                BooleanAlgebra<CharTuplePred, Character[]> algebra = new CharIntervalTupleSolver(samples.getDimension().get());

                SFA<CharTuplePred, Character[]> res = RPNI.run(samples.getPositiveSamples(), samples.getNegativeSamples(), algebra);

                for (List<Character[]> e : samples.getPositiveSamples()) {
                    assertTrue(format("The following sample should be accepted but is rejected: %s", e), res.accepts(e, algebra));
                }

                for (List<Character[]> e : samples.getNegativeSamples()) {
                    assertFalse(format("The following sample should NOT be accepted but is accepted: %s", e), res.accepts(e, algebra));
                }
                //res = res.mkTotal(algebra);
                //res.createDotFile("ASD", "/home/julian/");

            }
        }
        assert(files > 0);
    }


    @Test
    public void RPNICharIntervalTest() throws IOException, InvalidConfigurationException, DeterminismViolationException, TimeoutException {
        File folder = new File("src/test/resources/IntervalProblems");
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;

        int files = 0;

        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                files += 1;
                LearningSamples<Character> samples =  LearningSamples.readSamplesFromJsonFile(file.getPath(), new StringToUnicodeWordParser());

                BooleanAlgebra<CharPred, Character> algebra = new UnaryCharIntervalSolver();

                SFA<CharPred, Character> res = RPNI.run(samples.getPositiveSamples(), samples.getNegativeSamples(), algebra);

                for (List<Character> e : samples.getPositiveSamples()) {
                    assertTrue(format("The following sample should be accepted but is rejected: %s", e), res.accepts(e, algebra));
                }

                for (List<Character> e : samples.getNegativeSamples()) {
                    assertFalse(format("The following sample should NOT be accepted but is accepted: %s", e), res.accepts(e, algebra));
                }
                //res = res.mkTotal(algebra);
                //res.createDotFile("ASD", "/home/julian/");

            }
        }
        assert(files > 0);
    }



    @Test
    public void RPNIRealIntervals() throws IOException, InvalidConfigurationException, DeterminismViolationException, TimeoutException {
        File folder = new File("src/test/resources/RealIntervalProblems");
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;

        int files = 0;

        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                files += 1;
                LearningSamples<Double> samples =  LearningSamples.readSamplesFromJsonFile(file.getPath(), new DoubleWordParser());

                BooleanAlgebra<RealPred, Double> algebra = new RealSolver();

                SFA<RealPred, Double> res = RPNI.run(samples.getPositiveSamples(), samples.getNegativeSamples(), algebra);

                for (List<Double> e : samples.getPositiveSamples()) {
                    assertTrue(format("The following sample should be accepted but is rejected: %s", e), res.accepts(e, algebra));
                }

                for (List<Double> e : samples.getNegativeSamples()) {
                    assertFalse(format("The following sample should NOT be accepted but is accepted: %s", e), res.accepts(e, algebra));
                }
                res = res.mkTotal(algebra);
                res.createDotFile("ASD", "/home/julian/");

            }
        }
        assert(files > 0);
    }


    @Test
    public void printingAfterRPNI() throws IOException, DeterminismViolationException, TimeoutException {
        File file = new File("src/test/resources/IntervalProblems/OddPrintingBehaviour.json");
        LearningSamples<Character> samples =  LearningSamples.readSamplesFromJsonFile(file.getPath(), new StringToUnicodeWordParser());

        BooleanAlgebra<CharPred, Character> algebra = new UnaryCharIntervalSolver();

        SFA<CharPred, Character> res = RPNI.run(samples.getPositiveSamples(), samples.getNegativeSamples(), algebra);
        res = res.mkTotal(algebra);

        res.createDotFile("RPNITest", "/home/julian/");

        for (List<Character> e : samples.getPositiveSamples()) {
            assertTrue(format("The following sample should be accepted but is rejected: %s", e), res.accepts(e, algebra));
        }

        for (List<Character> e : samples.getNegativeSamples()) {
            assertFalse(format("The following sample should NOT be accepted but is accepted: %s", e), res.accepts(e, algebra));
        }
    }

    @Test
    public void PTATest() throws FileNotFoundException, InvalidConfigurationException, DeterminismViolationException, TimeoutException {
        LearningSamples<LRATuple> lratuple =  LearningSamples.readSamplesFromFile("../benchmarks/passiveLearning/ratTuple.txt", new LRATupleParser());
        //lratuple.printSamples();

        BooleanAlgebra<BooleanFormula, LRATuple> algebra = new RationalTupleCompAlgebra(2);
        //SFA<BooleanFormula,LRATuple> res = rpni.runRPNI(lratuple.getPositiveSamples(), lratuple.getNegativeSamples(), algebra);
        SFA<BooleanFormula, LRATuple> res = SFA.MkPTA(lratuple.getPositiveSamples(), lratuple.getNegativeSamples(), algebra);

        for (List<LRATuple> e : lratuple.getPositiveSamples()) {
            assertTrue(format("The following sample should be accepted but is rejected: %s", e.toString()), res.accepts(e, algebra));
        }
        for (List<LRATuple> e : lratuple.getNegativeSamples()) {
            assertFalse(res.accepts(e, algebra));
        }

    }

    @Test
    public void TestRPNI() throws FileNotFoundException, InvalidConfigurationException, DeterminismViolationException, TimeoutException {
        LearningSamples<LRATuple> lratuple =  LearningSamples.readSamplesFromFile("../benchmarks/passiveLearning/ratTuple.txt", new LRATupleParser());
        //lratuple.printSamples();

        BooleanAlgebra<BooleanFormula, LRATuple> algebra = new RationalTupleCompAlgebra(2);

        SFA<BooleanFormula,LRATuple> res = RPNI.run(lratuple.getPositiveSamples(), lratuple.getNegativeSamples(), algebra);
        //res.createDotFile("RPNIEND", "/home/julian/");

        for (List<LRATuple> e : lratuple.getPositiveSamples()) {
            assertTrue(format("The following sample should be accepted but is rejected: %s", e), res.accepts(e, algebra));
        }

        for (List<LRATuple> e : lratuple.getNegativeSamples()) {
            assertFalse(format("The following sample should NOT be accepted but is accepted: %s", e), res.accepts(e, algebra));
        }
    }


    @Test
    public void TestRPNINegEx() throws FileNotFoundException, InvalidConfigurationException, DeterminismViolationException, TimeoutException {
        LearningSamples<LRATuple> lratuple =  LearningSamples.readSamplesFromFile("src/test/resources/RatTuples/rpniNegExp.txt", new LRATupleParser());
        //lratuple.printSamples();

        BooleanAlgebra<BooleanFormula, LRATuple> algebra = new RationalTupleCompAlgebra(2);

        SFA<BooleanFormula,LRATuple> res = RPNI.run(lratuple.getPositiveSamples(), lratuple.getNegativeSamples(), algebra);
        //res.createDotFile("RPNIEND", "/home/julian/");

        for (List<LRATuple> e : lratuple.getPositiveSamples()) {
            assertTrue(format("The following sample should be accepted but is rejected: %s", e), res.accepts(e, algebra));
        }

        for (List<LRATuple> e : lratuple.getNegativeSamples()) {
            assertFalse(format("The following sample should NOT be accepted but is accepted: %s", e), res.accepts(e, algebra));
        }
    }

    private static <A,B> void assertIsSameAutomaton(SFA<A,B> a, SFA<A,B> b) {
        // same states
        assertEquals(a.getStates(), b.getStates());

        //Same Transitions
        for(var s : a.getStates()) {
            Collection<Move<A, B>> trans = a.getMovesFrom(s);
            for (var t : trans) {
                assertTrue(b.getMovesFrom(s).contains(t));
            }
        }

        for(var s : b.getStates()) {
            Collection<Move<A, B>> trans = b.getMovesFrom(s);
            for (var t : trans) {
                assertTrue(a.getMovesFrom(s).contains(t));
            }
        }

        assertEquals(a.getFinalStates(), b.getFinalStates());
        assertEquals(a.getInitialState(), b.getInitialState());
    }

    @Test
    public void DeterminismTest() throws IOException, InvalidConfigurationException, DeterminismViolationException, TimeoutException {

        File folder = new File("src/test/resources/CharIntervalTuples");
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;

        int files = 0;

        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                files += 1;
                LearningSamples<Character[]> samples =  LearningSamples.readSamplesFromJsonFile(file.getPath(), new CharIntervalTupleParser());
                assertTrue(samples.getDimension().isPresent());
                BooleanAlgebra<CharTuplePred, Character[]> algebra = new CharIntervalTupleSolver(samples.getDimension().get());

                SFA<CharTuplePred, Character[]> res1 = RPNI.run(samples.getPositiveSamples(), samples.getNegativeSamples(), algebra);

                LearningSamples<Character[]> samples2 =  LearningSamples.readSamplesFromJsonFile(file.getPath(), new CharIntervalTupleParser());
                assertTrue(samples2.getDimension().isPresent());
                BooleanAlgebra<CharTuplePred, Character[]> algebra2 = new CharIntervalTupleSolver(samples2.getDimension().get());

                SFA<CharTuplePred, Character[]> res2 = RPNI.run(samples2.getPositiveSamples(), samples2.getNegativeSamples(), algebra2);

               assertIsSameAutomaton(res1, res2);

            }
        }
        assert(files > 0);


        folder = new File("src/test/resources/IntervalProblems");
        listOfFiles = folder.listFiles();
        assert listOfFiles != null;

        files = 0;

        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                files += 1;
                LearningSamples<Character> samples =  LearningSamples.readSamplesFromJsonFile(file.getPath(), new StringToUnicodeWordParser());
                BooleanAlgebra<CharPred, Character> algebra = new UnaryCharIntervalSolver();
                SFA<CharPred, Character> res = RPNI.run(samples.getPositiveSamples(), samples.getNegativeSamples(), algebra);

                LearningSamples<Character> samples1 =  LearningSamples.readSamplesFromJsonFile(file.getPath(), new StringToUnicodeWordParser());
                BooleanAlgebra<CharPred, Character> algebra1 = new UnaryCharIntervalSolver();
                SFA<CharPred, Character> res1 = RPNI.run(samples1.getPositiveSamples(), samples1.getNegativeSamples(), algebra1);

                assertIsSameAutomaton(res, res1);

            }
        }
        assert(files > 0);
    }



}
