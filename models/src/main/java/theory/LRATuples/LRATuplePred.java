package theory.LRATuples;

import org.sosy_lab.common.rationals.Rational;

sealed public interface LRATuplePred permits VarVar, VarRat{}

/**
 * Implements the class of predicates 'x <= y' where x and y are indexes (i.e. variables)
 */
final class VarVar implements LRATuplePred {

    // the semantics are tuple[index_left] < tuple[index_right]
    int index_left;
    int index_right;

    VarVar(int index_left, int index_right) {
        this.index_left = index_left;
        this.index_right = index_right;
    }
}


/**
 * Implements the class of predicates 'x <= c' where x is an index (i.e. variable) and 'c' is a rational constant
 */
final class VarRat implements LRATuplePred {

    int index_left;
    Rational c;

    VarRat(int index_left, Rational c) {
        this.index_left = index_left;
        this.c = c;
    }
}

