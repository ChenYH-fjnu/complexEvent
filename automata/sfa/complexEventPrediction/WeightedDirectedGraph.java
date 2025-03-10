package automata.sfa.complexEventPrediction;
import java.util.*;
import static  automata.sfa.complexEventPrediction.PSTree.*;


public  class WeightedDirectedGraph {
    private static Map<String, Map<String, Edge>> adjList;


    WeightedDirectedGraph() {
        adjList = new HashMap<>();
    }

    void addVertex(String v) {
        adjList.putIfAbsent(v, new HashMap<>());
    }

    void addEdge(String source, String destination, double weight, String attribute) {
        if (!adjList.containsKey(source))
            addVertex(source);
        if (!adjList.containsKey(destination))
            addVertex(destination);

        adjList.get(source).put(destination, new Edge(weight, attribute));
    }

    Double getEdgeWeight(String source, String destination) {
        Map<String, Edge> sourceNeighbors = adjList.get(source);
        if (sourceNeighbors == null || !sourceNeighbors.containsKey(destination)) {
            return -1.0;
        }

        Edge edge = sourceNeighbors.get(destination);
        return edge.getWeight();
    }

    void printGraph() {
        for (String vertex : adjList.keySet()) {
            System.out.print(vertex + " -> ");
            Map<String, Edge> neighbors = adjList.get(vertex);
            for (Map.Entry<String, Edge> entry : neighbors.entrySet()) {
                Edge edge = entry.getValue();
                System.out.print("(" + entry.getKey() + ", weight: " + edge.getWeight() + ", attribute: " + edge.getAttribute() + ") ");
            }
            System.out.println();
        }
    }


    public static Map<String, Edge> getOutEdgesWithoutState(String vertex) {
        return adjList.get(vertex);
    }

    public List<String> getVertex(){
        List<String> list =new LinkedList<String>();
        for (String vertex : adjList.keySet()) {
            list.add(vertex);
        }
        return list;
    }

    class Edge {
        private double weight;
        private String attribute;

        public Edge(double weight, String attribute) {
            this.weight = weight;
            this.attribute = attribute;
        }

        public double getWeight() {
            return weight;
        }

        public String getAttribute() {
            return attribute;
        }
    }

    public static double getPro(PSTTree<String> PSTree, String leaf, String predicaite,String[] weatherPredicates){
        double[] resultProbabilities = findProbabilities(PSTree, leaf);
        int i = 0;
        for (; i < weatherPredicates.length; i++) {
            if (weatherPredicates[i].equals(String.valueOf(predicaite))) {
                break;
            }
        }
        return resultProbabilities[i];
    }

    public static String removeFirstAndLast(String input) {

        if (input != null && input.length() > 1) {
            return input.substring(1, input.length() - 1);
        }
        return input;
    }

    public static List<String> parseStringRange(String input) {
        List<String> result = new ArrayList<>();
        int i = 0;
        while (i < input.length()) {
            char currentChar = input.charAt(i);
            if (i + 2 < input.length() && input.charAt(i + 1) == '-') {
                char start = currentChar;
                char end = input.charAt(i + 2);


                if (start <= end) {
                    for (char c = start; c <= end; c++) {
                        result.add(String.valueOf(c));
                    }
                }
                i += 3;
            } else {
                result.add(String.valueOf(currentChar));
                i++;
            }
        }

        return result;
    }


    public static WeightedDirectedGraph  transToPSA(PSTTree<String> PSTree, int memThreadShold,String[] weatherPredicate){
        List<String> leafNodes = PSTree.getAllLeafStrings();
        WeightedDirectedGraph graph = new WeightedDirectedGraph();
        for (String leaf: leafNodes) {
            if (!leaf.equals(PSTree.getRoot().getStr())) {
                for (String predicaite : weatherPredicate) {
                    if (leaf.length() < memThreadShold) {
                        String outGoingVertex = leaf + predicaite;
                        if (leafNodes.contains(outGoingVertex)) {
                            double pro = getPro(PSTree, leaf, predicaite,weatherPredicate);
                            graph.addEdge(leaf, outGoingVertex, pro, predicaite);
                        } else {
                            while (outGoingVertex.length() > 1) {
                                outGoingVertex = outGoingVertex.substring(1);
                                if (leafNodes.contains(outGoingVertex)) {
                                    double pro = getPro(PSTree, leaf, predicaite,weatherPredicate);
                                    graph.addEdge(leaf, outGoingVertex, pro, predicaite);
                                }
                            }
                        }
                    } else {
                        String outGoingVertex = leaf.substring(1) + predicaite;
                        if (leafNodes.contains(outGoingVertex)) {
                            double pro = getPro(PSTree, leaf, predicaite,weatherPredicate);
                            graph.addEdge(leaf, outGoingVertex, pro, predicaite);
                        }
                        else {
                            while (outGoingVertex.length() > 1) {
                                outGoingVertex = outGoingVertex.substring(1);
                                if (leafNodes.contains(outGoingVertex)) {
                                    double pro = getPro(PSTree, leaf, predicaite,weatherPredicate);
                                    graph.addEdge(leaf, outGoingVertex, pro, predicaite);
                                }
                            }
                        }
                    }
                }
            }
        }
        return graph;
    }
}

