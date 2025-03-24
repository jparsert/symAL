package learning.sfa;


import automata.Move;
import automata.sfa.SFA;
import org.sat4j.specs.TimeoutException;
import org.sosy_lab.java_smt.api.BooleanFormula;
import theory.BooleanAlgebra;
import theory.LRATuples.LRATuple;
import theory.LRATuples.RationalTupleCompAlgebra;
import utilities.DeterminismViolationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// RPNI
// Regular Positive and Negative Inference

// We follow the algorithm presented in Higuera "Grammatical Inference Learning Automata and Grammars" (2010)

public class RPNI<P,S> {

    private List<Integer> blueStates;
    private List<Integer> redStates;


    private RPNI() {
        blueStates = new ArrayList<>();
        redStates = new ArrayList<>();
    }


    public static <P,S> SFA<P,S> MakeRPNI(Collection<List<S>> positive, Collection<List<S>> negative, BooleanAlgebra<P, S> algebra) throws DeterminismViolationException, TimeoutException {
        SFA<P,S> result = SFA.MkPTA(positive, new ArrayList<>(), algebra);


        return result;
    }


    private void RPNIPromote(SFA<P,S> A, int state) {
        assert blueStates.contains(state);

        //add to red states
        redStates.add(state);

        // add all one step reachable states to blue states
        Collection<Move<P, S>> moves = A.getMovesFrom(state);
        for(Move<P,S> mv : moves) {
            blueStates.add(mv.to);
        }
    }

    private boolean RPNICompatible(SFA<P,S> A, Collection<List<S>> negative, BooleanAlgebra<P, S> ba) throws TimeoutException {
        for (List<S> word : negative) {
            if (A.accepts(word,ba)) {
                return false;
            }
        }
        return true;
    }

    private SFA<P,S> RPNIMerge(SFA<P,S> A, int q, int qPrime, BooleanAlgebra<P, S> ba) throws TimeoutException {
        assert redStates.contains(q);
        assert blueStates.contains(qPrime);

        //todo
        return null;
    }

    private SFA<P,S> RPNIFold(SFA<P,S> A, int q, int qPrime, BooleanAlgebra<P, S> ba) {
        assert A.getStates().contains(q);
        assert A.getStates().contains(qPrime);
        // qPrime should be the root of a tree

        //todo
        return null;
    }

}
