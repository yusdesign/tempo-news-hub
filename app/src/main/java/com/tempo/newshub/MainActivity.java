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
    
    private TextView timeIcon, greetingText;
    private RecyclerView newsRecyclerView;
    private NewsAdapter newsAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        setupTimeBasedUI();
        setupNewsFeed();
    }
    
    private void setupTimeBasedUI() {
        timeIcon = findViewById(R.id.time_icon);
        greetingText = findViewById(R.id.greeting_text);
        
        // Simple time-based logic
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
        
        // Sample data - replace with your qualia-sorted data
        List<NewsItem> sampleNews = Arrays.asList(
            new NewsItem("💡", "Pattern: Simple solutions often overlooked", 
                        "The most elegant ideas hide in plain sight", "Insight", "2h ago", 0.87),
            new NewsItem("🔄", "Contradictions fuel innovation", 
                        "Opposing ideas create breakthrough friction", "Paradox", "4h ago", 0.92),
            new NewsItem("🎯", "Happiness through making", 
                        "Creation itself generates joy", "Principle", "1h ago", 0.95)
        );
        
        newsAdapter = new NewsAdapter(sampleNews);
        newsRecyclerView.setAdapter(newsAdapter);
    }
}
