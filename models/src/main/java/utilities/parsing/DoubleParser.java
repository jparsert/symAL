package utilities.parsing;

import java.io.IOException;



public class DoubleParser implements ElementParser<Double> {
    @Override
    public Double parse(String word) throws IOException {
        return Double.parseDouble(word);
    }
}
