package com.aleqsio.testscanning.marking_utils;

import android.graphics.Bitmap;
import android.graphics.Point;

/**
 * Created by Alek on 2016-05-19.
 */
public class Marking {
    public static void markwithcolor(Bitmap bitmap, Point point, int color, int radius) {
        for (int i = -radius; i < radius; i++) {
            for (int j = -radius; j < radius; j++) {
                bitmap.setPixel(point.x + i, point.y + j, color);
            }
        }
    }

    public static Point resizepoint(Point p)
    {
        return new Point(p.x*2,p.y*2);
    }

    public static void markboxwithcolor(Bitmap bitmap, Point point, int color, int radiusx, int radiusy) {
        for (int i = -radiusx; i < radiusx; i++) {

            if (point.x + i >= 0 && point.x + i < bitmap.getWidth()) {
                if (point.y - radiusy >= 0 && point.y + radiusy < bitmap.getHeight()) {
                    bitmap.setPixel(point.x + i, point.y - radiusy, color);
                    bitmap.setPixel(point.x + i, point.y + radiusy, color);
                }
            }

        }
    }
}
