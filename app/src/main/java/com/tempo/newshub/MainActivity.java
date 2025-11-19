package com.tempo.newshub;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        WebView webView = new WebView(this);
        webView.loadData(
            "<html><body><h1>✅ Tempo v2.6.49</h1><p>Basic WebView Working</p></body></html>",
            "text/html",
            "UTF-8"
        );
        setContentView(webView);
    }
}
