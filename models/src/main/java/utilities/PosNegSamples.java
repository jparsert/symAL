package utilities;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


public class PosNegSamples<E, P extends ElementParser<E>> {

    public List<List<E>> getPositiveSamples() {
        return positiveSamples;
    }

    public List<List<E>> getNegativeSamples() {
        return negativeSamples;
    }

    private final List<List<E>> positiveSamples;

    private final List<List<E>> negativeSamples;

    private final P parser;

    private PosNegSamples(P parser) {
        this.positiveSamples = new ArrayList<>();
        this.negativeSamples = new ArrayList<>();
        this.parser = parser;
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

    public static <E,P extends ElementParser<E>> PosNegSamples<E, P> readSamplesfromFile(String fileName, P parser) throws FileNotFoundException {

        PosNegSamples<E, P> readPosNegSamples = new PosNegSamples<>(parser);

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
                    return readPosNegSamples.parser.parse(el);
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
}
