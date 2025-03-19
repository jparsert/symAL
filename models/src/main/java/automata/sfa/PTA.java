package automata.sfa;


import org.sat4j.specs.TimeoutException;
import theory.BooleanAlgebra;

import java.util.Collection;
import java.util.List;

/**
 * Prefix Tree Acceptor
 *
 * technically this might implement it such that it extends Automaton. But for now, we'll just create a static function
 * that builds a PTA
 */
public class PTA {


    /**
     * Build a PTA  from positive and negative samples of sequences of elements in the domain
     *
     * @param
     * 			<P>
     *            set of predicates over the domain S
     * @param <S>
     *            domain of the automaton alphabet
     */
    public static <P,S> SFA<P,S> buildPTA(Collection<List<S>> positive, Collection<List<S>> negative, BooleanAlgebra<P,S> algebra) throws TimeoutException {
        SFA<P,S> automaton = SFA.getEmptySFA(algebra);


        return automaton;
    }

}
