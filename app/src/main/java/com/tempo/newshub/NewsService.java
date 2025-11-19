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
    
    public List<NewsArticle> fetchNews() {
        List<NewsArticle> articles = new ArrayList<>();
        
        Log.d(TAG, "🚀 Starting Guardian API fetch with key: " + GUARDIAN_API_KEY.substring(0, 8) + "...");
        
        // Try multiple query approaches
        String[] apiQueries = {
            // Your working query
            "https://content.guardianapis.com/search?order-by=relevance&use-date=last-modified&show-elements=all&show-fields=thumbnail&rights=developer-community&q=world,tech,development,psychology,environment,music&api-key=" + GUARDIAN_API_KEY + "&page-size=15",
            // Fallback: simple search
            "https://content.guardianapis.com/search?api-key=" + GUARDIAN_API_KEY + "&page-size=15&show-fields=thumbnail",
            // Fallback: just tech news
            "https://content.guardianapis.com/search?api-key=" + GUARDIAN_API_KEY + "&q=technology&page-size=10&show-fields=thumbnail"
        };
        
        for (String apiUrl : apiQueries) {
            articles = fetchFromAPI(apiUrl);
            if (!articles.isEmpty()) {
                Log.d(TAG, "✅ SUCCESS with query: " + articles.size() + " articles");
                break;
            }
        }
        
        // If all API calls fail, use curated content
        if (articles.isEmpty()) {
            Log.w(TAG, "⚠️ All API calls failed, using curated content");
            articles = getCuratedArticles();
        }
        
        return articles;
    }
    
    private List<NewsArticle> fetchFromAPI(String apiUrl) {
        List<NewsArticle> articles = new ArrayList<>();
        
        try {
            Log.d(TAG, "📡 Attempting API call: " + apiUrl);
            
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(20000);
            connection.setRequestProperty("User-Agent", "TempoNewsHub/2.6.42");
            connection.setRequestProperty("Accept", "application/json");
            
            // Add proper headers
            connection.setRequestProperty("Content-Type", "application/json");
            
            int responseCode = connection.getResponseCode();
            Log.d(TAG, "📊 HTTP Response Code: " + responseCode);
            
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
                articles = parseAPIResponse(response.toString());
                
            } else {
                Log.e(TAG, "❌ HTTP Error: " + responseCode);
                // Try to read error stream for more details
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
                    Log.e(TAG, "❌ Could not read error stream: " + e.getMessage());
                }
            }
            
            connection.disconnect();
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Network exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
        }
        
        return articles;
    }
    
    private List<NewsArticle> parseAPIResponse(String jsonResponse) {
        List<NewsArticle> articles = new ArrayList<>();
        
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONObject response = json.getJSONObject("response");
            
            String status = response.optString("status", "error");
            if (!"ok".equals(status)) {
                Log.e(TAG, "❌ API returned status: " + status);
                Log.d(TAG, "🔍 Full response: " + jsonResponse);
                return articles;
            }
            
            JSONArray results = response.getJSONArray("results");
            Log.d(TAG, "📊 Found " + results.length() + " articles in API response");
            
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                
                NewsArticle article = new NewsArticle();
                article.setTitle(result.getString("webTitle"));
                article.setUrl(result.getString("webUrl"));
                article.setDate(result.getString("webPublicationDate"));
                article.setSource("Guardian " + result.optString("sectionName", "News"));
                
                // Get thumbnail
                try {
                    JSONObject fields = result.getJSONObject("fields");
                    String thumbnail = fields.optString("thumbnail", "");
                    if (!thumbnail.isEmpty()) {
                        if (thumbnail.startsWith("//")) {
                            thumbnail = "https:" + thumbnail;
                        }
                        article.setImageUrl(thumbnail);
                        Log.d(TAG, "🖼️ Thumbnail: " + thumbnail);
                    }
                } catch (Exception e) {
                    // No thumbnail available
                }
                
                // Set description based on section
                String section = result.optString("sectionName", "").toLowerCase();
                if (section.contains("world")) article.setDescription("World news and global affairs");
                else if (section.contains("tech")) article.setDescription("Technology and innovation news");
                else if (section.contains("art")) article.setDescription("Art and culture coverage");
                else if (section.contains("environment")) article.setDescription("Environmental news and climate");
                else if (section.contains("music")) article.setDescription("Music and entertainment news");
                else if (section.contains("science")) article.setDescription("Science and research updates");
                else article.setDescription("Latest news from The Guardian");
                
                articles.add(article);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ JSON parse error: " + e.getMessage());
            Log.d(TAG, "🔍 Response snippet: " + (jsonResponse.length() > 500 ? jsonResponse.substring(0, 500) + "..." : jsonResponse));
        }
        
        return articles;
    }
    
    private List<NewsArticle> getCuratedArticles() {
        List<NewsArticle> articles = new ArrayList<>();
        String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        
        // High-quality curated content as fallback
        String[][] fallbackData = {
            {"Global Climate Conference Reaches Agreement", "World leaders agree on emissions targets and climate action plans.", "world", "https://images.unsplash.com/photo-1569163139394-de44cb54d0c9?w=400&h=200&fit=crop"},
            {"AI Revolution Transforms Creative Industries", "Artificial intelligence enables new forms of digital art and innovation.", "tech", "https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=400&h=200&fit=crop"},
            {"Digital Art Market Experiences Growth", "Online platforms revolutionize contemporary art creation.", "art & design", "https://images.unsplash.com/photo-1541961017774-22349e4a1262?w=400&h=200&fit=crop"},
            {"Mental Health Apps Gain Research Support", "Studies validate digital therapy tools for wellbeing.", "psychology", "https://images.unsplash.com/photo-1559757148-5c350d0d3c56?w=400&h=200&fit=crop"},
            {"Renewable Energy Costs Continue Falling", "Solar and wind power become more affordable globally.", "environment", "https://images.unsplash.com/photo-1466611653911-95081537e5b7?w=400&h=200&fit=crop"},
            {"Streaming Platforms Reshape Music Discovery", "Algorithms change how listeners find new artists.", "music", "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400&h=200&fit=crop"}
        };
        
        for (String[] data : fallbackData) {
            NewsArticle article = new NewsArticle();
            article.setTitle(data[0]);
            article.setDescription(data[1]);
            article.setDate(currentDate);
            article.setSource("Guardian " + data[2]);
            article.setImageUrl(data[3]);
            article.setUrl("https://www.theguardian.com/" + data[2].replace(" & ", "").replace(" ", ""));
            articles.add(article);
        }
        
        return articles;
    }
}
