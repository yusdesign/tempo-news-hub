public class MainActivity extends AppCompatActivity {
        
    private TextView timeIcon, greetingText;
    private RecyclerView newsList;
    private DrawerLayout drawerLayout;
    private RecyclerView newsRecyclerView;
    private NewsAdapter newsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        setupViews();
        setupTimeBasedUI();
        setupIconBehavior();
        loadNews();
    }
    
    private void setupViews() {
        timeIcon = findViewById(R.id.time_icon);
        greetingText = findViewById(R.id.greeting_text);
        newsList = findViewById(R.id.news_list);
        
        // Setup RecyclerView
        newsList.setLayoutManager(new LinearLayoutManager(this));
        newsList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }
    
    private void setupTimeBasedUI() {
        int hour = LocalTime.now().getHour();
        
        String icon, greeting;
        if (hour >= 5 && hour < 12) {
            icon = "🌅"; greeting = "Morning";
        } else if (hour >= 12 && hour < 18) {
            icon = "🏙️"; greeting = "Afternoon";
        } else {
            icon = "🌃"; greeting = "Evening";
        }
        
        timeIcon.setText(icon);
        greetingText.setText(greeting);
    }
    
    private void setupIconBehavior() {
        // Icon click opens menu/sidebar
        timeIcon.setOnClickListener(v -> {
            // Simple menu for now - can upgrade to Navigation Drawer later
            showQuickMenu();
        });
        
        // Optional: Auto-minimize header after delay
        new Handler().postDelayed(() -> {
            // Could animate icon smaller if desired
            timeIcon.setTextSize(20);
            greetingText.setTextSize(16);
        }, 3000);
    }
    
    private void showQuickMenu() {
        PopupMenu menu = new PopupMenu(this, timeIcon);
        menu.getMenu().add("Guardian News");
        menu.getMenu().add("Hacker News");
        menu.getMenu().add("Settings");
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getTitle().toString()) {
                case "Guardian News":
                    loadGuardianNews();
                    return true;
                case "Hacker News":
                    loadHackerNews();
                    return true;
                default:
                    return false;
            }
        });
        menu.show();
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
        
    private void loadNews() {
        // Start with Guardian News
        loadGuardianNews();
    }
    
    private void loadGuardianNews() {
        // Placeholder - will implement Guardian API
        List<NewsItem> placeholderItems = Arrays.asList(
            new NewsItem("📰", "Guardian World News", "Latest global updates"),
            new NewsItem("🔬", "Science Breakthrough", "New discoveries today"),
            new NewsItem("🎨", "Culture & Arts", "Creative developments")
        );
        
        NewsAdapter adapter = new NewsAdapter(placeholderItems);
        newsList.setAdapter(adapter);
        
        // Update footer
        updateFooter("Guardian • " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    }
    
    private void loadHackerNews() {
        // Placeholder - will implement Hacker News
        List<NewsItem> placeholderItems = Arrays.asList(
            new NewsItem("💻", "New Programming Language", "Community discussion"),
            new NewsItem("🚀", "Startup Funding", "Tech investments"),
            new NewsItem("🤖", "AI Developments", "Latest in machine learning")
        );
        
        NewsAdapter adapter = new NewsAdapter(placeholderItems);
        newsList.setAdapter(adapter);
        
        updateFooter("Hacker News • " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    }
    
    private void updateFooter(String status) {
        TextView footerStatus = findViewById(R.id.footer_status);
        if (footerStatus != null) {
            footerStatus.setText(status);
        }
    }
}
 
