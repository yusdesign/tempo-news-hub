package com.tempo.newshub;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GuardianAPIClient {
    
    private static final String TAG = "GuardianAPIClient";
    private static final String API_KEY = "1f962fc0-b843-4a63-acb9-770f4c24a86e";
    private static final String BASE_URL = "https://content.guardianapis.com";
    
    // PROPER Guardian API endpoint construction
    public List<NewsArticle> fetchArticles() {
        List<NewsArticle> articles = new ArrayList<>();
        
        try {
            // Build proper Guardian API URL like the clients do
            String apiUrl = buildSearchURL();
            Log.d(TAG, "🔗 API URL: " + apiUrl);
            
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "Tempo-Android/2.6.46");
            
            int responseCode = connection.getResponseCode();
            Log.d(TAG, "📡 Response Code: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                String response = readStream(inputStream);
                inputStream.close();
                
                Log.d(TAG, "✅ Response length: " + response.length());
                articles = parseArticles(response);
                
            } else {
                Log.e(TAG, "❌ HTTP Error: " + responseCode);
                // Try to read error stream
                try {
                    InputStream errorStream = connection.getErrorStream();
                    if (errorStream != null) {
                        String errorResponse = readStream(errorStream);
                        Log.e(TAG, "🔍 Error Response: " + errorResponse);
                        errorStream.close();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ No error stream");
                }
            }
            
            connection.disconnect();
            
        } catch (Exception e) {
            Log.e(TAG, "💥 Exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
        
        Log.d(TAG, "📦 Returning: " + articles.size() + " articles");
        return articles;
    }
    
    private String buildSearchURL() {
        // Proper Guardian API URL construction like the clients
        return BASE_URL + "/search?" +
               "api-key=" + API_KEY +
               "&page-size=12" +
               "&show-fields=thumbnail,trailText" +
               "&show-tags=keyword" +
               "&order-by=newest" +
               "&q=(world OR technology OR science OR environment OR music OR art)";
    }
    
    private String readStream(InputStream inputStream) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        reader.close();
        return stringBuilder.toString();
    }
    
    private List<NewsArticle> parseArticles(String jsonResponse) {
        List<NewsArticle> articles = new ArrayList<>();
        
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONObject response = json.getJSONObject("response");
            
            String status = response.getString("status");
            if (!"ok".equals(status)) {
                Log.e(TAG, "❌ API Status: " + status);
                return articles;
            }
            
            JSONArray results = response.getJSONArray("results");
            Log.d(TAG, "📊 Results count: " + results.length());
            
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                NewsArticle article = parseArticle(result);
                if (article != null) {
                    articles.add(article);
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Parse Error: " + e.getMessage());
            // Log first 500 chars of response for debugging
            Log.d(TAG, "🔍 Response snippet: " + 
                (jsonResponse.length() > 500 ? jsonResponse.substring(0, 500) + "..." : jsonResponse));
        }
        
        return articles;
    }
    
    private NewsArticle parseArticle(JSONObject result) {
        try {
            NewsArticle article = new NewsArticle();
            
            // Required fields
            article.setTitle(result.getString("webTitle"));
            article.setUrl(result.getString("webUrl"));
            article.setDate(result.getString("webPublicationDate"));
            article.setSource("Guardian");
            
            // Optional fields with proper error handling
            if (result.has("sectionName")) {
                article.setSource("Guardian " + result.getString("sectionName"));
            }
            
            // Fields object
            if (result.has("fields")) {
                JSONObject fields = result.getJSONObject("fields");
                
                if (fields.has("thumbnail")) {
                    String thumbnail = fields.getString("thumbnail");
                    if (thumbnail != null && !thumbnail.isEmpty()) {
                        // Fix protocol-relative URLs
                        if (thumbnail.startsWith("//")) {
                            thumbnail = "https:" + thumbnail;
                        }
                        article.setImageUrl(thumbnail);
                    }
                }
                
                if (fields.has("trailText")) {
                    String trailText = fields.getString("trailText");
                    if (trailText != null && !trailText.isEmpty()) {
                        // Clean HTML and truncate
                        trailText = trailText.replaceAll("<[^>]*>", "").trim();
                        if (trailText.length() > 150) {
                            trailText = trailText.substring(0, 150) + "...";
                        }
                        article.setDescription(trailText);
                    }
                }
            }
            
            Log.d(TAG, "📰 Article: " + article.getTitle());
            return article;
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Article parse error: " + e.getMessage());
            return null;
        }
    }
}
