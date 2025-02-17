package theory.predicates;

import theory.BooleanAlgebra;

import java.util.List;

public class SymbolicDecisionTree <Pred,  Domain> {

    private class Node {

        private Pred pred;

        //List<Domain> data
        //feature_inded
        //feature_value
        //prediction_probability
        //information_gain


        // IF we have a leaf node we can set pred to true and set the data to the index of class
        Node left;
        Node right;

        Integer class_index = null; // Stay at null as long as we are not in a leaf node
    }


    private SymbolicDecisionTree() {

    }


    public static <Pred, Domain> SymbolicDecisionTree<Pred,Domain> buildTree(List<List<Domain>> values) {
        return new SymbolicDecisionTree<>();
    }


}
