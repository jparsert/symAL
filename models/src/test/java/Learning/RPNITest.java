package Learning;

import java.io.FileNotFoundException;
import java.util.List;

import automata.sfa.SFA;
import learning.sfa.RPNI;
import org.junit.Test;
import org.sat4j.specs.TimeoutException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.java_smt.api.BooleanFormula;
import theory.BooleanAlgebra;
import theory.LRATuples.LRATuple;
import theory.LRATuples.RationalTupleCompAlgebra;
import utilities.DeterminismViolationException;
import utilities.LRATupleParser;
import utilities.PosNegSamples;


public class RPNITest {

    @Test
    public void TestRPNI() throws FileNotFoundException, InvalidConfigurationException, DeterminismViolationException, TimeoutException {
        PosNegSamples<LRATuple, LRATupleParser> lratuple =  PosNegSamples.readSamplesfromFile("../benchmarks/passiveLearning/ratTuple.txt", new LRATupleParser());
        //lratuple.printSamples();

        BooleanAlgebra<BooleanFormula, LRATuple> algebra = new RationalTupleCompAlgebra(2);

        RPNI<BooleanFormula, LRATuple> rpni = new RPNI<>();

        SFA<BooleanFormula,LRATuple> res = rpni.runRPNI(lratuple.getPositiveSamples(), lratuple.getNegativeSamples(), algebra);
        res.createDotFile("ASD", "/home/julian/");

        for (List<LRATuple> e : lratuple.getPositiveSamples()) {
            assertTrue(res.accepts(e, algebra));
        }

        // not implemented yet
        //for (List<LRATuple> e : lratuple.getPositiveSamples()) {
        //    assertFalse(res.accepts(e, algebra));
        //}
    }
}
