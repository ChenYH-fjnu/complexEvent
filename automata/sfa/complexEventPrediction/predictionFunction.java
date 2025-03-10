package automata.sfa.complexEventPrediction;

import automata.sfa.SFA;
import theory.characters.CharPred;
import theory.intervals.UnaryCharIntervalSolver;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeoutException;

import static automata.Automaton.processInputString;
import static automata.sfa.complexEventPrediction.Dis.*;
import static automata.sfa.complexEventPrediction.PSTree.*;
import static automata.sfa.complexEventPrediction.Graph.*;
public class predictionFunction {

    public static List<Integer> initialState4And(SFA<CharPred, Character> sfa, int[] andLength, int[] andBeginIndex){
        List<Integer> list = new ArrayList<>();
        for (int state:sfa.getStates()){
            for (int i = 0;i < andLength.length ;i++){
                 if (state >= andBeginIndex[i] && state <= andBeginIndex[i]+andLength[i]-1){
                     list.add(state);
                     break;
                 }
            }
        }
     return list;
    }

    public static void prediction(int predicteState, String inputFilePath, SFA<CharPred, Character> sfa, PSTTree<String> PSTree, String[] predicates,
                                  int memThreadshold, int upper, double threshold, Boolean existAnd, List<String[]> andParts, int[] andLength, int[] andBeginIndex, String[] symbols)
            throws org.sat4j.specs.TimeoutException, TimeoutException, IOException {

        UnaryCharIntervalSolver ba = new UnaryCharIntervalSolver();
        StringBuilder sb = new StringBuilder();
        boolean intoAnd = false;
        int currentAndIndex = -1;
        Collection<Integer> currentStates = new ArrayList<>();
        currentStates.add(sfa.getInitialState());
        List<String> sub_PatternTable = new LinkedList<>();
        if (existAnd) {
            List<Integer> andStatelist = initialState4And(sfa, andLength, andBeginIndex);
            try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (intoAnd){
                        for (int currentState:currentStates){
                             if (!andStatelist.contains(currentState)){
                                 intoAnd = false;
                                 sub_PatternTable.clear();
                             }
                        }
                    } else {
                        for (int currentState :currentStates){
                            for (int i = 0; i < andBeginIndex.length; i++){
                                    if (andBeginIndex[i] == currentState) {
                                        currentAndIndex = i;
                                        intoAnd = true;
                                        break;
                                    }
                            }
                        }
                    }

                    String event = determineEvent4Traffic(parts);
                    if (intoAnd){
                       for (int i = 0; i < andParts.get(currentAndIndex).length; i++){
                                if (andParts.get(currentAndIndex)[i].equals(event)){
                                     if (sub_PatternTable.contains(event)) {
                                         currentStates = processInputString(currentStates, sfa, ba, event.toLowerCase());

                                     } else {
                                         currentStates = processInputString(currentStates, sfa, ba, symbols[currentAndIndex]);
                                         sub_PatternTable.add(event);
                                     }
                                    break;
                                }
                        }
                    }
                    else currentStates = processInputString(currentStates, sfa, ba, event);
                    if (sb.length() == 0) {
                        sb.append(event);
                    } else if (sb.length() < memThreadshold) {
                        sb.append(event);
                    } else {
                        sb.append(event);
                        sb.deleteCharAt(0);
                    }
                    for (int i:currentStates){
                        if (i == predicteState){
                            List<Dis> distribution = countDis(PSTree, predicates, sfa, existAnd,andParts,andLength, andBeginIndex, symbols);
                            Dis foundDis = findDistribution(distribution, predicteState, sb.toString(), PSTree);
                            postPredictResult(foundDis,upper,threshold);
                        }
                    }
                }
            }
        }
        else {
            try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    String event = determineEvent4Traffic(parts);
                    currentStates = processInputString(currentStates, sfa, ba, event);

                    if (sb.length() == 0) {
                        sb.append(event);
                    } else if (sb.length() < memThreadshold) {
                        sb.append(event);
                    } else {
                        sb.append(event);
                        sb.deleteCharAt(0);
                    }

                    for (int i:currentStates){
                        if (i == predicteState){
                            List<Dis> distribution = countDis(PSTree, predicates, sfa, existAnd,andParts,andLength, andBeginIndex, symbols);
                            Dis foundDis = findDistribution(distribution, predicteState, sb.toString(), PSTree);
                            postPredictResult(foundDis,upper,threshold);
                        }
                    }
                }
            }
        }
    }



    public static void predictionWithEmbedding(int predicteState, String inputFilePath, SFA<CharPred, Character> sfa, Graph graphWithState,PSTTree<String> PSTree, String[] predicates,
                                  int memThreadshold, int upper, double threshold, Boolean existAnd, List<String[]> andParts, int[] andLength, int[] andBeginIndex, String[] symbols)
            throws org.sat4j.specs.TimeoutException, TimeoutException, IOException {

        UnaryCharIntervalSolver ba = new UnaryCharIntervalSolver();
        StringBuilder sb = new StringBuilder();
        boolean intoAnd = false;
        int currentAndIndex = -1;
        List<String> sub_PatternTable = new LinkedList<>();
        List<Integer> andStatelist = new LinkedList<>();
        if (existAnd) {
            andStatelist = initialState4And(sfa, andLength, andBeginIndex);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String event = determineEvent4Traffic(parts);

                if (graphWithState.getCurrentVertex() == null) {
                    if (sb.toString().length() < memThreadshold) {
                        sb.append(event);
                    } else {
                        sb.append(event);
                        sb.deleteCharAt(0);
                    }
                    for (int i = 0; i < sb.toString().length(); i++) {
                        if (graphWithState.containsVertex(sb.toString().substring(i), graphWithState.getInitialState())) {
                            Vertex CurrentVertex = new Vertex(sb.toString().substring(i), graphWithState.getInitialState());
                            graphWithState.setCurrentVertex(CurrentVertex);
                            break;
                        }
                    }
                } else {
                    if (existAnd) {
                        if (!andStatelist.contains(graphWithState.getCurrentVertex().state)) {
                            intoAnd = false;
                            sub_PatternTable.clear();
                        } else {
                            for (int i = 0; i < andBeginIndex.length; i++) {
                                if (andBeginIndex[i] == graphWithState.getCurrentVertex().state) {
                                    currentAndIndex = i;
                                    intoAnd = true;
                                    break;
                                }
                            }
                        }
                        if (intoAnd) {
                            for (int i = 0; i < andParts.get(currentAndIndex).length; i++) {
                                if (andParts.get(currentAndIndex)[i].equals(event)) {
                                    if (sub_PatternTable.contains(event)) {
                                        graphWithState.processInputEvent(event.toLowerCase());

                                    } else {
                                        graphWithState.processInputEvent(symbols[currentAndIndex]);
                                        sub_PatternTable.add(event);
                                    }
                                    break;
                                }
                            }
                        } else graphWithState.processInputEvent(event);


                        if (graphWithState.getCurrentVertex().state == predicteState) postPredictResult(graphWithState,upper,threshold);
                    }
                    else {
                        graphWithState.processInputEvent(event);
                        if (graphWithState.getCurrentVertex().state == predicteState) postPredictResult(graphWithState,upper,threshold);
                    }
                }
            }
        }
    }

    public static String determineEvent4Traffic(String[] parts) {
        String event = "";
        String trafficVolume = parts[2];
        String time = parts[0];
        int trafficVolumeInt;
        try {
            trafficVolumeInt = Integer.parseInt(trafficVolume);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format: " + trafficVolume);
            trafficVolumeInt = 0;
        }
        if (trafficVolumeInt <= 50) event = "A";
        else if (trafficVolumeInt <= 70) event = "B";
        else if (trafficVolumeInt <= 90) event = "C";
        else if (trafficVolumeInt > 90) event = "D";
        return event;
    }

}
