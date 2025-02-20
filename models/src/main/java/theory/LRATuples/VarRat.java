package theory.LRATuples;

import org.sosy_lab.common.rationals.Rational;

/**
 * Implements the class of predicates 'x <= c' where x is an index (i.e. variable) and 'c' is a rational constant
 * the semantics are tuple[index_left] <= c
 */
public record VarRat(int index_left, Rational rat) implements LRATuplePred {


}
