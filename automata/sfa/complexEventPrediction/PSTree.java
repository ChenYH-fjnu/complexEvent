package automata.sfa.complexEventPrediction;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.util.*;

import static automata.sfa.complexEventPrediction.suffixTree.*;


public class PSTree {
    public static class PSTTreeNode<T> {
    private T Str;
    private List<PSTTreeNode<T>> children;
    private PSTTreeNode<T> parent;
    private double[] probabilities;

    public PSTTreeNode(T Str, int length) {
        this.Str = Str;
        this.children = new ArrayList<>();
        this.parent = null;
        this.probabilities = new double[length];
    }
    public T getStr() {
        return Str;
    }

    public List<PSTTreeNode<T>> getChildren() {
        return children;
    }
    public void setParent(PSTTreeNode<T> parent) {
        this.parent = parent;
    }

    public PSTTreeNode<T> addChild(PSTTreeNode<T> child) {
        for (PSTTreeNode<T> existingChild : children) {
            if (existingChild.getStr().equals(child.getStr())) {
                return existingChild;
            }
        }
        children.add(child);
        child.setParent(this);
        return child;
    }

    public double[] getProbabilities() {
        return probabilities;
    }


    public double getProbabilities(int index) {
        return probabilities[index];
    }

    public void setProbabilities(int index, double pro) {
        this.probabilities[index] = pro;
    }



    public void printTree(String prefix, boolean isTail) {
        System.out.print(prefix);
        System.out.print(isTail ? "|__" : "|--");
        System.out.print(getStr());
        System.out.print(" (Probabilities: ");
        for (double probability : probabilities) {
            System.out.print(probability + ", ");
        }
        System.out.print(")");
        System.out.println();

        for (int i = 0; i < getChildren().size(); i++) {
            getChildren().get(i).printTree(prefix + (isTail ? "    " : "|   "), i == getChildren().size() - 1);
        }
    }
    @Override
    public String toString() {
        return Arrays.toString(probabilities);
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }


        public void setProbabilities(double[] doubles) {
                this.probabilities = doubles;
        }
    }

    public static class PSTTree<T> {
    private PSTTreeNode<T> root;

    public PSTTree(T rootData,int length) {
        this.root = new PSTTreeNode<>(rootData,length);
    }

    public PSTTreeNode<T> getRoot() {
        return root;
    }

    public void printTree() {
        if (root != null) {
            root.printTree("", true);
        }
    }

    public List<PSTTreeNode<T>> getAllLeafNodes() {
        List<PSTTreeNode<T>> leafNodes = new ArrayList<>();
        getAllLeafNodesHelper(root, leafNodes);
        return leafNodes;
    }
        public List<String> getAllLeaf() {
            List<PSTTreeNode<T>> leafNodes = new ArrayList<>();
            List<String> leaf = new ArrayList<>();
            getAllLeafNodesHelper(root, leafNodes);
            for (PSTTreeNode<T> node:leafNodes){
                leaf.add((String) node.Str);
            }
            return leaf;
        }

    private void getAllLeafNodesHelper(PSTTreeNode<T> node, List<PSTTreeNode<T>> leafNodes) {
        if (node.isLeaf()) {
            leafNodes.add(node);
            return;
        }

        for (PSTTreeNode<T> child : node.getChildren()) {
            getAllLeafNodesHelper(child, leafNodes);
        }
    }

    public List<T> getAllLeafStrings() {
        List<T> leafStrings = new ArrayList<>();
        getAllLeafStringsHelper(root, leafStrings);
        return leafStrings;
    }

    private void getAllLeafStringsHelper(@NotNull PSTTreeNode<T> node, List<T> leafStrings) {
        if (node.isLeaf()) {
            leafStrings.add(node.getStr());
            return;
        }

        for (PSTTreeNode<T> child : node.getChildren()) {
            getAllLeafStringsHelper(child, leafStrings);
        }
    }
}
    static int findIndex(String str,String[] weatherPredicates){
        int index=0;
        for (;index< weatherPredicates.length;index++){
            if (str.equals(weatherPredicates[index])){
                break;
            }
        }
        return index;
    }

     public static double[] findProbabilities(@NotNull PSTTree<String> tree, String searchString) {
        PSTTreeNode<String> root = tree.getRoot();
        return findProbabilitiesHelper(root, searchString);
    }

