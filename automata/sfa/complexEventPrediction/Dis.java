package automata.sfa.complexEventPrediction;

import automata.sfa.SFA;
import automata.sfa.SFAInputMove;
import org.sat4j.specs.TimeoutException;
import theory.characters.CharPred;
import theory.intervals.UnaryCharIntervalSolver;
import java.util.List;
import java.util.*;
import automata.sfa.complexEventPrediction.PSTree.*;
import static automata.sfa.complexEventPrediction.PSTree.travelPSTree;
import static automata.sfa.complexEventPrediction.function.checkPathValid;


public class Dis {
    int state;
    double[] pro = new double[12];
    String mem = "";

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getMem() {
        return mem;
    }

    public void setMem(String mem) {
        this.mem = mem;
    }

    public void setPro(double[] pro) {
        this.pro = pro;
    }

    public double[] getPro() {
        return pro;
    }

    public static Dis findDistribution(List<Dis> distributions, int targetState, String targetMem,PSTTree<String> PSTree) {
        List<String> leafsFromTree = PSTree.getAllLeaf();
        while (targetMem.length()>0){
            if (leafsFromTree.contains(targetMem)) break;
            else  targetMem = targetMem.substring(1);
        }
        for (Dis dist : distributions) {
            if (dist.getState() == targetState && dist.getMem().equals(targetMem)) {
                return dist;
            }
        }
        return null;
    }


    public static List<String> permute(String[] nums) {
        List<String> result = new ArrayList<>();
        backtrack(nums, result, new StringBuilder(), new boolean[nums.length]);
        return result;
    }


    private static void backtrack(String[] nums, List<String> result, StringBuilder current, boolean[] used) {

        if (current.length() == nums.length) {
            result.add(current.toString());
            return;
        }


        for (int i = 0; i < nums.length; i++) {

            if (used[i]) continue;


            used[i] = true;
            current.append(nums[i]);

            backtrack(nums, result, current, used);

            used[i] = false;
            current.deleteCharAt(current.length() - 1);
        }
    }

    public static String[] findShortestString(SFA<CharPred, Character> sfa, int state) throws TimeoutException {
        UnaryCharIntervalSolver ba = new UnaryCharIntervalSolver();
        List<Character> shortestPath = null;


        for (List<Character> list : sfa.getMyWitnesses(ba, 5, state)) {
            if (shortestPath == null || list.size() < shortestPath.size()) {
                shortestPath = list;
            }
        }

        if (shortestPath != null) {
            String[] result = new String[shortestPath.size()];
            for (int i = 0; i < shortestPath.size(); i++) {
                result[i] = shortestPath.get(i).toString();
            }
            return result;
        }

        return new String[]{};
    }

    public static void postPredictResult(Dis dis, int upper, double threshold ){
            double[] getPro = dis.getPro();
            double pro=0;
            String predictionResultPath = "";
            for (int i = 0; i<= upper; i++){
                    pro = pro + getPro[i];
            }
            if (pro >= threshold){
                //Write your result
            }else{
                //Write your result
            }
    }

    public static void postPredictResult(Graph graphWithState, int upper, double threshold ){
        double pro=0;
        List<Map.Entry<List<Graph.Vertex>, Double>> pathsWithProbabilities = graphWithState.findPathsToState(graphWithState.getFinalState());
        for (Map.Entry<List<Graph.Vertex>, Double> entry : pathsWithProbabilities) {
            List<Graph.Vertex> path = entry.getKey();
            double probability = entry.getValue();
            if (path.size()-1 <=upper) pro = pro +probability;
        }

        if (pro >= threshold){
            //Write your result
        }else{
            //Write your result
        }
    }


