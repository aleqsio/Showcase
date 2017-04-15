package com.aleqsio.testscanning.geometry;

import android.graphics.Point;

/**
 * Created by Alek on 2016-05-17.
 */
public class Line {
    public boolean vertical = false;
    public double a;
    public double b;
    public double x;
    public int answernumber;
    public int questionnumber;
    public boolean ispositionline=false;
    public boolean iscodeline=false;
    public Line(Point p1, Point p2,int lineanswernumber) {
        if(lineanswernumber==0)
        {
            ispositionline=true;
        }
        if(lineanswernumber==-1)
        {
            iscodeline=true;
        }
     create(p1,p2);
        answernumber=lineanswernumber;
    }
    public Line(Point p1, Point p2) {
        create(p1, p2);
    }
    public Line(boolean isvertical, double coeff_a, Point p) {
       if(isvertical)
       {
           vertical=true;
           x=p.x;
       }else
       {
           a=coeff_a;
           b=p.y-a*p.x;
       }
    }
    public Line(boolean isvertical, double coeff_a, Point p,int _questionnumber) {
        if(isvertical)
        {
            vertical=true;
            x=p.x;
        }else
        {
            a=coeff_a;
            b=p.y-a*p.x;
        }
        questionnumber=_questionnumber;
    }
    public int getquestionnumber()
    {
       return questionnumber;
    }
    void create(Point p1,Point p2)
    {
        if (p1.x == p2.x) {
            vertical = true;
            x=p1.x;
        } else {
            vertical = false;
            a = (p2.y - p1.y) / (double)((p2.x - p1.x));
            b = p1.y - a * p1.x;
        }
    }

   public double getYfromX(float x)
   {
       if(!vertical) {
           return a * x + b;
       }else
       {
           return -1;
       }
   }
    public double getdistancefrompoint(Point p1)
    {
        double dist=0;
        if(!vertical) {
            dist = Math.abs(a * p1.x - 1 * p1.y + b) / Math.sqrt(Math.pow(a, 2) + 1);
        }else
        {
           dist= Math.abs(p1.x-x);
        }
        return dist;
    }
}
