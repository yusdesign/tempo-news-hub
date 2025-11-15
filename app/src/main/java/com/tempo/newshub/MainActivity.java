package com.tempo.newshub;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        TextView textView = new TextView(this);
        textView.setText("🎉 TEMPO NEWS HUB\n\n" +
                        "✅ Pure Java UI\n" +
                        "🚀 No XML Dependencies\n" +
                        "📱 Running Successfully\n\n" +
                        "Version: 1.0.2");
        textView.setTextSize(18);
        textView.setTextColor(Color.DKGRAY);
        textView.setBackgroundColor(Color.LTGRAY);
        textView.setPadding(50, 50, 50, 50);
        textView.setLineSpacing(1.5f, 1.5f);
        
        setContentView(textView);
    }
}
