package utilities.parsing;

import com.google.common.primitives.Chars;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StringToUnicodeWordParser implements WordParser<Character> {
    @Override
    public List<Character> parse(String word) throws IOException {
        return new ArrayList<Character>(Chars.asList(word.toCharArray()));
    }
}
