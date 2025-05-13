package learning.sfa;


import automata.Move;
import automata.sfa.SFA;
import automata.sfa.SFAInputMove;
import automata.sfa.SFAMove;
import org.sat4j.specs.TimeoutException;
import theory.BooleanAlgebra;

import utilities.Pair;

import java.util.*;

/**
 * Algorithms for generalising automata predicates
 * This can be seen as post-processing, e.g. adding dead sates to make an automata complete,
 * @param <P>
 * @param <S>
 */

public class GenAutomata<P,S> {



    /**
    *  For now, this only works if the set of witnesses for a transition is a singleton set (i.e. unique).
     *  e.g. if the transitions is something like x = 5 instead of x > 5
     *  We fail, if a transition is FALSE
     *  TODO, potentially move this as a method to SFA class
    * */
    public static <P,S>  SFA<P,S> generaliseTransitions(SFA<P, S> sfa, BooleanAlgebra<P, S> ba) throws TimeoutException {

        sfa = sfa.mkTotal(ba);

        // the following does not change anything (yet) since it's already a total automaton.

        for(var out : sfa.getStates()) {
            Map<Pair<Integer,Integer>, List<P>> map = new HashMap<>();
            for (var mv : sfa.getMovesFrom(out)) {
                if (Objects.requireNonNull(mv) instanceof SFAInputMove<P, S> imv) {
                    Pair<Integer, Integer> key = new Pair<>(mv.from, mv.to);
                    if (map.containsKey(key)) {
                        map.get(key).add(imv.guard);
                    } else {
                        var newList = new ArrayList<P>();
                        newList.add(imv.guard);
                        map.put(key, newList);
                    }
                    // careful, we now destroy something
                    sfa.removeTransition(imv);
                } else {
                    throw new IllegalStateException("Move should be an instance of SFAInputMove, but is an instance of " + mv.getClass().getSimpleName());
                }

            }
            List<Pair<Integer,Integer>> keys = new ArrayList<>();
            ArrayList<Collection<P>> vals = new ArrayList<>();
            for (var entry: map.entrySet()) {
                keys.add(entry.getKey());
                vals.add(entry.getValue());
            }

            ArrayList<P> preds = ba.GetSeparatingPredicatesFromPredicates(vals, Long.MAX_VALUE);

            for (int i = 0; i < keys.size(); i++) {
                SFAInputMove<P,S> mv = new SFAInputMove<>(keys.get(i).first, keys.get(i).second, preds.get(i));
                sfa.addTransition(mv,ba,false);
            }
        }
        return sfa;
    }

}
