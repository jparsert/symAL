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

        for(var out : sfa.getStates()) {
            Map<Pair<Integer,Integer>, List<S>> map = new HashMap<Pair<Integer,Integer>, List<S>>();
            for (var mv : sfa.getTransitionsFrom(out)) {
                S witness = mv.getWitness(ba);
                Pair<Integer,Integer> key = new Pair<>(mv.from, mv.to);
                if (map.containsKey(key)) {
                    map.get(key).add(witness);
                } else {
                    var newList = new ArrayList<S>();
                    newList.add(witness);
                    map.put(key, newList);
                }
                // careful, we now destroy something
                sfa.removeTransition(mv);
            }
            List<Pair<Integer,Integer>> keys = new ArrayList<>();
            ArrayList<Collection<S>> vals = new ArrayList<>();
            for (var entry: map.entrySet()) {
                keys.add(entry.getKey());
                vals.add(entry.getValue());
            }

            ArrayList<P> preds = ba.GetSeparatingPredicates(vals, Long.MAX_VALUE);

            for (int i = 0; i < keys.size(); i++) {
                SFAInputMove<P,S> mv = new SFAInputMove<>(keys.get(i).first, keys.get(i).second, preds.get(i));
                sfa.addTransition(mv,ba,false);
            }
        }
        return sfa;
    }

}
