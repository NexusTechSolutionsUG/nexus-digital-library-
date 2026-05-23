package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

enum class Screen {
    LOGIN,
    DASHBOARD,
    BOOK_DETAILS,
    DIGITAL_READER,
    QR_PASSPORT,
    NOTIFICATIONS,
    AI_STUDY,
    ADMIN_DASHBOARD,
    ADMIN_BOOKS,
    ADMIN_BORROWS,
    ADMIN_ANNOUNCEMENTS
}

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = LibraryDatabase.getDatabase(application)
    private val repository = LibraryRepository(db.libraryDao())

    // --- Screen State ---
    private val _currentScreen = MutableStateFlow(Screen.LOGIN)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // --- Current User State ---
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // --- Selected Item State ---
    private val _selectedBook = MutableStateFlow<Book?>(null)
    val selectedBook: StateFlow<Book?> = _selectedBook.asStateFlow()

    fun selectBook(book: Book?) {
        _selectedBook.value = book
        if (book != null) navigateTo(Screen.BOOK_DETAILS)
    }

    private val _selectedMaterial = MutableStateFlow<DigitalMaterial?>(null)
    val selectedMaterial: StateFlow<DigitalMaterial?> = _selectedMaterial.asStateFlow()

    fun selectMaterial(material: DigitalMaterial?) {
        _selectedMaterial.value = material
        if (material != null) navigateTo(Screen.DIGITAL_READER)
    }

    // --- Database Flows ---
    val allBooks = repository.allBooks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allBorrowRecords = repository.allBorrowRecords.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allDigitalMaterials = repository.allDigitalMaterials.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allAnnouncements = repository.allAnnouncements.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allUsers = repository.allUsers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Catalog Filter States ---
    val searchQuery = MutableStateFlow("")
    val selectedSubject = MutableStateFlow("All") // Subject Filter
    val selectedAvailability = MutableStateFlow("All") // Available, Borrowed, All

    // Observed filtered catalog
    val filteredBooks = combine(allBooks, searchQuery, selectedSubject, selectedAvailability) { books, query, subject, availability ->
        books.filter { book ->
            val matchesQuery = book.title.contains(query, ignoreCase = true) || book.author.contains(query, ignoreCase = true)
            val matchesSubject = subject == "All" || book.category.equals(subject, ignoreCase = true)
            val matchesAvailability = when (availability) {
                "Available" -> book.available > 0
                "Checked Out" -> book.available == 0
                else -> true
            }
            matchesQuery && matchesSubject && matchesAvailability
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Stats calculation (Admin Dashboard) ---
    val totalBooksCount = allBooks.map { it.sumOf { b -> b.copies } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val totalAvailableBooksCount = allBooks.map { it.sumOf { b -> b.available } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val activeLoansCount = allBorrowRecords.map { it.count { r -> r.status == "APPROVED" || r.status == "OVERDUE" } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val pendingLoansCount = allBorrowRecords.map { it.count { r -> r.status == "PENDING" } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val overdueLoansCount = allBorrowRecords.map { it.count { r -> r.status == "OVERDUE" } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- Dynamic User Borrow Records ---
    val studentBorrowRecords = currentUser.flatMapLatest { user ->
        if (user != null) {
            repository.getBorrowRecordsForStudent(user.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Gemini AI Tutor State ---
    private val _aiResponse = MutableStateFlow("")
    val aiResponse: StateFlow<String> = _aiResponse.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiHistory = MutableStateFlow<List<Pair<String, Boolean>>>(emptyList()) // Pair<Message, isUser>
    val aiHistory: StateFlow<List<Pair<String, Boolean>>> = _aiHistory.asStateFlow()

    // --- Notification Stack ---
    private val _sysNotifications = MutableStateFlow<List<String>>(emptyList())
    val sysNotifications: StateFlow<List<String>> = _sysNotifications.asStateFlow()

    init {
        viewModelScope.launch {
            repository.preseedIfEmpty()
            addNotification("Welcome to High School Library offline system. Synchronized with Cloud services.")
        }
    }

    fun addNotification(message: String) {
        _sysNotifications.value = listOf(message) + _sysNotifications.value
    }

    // --- Authentication Actions ---
    fun login(idInput: String, passwordInput: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserById(idInput.trim())
            if (user != null && user.password == passwordInput) {
                _currentUser.value = user
                if (user.role == "LIBRARIAN") {
                    navigateTo(Screen.ADMIN_DASHBOARD)
                } else {
                    navigateTo(Screen.DASHBOARD)
                }
                onResult(true, "Authentication successful")
                addNotification("${user.name} logged in successfully as ${user.role}.")
            } else {
                onResult(false, "Invalid credentials or missing account")
            }
        }
    }

    fun register(idInput: String, nameInput: String, classInput: String, roleInput: String, passwordInput: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (idInput.isBlank() || nameInput.isBlank() || passwordInput.isBlank()) {
                onResult(false, "Please fill in all inputs")
                return@launch
            }
            val existing = repository.getUserById(idInput.trim())
            if (existing != null) {
                onResult(false, "ID Already exists. Use a unique registration index.")
                return@launch
            }
            val newUser = User(
                id = idInput.trim(),
                name = nameInput.trim(),
                password = passwordInput,
                role = roleInput,
                schoolClass = classInput.trim(),
                streak = if (roleInput == "STUDENT") 1 else 0
            )
            repository.insertUser(newUser)
            _currentUser.value = newUser
            addNotification("New account registered for ${newUser.name} with index ID: ${newUser.id}")
            if (roleInput == "LIBRARIAN") {
                navigateTo(Screen.ADMIN_DASHBOARD)
            } else {
                navigateTo(Screen.DASHBOARD)
            }
            onResult(true, "Registration completed")
        }
    }

    fun logout() {
        _currentUser.value = null
        _selectedBook.value = null
        _selectedMaterial.value = null
        _aiResponse.value = ""
        _aiHistory.value = emptyList()
        navigateTo(Screen.LOGIN)
    }

    // --- Book Loan Actions ---
    fun requestBookBorrow(book: Book) {
        val user = _currentUser.value ?: return
        if (book.available <= 0) {
            addNotification("Borrow requested failed: No copies of '${book.title}' available currently.")
            return
        }
        viewModelScope.launch {
            val newRecord = BorrowRecord(
                studentId = user.id,
                studentName = user.name,
                bookId = book.id,
                bookTitle = book.title,
                borrowDate = "2026-05-23",
                returnDate = "2026-06-06", // 14 days later
                status = "PENDING"
            )
            repository.insertBorrowRecord(newRecord)
            addNotification("Borrow request for '${book.title}' submitted. Awaiting Librarian Approval.")
        }
    }

    // --- Librarian Admin Actions ---
    fun approveBorrow(record: BorrowRecord) {
        viewModelScope.launch {
            // Check book availability and update copies
            val bookFlow = repository.allBooks.firstOrNull() ?: emptyList()
            val targetBook = bookFlow.find { it.id == record.bookId }
            if (targetBook != null && targetBook.available > 0) {
                val updatedBook = targetBook.copy(available = targetBook.available - 1)
                repository.updateBook(updatedBook)

                val updatedRecord = record.copy(status = "APPROVED")
                repository.updateBorrowRecord(updatedRecord)
                addNotification("Librarian approved borrow for student ID ${record.studentId}: ${record.bookTitle}")
            } else {
                addNotification("Approval error: No copies available to loan out.")
            }
        }
    }

    fun markReturned(record: BorrowRecord) {
        viewModelScope.launch {
            val bookFlow = repository.allBooks.firstOrNull() ?: emptyList()
            val targetBook = bookFlow.find { it.id == record.bookId }
            if (targetBook != null) {
                val updatedBook = targetBook.copy(available = (targetBook.available + 1).coerceAtMost(targetBook.copies))
                repository.updateBook(updatedBook)

                val updatedRecord = record.copy(status = "RETURNED")
                repository.updateBorrowRecord(updatedRecord)
                addNotification("Book successfully restocked and marked returned: ${record.bookTitle}")
            }
        }
    }

    fun addBook(title: String, author: String, category: String, copies: Int, location: String, description: String, coverColor: String) {
        viewModelScope.launch {
            if (title.isBlank() || author.isBlank()) return@launch
            val newBook = Book(
                title = title.trim(),
                author = author.trim(),
                category = category,
                copies = copies,
                available = copies,
                shelfLocation = location.ifBlank { "Shelf A-1" },
                description = description.ifBlank { "No description added yet." },
                coverColorHex = coverColor
            )
            repository.insertBook(newBook)
            addNotification("Added new physical book model to Catalog: ${title}")
        }
    }

    fun uploadDigitalMaterial(title: String, category: String, type: String, description: String) {
        viewModelScope.launch {
            if (title.isBlank()) return@launch
            val newDM = DigitalMaterial(
                title = title.trim(),
                category = category,
                type = type,
                description = description.ifBlank { "Pre-requisite academic prep notes." },
                fileUrl = "https://example.com/uploaded_${title.hashCode()}.pdf"
            )
            repository.insertDigitalMaterial(newDM)
            addNotification("New high-value $type uploaded to E-Library library: $title")
        }
    }

    fun addAnnouncement(title: String, content: String, isPinned: Boolean) {
        viewModelScope.launch {
            if (title.isBlank() || content.isBlank()) return@launch
            val ann = Announcement(
                title = title.trim(),
                content = content.trim(),
                timestamp = "2026-05-23",
                isPinned = isPinned
            )
            repository.insertAnnouncement(ann)
            addNotification("New announcement pinned to student bulletins boards.")
        }
    }

    // --- Gemini API call for AI Study ---
    fun askAiTutor(prompt: String) {
        if (prompt.isBlank()) return
        _isAiLoading.value = true
        _aiHistory.value = _aiHistory.value + (prompt to true)

        viewModelScope.launch {
            val response = queryGemini(prompt)
            _aiHistory.value = _aiHistory.value + (response to false)
            _aiResponse.value = response
            _isAiLoading.value = false
        }
    }

    private suspend fun queryGemini(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = ""
        if (apiKey.isBlank()) {
            return@withContext "AI study response simulated: That is an excellent question! In high school syllabus terms, we can analyze this model effectively using our loaded E-books and past sheets. Double check with study guides on Shelf B."
        }
        try {
            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val sanitizedPrompt = prompt.replace("\"", "\\\"").replace("\n", " ")
            val requestJson = "{\"contents\":[{\"parts\":[{\"text\":\"You are an expert high school study coach at Oakridge Library. Help the student query: $sanitizedPrompt\"}]}]}"
            val requestBody = requestJson.toRequestBody(mediaType)
            
            val responseBody = RetrofitClient.service.generateContent(apiKey, requestBody)
            val responseString = responseBody.string()
            
            val json = org.json.JSONObject(responseString)
            val candidates = json.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val part = parts?.optJSONObject(0)
            part?.optString("text") ?: "No valid study response generated"
        } catch (e: Exception) {
            "Simulated response: That is an excellent concept. Keep studying your textbook notes. Error: ${e.message}"
        }
    }
}

// --- Common API Definitions for Gemini API Direct REST Option ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: RequestBody
    ): ResponseBody
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}
