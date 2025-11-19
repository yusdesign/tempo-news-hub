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
        
        getSupportActionBar().hide();
        
        setupWebView();
        webView.loadUrl("file:///android_asset/news_app.html");
        setContentView(webView);
    }
    
    private void setupWebView() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http")) {
                    android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
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
                    Toast.makeText(MainActivity.this, "🎯 " + articleCount + " articles loaded", Toast.LENGTH_SHORT).show();
                });
                
                StringBuilder js = new StringBuilder("javascript:window.tempoArticles = [");
                for (int i = 0; i < articles.size(); i++) {
                    NewsArticle article = articles.get(i);
                    String[] tags = extractTags(article);
                    String imageUrl = getTopicImage(article);
                    
                    js.append("{")
                      .append("id:'").append(i).append("',")
                      .append("title:'").append(cleanForJS(article.getTitle())).append("',")
                      .append("description:'").append(cleanForJS(article.getDescription())).append("',")
                      .append("source:'").append(cleanForJS(article.getSource())).append("',")
                      .append("date:'").append(cleanForJS(formatDate(article.getDate()))).append("',")
                      .append("url:'").append(cleanForJS(article.getUrl())).append("',")
                      .append("image:'").append(cleanForJS(imageUrl)).append("',")
                      .append("tags:['").append(String.join("','", tags)).append("'],")
                      .append("expanded:false")
                      .append("}");
                    if (i < articles.size() - 1) js.append(",");
                }
                js.append("];");
                js.append("if(window.tempoRenderArticles) window.tempoRenderArticles(window.tempoArticles);");
                
                final String finalJS = js.toString();
                
                Thread.sleep(800);
                runOnUiThread(() -> {
                    webView.evaluateJavascript(finalJS, null);
                });
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private String[] extractTags(NewsArticle article) {
        String source = article.getSource().toLowerCase();
        String title = article.getTitle().toLowerCase();
        String desc = article.getDescription().toLowerCase();
        
        java.util.List<String> tags = new java.util.ArrayList<>();
        
        // Smart tagging based on content
        if (source.contains("world") || title.contains("global") || title.contains("international")) tags.add("world");
        if (source.contains("tech") || title.contains("ai") || title.contains("digital")) tags.add("tech");
        if (source.contains("art") || title.contains("design") || title.contains("creative")) tags.add("art & design");
        if (title.contains("psychology") || title.contains("mental") || title.contains("mind")) tags.add("psychology");
        if (source.contains("environment") || title.contains("climate") || title.contains("energy")) tags.add("environment");
        if (source.contains("music") || title.contains("song") || title.contains("album")) tags.add("music");
        
        if (tags.isEmpty()) tags.add("news");
        
        return tags.toArray(new String[0]);
    }
    
    private String getTopicImage(NewsArticle article) {
        String[] tags = extractTags(article);
        String primaryTag = tags.length > 0 ? tags[0] : "news";
        
        // Topic-specific images
        switch (primaryTag) {
            case "tech": return "https://images.unsplash.com/photo-1518709268805-4e9042af2176?w=400&h=200&fit=crop";
            case "art & design": return "https://images.unsplash.com/photo-1541961017774-22349e4a1262?w=400&h=200&fit=crop";
            case "psychology": return "https://images.unsplash.com/photo-1559757148-5c350d0d3c56?w=400&h=200&fit=crop";
            case "environment": return "https://images.unsplash.com/photo-1532094349884-543bc11b234d?w=400&h=200&fit=crop";
            case "music": return "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400&h=200&fit=crop";
            default: return "https://images.unsplash.com/photo-1586339949916-3e9457bef6d3?w=400&h=200&fit=crop";
        }
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
