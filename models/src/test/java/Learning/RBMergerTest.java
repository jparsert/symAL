package Learning;

import automata.sfa.SFA;
import learning.sfa.RBMerging;
import org.junit.Test;
import org.sat4j.specs.TimeoutException;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.java_smt.api.BooleanFormula;
import theory.BooleanAlgebra;
import theory.LRATuples.LRATuple;
import theory.LRATuples.RationalTupleCompAlgebra;
import utilities.DeterminismViolationException;
import utilities.LRATupleParser;
import utilities.PosNegSamples;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import static java.lang.String.format;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RBMergerTest {

    @Test
    public void RBMergerAllRatTuplesInResources() throws FileNotFoundException, InvalidConfigurationException, DeterminismViolationException, TimeoutException {
        File folder = new File("src/test/resources/RatTuples");
        File[] listOfFiles = folder.listFiles();

        assert listOfFiles != null;
        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(".txt")) {
                PosNegSamples<LRATuple, LRATupleParser> lratuple =  PosNegSamples.readSamplesfromFile(file.getPath(), new LRATupleParser());

                BooleanAlgebra<BooleanFormula, LRATuple> algebra = new RationalTupleCompAlgebra(2);


                SFA<BooleanFormula,LRATuple> res = RBMerging.run(lratuple.getPositiveSamples(), lratuple.getNegativeSamples(), algebra);
                res.createDotFile("ASD", "/home/julian/");

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
