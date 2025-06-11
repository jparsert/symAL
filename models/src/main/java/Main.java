import automata.sfa.SFA;
import learning.sfa.EDSM;
import learning.sfa.RPNI;
import learning.sfa.LearningWithIndEx;
import org.sat4j.specs.TimeoutException;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.java_smt.api.BooleanFormula;
import theory.BooleanAlgebra;
import theory.LRATuples.LRATuple;
import theory.LRATuples.RationalTupleCompAlgebra;
import theory.characters.CharPred;
import theory.characters.CharTuplePred;
import theory.intervals.CharIntervalTupleSolver;
import theory.intervals.UnaryCharIntervalSolver;
import utilities.*;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.*;
import utilities.exceptions.DeterminismViolationException;
import utilities.parsing.CharIntervalTupleParser;
import utilities.parsing.LRATupleParser;
import utilities.parsing.StringToUnicodeWordParser;

public class Main {

    private static String getJSONInputFile(CommandLine cmdline) {
        if (!cmdline.getOptionValue("i").endsWith(".json")) {
            if (cmdline.hasOption("format")) {
                if (! cmdline.getOptionValue("format").equals("json")) {
                    throw new IllegalArgumentException("Expected Json file as input!");
                }
            } else {
                throw new IllegalArgumentException("Expected Json file as input!");
            }
        }
        return cmdline.getOptionValue("i");
    }

    private static  <E,P> void printResultAutomaton(SFA<E,P> res, CommandLine cmdline) {
        if (cmdline.hasOption("o")) {
            Path p = Paths.get(cmdline.getOptionValue("o"));
            res.createDotFile(p.getFileName().toString(), p.getParent().toString() + File.separator);
        } else {
            System.out.println(res.toString());
        }
    }

    private static void ratTupleComparisonAlgebra(CommandLine cmdline) throws FileNotFoundException, InvalidConfigurationException, DeterminismViolationException, TimeoutException {
        LearningSamples<LRATuple> lratuple =  LearningSamples.readSamplesFromFile(cmdline.getOptionValue("i"), new LRATupleParser());
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
        String jsonInputFile = getJSONInputFile(cmdline);
        LearningSamples<Character> samples =  LearningSamples.readSamplesFromJsonFile(jsonInputFile, new StringToUnicodeWordParser());

        BooleanAlgebra<CharPred, Character> algebra = new UnaryCharIntervalSolver();

        SFA<CharPred,Character> res;

        if (cmdline.hasOption("strategy")) {
            switch (cmdline.getOptionValue("strategy")) {
                case "edsm" -> {
                    res = EDSM.run(samples.getPositiveSamples(), samples.getNegativeSamples(), algebra);
                }
                case "rpni" -> {
                    res = RPNI.run(samples.getPositiveSamples(), samples.getNegativeSamples(), algebra);
                }
                case "rpniind" -> {
                    res = LearningWithIndEx.RPNIInductiveFixedPoint(samples, algebra);
                }
                case "edsmind" -> {
                    res = LearningWithIndEx.EDSMInductiveFixedPoint(samples, algebra);
                }
                default -> {
                    res = EDSM.run(samples.getPositiveSamples(), samples.getNegativeSamples(), algebra);
                }
            }
        } else {
            res = EDSM.run(samples.getPositiveSamples(), samples.getNegativeSamples(), algebra);
        }

        res = res.mkTotal(algebra);

        printResultAutomaton(res, cmdline);

    }

    private static void charIntervalTupleSolver(CommandLine cmdline) throws IOException, DeterminismViolationException, TimeoutException {
        String jsonInputFile = getJSONInputFile(cmdline);
        LearningSamples<Character[]> samples =  LearningSamples.readSamplesFromJsonFile(jsonInputFile, new CharIntervalTupleParser());

        if(samples.getDimension().isEmpty()) {
            throw new UnknownError("Letters are not iterables.");
        }

        BooleanAlgebra<CharTuplePred, Character[]> algebra = new CharIntervalTupleSolver(samples.getDimension().get());

        SFA<CharTuplePred,Character[]> res;

        if (cmdline.hasOption("strategy")) {
            switch (cmdline.getOptionValue("strategy")) {
                case "edsm" -> {
                   res = EDSM.run(samples.getPositiveSamples(), samples.getNegativeSamples(), algebra);
                }
                case "rpni" -> {
                   res = RPNI.run(samples.getPositiveSamples(), samples.getNegativeSamples(), algebra);
                }
                default -> {
                   res = EDSM.run(samples.getPositiveSamples(), samples.getNegativeSamples(), algebra);
                }
            }
        } else {
           res = EDSM.run(samples.getPositiveSamples(), samples.getNegativeSamples(), algebra);
        }

        //res = res.mkTotal(algebra);

        printResultAutomaton(res, cmdline);

    }


    public static void main(String[] args) throws TimeoutException, InvalidConfigurationException, IOException, DeterminismViolationException {

        Option theory = Option.builder("thy")
                .longOpt("theory")
                .hasArg()
                .required()
                .build();

        Option strat = Option.builder("strat")
                .longOpt("strategy")
                .hasArg()
                //. ("Strategy to use. Default is EDSM for CharComparison.")
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
        options.addOption(format);
        options.addOption(strat);

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
            case "CharIntervalTupleSolver" -> charIntervalTupleSolver(commandLine);
        }

    }

}
