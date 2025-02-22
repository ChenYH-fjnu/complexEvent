package automata.sfa.complexEventPrediction;
import automata.sfa.SFA;
import theory.characters.CharPred;
import theory.intervals.UnaryCharIntervalSolver;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeoutException;
import static automata.Automaton.processInputString;
import static automata.sfa.complexEventPrediction.PSTree.*;

public class predictionFunction {
    public static class Distributions {

        int state;
        int[] pro = new int[12];
        String mem="";
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

        Paths paths = new Paths();
        public Distributions(int state) {
            setState(state);
        }
        public void addIntoPaths(String event, double pro) {
            paths.addPaths(event, pro);
        }
        public Paths getPaths() {
            return paths;
        }
        public void setPaths(Paths paths) {
            this.paths = paths;
        }


        @Override
        public String toString() {
            return "Distributions{" +
                    "State=" + getState() +
                    ", paths=" + paths.toString() +
                    '}';
        }
    }
    static class Paths {
        Collection<path> paths = new ArrayList<path>();

        public Collection<path> getPaths() {
            return paths;
        }

        public void setPaths(Collection<path> paths) {
            this.paths = paths;
        }

        public void addPaths(String event, double pro) {
            paths.add(new path(event, pro));
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            for (path p : paths) {
                result.append(p.toString()).append("\n");
            }
            return result.toString();
        }
    }

    static class path {
        String event;
        double pro;

        public path(String event, double pro) {
            this.event = event;
            this.pro = pro;
        }

        @Override
        public String toString() {
            return "path{" +
                    "event='" + event + '\'' +
                    ", pro=" + pro +
                    '}';
        }
    }


    public static String getFileName(String filePath) {
        File file = new File(filePath);
        return file.getName();
    }
    public static String removeFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }
    public static String getEventType(String weatherType) {
        String event = null;

        if (weatherType.equals("Snow")) {
            event = "a";
        } else if (weatherType.equals("Soft")) {
            event = "s";
        } else if (weatherType.equals("Fog")) {
            event = "c";
        } else if (weatherType.equals("Cold")) {
            event = "b";
        } else if (weatherType.equals("Hail")) {
            event = "d";
        } else if (weatherType.equals("Rain")) {
            event = "e";
        } else if (weatherType.equals("Storm")) {
            event = "f";
        } else if (weatherType.equals("Precipitation")) {
            event = "e";
        } else {
            event = null;
        }

        return event;
    }



    public static void prediction(int State, String inputFilePath,PSTTree<String> PSTree, SFA<CharPred, Character> sfa,double minProbability,UnaryCharIntervalSolver ba, String[] weatherPredicates,
                                  int memThreadshold, double threshold, int lower, int upper) throws org.sat4j.specs.TimeoutException, TimeoutException, IOException {

        int forcastFre = 1;
        StringBuilder sb = new StringBuilder();
        boolean intoPredicateFor = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            Collection<Integer> currentStates = new ArrayList<>();
            currentStates.add(1);
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String event = "";
                String weatherIndex = parts[0];
                String weatherType = parts[1];
                event = getEventType(weatherType);
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
                        currentStates = processInputString(currentStates, sfa, ba, event);
                        boolean finished = false;
                        for (int i : currentStates) {
                            if (i == State && !intoPredicateFor) {
                                intoPredicateFor = true;
                                String predictionResultPath = "your_Result_Path";
                                Distributions dis = drawWaitingDistributionForOneState(sb.toString(), sfa, ba, PSTree.getRoot(), i, weatherPredicates);
                                Paths totalPaths = dis.getPaths();
                                double pro = 0;
                                double totalPro = 0;

                                for (path p : totalPaths.getPaths()) {
                                    totalPro = totalPro + p.pro;
                                    if (p.event.length() >= lower && p.event.length() <= upper) {
                                        pro = p.pro + pro;
                                    }
                                }

                                if (pro == 0) pro = minProbability;
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
                                break;
                            }
                        }
                        if (finished) {
                            finished = false;
                            currentStates.clear();
                            currentStates.add(1);
                        }
                    }
                }
            }
        }
    }

    public static Distributions drawWaitingDistributionForOneState(String memory, SFA<CharPred, Character> sfa, UnaryCharIntervalSolver ba, PSTTreeNode<String> root, int state,String[] predicates) throws TimeoutException, org.sat4j.specs.TimeoutException {
        Distributions distributions = new Distributions(state);
       int numBound = 10;
        Set<String> pathSet = new HashSet<>();
        for (List<Character> list : sfa.getMyWitnesses(ba, numBound, state)) {

            StringBuilder sb = new StringBuilder();
            sb.append(memory);

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
                        if (pro == 0) pro = travelPSTree(root, sb.toString(), i);
                        else pro = pro * travelPSTree(root, sb.toString(), i);
                    } catch (NullPointerException e) {
                        System.out.println("NullPointerException: " + e.getMessage());
                    }
                    sb.append(c);
                    sb.deleteCharAt(0);
                    path = path + c;
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

    public static double travelPSTree2( PSTTree<String> PSTree,String memory,int predicatesIndex){
        double pro=0;
        String mem=memory;
        while (mem.length() > 0){
            if (PSTree.getAllLeafStrings().contains(mem)){
                break;
            }else  mem = mem.substring(1);
        }

        for (PSTTreeNode<String> e:PSTree.getAllLeafNodes()){
           if (e.getStr().equals(mem)){
               pro=e.getProbabilities(predicatesIndex);
               break;
           }
        }
        return pro;
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
    public static String findSubstringInList(String str, List<String> list) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        if (list.contains(str)) {
            return str;
        }
        return findSubstringInList(str.substring(1), list);
    }

    public static String getRandomStringFromList(List<String> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("The list cannot be null or empty");
        }

        Random random = new Random();
        int randomIndex = random.nextInt(list.size());
        return list.get(randomIndex);
    }


}
