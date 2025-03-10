

You can find the complete **symbolicautomata** project and learn from it at https://github.com/lorisdanto/symbolicautomata/tree/master.

### suffixTree.java

The `suffixTreeModel4Weather` and `suffixTreeModel4Traffic` under `suffixTree.java` are used to build suffix trees.



### trafficSFA.java && weatherSFA.java

The automaton used in this experiment for coding.



### PSTree.java

The `createPSTree` function under `PSTree.java` is used to create the PSTree.

### Dis.java

`dis` is the probability distribution of the model's path, and the `countDis` function is used to calculate the distribution. The parameter `existAnd` specifies whether there is an AND component in the SFA. If true, the corresponding `andParts`, `andLength`, `andBeginIndex`, and `symbols` parameters need to be provided. If false, `null` can be passed instead.



### WeightedDirectedGraph.java

`WeightedDirectedGraph.java` implements the PSA model.



### Graph.java

`Graph` implements the PSA model and the embedding process of the SFA.



### predictionFunction.java

It implements two functions for predicting the release of both models.
