package com.tempo.newshub;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    
    private String getDaytimeGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        
        if (hour >= 5 && hour < 12) {
            return "Good morning";
        } else if (hour >= 12 && hour < 18) {
            return "Good afternoon"; 
        } else {
            return "Good evening";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Toast.makeText(this, "🚀 GUARDIAN CHARGE!", Toast.LENGTH_LONG).show();
        
        String greeting = getDaytimeGreeting();
        Toast.makeText(this, "🌅 " + greeting + "!", Toast.LENGTH_SHORT).show();

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
                    // Inject greeting into the WebView
                    String js = "javascript:if(document.getElementById('greeting')) { " +
                                "document.getElementById('greeting').innerText = '" + greeting + "'; " +
                                "}";
                    view.evaluateJavascript(js, null);
                    
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
