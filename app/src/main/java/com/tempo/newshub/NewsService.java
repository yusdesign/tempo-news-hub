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
    
    // Multiple focused API calls for better content
    private static final String[] API_URLS = {
        // World news
        "https://content.guardianapis.com/search?api-key=" + GUARDIAN_API_KEY + "&section=world&page-size=3&order-by=newest&show-fields=trailText",
        // Technology
        "https://content.guardianapis.com/search?api-key=" + GUARDIAN_API_KEY + "&section=technology&page-size=3&order-by=newest&show-fields=trailText", 
        // Art & design
        "https://content.guardianapis.com/search?api-key=" + GUARDIAN_API_KEY + "&q=art%20OR%20design%20OR%20culture&page-size=3&order-by=newest&show-fields=trailText",
        // Environment
        "https://content.guardianapis.com/search?api-key=" + GUARDIAN_API_KEY + "&section=environment&page-size=3&order-by=newest&show-fields=trailText",
        // Music
        "https://content.guardianapis.com/search?api-key=" + GUARDIAN_API_KEY + "&section=music&page-size=3&order-by=newest&show-fields=trailText"
    };
    
    public List<NewsArticle> fetchNews() {
        List<NewsArticle> articles = new ArrayList<>();
        
        Log.d(TAG, "🚀 Starting multi-topic fetch");
        
        // Try each topic-specific API call
        for (int i = 0; i < API_URLS.length; i++) {
            List<NewsArticle> topicArticles = fetchTopicArticles(API_URLS[i], i);
            articles.addAll(topicArticles);
            
            if (articles.size() >= 15) break; // Limit total articles
        }
        
        // If we still don't have enough articles, use fallback
        if (articles.size() < 6) {
            Log.w(TAG, "⚠️ Few articles, adding fallback content");
            articles.addAll(getCuratedArticles());
        }
        
        Log.d(TAG, "✅ Total articles: " + articles.size());
        return articles;
    }
    
    private List<NewsArticle> fetchTopicArticles(String apiUrl, int topicIndex) {
        List<NewsArticle> articles = new ArrayList<>();
        
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            
            Log.d(TAG, "📡 Calling: " + apiUrl);
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
                
                List<NewsArticle> parsedArticles = parseGuardianResponse(response.toString(), topicIndex);
                articles.addAll(parsedArticles);
                
            } else {
                Log.e(TAG, "❌ HTTP " + responseCode + " for topic " + topicIndex);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Topic " + topicIndex + " error: " + e.getMessage());
        }
        
        return articles;
    }
    
    private List<NewsArticle> parseGuardianResponse(String jsonResponse, int topicIndex) {
        List<NewsArticle> articles = new ArrayList<>();
        String[] topics = {"world", "tech", "art & design", "environment", "music"};
        String currentTopic = topicIndex < topics.length ? topics[topicIndex] : "news";
        
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONObject response = json.getJSONObject("response");
            
            String status = response.optString("status", "error");
            if (!"ok".equals(status)) {
                return articles;
            }
            
            JSONArray results = response.getJSONArray("results");
            Log.d(TAG, "📊 Topic '" + currentTopic + "' found " + results.length() + " articles");
            
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                
                NewsArticle article = new NewsArticle();
                article.setTitle(result.getString("webTitle"));
                article.setUrl(result.getString("webUrl"));
                article.setDate(result.getString("webPublicationDate"));
                article.setSource("Guardian " + result.optString("sectionName", "News"));
                
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
                    article.setDescription("Latest from " + currentTopic);
                }
                
                articles.add(article);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Parse error for " + currentTopic + ": " + e.getMessage());
        }
        
        return articles;
    }
    
    private List<NewsArticle> getCuratedArticles() {
        List<NewsArticle> articles = new ArrayList<>();
        String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        
        // High-quality curated articles matching your topics
        String[][] curatedData = {
            {"Global Climate Efforts Show Progress", "International cooperation leads to measurable improvements in carbon reduction targets and renewable energy adoption worldwide.", "world"},
            {"AI Revolution Transforms Creative Industries", "Artificial intelligence tools are being adopted by artists and designers, creating new forms of digital expression and automation.", "tech"},
            {"Digital Art Market Sees Unprecedented Growth", "NFTs and digital galleries are reshaping how art is created, sold, and experienced in the modern era.", "art & design"},
            {"Mental Health Apps Gain Scientific Backing", "New research validates the effectiveness of digital therapy tools for anxiety, depression, and stress management.", "psychology"},
            {"Renewable Energy Costs Hit Record Lows", "Solar and wind power become more affordable than fossil fuels in most markets, accelerating clean energy transition.", "environment"},
            {"Streaming Platforms Reshape Music Discovery", "Algorithms and personalized playlists are changing how people find and connect with new artists and genres.", "music"},
            {"Urban Farming Solutions Address Food Security", "Vertical farms and community gardens provide fresh produce while reducing transportation emissions in cities.", "environment"},
            {"Virtual Reality Expands Artistic Possibilities", "Artists are using VR to create immersive experiences that challenge traditional gallery boundaries.", "art & design"},
            {"Global Tech Standards Promote Interoperability", "International agreements on technology standards facilitate innovation and cross-border collaboration.", "tech"},
            {"Mindfulness Practices Gain Corporate Adoption", "Major companies implement meditation and mindfulness programs to improve employee wellbeing and productivity.", "psychology"}
        };
        
        for (String[] data : curatedData) {
            NewsArticle article = new NewsArticle();
            article.setTitle(data[0]);
            article.setDescription(data[1]);
            article.setDate(currentDate);
            article.setSource("Tempo " + data[2]);
            article.setUrl("https://www.theguardian.com/" + data[2].replace(" & ", "-").replace(" ", "-"));
            articles.add(article);
        }
        
        return articles;
    }
}
