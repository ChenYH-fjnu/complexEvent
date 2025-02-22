package automata.sfa.complexEventPrediction;

import automata.sfa.SFA;
import theory.characters.CharPred;
import theory.intervals.UnaryCharIntervalSolver;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeoutException;

import static automata.Automaton.processInputString;
import static automata.sfa.complexEventPrediction.PSTree.travelPSTree;
import static automata.sfa.complexEventPrediction.WeightedDirectedGraphWithState4Embedding.getOutgoingEdges;
import static automata.sfa.complexEventPrediction.predictionFunction.*;
import static automata.sfa.complexEventPrediction.suffixTree.suffixTreeModel4Traffic;
import static automata.sfa.complexEventPrediction.trafficModeling.createPSTreeForWeather;

public class prediction {
    public static void prediction(int State, String inputFilePath, SFA<CharPred, Character> sfa, UnaryCharIntervalSolver ba, String[] trafficPredicates,
                                  int memThreadshold, double threshold, int lower, int upper) throws org.sat4j.specs.TimeoutException, TimeoutException, IOException {
        int forcastFre = 1;
        String fileNameWithExtension = getFileName(inputFilePath);
        String fileNameWithoutExtension = removeFileExtension(fileNameWithExtension);
        String modelData = "data_for_model";

        suffixTree.Tree<String> tree = suffixTreeModel4Traffic(modelData, memThreadshold + 1);
        PSTree.PSTTree<String> PSTree = createPSTreeForWeather(tree, trafficPredicates, memThreadshold + 1);

        StringBuilder sb = new StringBuilder();
        boolean intoPredicateFor = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            Collection<Integer> currentStates = new ArrayList<>();
            boolean and =false;
            currentStates.add(0);
            char[] andContent = {'e', 'f'};
            boolean[] andCon={false,false};

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String event = "";
                String trafficVolume = parts[2];
                String time = parts[0];
                int trafficVolumeInt;
                try {
                    trafficVolumeInt = Integer.parseInt(trafficVolume);
                } catch (NumberFormatException e) {
                    trafficVolumeInt = 0;
                }
                 if (trafficVolumeInt <= 50) event = "a";
                else if (trafficVolumeInt <= 60) event = "b";
                else if (trafficVolumeInt <= 70) event = "c";
                else if (trafficVolumeInt <= 80) event = "d";
                else if (trafficVolumeInt <= 90) event = "e";
                else if (trafficVolumeInt <= 100) event = "f";
                else if (trafficVolumeInt > 100) event = "g";
                else event = null;

                if (event != null) {
                    if (sb.length() == 0) {
                        sb.append(event);
                    } else {
                        if (sb.length() < memThreadshold) {
                            sb.append(event);
                        } else {
                            sb.append(event);
                            sb.deleteCharAt(0);
                        }

                        for (int i : currentStates){
                            if (i==2){
                                and=true;
                            }
                        }

                        if (and){
                            boolean andTrue=false;

                            int index = 0;
                            for (;index < andContent.length;index++){
                                if (event.equals(String.valueOf(andContent[index]))){
                                    andTrue=true;
                                    break;
                                }
                            }
                            if (andTrue){
                                if (!andCon[index]) {
                                    int nowState = 0;
                                    int afterState = 0;
                                    for (int i : currentStates) nowState = i;
                                    currentStates = processInputString(currentStates, sfa, ba, "#");
                                    for (int i : currentStates) afterState = i;
                                    System.out.println(nowState + "-" + event + ">" + afterState);
                                    andCon[index] = true;
                                }
                            }


                        }else{
                            int nowState =0;
                            int afterState=0;
                            for (int i : currentStates) nowState=i;
                            currentStates = processInputString(currentStates, sfa, ba, event);
                            for (int i : currentStates) afterState=i;
                            System.out.println(nowState+"-"+event+">"+afterState);
                        }
                        boolean finished = false;
                        for (int i : currentStates) {
                            if (i == State && !intoPredicateFor) {
                                intoPredicateFor = true;
                                String predictionResultPath = "filepath";
                                predictionFunction.Distributions dis = drawWaitingDistributionForOneState(sb.toString(), sfa, ba, PSTree.getRoot(), i, trafficPredicates,andCon,andContent);

                                Paths totalPaths = dis.getPaths();
                                double pro = 0;
                                double totalPro = 0;

                                for (path p : totalPaths.getPaths()) {
                                    totalPro = totalPro + p.pro;
                                    if (p.event.length() >= lower && p.event.length() <= upper) {
                                        pro = p.pro + pro;
                                    }
                                System.out.println(p.toString());
                                }

                                if (pro == 0) pro = 0.0001;
                                if (pro >= threshold) {
                                    String contentToAppend = "prediction_result";
                                    try (PrintWriter writer = new PrintWriter(new FileWriter(predictionResultPath, true))) {

                                        writer.println(contentToAppend);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    String contentToAppend = "prediction_result";
                                    try (PrintWriter writer = new PrintWriter(new FileWriter(predictionResultPath, true))) {
                                    writer.println(contentToAppend);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                forcastFre++;
                            }
                            if (sfa.getFinalStates().contains(i)) {
                                finished = true;

                                intoPredicateFor = false;

                                //重置AND部分
                                for (int j=0;j<andCon.length;j++){
                                    andCon[j]=false;
                                }
                                break;
                            }
                        }
                        if (finished) {
                            finished = false;
                            currentStates.clear();
                            currentStates.add(sfa.getInitialState());
                            and=false;
                        }
                    }
                }
            }
        }
    }

    public static predictionFunction.Distributions drawWaitingDistributionForOneState(String memory, SFA<CharPred, Character> sfa, UnaryCharIntervalSolver ba, PSTree.PSTTreeNode<String> root, int state, String[] trafficPredicates
            ,boolean[] andComponentBoo,char[] andComponentStr) throws TimeoutException, org.sat4j.specs.TimeoutException {
        predictionFunction.Distributions distributions = new predictionFunction.Distributions(state);
        int numBound = 0;
        if (state==3){
            numBound=1;
        }else {
            numBound=15;
        }

        Set<String> pathSet = new HashSet<>();
        for (List<Character> list : sfa.getMyWitnesses(ba, numBound, state)) {
            //=mem
            StringBuilder sb = new StringBuilder();
            sb.append(memory);
            List<Character> andPath =new ArrayList<>();
            double pro = 0;  int i = 0;
            if (list.size() <= 12) {
                String path = "";
                int countAND = 0;
                boolean enterAND=false;
                for (char c : list) {
                    if (c != '#') {
                        if (enterAND){
                            for (int k=0;k<andComponentBoo.length;k++){
                                if (!andComponentBoo[k]){
                                        andPath.add(andComponentStr[k]);
                                }
                            }
                            double maxPro =0;
                            char maxChar = 0;
                            for (char ch:andPath){
                                i=0;
                                for (; i < trafficPredicates.length; i++) {
                                    if (trafficPredicates[i].equals(String.valueOf(ch))) {
                                        break;
                                    }
                                }
                                double pro2 = travelPSTree(root, sb.toString(), i);
                                if (pro2 > maxPro){
                                    maxChar=ch;
                                    maxPro= pro2;
                                }
                            }
                            if (pro == 0) pro = maxPro;
                            else pro = pro * maxPro;

                            sb.append(maxChar);
                            sb.deleteCharAt(0);
                            for (;countAND>0;countAND--){
                                path = path + '#';
                            }
                            enterAND=false;
                        }

                        i =0;
                        for (; i < trafficPredicates.length; i++) {
                            if (trafficPredicates[i].equals(String.valueOf(c))) {
                                break;
                            }
                        }
                        try {
                            if (pro == 0) pro = travelPSTree(root, sb.toString(), i);
                            else pro = pro * travelPSTree(root, sb.toString(), i);
                        } catch (NullPointerException e) {
                            System.out.println("NullPointerException: " + e.getMessage());
                        }
                        sb.append(c);
                        sb.deleteCharAt(0);
                        path = path +c;

                    }else {
                        enterAND=true;
                        countAND++;
                    }
                }

                if (enterAND){
                    for (int k=0;k<andComponentBoo.length;k++){
                        if (!andComponentBoo[k]){
                            andPath.add(andComponentStr[k]);
                        }
                    }

                    double maxPro =0;
                    char maxChar = 0;
                    for (char ch:andPath){
                            i=0;
                        for (; i < trafficPredicates.length; i++) {
                            if (trafficPredicates[i].equals(String.valueOf(ch))) {
                                break;
                            }
                        }
                        double pro2 = travelPSTree(root, sb.toString(), i);
                        if (pro2 > maxPro){
                            maxChar=ch;
                            maxPro= pro2;
                        }
                    }
                    if (pro == 0) pro = maxPro;
                    else pro = pro * maxPro;

                    sb.append(maxChar);
                    sb.deleteCharAt(0);

                    for (;countAND>0;countAND--){
                        path = path + '#';
                    }
                    enterAND=false;
                }
                if (pro >= 0.0001) {
                    if (!pathSet.contains(path)) {
                        if (checkPathValid(path,state,sfa,ba)){
                            distributions.addIntoPaths(path, pro);
                            pathSet.add(path);

                        }
                    }
                }
            }
        }
        return  distributions;
    }

    public static Distributions drawWaitingDistributionForOneState2(SFA<CharPred, Character> sfa, UnaryCharIntervalSolver ba, WeightedDirectedGraphWithState4Embedding graphWithState) throws TimeoutException, org.sat4j.specs.TimeoutException {
        Distributions distributions = new Distributions(graphWithState.currentVertex.state);
        int numBound = 10;
        for (List<Character> list : sfa.getMyWitnesses(ba, numBound, graphWithState.currentVertex.state)){
            double  pro = 0;
            if (list.size() <= 12){
                WeightedDirectedGraphWithState4Embedding.Vertex paitialVertex= new WeightedDirectedGraphWithState4Embedding.Vertex(graphWithState.currentVertex.v,graphWithState.currentVertex.state);
                String path = "";
                for (Character c : list) {
                    List<WeightedDirectedGraphWithState4Embedding.Edge> edges = getOutgoingEdges(paitialVertex);
                    for (WeightedDirectedGraphWithState4Embedding.Edge edge : edges) {
                        if (edge.getStringAttribute().equals(String.valueOf(c))){
                            paitialVertex = edge.getDestination();
                            if (pro == 0) pro = (double) edge.getDoubleAttribute();
                            else pro = pro * (double) edge.getDoubleAttribute();
                            path = path + c;
                            break;
                        }
                    }
                }

                if (pro >= 0.0001) {
                    distributions.addIntoPaths(path, pro);
                }
            }
        }

        return distributions;
    }
    public static  Boolean checkPathValid(String path,int state,SFA<CharPred, Character> sfa,UnaryCharIntervalSolver ba) throws org.sat4j.specs.TimeoutException {
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
    public static void predictionWithoutDSFA(int State, String inputFilePath, SFA<CharPred, Character> sfa, UnaryCharIntervalSolver ba, WeightedDirectedGraphWithState4Embedding graph,
                                             int memThreadshold, double threshold,
                                             int lower, int upper) throws IOException {
        System.out.println(inputFilePath);
        int forcastFre = 1;
        String fileNameWithExtension = getFileName(inputFilePath);
        String fileNameWithoutExtension = removeFileExtension(fileNameWithExtension);
        String modelData = "model_data_path";
        StringBuilder sb = new StringBuilder();
        String predictionResultPath = "result_path";

        boolean intoPredicateFor = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String event = "";
                String trafficVolume = parts[2];
                int trafficVolumeInt;
                try {
                    trafficVolumeInt = Integer.parseInt(trafficVolume);
                } catch (NumberFormatException e) {
                    // Handle conversion failure
                    System.out.println("Invalid number format: " + trafficVolume);
                    trafficVolumeInt = 0;
                }
                if (graph.currentVertex == null) {
                    if (sb.length() < memThreadshold) { sb.append(event); } else { sb.append(event);sb.deleteCharAt(0); }
                    if (graph.findVerticesWithStateReturnByStr(sfa.getInitialState()).contains(sb.toString())){
                        WeightedDirectedGraphWithState4Embedding.Vertex initialVertex = new WeightedDirectedGraphWithState4Embedding.Vertex(sb.toString(),sfa.getInitialState());
                        graph.setCurrentVertex(initialVertex); } }

                else {
                    List<WeightedDirectedGraphWithState4Embedding.Edge> edges = getOutgoingEdges(graph.currentVertex);
                    for (WeightedDirectedGraphWithState4Embedding.Edge edge : edges)
                        if (edge.getStringAttribute().equals(event)) {
                            graph.currentVertex = edge.getDestination();
                            break;
                        }
                    if (graph.currentVertex.state==State && !intoPredicateFor){
                        intoPredicateFor = true;
                        Distributions dis1 = drawWaitingDistributionForOneState2(sfa,ba,graph);

                        Paths totalPaths = dis1.getPaths();
                        double pro = 0;
                        for (path p : totalPaths.getPaths()) {
                            if (p.event.length() >= lower && p.event.length() <= upper) {
                                pro = p.pro + pro;
                            }
                        }

                        if (pro >= threshold) {
                            String contentToAppend = "predicitonResult";
                            try (PrintWriter writer = new PrintWriter(new FileWriter(predictionResultPath, true))) {
                                //                                    // 第二个参数 true 表示追加而不是覆盖
                                    writer.println(contentToAppend);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            String contentToAppend = "predicitonResult";
                            try (PrintWriter writer = new PrintWriter(new FileWriter(predictionResultPath, true))) {
                                    writer.println(contentToAppend);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        forcastFre++;
                    }

                    if (sfa.getFinalStates().contains(graph.currentVertex.state)){
                        String buffer= graph.currentVertex.v;
                        List<String> list = graph.findVerticesWithStateReturnByStr(graph.currentVertex.state);

                        String result = findSubstringInList(buffer, list);
                        if (result!=null){
                            WeightedDirectedGraphWithState4Embedding.Vertex initialVertex = new WeightedDirectedGraphWithState4Embedding.Vertex(result,sfa.getInitialState());
                            graph.setCurrentVertex(initialVertex);
                        }else {
                            String randomString = getRandomStringFromList(list);
                            WeightedDirectedGraphWithState4Embedding.Vertex initialVertex = new WeightedDirectedGraphWithState4Embedding.Vertex(randomString,sfa.getInitialState());
                            graph.setCurrentVertex(initialVertex);
                        }
                        intoPredicateFor = false;
                    }
                }
            }
        } catch (org.sat4j.specs.TimeoutException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

}
