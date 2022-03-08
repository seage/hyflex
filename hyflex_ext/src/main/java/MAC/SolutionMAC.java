package MAC;

import hfu.BasicSolution;
import hfu.RNG;
import hfu.datastructures.AdjecencyList;
import hfu.datastructures.Graph;
import hfu.heuristics.modifiers.nh.CompositeNH;
import hfu.heuristics.modifiers.nh.IteratorNH;
import hfu.heuristics.modifiers.nh.RangeNH;
import hfu.heuristics.modifiers.nh.filter.Filter;
import java.util.ArrayList;
import java.util.Arrays;

public class SolutionMAC extends BasicSolution<InfoMAC> {
  private int[] assignment;
  
  private int assigned1;
  
  private int assigned2;
  
  public SolutionMAC(InfoMAC instance) {
    super(instance);
    this.assigned1 = 0;
    this.assigned2 = 0;
    this.assignment = new int[instance.getNvertices()];
  }
  
  protected SolutionMAC(SolutionMAC other) {
    super(other);
    this.assigned1 = other.assigned1;
    this.assigned2 = other.assigned2;
    this.assignment = new int[((InfoMAC)this.instance).getNvertices()];
    System.arraycopy(other.assignment, 0, this.assignment, 0, this.assignment.length);
  }
  
  public boolean isEqualTo(BasicSolution<InfoMAC> other) {
    SolutionMAC o = (SolutionMAC)other;
    return (this.assigned1 == o.assigned1 && this.assigned2 == o.assigned2 && Arrays.equals(this.assignment, o.assignment));
  }
  
  public BasicSolution<InfoMAC> deepCopy() {
    return new SolutionMAC(this);
  }
  
  public String toText() {
    String result = "partition 1: ";
    int i;
    for (i = 0; i < this.assignment.length; i++) {
      if (this.assignment[i] == 1)
        result = String.valueOf(result) + (i + 1) + " "; 
    } 
    result = String.valueOf(result) + System.lineSeparator() + "partition 2: ";
    for (i = 0; i < this.assignment.length; i++) {
      if (this.assignment[i] == 2)
        result = String.valueOf(result) + (i + 1) + " "; 
    } 
    return String.valueOf(result) + System.lineSeparator() + " (" + getFunctionValue() + ")";
  }
  
  protected double evaluateFunctionValue() {
    Graph G = ((InfoMAC)this.instance).getGraph();
    int e = 0;
    for (int v1 = 0; v1 < this.assignment.length; v1++) {
      if (this.assignment[v1] == 1)
        e += evaluateVertice(v1, G); 
    } 
    return e;
  }
  
  private int evaluateVertice(int v, Graph G) {
    int e = 0;
    if (this.assignment[v] != 0) {
      int other = 3 - this.assignment[v];
      ArrayList<AdjecencyList.Neighbor> neighbors = G.getNeighbors(v);
      for (int vi = 0; vi < neighbors.size(); vi++) {
        AdjecencyList.Neighbor nb = neighbors.get(vi);
        if (this.assignment[nb.getID()] == other)
          e += nb.getW(); 
      } 
    } 
    return -e;
  }
  
  public boolean isPartial() {
    return (this.assigned1 + this.assigned2 < ((InfoMAC)this.instance).getNvertices());
  }
  
  public boolean isEmpty() {
    return (this.assigned1 + this.assigned2 == 0);
  }
  
  public void insert(int v, int p) {
    double e = getFunctionValue();
    this.assignment[v] = p;
    if (p == 1) {
      this.assigned1++;
    } else {
      this.assigned2++;
    } 
    e += evaluateVertice(v, ((InfoMAC)this.instance).getGraph());
    setFunctionValue(e);
  }
  
  public void swap(int v) {
    double e = getFunctionValue();
    if (this.assignment[v] == 1) {
      this.assigned1--;
      this.assigned2++;
    } else {
      this.assigned2--;
      this.assigned1++;
    } 
    e -= evaluateVertice(v, ((InfoMAC)this.instance).getGraph());
    this.assignment[v] = 3 - this.assignment[v];
    e += evaluateVertice(v, ((InfoMAC)this.instance).getGraph());
    setFunctionValue(e);
  }
  
  public void remove(int v) {
    double e = getFunctionValue();
    if (this.assignment[v] == 0)
      return; 
    if (this.assignment[v] == 1) {
      this.assigned1--;
    } else {
      this.assigned2--;
    } 
    e -= evaluateVertice(v, ((InfoMAC)this.instance).getGraph());
    this.assignment[v] = 0;
    setFunctionValue(e);
  }
  
  public void removeRadial(int v) {
    remove(v);
    ArrayList<AdjecencyList.Neighbor> neighbors = ((InfoMAC)this.instance).getGraph().getNeighbors(v);
    for (int i = 0; i < neighbors.size(); i++)
      remove(((AdjecencyList.Neighbor)neighbors.get(i)).getID()); 
  }
  
  public void swapNeighbours(int vi, int vj) {
    swap(vi);
    swap(vj);
  }
  
  private void compute_assigned() {
    this.assigned1 = 0;
    this.assigned2 = 0;
    for (int i = 0; i < this.assignment.length; i++) {
      if (this.assignment[i] == 1) {
        this.assigned1++;
      } else if (this.assignment[i] == 2) {
        this.assigned2++;
      } 
    } 
  }
  
