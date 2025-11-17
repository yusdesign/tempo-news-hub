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
        
        Toast.makeText(this, "🚀 GUARDIAN CHARGE!", Toast.LENGTH_LONG).show();
        
        try {
            WebView webView = new WebView(this);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
                
                @Override
                public void onPageFinished(WebView view, String url) {
                    if (url.startsWith("file://")) {
                        Toast.makeText(MainActivity.this, "📰 Guardian Edition Loaded!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "🌐 Loading: " + url, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            
            webView.loadUrl("file:///android_asset/news_app.html");
            setContentView(webView);
            
        } catch (Exception e) {
            Toast.makeText(this, "❌ Crash: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
