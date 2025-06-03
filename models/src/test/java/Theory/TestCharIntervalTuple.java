package Theory;

import org.junit.Test;
import theory.BooleanAlgebra;
import theory.characters.CharTuplePred;
import theory.intervals.CharIntervalTupleSolver;
import utilities.PosNegSamples;
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
                PosNegSamples<Character[]> samples =  PosNegSamples.readSamplesFromJsonFile(file.getPath(), new CharIntervalTupleParser());

                assert(samples.getDimension().isPresent());
                assert(samples.verifyDimensionality());
            }
        }
        assert files > 0;
    }
}
