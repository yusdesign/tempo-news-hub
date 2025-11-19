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
        
        // SIMPLE STABLE VERSION
        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        
        // Basic setup - no complex features
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false; // Let WebView handle all URLs
            }
        });
        
        webView.loadUrl("file:///android_asset/news_app.html");
        setContentView(webView);
    }
    
    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
