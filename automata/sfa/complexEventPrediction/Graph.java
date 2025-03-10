package automata.sfa.complexEventPrediction;

import automata.sfa.SFA;
import automata.sfa.SFAInputMove;
import theory.characters.CharPred;
import java.util.*;
import static automata.sfa.complexEventPrediction.WeightedDirectedGraph.parseStringRange;
import static automata.sfa.complexEventPrediction.WeightedDirectedGraph.removeFirstAndLast;
import static automata.sfa.complexEventPrediction.function.*;


public class Graph {
    static Vertex currentVertex;
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

    public Vertex findVertex(String v, int state) {
        for (Vertex vertex : adjacencyList.keySet()) {
            if (vertex.v.equals(v) && vertex.state == state) {
                return vertex;
            }
        }
        return null;
    }
    static int initialState;

    public static int getFinalState() {
        return finalState;
    }

    public static void setFinalState(int finalState) {
        Graph.finalState = finalState;
    }

    static int finalState;

    public static int getInitialState() {
        return initialState;
    }

    public static void setInitialState(int initialState) {
        Graph.initialState = initialState;
    }

    public static Vertex getCurrentVertex() {
        return currentVertex;
    }

    public static void setCurrentVertex(Vertex currentVertex) {
        Graph.currentVertex = currentVertex;
    }

    static class Edge {
        private String guard;
        private double pro;
        private Vertex source;
        private Vertex destination;



        public Edge(String guard, double pro, Vertex source, Vertex destination) {
            this.guard = guard;
            this.pro = pro;
            this.source = source;
            this.destination = destination;
        }

        public String getGuard() {
            return guard;
        }

        public void setGuard(String guard) {
            this.guard = guard;
        }

        public double getPro() {
            return pro;
        }

        public void setPro(double pro) {
            this.pro = pro;
        }

        public Vertex getSource() {
            return source;
        }

        public void setSource(Vertex source) {
            this.source = source;
        }

        public Vertex getDestination() {
            return destination;
        }

        public void setDestination(Vertex destination) {
            this.destination = destination;
        }
    }
    private Map<Vertex, List<Edge>> adjacencyList;

    public Graph() {
        adjacencyList = new HashMap<>();
    }


    public void addVertex(Vertex vertex) {
        if (!adjacencyList.containsKey(vertex)) {
            adjacencyList.put(vertex, new ArrayList<>());
        }
    }


    public void addEdge(Edge edge) {
        Vertex source = edge.getSource();
        Vertex destination = edge.getDestination();
        addVertex(source);
        addVertex(destination);
        adjacencyList.get(source).add(edge);
    }


    public List<Edge> getEdges(Vertex vertex) {
        return adjacencyList.getOrDefault(vertex, new ArrayList<>());
    }


    public Set<Vertex> getVertices() {
        return adjacencyList.keySet();
    }


    public List<Edge> getAllEdges() {
        List<Edge> edges = new ArrayList<>();
        for (List<Edge> edgeList : adjacencyList.values()) {
            edges.addAll(edgeList);
        }
        return edges;
    }

    public void printGraph() {
        for (Map.Entry<Vertex, List<Edge>> entry : adjacencyList.entrySet()) {
            Vertex vertex = entry.getKey();
            List<Edge> edges = entry.getValue();
            System.out.print("Vertex " + vertex.v + " (" + vertex.state + ") -> ");
            for (Edge edge : edges) {
                System.out.print("[" + edge.getDestination().v + " (" + edge.getDestination().state + "), " +
                        "String: " + edge.getGuard() + ", Double: " + edge.getPro() + "] ");
            }
            System.out.println();
        }
    }


