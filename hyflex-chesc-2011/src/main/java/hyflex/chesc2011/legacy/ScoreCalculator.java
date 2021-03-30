package hyflex.chesc2011.legacy;

import java.util.ArrayList;
import java.util.Collections;

/*
 * @author Dr Matthew Hyde
 * ASAP Research Group, School of Computer Science, University of Nottingham
 * 
 * This program is for use when developing Hyper-Heuristics 
 * for the Cross-Domain Heuristic Search Challenge 2011.
 * It allows the comparison of your Hyper-Heuristic's 
 * scores against some Hyper-Heuristics that have been
 * developed by us during the preparation and testing of the competition software.
 * 
 * Once the objective function values found by your hyper-heuristic 
 * (each over 10 minutes) have been entered, the program outputs the relative scores
 * 
 * disclaimer:
 * -We provide this test program ONLY as a rough benchmark.
 * -We make no guarantees about the quality (or lack thereof)
 *  of these hyper-heuristics, but we believe they are of average quality. 
 * -Success against these hyper-heuristics will not necessarily translate 
 * into success in the competition, as that depends on the quality of the 
 * other competitors' hyper-heuristics.
 * 
 * please report any bugs to Matthew Hyde at mvh@cs.nott.ac.uk
 */

public class ScoreCalculator {

  //please replace the ten '0's in each problem domain 
  //with your scores for each of the 10 training instances
  static final double[] YourSAT = {6.0, 25.0, 21.0, 3.0, 8.0, 14.0, 6.0, 5.0, 9.0, 213.0};
  static final double[] YourBinPacking = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  static final double[] YourPersonnelScheduling = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  static final double[] YourFlowshop = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

  /*the competition will use 5 instances, and we provide 10 training instances so choose the 
   * instances from the training set that you would like to use for this 'mock' competition. */
  static final int number_of_instances = 10;
  static final int[] chosen_instances_sat = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
  static final int[] chosen_instances_binpacking = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
  static final int[] chosen_instances_personnelscheduling = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
  static final int[] chosen_instances_flowshop = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

  static final int[][] YourInstances = {
    chosen_instances_sat,
    chosen_instances_binpacking,
    chosen_instances_personnelscheduling, 
    chosen_instances_flowshop
  };
  static final double[][] YourResults = {
    YourSAT, 
    YourBinPacking, 
    YourPersonnelScheduling, 
    YourFlowshop
  };

