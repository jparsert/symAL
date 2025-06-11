package learning.sfa;

import automata.sfa.SFA;
import automata.sfa.SFAInputMove;
import automata.sfa.SFAMove;
import org.sat4j.specs.TimeoutException;
import theory.BooleanAlgebra;
import utilities.exceptions.DeterminismViolationException;
import utilities.exceptions.InvariantViolationException;
import utilities.Pair;

import java.util.*;
import java.util.stream.Collectors;


public class EDSM<P,S>  {

    private final Set<Integer> blueStates;
    private final Set<Integer> redStates;


    private EDSM() {
        blueStates = new HashSet<>();
        redStates = new HashSet<>();
    }

    public static <P,S> SFA<P,S> run(Collection<List<S>> positive, Collection<List<S>> negative, BooleanAlgebra<P, S> algebra)
            throws DeterminismViolationException, TimeoutException {

        for(List<S> e : negative) {
            if(positive.contains(e)) {
                throw new RuntimeException("Negative and Positive Set are not disjoint! The following are in both sets: " + e);
            }
        }

        EDSM<P,S> syntesiser = new EDSM<P,S>();
        return syntesiser.execute(positive, negative, algebra);
    }

    private SFA<P,S> execute(Collection<List<S>> positive, Collection<List<S>> negative, BooleanAlgebra<P, S> algebra) throws DeterminismViolationException, TimeoutException {

        SFA<P, S> A = SFA.MkPTA(positive, negative, algebra);
        redStates.add(A.getInitialState());

        // todo this can also be optimised to only include the states that can reach an accepting state
        blueStates.addAll(A.getTransitionsFrom(A.getInitialState()).stream().map((mv -> mv.to)).collect(Collectors.toSet()));

        while (!blueStates.isEmpty()) {
            boolean promotion = false;
            Integer qbHat = null;
            Integer qrHat = null;

            Collection<Integer> tmpBlueStates = blueStates.stream().toList();
            for (Integer qb :tmpBlueStates) {
                if (!promotion) {
                    Optional<Integer> bs = Optional.empty();
                    boolean atLeastOneMerge = false;
                    for (Integer qr : redStates) {
                        SFA<P,S> tmpA = (SFA<P, S>) A.clone();
                        tmpA = EDSMMerge(tmpA, qr, qb, algebra);
                        Optional<Integer> s = EDSMCount(tmpA, positive, negative, algebra);
                        if (extendedGT(s, Optional.empty())) {
                            atLeastOneMerge = true;
                        }
                        if (extendedGT(s, bs)) {
                            bs = s;
                            qrHat = qr;
                            qbHat = qb;
                        }
                    }
                    if (!atLeastOneMerge) {
                        promote(A, qb);
                        promotion = true;
                    }
                }
            }

            if (!promotion) {
                assert qbHat != null;
                assert qrHat != null;

                blueStates.remove(qbHat);
                A = EDSMMerge(A, qrHat, qbHat, algebra);
            }
        }
        for (List<S> word : positive) {
            Pair<Collection<Integer>, List<S>> res = A.consumeLongestPrefix(word, algebra);
            assert res.getFirst().size() == 1;
            A.getFinalStates().add(res.getFirst().iterator().next());
        }

        for (List<S> word : negative) {
            Pair<Collection<Integer>, List<S>> res = A.consumeLongestPrefix(word, algebra);
            assert !A.getFinalStates().contains(res.first.iterator().next());
        }

        A = SFA.removeDeadOrUnreachableStates(A, algebra);

        return A;
    }

    //exnteded greater than a > b
    static boolean extendedGT(Optional<Integer> a, Optional<Integer> b) {
        if (a.isPresent() && b.isPresent()) {
            return a.get() > b.get();
        }
        if (b.isEmpty()) {
            return a.isPresent();
        }
        return false;
    }


    private void promote(SFA<P,S> A, int state) {
        assert blueStates.contains(state);
        //add to red states
        redStates.add(state);

        // this line is not described in the book, but *I think* that's a mistake as blue and red sets should be disjoint
        blueStates.remove(state);

        // add all one step reachable states to blue states
        Collection<SFAMove<P, S>> moves = A.getTransitionsFrom(state);
        for(SFAMove<P,S> mv : moves) {
            if (!redStates.contains(mv.to)) {
                blueStates.add(mv.to);
            }
        }

    }

