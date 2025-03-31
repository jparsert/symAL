package learning.sfa;


import automata.Move;
import automata.sfa.SFA;
import automata.sfa.SFAInputMove;
import automata.sfa.SFAMove;
import org.sat4j.specs.TimeoutException;
import theory.BooleanAlgebra;
import utilities.DeterminismViolationException;
import utilities.InvariantViolationException;
import utilities.Pair;

import java.util.*;
import java.util.stream.Collectors;

import static java.io.IO.print;
import static java.io.IO.println;

// RPNI
// Regular Positive and Negative Inference

// We follow the algorithm presented in Higuera "Grammatical Inference Learning Automata and Grammars" (2010)

public class RPNI<P,S> {

    private final Set<Integer> blueStates;
    private final Set<Integer> redStates;


    public RPNI() {
        blueStates = new HashSet<>();
        redStates = new HashSet<>();
    }


    public SFA<P,S> runRPNI(Collection<List<S>> positive, Collection<List<S>> negative, BooleanAlgebra<P, S> algebra) throws DeterminismViolationException, TimeoutException {

        SFA<P,S> A = SFA.MkPTA(positive, new ArrayList<>(), algebra);

        //A.createDotFile("B", "/home/julian/");

        redStates.add(A.getInitialState());
        // The book is lacking a definition for PREF(S+). The book initialises the blue states with the states reached by
        // intersection between alphabet and (probably) prefixes of S+.
        // If I understand correctly this is just the single step reachable states from the start state.
        blueStates.addAll(A.getTransitionsFrom(A.getInitialState()).stream().map((mv -> mv.to)).collect(Collectors.toSet()));

        while (!blueStates.isEmpty()) {
            int qb = choose(A, blueStates);

            Optional<Pair<Integer, SFA<P,S>>> compMerge = getCompatibleRedMerge(A, qb, negative, algebra);
            if (compMerge.isPresent()) {
                A = compMerge.get().second;
                for (int q : redStates) {
                    for (Move<P,S> move : A.getTransitionsFrom(q)) {
                        if (! redStates.contains(move.to)) {
                            blueStates.add(move.to);
                        }
                    }
                }
            } else {
                RPNIPromote(A, qb);
            }
            blueStates.remove(qb); // In book this is done right after choose() is called
        }
        return A;
    }

    private Optional<Pair<Integer, SFA<P,S>>> getCompatibleRedMerge(SFA<P,S> A, int qb, Collection<List<S>> negative, BooleanAlgebra<P, S> algebra) throws TimeoutException {
        for (int qr : redStates) {
            SFA<P,S> tmpA = (SFA<P, S>) A.clone();
            SFA<P,S> mergedA = RPNIMerge(tmpA, qr, qb, algebra);
            if (RPNICompatible(mergedA, negative, algebra)) {
                mergedA = SFA.removeDeadOrUnreachableStates(mergedA, algebra);
                return Optional.of(new Pair<>(qr, mergedA));
            }
        }

        return Optional.empty();
    }

    private int choose(SFA<P,S> posPTA, Set<Integer> set) {
        // some form of lex-length smallest element in set is probably required
        return set.iterator().next();
    }


    private void RPNIPromote(SFA<P,S> A, int state) {
        assert blueStates.contains(state);

        //add to red states
        redStates.add(state);

        // add all one step reachable states to blue states
        Collection<SFAMove<P, S>> moves = A.getTransitionsFrom(state);
        for(SFAMove<P,S> mv : moves) {
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

    public SFA<P,S> RPNIMerge(SFA<P,S> A, int q, int qPrime, BooleanAlgebra<P, S> ba) throws TimeoutException {
        //assert redStates.contains(q);
        //todo the following assertion is probably wrong in the book
        //assert blueStates.contains(qPrime);

        A = SFA.removeDeadOrUnreachableStates(A, ba);
        Collection<SFAMove<P,S>> moves =  A.getTransitions();
        //assert moves.size() == 1; // this should be unique because 'qPrime is/was blue and is therefore the root of a tree'
        for (SFAMove<P,S> mv : moves) {
            if (mv.to.equals(qPrime)) {
                A.bendMoveTarget(mv, q, ba);
                return RPNIFold(A,q,qPrime,ba);
            }
        }
        throw new InvariantViolationException("We could not find a parent node of " + qPrime + " in the moves during RPNI-MERGE! This should not happen!");
    }

    public SFA<P,S> RPNIFold(SFA<P,S> A, int q, int qPrime, BooleanAlgebra<P, S> ba) throws TimeoutException {
        assert A.getStates().contains(q);
        assert A.getStates().contains(qPrime);
        // qPrime should be the root of a tree

        if (A.isFinalState(qPrime)) {
            A.getFinalStates().add(q);
        }

        //todo this is wrong, we need to update the list continuously
        // todo redesign algorithm
        Collection<SFAMove<P,S>> workList = A.getTransitionsFrom(qPrime);
        while(!workList.isEmpty()) {
            var dAqP = workList.iterator().next();// transition that has q' as parent
            assert dAqP.from == qPrime;

            if (dAqP instanceof SFAInputMove<P, S> dAqPmv) { // ensure correct type when casting
                boolean change = false; // flag if it is already defined or not for the symbol. This should 'almost' never ocur for SFAs
                for(SFAMove<P, S> mvq : A.getTransitionsFrom(q)) {
                    SFAInputMove<P,S> daq = (SFAInputMove<P, S>) mvq;
                    if (daq.guard.equals(dAqPmv.guard)) { // this will rarely occur in SFAs
                        A = RPNIFold(A, daq.to, dAqPmv.to, ba);
                        change = true;
                    }
                }
                if (!change) {
                    A.bendMoveSource(dAqPmv, q, ba);
                }
                workList = A.getTransitionsFrom(qPrime);
                workList.remove(dAqPmv);
            } else {
                throw new InvariantViolationException("We should be dealing exclusively with SFAInputMove. But here we have " + dAqP.getClass());
            }

        }
        return A;
    }

}
