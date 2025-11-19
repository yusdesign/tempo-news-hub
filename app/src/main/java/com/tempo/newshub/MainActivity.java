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
        if (hour >= 5 && hour < 12) return "Good morning 🌅";
        else if (hour >= 12 && hour < 18) return "Good afternoon ☀️"; 
        else return "Good evening 🌙";
    }
    
    private String getGreetingEmoji() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 12) return "🌅";
        else if (hour >= 12 && hour < 18) return "☀️"; 
        else return "🌙";
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
                String emoji = getGreetingEmoji();
                String jsGreeting = "javascript:{" +
                                   "document.getElementById('greeting').innerText = '" + greeting + "';" +
                                   "document.getElementById('greetingEmoji').innerText = '" + emoji + "';" +
                                   "}";
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
                    Toast.makeText(MainActivity.this, "✅ " + articleCount + " Guardian articles", Toast.LENGTH_SHORT).show();
                });
                
                // Build JavaScript data
                StringBuilder js = new StringBuilder("javascript:window.tempoArticles = [");
                for (int i = 0; i < articles.size(); i++) {
                    NewsArticle article = articles.get(i);
                    js.append("{")
                      .append("id:'").append(i).append("',")
                      .append("title:'").append(cleanForJS(article.getTitle())).append("',")
                      .append("description:'").append(cleanForJS(article.getDescription())).append("',")
                      .append("source:'").append(cleanForJS(article.getSource())).append("',")
                      .append("date:'").append(cleanForJS(formatDate(article.getDate()))).append("',")
                      .append("url:'").append(cleanForJS(article.getUrl())).append("',")
                      .append("expanded:false")
                      .append("}");
                    if (i < articles.size() - 1) js.append(",");
                }
                js.append("];");
                js.append("if(window.tempoRenderArticles) window.tempoRenderArticles(window.tempoArticles);");
                
                final String finalJS = js.toString();
                
                Thread.sleep(800);
                runOnUiThread(() -> {
                    webView.evaluateJavascript(finalJS, value -> {
                        Toast.makeText(MainActivity.this, "📡 Articles ready!", Toast.LENGTH_SHORT).show();
                    });
                });
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) return "Today";
        try {
            // Simple date formatting
            if (dateString.contains("T")) {
                return dateString.substring(0, 10); // Just the date part
            }
            return dateString;
        } catch (Exception e) {
            return "Today";
        }
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
