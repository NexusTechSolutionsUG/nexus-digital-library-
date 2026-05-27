package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = LibraryDatabase.getDatabase(application)
    val repository = LibraryRepository(db.libraryDao)

    // User session state
    val studentName = MutableStateFlow("Alex Rivera (Grade 11)")
    val studentId = MutableStateFlow("StudentID-2026-HSL")
    val readingStreak = MutableStateFlow(5) // Default academic reading streak

    // Multi-role State
    val currentRole = MutableStateFlow(UserRole.STUDENT)

    // Optional Cloud Sync state (WorkManager queues, conflicts, offline indicators)
    val cloudSyncEnabled = MutableStateFlow(false)
    val syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncQueueCount = MutableStateFlow(0)
    val syncConflicts = MutableStateFlow<List<SyncConflict>>(emptyList())
    val downloadCacheDownloaded = MutableStateFlow<Set<String>>(emptySet()) // Store local book IDs available offline

    // AI Chat state
    val chatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("init", "gemini", "Hello! I am **Auden**, your AI High School Assistant. How would you like to explore literature today? You can ask me to summarize, recommend, generate quizzes or flashcards!")
    ))
    val aiIsTyping = MutableStateFlow(false)

    // CMS Materials upload state
    val cmsMaterials = MutableStateFlow<List<CMSMaterial>>(listOf(
        CMSMaterial("m1", "Hamlet Lecture Notes.pdf", MaterialType.PDF, "Drama", "lectures/hamlet.pdf", true, "1.4 MB", "May 25"),
        CMSMaterial("m2", "Advanced Physics SparkNotes.pdf", MaterialType.PDF, "Science", "notes/physics.pdf", false, "980 KB", "May 26"),
        CMSMaterial("m3", "Macbeth Cinematic Trailer.mp4", MaterialType.VIDEO, "Drama", "videos/macbeth.mp4", true, "12.8 MB", "May 27")
    ))

    // Teacher Assignments tracker
    val teacherAssignments = MutableStateFlow<List<TeacherAssignment>>(listOf(
        TeacherAssignment("asg1", "Complete Act I Reading Quiz", "1", "To Kill a Mockingbird", "Harper Lee", "May 30", 12, 15, "Read chapters 1 to 4 and complete the practice quiz."),
        TeacherAssignment("asg2", "Analyze Rhetoric in Gatsby", "2", "The Great Gatsby", "F. Scott Fitzgerald", "June 02", 5, 15, "Summarize Daisy's characterization and write a 100-word review.")
    ))

    // Push/Local Notification inbox
    val appNotifications = MutableStateFlow<List<AppNotification>>(listOf(
        AppNotification(1, "Due Date Tomorrow!", "Macbeth is due tomorrow. Complete reading and return virtually.", "1h ago", "due_date"),
        AppNotification(2, "New Assignment Assigned", "Mr. Harrison assigned 'Complete Act I Reading Quiz'.", "3h ago", "assignment"),
        AppNotification(3, "Daily Streak Goal", "Keep your 5-day reading streak alive! Open any companion book to play.", "5h ago", "goal")
    ))

    // Advanced search variables
    val searchSortOrder = MutableStateFlow("Title") // Options: "Title", "Rating", "Year"
    val searchFilterAvailability = MutableStateFlow(false) // Toggle to show only available copies
    val voiceInputMockActive = MutableStateFlow(false)
    val ocrMockExtractedText = MutableStateFlow<String?>(null)

    // Search and Category Filters
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")

    // Active screen state context (e.g. for showing book details)
    val selectedBookId = MutableStateFlow<String?>(null)

    // Books stream - dynamically combined with search, category, sort order, and copy availability
    val books: StateFlow<List<Book>> = combine(
        repository.allBooks,
        searchQuery,
        selectedCategory,
        searchSortOrder,
        searchFilterAvailability
    ) { allBooks, query, category, sort, availOnly ->
        allBooks.filter { book ->
            val matchesCategory = category == "All" || book.category == category
            val matchesSearch = query.isBlank() || 
                    book.title.contains(query, ignoreCase = true) ||
                    book.author.contains(query, ignoreCase = true) ||
                    book.description.contains(query, ignoreCase = true)
            val matchesAvail = !availOnly || book.availableCopies > 0
            matchesCategory && matchesSearch && matchesAvail
        }.sortedWith { b1, b2 ->
            when (sort) {
                "Rating" -> b2.rating.compareTo(b1.rating)
                "Year" -> b2.publishedYear.compareTo(b1.publishedYear)
                else -> b1.title.compareTo(b2.title, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Get selected book details
    val selectedBook: StateFlow<Book?> = selectedBookId.flatMapLatest { id ->
        if (id != null) repository.getBookById(id) else flowOf(null)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Get reviews for selected book
    val selectedBookReviews: StateFlow<List<BookReview>> = selectedBookId.flatMapLatest { id ->
        if (id != null) repository.getReviewsForBook(id) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Borrow Records Flow
    val allBorrowRecords: StateFlow<List<BorrowRecord>> = repository.allBorrowRecords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val activeBorrowRecords: StateFlow<List<BorrowRecord>> = repository.activeBorrowRecords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Announcements Flow
    val announcements: StateFlow<List<Announcement>> = repository.allAnnouncements.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // User Actions
    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun selectCategory(category: String) {
        selectedCategory.value = category
    }

    fun selectBook(bookId: String?) {
        selectedBookId.value = bookId
    }

    fun toggleFavorite(bookId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(bookId, isFavorite)
        }
    }

    fun borrowSelectedBook(book: Book, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            if (book.availableCopies <= 0) {
                onError("No available copies of this physical book right now.")
                return@launch
            }
            
            // Check if already borrowed and not returned
            val alreadyBorrowed = activeBorrowRecords.value.any { it.bookId == book.id }
            if (alreadyBorrowed) {
                onError("You are already borrowing this book.")
                return@launch
            }

            val success = repository.borrowBook(book, studentName.value)
            if (success) {
                onSuccess()
            } else {
                onError("An error occurred during borrowing.")
            }
        }
    }

    fun returnBookRecord(record: BorrowRecord) {
        viewModelScope.launch {
            repository.returnBook(record)
        }
    }

    fun updateRecordProgress(record: BorrowRecord, progress: Int) {
        viewModelScope.launch {
            repository.updateReadingProgress(record, progress)
            // If they reach 100%, increment reading streak as encouragement!
            if (progress == 100 && record.readingProgress < 100) {
                readingStreak.value += 1
            }
        }
    }

    fun addStudentReview(bookId: String, rating: Int, reviewText: String) {
        viewModelScope.launch {
            repository.addReview(bookId, studentName.value, rating, reviewText)
        }
    }

    fun postNewAnnouncement(title: String, content: String, isPinned: Boolean) {
        viewModelScope.launch {
            repository.addAnnouncement(title, content, isPinned)
        }
    }

    fun updateStudentProfile(name: String, id: String) {
        if (name.isNotBlank()) studentName.value = name
        if (id.isNotBlank()) studentId.value = id
    }

    // --- NEW VIEWER FLOWS & ACTIONS ---
    val activeViewerBookId = MutableStateFlow<String?>(null)

    val activeViewingBook: StateFlow<Book?> = activeViewerBookId.flatMapLatest { id ->
        if (id != null) repository.getBookById(id) else flowOf(null)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val activeBookProgress: StateFlow<ReadingProgress?> = activeViewerBookId.flatMapLatest { id ->
        if (id != null) repository.getReadingProgressFlow(id) else flowOf(null)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val bookmarksForActiveBook: StateFlow<List<BookBookmark>> = activeViewerBookId.flatMapLatest { id ->
        if (id != null) repository.getBookmarksForBook(id) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val highlightsForActiveBook: StateFlow<List<BookHighlight>> = activeViewerBookId.flatMapLatest { id ->
        if (id != null) repository.getHighlightsForBook(id) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val annotationsForActiveBook: StateFlow<List<BookAnnotation>> = activeViewerBookId.flatMapLatest { id ->
        if (id != null) repository.getAnnotationsForBook(id) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun openBookInViewer(bookId: String) {
        activeViewerBookId.value = bookId
    }

    fun closeBookViewer() {
        activeViewerBookId.value = null
    }

    fun savePageProgress(bookId: String, page: Int, totalPages: Int) {
        viewModelScope.launch {
            val progress = ReadingProgress(
                bookId = bookId,
                lastPage = page,
                totalPages = totalPages,
                lastReadTime = System.currentTimeMillis()
            )
            repository.saveReadingProgress(progress)
            
            // Sync with physical borrow record if exists
            val activeRecords = activeBorrowRecords.value
            val currentRecord = activeRecords.find { it.bookId == bookId }
            if (currentRecord != null) {
                val percentage = ((page.toFloat() / totalPages) * 100).toInt().coerceIn(0, 100)
                updateRecordProgress(currentRecord, percentage)
            }
        }
    }

    fun addBookmark(bookId: String, page: Int, note: String) {
        viewModelScope.launch {
            repository.addBookmark(BookBookmark(bookId = bookId, page = page, note = note))
        }
    }

    fun deleteBookmark(id: Int) {
        viewModelScope.launch {
            repository.deleteBookmark(id)
        }
    }

    fun addHighlight(bookId: String, page: Int, text: String, colorHex: String) {
        viewModelScope.launch {
            repository.addHighlight(BookHighlight(bookId = bookId, page = page, text = text, colorHex = colorHex))
        }
    }

    fun deleteHighlight(id: Int) {
        viewModelScope.launch {
            repository.deleteHighlight(id)
        }
    }

    fun addAnnotation(bookId: String, page: Int, strokesJson: String, typedNote: String) {
        viewModelScope.launch {
            repository.addAnnotation(BookAnnotation(bookId = bookId, page = page, drawStrokesJson = strokesJson, typedNote = typedNote))
        }
    }

    fun deleteAnnotation(id: Int) {
        viewModelScope.launch {
            repository.deleteAnnotation(id)
        }
    }

    // Role Manager Actions
    fun changeRole(role: UserRole) {
        currentRole.value = role
        addNotification(
            "Security: Role Shifted",
            "Authenticated successfully as ${role.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }}.",
            "announcement"
        )
    }

    // Cloud Synchronization Actions
    fun toggleCloudSync(enabled: Boolean) {
        cloudSyncEnabled.value = enabled
        if (enabled) {
            triggerManualSync()
        } else {
            syncStatus.value = SyncStatus.IDLE
        }
    }

    fun triggerManualSync() {
        if (!cloudSyncEnabled.value) return
        viewModelScope.launch {
            syncStatus.value = SyncStatus.SYNCING
            syncQueueCount.value = 3
            kotlinx.coroutines.delay(2000) // Visual simulation of WorkManager queue execution

            // Create a mock conflict for interactive resolution
            val conflicts = listOf(
                SyncConflict(
                    id = "conf_1",
                    itemTitle = "Reading Progress: To Kill a Mockingbird",
                    localValue = "Page 15 (Read 30% locally)",
                    serverValue = "Page 22 (Read 44% on Server)"
                )
            )
            syncConflicts.value = conflicts
            syncQueueCount.value = 0
            syncStatus.value = SyncStatus.SUCCESS
            
            addNotification(
                "Sync Complete",
                "WorkManager finished sync queue. ${conflicts.size} manual conflict detected.",
                "goal"
            )
        }
    }

    fun resolveConflict(conflictId: String, chooseLocal: Boolean) {
        viewModelScope.launch {
            val conflicts = syncConflicts.value.toMutableList()
            val conflict = conflicts.find { it.id == conflictId }
            if (conflict != null) {
                conflicts.remove(conflict)
                syncConflicts.value = conflicts
                if (!chooseLocal) {
                    // Say Server wins: update local progress
                    savePageProgress("1", 22, 50)
                }
                addNotification(
                    "Conflict Resolved",
                    "Resolved conflict for ${conflict.itemTitle} using ${if (chooseLocal) "Local" else "Cloud Server"} state.",
                    "goal"
                )
            }
        }
    }

    fun toggleOfflineDownload(bookId: String) {
        val currentSet = downloadCacheDownloaded.value.toMutableSet()
        if (currentSet.contains(bookId)) {
            currentSet.remove(bookId)
            addNotification("Cache Removed", "Book items deleted from local offline storage.", "goal")
        } else {
            currentSet.add(bookId)
            addNotification("Downloaded", "Book & companion materials cached for 100% offline access.", "goal")
        }
        downloadCacheDownloaded.value = currentSet
    }

    // AI Chat Actions
    fun sendChatMessage(text: String, attachmentName: String? = null, attachmentMime: String? = null) {
        if (text.isBlank() && attachmentName == null) return
        viewModelScope.launch {
            val userMsg = ChatMessage(
                id = java.util.UUID.randomUUID().toString(),
                sender = "user",
                content = text,
                attachmentName = attachmentName,
                attachmentMime = attachmentMime
            )
            chatMessages.value = chatMessages.value + userMsg
            aiIsTyping.value = true

            // Send to Gemini Rest Service
            val responseText = GeminiApiService.generateContent(chatMessages.value)
            
            val aiMsg = ChatMessage(
                id = java.util.UUID.randomUUID().toString(),
                sender = "gemini",
                content = responseText
            )
            chatMessages.value = chatMessages.value + aiMsg
            aiIsTyping.value = false
        }
    }

    fun selectQueryFromAI(prompt: String) {
        sendChatMessage(prompt)
    }

    fun clearChat() {
        chatMessages.value = listOf(
            ChatMessage("init", "gemini", "Chat reset! Let me know if you need summaries, flashcards, or interactive homework quizzes.")
        )
    }

    // CMS Actions
    fun uploadMaterial(title: String, type: MaterialType, category: String, isPinned: Boolean) {
        val newMat = CMSMaterial(
            id = "m_${System.currentTimeMillis()}",
            title = title,
            type = type,
            category = category,
            path = "uploads/${title.lowercase().replace(" ", "_")}",
            isPinned = isPinned,
            fileSize = "2.4 MB"
        )
        cmsMaterials.value = listOf(newMat) + cmsMaterials.value
        addNotification("Resource Uploaded", "Successfully added '$title' to the campus media folders.", "announcement")
    }

    fun deleteMaterial(id: String) {
        cmsMaterials.value = cmsMaterials.value.filter { it.id != id }
    }

    // Teacher Assignment Actions
    fun createAssignment(title: String, bookId: String, bookTitle: String, author: String, dueDate: String, instructions: String) {
        val newAsg = TeacherAssignment(
            id = "asg_${System.currentTimeMillis()}",
            title = title,
            bookId = bookId,
            bookTitle = bookTitle,
            author = author,
            dueDate = dueDate,
            completedCount = 0,
            totalCount = 15,
            instructions = instructions
        )
        teacherAssignments.value = listOf(newAsg) + teacherAssignments.value
        addNotification("New Reading Assignment", "Class Assignment: '$title' assigned in $bookTitle.", "assignment")
    }

    // Notifications Inbox Actions
    fun addNotification(title: String, message: String, type: String) {
        val newNotif = AppNotification(
            id = (System.currentTimeMillis() % 100000).toInt(),
            title = title,
            message = message,
            time = "Just now",
            type = type
        )
        appNotifications.value = listOf(newNotif) + appNotifications.value
    }

    fun markAllNotificationsRead() {
        appNotifications.value = appNotifications.value.map { it.copy(read = true) }
    }

    fun clearNotifications() {
        appNotifications.value = emptyList()
    }
}
