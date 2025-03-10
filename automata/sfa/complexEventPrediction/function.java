package automata.sfa.complexEventPrediction;

import automata.sfa.SFA;
import theory.characters.CharPred;
import theory.intervals.UnaryCharIntervalSolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static automata.Automaton.processInputString;

public class function {

    public static List<String> getLastNCharactersAsList(String str, int n) {
        List<String> result = new ArrayList<>();

        if (str == null || str.length() <= n) {
            for (int i = 0; i < str.length(); i++) {
                result.add(String.valueOf(str.charAt(i)));
            }
        } else {
            String lastNChars = str.substring(str.length() - n);
            for (int i = 0; i < lastNChars.length(); i++) {
                result.add(String.valueOf(lastNChars.charAt(i)));
            }
        }
        return result;
    }
    public static boolean isLowerCase(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return str.equals(str.toLowerCase());
    }
    public static String toUpperCase(String str) {
        if (str == null) {
            return null;
        }
        return str.toUpperCase();
    }
    public static  Boolean checkPathValid(String path, int state, SFA<CharPred, Character> sfa, UnaryCharIntervalSolver ba) throws org.sat4j.specs.TimeoutException {
        Collection<Integer> currentStates = new ArrayList<>();
        currentStates.add(state);
        currentStates = processInputString(currentStates, sfa, ba, path);
        boolean finalState =false;
        for (int i:currentStates){
            if (sfa.getFinalStates().contains(i)){
                finalState = true;
            }
        }
        return  finalState;
    }
}
