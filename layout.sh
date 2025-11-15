# Create layout directory
mkdir -p app/src/main/res/layout

# Create activity_main.xml
cat > app/src/main/res/layout/activity_main.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="24dp"
    android:gravity="center"
    android:background="#F8F9FA">

    <!-- Time Icon -->
    <TextView
        android:id="@+id/time_icon"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textSize="72sp"
        android:text="🎯"
        android:layout_marginBottom="16dp" />

    <!-- Greeting -->
    <TextView
        android:id="@+id/greeting_text"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textSize="28sp"
        android:text="Hello Builder!"
        android:textStyle="bold"
        android:textColor="#2D3748" />

    <!-- App Name -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Tempo News Hub"
        android:textSize="18sp"
        android:textColor="#718096"
        android:layout_marginTop="8dp" />

    <!-- Status -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="📱 Layout Found & Loaded!"
        android:textSize="14sp"
        android:textColor="#4299E1"
        android:layout_marginTop="32dp" />

    <!-- Next Steps -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Next: Qualia-aware news feed"
        android:textSize="12sp"
        android:textColor="#A0AEC0"
        android:layout_marginTop="16dp" />

</LinearLayout>
EOF
