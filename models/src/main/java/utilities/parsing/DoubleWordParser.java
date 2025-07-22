package utilities.parsing;

import com.google.common.primitives.Chars;
import com.google.common.primitives.Doubles;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public class DoubleWordParser  implements WordParser<Double> {
    @Override
    public List<Double> parse(Object word) throws IOException {
        if (word instanceof JSONArray arrWord) {
            List<Double> result = new ArrayList<>();
            for (int i = 0; i < arrWord.length(); i++) {
                Double e = arrWord.getDouble(i);
                result.add(e);
            }
            return result;
        }

        throw new JSONException("The following is not a string:" + word.toString());
    }
}
