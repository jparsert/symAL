package utilities.parsing;

import org.sosy_lab.common.rationals.Rational;
import theory.LRATuples.LRATuple;
import utilities.RationalParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LRATupleParser implements  ElementParser<LRATuple> {
    @Override
    public LRATuple parse(String word) throws IOException {
        String s = word.substring(1, word.length()-1);

        String[] els = s.split(",");
        RationalParser parser = new RationalParser();
        List<LRATuple> l = new ArrayList<>();
        List<Rational> res = Arrays.stream(els).map((String el) -> {
            try {
                return parser.parse(el);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).toList();

        return new LRATuple(res);
    }
}
