// Update MainActivity.java
package com.tempo.newshub;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    
    private RecyclerView newsRecyclerView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        setupTimeBasedUI();
        setupNewsFeed();
    }
    
    private void setupTimeBasedUI() {
        TextView timeIcon = findViewById(R.id.time_icon);
        TextView greetingText = findViewById(R.id.greeting_text);
        
        int hour = LocalTime.now().getHour();
        
        if (hour >= 5 && hour < 12) {
            greetingText.setText("Good Morning");
            timeIcon.setText("🌅");
        } else if (hour >= 12 && hour < 18) {
            greetingText.setText("Good Afternoon"); 
            timeIcon.setText("🏙️");
        } else {
            greetingText.setText("Good Evening");
            timeIcon.setText("🌃");
        }
    }
    
    private void setupNewsFeed() {
        newsRecyclerView = findViewById(R.id.news_recycler_view);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<NewsItem> sampleNews = Arrays.asList(
            new NewsItem("📰", "Welcome to Tempo News", 
                    "Your qualia-sorted news hub is running!", "System", "Now", 0.95),
            new NewsItem("💡", "Step 1 Complete", 
                    "Stable app foundation achieved", "Progress", "Now", 0.90),
            new NewsItem("🎯", "Next: RSS Feeds", 
                    "Time-aware news aggregation coming soon", "Roadmap", "Now", 0.85),
            new NewsItem("🌅", "Time-Aware Design", 
                    "Content adapts to your daily rhythm", "Feature", "Now", 0.88),
            new NewsItem("🧠", "Qualia Engine", 
                    "Smart filtering for meaningful content", "Core", "Now", 0.92)
        );
                
        NewsAdapter newsAdapter = new NewsAdapter(sampleNews);
        newsRecyclerView.setAdapter(newsAdapter);
    }
}
