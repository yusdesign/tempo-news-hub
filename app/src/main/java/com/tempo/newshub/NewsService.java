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
    
    // Your REAL Guardian API Key
    private static final String GUARDIAN_API_KEY = "1f962fc0-b843-4a63-acb9-770f4c24a86e";
    
    // CORRECT Guardian API URL based on documentation
    private static final String GUARDIAN_URL = 
        "https://content.guardianapis.com/search?" +
        "api-key=" + GUARDIAN_API_KEY +
        "&show-fields=trailText" +
        "&page-size=8" +
        "&order-by=newest" +
        "&show-tags=contributor" +
        "&lang=en";
    
    public List<NewsArticle> fetchNews() {
        List<NewsArticle> articles = new ArrayList<>();
        
        Log.d(TAG, "🚀 Starting news fetch with Guardian API");
        
        // FIRST: Try Guardian API (with proper error handling)
        articles = fetchGuardianAPI();
        if (!articles.isEmpty()) {
            Log.d(TAG, "✅ GUARDIAN API SUCCESS! Loaded " + articles.size() + " articles");
            return articles;
        }
        
        // SECOND: Try simple RSS as backup
        Log.w(TAG, "⚠️ Guardian API failed, trying RSS backup");
        articles = fetchSimpleRSS();
        
        return articles;
    }
    
    private List<NewsArticle> fetchGuardianAPI() {
        List<NewsArticle> articles = new ArrayList<>();
        HttpURLConnection connection = null;
        
        try {
            URL url = new URL(GUARDIAN_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000); // 15 seconds
            connection.setReadTimeout(15000);
            connection.setRequestProperty("User-Agent", "TempoNewsHub/1.0");
            connection.setRequestProperty("Accept", "application/json");
            
            Log.d(TAG, "📡 Calling Guardian API: " + GUARDIAN_URL);
            
            int responseCode = connection.getResponseCode();
            Log.d(TAG, "📊 Guardian API HTTP Response: " + responseCode);
            
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
                
                Log.d(TAG, "✅ Received Guardian API response");
                return parseGuardianResponse(response.toString());
                
            } else {
                // Handle different HTTP errors
                Log.e(TAG, "❌ Guardian API HTTP Error: " + responseCode);
                
                // Try to read error stream
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
                    Log.e(TAG, "🔍 Guardian API Error Response: " + errorResponse.toString());
                } catch (Exception e) {
                    Log.e(TAG, "❌ Could not read error stream: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Guardian API Exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        
        return articles;
    }
    
    private List<NewsArticle> parseGuardianResponse(String jsonResponse) {
        List<NewsArticle> articles = new ArrayList<>();
        
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONObject response = json.getJSONObject("response");
            
            // Check API status
            String status = response.optString("status", "error");
            if (!"ok".equals(status)) {
                Log.e(TAG, "❌ Guardian API status: " + status);
                return articles;
            }
            
            JSONArray results = response.getJSONArray("results");
            Log.d(TAG, "📊 Parsing " + results.length() + " Guardian articles");
            
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                
                NewsArticle article = new NewsArticle();
                article.setTitle(result.getString("webTitle"));
                article.setUrl(result.getString("webUrl"));
                article.setDate(result.getString("webPublicationDate"));
                
                // Get section name for source
                String section = result.optString("sectionName", "News");
                article.setSource("Guardian " + section);
                
                // Get trail text from fields
                try {
                    JSONObject fields = result.getJSONObject("fields");
                    String trailText = fields.optString("trailText", "");
                    if (!trailText.isEmpty()) {
                        // Clean HTML and limit length
                        trailText = trailText.replaceAll("<[^>]*>", "");
                        if (trailText.length() > 140) {
                            trailText = trailText.substring(0, 140) + "...";
                        }
                        article.setDescription(trailText);
                    } else {
                        article.setDescription("Read the full story on The Guardian");
                    }
                } catch (Exception e) {
                    article.setDescription("Latest news from The Guardian");
                }
                
                articles.add(article);
                Log.d(TAG, "📰 Added: " + article.getTitle());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing Guardian JSON: " + e.getMessage());
            Log.d(TAG, "🔍 JSON Response: " + jsonResponse.substring(0, Math.min(500, jsonResponse.length())));
        }
        
        return articles;
    }
    
    private List<NewsArticle> fetchSimpleRSS() {
        List<NewsArticle> articles = new ArrayList<>();
        
        // Simple BBC RSS as reliable backup
        String rssUrl = "https://feeds.bbci.co.uk/news/world/rss.xml";
        
        try {
            String proxyUrl = "https://api.rss2json.com/v1/api.json?rss_url=" + 
                             java.net.URLEncoder.encode(rssUrl, "UTF-8");
            
            URL url = new URL(proxyUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            
            int responseCode = connection.getResponseCode();
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
                
                return parseRSSResponse(response.toString());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ RSS backup also failed: " + e.getMessage());
        }
        
        // Final fallback
        return getCuratedNews();
    }
    
    private List<NewsArticle> parseRSSResponse(String jsonResponse) {
        List<NewsArticle> articles = new ArrayList<>();
        
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONArray items = json.getJSONArray("items");
            
            for (int i = 0; i < Math.min(items.length(), 6); i++) {
                JSONObject item = items.getJSONObject(i);
                
                NewsArticle article = new NewsArticle();
                article.setTitle(item.getString("title"));
                article.setUrl(item.getString("link"));
                article.setSource("BBC News");
                
                String description = item.optString("description", "");
                description = description.replaceAll("<[^>]*>", "");
                if (description.length() > 120) {
                    description = description.substring(0, 120) + "...";
                }
                article.setDescription(description);
                
                String pubDate = item.optString("pubDate", "");
                article.setDate(pubDate);
                
                articles.add(article);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error parsing RSS: " + e.getMessage());
        }
        
        return articles;
    }
    
    private List<NewsArticle> getCuratedNews() {
        List<NewsArticle> articles = new ArrayList<>();
        String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        
        String[][] newsData = {
            {"Global Climate Conference Reaches New Agreements", "World leaders agree on enhanced emissions targets at latest international summit.", "World News"},
            {"Technology Sector Reports Strong Quarterly Results", "Major tech companies exceed earnings expectations amid AI innovation surge.", "Technology"},
            {"Breakthrough in Renewable Energy Storage", "New battery technology promises improved capacity for solar and wind systems.", "Science"},
            {"Economic Indicators Show Positive Trends", "Global markets respond to improved economic data and policy developments.", "Business"}
        };
        
        for (String[] data : newsData) {
            NewsArticle article = new NewsArticle();
            article.setTitle(data[0]);
            article.setDescription(data[1]);
            article.setDate(currentDate);
            article.setSource(data[2]);
            article.setUrl("https://www.theguardian.com/international");
            articles.add(article);
        }
        
        return articles;
    }
}
