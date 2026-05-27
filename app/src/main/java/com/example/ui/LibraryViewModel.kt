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

    // Search and Category Filters
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")

    // Active screen state context (e.g. for showing book details)
    val selectedBookId = MutableStateFlow<String?>(null)

    // Books stream - dynamically combined with search and category filters
    val books: StateFlow<List<Book>> = combine(
        repository.allBooks,
        searchQuery,
        selectedCategory
    ) { allBooks, query, category ->
        allBooks.filter { book ->
            val matchesCategory = category == "All" || book.category == category
            val matchesSearch = query.isBlank() || 
                    book.title.contains(query, ignoreCase = true) ||
                    book.author.contains(query, ignoreCase = true) ||
                    book.description.contains(query, ignoreCase = true)
            matchesCategory && matchesSearch
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
}
