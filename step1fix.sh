cd ~/tempo

# Option A: Update NewsItem.java with simpler constructors
# (Use the code above)

# Option B: Or just fix the constructor calls in MainActivity.java
# Add the missing usefulnessScore parameter (0.8, 0.9, etc.)

git add app/src/main/java/com/tempo/newshub/NewsItem.java app/src/main/java/com/tempo/newshub/MainActivity.java
git commit -m "Fix NewsItem constructor calls - add missing usefulnessScore parameter"
git push origin main
