# Update NewsItem.java with multiple constructors
cat > app/src/main/java/com/tempo/newshub/NewsItem.java << 'EOF'
package com.tempo.newshub;

public class NewsItem {
    private String icon;
    private String title;
    private String description;
    private String source;
    private String time;
    private double usefulnessScore;
    
    // Full constructor
    public NewsItem(String icon, String title, String description, String source, String time, double usefulnessScore) {
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.source = source;
        this.time = time;
        this.usefulnessScore = usefulnessScore;
    }
    
    // Simplified constructor (for testing)
    public NewsItem(String icon, String title, String description, String source, String time) {
        this(icon, title, description, source, time, 0.8); // Default usefulness
    }
    
    // Minimal constructor
    public NewsItem(String icon, String title, String description) {
        this(icon, title, description, "Tempo", "Recent", 0.8);
    }
    
    // Getters
    public String getIcon() { return icon; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getSource() { return source; }
    public String getTime() { return time; }
    public double getUsefulnessScore() { return usefulnessScore; }
}
EOF