    private Optional<Integer> EDSMCount(SFA<P,S> A, Collection<List<S>> positive, Collection<List<S>> negative, BooleanAlgebra<P,S> algebra) throws TimeoutException {
        // calculate initial scores
        Map<Integer,Integer> tp = new HashMap<>();
        Map<Integer,Integer> tn = new HashMap<>();
        // could be omitted with a better data structure
        for (int q : A.getStates()) {
            tp.put(q,0);
            tn.put(q,0);
        }
        for (List<S> word : positive) {
            Pair<Collection<Integer>, List<S>> res = A.consumeLongestPrefix(word, algebra);
            if (res.first.size() != 1 || !res.second.isEmpty()) {
                throw new InvariantViolationException("This means A is either nondeterministic or not complete.");
            }
            int q = res.first.iterator().next();
            assert A.getFinalStates().contains(q);
            tp.put(q, tp.get(q) + 1);
        }
        for (List<S> word : negative) {
            Pair<Collection<Integer>, List<S>> res = A.consumeLongestPrefix(word, algebra);
            if (res.first.size() != 1) {
                throw new InvariantViolationException("This means A is either nondeterministic or not complete. " +
                        "Error occurred when reading the rejecting negative sample: " + word + " leads to states: " + res.first + ".");
            }
            int q = res.first.iterator().next();
            //assert A.getNonFinalStates().contains(q);
            tn.put(q, tn.get(q) + 1);
        }

        //calculate score
        Optional<Integer> score = Optional.of(0);
        for (Integer q : A.getStates()) {
            if (score.isPresent()){
                if (tn.get(q) > 0) {
                    if (tp.get(q) > 0) {
                        score = Optional.empty();
                    } else {
                        score = Optional.of(score.get() + tn.get(q) - 1);
                    }
                } else {
                    if (tp.get(q) > 0) {
                        score = Optional.of(score.get() + tp.get(q) - 1);
                    }
                }
            }
        }
        return score;
    }

    private SFA<P,S> EDSMMerge(SFA<P,S> A, int q, int qPrime, BooleanAlgebra<P, S> ba) throws TimeoutException {
        assert redStates.contains(q);
        //todo the following assertion is probably wrong in the book
        //assert blueStates.contains(qPrime);

        A = SFA.removeDeadOrUnreachableStates(A, ba);
        Collection<SFAMove<P,S>> moves =  A.getTransitionsTo(qPrime);

        //assert moves.size() == 1; // this should be unique because 'qPrime is/was blue and is therefore the root of a tree'
        if(moves.isEmpty()) {
            return A;
        }
        SFAMove<P,S> mv = moves.iterator().next();
        assert mv.to.equals(qPrime); // given that we use getTransitionsTo, this should always be true
        A.bendMoveTarget(mv, q, ba);
        return EDSMFold(A,q,qPrime,ba);
    }

    private SFA<P,S> EDSMFold(SFA<P,S> A, int q, int qPrime, BooleanAlgebra<P, S> ba) throws TimeoutException {
        assert A.getStates().contains(q);
        assert A.getStates().contains(qPrime);
        // qPrime should be the root of a tree

        if (A.isFinalState(qPrime)) {
            A.getFinalStates().add(q);
        }

        Collection<SFAMove<P,S>> workList = A.getTransitionsFrom(qPrime);
        while(!workList.isEmpty()) {
            var dAqP = workList.iterator().next();// transition that has q' as parent
            assert dAqP.from == qPrime;

            if (dAqP instanceof SFAInputMove<P, S> dAqPmv) { // ensure correct type when casting
                boolean change = false; // flag if it is already defined or not for the symbol. This should 'almost' never ocur for SFAs
                for(SFAMove<P, S> mvq : A.getTransitionsFrom(q)) {
                    SFAInputMove<P,S> daq = (SFAInputMove<P, S>) mvq;
                    if (daq.guard.equals(dAqPmv.guard)) { // this will rarely occur in SFAs
                        A = EDSMFold(A, daq.to, dAqPmv.to, ba);
                        change = true;
                    }
                }
                if (!change) {
                    A.bendMoveSource(dAqPmv, q, ba);
                }
                workList.remove(dAqP);
            } else {
                throw new InvariantViolationException("We should be dealing exclusively with SFAInputMove. But here we have " + dAqP.getClass());
            }

        }
        return A;
    }

}