  private int nmatch(SolutionMAC c2) {
    int matches = 0;
    for (int i = 0; i < this.assignment.length; i++) {
      if (this.assignment[i] == c2.assignment[i])
        matches++; 
    } 
    return matches;
  }
  
  private void swap_labels() {
    int temp = this.assigned1;
    this.assigned1 = this.assigned2;
    this.assigned2 = temp;
    for (int i = 0; i < this.assignment.length; i++)
      this.assignment[i] = 3 - this.assignment[i]; 
  }
  
  public void one_point_crossover(int pivot, SolutionMAC c2) {
    if (2 * nmatch(c2) < this.assignment.length)
      swap_labels(); 
    System.arraycopy(c2.assignment, pivot, this.assignment, pivot, this.assignment.length - pivot);
    compute_assigned();
    setFunctionValue(-1.0D);
  }
  
  public void mp_crossover(SolutionMAC c2) {
    this.assigned1 = 0;
    this.assigned2 = 0;
    int[] new_assignment = new int[this.assignment.length];
    int[][] c = new int[2][2];
    for (int i = 0; i < this.assignment.length; i++)
      c[this.assignment[i] - 1][c2.assignment[i] - 1] = c[this.assignment[i] - 1][c2.assignment[i] - 1] + 1; 
    int v1 = -1;
    int v2 = -1;
    int bestc = -1;
    int j;
    for (j = 0; j < 2; j++) {
      for (int k = 0; k < 2; k++) {
        if (c[j][k] > bestc) {
          v1 = j + 1;
          v2 = k + 1;
          bestc = c[j][k];
        } 
      } 
    } 
    for (j = 0; j < this.assignment.length; j++) {
      if (this.assignment[j] == v1 && c2.assignment[j] == v2) {
        new_assignment[j] = 1;
        this.assigned1++;
      } 
    } 
    c = new int[2][2];
    for (j = 0; j < this.assignment.length; j++) {
      if (new_assignment[j] == 0)
        c[this.assignment[j] - 1][c2.assignment[j] - 1] = c[this.assignment[j] - 1][c2.assignment[j] - 1] + 1; 
    } 
    v1 = -1;
    v2 = -1;
    bestc = -1;
    for (j = 0; j < 2; j++) {
      for (int k = 0; k < 2; k++) {
        if (c[j][k] > bestc) {
          v1 = j + 1;
          v2 = k + 1;
          bestc = c[j][k];
        } 
      } 
    } 
    for (j = 0; j < this.assignment.length; j++) {
      if (this.assignment[j] == v1 && c2.assignment[j] == v2) {
        new_assignment[j] = 2;
        this.assigned2++;
      } 
    } 
    for (j = 0; j < this.assignment.length; j++) {
      if (new_assignment[j] == 0)
        if (RNG.get().nextBoolean()) {
          new_assignment[j] = 1;
          this.assigned1++;
        } else {
          new_assignment[j] = 2;
          this.assigned2++;
        }  
    } 
    this.assignment = new_assignment;
    setFunctionValue(-1.0D);
  }
  
  public int getNassigned() {
    return this.assigned1 + this.assigned2;
  }
  
  public boolean isAssigned(int v) {
    return (this.assignment[v] != 0);
  }
  
  public static class SwapNH extends RangeNH<InfoMAC> {
    public SwapNH(InfoMAC instance) {
      super(0, instance.getNvertices(), instance);
    }
  }
  
  public static class RemoveNH extends RangeNH<InfoMAC> {
    public RemoveNH(InfoMAC instance, SolutionMAC c) {
      super(0, instance.getNvertices(), new Assigned(c), instance);
    }
    
    static class Assigned implements Filter {
      SolutionMAC c;
      
      Assigned(SolutionMAC c) {
        this.c = c;
      }
      
      public boolean include(int[] param) {
        return this.c.isAssigned(param[0]);
      }
    }
  }
  
  public static class InsertNH extends CompositeNH<InfoMAC> {
    SolutionMAC c;
    
    public InsertNH(InfoMAC instance, SolutionMAC c, int v) {
      super(instance, new RangeNH[] { new RangeNH(v, v + 1, instance), new RangeNH(1, 3, instance) });
      this.c = c;
    }
    
    public InsertNH(InfoMAC instance, SolutionMAC c) {
      super(instance, new RangeNH[] { new RangeNH(0, instance.getNvertices(), new UnAssigned(c), instance), new RangeNH(1, 3, instance) });
      this.c = c;
    }
    
    public int[] sample() {
      IteratorNH it = getIterator();
      ArrayList<int[]> included = (ArrayList)new ArrayList<>(((InfoMAC)this.instance).getNvertices());
      while (it.hasNext()) {
        int[] p = (int[])it.next();
        if (!this.c.isAssigned(p[0]))
          included.add(p); 
      } 
      return included.get(RNG.get().nextInt(included.size()));
    }
    
    static class UnAssigned implements Filter {
      SolutionMAC c;
      
      UnAssigned(SolutionMAC c) {
        this.c = c;
      }
      
      public boolean include(int[] param) {
        return !this.c.isAssigned(param[0]);
      }
    }
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\MAC\SolutionMAC.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */