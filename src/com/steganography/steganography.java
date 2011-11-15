package com.steganography;

import android.app.Activity;

//comment added for testing - aj
import android.os.Bundle;
import android.widget.TextView;
public class steganography extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        System.out.println("test");
    }
}