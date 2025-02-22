package automata.sfa.complexEventPrediction;

import automata.sfa.SFA;
import automata.sfa.SFAInputMove;
import theory.characters.CharPred;


import java.util.*;

public class WeightedDirectedGraphWithState4Embedding {
     static Map<Vertex, Map<String, Edge>> adjListWithState;
     static Vertex currentVertex;
    public WeightedDirectedGraphWithState4Embedding() {
        adjListWithState = new HashMap<>();
    }
    public static void addVertex(String v, int state) {
        Vertex vertex = new Vertex(v, state);
        adjListWithState.putIfAbsent(vertex, new HashMap<>());
    }
    public List<Edge> getOutEdges(Vertex vertex) {
        List<Edge> outEdges = new ArrayList<>();

        if (!adjListWithState.containsKey(vertex)) {
            return outEdges;
        }

        Map<String, Edge> neighbors = adjListWithState.get(vertex);

        for (Map.Entry<String, Edge> neighborEntry : neighbors.entrySet()) {
            outEdges.add(neighborEntry.getValue());
        }

        return outEdges;
}

    public List<Edge> getAllEdges() {
        List<Edge> allEdges = new ArrayList<>();

        for (Map.Entry<Vertex, Map<String, Edge>> entry : adjListWithState.entrySet()) {
            for (Map.Entry<String, Edge> neighborEntry : entry.getValue().entrySet()) {
                allEdges.add(neighborEntry.getValue());
            }
        }

        return allEdges;
    }
    public Set<String> getAllVertices() {
        Set<String> leafSet = new HashSet<>();
        Set<Vertex> allVertices = adjListWithState.keySet();
        for (Vertex vertex : allVertices) {
            leafSet.add(vertex.v);
        }
        return leafSet;
    }

    public Set<Vertex> getAllVertices1() {
        Set<String> leafSet = new HashSet<>();
        Set<Vertex> allVertices = adjListWithState.keySet();
        return allVertices;
    }

    public void printGraph() {
        for (Vertex vertex : adjListWithState.keySet()) {
            System.out.print(vertex.v + "(" + vertex.state + ") -> ");
            Map<String, Edge> neighbors = adjListWithState.get(vertex);
            for (Map.Entry<String, Edge> entry : neighbors.entrySet()) {
                Edge edge = entry.getValue();
                System.out.print("("+"state "+edge.getDestination().state+", " + entry.getKey() + ", guard: " + edge.getStringAttribute() + ", pro: " + edge.getDoubleAttribute() + ") ");
            }
            System.out.println();
        }
    }

    static void initialVertex(WeightedDirectedGraph graph, SFA<CharPred, Character> sfa) {
        int stateNumber = sfa.getStates().size();
        List<String> list = graph.getVertex();
        for (String leaf : list) {
            for (int state:sfa.getStates()){
                addVertex(leaf, state);
            }
        }
    }
    public static List<Edge> getOutgoingEdges(Vertex vertex) {
        List<Edge> outgoingEdges = new ArrayList<>();
        Map<String, Edge> edges = adjListWithState.get(vertex);
        if (edges != null) {
            outgoingEdges.addAll(edges.values());
        }
        return outgoingEdges;
    }
    static void initialEdge(WeightedDirectedGraph graph, SFA<CharPred, Character> sfa) {
        List<String> list = graph.getVertex();
        for (int state:sfa.getStates()) {
            for (String leaf : list) {
                Map<String, WeightedDirectedGraph.Edge> outEdgesOfLeaf = WeightedDirectedGraph.getOutEdgesWithoutState(leaf);
                for (Map.Entry<String, WeightedDirectedGraph.Edge> entry : outEdgesOfLeaf.entrySet()) {
                    String destinationVertex = entry.getKey();
                    boolean stateChange = false;
                    WeightedDirectedGraph.Edge edge = entry.getValue();
                    for (SFAInputMove<CharPred, Character> move : sfa.getInputMovesFrom(state)) {
                        if (move.getGuard().isSatisfiedBy(edge.getAttribute().charAt(0))){
                            stateChange = true;
                            addEdge(leaf, state, destinationVertex, move.to, edge.getAttribute(), edge.getWeight());

                        }
                    }
                    if (!stateChange) {
                        addEdge(leaf, state, destinationVertex, state, edge.getAttribute(), edge.getWeight());
                    }
                }
            }
        }
    }
    public static void addEdge(String sourceV, int sourceState, String destinationV, int destinationState, String stringAttribute, double doubleAttribute) {
        addVertex(destinationV, destinationState);
        Vertex sourceVertex = new Vertex(sourceV, sourceState);
        Vertex destinationVertex = new Vertex(destinationV, destinationState);
        Edge e1 = new Edge(stringAttribute, doubleAttribute, sourceVertex, destinationVertex);
        adjListWithState.get(sourceVertex).put(destinationV, e1);
    }
    static class Vertex {
        String v;
        int state;