    public static Graph createEmbedding(String[] predicates, WeightedDirectedGraph graph, SFA<CharPred, Character> sfa,
                                          Boolean existAnd, List<String[]> andParts, int[] andLength, int[] andBeginIndex, String[] symbols) {
        Graph graphWithState = new Graph();
        if (existAnd){
            int andContentNum = andParts.size();
            List<Integer> andStateList = new LinkedList<>();
            for (int i = 0; i < andContentNum; i++){
                for (int andStateCurrent = andBeginIndex[i]; andStateCurrent < andBeginIndex[i] + andLength[i]; andStateCurrent++){
                    andStateList.add(andStateCurrent);


                    int memToSee = andStateCurrent - andBeginIndex[i];
                    for (SFAInputMove<CharPred, Character> move : sfa.getInputMovesFrom(andStateCurrent)){
                        List<String> guard4ThisMove = parseStringRange(removeFirstAndLast(move.getGuard().toString()));


                        for (String guard : guard4ThisMove) {
                            if (guard.equals(symbols[i])) {
                                for (String leafNodeFromPSA : graph.getVertex()) {
                                    List<String> saveEdge = getLastNCharactersAsList(leafNodeFromPSA, memToSee);
                                    for (String andPartCharecter : andParts.get(i)) {
                                        if (saveEdge.size() != 0) {
                                            if (!saveEdge.contains(andPartCharecter)) {
                                                Map<String, WeightedDirectedGraph.Edge> outEdgesOfLeaf = WeightedDirectedGraph.getOutEdgesWithoutState(leafNodeFromPSA);
                                                for (Map.Entry<String, WeightedDirectedGraph.Edge> entry : outEdgesOfLeaf.entrySet()) {
                                                    String destinationVertex = entry.getKey();
                                                    WeightedDirectedGraph.Edge edge = entry.getValue();
                                                    if (edge.getAttribute().equals(andPartCharecter)) {
                                                        Vertex souce = new Vertex(leafNodeFromPSA, andStateCurrent);
                                                        Vertex des = new Vertex(destinationVertex, move.to);
                                                        Edge e1 = new Edge(edge.getAttribute(), edge.getWeight(), souce, des);
                                                        graphWithState.addEdge(e1);

                                                    }
                                                }
                                            }
                                        } else {
                                            Map<String, WeightedDirectedGraph.Edge> outEdgesOfLeaf = WeightedDirectedGraph.getOutEdgesWithoutState(leafNodeFromPSA);
                                            for (Map.Entry<String, WeightedDirectedGraph.Edge> entry : outEdgesOfLeaf.entrySet()) {
                                                String destinationVertex = entry.getKey();
                                                WeightedDirectedGraph.Edge edge = entry.getValue();
                                                if (edge.getAttribute().equals(andPartCharecter)) {
                                                    Vertex souce = new Vertex(leafNodeFromPSA, andStateCurrent);
                                                    Vertex des = new Vertex(destinationVertex, move.to);
                                                    Edge e1 = new Edge(edge.getAttribute(), edge.getWeight(), souce, des);
                                                    graphWithState.addEdge(e1);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else if (isLowerCase(guard)) {
                                for (String leafNodeFromPSA : graph.getVertex()) {
                                    Map<String, WeightedDirectedGraph.Edge> outEdgesOfLeaf = WeightedDirectedGraph.getOutEdgesWithoutState(leafNodeFromPSA);
                                    for (Map.Entry<String, WeightedDirectedGraph.Edge> entry : outEdgesOfLeaf.entrySet()) {
                                        String destinationVertex = entry.getKey();
                                        WeightedDirectedGraph.Edge edge = entry.getValue();
                                        if (edge.getAttribute().equals(toUpperCase(guard))) {
                                            Vertex souce = new Vertex(leafNodeFromPSA, andStateCurrent);
                                            Vertex des = new Vertex(destinationVertex, move.to);
                                            Edge e1 = new Edge(guard, edge.getWeight(), souce, des);
                                            graphWithState.addEdge(e1);
                                        }
                                    }
                                }
                            }

                            else {
                                for (String leafNodeFromPSA : graph.getVertex()) {

                                    Map<String, WeightedDirectedGraph.Edge> outEdgesOfLeaf = WeightedDirectedGraph.getOutEdgesWithoutState(leafNodeFromPSA);
                                    for (Map.Entry<String, WeightedDirectedGraph.Edge> entry : outEdgesOfLeaf.entrySet()) {
                                        String destinationVertex = entry.getKey();
                                        WeightedDirectedGraph.Edge edge = entry.getValue();
                                        if (edge.getAttribute().equals(guard)) {
                                            Vertex souce = new Vertex(leafNodeFromPSA, andStateCurrent);
                                            Vertex des = new Vertex(destinationVertex, move.to);
                                            Edge e1 = new Edge(edge.getAttribute(), edge.getWeight(), souce, des);
                                            graphWithState.addEdge(e1);
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }
            for (int state:sfa.getStates()){
                if (!andStateList.contains(state)){
                    List<String> stateChangeGuard = new LinkedList<>();
                    for (SFAInputMove<CharPred, Character> move : sfa.getInputMovesFrom(state)) {
                        List<String> guard4ThisMove = parseStringRange(removeFirstAndLast( move.getGuard().toString()));
                        stateChangeGuard.addAll(guard4ThisMove);
                        for (String guard:guard4ThisMove) {
                            for (String leafNodeFromPSA : graph.getVertex()) {
                                Map<String, WeightedDirectedGraph.Edge> outEdgesOfLeaf = WeightedDirectedGraph.getOutEdgesWithoutState(leafNodeFromPSA);
                                for (Map.Entry<String, WeightedDirectedGraph.Edge> entry : outEdgesOfLeaf.entrySet()) {
                                    String destinationVertex = entry.getKey();
                                    WeightedDirectedGraph.Edge edge = entry.getValue();
                                    if (edge.getAttribute().equals(guard)) {

                                        Vertex souce = new Vertex(leafNodeFromPSA, state);
                                        Vertex des = new Vertex(destinationVertex, move.to);
                                        Edge e1 = new Edge(edge.getAttribute(), edge.getWeight(), souce, des);
                                        graphWithState.addEdge(e1);
                                    }
                                }
                            }
                        }
                    }
                    if (stateChangeGuard.size()>0){
                        for (String remainStr:predicates){
                            if (!stateChangeGuard.contains(remainStr)){
                                for (String leafNodeFromPSA : graph.getVertex()) {
                                    Map<String, WeightedDirectedGraph.Edge> outEdgesOfLeaf = WeightedDirectedGraph.getOutEdgesWithoutState(leafNodeFromPSA);
                                    for (Map.Entry<String, WeightedDirectedGraph.Edge> entry : outEdgesOfLeaf.entrySet()) {
                                        String destinationVertex = entry.getKey();
                                        WeightedDirectedGraph.Edge edge = entry.getValue();
                                        if (edge.getAttribute().equals(remainStr)) {

                                            Vertex souce = new Vertex(leafNodeFromPSA, state);
                                            Vertex des = new Vertex(destinationVertex, state);
                                            Edge e1 = new Edge(edge.getAttribute(), edge.getWeight(), souce, des);
                                            graphWithState.addEdge(e1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        else {
            for (int fromState:sfa.getStates()) {
                List<String> stateChangeGuard = new LinkedList<>();
                for (SFAInputMove<CharPred, Character> move : sfa.getInputMovesFrom(fromState)) {
                    List<String> guard4ThisMove = parseStringRange(removeFirstAndLast( move.getGuard().toString()));
                    stateChangeGuard.addAll(guard4ThisMove);
                    for (String guard:guard4ThisMove) {
                        for (String leafNodeFromPSA : graph.getVertex()) {
                            Map<String, WeightedDirectedGraph.Edge> outEdgesOfLeaf = WeightedDirectedGraph.getOutEdgesWithoutState(leafNodeFromPSA);
                            for (Map.Entry<String, WeightedDirectedGraph.Edge> entry : outEdgesOfLeaf.entrySet()) {
                                String destinationVertex = entry.getKey();
                                WeightedDirectedGraph.Edge edge = entry.getValue();
                                if (edge.getAttribute().equals(guard)) {
                                    Vertex souce = new Vertex(leafNodeFromPSA, fromState);
                                    Vertex des = new Vertex(destinationVertex, move.to);
                                    Edge e1 = new Edge(edge.getAttribute(), edge.getWeight(), souce, des);
                                    graphWithState.addEdge(e1);
                                }
                            }
                        }
                    }
                }
                if (stateChangeGuard.size() > 0){
                    for (String remainStr:predicates){
                        if (!stateChangeGuard.contains(remainStr)){
                            for (String leafNodeFromPSA : graph.getVertex()) {
                                Map<String, WeightedDirectedGraph.Edge> outEdgesOfLeaf = WeightedDirectedGraph.getOutEdgesWithoutState(leafNodeFromPSA);
                                for (Map.Entry<String, WeightedDirectedGraph.Edge> entry : outEdgesOfLeaf.entrySet()) {
                                    String destinationVertex = entry.getKey();
                                    WeightedDirectedGraph.Edge edge = entry.getValue();
                                    if (edge.getAttribute().equals(remainStr)) {
                                        Vertex souce = new Vertex(leafNodeFromPSA, fromState);
                                        Vertex des = new Vertex(destinationVertex,  fromState);
                                        Edge e1 = new Edge(edge.getAttribute(), edge.getWeight(), souce, des);
                                        graphWithState.addEdge(e1);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return graphWithState;
    }


    public void removeNodesWithNoIncomingEdges(int startState) {
        Set<Vertex> startNodes = new HashSet<>();
        for (Vertex vertex : adjacencyList.keySet()) {
            if (vertex.state == startState) {
                startNodes.add(vertex);
            }
        }

        Map<Vertex, Integer> inDegree = new HashMap<>();
        for (Vertex vertex : adjacencyList.keySet()) {
            inDegree.put(vertex, 0);
        }
        for (List<Edge> edges : adjacencyList.values()) {
            for (Edge edge : edges) {
                Vertex destination = edge.getDestination();
                inDegree.put(destination, inDegree.get(destination) + 1);
            }
        }
        Set<Vertex> toRemove = new HashSet<>();
        for (Vertex vertex : inDegree.keySet()) {
            if (inDegree.get(vertex) == 0 && !startNodes.contains(vertex)) {
                toRemove.add(vertex);
            }
        }

        for (Vertex vertex : toRemove) {
            adjacencyList.remove(vertex);
        }
        for (List<Edge> edges : adjacencyList.values()) {
            edges.removeIf(edge -> toRemove.contains(edge.getDestination()));
        }
    }

    public boolean containsVertex(String vertexValue, int state) {
        Vertex targetVertex = new Vertex(vertexValue, state);
        return adjacencyList.containsKey(targetVertex);
    }





    public void printVertexStatistics() {
        System.out.println("Total vertices: " + getTotalVertexCount());
        Map<Integer, Integer> stateCountMap = getVertexCountByState();
        for (Map.Entry<Integer, Integer> entry : stateCountMap.entrySet()) {
            System.out.println("State " + entry.getKey() + ": " + entry.getValue() + " vertices");
        }
    }

    public int getTotalVertexCount() {
        return adjacencyList.size();
    }


    public Map<Integer, Integer> getVertexCountByState() {
        Map<Integer, Integer> stateCountMap = new HashMap<>();
        for (Vertex vertex : adjacencyList.keySet()) {
            int state = vertex.state;
            stateCountMap.put(state, stateCountMap.getOrDefault(state, 0) + 1);
        }
        return stateCountMap;
    }

    public void processInputEvent(String guard) {

        List<Edge> edges = adjacencyList.get(currentVertex);
        if (edges != null) {
            for (Edge edge : edges) {
                if (edge.getGuard().equals(guard)) {
                    currentVertex = edge.getDestination();
                    return;
                }
            }
        }
    }


    public List<Map.Entry<List<Vertex>, Double>> findPathsToState(int targetState) {
        List<Map.Entry<List<Vertex>, Double>> pathsWithProbabilities = new ArrayList<>();
        if (currentVertex == null) {
            return pathsWithProbabilities;
        }
        Queue<Map.Entry<List<Vertex>, Double>> queue = new LinkedList<>();
        List<Vertex> initialPath = new ArrayList<>();
        initialPath.add(currentVertex);
        queue.add(new AbstractMap.SimpleEntry<>(initialPath, 1.0));
        while (!queue.isEmpty()) {
            Map.Entry<List<Vertex>, Double> currentEntry = queue.poll();
            List<Vertex> currentPath = currentEntry.getKey();
            double currentProbability = currentEntry.getValue();
            Vertex lastVertex = currentPath.get(currentPath.size() - 1);
            if (lastVertex.state == targetState) {
                pathsWithProbabilities.add(new AbstractMap.SimpleEntry<>(new ArrayList<>(currentPath), currentProbability));
            }
            for (Edge edge : getEdges(lastVertex)) {
                Vertex neighbor = edge.getDestination();
                if (!currentPath.contains(neighbor)) {
                    List<Vertex> newPath = new ArrayList<>(currentPath);
                    newPath.add(neighbor);
                    double newProbability = currentProbability * edge.getPro();
                    queue.add(new AbstractMap.SimpleEntry<>(newPath, newProbability));
                }
            }
        }
        return pathsWithProbabilities;
    }
}