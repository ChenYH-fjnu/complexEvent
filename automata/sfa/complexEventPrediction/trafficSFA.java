package automata.sfa.complexEventPrediction;


import automata.sfa.SFA;
import automata.sfa.SFAInputMove;
import automata.sfa.SFAMove;
import org.sat4j.specs.TimeoutException;
import theory.characters.CharPred;
import theory.intervals.UnaryCharIntervalSolver;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;


public class trafficSFA {
    public final static CharPred Mild  = new CharPred('A');
    public final static CharPred Moderate = new CharPred('B');
    public final static CharPred Heavy = new CharPred('C');
    public final static CharPred Severe = new CharPred('D');
    public final static CharPred AND = new CharPred('#');
    public final static CharPred traditionalTrasition = new CharPred('b');
    public final static CharPred traditionalTrasition2 = new CharPred('c');
    public static SFA<CharPred, Character> trafficSFA(UnaryCharIntervalSolver ba) {
        Collection<SFAMove<CharPred, Character>> transitionsA = new LinkedList<SFAMove<CharPred, Character>>();
        transitionsA.add(new SFAInputMove<CharPred, Character>(1, 2, Mild));
        transitionsA.add(new SFAInputMove<CharPred, Character>(1, 1, Moderate));
        transitionsA.add(new SFAInputMove<CharPred, Character>(1, 1, Heavy));
        transitionsA.add(new SFAInputMove<CharPred, Character>(1, 1, Severe));

        transitionsA.add(new SFAInputMove<CharPred, Character>(2, 3, Mild));
        transitionsA.add(new SFAInputMove<CharPred, Character>(2, 1, Moderate));
        transitionsA.add(new SFAInputMove<CharPred, Character>(2, 1, Heavy));
        transitionsA.add(new SFAInputMove<CharPred, Character>(2, 1, Severe));

        transitionsA.add(new SFAInputMove<CharPred, Character>(3, 4, AND));
        transitionsA.add(new SFAInputMove<CharPred, Character>(3, 2, Mild));
        transitionsA.add(new SFAInputMove<CharPred, Character>(3, 1, Severe));


        transitionsA.add(new SFAInputMove<CharPred, Character>(4, 5, AND));
        transitionsA.add(new SFAInputMove<CharPred, Character>(4, 2, Mild));
        transitionsA.add(new SFAInputMove<CharPred, Character>(4, 1, Severe));
        transitionsA.add(new SFAInputMove<CharPred, Character>(4, 1, traditionalTrasition));
        transitionsA.add(new SFAInputMove<CharPred, Character>(4, 1, traditionalTrasition2));
        try {
            return SFA.MkSFA(transitionsA, 1, Arrays.asList(5), ba);
        } catch (TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
