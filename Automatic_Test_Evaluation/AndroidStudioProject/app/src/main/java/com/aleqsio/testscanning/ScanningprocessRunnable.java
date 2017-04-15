package com.aleqsio.testscanning;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.aleqsio.testscanning.geometry.AnswerPoint;
import com.aleqsio.testscanning.geometry.DividedSegment;
import com.aleqsio.testscanning.geometry.Line;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class ScanningprocessRunnable implements Runnable {
    public static  int AMMOUNT_OF_EXTERNAL_POINTS_TO_REMOVE =5;
    public static  int MAX_ACCEPTABLE_DIFFERENCE_BETWEEN_BLACKSQUARE_WIDTH_AND_HEIGHT =5;
    public static  int  MAX_ERROR_BETWEEN_ABCD_LINE_AND_POINT=3; // THE GREATER THE SMALLER MAX ERROR
    public static  int  BOUNDING_BOX_MARGIN=6; // THE GRATER THE SMALLER MARGIN
    public static  float  BOUNDING_BOX_X_DEVIATION=3.4f; // THE GRATER THE SMALLER MARGIN
    public static  float BOUNDING_BOX_Y_DEVIATION =1f; // THE GRATER THE SMALLER MARGIN
    public static  int MAX_DIFFERENCE_PERSPECTIVE =18; // THE GRATER THE SMALLER MARGIN
    public static  int MIN_COLOR_DIFFERENCE_BETWEEN_SQUARE_AND_SIDES=20;

   private boolean terminatethread = false;
    Camera.Size framesize;
    byte[] data=null;

    Bitmap maingrayscalebitmap;
    Bitmap cornersdetectionbitmap; //two times smaller

    Rect boundingbox;
    Point middleofboundingbox;
    Line topline;

    ArrayList<Point> blacksquares = new ArrayList<>(); //all detected
    ArrayList<Point> sortedcorners = new ArrayList<>(); //from topleft, clockwise, looking at grayscalebitmap (90deg rotation)
    ArrayList<Point> answerpoints = new ArrayList<>(); //not corners
    ArrayList<AnswerPoint> sortedanswerpoints = new ArrayList<>(); //user marked points
    ArrayList<AnswerPoint> sortedpositionpoints = new ArrayList<>(); //marking squares
    ArrayList<Line> choiceslines = new ArrayList<>(); //first is always the 0 line (position squares), -1 means error and aborts
    ArrayList<Line> positionquestionlines = new ArrayList<>(); //contains question lines created from pos points
    DividedSegment leftoptions;

    boolean abortflag;

    int numberofchoices;
    int numberofquestions;

    char left_right_tilt='C';

    String issue_description="";

    long counter=0;

    @Override
    public void run() {
        while (!terminatethread) {
            Log.i("RUN","RUN");
            if (data != null) {
                counter++;
                numberofchoices = TestScanningActivity.test_layout.max_count_of_choices;
                numberofquestions = TestScanningActivity.test_layout.count_of_questions;
                framesize = ScanningSetupClass.imageframesize;
                left_right_tilt='C';

                abortflag = false;
                issue_description = "";
                createbitmapsfromdata(); //creates grayscale avreaged bitmap
                createcornerdetectionbitmap();// -||- corner detection bitmap
                findblacksquarepoints(); //looks for squares using cross pattern

                //curently blacksquares holds a list of black squares located in the image

                removeduplicates();
                //removes overlapping points with margin
                if (blacksquares.size() < 6 + AMMOUNT_OF_EXTERNAL_POINTS_TO_REMOVE * 2) { //checks size
                    abortflag = true;
                    issue_description = "notenoughsquares";
                }
                Point distances = new Point(0, 0);//once used two dots for orientation mesure, now only one (top)
                if (!abortflag) {
                    boundingbox = findboundingbox(); //adjusts bounding box, finds corners
                }
                if (!abortflag) {
                    removepointsoutsideboundingbox();
                    distances = sortcorners();
                    improveboundingbox();
                    removepointsoutsideboundingbox();
                }
                if (sortedcorners.size() < 6) {
                    abortflag = true;
                    if (issue_description == "") {
                        issue_description = "notenoughsquaresinboundingbox failed to sort";
                    }
                }

                if (!abortflag) {
                    //currently has all 6 corners in sortedcorners
                    boolean orientationcorrect = verifyorientation(distances); //checks orientation
                    if (!orientationcorrect) {
                        abortflag = true;

                    }
                }
                if (!abortflag) {
                    answerpoints.clear(); //finds answer points
                    findanswerpoints();
                    //answerpoints contains squares inside boundingbox
                    create_abcd_lines(); //creates lines for all options of choice
                    identifypointsbychoice(leftoptions.getdistancebetweendivisions() / MAX_ERROR_BETWEEN_ABCD_LINE_AND_POINT); //arranges them based on a margin

                }
                verifypositionpointscount();
                if (!abortflag) {
                    sortpositionpointslefttoright();// sorts them top to bottom irl
                    createquestionlineforeachposition();
                    assignanswerpointstoeachquestion();
                    issue_description = "success";
                }
                if(left_right_tilt!='C')
                {
                    abortflag=true;
                }
                senddatatoactivity();
            }


            data= ScanningSetupClass.receiveddata;
        }

    }

    private void senddatatoactivity() {
        Message msg = new Message();
        Bundle bnd = new Bundle();
        bnd.putChar("angle",left_right_tilt);
        bnd.putString("error", issue_description);
        bnd.putLong("counter", counter);
        bnd.putString("integrity", "");
        bnd.putString("answers", "");
        if(abortflag) {
            bnd.putBoolean("success",false);

        }else
        {
            bnd.putBoolean("success",true);
            bnd.putString("integrity", generateintegritygrid());

            bnd.putString("answers", generateanswersstring());
        }
        msg.setData(bnd);
        TestScanningActivity.mainthreadhandler.sendMessage(msg);


}

    private void verifypositionpointscount() {
        if (sortedpositionpoints.size() < numberofquestions || sortedpositionpoints.size() > numberofquestions) {
            abortflag = true;
        }
    }

    private String generateanswersstring() {
        String result_answers = "";
        for (AnswerPoint pospoint : sortedpositionpoints
                ) {
            result_answers = result_answers + pospoint.getAnswers() + "_";
        }
        if(result_answers.length()==0)
        {
            result_answers=".";
        }
        return result_answers.substring(0,result_answers.length()-1);
    }
    private String generateintegritygrid() {
        String result_answers = "";
        for (AnswerPoint pospoint : sortedpositionpoints
                ) {
            result_answers = result_answers + pospoint.getIntegrityGrid() + "_";
        }
        if(result_answers.length()==0)
        {
            result_answers="_";
        }
        return result_answers.substring(0,result_answers.length()-1);
    }

    private void assignanswerpointstoeachquestion() {
        for (AnswerPoint p : sortedanswerpoints) {
            int questionnumber = p.getclosestlinequestionnumber(positionquestionlines, p.getPoint());
            AnswerPoint temppoint = sortedpositionpoints.get(questionnumber);
            if(p.choice==-1) {
                temppoint.answers.add(0);
            }else
            {
                temppoint.answers.add(p.choice);
            }
            sortedpositionpoints.set(questionnumber, temppoint);
        }
    }

    private void createquestionlineforeachposition() {
        positionquestionlines.clear();
        topline = new Line(sortedcorners.get(4), sortedcorners.get(0));
        for (AnswerPoint position_point : sortedpositionpoints) {
            Line l = new Line(topline.vertical, topline.a, position_point.getPoint(), sortedpositionpoints.indexOf(position_point));
            positionquestionlines.add(l);
        }
    }



    private void sortpositionpointslefttoright() {
        Collections.sort(sortedpositionpoints, new Comparator<AnswerPoint>() {
            @Override
            public int compare(AnswerPoint o1, AnswerPoint o2) {
                if (o1.x > o2.x)
                    return 1;
                if (o1.x < o2.x)
                    return -1;
                return 0;

            }
        });
    }

    private void improveboundingbox() {
        int left = Math.min(sortedcorners.get(0).x, sortedcorners.get(4).x);
        int top = Math.min(sortedcorners.get(0).y, sortedcorners.get(2).y);
        int right = Math.max(sortedcorners.get(2).x, sortedcorners.get(3).x);
        int bottom = Math.max(sortedcorners.get(4).y, sortedcorners.get(3).y);
        int margin = (bottom - top) / BOUNDING_BOX_MARGIN;
        boundingbox = new Rect(left, top - margin, right, bottom + margin);
    }



    private void identifypointsbychoice(double maxdist) {
        sortedanswerpoints.clear();
        sortedpositionpoints.clear();
        for (Point p : answerpoints) {
            double dist = Double.MAX_VALUE;
            int choice = -1;
            for (Line l : choiceslines
                    ) {

                double currdist = l.getdistancefrompoint(p);
                if (currdist < dist) {
                    dist = currdist;
                    choice = l.answernumber;
                }
            }
            if (choice != 0) {
                if (dist < maxdist) {
                    if (sortedanswerpoints.size() > 0) {
                        if (sortedanswerpoints.get(sortedanswerpoints.size() - 1).x != p.x || sortedanswerpoints.get(sortedanswerpoints.size() - 1).y != p.y) {
                            sortedanswerpoints.add(new AnswerPoint(p.x, p.y, choice));
                        }
                    } else {
                        sortedanswerpoints.add(new AnswerPoint(p.x, p.y, choice));
                    }
                } else {
                    sortedanswerpoints.add(new AnswerPoint(p.x, p.y, -1));
                    abortflag = true;
                    if(issue_description=="")
                    {
                        issue_description="no sorted answer points";
                    }
                }
            } else {
                if (dist < maxdist) {
                    AnswerPoint temppoint = new AnswerPoint(p.x, p.y, 0);
                    if (sortedpositionpoints.size() > 0) {
                        if (sortedpositionpoints.get(sortedpositionpoints.size() - 1).x != p.x || sortedpositionpoints.get(sortedpositionpoints.size() - 1).y != p.y) {
                            sortedpositionpoints.add(temppoint);
                        }
                    } else {
                        sortedpositionpoints.add(temppoint);
                    }
                } else {
                    abortflag = true;
                    if(issue_description=="")
                    {
                        issue_description="distance too big";
                    }
                }
            }
        }
    }

    private void create_abcd_lines() {
        //can assume not veritcal
        choiceslines.clear();
        leftoptions = new DividedSegment(sortedcorners.get(4), sortedcorners.get(0), numberofchoices+1);
        DividedSegment rightoptions = new DividedSegment(sortedcorners.get(3), sortedcorners.get(2), numberofchoices+1);
        for (int i = 0; i < numberofchoices; i++) {
            Line choiceline = new Line(leftoptions.divisionpoints.get(i), rightoptions.divisionpoints.get(i), i + 1);
            choiceslines.add(choiceline);
        }
        choiceslines.add(new Line(sortedcorners.get(0), sortedcorners.get(2), 0));
        choiceslines.add(new Line(sortedcorners.get(3), sortedcorners.get(4), -1));

    }



    private void removeduplicates() {
        double margin = 5;
        ArrayList<Point> pointstoremove = new ArrayList<>();
        for (Point p1 : blacksquares) {
            for (Point p2 : blacksquares) {
                if (blacksquares.indexOf(p1) != blacksquares.indexOf(p2) && !pointstoremove.contains(p1)) {
                    if (distance(p1, p2) < margin) {
                        pointstoremove.add(p2);
                    }
                }
            }
        }
        for (Point premoving : pointstoremove) {
            blacksquares.remove(premoving);

        }
    }

    private boolean verifyorientation(Point distances) {

        double diff;
        double reas;

        diff = distance(sortedcorners.get(0),sortedcorners.get(2))-distance(sortedcorners.get(3),sortedcorners.get(4));
        reas = Math.abs(sortedcorners.get(0).x - sortedcorners.get(2).x)/((double) MAX_DIFFERENCE_PERSPECTIVE);
        if (Math.abs(diff)<reas) {
            return true;
        } else {
            if(issue_description=="")
            {
                if(diff>0) {
                    issue_description = "toofar_right";
                    left_right_tilt='R';
                }else
                {
                    issue_description = "toofar_left";
                    left_right_tilt='L';
                }
            }
            return false;
        }

    }

    private void findanswerpoints() {

        answerpoints.addAll(blacksquares);
        answerpoints.removeAll(sortedcorners);
    }

    private void removepointsoutsideboundingbox() {
        ArrayList<Point> pointstoremove = new ArrayList<>();

        for (Point blacksquare : blacksquares) {
            if (!boundingbox.contains(blacksquare.x, blacksquare.y)) {
                pointstoremove.add(blacksquare);

            }
        }
        blacksquares.removeAll(pointstoremove);
    }


    private Rect findboundingbox() {
        ArrayList<Point> copyofblacksquares = new ArrayList<>(); //all detected
        copyofblacksquares=(ArrayList<Point>)blacksquares.clone();
        int top = Integer.MAX_VALUE;
        int  bottom = 0;
        for (int rep = 0; rep < AMMOUNT_OF_EXTERNAL_POINTS_TO_REMOVE; rep++) {
            top = Integer.MAX_VALUE;
            int topindex = -1;
            for (Point blacksquare : copyofblacksquares) {
                if (blacksquare.y < top) {
                    top = blacksquare.y;
                    topindex = copyofblacksquares.indexOf(blacksquare);
                }
            }
            if(top==-1) {
                abortflag = true;
            }else
            {
                copyofblacksquares.remove(topindex);
            }

        }
        for (int rep = 0; rep < AMMOUNT_OF_EXTERNAL_POINTS_TO_REMOVE; rep++) {
            bottom = -1;
            int bottomindex = -1;
            for (Point blacksquare : copyofblacksquares) {
                if (blacksquare.y > bottom) {
                    bottom=blacksquare.y;
                    bottomindex = copyofblacksquares.indexOf(blacksquare);
                }
            }
            if(bottom==-1) {
                abortflag = true;
            }else
            {
                copyofblacksquares.remove(bottomindex);
            }

        }
        if(copyofblacksquares.size()<6)
        {
            abortflag=true;
        }
        if(!abortflag) {


            int middlex = 0;
            int middley = 0;
            int deviationx = 0;
            int deviationy = 0;
            for (Point blacksquare : copyofblacksquares) {
                middlex += blacksquare.x;
                middley += blacksquare.y;
            }
            middlex = middlex / copyofblacksquares.size();
            middley = middley / copyofblacksquares.size();
            for (Point blacksquare : copyofblacksquares) {
                deviationx += Math.abs(blacksquare.x - middlex);
                deviationy += Math.abs(blacksquare.y - middley);
            }
            deviationx = (int) (deviationx / copyofblacksquares.size() * BOUNDING_BOX_X_DEVIATION);
            float multiplier = numberofchoices / ((float) 6);
            deviationy = (int) (deviationy / copyofblacksquares.size() * BOUNDING_BOX_Y_DEVIATION * multiplier);
            int left = middlex - deviationx;
            if (left < 0) {
                left = 0;
            }
            int right = middlex + deviationx;
            if (right >= cornersdetectionbitmap.getWidth()) {
                right = cornersdetectionbitmap.getWidth() - 1;
            }
            top = top - deviationy / 3;
            if (top < 0) {
                top = 0;
            }
            bottom = bottom + deviationy / 3;
            if (bottom >= cornersdetectionbitmap.getHeight()) {
                bottom = cornersdetectionbitmap.getHeight() - 1;
            }
            middleofboundingbox = new Point(middlex, middley);

            return new Rect(left, top, right, bottom);
        }else
        {
            return new Rect(0, 0, 0, 0);
        }

    }

    private Point sortcorners() {
        sortedcorners.clear();
        int lefttopindex = 0;
        int righttopindex = 0;
        int leftbottomindex = 0;
        int rightbottomindex = 0;
        double lefttoprecordeddist = 65530;
        double righttoprecordeddist = 65530;
        double leftbottomrecordeddist = 65530;
        double rightbottomrecordeddist = 65530;

        ArrayList<Integer> occupiedindexes = new ArrayList<>();

        for (Point blacksquare : blacksquares) {
            if (distance(blacksquare, new Point(0, 0)) < lefttoprecordeddist) {
                lefttoprecordeddist = distance(blacksquare, new Point(0, 0));
                lefttopindex = blacksquares.indexOf(blacksquare);

            }
            if (distance(blacksquare, new Point(cornersdetectionbitmap.getWidth(), 0)) < righttoprecordeddist) {
                righttoprecordeddist = distance(blacksquare, new Point(cornersdetectionbitmap.getWidth(), 0));
                righttopindex = blacksquares.indexOf(blacksquare);
            }
            if (distance(blacksquare, new Point(0, cornersdetectionbitmap.getHeight())) < leftbottomrecordeddist) {
                leftbottomrecordeddist = distance(blacksquare, new Point(0, cornersdetectionbitmap.getHeight()));
                leftbottomindex = blacksquares.indexOf(blacksquare);
            }
            if (distance(blacksquare, new Point(cornersdetectionbitmap.getWidth(), cornersdetectionbitmap.getHeight())) < rightbottomrecordeddist) {
                rightbottomrecordeddist = distance(blacksquare, new Point(cornersdetectionbitmap.getWidth(), cornersdetectionbitmap.getHeight()));
                rightbottomindex = blacksquares.indexOf(blacksquare);
            }
        }
        occupiedindexes.add(lefttopindex);
        occupiedindexes.add(righttopindex);
        occupiedindexes.add(rightbottomindex);
        occupiedindexes.add(leftbottomindex);
        Point leftmiddle = new Point((blacksquares.get(leftbottomindex).x + blacksquares.get(lefttopindex).x) / 2, (blacksquares.get(leftbottomindex).y + blacksquares.get(lefttopindex).y) / 2);

        int middleleftindex = 0;
        int middleleftrecordeddist = Integer.MAX_VALUE;
        for (Point blacksquare : blacksquares) {
            if (distance(leftmiddle, blacksquare) < middleleftrecordeddist) {
                if (!occupiedindexes.contains(blacksquares.indexOf(blacksquare))) {
                    middleleftrecordeddist = (int) distance(leftmiddle, blacksquare);
                    middleleftindex = blacksquares.indexOf(blacksquare);
                }
            }

        }
        Point placeholder = new Point(0,0);
        sortedcorners.add(blacksquares.get(lefttopindex));
        sortedcorners.add(placeholder);
        sortedcorners.add(blacksquares.get(righttopindex));
        sortedcorners.add(blacksquares.get(rightbottomindex));
        sortedcorners.add(blacksquares.get(leftbottomindex));
        sortedcorners.add(blacksquares.get(middleleftindex));
        return new Point(middleleftrecordeddist, 0);

    }

    private double distance(Point p1, Point p2) {
        return Math.hypot(p1.x - p2.x, p1.y - p2.y);
    }

    private void createcornerdetectionbitmap() {
        int cornersbrushsize = 4;//for fullsize bitmap
        int blacktreshold = 120; //0-255, bigger means more dark areas qualify//100
        int difference=30;
        for (int x = cornersbrushsize * 3; x < framesize.width / 2 - cornersbrushsize * 3; x += 1) {
            for (int y = cornersbrushsize * 3; y < framesize.height / 2 - cornersbrushsize * 3; y += 1) {
                if (Color.red(maingrayscalebitmap.getPixel(x * 2, y * 2)) < blacktreshold) {
                    {
                        int blackcolor=Color.red(maingrayscalebitmap.getPixel(x * 2, y * 2));
                        int whitetreshold=blackcolor+difference;
                        if (Color.red(maingrayscalebitmap.getPixel(x * 2 + cornersbrushsize, y * 2)) > whitetreshold) {
                            //when it's a vertical bright|dark edge, marked by red;
                            cornersdetectionbitmap.setPixel(x + cornersbrushsize / 2, y, Color.RED);

                        }
                        if (Color.red(maingrayscalebitmap.getPixel(x * 2 - cornersbrushsize, y * 2)) >  whitetreshold) {
                            //when it's a vertical dark|bright edge, marked by blue;
                            cornersdetectionbitmap.setPixel(x - cornersbrushsize / 2, y, Color.BLUE);


                        }
                        if (Color.red(maingrayscalebitmap.getPixel(x * 2, y * 2 + cornersbrushsize)) > whitetreshold) {
                            //when it's a horizontal bright/dark edge, marked by green;
                            cornersdetectionbitmap.setPixel(x, y + cornersbrushsize / 2, Color.GREEN);
                            ;
                        }
                        if (Color.red(maingrayscalebitmap.getPixel(x * 2, y * 2 - cornersbrushsize)) > whitetreshold) {
                            //when it's a horizontal dark/bright edge, marked by green;
                            cornersdetectionbitmap.setPixel(x, y - cornersbrushsize / 2, Color.YELLOW);

                        }

                    }
                }
            }
        }
    }

    private void findblacksquarepoints() {
        blacksquares.clear();
        int blacktreshold = 120; //0-255, bigger means more dark areas qualify
        int boxsize = framesize.width/40;
        int edge = (boxsize+boxsize/2)+2;
        for (int x = edge; x < framesize.width / 2 - edge; x += 1) {
            for (int y = edge; y < framesize.height / 2 - edge; y += 1) {
                if (Color.red(maingrayscalebitmap.getPixel(x * 2, y * 2)) < blacktreshold) {
                    {
                        boolean cornerdetected=false;
                        for(int cornerdetectiondistancetemporary=1;cornerdetectiondistancetemporary<5;cornerdetectiondistancetemporary++)
                        {
                            if(!cornerdetected)
                            {
                                cornerdetected= (cornersdetectionbitmap.getPixel(x +cornerdetectiondistancetemporary, y) == Color.RED && cornersdetectionbitmap.getPixel(x, y - cornerdetectiondistancetemporary) == Color.YELLOW);
                            }
                        }
                        if (cornerdetected) {
                            int downheight = -1;
                            int leftheight = -1;
                            for (int i = 0; i < boxsize; i++) {

                                if (cornersdetectionbitmap.getPixel(x - 1, y + i) == Color.GREEN) {
                                    downheight = i;
                                    break;
                                }
                            }

                            for (int i = 0; i < boxsize; i++) {

                                if (cornersdetectionbitmap.getPixel(x - i, y + 1) == Color.BLUE) {
                                    leftheight = i;
                                    break;
                                }
                            }
                            if (leftheight != -1 && downheight != -1 && (leftheight+downheight)>4 && Math.abs((leftheight-downheight))<MAX_ACCEPTABLE_DIFFERENCE_BETWEEN_BLACKSQUARE_WIDTH_AND_HEIGHT) {
                                //calculate middle
                                Point corner = new Point((x + (x - leftheight)) / 2, (y + (y + downheight)) / 2);
                                int margin=boxsize/4;
                                if(margin>(leftheight+downheight)/2)
                                {
                                    margin=(leftheight+downheight)/2;
                                }
                                int difference=MIN_COLOR_DIFFERENCE_BETWEEN_SQUARE_AND_SIDES;
                                int frame=3;
                                int avreagecolorofblacksquare = averagecolor(new Point(x - leftheight, y), new Point(x, y + downheight));
                                int avreagecolorleft = averagecolor(new Point(x - leftheight-margin, y), new Point(x- leftheight, y + downheight));
                                int avreagecolortop = averagecolor(new Point(x - leftheight, y-margin), new Point(x, y));
                                int avreagecolorbottom = averagecolor(new Point(x - leftheight, y+downheight), new Point(x, y + downheight+margin));
                                int avreagecolorright = averagecolor(new Point(x, y), new Point(x+margin, y + downheight));
                                if(avreagecolorleft>avreagecolorofblacksquare+difference&&avreagecolortop>avreagecolorofblacksquare+difference&&avreagecolorbottom>avreagecolorofblacksquare+difference&&avreagecolorright>avreagecolorofblacksquare+difference)
                                {

                                    blacksquares.add(corner);



                                }
                            }


                        }
                    }
                }
            }
        }
    }

    public int averagecolor(Point lefttop, Point rightbottom) {
        long colorsum = 0;
        long colorcount = 0;
        for (int i = lefttop.x; i < rightbottom.x; i++) {
            for (int j = lefttop.y; j < rightbottom.y; j++) {
                colorcount++;
                colorsum += Color.red(maingrayscalebitmap.getPixel(i * 2, j * 2));
            }
        }
        if (colorcount > 0) {
            return (int) (colorsum / colorcount);
        } else {
            return 256;
        }
    }


    private void createbitmapsfromdata() {
        int pixelCount = framesize.width * framesize.height;
        int[] avreagearray = new int[pixelCount];
        long luminanceavreage = 0;
        for (int i = 0; i < pixelCount; ++i) {
            int luminance = data[i] & 0xFF;
            luminanceavreage += luminance;
        }
        luminanceavreage = luminanceavreage / pixelCount;

        for (int i = 0; i < pixelCount; ++i) {
            int diffluminance = 127 + (int) ((data[i] & 0xFF) - luminanceavreage);
            if (diffluminance > 255) {
                diffluminance = 255;
            }
            if (diffluminance < 0) {
                diffluminance = 0;
            }
            avreagearray[i] = Color.argb(0xFF, diffluminance, diffluminance, diffluminance);
        }
        Bitmap immutablebitmapforcopy = Bitmap.createBitmap(avreagearray, framesize.width, framesize.height, Bitmap.Config.ARGB_4444);
        cornersdetectionbitmap = Bitmap.createBitmap(framesize.width / 2, framesize.height / 2, Bitmap.Config.ARGB_4444);
        cornersdetectionbitmap.eraseColor(Color.WHITE);
        maingrayscalebitmap = immutablebitmapforcopy.copy(Bitmap.Config.ARGB_4444, true);
    }

    public void terminatethread() {
        terminatethread = true;
    }


     /* private void markboundingbox(int color) {
        Marking.markboxwithcolor(maingrayscalebitmap, new Point(middleofboundingbox.x * 2, middleofboundingbox.y * 2), color, boundingbox.width(), boundingbox.height());
    }
      private void markline(Line l1, int c1) {
        for (int i = boundingbox.left; i < boundingbox.right; i = i + 2) {
            int y = (int) l1.getYfromX(i);
            if (y > boundingbox.top && y < boundingbox.bottom) {
                maingrayscalebitmap.setPixel(i * 2, y * 2, c1);
                maingrayscalebitmap.setPixel(i * 2, y * 2 + 1, c1);
                maingrayscalebitmap.setPixel(i * 2, y * 2 - 1, c1);
            }
        }
    }

    private void markpoints() {
        if (blacksquares.size() > 0) {
            for (Point p : blacksquares
                    ) {
                Marking.markwithcolor(maingrayscalebitmap, new Point(p.x * 2, p.y * 2), Color.GREEN, 3);
            }

        }
        //  markcorners();
    }

    private void markpospoints() {
        if (sortedpositionpoints.size() > 0) {
            for (AnswerPoint p : sortedpositionpoints
                    ) {
                Marking.markwithcolor(maingrayscalebitmap, p.getscaledPoint(2), Color.WHITE, 2);
            }

        }
        //  markcorners();
    }

    private void marksortedpoints() {
        if (sortedanswerpoints.size() > 0) {
            for (AnswerPoint p : sortedanswerpoints
                    ) {
                int[] color = {Color.WHITE, Color.YELLOW, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW};
                Marking.markwithcolor(maingrayscalebitmap, new Point(p.x * 2, p.y * 2), color[p.choice + 1], 3);
            }

        }
        //  markcorners();
    }

    private void markcorners() {
        int dif = 0;
        for (Point sortedcorner : sortedcorners) {
            //  Color.argb(255, 255 - dif, dif, 0)
            Marking.markwithcolor(maingrayscalebitmap, new Point(sortedcorner.x * 2, sortedcorner.y * 2), Color.RED, 5);
            dif += 250 / 6;
        }
    }

    */
}
