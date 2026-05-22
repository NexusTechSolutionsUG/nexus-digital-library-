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
