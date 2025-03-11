package theory.predicates;

import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.BasicLogManager;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.common.rationals.Rational;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.api.*;
import theory.LRATuples.LRATuple;
import theory.LRATuples.LRATuplePred;
import theory.LRATuples.VarRat;
import theory.LRATuples.VarVar;

import java.util.*;
import java.util.stream.Stream;

public class SymbolicDecisionTree {

    sealed private abstract static class TreeADT permits SymbolicDecisionTree.Leaf, SymbolicDecisionTree.Node {}

    final class Leaf extends SymbolicDecisionTree.TreeADT {
        Integer classLabel = null;

        Leaf(Integer label) {
            this.classLabel = label;
        }
    }

    final class Node extends SymbolicDecisionTree.TreeADT {
        LRATuplePred pred;

        //List<Domain> data
        //feature_inded
        //feature_value
        //prediction_probability
        //information_gain

        TreeADT left;
        TreeADT right;

        Node(int featureIndex, Rational threshold) {
            pred = new VarRat(featureIndex, threshold);
        }

        Node(LRATuplePred pred) {
            this.pred = pred;
        }

        Node (int featIdx1, int featIdx2) {
            pred = new VarVar(featIdx1, featIdx2);
        }
    }


    private TreeADT root;

    private SymbolicDecisionTree() {

    }

    public static SymbolicDecisionTree buildByIterativeDeepening(List<LRATuple> features, List<Integer> labels) {
        SymbolicDecisionTree tree = new SymbolicDecisionTree();
        for (int depthLimit = 1; ; depthLimit++) {
            TreeADT root = tree.buildTree(features, labels, depthLimit);
            if (root != null) {
                tree.root = root;
                return tree;
            }
        }
    }




    private TreeADT buildTree(List<LRATuple> data, List<Integer> labels, int depthLimit) {
        // check if they are all of the same class
        if (labels.isEmpty() || labels.stream().allMatch(labels.getFirst()::equals)) {
            return new Leaf(labels.getFirst()); // Leaf node
        }

        if (depthLimit == 0) {
            return null; // Depth limit reached without perfect classification
        }

        LRATuplePred bestPred = new VarRat(-1, Rational.of(0));

        double bestGain = -1;
        List<LRATuple> bestLeftFeatures = new ArrayList<>();
        List<Integer> bestLeftLabels = new ArrayList<>();
        List<LRATuple> bestRightFeatures = new ArrayList<>();
        List<Integer> bestRightLabels = new ArrayList<>();

        // consider constraints of type x_i <= x_j
        for (int i = 0; i < data.getFirst().tuple.size(); i++) {
            for (int j = 0; j < data.getFirst().tuple.size(); j++) {
                if (i == j) {
                    continue;
                }
                List<LRATuple> leftFeatures = new ArrayList<>();
                List<Integer> leftLabels = new ArrayList<>();
                List<LRATuple> rightFeatures = new ArrayList<>();
                List<Integer> rightLabels = new ArrayList<>();

                for (int k = 0; k < data.size(); k++) {
                    if (leq(data.get(k).tuple.get(i), data.get(k).tuple.get(j))) {
                        leftFeatures.add(data.get(k));
                        leftLabels.add(labels.get(k));
                    } else {
                        rightFeatures.add(data.get(k));
                        rightLabels.add(labels.get(k));
                    }
                }

                if (!leftLabels.isEmpty() && !rightLabels.isEmpty()) {
                    double gain = informationGain(labels, leftLabels, rightLabels);
                    if (gain > bestGain) {
                        bestGain = gain;
                        bestPred = new VarVar(i , j);
                        bestLeftFeatures = leftFeatures;
                        bestLeftLabels = leftLabels;
                        bestRightFeatures = rightFeatures;
                        bestRightLabels = rightLabels;
                    }
                }
            }
        }

        // consider constraints of type x_i <= c
        for (int i = 0; i < data.getFirst().tuple.size(); i++) {
            Rational threshold = findBestThreshold(data, labels, i);
            List<LRATuple> leftFeatures = new ArrayList<>();
            List<Integer> leftLabels = new ArrayList<>();
            List<LRATuple> rightFeatures = new ArrayList<>();
            List<Integer> rightLabels = new ArrayList<>();

            for (int j = 0; j < data.size(); j++) {
                if (leq(data.get(j).tuple.get(i), threshold)) {
                    leftFeatures.add(data.get(j));
                    leftLabels.add(labels.get(j));
                } else {
                    rightFeatures.add(data.get(j));
                    rightLabels.add(labels.get(j));
                }
            }


            if (!leftLabels.isEmpty() && !rightLabels.isEmpty()) {
                double gain = informationGain(labels, leftLabels, rightLabels);
                if (gain > bestGain) {
                    bestGain = gain;
                    bestPred = new VarRat(i, threshold);
                    bestLeftFeatures = leftFeatures;
                    bestLeftLabels = leftLabels;
                    bestRightFeatures = rightFeatures;
                    bestRightLabels = rightLabels;
                }
            }
        }

        if (bestGain == -1) {
            return null; // No valid split found
        }

        Node node = new Node(bestPred);
        node.left = buildTree(bestLeftFeatures, bestLeftLabels, depthLimit - 1);
        node.right = buildTree(bestRightFeatures, bestRightLabels, depthLimit - 1);

        // Could not perfectly classify
        if (node.left == null || node.right == null) return null;

        return node;
    }


