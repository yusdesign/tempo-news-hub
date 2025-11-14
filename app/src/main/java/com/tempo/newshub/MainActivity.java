package com.tempo.newshub;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.time.LocalTime;

public class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        setupTimeBasedUI();
    }
    
    private void setupTimeBasedUI() {
        TextView greetingText = findViewById(R.id.greeting_text);
        TextView timeIcon = findViewById(R.id.time_icon);
        
        // Simple time-based logic
        int hour = LocalTime.now().getHour();
        
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
    }
}
