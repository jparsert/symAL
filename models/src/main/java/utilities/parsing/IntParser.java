package utilities.parsing;

import java.io.IOException;



public class IntParser implements ElementParser<Integer> {
    @Override
    public Integer parse(String word) throws IOException {
        return Integer.parseInt(word);
    }
}
