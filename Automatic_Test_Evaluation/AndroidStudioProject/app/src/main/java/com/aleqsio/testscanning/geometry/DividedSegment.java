package com.aleqsio.testscanning.geometry;

import android.graphics.Point;

import java.util.ArrayList;

/**
 * Created by Alek on 2016-05-17.
 */
public class DividedSegment {
    public ArrayList<Point> divisionpoints=new ArrayList<>();
    float changex;
    float changey;
    public DividedSegment(Point p1, Point p2, int segments)
    {
 changex=(p2.x-p1.x)/(float)segments;
 changey=(p2.y-p1.y)/(float)segments;
        for(int i=1;i<segments;i++)
        {
            divisionpoints.add(new Point((int)(p1.x+i*changex),(int)(p1.y+i*changey)));
        }
    }
    public double getdistancebetweendivisions()
    {
      return Math.sqrt(Math.pow(changex,2)+Math.pow(changey,2));
    }
}
