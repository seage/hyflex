package QAP;

import hfu.BasicSolution;
import hfu.RNG;
import hfu.heuristics.modifiers.nh.CombinationNH;
import hfu.heuristics.modifiers.nh.RangeNH;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SolutionQAP extends BasicSolution<InfoQAP> {
  private int[] fac2loc;
  
  int assigned;
  
  public SolutionQAP(InfoQAP instance) {
    super(instance);
    this.fac2loc = new int[instance.getN()];
    for (int i = 0; i < this.fac2loc.length; i++)
      this.fac2loc[i] = -1; 
    this.assigned = 0;
  }
  
  protected SolutionQAP(SolutionQAP other) {
    super(other);
    this.assigned = other.assigned;
    this.fac2loc = new int[((InfoQAP)this.instance).getN()];
    System.arraycopy(other.fac2loc, 0, this.fac2loc, 0, this.fac2loc.length);
  }
  
  public boolean isEqualTo(BasicSolution<InfoQAP> other) {
    if (other instanceof SolutionQAP) {
      SolutionQAP other_qap = (SolutionQAP)other;
      if (this.assigned == other_qap.assigned && getFunctionValue() == other_qap.getFunctionValue())
        return Arrays.equals(this.fac2loc, other_qap.fac2loc); 
    } 
    return false;
  }
  
  public BasicSolution<InfoQAP> deepCopy() {
    return new SolutionQAP(this);
  }
  
  public String toText() {
    String result = "";
    for (int i = 0; i < this.fac2loc.length; i++)
      result = String.valueOf(result) + "f" + i + " -> l" + this.fac2loc[i] + System.lineSeparator(); 
    return String.valueOf(result) + System.lineSeparator() + " (" + getFunctionValue() + ")";
  }
  
  protected double evaluateFunctionValue() {
    double e = 0.0D;
    for (int i = 0; i < this.fac2loc.length; i++) {
      for (int j = 0; j < this.fac2loc.length; j++)
        e += (((InfoQAP)this.instance).getFlow(i, j) * ((InfoQAP)this.instance).getDistance(this.fac2loc[i], this.fac2loc[j])); 
    } 
    return e;
  }
  
  public boolean isPartial() {
    return (this.assigned < ((InfoQAP)this.instance).getN());
  }
  
  public boolean isEmpty() {
    return (this.assigned == 0);
  }
  
  private static void shuffleArray(int[] ar) {
    for (int i = ar.length - 1; i > 0; i--) {
      int index = RNG.get().nextInt(i + 1);
      int a = ar[index];
      ar[index] = ar[i];
      ar[i] = a;
    } 
  }
  
  public void randomInit() {
    for (int i = 0; i < this.fac2loc.length; i++)
      this.fac2loc[i] = i; 
    shuffleArray(this.fac2loc);
    this.assigned = this.fac2loc.length;
    setFunctionValue(-1.0D);
  }
  
  public void swap(int fi, int fj) {
    double e = getFunctionValue();
    e -= evaluateFacility(fi);
    e -= evaluateFacility(fj);
    int temp_loc = this.fac2loc[fi];
    this.fac2loc[fi] = this.fac2loc[fj];
    this.fac2loc[fj] = temp_loc;
    e += evaluateFacility(fi);
    e += evaluateFacility(fj);
    setFunctionValue(e);
  }
  
  public void reAssignRandomFraction(double frac) {
    int n_ua = (int)Math.ceil(frac * this.fac2loc.length);
    int[] ua_locs = new int[n_ua];
    int[] fs = new int[this.fac2loc.length];
    int i;
    for (i = 0; i < this.fac2loc.length; i++)
      fs[i] = i; 
    removeRandom(ua_locs, fs);
    shuffleArray(ua_locs);
    for (i = 0; i < n_ua; i++) {
      this.fac2loc[fs[this.assigned]] = ua_locs[i];
      this.assigned++;
    } 
    setFunctionValue(-1.0D);
  }
  
  public void reAssignGreedyFFraction(double frac) {
    int n_ua = (int)Math.ceil(frac * this.fac2loc.length);
    int[] ua_locs = new int[n_ua];
    int[] fs = new int[this.fac2loc.length];
    for (int i = 0; i < this.fac2loc.length; i++)
      fs[i] = i; 
    removeRandom(ua_locs, fs);
    int[] f_ordered = ((InfoQAP)this.instance).getWeightedFacilities();
    for (int j = 0; j < this.fac2loc.length && 
      isPartial(); j++) {
      if (this.fac2loc[f_ordered[j]] == -1) {
        double sof = ((InfoQAP)this.instance).getSumOfFlow(f_ordered[j]);
        ArrayList<Integer> candidates = new ArrayList<>(1);
        int m = j;
        while (m < this.fac2loc.length && ((InfoQAP)this.instance).getSumOfFlow(f_ordered[m]) == sof) {
          if (this.fac2loc[f_ordered[m]] == -1)
            candidates.add(Integer.valueOf(m)); 
          m++;
        } 
        int pivot = ((Integer)candidates.get(RNG.get().nextInt(candidates.size()))).intValue();
        int f = f_ordered[pivot];
        f_ordered[pivot] = f_ordered[j];
        f_ordered[j] = f;
        double best = Double.MAX_VALUE;
        ArrayList<Integer> bestLoc = new ArrayList<>(1);
        for (int k = 0; k < n_ua; k++) {
          if (ua_locs[k] >= 0) {
            double e = evaluateFacilityAtLocation(f, ua_locs[k]);
            if (e <= best) {
              if (e < best) {
                bestLoc.clear();
                best = e;
              } 
              bestLoc.add(Integer.valueOf(k));
            } 
          } 
        } 
        pivot = ((Integer)bestLoc.get(RNG.get().nextInt(bestLoc.size()))).intValue();
        this.fac2loc[f] = ua_locs[pivot];
        ua_locs[pivot] = -1;
        this.assigned++;
      } 
    } 
    setFunctionValue(-1.0D);
  }
  
  public void reAssignGreedyLFraction(double frac) {
    int n_ua = (int)Math.ceil(frac * this.fac2loc.length);
    int[] ua_locs = new int[n_ua];
    int[] fs = new int[this.fac2loc.length];
    for (int i = 0; i < this.fac2loc.length; i++)
      fs[i] = i; 
    removeRandom(ua_locs, fs);
    int[] l_ordered = ((InfoQAP)this.instance).getWeightedLocations();
    for (int j = 0; j < this.fac2loc.length && 
      isPartial(); j++) {
      boolean l_ua = false;
      for (int m = 0; m < n_ua; m++) {
        if (l_ordered[j] == ua_locs[m]) {
          l_ua = true;
          break;
        } 
      } 
      if (l_ua) {
        double sod = ((InfoQAP)this.instance).getSumOfDist(l_ordered[j]);
        ArrayList<Integer> candidates = new ArrayList<>(1);
        int n = j;
        while (n < this.fac2loc.length && ((InfoQAP)this.instance).getSumOfDist(l_ordered[n]) == sod) {
          l_ua = false;
          for (int i1 = 0; i1 < n_ua; i1++) {
            if (l_ordered[n] == ua_locs[i1]) {
              l_ua = true;
              break;
            } 
          } 
          if (l_ua)
            candidates.add(Integer.valueOf(n)); 
          n++;
        } 
        int pivot = ((Integer)candidates.get(RNG.get().nextInt(candidates.size()))).intValue();
        int l = l_ordered[pivot];
        l_ordered[pivot] = l_ordered[j];
        l_ordered[j] = l;
        double best = Double.MAX_VALUE;
        ArrayList<Integer> bestFac = new ArrayList<>(1);
        for (int k = 0; k < n_ua; k++) {
          int index = fs.length + k - n_ua;
          if (fs[index] >= 0) {
            double e = evaluateFacilityAtLocation(fs[index], l);
            if (e <= best) {
              if (e < best) {
                bestFac.clear();
                best = e;
              } 
              bestFac.add(Integer.valueOf(index));
            } 
          } 
        } 
        pivot = ((Integer)bestFac.get(RNG.get().nextInt(bestFac.size()))).intValue();
        this.fac2loc[fs[pivot]] = l;
        fs[pivot] = -1;
        this.assigned++;
      } 
    } 
    setFunctionValue(-1.0D);
  }
  
  private double evaluateFacilityAtLocation(int f, int l) {
    this.fac2loc[f] = l;
    double ef = 0.0D;
    for (int i = 0; i < this.fac2loc.length; i++) {
      if (this.fac2loc[i] >= 0) {
        ef += (((InfoQAP)this.instance).getFlow(f, i) * ((InfoQAP)this.instance).getDistance(l, this.fac2loc[i]));
        ef += (((InfoQAP)this.instance).getFlow(i, f) * ((InfoQAP)this.instance).getDistance(this.fac2loc[i], l));
      } 
    } 
    this.fac2loc[f] = -1;
    return ef;
  }
  
  private void removeRandom(int[] ua_locs, int[] fs) {
    for (int i = 0; i < ua_locs.length; i++) {
      int pivot = RNG.get().nextInt(this.assigned);
      ua_locs[i] = this.fac2loc[fs[pivot]];
      this.fac2loc[fs[pivot]] = -1;
      this.assigned--;
      int temp_fac = fs[pivot];
      fs[pivot] = fs[this.assigned];
      fs[this.assigned] = temp_fac;
    } 
  }
  
  public void pmx(SolutionQAP o) {
    int i = 1 + RNG.get().nextInt(this.fac2loc.length - 1);
    int j = RNG.get().nextInt(this.fac2loc.length);
    if (i > j) {
      int temp = i;
      i = j;
      j = temp;
    } 
    Set<Integer> this_middle = new HashSet<>();
    for (int k = i; k < j; k++)
      this_middle.add(Integer.valueOf(this.fac2loc[k])); 
    Set<Integer> other_middle = new HashSet<>();
    for (int m = i; m < j; m++)
      other_middle.add(Integer.valueOf(o.fac2loc[m])); 
    HashMap<Integer, Integer> map = new HashMap<>();
    int key_k = i;
    int val_k = i;
    while (key_k < j && val_k < j) {
      while (key_k < j && this_middle.contains(Integer.valueOf(o.fac2loc[key_k])))
        key_k++; 
      while (val_k < j && other_middle.contains(Integer.valueOf(this.fac2loc[val_k])))
        val_k++; 
      if (key_k < j && val_k < j) {
        map.put(Integer.valueOf(o.fac2loc[key_k]), Integer.valueOf(this.fac2loc[val_k]));
        val_k++;
        key_k++;
      } 
    } 
    System.arraycopy(o.fac2loc, i, this.fac2loc, i, j - i);
    int n;
    for (n = 0; n < i; n++) {
      if (map.containsKey(Integer.valueOf(this.fac2loc[n])))
        this.fac2loc[n] = ((Integer)map.get(Integer.valueOf(this.fac2loc[n]))).intValue(); 
    } 
    for (n = j; n < this.fac2loc.length; n++) {
      if (map.containsKey(Integer.valueOf(this.fac2loc[n])))
        this.fac2loc[n] = ((Integer)map.get(Integer.valueOf(this.fac2loc[n]))).intValue(); 
    } 
    setFunctionValue(-1.0D);
  }
  
  public void ox(SolutionQAP o) {
    int i = 1 + RNG.get().nextInt(this.fac2loc.length - 1);
    int j = RNG.get().nextInt(this.fac2loc.length);
    if (i > j) {
      int temp = i;
      i = j;
      j = temp;
    } 
    Set<Integer> other_middle = new HashSet<>();
    for (int k = i; k < j; k++)
      other_middle.add(Integer.valueOf(o.fac2loc[k])); 
    int g = j;
    int m;
    for (m = j; m < this.fac2loc.length; m++) {
      if (!other_middle.contains(Integer.valueOf(this.fac2loc[m]))) {
        this.fac2loc[g] = this.fac2loc[m];
        g++;
      } 
    } 
    for (m = 0; m < j; m++) {
      if (!other_middle.contains(Integer.valueOf(this.fac2loc[m]))) {
        this.fac2loc[g % this.fac2loc.length] = this.fac2loc[m];
        g++;
      } 
    } 
    System.arraycopy(o.fac2loc, i, this.fac2loc, i, j - i);
    setFunctionValue(-1.0D);
  }
  
  public int swapBest(Set<Integer> tabu) {
    int best_index = -1;
    double best_value = Double.MAX_VALUE;
    int i;
    for (i = 0; i < this.fac2loc.length; i++) {
      for (int j = i + 1; j < this.fac2loc.length; j++) {
        int index = i * this.fac2loc.length + j;
        if (!tabu.contains(Integer.valueOf(index))) {
          double e_delta = 0.0D;
          e_delta -= evaluateFacility(i);
          e_delta -= evaluateFacility(j);
          int temp_loc = this.fac2loc[i];
          this.fac2loc[i] = this.fac2loc[j];
          this.fac2loc[j] = temp_loc;
          e_delta += evaluateFacility(i);
          e_delta += evaluateFacility(j);
          temp_loc = this.fac2loc[j];
          this.fac2loc[j] = this.fac2loc[i];
          this.fac2loc[i] = temp_loc;
          if (e_delta < best_value) {
            best_index = index;
            best_value = e_delta;
          } 
        } 
      } 
    } 
    if (best_index != -1) {
      i = best_index / this.fac2loc.length;
      int j = best_index % this.fac2loc.length;
      int temp_loc = this.fac2loc[i];
      this.fac2loc[i] = this.fac2loc[j];
      this.fac2loc[j] = temp_loc;
      setFunctionValue(getFunctionValue() + best_value);
    } 
    return best_index;
  }
  
  private double evaluateFacility(int f) {
    double ef = 0.0D;
    int loc = this.fac2loc[f];
    for (int i = 0; i < this.fac2loc.length; i++) {
      ef += (((InfoQAP)this.instance).getFlow(f, i) * ((InfoQAP)this.instance).getDistance(loc, this.fac2loc[i]));
      ef += (((InfoQAP)this.instance).getFlow(i, f) * ((InfoQAP)this.instance).getDistance(this.fac2loc[i], loc));
    } 
    return ef;
  }
  
  public static class SwapNH extends CombinationNH<InfoQAP> {
    public SwapNH(InfoQAP instance) {
      super(new RangeNH(0, instance.getN(), instance), 2, instance);
    }
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\QAP\SolutionQAP.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */