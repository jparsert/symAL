package theory.LRATuples;

/**
 * Implements the class of predicates 'x <= y' where x and y are indexes (i.e. variables)
 * the semantics are tuple[index_left] <= tuple[index_right]
 */
public record VarVar(int index_left, int index_right) implements LRATuplePred {
}
