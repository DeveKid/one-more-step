# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\dnyan\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard/index.html

# Keep Room related classes
-keep class androidx.room.RoomDatabase
-keep class * extends androidx.room.RoomDatabase
-keep class androidx.room.Entity
-keep class androidx.room.Dao
-keep class androidx.room.PrimaryKey
-keep class androidx.room.Insert
-keep class androidx.room.Query
-keep class androidx.room.Database
