package theory.predicates;


import scala.Tuple1;
import scala.util.parsing.combinator.PackratParsers;
import theory.BooleanAlgebra;
import theory.LRATuples.LRATuple;
import theory.LRATuples.LRATuplePred;
import theory.LRATuples.VarRat;
import theory.LRATuples.VarVar;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

public class SymbolicDecisionTree {


    int dimension;

    sealed private abstract class TreeADT permits Leaf, Tree {}

    final class Leaf extends TreeADT {
        Integer class_index = null;
    }

    final class Tree extends TreeADT {
        LRATuplePred pred;

        //List<Domain> data
        //feature_inded
        //feature_value
        //prediction_probability
        //information_gain

        TreeADT left;
        TreeADT right;
    }

    private record DataSplit (LRATuplePred predicate, List<List<LRATuple>> leftData, List<List<LRATuple>> rightData) {}


    TreeADT root = null;

    private SymbolicDecisionTree(int dim) {
        this.dimension = dim;
    }

    private long countNonEmptyClasses(List<List<LRATuple>> data) {
        return data.stream().filter((List<LRATuple> l) -> !l.isEmpty()).count();
    }

    private DataSplit splitData(List<List<LRATuple>> data, VarVar pred) {
        List<List<LRATuple>> left = new ArrayList<>();
        List<List<LRATuple>> right = new ArrayList<>();

        for (List<LRATuple> dt : data) {
            List<LRATuple> l = new ArrayList<>();
            List<LRATuple> r = new ArrayList<>();

            for (LRATuple d : dt) {
                if (d.tuple.get(pred.index_left()).compareTo(d.tuple.get(pred.index_right())) < 0) { //todo that < is what we want
                    l.add(d);
                } else {
                    r.add(d);
                }
            }

            left.add(l);
            right.add(r);
        }

        return new DataSplit(pred, left, right);
    }

    private DataSplit splitData(List<List<LRATuple>> data, VarRat pred) {
        List<List<LRATuple>> left = new ArrayList<>();
        List<List<LRATuple>> right = new ArrayList<>();

        for (List<LRATuple> dt : data) {
            List<LRATuple> l = new ArrayList<>();
            List<LRATuple> r = new ArrayList<>();

            for (LRATuple d : dt) {
                if (d.tuple.get(pred.index_left()).compareTo(pred.rat()) < 0) { //todo verify that < is what we want and not >
                    l.add(d);
                } else {
                    r.add(d);
                }
            }

            left.add(l);
            right.add(r);
        }

        return new DataSplit(pred, left, right);
    }

    private DataSplit splitData(List<List<LRATuple>> data, LRATuplePred pred) {
        return switch (pred) {
            case VarRat vr -> splitData(data, vr);
            case VarVar vv -> splitData(data, vv);
        };
    }

    private DataSplit findBestSplit(List<List<LRATuple>> data) {
        // check index splits of 'x < y'
        DataSplit resultSplit = null;
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                if (i == j) {
                    continue;
                }
                LRATuplePred p = new VarVar(i, j);
                DataSplit split = splitData(data, p);
                double gini = calculateInformationGain(split);
                if (gini > 0.0) { // todo
                    resultSplit = split;
                }
            }
        }

        return resultSplit;
    }

    private double calculateInformationGain(DataSplit split) {
        return 0.0;
    }

    private TreeADT createTree(List<List<LRATuple>> data) {
        // No stopping condition as we need perfect classification for now

        //We check if there are more than 2 element classes in the data
        if (countNonEmptyClasses(data) <= 1) {
            int idx = IntStream.range(0, data.size()).filter(i -> !data.get(i).isEmpty()).findFirst().getAsInt();
            Leaf res = new Leaf();
            res.class_index = idx;
            return res;
        }

        DataSplit split =  findBestSplit(data);

        Tree res = new Tree();
        res.left = createTree(split.leftData);
        res.right = createTree(split.rightData);
        res.pred = split.predicate;

        return res;
    }



    private void train(List<List<LRATuple>> values) {
        //todo
    }

    public LRATuplePred getDNFOfClassWithIndex(int classIdx) {
        //todo
        return null;
    }


    public static SymbolicDecisionTree buildTree(List<List<LRATuple>> values) {
        LRATuple el = values.getFirst().getFirst();
        return new SymbolicDecisionTree(el.tuple.size());
    }


}
