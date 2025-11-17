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
            
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    Toast.makeText(MainActivity.this, "📰 HTML LOADED!", Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    Toast.makeText(MainActivity.this, "❌ WebView Error: " + description, Toast.LENGTH_LONG).show();
                }
            });
            
            webView.loadUrl("file:///android_asset/news_app.html");
            setContentView(webView);
            
        } catch (Exception e) {
            Toast.makeText(this, "❌ Crash: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
