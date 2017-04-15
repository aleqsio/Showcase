package com.aleqsio.testscanning.formats;

/**
 * Created by Alek on 2016-06-17.
 */
public class Test_layout {
    public static int count_of_questions=11;
    public static int max_count_of_choices=4;
    public static String integritygrid; //0010101
    public static String answersgrid; //1_3_24_15
    public static String creditgrid; //5_6_3_2_5
public static int max_score;
    public static int max_points;

    public Test_layout() {

    }

    public boolean verifyintegritygrid(String integritygrid_param) {
        integritygrid.replaceAll("_", "");
        integritygrid_param.replaceAll("_", "");
        if (integritygrid_param.equals(integritygrid)) {
            return true;
        } else {
            return false;
        }
    }
    public int generatescore(String answers)
    {
        String[] correctanswersarray = answersgrid.split("_");
        String[] answersarray = answers.split("_");
        String[] scoring = creditgrid.split("_");
        int currentscore=0;
        if(answersarray.length<count_of_questions)
        {
            return -1;
        }
        for(int i=1;i<=count_of_questions;i++)
        {
if(answersarray[i-1].equals(correctanswersarray[i-1]))
{
    currentscore=currentscore+ Integer.parseInt(scoring[i-1]);
}
        }
        return currentscore;
    }
    public int get_max_score()
    {
        String[] scoring = creditgrid.split("_");
        int currentscore=0;
        for(int i=1;i<=count_of_questions;i++)
        {
                currentscore=currentscore+ Integer.parseInt(scoring[i-1]);
        }
        return currentscore;
    }
}
