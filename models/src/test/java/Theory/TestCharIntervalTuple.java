package Theory;

import org.junit.Test;
import utilities.LearningSamples;
import utilities.parsing.CharIntervalTupleParser;

import java.io.File;
import java.io.IOException;

public class TestCharIntervalTuple {

    @Test
    public void parsingTest() throws IOException {
        File folder = new File("src/test/resources/CharIntervalTuples");
        File[] listOfFiles = folder.listFiles();

        assert listOfFiles != null;

        int files = 0;
        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                files++;
                LearningSamples<Character[]> samples =  LearningSamples.readSamplesFromJsonFile(file.getPath(), new CharIntervalTupleParser());

                assert(samples.getDimension().isPresent());
                assert(samples.verifyDimensionality());
            }
        }
        assert files > 0;
    }
}
