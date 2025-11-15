cat > app/src/main/java/com/tempo/newshub/NewsItem.java << 'EOF'
package com.tempo.newshub;

public class NewsItem {
    private String icon;
    private String title;
    private String description;
    private String source;
    private String time;
    private double usefulnessScore;
    
    public NewsItem(String icon, String title, String description, String source, String time, double usefulnessScore) {
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.source = source;
        this.time = time;
        this.usefulnessScore = usefulnessScore;
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
