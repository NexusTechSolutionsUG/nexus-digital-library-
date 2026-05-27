package com.example.data

import java.io.Serializable

enum class AcademicClassLevel(val label: String, val isAdvanced: Boolean) {
    S1("Senior 1", false),
    S2("Senior 2", false),
    S3("Senior 3", false),
    S4("Senior 4", false),
    S5("Senior 5", true),
    S6("Senior 6", true)
}

data class TeacherProfile(
    val id: String,
    val name: String,
    val imageUrl: String, // coil loading
    val subjectName: String,
    val uploadedCount: Int,
    val activePollsCount: Int,
    val email: String
) : Serializable

enum class ResourceCategory(val label: String) {
    NOTES("Teacher Notes"),
    PDFS("PDFs & Shell Docs"),
    TEXTBOOKS("Textbooks"),
    VIDEOS("Recorded Lessons"),
    ASSIGNMENTS("Assignments"),
    PAST_PAPERS("Past Papers"),
    MARKING_GUIDES("Marking Guides"),
    REVISION_QUESTIONS("Revision Questions"),
    AI_SUMMARIES("AI-Generated Summaries"),
    FLASHCARDS("Flashcards"),
    DISCUSSION_THREADS("Discussion Threads"),
    AUDIO_LESSONS("Audio Lessons"),
    DIAGRAMS("Images & Diagrams")
}

data class AcademicResource(
    val id: String,
    val title: String,
    val category: ResourceCategory,
    val sizeLabel: String = "2.2 MB",
    val contentSnippet: String = "",
    val virtualUrl: String = "",
    val authorTeacher: String = "Admin",
    val durationSeconds: Int = 0 // for video/audio playbacks
) : Serializable

data class Subject(
    val id: String,
    val name: String,
    val classLevel: AcademicClassLevel,
    val teachers: List<TeacherProfile>,
    val resources: List<AcademicResource>,
    val description: String = "Oakridge high compliance subject"
) : Serializable

// Flashcard Interactive
data class StudyFlashcard(
    val id: String,
    val question: String,
    val answer: String,
    var isKnown: Boolean = false
) : Serializable

// Discussion Forums
data class ForumPost(
    val id: String,
    val userName: String,
    val userRole: String, // "STUDENT" or "TEACHER"
    val avatarColorHex: String,
    val messageText: String,
    val minutesAgo: Int
) : Serializable

// Revision QA
data class RevisionQuestion(
    val id: String,
    val questionText: String,
    val optionsList: List<String>,
    val correctIndex: Int,
    val rationale: String
) : Serializable

// Personal Planner Countdowns
data class ExamCountdown(
    val id: String,
    val title: String,
    val examDate: String,
    val daysRemaining: Int
) : Serializable

data class StudyPlannerItem(
    val id: String,
    val subjectName: String,
    val topicToCover: String,
    val targetMinutes: Int,
    val scheduledDay: String,
    var isDone: Boolean = false
) : Serializable

// Analytics tracker model
data class AcademicUsageMetric(
    val subjectName: String,
    val studyDurationMinutes: Int,
    val completeAssignmentsRatio: Float,
    val resourcesOpenedCount: Int,
    val lastAccessedTimestamp: Long
) : Serializable
