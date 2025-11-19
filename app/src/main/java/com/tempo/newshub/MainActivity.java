package com.tempo.newshub;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    
    private WebView webView;
    private boolean isInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Simple stable setup
        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        
        // Basic WebView client
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (!isInitialized) {
                    loadNewsData();
                    isInitialized = true;
                }
            }
        });
        
        webView.loadUrl("file:///android_asset/news_app.html");
        setContentView(webView);
    }
    
    private void loadNewsData() {
        new Thread(() -> {
            try {
                NewsService newsService = new NewsService();
                java.util.List<NewsArticle> articles = newsService.fetchNews();
                
                // Build simple JavaScript data
                StringBuilder js = new StringBuilder("javascript:window.guardianArticles = [");
                for (int i = 0; i < articles.size(); i++) {
                    NewsArticle article = articles.get(i);
                    js.append("{")
                      .append("id:").append(i).append(",")
                      .append("title:'").append(escapeJS(article.getTitle())).append("',")
                      .append("url:'").append(escapeJS(article.getUrl())).append("',")
                      .append("date:'").append(escapeJS(article.getDate())).append("',")
                      .append("source:'").append(escapeJS(article.getSource())).append("',")
                      .append("image:'").append(escapeJS(article.getImageUrl())).append("'")
                      .append("}");
                    if (i < articles.size() - 1) js.append(",");
                }
                js.append("];");
                js.append("if(window.renderGuardianArticles) window.renderGuardianArticles(window.guardianArticles);");
                
                final String finalJS = js.toString();
                
                // Run on UI thread
                runOnUiThread(() -> {
                    webView.evaluateJavascript(finalJS, null);
                });
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private String escapeJS(String text) {
        if (text == null) return "";
        return text.replace("'", "\\'")
                  .replace("\"", "\\\"")
                  .replace("\n", " ")
                  .replace("\r", " ");
    }
    
    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
