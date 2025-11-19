package com.tempo.newshub;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create layout programmatically - no XML resources needed
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(android.view.Gravity.CENTER);
        layout.setBackgroundColor(Color.WHITE);
        
        TextView textView = new TextView(this);
        textView.setText("✅ Tempo v2.7.1");
        textView.setTextSize(24);
        textView.setTextColor(Color.BLACK);
        
        TextView subText = new TextView(this);
        subText.setText("Working without resources");
        subText.setTextSize(16);
        subText.setTextColor(Color.GRAY);
        
        layout.addView(textView);
        layout.addView(subText);
        
        setContentView(layout);
    }
}
