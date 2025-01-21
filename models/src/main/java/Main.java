import automata.sfa.SFA;
import org.sat4j.specs.TimeoutException;
import theory.intervals.RealPred;
import theory.intervals.RealSolver;


public class Main {


    public static void main(String[] args) throws TimeoutException {
        SFA<RealPred, Double> a = SFA.getEmptySFA(new RealSolver());

    }
}
