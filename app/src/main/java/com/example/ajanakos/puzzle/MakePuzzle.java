package com.example.ajanakos.puzzle;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

// Displays the grid view to the user and lets them interact with the tiles
public class MakePuzzle extends Activity {


    public void goBack(View v) {
        // Create new intent for puzzle activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // Displays Bitmap inside a grid view
    public void display (ArrayList<Bitmap> chunkedImages) {

        //Getting the grid view and setting an adapter to it
        GridView grid = (GridView) findViewById(R.id.gridview);
        grid.setAdapter(new Adapter(this, chunkedImages));
        grid.setNumColumns((int) Math.sqrt(chunkedImages.size()));
    }

    // Stores initial grid values and displays board
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.grid);

        // Set move count to 0
        globals.moveCount = 0;

        final TextView time = (TextView) findViewById( R.id.textView );

        // Create a timer for puzzle
        new CountDownTimer(3600000, 1000) {

            public void onTick(long millisUntilFinished) {
                time.setText(""+(3600 - (millisUntilFinished / 1000)));
            }

            // Player could not solve the puzzle in 1 hour
            // Return them to the main menu
            public void onFinish() {
                time.setText("you lose!");
                ViewGroup view = (ViewGroup)getWindow().getDecorView();
                goBack(view);
            }
        }.start();



        // Load Bitmap from globals
        ArrayList<Bitmap> chunkedImages = globals.chunkedImages;

        // Retreive and store image attributes in shared preferences
        SharedPreferences sharedPref = MakePuzzle.this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("rows", getIntent().getIntExtra("rows", -1));

        // Write black tile position and grid dimensions to sharedPref
        editor.apply();

        // Shuffle ArrayList<Bitmap> according to curlist
        ArrayList<Bitmap> puzzle = new ArrayList<Bitmap>();
        for (int i = 0; i < chunkedImages.size(); i++) {
            puzzle.add(chunkedImages.get(globals.curState.get(i)));
        }

        // Display initial grid view
        display(puzzle);
    }


    // Everytime a directional button is pressed we swap the black tile with the direction pressed
    public void onClick(View v) {

        // Increment move vount
        ++globals.moveCount;

        // Get direction
        String dir = (String) v.getTag();

        // Create a sharePref object for storing and inserting values
        SharedPreferences sharedPref = MakePuzzle.this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        // Get row length value and current position of the black tile
        int rows = sharedPref.getInt("rows", -1);

        // Bring in global ArrayList<Bitmap>
        ArrayList<Bitmap> chunkedImages = globals.chunkedImages;
        ArrayList<Integer> winState = globals.winState;
        ArrayList<Integer> curState = globals. curState;

        // Set size of list and position of empty tile
        int size = curState.size()-1;
        int pos = curState.indexOf(size);

        // Swap position of the black tile with the tile directly above it
        if (dir.equals("up")) {
            if (pos-rows >= 0) {
                Collections.swap(curState, pos, pos - rows);
            }

        }
        // Swap position of the black tile and the tile to the right
        else if (dir.equals("right")) {
            if ((pos + 1) % rows != 0 && (pos + 1) <= size) {
                Collections.swap(curState, pos, pos + 1);
            }

        }
        // Swap position of the black tile and tile to the left
        else if (dir.equals("left")) {
            if ((pos % rows != 0) && (pos - 1) >= 0) {
                Collections.swap(curState, pos, pos - 1);
            }
        }
        // Swap position of the black tile and the tile below
        else {
            if (pos + rows <= size) {
                Collections.swap(curState, pos, pos + rows);
            }
        }

        // Write the position changed back to sharedPref
        editor.apply();

        // Update the global variable with new bitmap
        globals.chunkedImages = chunkedImages;

        // Update curState
        globals.curState = curState;

        // Player has completed the puzzle
        if (globals.winState.equals(globals.curState)) {

            // Grab timer TextView
            TextView time = (TextView) findViewById(R.id.textView);

            // Initialize local sqlite database
            SQLiteDatabase db = openOrCreateDatabase("puz", MODE_PRIVATE, null);

            // Create or open table
            db.execSQL("CREATE TABLE IF NOT EXISTS scores(uri VARCHAR, time VARCHAR, moves INTEGER, grid INTEGER);");

            // Insert solve details into sqlite database
            db.execSQL("INSERT INTO scores VALUES('" + globals.uri.toString() + "','" + time.getText() + "','" + globals.moveCount + "','" + globals.dimension + "');");
            db.close();

            // Bring user to win screen
            Intent intent = new Intent(this, WinScreen.class);
            startActivity(intent);

        }

        // Build puzzle based on curState list
        ArrayList<Bitmap> puzzle = new ArrayList<Bitmap>();
        for (int i = 0; i < chunkedImages.size(); i++) {
            puzzle.add(chunkedImages.get(curState.get(i)));
        }

        // Update curState
        globals.curState = curState;

        // Display new grid
        display(puzzle);
    }
}
