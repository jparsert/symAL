package utilities.parsing;

import org.json.JSONArray;
import org.json.JSONException;
import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CharIntervalTupleParser implements WordParser<Character[]> {

    @Override
    public List<Character[]> parse(Object word) throws IOException {

        if (word instanceof JSONArray arrWord) {
            List<Character[]> result = new ArrayList<>();
            for (int i = 0; i < arrWord.length(); i++) {
                JSONArray arr = arrWord.getJSONArray(i);
                Character[] chars = new Character[arr.length()];
                for (int j = 0; j < arr.length(); j++) {
                    checkArgument(arr.getString(j).length() == 1);
                    chars[j] = arr.getString(j).charAt(0);
                }
                result.add(chars);
            }
            return result;
        }

        throw new JSONException("The following is not a string:" + word.toString());
    }
}
