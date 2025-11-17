package com.tempo.newshub;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    
    private WebView webView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create WebView programmatically - NO XML
        webView = new WebView(this);
        
        // Enable JavaScript
        webView.getSettings().setJavaScriptEnabled(true);
        
        // Keep navigation in the app
        webView.setWebViewClient(new WebViewClient());
        
        // Load our local HTML file
        webView.loadUrl("file:///android_asset/news_app.html");
        
        setContentView(webView);
    }
    
    @Override
    public void onBackPressed() {
        // Handle back button in WebView
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
