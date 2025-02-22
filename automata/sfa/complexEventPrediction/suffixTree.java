package automata.sfa.complexEventPrediction;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public  class suffixTree {
     public static class Tree<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private TreeNode<T> root;

    public Tree(T rootData) {
        this.root = new TreeNode<>(rootData);
    }

    public TreeNode<T> getRoot() {
        return root;
    }
public static void printTree(Tree<String> tree) {
    printTreeNode(tree.getRoot(), 0);
}

         private static void printTreeNode(TreeNode<String> node, int depth) {
             if (node == null) {
                 return;
             }

             StringBuilder indentation = new StringBuilder();
             for (int i = 0; i < depth; i++) {
                 indentation.append("  ");
             }
             System.out.println(indentation.toString() + node.getStr() + " (Frequency: " + node.getFrequency() + ")");
             for (TreeNode<String> child : node.getChildren()) {
                 printTreeNode(child, depth + 1);
             }
         }
}
    static class TreeNode<T> implements  Serializable{
    private static final long serialVersionUID = 1L;
    private T Str;
    private List<TreeNode<T>> children;
    private  int frequency;

    public TreeNode(T Str) {
        this.Str = Str;
        this.children = new ArrayList<>();
        this.frequency=0;
    }
    public void increseFre(){
        frequency++;
    }
    public int getFrequency() {
        return frequency;
    }
    public T getStr() {
        return Str;
    }

    public List<TreeNode<T>> getChildren() {
        return children;
    }


    public TreeNode<T> addChild(TreeNode<T> child) {

        for (TreeNode<T> existingChild : children) {
            if (existingChild.getStr().equals(child.getStr())) {

                existingChild.increseFre();
                return existingChild;
            }
        }
        children.add(child);
        return child;
    }

}

    public static Tree<String> loadTreeFromFile(String fileName) {
        Tree<String> loadedTree = null;
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(fileName))) {
            loadedTree = (Tree<String>) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return loadedTree;
    }

    public static Tree<String> createTree() {
        Tree<String> tree = new Tree<>("Root");
        return tree;
    }
    public static void update(TreeNode<String> parent, int start, int end, String[] memory) {
        TreeNode<String> child = new TreeNode<>(memory[start]);
        child.increseFre();
        TreeNode<String> upgrade = parent.addChild(child);
        if (start != end) {
            if (start != 0) {
                if (memory[start - 1] != null) {
                    update(upgrade, start - 1, end, memory);
                }
            } else {
                if (memory[memory.length - 1] != null) {
                    update(upgrade, memory.length - 1, end, memory);
                }
            }
        }
    }
     public static int traverseCSTFromEnd(TreeNode<String> parent, String input, int lower, int upper) {
        if (parent == null || input.isEmpty() || upper < 0) {
            return -1;
        }

        char lastChar = input.charAt(upper);
        List<TreeNode<String>> children = parent.getChildren();

        for (TreeNode<String> child : children) {
            char childStr = child.getStr().charAt(0);
            if (childStr == lastChar) {
                if (lower < upper) {
                    return traverseCSTFromEnd(child, input, 0, upper - 1);
                } else {
                    return child.getFrequency();
                }
            } else {
            System.out.println("Not this");
            }
        }
        return 0;
    }

    public static  Tree<String> suffixTreeModel4Weather(String inputFilePath,int order) throws FileNotFoundException {
        String[] memory = new String[order];
        Tree<String> tree = createTree();
        TreeNode<String> root = tree.getRoot();
        int index = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String event="";
                String weatherType=parts[1];
                if(weatherType.equals("Snow"))  event ="a";
                else if (weatherType.equals("Soft") )event ="s";
                else if (weatherType.equals("Fog")) event="c";
                else if (weatherType.equals("Rain")) event="e";
                else if (weatherType.equals("Cold")) event="b";
                else if (weatherType.equals("Hail")) event="d";
                else if (weatherType.equals("Storm")) event="f";
                else if (weatherType.equals("Precipitation")) event="e";
                else event = null;
                if (event!=null) {
                    memory[index] = event;
                    index = (index + 1) % order;
                    root.increseFre();
                    if (index != 0) {
                        update(root, index - 1, index, memory);
                    } else {
                        update(root, order - 1, index, memory);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tree;
    }

    public static Tree<String> suffixTreeModel4Traffic(String inputFilePath, int order) throws FileNotFoundException {
        String[] memory = new String[order];
        suffixTree.Tree<String> tree = createTree();
        suffixTree.TreeNode<String> root = tree.getRoot();
        int index = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String event = "";

                String trafficVolume = parts[2];

                int trafficVolumeInt;
                try {
                    trafficVolumeInt = Integer.parseInt(trafficVolume);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format: " + trafficVolume);
                    trafficVolumeInt = 0;
                }
                if (trafficVolumeInt <= 30) {
                    event = "a";
                }
                else if (trafficVolumeInt <= 50) {
                    event = "b";
                }
                else if (trafficVolumeInt <= 60) {
                    event = "c";
                }
                else if (trafficVolumeInt <= 80){
                    event = "d";
                }
                else if (trafficVolumeInt > 80) {
                    event = "e";
                } else event = null;
                if (event != null) {
                    memory[index] = event;
                    index = (index + 1) % order; // Update index in circular fashion
                    root.increseFre();
                    if (index != 0) {
                        update(root, index - 1, index, memory);
                    } else {
                        update(root, order - 1, index, memory);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tree;
    }
}
