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
    
    // YOUR WORKING GUARDIAN API QUERY - NO FALLBACKS
    private static final String GUARDIAN_URL = 
        "https://content.guardianapis.com/search?" +
        "order-by=relevance" +
        "&use-date=last-modified" +
        "&show-elements=all" +
        "&show-fields=thumbnail" +
        "&rights=developer-community" +
        "&q=world,tech,development,psychology,environment,music" +
        "&api-key=" + GUARDIAN_API_KEY +
        "&page-size=15";
    
    public List<NewsArticle> fetchNews() {
        List<NewsArticle> articles = new ArrayList<>();
        
        Log.d(TAG, "🚀 GUARDIAN API ONLY - NO FALLBACKS");
        Log.d(TAG, "🔑 API Key: " + GUARDIAN_API_KEY.substring(0, 8) + "...");
        
        try {
            URL url = new URL(GUARDIAN_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(20000);
            connection.setRequestProperty("User-Agent", "TempoNewsHub/2.6.44");
            connection.setRequestProperty("Accept", "application/json");
            
            Log.d(TAG, "📡 Calling Guardian API: " + GUARDIAN_URL);
            int responseCode = connection.getResponseCode();
            Log.d(TAG, "📊 HTTP Response: " + responseCode);
            
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
                
                Log.d(TAG, "✅ API Response received, length: " + response.length());
                articles = parseGuardianResponse(response.toString());
                Log.d(TAG, "🎯 Parsed " + articles.size() + " REAL Guardian articles");
                
            } else {
                Log.e(TAG, "❌ HTTP Error: " + responseCode);
                // Detailed error info
                try {
                    BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream())
                    );
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        errorResponse.append(errorLine);
                    }
                    errorReader.close();
                    Log.e(TAG, "🔍 Error Response: " + errorResponse.toString());
                } catch (Exception e) {
                    Log.e(TAG, "❌ No error stream available");
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Network Exception: " + e.getClass().getSimpleName());
            Log.e(TAG, "❌ Error Message: " + e.getMessage());
            e.printStackTrace();
        }
        
        // GUARDIAN API ONLY - return empty if API fails
        Log.d(TAG, "📦 Returning " + articles.size() + " Guardian articles");
        return articles;
    }
    
    private List<NewsArticle> parseGuardianResponse(String jsonResponse) {
        List<NewsArticle> articles = new ArrayList<>();
        
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONObject response = json.getJSONObject("response");
            
            String status = response.optString("status", "error");
            if (!"ok".equals(status)) {
                Log.e(TAG, "❌ API Status: " + status);
                Log.d(TAG, "🔍 Full response for debugging: " + jsonResponse);
                return articles;
            }
            
            JSONArray results = response.getJSONArray("results");
            Log.d(TAG, "📊 Found " + results.length() + " articles in Guardian API");
            
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                
                // REAL GUARDIAN ARTICLE DATA
                NewsArticle article = new NewsArticle();
                article.setTitle(result.getString("webTitle"));
                article.setUrl(result.getString("webUrl"));
                article.setDate(result.getString("webPublicationDate"));
                article.setSource("Guardian " + result.optString("sectionName", "News"));
                
                // REAL GUARDIAN THUMBNAIL
                try {
                    JSONObject fields = result.getJSONObject("fields");
                    String thumbnail = fields.optString("thumbnail", "");
                    if (!thumbnail.isEmpty()) {
                        if (thumbnail.startsWith("//")) {
                            thumbnail = "https:" + thumbnail;
                        }
                        article.setImageUrl(thumbnail);
                        Log.d(TAG, "🖼️ Guardian thumbnail: " + thumbnail);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "📷 No thumbnail for: " + article.getTitle());
                }
                
                articles.add(article);
                Log.d(TAG, "📰 Guardian Article: " + article.getTitle());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ JSON Parse Error: " + e.getMessage());
            Log.d(TAG, "🔍 Response snippet: " + (jsonResponse.length() > 1000 ? jsonResponse.substring(0, 1000) + "..." : jsonResponse));
        }
        
        return articles;
    }
}
