# --- GSON HARDENING ---
# Preserve generic signatures so TypeToken can function
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Prevent R8 from mangling GSON's internal types
-keep class com.google.gson.** { *; }
-keep class com.example.sakartveloguide.data.repository.RawLocationDto { *; }
-keep class com.example.sakartveloguide.data.repository.TripTemplateDto { *; }

# Keep the actual anonymous classes used for TypeTokens
-keep public class * extends com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken

# --- FIREBASE HARDENING ---
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**