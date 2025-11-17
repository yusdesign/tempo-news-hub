// app/src/main/java/com/tempo/newshub/NewsAdapter.java
package com.tempo.newshub;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    
    private List<NewsItem> newsItems;
    
    public NewsAdapter(List<NewsItem> newsItems) {
        this.newsItems = newsItems;
    }
    
    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_item, parent, false);
        return new NewsViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem item = newsItems.get(position);
        holder.newsIcon.setText(item.getIcon());
        holder.newsTitle.setText(item.getTitle());
        holder.newsDescription.setText(item.getDescription());
        holder.newsSource.setText(item.getSource());
        holder.newsTime.setText(item.getTime());
    }
    
    @Override
    public int getItemCount() {
        return newsItems.size();
    }
    
    static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView newsIcon, newsTitle, newsDescription, newsSource, newsTime;
        
        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            newsIcon = itemView.findViewById(R.id.news_icon);
            newsTitle = itemView.findViewById(R.id.news_title);
            newsDescription = itemView.findViewById(R.id.news_description);
            newsSource = itemView.findViewById(R.id.news_source);
            newsTime = itemView.findViewById(R.id.news_time);
        }
    }
}