     private static double[] findProbabilitiesHelper(PSTTreeNode<String> node, String searchString) {
        if (node == null) {
            return null;
        }

        if (node.getStr().equals(searchString)) {
            return node.getProbabilities();
        }

        for (PSTTreeNode<String> child : node.getChildren()) {
            double[] result = findProbabilitiesHelper(child, searchString);
            if (result != null) {
                return result;
            }
        }

        return null;
    }
    public static double travelPSTree(PSTTreeNode<String> root,String memory,int predicatesIndex){
        if (root == null) {
            return -1;
        }
        if (memory.length()==0){
            return root.getProbabilities(predicatesIndex);
        }
        else {
            double pro = root.getProbabilities(predicatesIndex);
            String lastChar = memory.substring(memory.length()-1);
            boolean exist=false;

            if (root.getChildren().size()>0) {
                for (PSTTreeNode<String> child : root.getChildren()) {
                    if (child.getStr().equals(lastChar)) {
                        if (child.getProbabilities(predicatesIndex)==0) break;
                        pro = child.getProbabilities(predicatesIndex);
                        int times=memory.length()-2;
                            PSTTreeNode<String> parent = child;
                        while (times>=0){
                            boolean inner=false;
                            lastChar = memory.substring(times);
                            if (parent.getChildren().size()>=0){
                                for (PSTTreeNode<String> newchild:parent.getChildren()){
                                    if (newchild.getStr().equals(lastChar)){
                                        if (newchild.getProbabilities(predicatesIndex) > 0){
                                            inner =true;
                                            pro=newchild.getProbabilities(predicatesIndex);
                                            parent = newchild;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (!inner)  break;
                            times--;
                        }
                        if (pro>0){
                            return pro;
                        }
                    }
                }
                return  pro;
            }
        }
        return -2;
    }


    public static void  formatePstNode( PSTTreeNode<String> PSTRoot,String[] weatherPredicates){
        double ymin = 0.001;
        double[] proArray =PSTRoot.getProbabilities();
        for (int i=0;i<proArray.length;i++){
            if (proArray[i]==0){
                proArray[i]=0.001;
            }else {
                double newDouble = (1-weatherPredicates.length*ymin)*proArray[i]+ymin;
                PSTRoot.setProbabilities(i,newDouble);
            }
        }
    }

    public static PSTTree<String> createPSTree(Tree<String> tree,String[] predicates,int order,int firstConditionsherFre,double secondConditionSher) {
        TreeNode<String> root = tree.getRoot();
        int length =  predicates.length;
        List<TreeNode<String>> children = root.getChildren();
        PSTTree<String> pstTree = new PSTTree<>("E", length);
        Queue<PSTTreeNode> PSTTreeNodeQueue = new LinkedList<>();
        PSTTreeNode<String> PSTRoot = pstTree.getRoot();
        TreeNode<String> CSTRoot = tree.getRoot();
        int rootFrequency = CSTRoot.getFrequency();
        for (TreeNode<String> node : children){
            String symbolic= node.getStr();
            PSTTreeNode<String> nodex = new PSTTreeNode<>(symbolic, length);
            PSTRoot.addChild(nodex);
            PSTTreeNodeQueue.add(nodex);
            PSTRoot.setProbabilities(findIndex(symbolic, predicates), (double) node.getFrequency() / rootFrequency);
        }
        formatePstNode(PSTRoot, predicates);


        while (!PSTTreeNodeQueue.isEmpty()) {
            PSTTreeNode<String> secondFloorNode = PSTTreeNodeQueue.poll();
            String suffix = secondFloorNode.getStr();
            int fre = 0;
            for (String s :  predicates) {
                String sym = suffix + s;
                fre = fre + traverseCSTFromEnd(CSTRoot, sym, 0, sym.length() - 1);
            }
            for (String s :  predicates) {
                String sym = suffix + s;
                int f = traverseCSTFromEnd(CSTRoot, sym, 0, sym.length() - 1);
                double fq = (double) f / fre;
                int index = -1;

                for (int i = 0; i <  predicates.length; i++) {
                    if (predicates[i].equals(s)) {
                        index = i;
                        break;
                    }
                }
                secondFloorNode.setProbabilities(index, fq);
            }
            formatePstNode(secondFloorNode,predicates);
            LearningPSTree(CSTRoot, secondFloorNode, order - 1, predicates, firstConditionsherFre,secondConditionSher);
        }
        return pstTree;
    }

    private static void LearningPSTree(TreeNode<String> CSTRoot, PSTTreeNode<String> parantNode, int m,String[] predicates,int firstConditionsherFre,
                                               double secondConditionSher) {
        if (m > 1) {
            for (String childnode :  predicates) {
                String child = childnode + parantNode.getStr();
                PSTTreeNode<String> childNode = new PSTTreeNode<>(child,  predicates.length);
                for (String predicate :  predicates) {
                    String grandson = child + predicate;
                    int numerator = traverseCSTFromEnd(CSTRoot, grandson, 0, grandson.length() - 1);
                    int denominator = traverseCSTFromEnd(CSTRoot, child, 0, child.length() - 1);
                    if (numerator != -1 && denominator != -1) {
                        if (denominator >= firstConditionsherFre) {
                            double fq = (double) numerator / denominator;
                            int index = -1;
                            for (int i = 0; i <  predicates.length; i++) {
                                if (predicates[i].equals(predicate)) {
                                    index = i;
                                    break;
                                }
                            }
                            childNode.setProbabilities(index, fq);
                        } else break;
                    }
                }
                double[] probabilities = childNode.getProbabilities();
                double sum = 0.0;
                for (double probability : probabilities) {
                    sum += probability;
                }
                for (int i = 0; i < probabilities.length; i++) {
                    probabilities[i] /= sum;
                }

                formatePstNode(childNode, predicates);
                int countForSecondCondition = 0;
                for (int i = 0; i <  predicates.length; i++) {
                    double pro1 = childNode.getProbabilities(i);
                    double pro2 = parantNode.getProbabilities(i);

                    if (pro1 / pro2 < secondConditionSher || pro1 / pro2 > 1/secondConditionSher){
                        countForSecondCondition++;
                    }
                }
                if (countForSecondCondition >= 1) {
                    parantNode.addChild(childNode);
                    if (m - 1 > 1) {
                        LearningPSTree(CSTRoot, childNode, m - 1, predicates,firstConditionsherFre,secondConditionSher);
                    }
                }
            }
        }
    }






}

