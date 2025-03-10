package automata.sfa.complexEventPrediction;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static automata.sfa.complexEventPrediction.PSTree.*;

public class PSA {
    public static class WeightedDirectedGraph {
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
    }
    public static double getPro(PSTTree<String> PSTree, String leaf, String predicaite,String[] predicates){
        double[] resultProbabilities = findProbabilities(PSTree, leaf);
        int i = 0;
        for (; i < predicates.length; i++) {
            if (predicates[i].equals(String.valueOf(predicaite))) {
                break;
            }
        }
        return resultProbabilities[i];
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
