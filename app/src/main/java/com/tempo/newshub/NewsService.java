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
    
    // 🎉 YOUR REAL GUARDIAN API KEY!
    private static final String GUARDIAN_API_KEY = "1f962fc0-b843-4a63-acb9-770f4c24a86e";
    private static final String GUARDIAN_URL = "https://content.guardianapis.com/search?api-key=" + GUARDIAN_API_KEY + "&show-fields=trailText,thumbnail&page-size=12&order-by=newest&section=world|technology|business|science";
    
    public List<NewsArticle> fetchNews() {
        List<NewsArticle> articles = new ArrayList<>();
        
        Log.d(TAG, "🚀 Starting news fetch with REAL Guardian API key!");
        
        // FIRST: Try REAL Guardian API with your key
        articles = fetchGuardianAPI();
        if (!articles.isEmpty()) {
            Log.d(TAG, "✅ GUARDIAN API SUCCESS! Loaded " + articles.size() + " real articles");
            return articles;
        }
        
        // SECOND: Fallback to RSS (only if Guardian fails)
        Log.w(TAG, "⚠️ Guardian API failed, trying RSS fallback");
        articles = fetchReliableRSS();
        if (!articles.isEmpty()) {
            Log.d(TAG, "✅ RSS fallback worked: " + articles.size() + " articles");
            return articles;
        }
        
        // FINAL: Curated news (should rarely happen now)
        Log.w(TAG, "❌ All sources failed, using curated news");
        return getCuratedNews();
    }
    
    private List<NewsArticle> fetchGuardianAPI() {
        List<NewsArticle> articles = new ArrayList<>();
        
        try {
            URL url = new URL(GUARDIAN_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", "TempoNewsHub/1.0");
            
            Log.d(TAG, "📡 Calling REAL Guardian API...");
            int responseCode = connection.getResponseCode();
            Log.d(TAG, "Guardian API response code: " + responseCode);
            
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
                
                Log.d(TAG, "✅ Guardian API response received, parsing...");
                return parseGuardianResponse(response.toString());
                
            } else {
                Log.e(TAG, "❌ Guardian API HTTP error: " + responseCode);
                if (responseCode == 403) {
                    Log.e(TAG, "🔑 API KEY ISSUE - Check if key is valid");
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Guardian API exception: " + e.getMessage());
        }
        
        return articles;
    }
    
    private List<NewsArticle> parseGuardianResponse(String jsonResponse) {
        List<NewsArticle> articles = new ArrayList<>();
        
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONObject response = json.getJSONObject("response");
            JSONArray results = response.getJSONArray("results");
            
            Log.d(TAG, "📊 Parsing " + results.length() + " Guardian articles");
            
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                
                NewsArticle article = new NewsArticle();
                article.setTitle(result.getString("webTitle"));
                article.setUrl(result.getString("webUrl"));
                article.setDate(result.getString("webPublicationDate"));
                article.setSource("The Guardian");
                
                // Get section name for better categorization
                String section = result.optString("sectionName", "News");
                if (!section.equals("News")) {
                    article.setSource("Guardian " + section);
                }
                
                // Get trail text if available
                try {
                    JSONObject fields = result.getJSONObject("fields");
                    String trailText = fields.optString("trailText", "");
                    if (!trailText.isEmpty()) {
                        article.setDescription(trailText);
                    } else {
                        article.setDescription("Read the full story on The Guardian");
                    }
                } catch (Exception e) {
                    article.setDescription("Latest news from The Guardian");
                }
                
                articles.add(article);
                
                Log.d(TAG, "📰 Article: " + article.getTitle());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing Guardian JSON: " + e.getMessage());
        }
        
        return articles;
    }
    
    private List<NewsArticle> fetchReliableRSS() {
        List<NewsArticle> articles = new ArrayList<>();
        
        String[] rssSources = {
            "https://feeds.bbci.co.uk/news/world/rss.xml",
            "https://rss.cnn.com/rss/edition.rss"
        };
        
        for (String rssUrl : rssSources) {
            try {
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
                    
                    List<NewsArticle> rssArticles = parseRSSResponse(response.toString(), rssUrl);
                    if (!rssArticles.isEmpty()) {
                        articles.addAll(rssArticles);
                        break;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "RSS source failed: " + rssUrl);
            }
        }
        
        return articles;
    }
    
    private List<NewsArticle> parseRSSResponse(String jsonResponse, String sourceUrl) {
        List<NewsArticle> articles = new ArrayList<>();
        
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONArray items = json.getJSONArray("items");
            
            String sourceName = "BBC News";
            if (sourceUrl.contains("cnn")) sourceName = "CNN";
            
            for (int i = 0; i < Math.min(items.length(), 6); i++) {
                JSONObject item = items.getJSONObject(i);
                
                NewsArticle article = new NewsArticle();
                article.setTitle(item.getString("title"));
                article.setUrl(item.getString("link"));
                article.setSource(sourceName);
                
                String description = item.optString("description", "");
                description = description.replaceAll("<[^>]*>", "");
                if (description.length() > 120) {
                    description = description.substring(0, 120) + "...";
                } else if (description.isEmpty()) {
                    description = "Read the full story on " + sourceName;
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
            {"Global Climate Conference Reaches Agreements", "World leaders agree on enhanced climate targets at latest international summit.", "World News"},
            {"Technology Sector Reports Strong Growth", "Major tech companies exceed earnings expectations amid innovation surge.", "Tech News"},
            {"Breakthrough in Renewable Energy Research", "New battery technology promises improved storage for sustainable energy.", "Science"},
            {"Economic Indicators Show Positive Trends", "Markets respond to improved economic data and policy developments.", "Business"}
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