    public static List<Dis> countDis(PSTTree<String> pstree, String[] predicates, SFA<CharPred, Character> sfa,
                                     Boolean existAnd, List<String[]> andParts, int[] andLength, int[] andBeginIndex, String[] symbols) throws TimeoutException {
        List<String> leafs = pstree.getAllLeaf();
        UnaryCharIntervalSolver ba = new UnaryCharIntervalSolver();
        List<Dis> Diss = new ArrayList<>();
        if (existAnd) {
            for (int state : sfa.getStates()) {
                if (!sfa.getFinalStates().contains(state)) {
                    String[] shortestPath = findShortestString(sfa, state);
                    int finalIndex = 0;
                    Boolean andYet = false;

                    int indexNow = 0;
                    for (; indexNow < andParts.size(); indexNow++) {
                        finalIndex = andBeginIndex[indexNow] + andLength[indexNow] - 1;
                        if (state > andBeginIndex[indexNow] && state <= finalIndex) {
                            andYet = true;
                            break;
                        }
                    }
                    if (andYet) {
                        for (String leaf : leafs) {
                            double pro = 0;
                            Dis dis = new Dis();
                            dis.setState(state);
                            dis.setMem(leaf);
                            StringBuilder sb = new StringBuilder();
                            sb.append(leaf);
                            List<String> restAndPart = new LinkedList<>();
                            int restMatch4ThisEnd = finalIndex - state + 1;
                            for (int i = 0; i < andParts.get(indexNow).length; i++) {
                                if (!leaf.contains(andParts.get(indexNow)[i]))
                                    restAndPart.add(andParts.get(indexNow)[i]);
                            }
                            for (String s : restAndPart) {
                                int indexInPredicate = 0;
                                for (; indexInPredicate < predicates.length; indexInPredicate++) {
                                    if (predicates[indexInPredicate].equals(s)) ;
                                    break;
                                }
                                if (travelPSTree(pstree.getRoot(), leaf, indexInPredicate) > pro)
                                    pro = travelPSTree(pstree.getRoot(), leaf, indexInPredicate);
                            }

                            for (int i = restMatch4ThisEnd; i < shortestPath.length; i++) {
                                if (Character.isUpperCase(shortestPath[i].charAt(0)) || Character.isLowerCase(shortestPath[i].charAt(0))) {
                                    int indexInPredicate = 0;
                                    for (; indexInPredicate < predicates.length; indexInPredicate++) {
                                        if (predicates[indexInPredicate].equals(shortestPath[i])) ;
                                        break;
                                    }
                                    if (pro == 0) pro = travelPSTree(pstree.getRoot(), sb.toString(), indexInPredicate);
                                    else pro = pro * travelPSTree(pstree.getRoot(), sb.toString(), indexInPredicate);

                                    sb.append(predicates[indexInPredicate]);
                                    sb.deleteCharAt(0);
                                }


                                else {
                                    int AndIndex = 0;
                                    for (; AndIndex < symbols.length; i++) {
                                        if (symbols[AndIndex].equals(shortestPath[i])) {
                                            break;
                                        }
                                    }

                                    int length = andLength[AndIndex];
                                    if (length > 2) {
                                        List<Integer> matchReady = new LinkedList<>();
                                        for (int maxLength = length; maxLength > 2; maxLength--) {
                                            int maxProInAnd = 0;
                                            double partialMaxPro = 0;
                                            int predicateInAnd = 0;
                                            for (; predicateInAnd < andLength[AndIndex]; predicateInAnd++) {
                                                if (!matchReady.contains(predicateInAnd)) {
                                                    int indexInPredicate = 0;
                                                    for (; indexInPredicate < predicates.length; indexInPredicate++) {
                                                        if (predicates[indexInPredicate].equals(andParts.get(AndIndex)[predicateInAnd]))
                                                            ;
                                                        break;
                                                    }
                                                    if (travelPSTree(pstree.getRoot(), sb.toString(), indexInPredicate) > partialMaxPro) {
                                                        partialMaxPro = travelPSTree(pstree.getRoot(), sb.toString(), indexInPredicate);
                                                        maxProInAnd = predicateInAnd;
                                                    }

                                                }
                                            }
                                            matchReady.add(maxProInAnd);
                                            if (pro == 0) pro = partialMaxPro;
                                            else pro = pro * partialMaxPro;
                                            sb.append(andParts.get(AndIndex)[maxProInAnd]);
                                            sb.deleteCharAt(0);
                                        }
                                        List<String> restEvent = new LinkedList<>();
                                        for (int l = 0; l < andParts.get(AndIndex).length; l++) {
                                            if (!matchReady.contains(l)) restEvent.add(andParts.get(AndIndex)[l]);
                                        }
                                        String[] array = restEvent.toArray(new String[0]);
                                        List<String> partialPaths = permute(array);
                                        double twoPathsPro = 0;
                                        for (String s : partialPaths) {
                                            double partPro = 0;
                                            String memRightNow = sb.toString();
                                            for (int j = 0; j < s.length(); j++) {
                                                int predicateIndex = 0;
                                                for (; predicateIndex < predicates.length; predicateIndex++) {
                                                    if (predicates[predicateIndex].equals(String.valueOf(s.toCharArray()[j]))) {
                                                        break;
                                                    }
                                                    if (partPro == 0)
                                                        partPro = travelPSTree(pstree.getRoot(), memRightNow, predicateIndex);
                                                    else
                                                        partPro = partPro * travelPSTree(pstree.getRoot(), memRightNow, predicateIndex);
                                                    memRightNow = memRightNow + predicates[predicateIndex];
                                                    memRightNow = memRightNow.substring(1);
                                                }
                                            }
                                            twoPathsPro = twoPathsPro + partPro;
                                        }
                                        pro = pro * twoPathsPro;
                                    } else {
                                        List<String> paths = permute(andParts.get(AndIndex));
                                        double twoPathPro = 0;
                                        for (String s : paths) {
                                            double partPro = 0;
                                            String mem = sb.toString();
                                            for (int j = 0; j < s.length(); j++) {
                                                int predicateIndex = 0;
                                                for (; predicateIndex < predicates.length; predicateIndex++) {
                                                    if (predicates[predicateIndex].equals(String.valueOf(s.toCharArray()[j]))) {
                                                        break;
                                                    }
                                                    if (partPro == 0)
                                                        partPro = travelPSTree(pstree.getRoot(), mem, predicateIndex);
                                                    else
                                                        partPro = partPro * travelPSTree(pstree.getRoot(), mem, predicateIndex);
                                                    mem = mem + predicates[predicateIndex];
                                                    mem = mem.substring(1);
                                                }
                                            }
                                            twoPathPro = twoPathPro + partPro;
                                        }
                                        if (pro == 0) pro = twoPathPro;
                                        else pro = pro * twoPathPro;

                                    }
                                    i = i + length - 1;
                                }
                            }

                            double[] paths = new double[13];
                            paths[shortestPath.length] = pro;
                            dis.setPro(paths);
                            Diss.add(dis);
                        }
                    }

                    else {
                        for (String leaf : leafs) {
                            Dis dis = new Dis();
                            dis.setState(state);
                            dis.setMem(leaf);
                            double pro = 0;
                            StringBuilder sb = new StringBuilder();
                            sb.append(leaf);
                            for (int i = 0; i < shortestPath.length; i++) {
                                if (Character.isUpperCase(shortestPath[i].charAt(0)) || Character.isLowerCase(shortestPath[i].charAt(0))) {
                                    int indexInPredicate = 0;
                                    for (; indexInPredicate < predicates.length; indexInPredicate++) {
                                        if (predicates[indexInPredicate].equals(shortestPath[i])) ;
                                        break;
                                    }
                                    if (pro == 0) pro = travelPSTree(pstree.getRoot(), sb.toString(), indexInPredicate);
                                    else pro = pro * travelPSTree(pstree.getRoot(), sb.toString(), indexInPredicate);
                                    sb.append(predicates[indexInPredicate]);
                                    sb.deleteCharAt(0);
                                }
                                else {
                                    int AndIndex = 0;
                                    for (; AndIndex < symbols.length; i++) {
                                        if (symbols[AndIndex].equals(shortestPath[i])) {
                                            break;
                                        }
                                    }

                                    int length = andLength[AndIndex];
                                    if (length > 2) {
                                        List<Integer> matchReady = new LinkedList<>();
                                        for (int maxLength = length; maxLength > 2; maxLength--) {
                                            int maxProInAnd = 0;
                                            double partialMaxPro = 0;
                                            int predicateInAnd = 0;
                                            for (; predicateInAnd < andLength[AndIndex]; predicateInAnd++) {
                                                if (!matchReady.contains(predicateInAnd)) {
                                                    int indexInPredicate = 0;
                                                    for (; indexInPredicate < predicates.length; indexInPredicate++) {
                                                        if (predicates[indexInPredicate].equals(andParts.get(AndIndex)[predicateInAnd]))
                                                            ;
                                                        break;
                                                    }
                                                    if (travelPSTree(pstree.getRoot(), sb.toString(), indexInPredicate) > partialMaxPro) {
                                                        partialMaxPro = travelPSTree(pstree.getRoot(), sb.toString(), indexInPredicate);
                                                        maxProInAnd = predicateInAnd;
                                                    }

                                                }
                                            }
                                            matchReady.add(maxProInAnd);
                                            if (pro == 0) pro = partialMaxPro;
                                            else pro = pro * partialMaxPro;
                                            sb.append(andParts.get(AndIndex)[maxProInAnd]);
                                            sb.deleteCharAt(0);
                                        }
                                        List<String> restEvent = new LinkedList<>();
                                        for (int l = 0; l < andParts.get(AndIndex).length; l++) {
                                            if (!matchReady.contains(l)) restEvent.add(andParts.get(AndIndex)[l]);
                                        }
                                        String[] array = restEvent.toArray(new String[0]);
                                        List<String> partialPaths = permute(array);
                                        double twoPathsPro = 0;
                                        for (String s : partialPaths) {
                                            double partPro = 0;
                                            String memRightNow = sb.toString();
                                            for (int j = 0; j < s.length(); j++) {
                                                int predicateIndex = 0;
                                                for (; predicateIndex < predicates.length; predicateIndex++) {
                                                    if (predicates[predicateIndex].equals(String.valueOf(s.toCharArray()[j]))) {
                                                        break;
                                                    }
                                                    if (partPro == 0)
                                                        partPro = travelPSTree(pstree.getRoot(), memRightNow, predicateIndex);
                                                    else
                                                        partPro = partPro * travelPSTree(pstree.getRoot(), memRightNow, predicateIndex);
                                                    memRightNow = memRightNow + predicates[predicateIndex];
                                                    memRightNow = memRightNow.substring(1);
                                                }
                                            }
                                            twoPathsPro = twoPathsPro + partPro;
                                        }
                                        pro = pro * twoPathsPro;
                                    } else {
                                        List<String> paths = permute(andParts.get(AndIndex));
                                        double twoPathPro = 0;
                                        for (String s : paths) {
                                            double partPro = 0;
                                            String mem = sb.toString();
                                            for (int j = 0; j < s.length(); j++) {
                                                int predicateIndex = 0;
                                                for (; predicateIndex < predicates.length; predicateIndex++) {
                                                    if (predicates[predicateIndex].equals(String.valueOf(s.toCharArray()[j]))) {
                                                        break;
                                                    }
                                                    if (partPro == 0)
                                                        partPro = travelPSTree(pstree.getRoot(), mem, predicateIndex);
                                                    else
                                                        partPro = partPro * travelPSTree(pstree.getRoot(), mem, predicateIndex);
                                                    mem = mem + predicates[predicateIndex];
                                                    mem = mem.substring(1);
                                                }
                                            }
                                            twoPathPro = twoPathPro + partPro;
                                        }

                                        if (pro == 0) pro = twoPathPro;
                                        else pro = pro * twoPathPro;

                                    }
                                    i = i + length - 1;
                                }
                            }
                            double[] paths = new double[13];
                            paths[shortestPath.length] = pro;
                            dis.setPro(paths);
                            Diss.add(dis);
                        }
                    }
                }
            }

        } else {
            for (int state : sfa.getStates()) {
                if (!sfa.getFinalStates().contains(state)) {
                    for (String leaf : leafs) {
                        int numBound = 10;
                        Dis dis = new Dis();
                        dis.setState(state);
                        dis.setMem(leaf);

                        double pro4Path[] = new double[13];
                        Set<String> pathSet = new HashSet<>();
                        for (List<Character> list : sfa.getMyWitnesses(ba, numBound, state)) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(leaf);
                            double pro = 0;
                            if (list.size() <= 12) {
                                String path = "";
                                for (char c : list) {
                                    int i = 0;
                                    for (; i < predicates.length; i++) {
                                        if (predicates[i].equals(String.valueOf(c))) {
                                            break;
                                        }
                                    }
                                    try {
                                        if (pro == 0) pro = travelPSTree(pstree.getRoot(), sb.toString(), i);
                                        else pro = pro * travelPSTree(pstree.getRoot(), sb.toString(), i);
                                    } catch (NullPointerException e) {
                                        System.out.println("NullPointerException: " + e.getMessage());
                                    }
                                    sb.append(c);
                                    sb.deleteCharAt(0);
                                    path = path + c;
                                }

                                if (pro >= 0.0001) {
                                    if (!pathSet.contains(path)) {
                                        if (checkPathValid(path, state, sfa, ba)) {
                                            pro4Path[path.length()] = pro4Path[path.length()] + pro;
                                        }
                                    }
                                }

                            }
                        }
                        dis.setPro(pro4Path);
                        Diss.add(dis);
                    }
                }
            }
        }
        return Diss;
    }
}