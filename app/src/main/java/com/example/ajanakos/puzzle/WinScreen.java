package com.example.ajanakos.puzzle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by ajanakos on 3/13/16.
 */
public class WinScreen extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.winner);
    }

    public void goBack(View v) {
        // Create new intent for puzzle activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
