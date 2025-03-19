package utilities;

import org.sosy_lab.common.rationals.Rational;

import java.io.IOException;

public class RationalParser implements ElementParser<Rational> {
    @Override
    public Rational parse(String word) throws IOException {
        return Rational.of(word);
    }
}