        Vertex(String v, int state) {
            this.v = v;
            this.state = state;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Vertex vertex = (Vertex) o;
            return state == vertex.state && Objects.equals(v, vertex.v);
        }

        @Override
        public int hashCode() {
            return Objects.hash(v, state);
        }
    }

    static class Edge {
        private String stringAttribute;
        private double doubleAttribute;
        private Vertex source; // 源顶点
        private Vertex destination; // 目标顶点

        public Edge(String stringAttribute, double doubleAttribute, Vertex source, Vertex destination) {
            this.stringAttribute = stringAttribute;
            this.doubleAttribute = doubleAttribute;
            this.source = source;
            this.destination = destination;
        }

        public String getStringAttribute() {
            return stringAttribute;
        }

        public double getDoubleAttribute() {
            return doubleAttribute;
        }

        public Vertex getSource() {
            return source;
        }

        public Vertex getDestination() {
            return destination;
        }
    }

    public void setCurrentVertex(Vertex vertex) {
        currentVertex = vertex;
    }

    public static boolean transition(String input) {
        Map<String, Edge> edges = adjListWithState.get(currentVertex);
        if (edges != null) {
            Edge edge = edges.get(input);
            if (edge != null) {
                currentVertex = edge.getDestination                               ();
                return true;
            }
        }
        return false;
    }

public static Object[] transition(Vertex currentVertex, String input) {
    Map<String, Edge> edges = adjListWithState.get(currentVertex);
    if (edges != null) {
        System.out.println("Edges found for current vertex.");
        Edge edge = edges.get(input);
        if (edge != null) {
            System.out.println("Transitioning to: " + edge.getDestination());
            System.out.println("Probability: " + edge.getDoubleAttribute());
            return new Object[]{edge.getDestination(), edge.getDoubleAttribute()};
        } else {
            System.out.println("No edge found for the given input.");
        }
    } else {
        System.out.println("No edges found for the current vertex.");
    }
    return null;
}

    public List<Vertex> findVerticesWithState(int state) {
        List<Vertex> verticesWithState = new ArrayList<>();
        for (Vertex vertex : adjListWithState.keySet()) {
            if (vertex.state == state) {
                verticesWithState.add(vertex);
            }
        }
        return verticesWithState;
    }
    public List<String> findVerticesWithStateReturnByStr(int state) {
        List<String> verticesWithState = new ArrayList<>();
        for (Vertex vertex : adjListWithState.keySet()) {
            if (vertex.state == state) {
                verticesWithState.add(vertex.v);
            }
        }
        return verticesWithState;
    }

    public static WeightedDirectedGraphWithState4Embedding createEmbedding(WeightedDirectedGraph graph, SFA<CharPred, Character> sfa) {
        WeightedDirectedGraphWithState4Embedding graphWithState = new WeightedDirectedGraphWithState4Embedding();
        initialVertex(graph, sfa);
        initialEdge(graph, sfa);
        return graphWithState;
    }
}
