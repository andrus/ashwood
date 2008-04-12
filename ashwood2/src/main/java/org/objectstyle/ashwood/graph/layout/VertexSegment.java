package org.objectstyle.ashwood.graph.layout;

import java.util.*;

public class VertexSegment {
  private List vertices = new ArrayList();

  public VertexSegment() {
  }

  public void moveX(double distance) {
    int size = vertices.size();
    for (int i = 0; i < size; i++) {
      LayerVertex v = (LayerVertex)vertices.get(i);
      v.moveX(distance);
    }
  }

  public void add(LayerVertex vertex) {
    vertices.add(vertex);
  }

  public LayerVertex getVertex(int index) {
    return (LayerVertex)vertices.get(index);
  }
}