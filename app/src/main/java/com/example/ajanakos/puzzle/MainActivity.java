package com.example.ajanakos.puzzle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

    private int PICK_IMAGE_REQUEST = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create database to store sovles
        SQLiteDatabase db = openOrCreateDatabase("puz", MODE_PRIVATE, null);
        db.close();

        // Set error text to invisible
        TextView text = (TextView) findViewById(R.id.errText);
        text.setVisibility(View.INVISIBLE);

        // Reset image and difficulty
        globals.image = null;
        globals.difficulty = null;

        // Get IDs of difficulty buttons
        Button b1 = (Button) findViewById(R.id.three);
        Button b2 = (Button) findViewById(R.id.four);
        Button b3 = (Button) findViewById(R.id.five);
        b1.setOnClickListener(this);
        b2.setOnClickListener(this);
        b3.setOnClickListener(this);


        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    // Send us to see our solves
    public void seeScores(View view) {
        // Create new intent for puzzle activity
        Intent intent = new Intent(this, yourScores.class);
        startActivity(intent);

    }

    // Convert image into ArrayListBitmap and start puzzle activity
    public void startGame(View v) {
        // Verify difficulty and image have been chosen
        if (globals.difficulty == null || globals.image == null) {
            TextView text = (TextView) findViewById(R.id.errText);
            text.setVisibility(View.VISIBLE);
        } else {

            // Set image and dimesions for puzzle
            splitImage(globals.image, globals.dimension);

            // Create new intent for puzzle activity
            Intent intent = new Intent(this, MakePuzzle.class);

            // Pass rows for later usage
            intent.putExtra("rows", globals.rows);

            // Start puzzle activity
            startActivity(intent);
        }
    }

    // Lets user choose how many rows/cols there puzzle will have
    @Override
    public void onClick(View view) {

        // Set flag that a difficulty has been chosen
        globals.difficulty = view.getId();

        // Set the dimesions the of the puzzle to be solved
        switch (view.getId()) {
            case R.id.three:
                globals.dimension = 4;
                break;
            case R.id.four:
                globals.dimension = 9;
                break;
            case R.id.five:
                globals.dimension = 16;
        }
    }

    // Transform image into into ArrayList<Bitmap>
    private void splitImage(ImageView image, int chunkNumbers) {

        // Number of rows and columns of grid
        int rows, cols;

        // Height and width of chunks
        int chunkHeight, chunkWidth;

        // Holds bitmaps objects
        ArrayList<Bitmap> chunkedImages = new ArrayList<Bitmap>(chunkNumbers);

        //Getting the scaled bitmap of the source image
        BitmapDrawable drawable = (BitmapDrawable) image.getDrawable();
        Bitmap mbitmap = drawable.getBitmap();

        // Make Bitmap mutable
        Bitmap bitmap = mbitmap.copy(Bitmap.Config.ARGB_8888, true);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
        rows = cols = (int) Math.sqrt(chunkNumbers);
        chunkHeight = bitmap.getHeight() / rows;
        chunkWidth = bitmap.getWidth() / cols;

        //x and y coords of pixels
        int yCoord = 0;
        for (int x = 0; x < rows; x++) {
            int xCoord = 0;
            for (int y = 0; y < cols; y++) {
                chunkedImages.add(Bitmap.createBitmap(scaledBitmap, xCoord, yCoord, chunkWidth, chunkHeight));
                xCoord += chunkWidth;
            }
            yCoord += chunkHeight;
        }

        // Turn last bit obj white and store its name
        String bitName = "";
        chunkedImages.get(chunkedImages.size()-1).eraseColor(1);

        // Set global chunkedImages value
        globals.chunkedImages = chunkedImages;

        // Create array to check win state
        ArrayList<Integer> winList = new ArrayList<Integer>();
        for (int i = 0; i < chunkedImages.size(); i++) {
            winList.add(i);
        }

        // Copy array state so we can check if user wins
        globals.winState = winList;

        // Create array for current state
        ArrayList<Integer> curList  = new ArrayList<Integer>();
        for (int i = 0; i < chunkedImages.size(); i++) {
            curList.add(i);
        }

        // Shuffle list and verify it is not equal to win state
        while (true) {
            Collections.shuffle(curList);
            if (!curList.equals(winList))
                break;
        }

        // Set curList
        globals.curState = curList;

        // Shuffle ArrayList<Bitmap> according to curlist
        ArrayList<Bitmap> puzzle = new ArrayList<Bitmap>();
        for (int i = 0; i < chunkedImages.size(); i++) {
            puzzle.add(chunkedImages.get(curList.get(i)));
        }

        // Get new position of bitName
        int pos = -1;
        for (int i = 0; i < curList.size(); i++ )
            if (curList.get(i) == curList.size()-1)
                pos = curList.get(i);


        // Write grid dimension to new activity
        globals.position = pos;
        globals.rows = rows;
        globals.cols = cols;
    }

    public void getImage(View v) {

        Intent intent = new Intent();

        // Show only image, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            globals.uri = uri;

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 170, 170, true);
                ImageView image = new ImageView(this);
                image.setImageBitmap(scaledBitmap);
                globals.image = image;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

