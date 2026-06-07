package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Book::class, 
        BorrowRecord::class, 
        BookReview::class, 
        Announcement::class,
        ReadingProgress::class,
        BookBookmark::class,
        BookHighlight::class,
        BookAnnotation::class
    ],
    version = 2,
    exportSchema = true
)
abstract class LibraryDatabase : RoomDatabase() {
    abstract val libraryDao: LibraryDao

    companion object {
        @Volatile
        private var INSTANCE: LibraryDatabase? = null

        fun getDatabase(context: Context): LibraryDatabase {
            return INSTANCE ?: synchronized(this) {
                val isDemoEnabled = try {
                    com.example.BuildConfig.DEBUG && (com.example.BuildConfig.DEMO_AUTH_ENABLED.toBoolean() || com.example.BuildConfig.DEMO_AUTH_ENABLED == "true")
                } catch (e: Exception) {
                    false
                }

                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    LibraryDatabase::class.java,
                    "high_school_library_db"
                )

                if (isDemoEnabled) {
                    // Only allow destructive migration in non-production, debug/demo modes
                    builder.fallbackToDestructiveMigration()
                } else {
                    // Production: fail closed instead of silently wiping database during schema changes!
                    // TODO: Explicit migrations must be defined and added here in production, e.g.:
                    // builder.addMigrations(MIGRATION_1_2)
                }

                val instance = builder.build()
                INSTANCE = instance
                instance
            }
        }
    }
}
