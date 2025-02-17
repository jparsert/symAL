package theory.LRATuples;

import com.google.common.collect.ImmutableList;
import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.java_smt.*;
import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.SolverContext;
import org.sosy_lab.common.log.BasicLogManager;
import org.sosy_lab.common.log.LogManager;

import org.sosy_lab.common.rationals.Rational;

import java.util.Arrays;
import java.util.List;

/**
 * Type of LRA tuple. We will let the underlying type be generic to allow for Reals and rationals in case they
 * are treated differently. Also we may rename it later to also allow for other types.
 */
public class LRATuple  {

    //maybe immutable list is better
    public final ImmutableList<Rational> tuple;

    public LRATuple(ImmutableList<Rational> tuple) {
        this.tuple = tuple;
    }

    public LRATuple(List<Rational> lst) {
        this.tuple = ImmutableList.copyOf(lst);
    }

    public LRATuple(int n) {
        Rational[] tuple = new Rational[n];
        Arrays.fill(tuple, Rational.ZERO);
        this.tuple = ImmutableList.copyOf(tuple);

    }


}
