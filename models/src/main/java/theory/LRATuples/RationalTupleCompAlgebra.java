package theory.LRATuples;

import com.google.common.collect.ImmutableList;
import jdk.jshell.spi.ExecutionControl;
import org.sat4j.specs.TimeoutException;
import org.sosy_lab.common.rationals.Rational;
import org.sosy_lab.java_smt.api.*;
import theory.BooleanAlgebra;
import utilities.Pair;


import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.java_smt.*;
import org.sosy_lab.common.log.BasicLogManager;
import org.sosy_lab.common.log.LogManager;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;



public class RationalTupleCompAlgebra extends BooleanAlgebra<BooleanFormula, LRATuple> {

    Configuration config = Configuration.defaultConfiguration();
    LogManager logger = BasicLogManager.create(config);
    ShutdownNotifier notifier = ShutdownNotifier.createDummy();
    SolverContext context =
            SolverContextFactory.createSolverContext(config, logger, notifier, SolverContextFactory.Solvers.Z3);

    BooleanFormulaManager boolMgr = context.getFormulaManager().getBooleanFormulaManager();
    RationalFormulaManager ratMgr = context.getFormulaManager().getRationalFormulaManager();



    //variables i.e. index of dimension
    NumeralFormula.RationalFormula[] variableVector;


    public RationalTupleCompAlgebra(int dimension) throws InvalidConfigurationException {
        // init var dimension
        variableVector = new NumeralFormula.RationalFormula[dimension];
        for (int i = 0; i < dimension; i++) {
            variableVector[i] = ratMgr.makeVariable("x" + i);
        }

    }

    @Override
    public BooleanFormula MkAtom(LRATuple lraTuple) {
        if(lraTuple.tuple.size() != variableVector.length) {
            throw new RuntimeException("lraTuple has Wrong dimension.");
        }

        List<BooleanFormula> e = new ArrayList<>();
        for (int i = 0; i < lraTuple.tuple.size(); i++) {
            e.add(ratMgr.equal(ratMgr.makeNumber(lraTuple.tuple.get(i)), variableVector[i]));
        }
        
        return boolMgr.and(e);
    }

    @Override
    public BooleanFormula MkNot(BooleanFormula booleanFormula) throws TimeoutException {
        return boolMgr.not(booleanFormula);
    }

    @Override
    public BooleanFormula MkOr(Collection<BooleanFormula> pset) throws TimeoutException {
        return boolMgr.or(pset);
    }

    @Override
    public BooleanFormula MkOr(BooleanFormula p1, BooleanFormula p2) throws TimeoutException {
        return boolMgr.or(p1, p2);
    }

    @Override
    public BooleanFormula MkAnd(Collection<BooleanFormula> pset) throws TimeoutException {
        return boolMgr.and(pset);
    }

    @Override
    public BooleanFormula MkAnd(BooleanFormula p1, BooleanFormula p2) throws TimeoutException {
        return boolMgr.and(p1, p2);
    }

    @Override
    public BooleanFormula True() {
        return boolMgr.makeTrue();
    }

    @Override
    public BooleanFormula False() {
        return boolMgr.makeFalse();
    }

    @Override
    public boolean AreEquivalent(BooleanFormula p1, BooleanFormula p2) throws TimeoutException {
        throw new TimeoutException("AreEquiv not implemented");
    }

    @Override
    public boolean IsSatisfiable(BooleanFormula p1) throws TimeoutException {
        ProverEnvironment x = this.context.newProverEnvironment();
        try {
            x.addConstraint(p1);
            return !x.isUnsat();
        } catch (InterruptedException | SolverException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean HasModel(BooleanFormula p1, LRATuple el) throws TimeoutException {
        BooleanFormula f = this.MkAtom(el);
        ProverEnvironment x = this.context.newProverEnvironment();
        try {
            x.addConstraint(p1);
            x.addConstraint(f);
            return !x.isUnsat();
        } catch (InterruptedException | SolverException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean HasModel(BooleanFormula p1, LRATuple el1, LRATuple el2) throws TimeoutException {
        throw new TimeoutException("HasModel for binary Predicate not Implemented");
    }

    @Override
    public LRATuple generateWitness(BooleanFormula p1) throws TimeoutException {
        ProverEnvironment x = this.context.newProverEnvironment(SolverContext.ProverOptions.GENERATE_MODELS);
        try {
            x.addConstraint(p1);
            if (x.isUnsat())  {
                return null;
            }

            List<Rational> res = new ArrayList<>();
            ImmutableList<Model.ValueAssignment> m = x.getModelAssignments();
            for(Model.ValueAssignment v : m) {
                System.out.println(v);
                // res.add(v); todo
            }

            //TODO return correct tuple
            //return new LRATuple(res);

        } catch (InterruptedException | SolverException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public Pair<LRATuple, LRATuple> generateWitnesses(BooleanFormula p1) throws TimeoutException {
        throw new TimeoutException("HasModel for binary Predicate not Implemented");
    }
}
