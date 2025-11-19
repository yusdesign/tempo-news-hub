package com.tempo.newshub;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    
    private WebView webView;
    private boolean articlesLoaded = false;
    
    private String getDaytimeGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 12) return "Good morning";
        else if (hour >= 12 && hour < 18) return "Good afternoon"; 
        else return "Good evening";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String greeting = getDaytimeGreeting();
        Toast.makeText(this, "🚀 " + greeting, Toast.LENGTH_SHORT).show();

        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        
        setupWebView();
        webView.loadUrl("file:///android_asset/news_app.html");
        setContentView(webView);
    }
    
    private void setupWebView() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // Inject greeting immediately
                String greeting = getDaytimeGreeting();
                String jsGreeting = "javascript:document.getElementById('greeting').innerText = '" + greeting + "';";
                view.evaluateJavascript(jsGreeting, null);
                
                // Load news data after page is ready
                if (!articlesLoaded) {
                    loadNewsData();
                    articlesLoaded = true;
                }
            }
        });
    }
    
    private void loadNewsData() {
        new Thread(() -> {
            try {
                NewsService newsService = new NewsService();
                List<NewsArticle> articles = newsService.fetchNews();
                
                final int articleCount = articles.size();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "✅ " + articleCount + " articles loaded", Toast.LENGTH_SHORT).show();
                });
                
                // Build SIMPLE JavaScript array
                StringBuilder js = new StringBuilder("javascript:window.articlesData = [");
                for (int i = 0; i < articles.size(); i++) {
                    NewsArticle article = articles.get(i);
                    js.append("{")
                      .append("title:'").append(cleanForJS(article.getTitle())).append("',")
                      .append("description:'").append(cleanForJS(article.getDescription())).append("',")
                      .append("source:'").append(cleanForJS(article.getSource())).append("',")
                      .append("date:'").append(cleanForJS(article.getDate())).append("',")
                      .append("url:'").append(cleanForJS(article.getUrl())).append("'")
                      .append("}");
                    if (i < articles.size() - 1) js.append(",");
                }
                js.append("];");
                js.append("if(window.renderArticles) window.renderArticles(window.articlesData);");
                
                final String finalJS = js.toString();
                
                // Wait a bit for WebView to be fully ready, then inject
                Thread.sleep(1000);
                runOnUiThread(() -> {
                    webView.evaluateJavascript(finalJS, value -> {
                        // Callback to check if JS executed
                        Toast.makeText(MainActivity.this, "📡 Data injected to WebView", Toast.LENGTH_SHORT).show();
                    });
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "❌ Load failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    private String cleanForJS(String text) {
        if (text == null) return "";
        return text.replace("'", "\\'")
                  .replace("\"", "\\\"")
                  .replace("\n", " ")
                  .replace("\r", " ")
                  .replace("\t", " ");
    }
}
