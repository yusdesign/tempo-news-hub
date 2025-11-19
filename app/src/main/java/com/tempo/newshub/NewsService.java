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
    
    // YOUR WORKING QUERY that returns real articles with thumbnails
    private static final String GUARDIAN_URL = 
        "https://content.guardianapis.com/search?" +
        "order-by=relevance" +
        "&use-date=last-modified" +
        "&show-elements=all" +
        "&show-fields=thumbnail" +
        "&rights=developer-community" +
        "&q=world,tech,development,psychology,environment,music" +
        "&api-key=" + GUARDIAN_API_KEY +
        "&page-size=20";
    
    public List<NewsArticle> fetchNews() {
        List<NewsArticle> articles = new ArrayList<>();
        
        Log.d(TAG, "🚀 Using YOUR working Guardian API query");
        
        try {
            URL url = new URL(GUARDIAN_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("User-Agent", "TempoNewsHub/1.0");
            
            Log.d(TAG, "📡 Calling YOUR query: " + GUARDIAN_URL);
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
                
                Log.d(TAG, "✅ Response received, parsing REAL articles...");
                articles = parseGuardianResponse(response.toString());
                Log.d(TAG, "🎯 Parsed " + articles.size() + " REAL Guardian articles");
                
            } else {
                Log.e(TAG, "❌ HTTP Error: " + responseCode);
                // Try to read error details
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
                    Log.e(TAG, "🔍 Error details: " + errorResponse.toString());
                } catch (Exception e) {
                    Log.e(TAG, "❌ Could not read error stream");
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Network error: " + e.getMessage());
            e.printStackTrace();
        }
        
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
                Log.d(TAG, "🔍 Full response: " + jsonResponse);
                return articles;
            }
            
            JSONArray results = response.getJSONArray("results");
            Log.d(TAG, "📊 Found " + results.length() + " REAL articles in response");
            
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                
                // THIS IS A REAL GUARDIAN ARTICLE FROM YOUR QUERY
                NewsArticle article = new NewsArticle();
                article.setTitle(result.getString("webTitle"));
                article.setUrl(result.getString("webUrl")); // REAL article URL
                article.setDate(result.getString("webPublicationDate"));
                article.setSource("Guardian " + result.optString("sectionName", "News"));
                
                // Get REAL thumbnail from your query
                try {
                    JSONObject fields = result.getJSONObject("fields");
                    String thumbnail = fields.optString("thumbnail", "");
                    if (!thumbnail.isEmpty()) {
                        // Ensure thumbnail URL is proper
                        if (thumbnail.startsWith("//")) {
                            thumbnail = "https:" + thumbnail;
                        }
                        article.setImageUrl(thumbnail);
                        Log.d(TAG, "🖼️ REAL thumbnail found: " + thumbnail);
                    } else {
                        Log.d(TAG, "📷 No thumbnail for: " + article.getTitle());
                    }
                } catch (Exception e) {
                    Log.d(TAG, "📷 No thumbnail field for: " + article.getTitle());
                }
                
                // Get section for tagging
                String section = result.optString("sectionName", "").toLowerCase();
                if (section.contains("world")) article.setDescription("World news coverage");
                else if (section.contains("tech") || section.contains("technology")) article.setDescription("Technology and innovation");
                else if (section.contains("art") || section.contains("culture")) article.setDescription("Art and cultural coverage");
                else if (section.contains("environment")) article.setDescription("Environmental news");
                else if (section.contains("music")) article.setDescription("Music and entertainment");
                else if (section.contains("science")) article.setDescription("Science and research");
                else article.setDescription("Latest news from The Guardian");
                
                articles.add(article);
                Log.d(TAG, "📰 REAL Article: " + article.getTitle() + " | URL: " + article.getUrl());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Parse error: " + e.getMessage());
            Log.d(TAG, "🔍 JSON snippet: " + jsonResponse.substring(0, Math.min(1000, jsonResponse.length())));
        }
        
        return articles;
    }
}
