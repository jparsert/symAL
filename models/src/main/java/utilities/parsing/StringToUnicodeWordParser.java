package utilities.parsing;

import com.google.common.primitives.Chars;
import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class StringToUnicodeWordParser implements WordParser<Character> {
    @Override
    public List<Character> parse(Object word) throws IOException {
        if (word instanceof String s) {
            return new ArrayList<Character>(Chars.asList(s.toCharArray()));
        }
        throw new JSONException("The following is not a string: " + word.toString());
    }
}
