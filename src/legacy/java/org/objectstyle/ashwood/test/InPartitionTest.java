package org.objectstyle.ashwood.test;

import org.objectstyle.ashwood.util.*;

public class InPartitionTest {
  double distance;
  double[] hangerX;
  double[] ballX;
  int[][] strings;
  IntPartition regions;

  public void setMinDistance(double minDistance) {
    distance = minDistance;
  }

  public void setupPendulum(double[] hangers, double[] balls, int[][] strings) {
    hangerX = hangers;
    ballX = balls;
    this.strings = strings;
    regions = new IntPartition(ballX.length);
  }

  public void balance() {
    double balanceMeasure;
    double newBalanceMeasure = Double.POSITIVE_INFINITY;
    printElements();
    System.out.println("start balance measure: " + newBalanceMeasure);
    int iterations = 0;
    do {
      regions.reset();
      balanceMeasure = newBalanceMeasure;
      createRegions();
      joinRegions();
      move();
      newBalanceMeasure = balanceMeasure();
      printElements();
      System.out.println("balance measure: " + newBalanceMeasure);
    } while (newBalanceMeasure < balanceMeasure);
  }

  public static void main(String[] args) {
    double distance = 1.0;
    double[] hangerX = new double[]{2.0, 5.3, 7.5};
    double[] ballX = new double[]{0.0, 3.5, 4.5, 6.0, 9.0, 10.0};
    int[][] strings = new int[][] {{0}, {0}, {1}, {1}, {2}, {2}};

    double[] hangerX1 = new double[]{2.0};
    double[] ballX1 = new double[]{0.0, 3.5};
    int[][] strings1 = new int[][] {{0}, {0}};

    InPartitionTest test = new InPartitionTest();
    test.setupPendulum(hangerX1, ballX1, strings1);
    test.setMinDistance(distance);

    test.balance();
  }

  private void printElements() {
    System.out.println("---------");
    for (int i = 0; i < regions.size(); i++) {
      System.out.println(i + "(" + regions.findSetId(i) + ") = " + ballX[i]);
    }
  }

  private void joinRegions() {
    int regionCount;
    do {
      regionCount = regions.getSetCount();
      double leftNeighborRegionForce = Double.NEGATIVE_INFINITY;
      int leftNeighborRegionId = -1;
      double leftNeighborRightmostBallX = Double.NEGATIVE_INFINITY;
      for (int ball = 0; ball < ballX.length;) {
        int regionId = regions.findSetId(ball);
        int regionSize = 0;
        double regionForce = 0;
        double leftmostBallX = ballX[ball];
        double rightmostBallX;
        do {
          rightmostBallX = ballX[ball];
          regionForce += force(rightmostBallX, strings[ball]);
          regionSize++;
          ball++;
        } while (ball < ballX.length && regionId == regions.findSetId(ball));
        regionForce /= regionSize;
        if (leftmostBallX - leftNeighborRightmostBallX <= distance) {
          if ((leftNeighborRegionForce >= 0 && regionForce <= 0) ||
              (regionForce >= 0 && leftNeighborRegionForce > regionForce) ||
              (leftNeighborRegionForce < 0 && regionForce < leftNeighborRegionForce)) {
            regions.joinSets(leftNeighborRegionId, regionId);
          }
        }
        leftNeighborRegionForce = regionForce;
        leftNeighborRegionId = regions.findSetId(regionId);
        leftNeighborRightmostBallX = rightmostBallX;
      }
    } while (regionCount > regions.getSetCount());
  }

  private void createRegions() {
    double leftX = Double.NEGATIVE_INFINITY;
    double leftForce = Double.NEGATIVE_INFINITY;
    int leftRegionId = -1;
    for (int i = 0; i < ballX.length; i++) {
      double x = ballX[i];
      double force = force(x, strings[i]);
      int regionId = regions.findSetId(i);
      if (x - leftX <= distance) {
        if ((leftForce * force > 0) || (leftForce == force))
          regions.joinSets(leftRegionId, regionId);
      }
      leftRegionId = regions.findSetId(regionId);
      leftForce = force;
      leftX = x;
    }
  }

  private void move() {
    for (int ball = 0; ball < ballX.length;) {
      int regionId = regions.findSetId(ball);
      int regionSize = 0;
      int leftmostBall = ball;
      double regionForce = 0;
      double leftmostBallX = ballX[leftmostBall];
      double rightmostBallX;
      do {
        rightmostBallX = ballX[ball];
        regionForce += force(rightmostBallX, strings[ball]);
        regionSize++;
        ball++;
      } while (ball < ballX.length && regionId == regions.findSetId(ball));
      int rightmostBall = ball - 1;
      regionForce /= regionSize;
      double moveDistance = 0.0;
      if (regionForce < 0) {
        moveDistance = - (leftmostBall > 0 ?
                          Math.min(-regionForce, leftmostBallX - ballX[leftmostBall-1] - distance):
                          -regionForce);
      } else if (regionForce > 0) {
        moveDistance = (rightmostBall < ballX.length - 1 ?
                        Math.min(regionForce, ballX[rightmostBall+1] - rightmostBallX - distance):
                        regionForce);
      }
      for (int i = leftmostBall; i <= rightmostBall; i++) {
        ballX[i] += moveDistance;
      }
    }
  }

  private double force(double x, int[] strings) {
    double value = 0;
    for (int i = 0; i < strings.length; i++) {
      value += hangerX[strings[i]] - x;
    }
    value /= strings.length;
    return value;
  }

  private double balanceMeasure() {
    double value = 0.0;
    int stringCount = 0;
    for (int ball = 0; ball < ballX.length; ball++) {
      double ballValue = 0;
      for (int i = 0; i < strings[ball].length; i++) {
        ballValue += hangerX[strings[ball][i]] - ballX[ball];
        stringCount += strings[ball].length;
      }
      value += ballValue;
    }
    value = Math.abs(value);
    value /= stringCount;
    return value;
  }
}