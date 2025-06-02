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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


public class PosNegSamples<E> {

    public List<List<E>> getPositiveSamples() {
        return positiveSamples;
    }

    public List<List<E>> getNegativeSamples() {
        return negativeSamples;
    }

    private final List<List<E>> positiveSamples;

    private final List<List<E>> negativeSamples;


    private PosNegSamples() {
        this.positiveSamples = new ArrayList<>();
        this.negativeSamples = new ArrayList<>();
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
    }

    public static <E,P extends ElementParser<E>> PosNegSamples<E> readSamplesFromFile(String fileName, P parser) throws FileNotFoundException {

        PosNegSamples<E> readPosNegSamples = new PosNegSamples<>();

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


    // We read from json file
    public static <E,P extends WordParser<E>> PosNegSamples<E> readSamplesFromJsonFile(String fileName, P parser) throws IOException {

        // Read the file into a String
        String content = new String(Files.readAllBytes(Paths.get(fileName)));
        JSONObject json = new JSONObject(content);

        JSONArray positiveSamples = json.getJSONArray("pos");
        JSONArray negativeSamples = json.getJSONArray("neg");

        PosNegSamples<E> readPosNegSamples = new PosNegSamples<>();

        for (int i = 0; i < positiveSamples.length(); i++) {
            String sample = positiveSamples.getString(i);
            readPosNegSamples.positiveSamples.add(parser.parse(sample));
        }

        for (int i = 0; i < negativeSamples.length(); i++) {
            String sample = negativeSamples.getString(i);
            readPosNegSamples.negativeSamples.add(parser.parse(sample));
        }

        return readPosNegSamples;
    }
}
