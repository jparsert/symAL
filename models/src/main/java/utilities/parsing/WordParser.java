package utilities.parsing;

import java.io.IOException;
import java.util.List;

public interface WordParser <E> {

    List<E> parse(String word) throws IOException;

}
