package theory.LRATuples;

import org.sosy_lab.java_smt.api.Formula;

/**
 * Implements the class of predicates 'x <= y' where x and y are indexes (i.e. variables)
 * the semantics are tuple[index_left] <= tuple[index_right]
 */
public record VarVar(int index_left, int index_right) implements LRATuplePred {

}
