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
    
    // CORRECT Guardian API queries for real articles
    private static final String[] GUARDIAN_URLS = {
        // World news - REAL section
        "https://content.guardianapis.com/world?api-key=" + GUARDIAN_API_KEY + "&show-fields=thumbnail,trailText&page-size=5",
        // Technology - REAL section  
        "https://content.guardianapis.com/technology?api-key=" + GUARDIAN_API_KEY + "&show-fields=thumbnail,trailText&page-size=5",
        // Art & design - from Culture section
        "https://content.guardianapis.com/artanddesign?api-key=" + GUARDIAN_API_KEY + "&show-fields=thumbnail,trailText&page-size=5",
        // Environment - REAL section
        "https://content.guardianapis.com/environment?api-key=" + GUARDIAN_API_KEY + "&show-fields=thumbnail,trailText&page-size=5",
        // Music - REAL section
        "https://content.guardianapis.com/music?api-key=" + GUARDIAN_API_KEY + "&show-fields=thumbnail,trailText&page-size=5",
        // Science (for psychology) - REAL section
        "https://content.guardianapis.com/science?api-key=" + GUARDIAN_API_KEY + "&show-fields=thumbnail,trailText&page-size=5"
    };
    
    public List<NewsArticle> fetchNews() {
        List<NewsArticle> articles = new ArrayList<>();
        
        Log.d(TAG, "🚀 Fetching REAL Guardian articles from specific sections");
        
        // Fetch from each REAL Guardian section
        for (int i = 0; i < GUARDIAN_URLS.length; i++) {
            List<NewsArticle> sectionArticles = fetchSectionArticles(GUARDIAN_URLS[i], i);
            articles.addAll(sectionArticles);
            Log.d(TAG, "✅ Section " + i + ": " + sectionArticles.size() + " articles");
        }
        
        Log.d(TAG, "🎯 TOTAL real articles: " + articles.size());
        return articles;
    }
    
    private List<NewsArticle> fetchSectionArticles(String apiUrl, int sectionIndex) {
        List<NewsArticle> articles = new ArrayList<>();
        
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            
            Log.d(TAG, "📡 Calling REAL section: " + apiUrl);
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
                
                return parseSectionResponse(response.toString(), sectionIndex);
                
            } else {
                Log.e(TAG, "❌ HTTP " + responseCode + " for section " + sectionIndex);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Section " + sectionIndex + " error: " + e.getMessage());
        }
        
        return articles;
    }
    
    private List<NewsArticle> parseSectionResponse(String jsonResponse, int sectionIndex) {
        List<NewsArticle> articles = new ArrayList<>();
        String[] sections = {"world", "technology", "artanddesign", "environment", "music", "science"};
        String currentSection = sections[sectionIndex];
        
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONObject response = json.getJSONObject("response");
            
            if (!"ok".equals(response.optString("status", "error"))) {
                Log.e(TAG, "❌ API status error for " + currentSection);
                return articles;
            }
            
            JSONArray results = response.getJSONArray("results");
            Log.d(TAG, "📊 Section '" + currentSection + "' found " + results.length() + " REAL articles");
            
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                
                // THIS IS A REAL GUARDIAN ARTICLE
                NewsArticle article = new NewsArticle();
                article.setTitle(result.getString("webTitle"));
                article.setUrl(result.getString("webUrl")); // REAL article URL
                article.setDate(result.getString("webPublicationDate"));
                article.setSource("Guardian " + currentSection);
                
                // Get REAL thumbnail and description
                try {
                    JSONObject fields = result.getJSONObject("fields");
                    
                    // REAL Guardian thumbnail
                    String thumbnail = fields.optString("thumbnail", "");
                    if (!thumbnail.isEmpty()) {
                        // Fix thumbnail URL if needed
                        if (thumbnail.startsWith("//")) {
                            thumbnail = "https:" + thumbnail;
                        }
                        article.setImageUrl(thumbnail);
                        Log.d(TAG, "🖼️ REAL thumbnail: " + thumbnail);
                    }
                    
                    // REAL Guardian trail text
                    String trailText = fields.optString("trailText", "");
                    if (!trailText.isEmpty()) {
                        trailText = cleanHtml(trailText);
                        article.setDescription(trailText);
                    } else {
                        article.setDescription("Latest from Guardian " + currentSection);
                    }
                } catch (Exception e) {
                    article.setDescription("Real Guardian article");
                }
                
                articles.add(article);
                Log.d(TAG, "📰 REAL Article: " + article.getTitle());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Parse error for " + currentSection + ": " + e.getMessage());
        }
        
        return articles;
    }
    
    private String cleanHtml(String html) {
        return html.replaceAll("<[^>]*>", "").replace("&nbsp;", " ").replace("&amp;", "&").trim();
    }
}
