# Add all the new files
git add .gitignore .github/ app/proguard-rules.pro .

# Commit the CI setup
git commit -m "Add GitHub Actions CI/CD workflow"

# Push to trigger first build!
git push origin main
