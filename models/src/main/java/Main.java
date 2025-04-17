import automata.sfa.SFA;
import learning.sfa.RBMerging;
import learning.sfa.RPNI;
import org.sat4j.specs.TimeoutException;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.java_smt.api.BooleanFormula;
import theory.BooleanAlgebra;
import theory.LRATuples.LRATuple;
import theory.LRATuples.RationalTupleCompAlgebra;
import theory.characters.CharFunc;
import theory.characters.CharPred;
import theory.intervals.IntPred;
import theory.intervals.IntegerSolver;
import theory.intervals.UnaryCharIntervalSolver;
import utilities.*;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.*;
import utilities.exceptions.DeterminismViolationException;
import utilities.parsing.LRATupleParser;
import utilities.parsing.StringToUnicodeWordParser;

public class Main {

    private static void ratTupleComparisonAlgebra(CommandLine cmdline) throws FileNotFoundException, InvalidConfigurationException, DeterminismViolationException, TimeoutException {
        PosNegSamples<LRATuple> lratuple =  PosNegSamples.readSamplesfromFile(cmdline.getOptionValue("i"), new LRATupleParser());
        //lratuple.printSamples();

        //todo remove fixed dimension
        BooleanAlgebra<BooleanFormula, LRATuple> algebra = new RationalTupleCompAlgebra(2);

        SFA<BooleanFormula,LRATuple> res = RPNI.run(lratuple.getPositiveSamples(), lratuple.getNegativeSamples(), algebra);

        if (cmdline.hasOption("o")) {
            Path p = Paths.get(cmdline.getOptionValue("o"));
            res.createDotFile(p.getFileName().toString(), p.getParent().toString());
        } else {
            System.out.println(res.toString());
        }
    }

    private static void integerComparisonAlgebra(CommandLine cmdline) throws IOException, DeterminismViolationException, TimeoutException {
        if (!cmdline.getOptionValue("i").endsWith(".json")) {
            if (cmdline.hasOption("format")) {
                if (! cmdline.getOptionValue("format").equals("json")) {
                    throw new IllegalArgumentException("Expected Json file as input!");
                }
            } else {
                throw new IllegalArgumentException("Expected Json file as input!");
            }
        }
        PosNegSamples<Character> samples =  PosNegSamples.readSamplesFromJsonFile(cmdline.getOptionValue("i"), new StringToUnicodeWordParser());


        //todo all of this
        BooleanAlgebra<CharPred, Character> algebra = new UnaryCharIntervalSolver();

        SFA<CharPred,Character> res = RBMerging.run(samples.getPositiveSamples(), samples.getNegativeSamples(), algebra);

        if (cmdline.hasOption("o")) {
            Path p = Paths.get(cmdline.getOptionValue("o"));
            res.createDotFile(p.getFileName().toString(), p.getParent().toString() + File.separator);
        } else {
            System.out.println(res.toString());
        }

    }

    public static void main(String[] args) throws TimeoutException, InvalidConfigurationException, IOException, DeterminismViolationException {

        Option theory = Option.builder("theory")
                .hasArg()
                .required()
                .build();
        Option inputFile = Option.builder("i")
                .longOpt("input")
                .hasArg()
                .required()
                .build();
        Option output = Option.builder("o")
                .longOpt("output")
                .hasArg()
                .required(false)
                .build();

        Option format = Option.builder("format")
                .hasArg()
                .required(false)
                .build();

        Options options = new Options();
        options.addOption(theory);
        options.addOption(inputFile);
        options.addOption(output);
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("help", options );
            System.exit(0);
        }

        switch (commandLine.getOptionValue("theory")) {
            case "RatTupComp" -> ratTupleComparisonAlgebra(commandLine);
            case "CharComparison" -> integerComparisonAlgebra(commandLine);
        }

    }
}
