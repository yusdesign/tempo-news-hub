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
    
    // Simple working query
    private static final String GUARDIAN_URL = 
        "https://content.guardianapis.com/search?" +
        "api-key=" + GUARDIAN_API_KEY +
        "&page-size=12" +
        "&show-fields=thumbnail,trailText";
    
    public List<NewsArticle> fetchNews() {
        List<NewsArticle> articles = new ArrayList<>();
        
        Log.d(TAG, "🚀 Starting Guardian API call");
        
        try {
            URL url = new URL(GUARDIAN_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            
            Log.d(TAG, "📡 Calling: " + GUARDIAN_URL);
            int responseCode = connection.getResponseCode();
            Log.d(TAG, "📊 Response: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
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
                Log.d(TAG, "✅ Parsed " + articles.size() + " articles");
                
            } else {
                Log.e(TAG, "❌ HTTP Error: " + responseCode);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Network error: " + e.getMessage());
        }
        
        // Always return content
        if (articles.isEmpty()) {
            articles = getFallbackArticles();
            Log.d(TAG, "🔄 Using fallback: " + articles.size() + " articles");
        }
        
        return articles;
    }
    
    private List<NewsArticle> parseResponse(String jsonResponse) {
        List<NewsArticle> articles = new ArrayList<>();
        
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONObject response = json.getJSONObject("response");
            
            if (!"ok".equals(response.optString("status", "error"))) {
                return articles;
            }
            
            JSONArray results = response.getJSONArray("results");
            
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                
                NewsArticle article = new NewsArticle();
                article.setTitle(result.getString("webTitle"));
                article.setUrl(result.getString("webUrl"));
                article.setDate(result.getString("webPublicationDate"));
                article.setSource("Guardian " + result.optString("sectionName", "News"));
                
                // Get thumbnail and description
                try {
                    JSONObject fields = result.getJSONObject("fields");
                    String thumbnail = fields.optString("thumbnail", "");
                    if (!thumbnail.isEmpty()) {
                        if (thumbnail.startsWith("//")) {
                            thumbnail = "https:" + thumbnail;
                        }
                        article.setImageUrl(thumbnail);
                    }
                    
                    String trailText = fields.optString("trailText", "");
                    if (!trailText.isEmpty()) {
                        trailText = trailText.replaceAll("<[^>]*>", "");
                        article.setDescription(trailText.length() > 120 ? trailText.substring(0, 120) + "..." : trailText);
                    } else {
                        article.setDescription("Read the full story on The Guardian");
                    }
                } catch (Exception e) {
                    article.setDescription("Latest news from The Guardian");
                }
                
                articles.add(article);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Parse error: " + e.getMessage());
        }
        
        return articles;
    }
    
    private List<NewsArticle> getFallbackArticles() {
        List<NewsArticle> articles = new ArrayList<>();
        String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        
        String[][] data = {
            {"Global Climate Conference Reaches Agreement", "World leaders unite on climate action and emissions targets.", "world", "https://images.unsplash.com/photo-1569163139394-de44cb54d0c9?w=400&h=200&fit=crop"},
            {"AI Revolution Transforms Industries", "Artificial intelligence enables breakthrough innovations across sectors.", "tech", "https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=400&h=200&fit=crop"},
            {"Digital Art Market Continues Growth", "Online platforms reshape contemporary art creation and distribution.", "art & design", "https://images.unsplash.com/photo-1541961017774-22349e4a1262?w=400&h=200&fit=crop"},
            {"Mental Health Apps Gain Support", "Research validates digital therapy tools for wellbeing.", "psychology", "https://images.unsplash.com/photo-1559757148-5c350d0d3c56?w=400&h=200&fit=crop"},
            {"Renewable Energy Costs Decline", "Solar and wind power become more affordable worldwide.", "environment", "https://images.unsplash.com/photo-1466611653911-95081537e5b7?w=400&h=200&fit=crop"},
            {"Streaming Reshapes Music Discovery", "Algorithms change how listeners find new artists.", "music", "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400&h=200&fit=crop"}
        };
        
        for (String[] item : data) {
            NewsArticle article = new NewsArticle();
            article.setTitle(item[0]);
            article.setDescription(item[1]);
            article.setDate(currentDate);
            article.setSource("Guardian " + item[2]);
            article.setImageUrl(item[3]);
            article.setUrl("https://www.theguardian.com/" + item[2].replace(" & ", ""));
            articles.add(article);
        }
        
        return articles;
    }
}
