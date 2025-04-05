import ap.util.CmdlParser;
import automata.sfa.SFA;
import learning.sfa.RPNI;
import org.sat4j.specs.TimeoutException;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.rationals.Rational;
import org.sosy_lab.java_smt.api.BooleanFormula;
import scala.Int;
import theory.BooleanAlgebra;
import theory.LRATuples.LRATuple;
import theory.LRATuples.RationalTupleCompAlgebra;
import utilities.*;


import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static automata.sfa.SFA.MkPTA;
import static java.io.IO.println;

import org.apache.commons.cli.*;

public class Main {

    private static void ratTupleComparisonAlgebra(CommandLine cmdline) throws FileNotFoundException, InvalidConfigurationException, DeterminismViolationException, TimeoutException {
        PosNegSamples<LRATuple, LRATupleParser> lratuple =  PosNegSamples.readSamplesfromFile(cmdline.getOptionValue("i"), new LRATupleParser());
        //lratuple.printSamples();

        //todo remove fixed dimension
        BooleanAlgebra<BooleanFormula, LRATuple> algebra = new RationalTupleCompAlgebra(2);

        SFA<BooleanFormula,LRATuple> res = RPNI.run(lratuple.getPositiveSamples(), lratuple.getNegativeSamples(), algebra);

        if (cmdline.hasOption("o")) {
            Path p = Paths.get(cmdline.getOptionValue("o"));
            res.createDotFile(p.getFileName().toString(), p.getParent().toString());
        } else {
            println(res.toString());
        }
    }


    public static void main(String[] args) throws TimeoutException, InvalidConfigurationException, FileNotFoundException, DeterminismViolationException, ParseException {

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

        Options options = new Options();
        options.addOption(theory);
        options.addOption(inputFile);
        options.addOption(output);
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = parser.parse(options, args);

        switch (commandLine.getOptionValue("theory")) {
            case "RatTupComp" -> ratTupleComparisonAlgebra(commandLine);
        }

    }
}
