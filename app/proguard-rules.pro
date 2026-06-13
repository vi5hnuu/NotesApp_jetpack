# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# Keep line numbers for readable crash reports in production, hide original file names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Room entities / models (accessed via generated + reflective code) ---
-keep class com.vi5hnu.notesapp.model.** { *; }

# --- Defensive keep for the Application class ---
# Hilt, Room and Play Services Ads ship their own consumer ProGuard rules.
-keep class com.vi5hnu.notesapp.NoteApplication { *; }

# org.json is part of the Android framework — no rules needed.