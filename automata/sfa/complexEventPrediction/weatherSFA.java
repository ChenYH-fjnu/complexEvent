package automata.sfa.complexEventPrediction;

import automata.sfa.SFA;
import automata.sfa.SFAInputMove;
import automata.sfa.SFAMove;
import com.google.common.collect.ImmutableList;
import org.sat4j.specs.TimeoutException;
import theory.characters.CharPred;
import theory.intervals.UnaryCharIntervalSolver;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
public class weatherSFA {
    public final static CharPred soft = new CharPred('s');
    public final static CharPred snow = new CharPred('a');
    public final static CharPred cold = new CharPred('b');
    public final static CharPred fog = new CharPred('c');
    public final static CharPred hail = new CharPred('d');
    public final static CharPred rain = new CharPred('e');
    public final static CharPred storm = new CharPred('f');
    public final static CharPred otherPrecipitation = new CharPred('g');
    public final static ImmutableList<Character> characters = ImmutableList.of('b', 'c', 'd','f','g');
    public final static CharPred irreWeather = CharPred.of(characters);

    public static SFA<CharPred, Character> weatherSFAForPattern(UnaryCharIntervalSolver ba) {
        Collection<SFAMove<CharPred, Character>> transitionsA = new LinkedList<SFAMove<CharPred, Character>>();
        transitionsA.add(new SFAInputMove<CharPred, Character>(1, 2, soft));
        transitionsA.add(new SFAInputMove<CharPred, Character>(1, 1, irreWeather));
        transitionsA.add(new SFAInputMove<CharPred, Character>(1, 1, snow));
        transitionsA.add(new SFAInputMove<CharPred, Character>(1, 1, rain));

        transitionsA.add(new SFAInputMove<CharPred, Character>(2, 3, soft));
        transitionsA.add(new SFAInputMove<CharPred, Character>(2, 1, irreWeather));
        transitionsA.add(new SFAInputMove<CharPred, Character>(2, 1, snow));
        transitionsA.add(new SFAInputMove<CharPred, Character>(2, 1, rain));

        transitionsA.add(new SFAInputMove<CharPred, Character>(3, 4, soft));
        transitionsA.add(new SFAInputMove<CharPred, Character>(3, 1, irreWeather));
        transitionsA.add(new SFAInputMove<CharPred, Character>(3, 1, snow));
        transitionsA.add(new SFAInputMove<CharPred, Character>(3, 1, rain));


        transitionsA.add(new SFAInputMove<CharPred, Character>(4, 5, rain));
        transitionsA.add(new SFAInputMove<CharPred, Character>(4, 5, snow));
        transitionsA.add(new SFAInputMove<CharPred, Character>(4, 1, irreWeather));
        transitionsA.add(new SFAInputMove<CharPred, Character>(4, 2, soft));

        transitionsA.add(new SFAInputMove<CharPred, Character>(5, 6, rain));
        transitionsA.add(new SFAInputMove<CharPred, Character>(5, 6, snow));
        transitionsA.add(new SFAInputMove<CharPred, Character>(5, 1, irreWeather));
        transitionsA.add(new SFAInputMove<CharPred, Character>(5, 2, soft));

        transitionsA.add(new SFAInputMove<CharPred, Character>(6, 7, rain));
        transitionsA.add(new SFAInputMove<CharPred, Character>(6, 7, snow));
        transitionsA.add(new SFAInputMove<CharPred, Character>(6, 1, irreWeather));
        transitionsA.add(new SFAInputMove<CharPred, Character>(6, 2, soft));

        transitionsA.add(new SFAInputMove<CharPred, Character>(7, 1, soft));
        try {
            return SFA.MkSFA(transitionsA, 1, Arrays.asList(7), ba);
        } catch (TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
