package com.tempo.newshub;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.List;

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
        
        Toast.makeText(this, "🚀 TEMPO NEWS HUB!", Toast.LENGTH_LONG).show();
        
        String greeting = getDaytimeGreeting();
        Toast.makeText(this, "🌅 " + greeting + "!", Toast.LENGTH_SHORT).show();

        try {
            WebView webView = new WebView(this);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            
            // Load news immediately
            loadNewsData(webView, greeting);
            
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    // Open external links in WebView
                    if (url.startsWith("http")) {
                        view.loadUrl(url);
                        return true;
                    }
                    return false;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    if (url.startsWith("file://")) {
                        Toast.makeText(MainActivity.this, "📰 News Loaded Successfully!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            
            webView.loadUrl("file:///android_asset/news_app.html");
            setContentView(webView);
            
        } catch (Exception e) {
            Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void loadNewsData(WebView webView, String greeting) {
        new Thread(() -> {
            try {
                NewsService newsService = new NewsService();
                List<NewsArticle> articles = newsService.fetchNews();
                
                // Convert to JSON for JavaScript
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
                    
                    if (i < articles.size() - 1) {
                        articlesJson.append(",");
                    }
                }
                articlesJson.append("]");
                
                final String finalJson = articlesJson.toString();
                
                // Update WebView on UI thread
                runOnUiThread(() -> {
                    String js = "javascript:{" +
                                "document.getElementById('greeting').innerText = '" + greeting + "';" +
                                "window.newsArticles = " + finalJson + ";" +
                                "if(window.renderArticles) window.renderArticles(" + finalJson + ");" +
                                "}";
                    webView.evaluateJavascript(js, null);
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                // Even if Java fails, HTML has its own fallback
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
