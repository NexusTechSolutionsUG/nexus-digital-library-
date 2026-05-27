package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "books")
data class Book(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val category: String,
    val description: String,
    val publishedYear: Int,
    val totalCopies: Int,
    val availableCopies: Int,
    val rating: Float,
    val coverUrl: String,
    val isFavorite: Boolean = false
) : Serializable

@Entity(tableName = "borrow_records")
data class BorrowRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookId: String,
    val bookTitle: String,
    val author: String,
    val borrowDate: Long,
    val dueDate: Long,
    val returnDate: Long? = null,
    val readingProgress: Int = 0 // 0 to 100
) : Serializable

@Entity(tableName = "book_reviews")
data class BookReview(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookId: String,
    val studentName: String,
    val rating: Int, // 1 to 5 stars
    val reviewText: String,
    val timestamp: Long
) : Serializable

@Entity(tableName = "announcements")
data class Announcement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val date: String,
    val isPinned: Boolean = false
) : Serializable

// --- NEW VIEWER ENTITIES ---

@Entity(tableName = "reading_progress")
data class ReadingProgress(
    @PrimaryKey val bookId: String,
    val lastPage: Int,
    val totalPages: Int = 50,
    val scrollOffset: Float = 0f,
    val lastReadTime: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "book_bookmarks")
data class BookBookmark(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookId: String,
    val page: Int,
    val note: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "book_highlights")
data class BookHighlight(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookId: String,
    val page: Int,
    val text: String,
    val colorHex: String, // e.g., "#FFEB3B" (Yellow), "#8BC34A" (Green), etc.
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "book_annotations")
data class BookAnnotation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookId: String,
    val page: Int,
    val drawStrokesJson: String, // SVG or custom JSON strokes listing
    val typedNote: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