  /**
   * Main method of ScoreCalculator class.
   */
  public static void main(String[] argv) {
    int instances = 10;
    //this adds your results (input above) to the end of the existing score arrays
    for (int d = 0; d < 4; d++) {
      for (int i = 0; i < instances; i++) {
        results[d][i][8] = YourResults[d][i];
      }
    }

    int hyperheuristics = 9;
    double[] scores = new double[hyperheuristics];
    double[] basescores = {10,8,6,5,4,3,2,1,0,0,0,0,0,0,0};
    double totaldomainscores = 0;
    for (double g : basescores) {
      totaldomainscores += g;
    }
    totaldomainscores *= instances;//total available per domain
    for (int domain = 0; domain < 4; domain++) {
      double[] domainscores = new double[hyperheuristics];
      for (int i = 0; i < number_of_instances; i++) {
        int instance = YourInstances[domain][i];
        double[] res = results[domain][instance];
        ArrayList<Score> al = new ArrayList<Score>();
        for (int s = 0; s < res.length; s++) {
          Score obj = new Score(s, res[s]);
          al.add(obj);
        }
        Collections.sort(al); 
        double lastscore = Double.POSITIVE_INFINITY;
        int scoreindex = 0;
        double tieaverage = 0;
        //int tienumber = 0;
        ArrayList<Integer> list = new ArrayList<Integer>();
        while (true) {
          Score test1 = null;
          if (scoreindex < al.size()) {
            test1 = al.get(scoreindex);
          }
          if (test1 == null || test1.score != lastscore) {
            double average = tieaverage / list.size();
            for (int f = 0; f < list.size(); f++) {
              scores[list.get(f)] += average;
              domainscores[list.get(f)] += average;
            }
            if (test1 == null) {
              break; //all HH have been given a score
            }
            //tienumber = 0;
            tieaverage = 0;
            list = new ArrayList<Integer>();
            list.add(test1.num);
            //tienumber++;
            tieaverage += basescores[scoreindex];
          } else {
            list.add(test1.num);
            //tienumber++;
            tieaverage += basescores[scoreindex];
          } 
          lastscore = test1.score;
          scoreindex++;
        }
      }
      String d = "";
      switch (domain) {
        case 0:
          d = "SAT";
          break;
        case 1:
          d = "Bin Packing";
          break;
        case 2:
          d = "Personnel Scheduling";
          break;
        case 3:
          d = "Flow Shop";
          break;
        default:
          break;
      }
      System.out.println(d);
      double domaintotal = 0;
      for (int g = 0; g < domainscores.length; g++) {
        domaintotal += domainscores[g];
        if (g == 8) {
          System.out.println("YourHH" + " " + domainscores[g]);
        } else {
          System.out.println("TestHH" + (g + 1) + " " + domainscores[g]);
        }
      }
      System.out.println();
      //sanity check
      if (Math.round(domaintotal) != totaldomainscores) {
        System.err.println(
            "Error, total scores for this domain ("
            + domaintotal + ") do not add up to " + totaldomainscores);
        System.err.println(
            "This represents a bug."
            + "Please email this java class file to Dr Matthew Hyde at mvh@cs.nott.ac.uk"
        );
        System.exit(-1);
      }
    }
    System.out.println("Overall Total ");
    for (int g = 0; g < scores.length; g++) {
      if (g == 8) {
        System.out.println("YourHH" + " " + scores[g]);
      } else {
        System.out.println("TestHH" + (g + 1) + " " + scores[g]);
      }
    }
  }

  static double[] SAT0 = {46.0, 33.0, 14.0, 28.0, 119.0, 12.0, 56.0, 40.0, -1.0};
  static double[] SAT1 = {40.0, 33.0, 36.0, 50.0, 136.0, 34.0, 38.0, 66.0, -1.0};
  static double[] SAT2 = {32.0, 24.0, 28.0, 47.0, 116.0, 29.0, 35.0, 53.0,  -1.0};
  static double[] SAT3 = {16.0, 13.0, 35.0, 24.0, 60.0, 15.0, 15.0, 25.0,  -1.0};
  static double[] SAT4 = {9.0, 10.0, 45.0, 37.0, 70.0, 33.0, 9.0, 36.0, -1.0};
  static double[] SAT5 = {22.0, 17.0, 52.0, 52.0, 106.0, 51.0, 24.0, 55.0, -1.0};
  static double[] SAT6 = {6.0, 6.0, 8.0, 12.0, 18.0, 9.0, 5.0, 15.0, -1.0};
  static double[] SAT7 = {6.0, 6.0, 8.0, 11.0, 13.0, 11.0, 6.0, 14.0,  -1.0};
  static double[] SAT8 = {8.0, 7.0, 11.0, 16.0, 21.0, 12.0, 9.0, 19.0, -1.0};
  static double[] SAT9 = {211.0, 211.0, 221.0, 239.0, 259.0, 215.0, 217.0, 239.0, -1.0};