    // return true if a <= b
    public static boolean leq(Rational a, Rational b) {
        return a.compareTo(b) <= 0;
    }

    private Rational findBestThreshold(List<LRATuple> subsetFeatures, List<Integer> subsetLabels, int featureIndex) {
        TreeSet<Rational> uniqueValues = new TreeSet<>();
        for (LRATuple row : subsetFeatures) {
            uniqueValues.add(row.tuple.get(featureIndex));
        }
        List<Rational> sortedValues = new ArrayList<>(uniqueValues);

        Rational bestThreshold = sortedValues.getFirst();
        double bestGain = -1;

        for (int i = 0; i < sortedValues.size() - 1; i++) {
            // take the middle/average point as a threshold
            //Rational threshold = (sortedValues.get(i).plus(sortedValues.get(i + 1))).divides(Rational.of(2));

            // Take the smaller one as a leq threshold
            Rational threshold = sortedValues.get(i);

            List<Integer> leftLabels = new ArrayList<>();
            List<Integer> rightLabels = new ArrayList<>();

            for (int j = 0; j < subsetFeatures.size(); j++) {
                if (leq(subsetFeatures.get(j).tuple.get(featureIndex),threshold)) {
                    leftLabels.add(subsetLabels.get(j));
                } else {
                    rightLabels.add(subsetLabels.get(j));
                }
            }

            if (!leftLabels.isEmpty() && !rightLabels.isEmpty()) {
                double gain = informationGain(subsetLabels, leftLabels, rightLabels);
                if (gain > bestGain) {
                    bestGain = gain;
                    bestThreshold = threshold;
                }
            }
        }
        return bestThreshold;
    }

    private double entropy(List<Integer> labels) {
        Map<Integer, Integer> counts = new HashMap<>();
        labels.forEach(l -> counts.put(l, counts.getOrDefault(l, 0) + 1));

        double entropy = 0.0;
        int total = labels.size();
        for (int count : counts.values()) {
            double p = (double) count / total;
            entropy -= p * Math.log(p) / Math.log(2);
        }
        return entropy;
    }

    private double informationGain(List<Integer> allLabels, List<Integer> leftLabels, List<Integer> rightLabels) {
        double parentEntropy = entropy(allLabels);

        double weightLeft = (double) leftLabels.size() / allLabels.size();
        double weightRight = (double) rightLabels.size() / allLabels.size();

        return parentEntropy - (weightLeft * entropy(leftLabels) + weightRight * entropy(rightLabels));
    }

    public int classify(LRATuple instance) {
        TreeADT curr = root;
        while (true) {
            switch (curr) {
                case Leaf l -> {
                    return l.classLabel;
                }
                case Node n -> curr = switch (n.pred) {
                    case VarRat v -> leq(instance.tuple.get(v.index_left()), v.rat()) ? n.left : n.right;
                    case VarVar v -> leq(instance.tuple.get(v.index_left()), instance.tuple.get(v.index_right())) ? n.left : n.right;
                };
            }
        }
    }

    public void printTree() {
        printTree(root, 0);
    }

    private void printTree(TreeADT node, int depth) {
        if (node == null) return;
        for (int i = 0; i < depth; i++) System.out.print("  ");

        switch (node) {
            case Leaf l -> {
                System.out.println("Class: " + l.classLabel);
            }
            case Node n -> {
                switch (n.pred) {
                    case VarVar v -> System.out.println("Feature idx " + v.index_left() + " <= Feature idx " + v.index_right());
                    case VarRat v -> System.out.println("Feature index " + v.index_left() + " <= " + v.rat());
                }
                printTree(n.left, depth + 1);
                printTree(n.right, depth + 1);
            }
        }
    }

    public BooleanFormula getDNFForLabel(int label, NumeralFormula.RationalFormula[] variables, BooleanFormulaManager boolMgr, RationalFormulaManager ratMgr) {
        List<BooleanFormula> fms = getDNFForLabel(label, variables, root, boolMgr, ratMgr);
        return fms == null ? boolMgr.makeFalse() : boolMgr.or(fms);

    }

    private List<BooleanFormula> getDNFForLabel(int label, NumeralFormula.RationalFormula[] variables, TreeADT tree, BooleanFormulaManager boolMgr, RationalFormulaManager ratMgr) {
        //Basically, exhaustive DFS
        switch (tree) {
            case Leaf l -> {
                if (l.classLabel == label) {
                    List<BooleanFormula> res = new ArrayList<>();
                    res.add(boolMgr.makeTrue());
                    return res;
                } else {
                    return null;
                }
            }
            case Node n -> {
                List<BooleanFormula> leftFms = getDNFForLabel(label, variables, n.left, boolMgr, ratMgr);
                List<BooleanFormula> rightFms = getDNFForLabel(label, variables, n.right, boolMgr, ratMgr);
                BooleanFormula pred = switch (n.pred) {
                    case VarVar v -> ratMgr.lessOrEquals(variables[v.index_left()], variables[v.index_right()]);
                    case VarRat r -> ratMgr.lessOrEquals(variables[r.index_left()], ratMgr.makeNumber(r.rat()));
                };

                BooleanFormula negPred = boolMgr.not(pred);

                leftFms = leftFms != null ? leftFms.stream().map(x -> boolMgr.and(x, pred)).toList() : new ArrayList<>();
                rightFms = rightFms != null ? rightFms.stream().map(x -> boolMgr.and(x, negPred)).toList() : new ArrayList<>();

                return Stream.concat(leftFms.stream(), rightFms.stream()).toList();
            }
        }
    }


    public static void main(String[] args) throws InvalidConfigurationException {

        Configuration config = Configuration.defaultConfiguration();
        LogManager logger = BasicLogManager.create(config);
        ShutdownNotifier notifier = ShutdownNotifier.createDummy();
        SolverContext context =
                SolverContextFactory.createSolverContext(config, logger, notifier, SolverContextFactory.Solvers.Z3);

        BooleanFormulaManager boolMgr = context.getFormulaManager().getBooleanFormulaManager();
        RationalFormulaManager ratMgr = context.getFormulaManager().getRationalFormulaManager();


        List<LRATuple> features = Arrays.asList(
                new LRATuple(new Long[]{1L, 2L}) ,
                new LRATuple(new Long[]{2L, 3L}),
                new LRATuple(new Long[]{4L, 3L}),
                new LRATuple(new Long[]{4L, 2L}),
                new LRATuple(new Long[]{5L, 6L}),
                new LRATuple(new Long[]{74L, 69L})
        );
        List<Integer> labels = Arrays.asList(0, 0, 1, 1,0,0);

        SymbolicDecisionTree tree = SymbolicDecisionTree.buildByIterativeDeepening(features, labels);
        tree.printTree();

        NumeralFormula.RationalFormula[] variables = new NumeralFormula.RationalFormula[]{ratMgr.makeVariable("x0"), ratMgr.makeVariable("x1")};
        System.out.println(tree.getDNFForLabel(0,variables,boolMgr,ratMgr));
    }
}
