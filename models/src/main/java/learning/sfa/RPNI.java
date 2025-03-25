package learning.sfa;


import automata.Move;
import automata.sfa.SFA;
import automata.sfa.SFAInputMove;
import org.sat4j.specs.TimeoutException;
import theory.BooleanAlgebra;
import utilities.DeterminismViolationException;
import utilities.InvariantViolationException;
import utilities.Pair;

import java.util.*;
import java.util.stream.Collectors;

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
        redStates.add(A.getInitialState());
        // The book is lacking a definition for PREF(S+). The book initialises the blue states with the states reached by
        // intersection between alphabet and (probably) prefixes of S+.
        // If I understand correctly this is just the single step reachable states from the start state.
        blueStates.addAll(A.getMovesFrom(A.getInitialState()).stream().map((mv -> mv.to)).collect(Collectors.toSet()));


        while (!blueStates.isEmpty()) {
            int qb = choose(A, blueStates);
            blueStates.remove(qb);

            Optional<Pair<Integer, SFA<P,S>>> compMerge = getCompatibleRedMerge(A, qb, negative, algebra);
            if (compMerge.isPresent()) {
                A = compMerge.get().second;
                for (int q : redStates) {
                    for (Move<P,S> move : A.getMovesFrom(q)) {
                        if (! redStates.contains(move.to)) {
                            blueStates.add(move.to);
                        }
                    }
                }

            } else {
                RPNIPromote(A, qb);
            }

        }
        return A;
    }

    private Optional<Pair<Integer, SFA<P,S>>> getCompatibleRedMerge(SFA<P,S> A, int qb, Collection<List<S>> negative, BooleanAlgebra<P, S> algebra) throws TimeoutException {
        for (int qr : redStates) {
            SFA<P,S> tmpA = RPNIMerge(A,qr, qb, algebra);
            if (RPNICompatible(tmpA, negative, algebra)) {
                return Optional.of(new Pair<>(qr, tmpA));
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
        //todo the following assertion is probably wrong in the book
        //assert blueStates.contains(qPrime);

        Collection<Move<P,S>> moves =  A.getMovesTo(qPrime);
        for (Move<P,S> mv : moves) {
            if (mv.to.equals(qPrime)) {
                A.getMoves().remove(mv);
                mv.to = q;
                A.getMoves().add(mv);
                return RPNIFold(A,q,qPrime,ba);
            }
        }
        A.createDotFile("ASD", "/home/julian/");
        throw new InvariantViolationException("We could not find a parent node of qPrime in the moves during RPNI-MERGE! This should not happen!");
    }

    private SFA<P,S> RPNIFold(SFA<P,S> A, int q, int qPrime, BooleanAlgebra<P, S> ba) {
        assert A.getStates().contains(q);
        assert A.getStates().contains(qPrime);
        // qPrime should be the root of a tree

        if (A.isFinalState(qPrime)) {
            A.getFinalStates().add(q);
        }

        for(Move<P, S> mvqP : A.getMovesFrom(qPrime)) {
            if (mvqP instanceof SFAInputMove<P, S> inputMoveqP) {
                for(Move<P, S> mvq : A.getMovesFrom(q)) {
                    SFAInputMove<P,S> inputMoveq = (SFAInputMove<P, S>) mvq;
                    if (inputMoveq.guard.equals(inputMoveqP.guard)) {
                        A = RPNIFold(A, inputMoveq.to, inputMoveqP.to, ba);
                    } else {
                        A.getMoves().remove(inputMoveq);
                        inputMoveq.to = inputMoveqP.to;
                        A.getMoves().add(inputMoveq);
                    }
                }
            } else {
                throw new InvariantViolationException("We should be dealing exclusively with SFAInputMove. But here we have something else.");
            }
        }
        return A;
    }

}
