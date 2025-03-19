package utilities;

import java.io.IOException;

public interface ElementParser <E> {

    E parse(String word) throws IOException;

}
