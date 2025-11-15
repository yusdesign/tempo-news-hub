cd ~/tempo

# 1. Fix the broken XML
# (Use the complete activity_main.xml above)

# 2. Use the simple MainActivity.java
# (Use the simplified version above) 

# 3. Remove RecyclerView dependency temporarily
# (Comment out in build.gradle)

# 4. Commit and push
git add app/src/main/res/layout/activity_main.xml app/src/main/java/com/tempo/newshub/MainActivity.java app/build.gradle
git commit -m "Fix broken XML layout and simplify for stable build"
git push origin main
