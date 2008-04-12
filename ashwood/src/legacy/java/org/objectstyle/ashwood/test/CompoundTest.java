package org.objectstyle.ashwood.test;

import java.util.*;
import org.objectstyle.ashwood.util.*;
import org.objectstyle.ashwood.graph.*;

public class CompoundTest {
  public static void main(String[] args) {
    Set[] partition = new Set[] {new TreeSet(), new TreeSet(), new TreeSet()};
    Pair[] partitionOrder = new Pair[partition.length];
    for (int i = 0; i < partition.length; i++) {
      partitionOrder[i] = new Pair(partition[i], new Double(i));
    }

    int elementCount = 25;
    Random randomizer = new Random(1000);
    //init partition
    for (int i = 0; i < elementCount; i++) {
      partition[randomizer.nextInt(partition.length)].add(new Integer(i));
    }
    //print partition
    for (int i = 0; i < partition.length; i++) {
      Set s = partition[i];
      System.out.print("partition[" + i + "] = {");
      for (Iterator j = s.iterator(); j.hasNext(); ) {
        Object element = j.next();
        System.out.print(element + ", ");
      }
      System.out.println("}");
    }
    //init layers
    int[][] layers = new int[5][5];
    int element = 0;
    for (int i = 0; i < layers.length; i++) {
      for (int j = 0; j < layers[i].length; j++) {
        layers[i][j] = element++;
      }
    }
    System.out.println("Initial positions");
    printLayers(layers);
    //group elements in layers by partition
    for (int i = 0; i < layers.length; i++) {
      int[] layer = layers[i];
      for (int j = 0; j < partition.length; j++) {
        double weight = weight((Set)partitionOrder[j].first, layer);
        partitionOrder[j].second = new Double(weight);
      }
      Arrays.sort(partitionOrder, Pair.SECOND_COMPARATOR);
      int[] orderedLayer = new int[layer.length];
      int index = 0;
      for (int j = 0; j < partitionOrder.length; j++) {
        if (((Double)partitionOrder[j].second).doubleValue() < 0) continue;
        Set s = (Set)partitionOrder[j].first;
        for (int k = 0; k < layer.length; k++) {
          if (s.contains(new Integer(layer[k])))
            orderedLayer[index++] = layer[k];
        }
      }
      layers[i] = orderedLayer;
    }
    System.out.println("Positions after reordering");
    printLayers(layers);
    System.out.println("labeled");
    printPartitionLabels(layers, partition);

    //form subgraph ordering graph
    Digraph subgraphOrderingGraph = new MapDigraph();
    for (int i = 0; i < partition.length; i++) {
      subgraphOrderingGraph.addVertex(partition[i]);
      for (int j = 0; j < partition.length; j++) {
        if (i == j) continue;
        if (isLeftOf(partition[i], partition[j], layers)) {
          if (subgraphOrderingGraph.hasArc(partition[j], partition[i])) {
            double weight1 = weight(partition[i], layers);
            double weight2 = weight(partition[j], layers);
            if (weight1 < weight2) {
              subgraphOrderingGraph.removeArc(partition[j], partition[i]);
              subgraphOrderingGraph.putArc(partition[i], partition[j], Boolean.TRUE);
            } else continue;
          } else
            subgraphOrderingGraph.putArc(partition[i], partition[j], Boolean.TRUE);
        }
      }
    }

    //break cycles in subgraph ordering graph:
    //make it dag for the topological sorting op
    List cycles = GraphUtils.findCycles(subgraphOrderingGraph);
    for (Iterator i = cycles.iterator(); i.hasNext(); ) {
      ArrayList cycle = (ArrayList)i.next();
      int minWeightIndex = -1;
      double minWeight = Double.POSITIVE_INFINITY;
      for (int j = 0; j < cycle.size(); j++) {
        double weight = weight((Set)cycle.get(j), layers);
        if (weight < minWeight) {
          minWeightIndex = j;
          minWeight = weight;
        }
      }
      if (minWeightIndex > 0) {
        subgraphOrderingGraph.removeArc(
            cycle.get(minWeightIndex - 1), cycle.get(minWeightIndex));
      } else {
        subgraphOrderingGraph.removeArc(
            cycle.get(cycle.size() - 1), cycle.get(0));
      }
    }

    //sort ubgraph ordering topologically
    IndegreeTopologicalSort sorter =
        new IndegreeTopologicalSort(subgraphOrderingGraph);
    Map indexMap = new HashMap();
    int sortIndex = 0;
    while (sorter.hasNext()) {
      indexMap.put(sorter.next(), new Integer(sortIndex++));
    }
    //reorder sequences of vertices in layers
    for (int j = 0; j < partition.length; j++) {
      partitionOrder[j].second = indexMap.get(partitionOrder[j].first);
    }
    Arrays.sort(partitionOrder, Pair.SECOND_COMPARATOR);
    for (int i = 0; i < layers.length; i++) {
      int[] layer = layers[i];
      int[] orderedLayer = new int[layer.length];
      int index = 0;
      for (int j = 0; j < partitionOrder.length; j++) {
        Set s = (Set)partitionOrder[j].first;
        for (int k = 0; k < layer.length; k++) {
          if (s.contains(new Integer(layer[k])))
            orderedLayer[index++] = layer[k];
        }
      }
      layers[i] = orderedLayer;
    }
    System.out.println("Final not interwined positions");
    printLayers(layers);
    System.out.println("labeled");
    printPartitionLabels(layers, partition);
  }

  static void printLayers(int[][] layers) {
    for (int i = 0; i < layers.length; i++) {
      System.out.print("l[" + i + "]: ");
      for (int j = 0; j < layers[i].length; j++) {
        System.out.print(layers[i][j] + ", ");
      }
      System.out.println("");
    }
  }

  static void printPartitionLabels(int[][] layers, Set[] partition) {
    for (int i = 0; i < layers.length; i++) {
      System.out.print("l[" + i + "]: ");
      for (int j = 0; j < layers[i].length; j++) {
        int element = layers[i][j];
        int partitionLabel = -1;
        for (int k = 0; k < partition.length; k++) {
          if (partition[k].contains(new Integer(element))) {
            partitionLabel = k;
            break;
          }
        }
        System.out.print(partitionLabel + ", ");
      }
      System.out.println("");
    }
  }

  static double weight(Set s, int[] layer) {
    double weight = 0;
    int count = 0;
    for (int i = 0; i < layer.length; i++) {
      if (s.contains(new Integer(layer[i]))) {
        weight += i;
        ++count;
      }
    }
    weight = (count > 0 ? weight / count : -1);
    return weight;
  }

  static double weight(Set s, int[][] layers) {
    double weight = 0;
    int count = 0;
    for (int i = 0; i < layers.length; i++) {
      for (int j = 0; j < layers[i].length; j++) {
        if (s.contains(new Integer(layers[i][j]))) {
          weight += j;
          ++count;
        }
      }
    }
    weight = (count > 0 ? weight / count : -1);
    return weight;
  }

  static boolean isLeftOf(Set s1, Set s2, int[][] layers) {
    for (int i = 0; i < layers.length; i++) {
      for (int j = 0; j < layers[i].length - 1; j++) {
        if (s1.contains(new Integer(layers[i][j])) &&
            s2.contains(new Integer(layers[i][j+1]))) return true;
      }
    }
    return false;
  }
}