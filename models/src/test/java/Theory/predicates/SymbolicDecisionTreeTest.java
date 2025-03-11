package Theory.predicates;


import org.junit.Test;
import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.BasicLogManager;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.api.*;
import theory.LRATuples.LRATuple;
import theory.predicates.SymbolicDecisionTree;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class SymbolicDecisionTreeTest {

    @Test
    public void basicTest() throws InvalidConfigurationException, InterruptedException, SolverException {

        Configuration config = Configuration.defaultConfiguration();
        LogManager logger = BasicLogManager.create(config);
        ShutdownNotifier notifier = ShutdownNotifier.createDummy();
        SolverContext context =
                SolverContextFactory.createSolverContext(config, logger, notifier, SolverContextFactory.Solvers.Z3);

        BooleanFormulaManager boolMgr = context.getFormulaManager().getBooleanFormulaManager();
        RationalFormulaManager ratMgr = context.getFormulaManager().getRationalFormulaManager();


        List<LRATuple> features = Arrays.asList(
                new LRATuple(new Long[]{1L, 2L}) ,
                new LRATuple(new Long[]{2L, 3L}),
                new LRATuple(new Long[]{4L, 3L}),
                new LRATuple(new Long[]{4L, 2L}),
                new LRATuple(new Long[]{5L, 6L}),
                new LRATuple(new Long[]{74L, 69L})
        );
        List<Integer> labels = Arrays.asList(0, 0, 1, 1,0,0);

        SymbolicDecisionTree tree = SymbolicDecisionTree.buildByIterativeDeepening(features, labels);

        for (LRATuple feature : features) {
            assertEquals(tree.classify(feature), tree.classify(feature));
        }

        NumeralFormula.RationalFormula[] variables = new NumeralFormula.RationalFormula[]{ratMgr.makeVariable("x0"), ratMgr.makeVariable("x1")};

        BooleanFormula fm1 = tree.getDNFForLabel(0,variables,boolMgr,ratMgr);
        BooleanFormula fm2 = tree.getDNFForLabel(1,variables,boolMgr,ratMgr);

        BooleanFormula fm = boolMgr.or(fm1, fm2);

        ProverEnvironment prover = context.newProverEnvironment();
        prover.push();
        prover.addConstraint(fm);
        assertFalse(prover.isUnsat());
        prover.pop();
        prover.addConstraint(boolMgr.not(fm));
        assertTrue(prover.isUnsat());
    }


}


