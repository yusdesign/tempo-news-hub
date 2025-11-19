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
        
        // HIDE the app header (WebView takes full screen)
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
                    Toast.makeText(MainActivity.this, "🎯 " + articleCount + " articles across 6 topics", Toast.LENGTH_SHORT).show();
                });
                
                StringBuilder js = new StringBuilder("javascript:window.tempoArticles = [");
                for (int i = 0; i < articles.size(); i++) {
                    NewsArticle article = articles.get(i);
                    String[] tags = extractTags(article);
                    String imageUrl = getGuardianImageUrl(article, i);
                    
                    js.append("{")
                      .append("id:'").append(i).append("',")
                      .append("title:'").append(cleanForJS(article.getTitle())).append("',")
                      .append("description:'").append(cleanForJS(enhanceDescription(article.getDescription()))).append("',")
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
        String source = article.getSource().toLowerCase();
        String title = article.getTitle().toLowerCase();
        String desc = article.getDescription().toLowerCase();
        
        java.util.List<String> tags = new java.util.ArrayList<>();
        
        // Your specific topics
        if (source.contains("world") || title.contains("global") || title.contains("international") || 
            title.contains("country") || title.contains("nation")) tags.add("world");
        if (source.contains("tech") || title.contains("ai") || title.contains("digital") || 
            title.contains("software") || title.contains("computer") || title.contains("internet")) tags.add("tech");
        if (source.contains("art") || title.contains("design") || title.contains("creative") || 
            title.contains("artist") || title.contains("exhibition") || title.contains("gallery")) tags.add("art & design");
        if (title.contains("psychology") || title.contains("mental") || title.contains("mind") || 
            title.contains("behavior") || title.contains("brain") || title.contains("therapy")) tags.add("psychology");
        if (source.contains("environment") || title.contains("climate") || title.contains("energy") || 
            title.contains("sustainable") || title.contains("planet") || title.contains("nature")) tags.add("environment");
        if (source.contains("music") || title.contains("song") || title.contains("album") || 
            title.contains("concert") || title.contains("band") || title.contains("artist")) tags.add("music");
        
        // Ensure we have at least one tag
        if (tags.isEmpty()) {
            // Fallback to source-based tagging
            if (source.contains("culture") || source.contains("arts")) tags.add("art & design");
            else if (source.contains("science")) tags.add("psychology");
            else if (source.contains("business")) tags.add("tech");
            else tags.add("world");
        }
        
        return tags.toArray(new String[0]);
    }
    
    private String getGuardianImageUrl(NewsArticle article, int index) {
        // Try to get actual Guardian image URLs based on section
        String section = article.getSource().toLowerCase();
        
        // Guardian-themed placeholder images that match their style
        String[] guardianPlaceholders = {
            "https://i.guim.co.uk/img/media/...", // We'll use generic news placeholders
            "https://images.unsplash.com/photo-1586339949916-3e9457bef6d3?w=400&h=200&fit=crop&crop=center", // News
            "https://images.unsplash.com/photo-1518709268805-4e9042af2176?w=400&h=200&fit=crop&crop=center", // Tech
            "https://images.unsplash.com/photo-1541961017774-22349e4a1262?w=400&h=200&fit=crop&crop=center", // Art
            "https://images.unsplash.com/photo-1559757148-5c350d0d3c56?w=400&h=200&fit=crop&crop=center", // Science/Psychology
            "https://images.unsplash.com/photo-1532094349884-543bc11b234d?w=400&h=200&fit=crop&crop=center", // Environment
            "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400&h=200&fit=crop&crop=center", // Music
        };
        
        // Match image to primary tag
        String[] tags = extractTags(article);
        if (tags.length > 0) {
            String primaryTag = tags[0];
            switch (primaryTag) {
                case "tech": return guardianPlaceholders[2];
                case "art & design": return guardianPlaceholders[3];
                case "psychology": return guardianPlaceholders[4];
                case "environment": return guardianPlaceholders[5];
                case "music": return guardianPlaceholders[6];
                default: return guardianPlaceholders[1];
            }
        }
        
        return guardianPlaceholders[1]; // Default news image
    }
    
    private String enhanceDescription(String description) {
        if (description == null || description.isEmpty()) {
            return "Read the full story on The Guardian for complete coverage and analysis.";
        }
        // Make descriptions more engaging
        if (description.length() < 150) {
            return description + " Continue reading on The Guardian for full details and expert analysis.";
        }
        return description.length() > 250 ? description.substring(0, 250) + "..." : description;
    }
    
    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) return "Today";
        try {
            if (dateString.contains("T")) {
                return dateString.substring(0, 10); // YYYY-MM-DD format
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
