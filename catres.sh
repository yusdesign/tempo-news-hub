# Create strings.xml
mkdir -p app/src/main/res/values
cat > app/src/main/res/values/strings.xml << 'EOF'
<resources>
    <string name="app_name">Tempo News Hub</string>
</resources>
EOF

# Create basic mipmap icon (required)
mkdir -p app/src/main/res/mipmap-anydpi-v26
cat > app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background"/>
    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
</adaptive-icon>
EOF

# Create basic colors
cat > app/src/main/res/values/colors.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="ic_launcher_background">#FFFFFF</color>
</resources>
EOF

# Create basic foreground icon
mkdir -p app/src/main/res/drawable
cat > app/src/main/res/drawable/ic_launcher_foreground.xml << 'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#3DDC84"
        android:pathData="M0,0h108v108h-108z"/>
    <path
        android:fillColor="#00000000"
        android:pathData="M0,0h108v108h-108z"/>
</vector>
EOF
