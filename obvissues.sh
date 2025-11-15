cd ~/tempo

# Verify all files exist
ls -la app/src/main/res/layout/activity_main.xml
ls -la app/src/main/java/com/tempo/newshub/MainActivity.java

# Check for syntax errors
grep -n "findViewById" app/src/main/java/com/tempo/newshub/MainActivity.java
