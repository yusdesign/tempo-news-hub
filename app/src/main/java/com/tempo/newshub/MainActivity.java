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
    
    private String getGreetingEmoji() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 12) return "🌅";
        else if (hour >= 12 && < 18) return "☀️"; 
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
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // FIX: Open external links in browser instead of WebView
                if (url.startsWith("http")) {
                    // Let system handle it (opens in browser)
                    return false;
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // Inject greeting
                String greeting = getDaytimeGreeting();
                String emoji = getGreetingEmoji();
                String js = "javascript:{" +
                           "document.getElementById('greeting').innerText = '" + greeting + "';" +
                           "document.getElementById('greetingEmoji').innerText = '" + emoji + "';" +
                           "}";
                view.evaluateJavascript(js, null);
                
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
                    Toast.makeText(MainActivity.this, "📰 " + articleCount + " articles", Toast.LENGTH_SHORT).show();
                });
                
                // Build enhanced article data with tags
                StringBuilder js = new StringBuilder("javascript:window.tempoArticles = [");
                for (int i = 0; i < articles.size(); i++) {
                    NewsArticle article = articles.get(i);
                    String[] tags = extractTags(article);
                    
                    js.append("{")
                      .append("id:'").append(i).append("',")
                      .append("title:'").append(cleanForJS(article.getTitle())).append("',")
                      .append("description:'").append(cleanForJS(enhanceDescription(article.getDescription()))).append("',")
                      .append("source:'").append(cleanForJS(article.getSource())).append("',")
                      .append("date:'").append(cleanForJS(formatDate(article.getDate()))).append("',")
                      .append("url:'").append(cleanForJS(article.getUrl())).append("',")
                      .append("tags:['").append(String.join("','", tags)).append("'],")
                      .append("expanded:false")
                      .append("}");
                    if (i < articles.size() - 1) js.append(",");
                }
                js.append("];");
                js.append("if(window.tempoRenderArticles) window.tempoRenderArticles(window.tempoArticles);");
                
                final String finalJS = js.toString();
                
                Thread.sleep(600);
                runOnUiThread(() -> {
                    webView.evaluateJavascript(finalJS, null);
                });
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private String[] extractTags(NewsArticle article) {
        // Extract tags from source and content
        String source = article.getSource().toLowerCase();
        String title = article.getTitle().toLowerCase();
        
        java.util.List<String> tags = new java.util.ArrayList<>();
        
        // Source-based tags
        if (source.contains("world") || title.contains("global")) tags.add("world");
        if (source.contains("tech") || title.contains("ai") || title.contains("digital")) tags.add("tech");
        if (source.contains("business") || title.contains("economy")) tags.add("business");
        if (source.contains("science") || title.contains("research")) tags.add("science");
        if (source.contains("environment") || title.contains("climate")) tags.add("environment");
        if (source.contains("health") || title.contains("medical")) tags.add("health");
        
        // Ensure we have at least one tag
        if (tags.isEmpty()) tags.add("news");
        
        return tags.toArray(new String[0]);
    }
    
    private String enhanceDescription(String description) {
        if (description == null || description.isEmpty()) {
            return "Read the full story on The Guardian for complete coverage and analysis.";
        }
        if (description.length() < 200) {
            return description + " Continue reading on The Guardian for full details and context.";
        }
        return description;
    }
    
    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) return "Today";
        try {
            if (dateString.contains("T")) {
                return dateString.substring(0, 10);
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
