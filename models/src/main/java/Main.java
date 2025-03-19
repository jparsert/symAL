import automata.sfa.SFA;
import org.sat4j.specs.TimeoutException;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.rationals.Rational;
import org.sosy_lab.java_smt.api.BooleanFormula;
import scala.Int;
import theory.LRATuples.LRATuple;
import theory.LRATuples.RationalTupleCompAlgebra;
import utilities.*;


import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static automata.sfa.SFA.MkPTA;


public class Main {


    public static void main(String[] args) throws TimeoutException, InvalidConfigurationException, FileNotFoundException, DeterminismViolationException {

        SFA<BooleanFormula, LRATuple> a = SFA.getEmptySFA(new RationalTupleCompAlgebra(5));

        List<List<LRATuple>> l = new ArrayList<>();
        l.add(new ArrayList<>());
        //SymbolicDecisionTree decTree = SymbolicDecisionTree.buildTree(l);


        List<Integer> i = new ArrayList<>();
        i.add(11);
        i.add(12);

        PosNegSamples<Integer, IntParser> samples =  PosNegSamples.readSamplesfromFile("../benchmarks/passiveLearning/test.txt", new IntParser());
        //samples.printSamples();

        PosNegSamples<Rational, RationalParser> ratsamp =  PosNegSamples.readSamplesfromFile("../benchmarks/passiveLearning/ratTest.txt", new RationalParser());
        //ratsamp.printSamples();

        PosNegSamples<LRATuple, LRATupleParser> lratuple =  PosNegSamples.readSamplesfromFile("../benchmarks/passiveLearning/ratTuple.txt", new LRATupleParser());
        //lratuple.printSamples();

        SFA<BooleanFormula,LRATuple> x = SFA.MkPTA(lratuple.getPositiveSamples(), lratuple.getNegativeSamples(), new RationalTupleCompAlgebra(2));
        x.createDotFile("ASD", "/home/julian/");
    }
}
