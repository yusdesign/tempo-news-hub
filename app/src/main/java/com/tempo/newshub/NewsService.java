package com.tempo.newshub;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NewsService {
    
    private static final String TAG = "NewsService";
    private static final String GUARDIAN_API_KEY = "1f962fc0-b843-4a63-acb9-770f4c24a86e";
    
    // SIMPLE STABLE QUERY
    private static final String GUARDIAN_URL = 
        "https://content.guardianapis.com/search?" +
        "api-key=" + GUARDIAN_API_KEY +
        "&page-size=10" +
        "&show-fields=thumbnail";
    
    public List<NewsArticle> fetchNews() {
        List<NewsArticle> articles = new ArrayList<>();
        
        Log.d(TAG, "🔍 Testing Guardian API");
        
        try {
            URL url = new URL(GUARDIAN_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); // Shorter timeout
            connection.setReadTimeout(10000);
            
            int responseCode = connection.getResponseCode();
            Log.d(TAG, "HTTP: " + responseCode);
            
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
                );
                
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                articles = parseResponse(response.toString());
                Log.d(TAG, "Articles: " + articles.size());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
        
        return articles;
    }
    
    private List<NewsArticle> parseResponse(String jsonResponse) {
        List<NewsArticle> articles = new ArrayList<>();
        
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONObject response = json.getJSONObject("response");
            
            if ("ok".equals(response.optString("status"))) {
                JSONArray results = response.getJSONArray("results");
                
                for (int i = 0; i < results.length(); i++) {
                    JSONObject result = results.getJSONObject(i);
                    
                    NewsArticle article = new NewsArticle();
                    article.setTitle(result.getString("webTitle"));
                    article.setUrl(result.getString("webUrl"));
                    article.setDate(result.getString("webPublicationDate"));
                    article.setSource("Guardian");
                    
                    // Get thumbnail
                    try {
                        JSONObject fields = result.getJSONObject("fields");
                        String thumbnail = fields.optString("thumbnail", "");
                        if (!thumbnail.isEmpty()) {
                            article.setImageUrl(thumbnail);
                        }
                    } catch (Exception e) {
                        // No thumbnail
                    }
                    
                    articles.add(article);
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Parse error");
        }
        
        return articles;
    }
}
