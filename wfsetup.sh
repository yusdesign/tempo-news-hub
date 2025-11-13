# Create workflows directory
mkdir -p .github/workflows

# Create Android build workflow
cat > .github/workflows/android-build.yml << 'EOF'
name: Android CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'

    - name: Setup Android SDK
      uses: android-actions/setup-android@v3

    - name: Build with Gradle
      run: |
        chmod +x gradlew
        ./gradlew assembleDebug

    - name: Upload APK
      uses: actions/upload-artifact@v4
      with:
        name: tempo-news-app
        path: app/build/outputs/apk/debug/app-debug.apk
        retention-days: 7

    - name: Upload build reports
      uses: actions/upload-artifact@v4
      with:
        name: build-reports
        path: app/build/reports/
        retention-days: 7
EOF
