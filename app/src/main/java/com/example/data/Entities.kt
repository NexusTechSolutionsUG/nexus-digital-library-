package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String, // e.g. student ID, admin code, etc.
    val name: String,
    val password: String,
    val role: String, // STUDENT, TEACHER, LIBRARIAN
    val schoolClass: String, // e.g. "Senior 4", "Staff"
    val streak: Int = 3,
    val isApproved: Boolean = true
)

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val author: String,
    val category: String, // Mathematics, Physics, Literature, History, Past Papers
    val copies: Int,
    val available: Int,
    val pdfUrl: String = "", // empty if print-only, some filled for Digital Reading
    val shelfLocation: String = "Shelf A-1",
    val description: String = "",
    val coverColorHex: String = "#D0BCFF" // Fallback aesthetic block representations
)

@Entity(tableName = "borrow_records")
data class BorrowRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: String,
    val studentName: String,
    val bookId: Int,
    val bookTitle: String,
    val borrowDate: String, // "YYYY-MM-DD" style
    val returnDate: String, // "YYYY-MM-DD" style
    val status: String // PENDING, APPROVED, RETURNED, OVERDUE
)

@Entity(tableName = "digital_materials")
data class DigitalMaterial(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // Mathematics, Physics, Past papers, etc.
    val type: String, // PDF, Notes, Past paper, Magazine
    val fileUrl: String = "",
    val description: String = "",
    val dateAdded: String = "2026-05-22"
)

@Entity(tableName = "announcements")
data class Announcement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: String,
    val isPinned: Boolean = false
)
