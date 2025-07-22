package utilities.parsing;

import java.io.IOException;



public class FloatParser implements ElementParser<Float> {
    @Override
    public Float parse(String word) throws IOException {
        return Float.parseFloat(word);
    }
}
