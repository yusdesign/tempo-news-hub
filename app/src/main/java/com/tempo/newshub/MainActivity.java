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
        Toast.makeText(this, "🔍 Testing Guardian API Only", Toast.LENGTH_SHORT).show();

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
                    if (articleCount > 0) {
                        Toast.makeText(MainActivity.this, "✅ " + articleCount + " Guardian articles", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "❌ No Guardian articles", Toast.LENGTH_LONG).show();
                    }
                });
                
                StringBuilder js = new StringBuilder("javascript:window.tempoArticles = [");
                for (int i = 0; i < articles.size(); i++) {
                    NewsArticle article = articles.get(i);
                    String[] tags = extractTagsFromSource(article.getSource());
                    
                    js.append("{")
                      .append("id:'").append(i).append("',")
                      .append("title:'").append(cleanForJS(article.getTitle())).append("',")
                      .append("description:'").append(cleanForJS("Real Guardian article - " + article.getSource())).append("',")
                      .append("source:'").append(cleanForJS(article.getSource())).append("',")
                      .append("date:'").append(cleanForJS(formatDate(article.getDate()))).append("',")
                      .append("url:'").append(cleanForJS(article.getUrl())).append("',")
                      .append("image:'").append(cleanForJS(article.getImageUrl())).append("',")
                      .append("tags:['").append(String.join("','", tags)).append("'],")
                      .append("expanded:false")
                      .append("}");
                    if (i < articles.size() - 1) js.append(",");
                }
                js.append("];");
                js.append("if(window.tempoRenderArticles) window.tempoRenderArticles(window.tempoArticles);");
                
                final String finalJS = js.toString();
                
                Thread.sleep(1000);
                runOnUiThread(() -> {
                    webView.evaluateJavascript(finalJS, value -> {
                        Log.d("MainActivity", "📡 Data injected to WebView");
                    });
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "❌ Failed to load Guardian API", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
    
    private String[] extractTagsFromSource(String source) {
        if (source.contains("world")) return new String[]{"world"};
        if (source.contains("tech")) return new String[]{"tech"};
        if (source.contains("art")) return new String[]{"art & design"};
        if (source.contains("science")) return new String[]{"psychology"};
        if (source.contains("environment")) return new String[]{"environment"};
        if (source.contains("music")) return new String[]{"music"};
        return new String[]{"news"};
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
