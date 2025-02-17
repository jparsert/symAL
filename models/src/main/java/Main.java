import automata.sfa.SFA;
import org.sat4j.specs.TimeoutException;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.java_smt.api.BooleanFormula;
import theory.LRATuples.LRATuple;
import theory.LRATuples.LRATuplePred;
import theory.intervals.RealPred;
import theory.intervals.RealSolver;
import theory.LRATuples.RationalTupleCompAlgebra;
import theory.predicates.SymbolicDecisionTree;


import java.util.List;


public class Main {


    public static void main(String[] args) throws TimeoutException, InvalidConfigurationException {

        SFA<BooleanFormula, LRATuple> a = SFA.getEmptySFA(new RationalTupleCompAlgebra(5));

        List<List<LRATuple>> l = null;
        SymbolicDecisionTree<LRATuplePred, LRATuple> decTree = SymbolicDecisionTree.buildTree(l);

    }
}
