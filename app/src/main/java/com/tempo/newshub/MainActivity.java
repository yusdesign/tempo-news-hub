package com.tempo.newshub;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.time.LocalTime;

public class MainActivity extends AppCompatActivity {
    
    private static final String TAG = "TempoNewsHub";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting app...");
        
        try {
            setContentView(R.layout.activity_main);
            Log.d(TAG, "onCreate: Layout inflated successfully");
            
            setupTimeBasedUI();
            Log.d(TAG, "onCreate: UI setup completed");
            
        } catch (Exception e) {
            Log.e(TAG, "onCreate: CRASH! " + e.getMessage(), e);
            // Show error screen instead of crashing
            showErrorScreen(e.getMessage());
        }
    }
    
    private void setupTimeBasedUI() {
        try {
            TextView timeIcon = findViewById(R.id.time_icon);
            TextView greetingText = findViewById(R.id.greeting_text);
            
            Log.d(TAG, "setupTimeBasedUI: Views found");
            
            int hour = LocalTime.now().getHour();
            Log.d(TAG, "setupTimeBasedUI: Current hour: " + hour);
            
            if (hour >= 5 && hour < 12) {
                greetingText.setText("Good Morning");
                timeIcon.setText("🌅");
            } else if (hour >= 12 && hour < 18) {
                greetingText.setText("Good Afternoon"); 
                timeIcon.setText("🏙️");
            } else {
                greetingText.setText("Good Evening");
                timeIcon.setText("🌃");
            }
            
            Log.d(TAG, "setupTimeBasedUI: UI updated successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "setupTimeBasedUI: Error: " + e.getMessage(), e);
            throw e; // Re-throw to be caught by onCreate
        }
    }
    
    private void showErrorScreen(String errorMessage) {
        // Create a simple error layout programmatically
        TextView errorView = new TextView(this);
        errorView.setText("🚨 App Error\n\n" + errorMessage + "\n\nPlease check logs");
        errorView.setTextSize(16);
        errorView.setPadding(50, 50, 50, 50);
        errorView.setBackgroundColor(0xFFFFDDDD);
        setContentView(errorView);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: App visible");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: App interactive");
    }
}