  static double[] BinPacking0 = {
    0.017355444997235736, 
    0.017589828634604343, 
    0.010814962593515554, 
    0.012038570241063584, 
    0.05409300813007656, 
    0.01570127142067379, 
    0.021652495175074615, 
    0.071442404723563, 
    -1.0
  };
  static double[] BinPacking1 = {
    0.016317522412388152, 
    0.016512469437653055, 
    0.007052361452360989, 
    0.0076789516789511625, 
    0.05007136752136332, 
    0.012856209150326148, 
    0.021442384823847638, 
    0.07121541303773904, 
    -1.0
  };
  static double[] BinPacking2 = {
    0.02379012903225841, 
    0.022925387096774497, 
    0.02472829032258106, 
    0.023020677419354985, 
    0.028263322580645567, 
    0.02313741935483904, 
    0.02359367741935514, 
    0.030762322580645374, 
    -1.0
  };
  static double[] BinPacking3 = {
    0.024800918032786945, 
    0.02493901639344298, 
    0.02662370491803312, 
    0.02425308196721343, 
    0.030760393442622935, 
    0.025728360655737803, 
    0.02550701639344255, 
    0.03267881967213149, 
    -1.0
  };
  static double[] BinPacking4 = {
    0.006383520599250847, 
    0.006192426133999396, 
    3.4269005847942235E-4,
    0.004612734082396819, 
    0.01511592039801013, 
    0.006563628797336407, 
    0.007271410736578998, 
    0.021792482445270323, 
    -1.0
  };
  static double[] BinPacking5 = {
    0.003981990912846345, 
    0.008451522633744935, 
    0.0035510945890124823, 
    0.0035811648079304703, 
    0.01868651086510842, 
    0.008943374485596012, 
    0.008970370370370295, 
    0.02338022875817014, 
    -1.0
  };
  static double[] BinPacking6 = {
    0.11451368539326023, 
    0.10466342372881376, 
    0.010692142857143039, 
    0.031228982352942358, 
    0.17152959782608768, 
    0.053767941860466295, 
    0.13861741828255003, 
    0.16656859945504243, 
    -1.0
  };
  static double[] BinPacking7 = {
    0.1336636111111108, 
    0.1285252841225597, 
    0.01676015727003133, 
    0.06399423988439246, 
    0.17190128804347815, 
    0.09418119886363552, 
    0.15059228885831988, 
    0.17990116756756647, 
    -1.0
  };
  static double[] BinPacking8 = {
    0.055936305441261935, 
    0.05688505484462947, 
    0.056182129798915015, 
    0.09438903803132759, 
    0.09265042581803007, 
    0.060832650273230815, 
    0.05817199634539194, 
    0.1267429010989034, 
    -1.0
  };
  static double[] BinPacking9 = {
    0.01350456081081508, 
    0.0128112112676112, 
    0.01900409882089893, 
    0.030852287946445678, 
    0.03437108635099517, 
    0.016587802136040497, 
    0.013911542792799825, 
    0.04275158069885232, 
    -1.0
  };

  static double[] PersonnelScheduling0 = {
    3346.0, 
    3321.0, 
    3389.0, 
    3318.0, 
    3338.0, 
    8017.0, 
    3344.0, 
    3342.0,  
    -1.0
  };

  static double[] PersonnelScheduling1 = {
    2220.0, 
    2315.0, 
    2400.0, 
    2275.0, 
    2454.0, 
    21008.0, 
    2095.0, 
    2893.0,  
    -1.0
  };

  static double[] PersonnelScheduling2 = {
    390.0, 
    400.0, 
    495.0, 
    375.0, 
    400.0, 
    905.0, 
    355.0, 
    340.0,  
    -1.0
  };

  static double[] PersonnelScheduling3 = {
    23.0, 
    17.0, 
    32.0, 
    19.0, 
    16.0, 
    80.0, 
    22.0, 
    16.0,  
    -1.0
  };
  static double[] PersonnelScheduling4 = {
    23.0, 
    26.0, 
    32.0, 
    24.0, 
    24.0, 
    81.0, 
    19.0, 
    28.0,  
    -1.0
  };
  static double[] PersonnelScheduling5 = {
    17.0, 
    17.0, 
    32.0, 
    24.0, 
    22.0, 
    81.0, 
    28.0, 
    38.0,  
    -1.0
  };
  static double[] PersonnelScheduling6 = {1111.0,
    1119.0,
    1231.0,
    1118.0,
    1113.0,
    35391.0,
    1211.0,
    1490.0,
    -1.0
  };
  static double[] PersonnelScheduling7 = {2188.0,
    2202.0,
    2205.0,
    2221.0,
    2288.0,
    46661.0,
    2275.0,
    3959.0,
    -1.0
  };
  static double[] PersonnelScheduling8 = {3163.0,
    3255.0,
    3465.0,
    3360.0,
    3354.0,
    46952.0,
    3414.0,
    6905.0,
    -1.0
  };
  static double[] PersonnelScheduling9 = {11486.0,
    9706.0,
    12505.0,
    12994.0,
    9771.0,
    105850.0,
    9807.0,
    17224.0,
    -1.0
  };

