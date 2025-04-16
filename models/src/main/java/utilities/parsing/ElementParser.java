package utilities.parsing;

import java.io.IOException;

public interface ElementParser <E> {

    E parse(String symbol) throws IOException;

}
