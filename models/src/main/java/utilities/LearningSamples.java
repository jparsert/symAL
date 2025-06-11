package utilities;


import org.json.JSONArray;
import org.json.JSONObject;
import utilities.parsing.ElementParser;
import utilities.parsing.WordParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class LearningSamples<E> {

    public List<List<E>> getPositiveSamples() {
        return positiveSamples;
    }

    public List<List<E>> getNegativeSamples() {
        return negativeSamples;
    }

    public List<Pair<List<E>, List<E>>> getImplicationSamples() {
        return implicationSamples;
    }


    private final List<List<E>> positiveSamples;

    private final List<List<E>> negativeSamples;

    private final List<Pair<List<E>,List<E>>> implicationSamples;

    private Optional<Integer> dimension = Optional.empty();


    private LearningSamples() {
        this.positiveSamples = new ArrayList<>();
        this.negativeSamples = new ArrayList<>();
        this.implicationSamples = new ArrayList<>();
    }

    public void printSamples() {
        System.out.println("Positive samples:");
        for (List<E> sample : positiveSamples) {
            System.out.println("\t" + sample);
        }

        System.out.println("Negative samples:");
        for (List<E> sample : negativeSamples) {
            System.out.println("\t" + sample);
        }

        System.out.println("Implication/Inductive samples:");
        for (Pair<List<E>, List<E>> sample : implicationSamples) {
            System.out.println("\t" + sample.first + "\t-->\t" + sample.second);
        }
    }

    public static <E,P extends ElementParser<E>> LearningSamples<E> readSamplesFromFile(String fileName, P parser) throws FileNotFoundException {

        LearningSamples<E> readPosNegSamples = new LearningSamples<>();

        File file = new File(fileName);
        Scanner scanner = new Scanner(file);
        int i = 0;
        while (scanner.hasNextLine()) {
            String data = scanner.nextLine();
            if (i == 0) { //first line; we don't care about the header for now.
                i++;
                continue;
            }
            String[] split = data.split(" ");
            List<E> e = Arrays.stream(split).toList().subList(2,split.length).stream().map((String el) -> {
                try {
                    return parser.parse(el);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }).toList();
            if (data.startsWith("0")) {
                readPosNegSamples.negativeSamples.add(e);
            } else if (data.startsWith("1")) {
                readPosNegSamples.positiveSamples.add(e);
            } else {
                throw new UnknownError("Read an unknown line in " + fileName + " line: " + i + ".");
            }
            i++;
        }
        scanner.close();

        return readPosNegSamples;
    }


    // We read from json file where each word is a string of single elements (e.g. chars) in the array.
    public static <E,P extends WordParser<E>> LearningSamples<E> readSamplesFromJsonFile(String fileName, P parser) throws IOException {

        // Read the file into a String
        String content = new String(Files.readAllBytes(Paths.get(fileName)));
        JSONObject json = new JSONObject(content);

        LearningSamples<E> readPosNegSamples = new LearningSamples<>();

        JSONArray positiveSamples = json.getJSONArray("pos");
        JSONArray negativeSamples = json.getJSONArray("neg");
        if (json.has("dimension")) {
            readPosNegSamples.dimension = Optional.of(json.getInt("dimension"));
        }

        for (int i = 0; i < positiveSamples.length(); i++) {
            readPosNegSamples.positiveSamples.add(parser.parse(positiveSamples.get(i)));
        }

        for (int i = 0; i < negativeSamples.length(); i++) {
            readPosNegSamples.negativeSamples.add(parser.parse(negativeSamples.get(i)));
        }

        if (json.has("ind")) {
            JSONArray ind = json.getJSONArray("ind");
            for (int i = 0; i < ind.length(); i++) {
                JSONArray pair = ind.getJSONArray(i);
                List<E> fst = parser.parse(pair.get(0));
                List<E> snd = parser.parse(pair.get(1));
                readPosNegSamples.implicationSamples.add(new Pair<>(fst,snd));
            }
        }


        return readPosNegSamples;
    }

    public Optional<Integer> getDimension() {
        return dimension;
    }

    public void setDimension(Optional<Integer> dimension) {
        this.dimension = dimension;
    }

    public boolean verifyDimensionality() {
        if (!dimension.isPresent()) {
            return true;
        }

        for(List<E> sample : positiveSamples) {
            for (E el : sample) {
                if (el instanceof Character[] arr) {
                    if (arr.length != dimension.get()) {
                        return false;
                    }
                } else {
                    throw new UnknownError("Letters in words are not instances of the supported types.");
                }
            }
        }

        for(List<E> sample : negativeSamples) {
            for (E el : sample) {
                if (el instanceof Character[] arr) {
                    if (arr.length != dimension.get()) {
                        return false;
                    }
                } else {
                    throw new UnknownError("Letters in words are not instances of the supported types.");
                }
            }
        }

        return true;
    }

}
