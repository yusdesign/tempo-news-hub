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
    
    // Your WORKING Guardian API Key
    private static final String GUARDIAN_API_KEY = "1f962fc0-b843-4a63-acb9-770f4c24a86e";
    
    // SIMPLE Guardian API URL - Based on your working test
    private static final String GUARDIAN_URL = 
        "https://content.guardianapis.com/search?" +
        "api-key=" + GUARDIAN_API_KEY +
        "&page-size=10" +
        "&order-by=newest" +
        "&show-fields=trailText";
    
    public List<NewsArticle> fetchNews() {
        List<NewsArticle> articles = new ArrayList<>();
        
        Log.d(TAG, "🚀 Starting Guardian API fetch");
        
        // Try Guardian API with simpler approach
        articles = fetchGuardianAPI();
        if (!articles.isEmpty()) {
            Log.d(TAG, "✅ GUARDIAN API SUCCESS! " + articles.size() + " articles");
            return articles;
        }
        
        // Fallback to RSS
        Log.w(TAG, "⚠️ Guardian API failed, using RSS");
        return fetchSimpleRSS();
    }
    
    private List<NewsArticle> fetchGuardianAPI() {
        List<NewsArticle> articles = new ArrayList<>();
        
        try {
            Log.d(TAG, "📡 Calling: " + GUARDIAN_URL);
            
            URL url = new URL(GUARDIAN_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(10000);
            
            // SIMPLE headers only
            connection.setRequestProperty("Accept", "application/json");
            
            Log.d(TAG, "🕐 Connecting to Guardian API...");
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
                
                Log.d(TAG, "✅ Received API response, length: " + response.length());
                return parseGuardianResponse(response.toString());
            } else {
                Log.e(TAG, "❌ HTTP Error: " + responseCode);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Network Exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());
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
                return articles;
            }
            
            JSONArray results = response.getJSONArray("results");
            Log.d(TAG, "📊 Parsing " + results.length() + " articles");
            
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                
                NewsArticle article = new NewsArticle();
                article.setTitle(result.getString("webTitle"));
                article.setUrl(result.getString("webUrl"));
                article.setDate(result.getString("webPublicationDate"));
                
                String section = result.optString("sectionName", "News");
                article.setSource("Guardian " + section);
                
                // Get trail text
                try {
                    JSONObject fields = result.getJSONObject("fields");
                    String trailText = fields.optString("trailText", "");
                    if (!trailText.isEmpty()) {
                        trailText = trailText.replaceAll("<[^>]*>", "");
                        if (trailText.length() > 120) {
                            trailText = trailText.substring(0, 120) + "...";
                        }
                        article.setDescription(trailText);
                    } else {
                        article.setDescription("Read the full story on The Guardian");
                    }
                } catch (Exception e) {
                    article.setDescription("Latest news from The Guardian");
                }
                
                articles.add(article);
            }
            
            Log.d(TAG, "✅ Successfully parsed " + articles.size() + " articles");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Parse Error: " + e.getMessage());
        }
        
        return articles;
    }
    
    private List<NewsArticle> fetchSimpleRSS() {
        List<NewsArticle> articles = new ArrayList<>();
        
        try {
            String rssUrl = "https://feeds.bbci.co.uk/news/world/rss.xml";
            String proxyUrl = "https://api.rss2json.com/v1/api.json?rss_url=" + 
                             java.net.URLEncoder.encode(rssUrl, "UTF-8");
            
            URL url = new URL(proxyUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            
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
            Log.e(TAG, "RSS backup failed: " + e.getMessage());
        }
        
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
            {"Global Climate Conference Reaches Agreements", "World leaders agree on enhanced climate targets at international summit.", "World News"},
            {"Technology Sector Reports Strong Growth", "Tech companies exceed earnings expectations amid innovation surge.", "Technology"},
            {"Economic Indicators Show Positive Trends", "Markets respond to improved economic data and developments.", "Business"}
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
