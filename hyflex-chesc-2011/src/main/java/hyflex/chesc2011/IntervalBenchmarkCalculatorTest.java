package hyflex.chesc2011;


public class IntervalBenchmarkCalculatorTest {
  private IntervalBenchmarkCalculator toTest;

  /**
   * .
   * @param args .
   */
  public static void main(String[] args) {
    IntervalBenchmarkCalculatorTest testClass = new IntervalBenchmarkCalculatorTest();
    
    testClass.testMetric();
  }

  

  private void prepare() {
    toTest = new IntervalBenchmarkCalculator();
  }

  /**
   * .
   */
  public void testMetric() {
    prepare();
    try {
      //test endpoints
      double test1 = toTest.getMetric(1000, 1, 1);
      if (Math.abs(test1 - toTest.intervalTo) >= 0.1) {
        System.out.println("Test1 failed");
      } else {
        System.out.println("Test1 passed");
      }

      double test2 = toTest.getMetric(1000, 1, 1000);
      if (Math.abs(test2 - toTest.intervalFrom) >= 0.1) {
        System.out.println("Test2 failed" + test2 + " " + toTest.intervalFrom);
      } else {
        System.out.println("Test2 passed");
      }

      //test middle
      double test3 = toTest.getMetric(1000, 1, 500);
      if (Math.abs(test3 - (toTest.intervalTo - toTest.intervalFrom) / 2) >= 0.1) {
        System.out.println("Test3 failed");
      } else {
        System.out.println("Test3 passed");
      }

      //test bad input
      try {
        toTest.getMetric(10, -1, 1);

        System.out.println("Test4 failed - expected exception");
      } catch (Exception e) {
        System.out.println("Test4 passed");
      }

      try {
        toTest.getMetric(-10, 1, 1);

        System.out.println("Test5 failed - expected exception");
      } catch (Exception e) {
        System.out.println("Test5 passed");
      }

      try {
        toTest.getMetric(10, 1, -1);

        System.out.println("Test4 failed - expected exception");
      } catch (Exception e) {
        System.out.println("Test4 passed");
      }

      try {
        toTest.getMetric(-10, -42, -11);

        System.out.println("Test5 failed - expected exception");
      } catch (Exception e) {
        System.out.println("Test5 passed");
      }

      try {
        toTest.getMetric(42, 6, 3);

        System.out.println("Test6 failed - expected exception");
      } catch (Exception e) {
        System.out.println("Test6 passed");
      }

      try {
        toTest.getMetric(2, 10, 3);

        System.out.println("Test7 failed - expected exception");
      } catch (Exception e) {
        System.out.println("Test7 passed");
      }

    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
}
