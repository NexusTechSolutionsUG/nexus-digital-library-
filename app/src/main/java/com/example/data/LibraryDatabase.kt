package com.example.data

import android.content.Context
import com.example.BuildConfig
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
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    LibraryDatabase::class.java,
                    "high_school_library_db"
                )
                if (BuildConfig.DEBUG && BuildConfig.DEMO_AUTH_ENABLED.equals("true", ignoreCase = true)) {
                    builder.fallbackToDestructiveMigration(false)
                }
                val instance = builder.build()
                INSTANCE = instance
                instance
            }
        }
    }
}
