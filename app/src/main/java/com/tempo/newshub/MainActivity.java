package com.tempo.newshub;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Toast.makeText(this, "🚀 APP STARTED!", Toast.LENGTH_LONG).show();
        
        try {
            WebView webView = new WebView(this);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            webView.setWebViewClient(new WebViewClient());
            
            // SIMPLE HTML THAT CAN'T FAIL
            String simpleHTML = "<html><body style='background: #4CAF50; color: white; padding: 50px;'><h1>✅ IT WORKS!</h1><p>WebView is working!</p></body></html>";
            webView.loadData(simpleHTML, "text/html", "UTF-8");
            
            setContentView(webView);
            Toast.makeText(this, "📰 HTML LOADED!", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "❌ Crash: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
