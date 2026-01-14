# --- HILT / DAGGER ---
-keep class com.example.sakartveloguide.SakartveloApp { *; }
-keep class dagger.hilt.internal.aggregatedroot.codegen.** { *; }
-keep class dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper$1
-keep class dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper$1 { *; }

# --- MAPLIBRE ---
-keep class org.maplibre.android.** { *; }
-keep interface org.maplibre.android.** { *; }
-dontwarn org.maplibre.android.**

# --- COIL (Image Loader) ---
-keep class coil.** { *; }
-dontwarn okio.**
-dontwarn coil.**

# --- ROOM DATABASE ---
-keep class androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# --- GSON (Used in TypeConverters) ---
-keep class com.google.gson.** { *; }
-keep class com.example.sakartveloguide.domain.model.** { *; }

# --- JETPACK COMPOSE ---
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}