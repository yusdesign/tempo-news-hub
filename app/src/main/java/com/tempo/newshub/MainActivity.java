package com.tempo.newshub;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Bypass ALL resources - create UI programmatically
        TextView textView = new TextView(this);
        textView.setText("🎉 TEMPO NEWS HUB v1.0.1\n\n" +
                        "✅ Build Successful\n" +
                        "🚀 App is Running\n" + 
                        "📱 No XML, No Resources\n" +
                        "🎯 Pure Java UI");
        textView.setTextSize(20);
        textView.setTextColor(Color.BLACK);
        textView.setBackgroundColor(Color.WHITE);
        textView.setPadding(40, 40, 40, 40);
        textView.setLineSpacing(1.2f, 1.2f);
        
        setContentView(textView);
    }
}
