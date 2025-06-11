package learning.sfa;

import automata.sfa.SFA;
import org.sat4j.specs.TimeoutException;
import theory.BooleanAlgebra;
import utilities.LearningSamples;
import utilities.Pair;
import utilities.exceptions.DeterminismViolationException;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;


/**
 * We implement different versions of RPNI that also consider implication/inductive examples
 * @param <P>
 * @param <S>
 */
public class RPNIInd<P,S> {


    /**
     * Algorithm that calls RPNI as a subroutine and computes a fixed point
     */
    public static <P, S> SFA<P, S> RPNIInductiveFixedPoint(LearningSamples<S> samples, BooleanAlgebra<P, S> algebra)
            throws DeterminismViolationException, TimeoutException {

        List<List<S>> pos = samples.getPositiveSamples();
        List<List<S>> neg = samples.getNegativeSamples();
        Collection<Pair<List<S>,List<S>>> ind = samples.getImplicationSamples();

        SFA<P,S> res = null;
        boolean flag = true;
        while(flag) {
            res = RPNI.run(pos,neg, algebra);
            Collection<Pair<List<S>,List<S>>> tmp = new HashSet<>();
            flag = false;
            for(Pair<List<S>,List<S>> pair : ind) {
                if (res.accepts(pair.first, algebra) && !neg.contains(pair.second)) {
                    flag = true;
                    pos.add(pair.first);
                    pos.add(pair.second);
                } else if (neg.contains(pair.second)) {
                    flag = true;
                    neg.add(pair.first);
                } else {
                    tmp.add(pair);
                }

            }
            ind = tmp;
        }

        return res;
    }
}