  static double[] FlowShop0 = {6365.0,
    6380.0,
    6399.0,
    6312.0,
    6393.0,
    6297.0,
    6375.0,
    6323.0,
    -1.0};
  static double[] FlowShop1 = {6327.0,
    6330.0,
    6337.0,
    6281.0,
    6328.0,
    6253.0,
    6335.0,
    6288.0,
    -1.0
  };
  static double[] FlowShop2 = {6401.0,
    6410.0,
    6401.0,
    6339.0,
    6418.0,
    6339.0,
    6407.0,
    6364.0,
    -1.0
  };
  static double[] FlowShop3 = {6388.0,
    6408.0,
    6366.0,
    6327.0,
    6373.0,
    6366.0,
    6371.0,
    6363.0,
    -1.0
  };
  static double[] FlowShop4 = {6461.0,
    6470.0,
    6438.0,
    6392.0,
    6483.0,
    6405.0,
    6478.0,
    6422.0,
    -1.0
  };
  static double[] FlowShop5 = {10540.0,
    10546.0,
    10506.0,
    10499.0,
    10547.0,
    10509.0,
    10546.0,
    10542.0,
    -1.0
  };
  static double[] FlowShop6 = {10976.0,
    10965.0,
    10965.0,
    10923.0,
    10980.0,
    10923.0,
    10965.0,
    10956.0,
    -1.0
  };
  static double[] FlowShop7 = {26483.0,
    26490.0,
    26538.0,
    26409.0,
    26506.0,
    26418.0,
    26512.0,
    26396.0,
    -1.0
  };
  static double[] FlowShop8 = {26979.0,
    26929.0,
    26978.0,
    26890.0,
    26913.0,
    26920.0,
    26960.0,
    26800.0,
    -1.0
  };
  static double[] FlowShop9 = {26755.0,
    26794.0,
    26833.0,
    26731.0,
    26755.0,
    26715.0,
    26811.0,
    26716.0,
    -1.0
  };

  static double[][] SAT = {SAT0,
    SAT1,
    SAT2,
    SAT3,
    SAT4,
    SAT5,
    SAT6,
    SAT7,
    SAT8,
    SAT9
  };
  static double[][] BinPacking = {BinPacking0,
    BinPacking1,
    BinPacking2,
    BinPacking3,
    BinPacking4,
    BinPacking5,
    BinPacking6,
    BinPacking7,
    BinPacking8,
    BinPacking9
  };
  static double[][] PersonnelScheduling = {PersonnelScheduling0,
    PersonnelScheduling1,
    PersonnelScheduling2,
    PersonnelScheduling3,
    PersonnelScheduling4,
    PersonnelScheduling5,
    PersonnelScheduling6,
    PersonnelScheduling7,
    PersonnelScheduling8,
    PersonnelScheduling9
  };
  static double[][] FlowShop = {FlowShop0,
    FlowShop1,
    FlowShop2,
    FlowShop3,
    FlowShop4,
    FlowShop5,
    FlowShop6,
    FlowShop7,
    FlowShop8,
    FlowShop9
  };

  static double[][][] results = {SAT,
    BinPacking,
    PersonnelScheduling,
    FlowShop
  };

  public static class Score implements Comparable<Score> {
    int num;
    double score;

    public Score(int n, double s) {
      num = n;
      score = s;
    }
  
    /**
      * .
    */
    public int compareTo(Score o) {
      Score obj = (Score)o;
      if (this.score < obj.score) {
        return -1;
      } else if (this.score == obj.score) {
        return 0;
      } else {
        return 1;
      }
    }
  }
}
