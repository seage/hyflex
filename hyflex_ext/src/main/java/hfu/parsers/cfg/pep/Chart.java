package hfu.parsers.cfg.pep;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class Chart {
  static final Integer NULL_INDEX = new Integer(-1);
  
  SortedMap<Integer, Set<Edge>> edgeSets;
  
  public Chart() {
    this(new TreeMap<>());
  }
  
  public Chart(Chart chart) {
    this(new TreeMap<>(chart.edgeSets));
  }
  
  Chart(SortedMap<Integer, Set<Edge>> edgeSets) {
    this.edgeSets = edgeSets;
  }
  
  public Set<Integer> indices() {
    return this.edgeSets.keySet();
  }
  
  public Integer firstIndex() {
    return this.edgeSets.firstKey();
  }
  
  public Integer lastIndex() {
    return this.edgeSets.lastKey();
  }
  
  public Chart subChart(Integer from, Integer to) {
    return new Chart(this.edgeSets.subMap(from, to));
  }
  
  public Chart headChart(Integer to) {
    return new Chart(this.edgeSets.headMap(to));
  }
  
  public Chart tailChart(Integer from) {
    return new Chart(this.edgeSets.tailMap(from));
  }
  
  public boolean contains(Edge edge) {
    return !indexOf(edge).equals(NULL_INDEX);
  }
  
  public Integer indexOf(Edge edge) {
    if (edge != null)
      for (Map.Entry<Integer, Set<Edge>> entry : this.edgeSets.entrySet()) {
        if (((Set)entry.getValue()).contains(edge))
          return entry.getKey(); 
      }  
    return NULL_INDEX;
  }
  
  public void clear() {
    this.edgeSets.clear();
  }
  
  public boolean isEmpty() {
    return this.edgeSets.isEmpty();
  }
  
  public boolean containsEdges(Integer index) {
    return this.edgeSets.containsKey(index);
  }
  
  public int countEdges() {
    int count = 0;
    for (Set<Edge> edgeSet : this.edgeSets.values())
      count += edgeSet.size(); 
    return count;
  }
  
  public Set<Edge> getEdges(Integer index) {
    if (index == null)
      throw new NullPointerException("null index"); 
    return Collections.unmodifiableSet(this.edgeSets.get(index));
  }
  
  public boolean addEdge(Integer index, Edge edge) {
    if (index == null)
      throw new NullPointerException("null index"); 
    if (edge == null)
      throw new NullPointerException("null edge"); 
    if (index.intValue() < 0)
      throw new IndexOutOfBoundsException("invalid index: " + index); 
    Set<Edge> edges = this.edgeSets.get(index);
    if (edges == null) {
      edges = new HashSet<>();
      this.edgeSets.put(index, edges);
    } 
    return edges.add(edge);
  }
  
  public boolean equals(Object obj) {
    return (obj instanceof Chart && this.edgeSets.equals(((Chart)obj).edgeSets));
  }
  
  public int hashCode() {
    return 37 * (1 + this.edgeSets.hashCode());
  }
  
  public String toString() {
    return this.edgeSets.toString();
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\parsers\cfg\pep\Chart.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */