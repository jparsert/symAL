package theory.characters;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * CharPred: a set of characters represented as contiguous intervals
 */
public class CharTuplePred extends ICharPred{

    // We model thisd as an ordered list of intervals. Each element in the list is the predicate for that
    // component/dimension. This is possible, because in this theory, we cannot compare between components/dimensions
    public final ImmutableList<CharPred> intervals;


    public CharTuplePred(ImmutableList<CharPred> intervals) {
        checkNotNull(intervals);
        for (CharPred interval : intervals) {
            interval.isValidInterval();
        }
        this.intervals = intervals;
    }


    /**
     * The set containing only the characters in <code>c</code>
     */
    public CharTuplePred(Collection<Character> chars, boolean isReturn) {
        ImmutableList.Builder<CharPred> builder = ImmutableList.builder();
        for (Character character : chars) {
            builder.add(new CharPred(character, isReturn));
        }

        this.intervals = builder.build();

        if(isReturn)
            this.setAsReturn();
    }

    /**
     * The set containing only the character <code>c</code>
     */
    //public CharTuplePred(Collection<Character> chars) {
    //    this(chars, false);
    //}



    /**
     * The set containing only the interval <code>[bot,top]</code> (extremes
     * included)
     */
    //public CharTuplePred(Character bot, Character top, boolean isReturn) {
    //    this(ImmutableList.of(ImmutablePair.of(bot, top)), isReturn);
    //}

    /**
     * The set containing only the interval <code>[bot,top]</code> (extremes
     * included)
     */
    //public CharTuplePred(Character bot, Character top) {
    //    this(bot, top, false);
    //}

    public static CharTuplePred of(ImmutableList<Character> characters) {
        return of(characters, false);
    }

    public static CharTuplePred of(ImmutableList<Character> characters, boolean isReturn) {
        ImmutableList.Builder<CharPred> preds = ImmutableList.builder();
        for (Character c : checkNotNull(characters)) {
            preds.add(new CharPred(c, isReturn));
        }
        CharTuplePred res = new CharTuplePred(preds.build());
        if(isReturn)
            res.setAsReturn();
        return res;
    }
    public CharTuplePred(Collection<CharPred> intervals) {
        this(ImmutableList.copyOf(intervals), false);
    }

    public CharTuplePred(ImmutableList<CharPred> intervals, boolean isReturn) {
        for (CharPred interval : checkNotNull(intervals)) {
            interval.isValidInterval();
        }

        this.intervals = sortIntervals(checkNotNull(intervals));
        if(isReturn)
            setAsReturn();
    }

    private static ImmutableList<CharPred> sortIntervals(ImmutableList<CharPred> intervals) {
        ImmutableList.Builder<CharPred> ansBuilder = ImmutableList.builder();

        for (CharPred interval : checkNotNull(intervals)) {
            interval.isValidInterval();
            ImmutableList<ImmutablePair<Character, Character>> sorted = CharPred.sortIntervals(interval.intervals);
            CharPred p = new CharPred(sorted);
            ansBuilder.add(p);
        }

        return ansBuilder.build();
    }

    public static ImmutableList<CharPred> invertIntervals(ImmutableList<CharPred> intervals) {
        ImmutableList.Builder<CharPred> ansBuilder = ImmutableList.builder();

        for (CharPred interval : checkNotNull(intervals)) {
            interval.isValidInterval();
            ImmutableList<ImmutablePair<Character, Character>> inverted = CharPred.invertIntervals(interval.intervals);
            CharPred p = new CharPred(inverted);
            ansBuilder.add(p);
        }

        return ansBuilder.build();
    }

    public boolean isSatisfiedBy(List<Character> c) {
        assert c.size() == intervals.size();

        for (int i = 0; i < intervals.size(); i++) {
            intervals.get(i).isSatisfiedBy(c.get(i));
        }

        return false;
    }

    @Override
    public String toString() {

        List<String> strings =  intervals.stream().map(CharPred::toString).toList();
        String res = "( " + String.join(", ", strings) + " )";

        return res;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CharTuplePred) {
            return Objects.equals(intervals, ((CharTuplePred)obj).intervals);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(CharTuplePred.class, intervals);
    }



}
