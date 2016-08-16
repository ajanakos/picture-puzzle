package com.example.ajanakos.puzzle;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageView;
import java.util.ArrayList;

//public class globals extends Application {
//    public static ArrayList<Bitmap> chunkedImages;
//    public static ImageView image;
//    public static ArrayList<Bitmap> winState;
//    public static Integer difficulty;
//    public static Integer position;
//    public static Integer rows;
//    public static Integer cols;
//    public static Integer dimension;
//}


public class globals extends Application {

    public static ArrayList<Bitmap> chunkedImages;
    public static ImageView image;
    public static ArrayList<Integer> winState;
    public static ArrayList<Integer> curState;
    public static Integer difficulty;
    public static Integer position;
    public static Integer rows;
    public static Integer cols;
    public static Integer dimension;
    public static Uri uri;
    public static int moveCount;

    private static globals singleton;

    public static globals getInstance() {
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }
}