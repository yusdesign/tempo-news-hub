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
            webView.loadUrl("file:///android_asset/news_app.html");
            
            setContentView(webView);
            Toast.makeText(this, "📰 WebView Loaded!", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "❌ WebView Failed!", Toast.LENGTH_LONG).show();
        }
    }
}
