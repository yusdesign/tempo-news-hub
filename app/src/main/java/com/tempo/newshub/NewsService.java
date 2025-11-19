package com.tempo.newshub;

import android.util.Log;
import java.util.List;

public class NewsService {
    
    private static final String TAG = "NewsService";
    
    public List<NewsArticle> fetchNews() {
        Log.d(TAG, "🚀 Starting Guardian API Client");
        
        GuardianAPIClient client = new GuardianAPIClient();
        List<NewsArticle> articles = client.fetchArticles();
        
        Log.d(TAG, "🎯 Final result: " + articles.size() + " articles");
        return articles;
    }
}
