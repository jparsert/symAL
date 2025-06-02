package theory.intervals;

import com.google.common.collect.ImmutableList;
import org.sat4j.specs.TimeoutException;
import theory.BooleanAlgebra;
import theory.characters.CharPred;
import theory.characters.CharTuplePred;
import theory.characters.StdCharPred;
import utilities.Pair;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;


public class CharIntervalTupleSolver extends BooleanAlgebra<CharTuplePred, Character[]> {

    UnaryCharIntervalSolver solver;

    int dimension;

    public CharIntervalTupleSolver(int dimension) {
        this.solver = new UnaryCharIntervalSolver();
        this.dimension = dimension;
    }

    @Override
    public CharTuplePred MkAtom(Character[] characters) {
        return new CharTuplePred(List.of(characters), false);
    }

    @Override
    public CharTuplePred MkNot(CharTuplePred charTuplePred) throws TimeoutException {
        List<CharPred> p = charTuplePred.intervals.stream().map((u)->solver.MkNot(u)).toList();
        return new CharTuplePred(p);
    }

    @Override
    public CharTuplePred True() {
        CharPred[] chars = new CharPred[dimension];
        for (int i = 0; i < dimension; i++) {
            chars[i] = StdCharPred.TRUE;
        }
        return new CharTuplePred(List.of(chars));
    }

    @Override
    public CharTuplePred False() {
        CharPred[] chars = new CharPred[dimension];
        for (int i = 0; i < dimension; i++) {
            chars[i] = StdCharPred.FALSE;
        }
        return new CharTuplePred(List.of(chars));
    }


    @Override
    public CharTuplePred MkOr(Collection<CharTuplePred> pset) throws TimeoutException {
        CharTuplePred or = this.False();
        for(CharTuplePred a : pset) {
            or = MkOr(or, a);
        }
        return or;
    }

    @Override
    public CharTuplePred MkOr(CharTuplePred p1, CharTuplePred p2) throws TimeoutException {
        return MkNot(MkAnd(MkNot(p1), MkNot(p2)));
    }

    @Override
    public CharTuplePred MkAnd(Collection<CharTuplePred> pset) throws TimeoutException {
        CharTuplePred or = this.True();
        for(CharTuplePred a : pset) {
            or = MkAnd(or, a);
        }
        return or;
    }

    private void validCharTuplePreds(CharTuplePred p1 , CharTuplePred p2) {
        checkArgument(p1.intervals.size() == p2.intervals.size());
        checkArgument(dimension == p2.intervals.size());
    }

    private void validCharTuplePreds(CharTuplePred p1) {
        checkArgument(dimension == p1.intervals.size());
    }

    @Override
    public CharTuplePred MkAnd(CharTuplePred p1, CharTuplePred p2) throws TimeoutException {
        validCharTuplePreds(p1, p2);

        ImmutableList.Builder<CharPred> v =  ImmutableList.builder();
        for (int i = 0; i < dimension; i++) {
            v.add(solver.MkAnd(p1.intervals.get(i), p2.intervals.get(i)));
        }

        return new CharTuplePred(v.build(), false);
    }


    @Override
    public boolean AreEquivalent(CharTuplePred p1, CharTuplePred p2) throws TimeoutException {
        validCharTuplePreds(p1, p2);
        for(int i = 0; i < dimension; i++) {
            if (! solver.AreEquivalent(p1.intervals.get(i), p2.intervals.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean IsSatisfiable(CharTuplePred p1) throws TimeoutException {
        validCharTuplePreds(p1);
        for(int i = 0; i < dimension; i++) {
            if (! solver.IsSatisfiable(p1.intervals.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean HasModel(CharTuplePred p1, Character[] el) throws TimeoutException {
        validCharTuplePreds(p1);
        checkArgument(dimension == p1.intervals.size());
        checkArgument(dimension == el.length);

        for(int i = 0; i < dimension; i++) {
            if (! solver.HasModel(p1.intervals.get(i), el[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean HasModel(CharTuplePred p1, Character[] el1, Character[] el2) throws TimeoutException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Character[] generateWitness(CharTuplePred p1) throws TimeoutException {
        validCharTuplePreds(p1);

        Character[] witness = new Character[dimension];
        for(int i = 0; i < dimension; i++) {
            witness[i] = solver.generateWitness(p1.intervals.get(i));
        }

        return witness;
    }

    @Override
    public Pair<Character[], Character[]> generateWitnesses(CharTuplePred p1) throws TimeoutException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
