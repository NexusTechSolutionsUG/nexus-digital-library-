package com.example.data

import java.io.Serializable

enum class UserRole {
    STUDENT, LIBRARIAN, TEACHER, ADMIN, SUPER_ADMIN
}

enum class SyncStatus {
    IDLE, SYNCING, SUCCESS, ERROR
}

data class SyncConflict(
    val id: String,
    val itemTitle: String,
    val localValue: String,
    val serverValue: String
) : Serializable

data class TeacherAssignment(
    val id: String,
    val title: String,
    val bookId: String,
    val bookTitle: String,
    val author: String,
    val dueDate: String,
    val completedCount: Int,
    val totalCount: Int,
    val instructions: String = ""
) : Serializable

enum class MaterialType {
    PDF, VIDEO, NOTES, INTERACTIVE
}

data class CMSMaterial(
    val id: String,
    val title: String,
    val type: MaterialType,
    val category: String,
    val path: String,
    val isPinned: Boolean = false,
    val fileSize: String = "1.2 MB",
    val uploadDate: String = "May 27"
) : Serializable

data class AppNotification(
    val id: Int,
    val title: String,
    val message: String,
    val time: String,
    val type: String, // e.g., "due_date", "goal", "assignment", "announcement"
    val read: Boolean = false
) : Serializable

data class ChatMessage(
    val id: String,
    val sender: String, // "user" or "gemini"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachmentPath: String? = null,
    val attachmentName: String? = null,
    val attachmentMime: String? = null
) : Serializable
