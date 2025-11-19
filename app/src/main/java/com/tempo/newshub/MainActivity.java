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
        
        // FIX: Allow WebView to make network requests
        webView.getSettings().setAllowFileAccess(true);
        
        setupWebView();
        loadNewsData(greeting);
        
        webView.loadUrl("file:///android_asset/news_app.html");
        setContentView(webView);
    }
    
    private void setupWebView() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // Inject greeting immediately
                String js = "javascript:{" +
                           "document.getElementById('greeting').innerText = '" + getDaytimeGreeting() + "';" +
                           "}";
                view.evaluateJavascript(js, null);
            }
        });
    }
    
    private void loadNewsData(String greeting) {
        new Thread(() -> {
            try {
                NewsService newsService = new NewsService();
                List<NewsArticle> articles = newsService.fetchNews();
                
                StringBuilder articlesJson = new StringBuilder("[");
                for (int i = 0; i < articles.size(); i++) {
                    NewsArticle article = articles.get(i);
                    articlesJson.append("{")
                        .append("\"title\":\"").append(escapeJson(article.getTitle())).append("\",")
                        .append("\"description\":\"").append(escapeJson(article.getDescription())).append("\",")
                        .append("\"date\":\"").append(escapeJson(article.getDate())).append("\",")
                        .append("\"source\":\"").append(escapeJson(article.getSource())).append("\",")
                        .append("\"url\":\"").append(escapeJson(article.getUrl())).append("\"")
                        .append("}");
                    if (i < articles.size() - 1) articlesJson.append(",");
                }
                articlesJson.append("]");
                
                final String finalJson = articlesJson.toString();
                
                runOnUiThread(() -> {
                    String js = "javascript:{" +
                                "window.newsArticles = " + finalJson + ";" +
                                "if(window.renderArticles) window.renderArticles(" + finalJson + ");" +
                                "}";
                    webView.evaluateJavascript(js, null);
                    Toast.makeText(MainActivity.this, "✅ " + articles.size() + " articles loaded", Toast.LENGTH_SHORT).show();
                });
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t")
                  .replace("'", "\\'");
    }
}
