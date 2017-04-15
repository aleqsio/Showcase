package com.aleqsio.testscanning.geometry;

import android.graphics.Point;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Created by Alek on 2016-05-18.
 */
public class AnswerPoint {
    public int x;
    public int y;
    public int choice;
    public boolean iserror=false;
    public boolean isposition=false;
    public ArrayList<Integer> answers=new ArrayList<>();
    public AnswerPoint(int _x, int _y, int _choice)
    {
        x=_x;
        y=_y;
        if(choice==-1)
        {
            iserror=true;
        }
        if(choice==0)
        {
            isposition=true;
        }
        choice=_choice;
    }
    public boolean isbelowline(Line l1)
    {
        if(l1.vertical)
        {
            return x<l1.x;
        }else {
            return y < l1.getYfromX(x);
        }
    }

    public Point getPoint() {
        return new Point(x,y);
    }
    public Point getscaledPoint(int scale) {
        return new Point(x*scale,y*scale);
    }
    public int getclosestline(ArrayList<Line> positionquestionlines) {
        if(positionquestionlines.size()>0) {
            double dist = Double.MAX_VALUE;
            int index=0;
            for (Line l : positionquestionlines
                    ) {
                double currdist = l.getdistancefrompoint(this.getPoint());
                if (currdist < dist) {
                    dist = currdist;
                    index = positionquestionlines.indexOf(l);
                }
            }
            return index;
        }else
        {
            return -1;
        }
    }
    public int getclosestlinequestionnumber(ArrayList<Line> positionquestionlines,Point p) {
        if(positionquestionlines.size()>0) {
            double dist = Double.MAX_VALUE;
            int questionnumber=0;
            for (Line l : positionquestionlines
                    ) {
                double currdist = l.getdistancefrompoint(p);
                if (currdist < dist) {
                    dist = currdist;
                    questionnumber = l.getquestionnumber();
                }
            }
            return questionnumber;
        }else
        {
            return -1;
        }
    }
    public String getAnswers() {
        String ans="";
        Collections.sort(answers);
ArrayList<Integer> answers2=new ArrayList<>();
        for (Integer i:answers) {

            if(!answers2.contains(i) &&i!=0) { //to remove validation_grid
                ans = ans + "" + i;
                answers2.add(i);
            }
        }
        if(ans.equals(""))
        {
            return "0";
        }else
        {
            return ans;
        }

    }
    public String getIntegrityGrid() {
        String ans="";
        Collections.sort(answers);
        ArrayList<Integer> answers2=new ArrayList<>();
        for (Integer i:answers) {

            if(!answers2.contains(i) &&i==0) { //to remove validation_grid
                ans = ans + "" + i;
                answers2.add(i);
            }
        }
        if(ans.equals("0"))
        {
            return "1";
        }else
        {
            return "0";
        }

    }

    public String getPosString() {
        return String.valueOf("X"+x+" Y"+y);
    }
}
