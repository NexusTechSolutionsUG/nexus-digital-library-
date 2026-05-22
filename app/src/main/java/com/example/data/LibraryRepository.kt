package com.example.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryRepository(private val libraryDao: LibraryDao) {

    val allBooks: Flow<List<Book>> = libraryDao.getAllBooks()
    val allBorrowRecords: Flow<List<BorrowRecord>> = libraryDao.getAllBorrowRecords()
    val activeBorrowRecords: Flow<List<BorrowRecord>> = libraryDao.getActiveBorrowRecords()
    val allAnnouncements: Flow<List<Announcement>> = libraryDao.getAllAnnouncements()

    fun getBookById(id: String): Flow<Book?> = libraryDao.getBookByIdFlow(id)

    fun getBooksByCategory(category: String): Flow<List<Book>> = libraryDao.getBooksByCategory(category)

    fun searchBooks(query: String): Flow<List<Book>> = libraryDao.searchBooks(query)

    fun getReviewsForBook(bookId: String): Flow<List<BookReview>> = libraryDao.getReviewsForBook(bookId)

    suspend fun insertBook(book: Book) = withContext(Dispatchers.IO) {
        libraryDao.insertBook(book)
    }

    suspend fun toggleFavorite(bookId: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        libraryDao.updateFavorite(bookId, isFavorite)
    }

    suspend fun borrowBook(book: Book, studentName: String): Boolean = withContext(Dispatchers.IO) {
        val currentBook = libraryDao.getBookById(book.id) ?: return@withContext false
        if (currentBook.availableCopies <= 0) return@withContext false

        // Update book copies
        libraryDao.updateAvailableCopies(book.id, currentBook.availableCopies - 1)

        // Make borrow record
        val record = BorrowRecord(
            bookId = book.id,
            bookTitle = book.title,
            author = book.author,
            borrowDate = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis() + (14L * 24L * 60L * 60L * 1000L), // 14 days
            readingProgress = 0
        )
        libraryDao.insertBorrowRecord(record)
        true
    }

    suspend fun returnBook(record: BorrowRecord) = withContext(Dispatchers.IO) {
        val currentBook = libraryDao.getBookById(record.bookId) ?: return@withContext
        libraryDao.updateAvailableCopies(record.bookId, currentBook.availableCopies + 1)
        
        val updatedRecord = record.copy(
            returnDate = System.currentTimeMillis(),
            readingProgress = 100
        )
        libraryDao.updateBorrowRecord(updatedRecord)
    }

    suspend fun updateReadingProgress(record: BorrowRecord, progress: Int) = withContext(Dispatchers.IO) {
        libraryDao.updateBorrowRecord(record.copy(readingProgress = progress.coerceIn(0, 100)))
    }

    suspend fun addReview(bookId: String, studentName: String, rating: Int, reviewText: String) = withContext(Dispatchers.IO) {
        val review = BookReview(
            bookId = bookId,
            studentName = studentName,
            rating = rating,
            reviewText = reviewText,
            timestamp = System.currentTimeMillis()
        )
        libraryDao.insertReview(review)

        // Recalculate rating on book - quick updates
        val reviews = libraryDao.getReviewsForBook(bookId).first()
        val totalRating = reviews.sumOf { it.rating } + rating
        val averageRating = totalRating.toFloat() / (reviews.size + 1)
        val currentBook = libraryDao.getBookById(bookId)
        if (currentBook != null) {
            libraryDao.insertBook(currentBook.copy(rating = averageRating))
        }
    }

    suspend fun addAnnouncement(title: String, content: String, isPinned: Boolean) = withContext(Dispatchers.IO) {
        val announcement = Announcement(
            title = title,
            content = content,
            date = "Today",
            isPinned = isPinned
        )
        libraryDao.insertAnnouncement(announcement)
    }

    init {
        // Run seed data initialization in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val books = libraryDao.getAllBooks().first()
                if (books.isEmpty()) {
                    seedDatabase()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun seedDatabase() {
        val defaultBooks = listOf(
            Book(
                id = "lit-001",
                title = "The Great Gatsby",
                author = "F. Scott Fitzgerald",
                category = "Literature",
                description = "Set in the glamorous, roaring twenties on Long Island, this quintessential masterpiece is a beautifully structured narration of the elusive American dream, high society, and Jay Gatsby's fateful, romantic obsession with Daisy Buchanan.",
                publishedYear = 1925,
                totalCopies = 5,
                availableCopies = 4,
                rating = 4.5f,
                coverUrl = "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&w=400&q=80"
            ),
            Book(
                id = "lit-002",
                title = "To Kill a Mockingbird",
                author = "Harper Lee",
                category = "Literature",
                description = "Set in the fictional town of Maycomb, Alabama, during the Great Depression, this stunning novel explores racial injustice and the destruction of innocence. Atticus Finch, an honorable lawyer, defends a Black man falsely accused of raping a white woman, seen through the eyes of young Scout Finch.",
                publishedYear = 1960,
                totalCopies = 6,
                availableCopies = 6,
                rating = 4.9f,
                coverUrl = "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&w=400&q=80"
            ),
            Book(
                id = "lit-003",
                title = "Hamlet",
                author = "William Shakespeare",
                category = "Literature",
                description = "Shakespeare's grandest tragedy. Prince Hamlet is called to avenge his father's murder, sparking an introspective journey exploring mortality, revenge, existentialism, and betrayal that definitions of dramatic literature.",
                publishedYear = 1603,
                totalCopies = 8,
                availableCopies = 7,
                rating = 4.7f,
                coverUrl = "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&w=400&q=80"
            ),
            Book(
                id = "fic-001",
                title = "1984",
                author = "George Orwell",
                category = "Fiction",
                description = "A gripping dystopian masterpiece exploring total government surveillance, control, propaganda, and thought-police under Big Brother. Winston Smith searches for truth and self-expression in a highly regimented society.",
                publishedYear = 1949,
                totalCopies = 4,
                availableCopies = 2,
                rating = 4.8f,
                coverUrl = "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=400&q=80"
            ),
            Book(
                id = "fic-002",
                title = "Fahrenheit 451",
                author = "Ray Bradbury",
                category = "Fiction",
                description = "In a future, consumer-driven society where books are completely outlawed and firemen burn academic collections, Guy Montag begins questioning the digital screens, virtual reality walls, and his high school curriculum.",
                publishedYear = 1953,
                totalCopies = 5,
                availableCopies = 5,
                rating = 4.6f,
                coverUrl = "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=400&q=80"
            ),
            Book(
                id = "sci-001",
                title = "A Brief History of Time",
                author = "Stephen Hawking",
                category = "Science & Tech",
                description = "The landmark, accessible science bestseller. Hawking explains cosmology, black holes, general relativity, quantum mechanics, and the origin of our vast, beautiful universe with crystal clarity.",
                publishedYear = 1988,
                totalCopies = 3,
                availableCopies = 3,
                rating = 4.7f,
                coverUrl = "https://images.unsplash.com/photo-1507842217343-583bb7270b66?auto=format&fit=crop&w=400&q=80"
            ),
            Book(
                id = "sci-002",
                title = "The Code Book",
                author = "Simon Singh",
                category = "Science & Tech",
                description = "An excellent, dramatic history of codes, cryptos, and code-breaking. From ancient ciphers protecting letters to high-school math foundations and modern quantum cryptography encryption systems.",
                publishedYear = 1999,
                totalCopies = 3,
                availableCopies = 2,
                rating = 4.6f,
                coverUrl = "https://images.unsplash.com/photo-1507842217343-583bb7270b66?auto=format&fit=crop&w=400&q=80"
            ),
            Book(
                id = "sci-003",
                title = "Automate the Boring Stuff",
                author = "Al Sweigart",
                category = "Science & Tech",
                description = "The ultimate practical introduction to Python. Great for code-oriented high school students looking to build scrapers, auto-sort folders, automate sheets, and customize digital homework systems.",
                publishedYear = 2015,
                totalCopies = 5,
                availableCopies = 5,
                rating = 4.8f,
                coverUrl = "https://images.unsplash.com/photo-1507842217343-583bb7270b66?auto=format&fit=crop&w=400&q=80"
            ),
            Book(
                id = "his-001",
                title = "The Diary of Anne Frank",
                author = "Anne Frank",
                category = "History",
                description = "The poignant journal kept by a young Jewish girl in hiding during the Nazi occupation of the Netherlands in WWII. A classic deeply personal, historical document and profile of hope.",
                publishedYear = 1947,
                totalCopies = 4,
                availableCopies = 4,
                rating = 4.9f,
                coverUrl = "https://images.unsplash.com/photo-1461360370896-922624d12aa1?auto=format&fit=crop&w=400&q=80"
            ),
            Book(
                id = "his-002",
                title = "Sapiens",
                author = "Yuval Noah Harari",
                category = "History",
                description = "An overarching, dramatic narrative of human history. Sapiens describes our evolutionary breakthroughs from hunter-gatherers to cognitive, agricultural, and modern scientific revolutions.",
                publishedYear = 2011,
                totalCopies = 4,
                availableCopies = 3,
                rating = 4.5f,
                coverUrl = "https://images.unsplash.com/photo-1461360370896-922624d12aa1?auto=format&fit=crop&w=400&q=80"
            ),
            Book(
                id = "self-001",
                title = "Atomic Habits",
                author = "James Clear",
                category = "Self-Growth",
                description = "The absolute guide to systemizing your daily routines. Clear shares how tiny 1% daily changes translate into massive life successes over years, perfect for setting study routines and focus strategies.",
                publishedYear = 2018,
                totalCopies = 7,
                availableCopies = 5,
                rating = 4.9f,
                coverUrl = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?auto=format&fit=crop&w=400&q=80"
            ),
            Book(
                id = "self-002",
                title = "Mindset",
                author = "Carol S. Dweck",
                category = "Self-Growth",
                description = "Stanford psychologist Carol Dweck shows how our intelligence and abilities can be actively developed via hard work, mentorship, resilient learning loops, and embracing a growth mindset.",
                publishedYear = 2006,
                totalCopies = 6,
                availableCopies = 6,
                rating = 4.7f,
                coverUrl = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?auto=format&fit=crop&w=400&q=80"
            )
        )

        val defaultAnnouncements = listOf(
            Announcement(
                title = "Welcome to your Digital Library Space! 📚",
                content = "We are thrilled to launch the brand-new High School Digital Library Interface. Explore award-winning literature, check out coding manuals, build daily streaks, write student reviews, and keep track of classroom assignments. Happy reading!",
                date = "May 22, 2026",
                isPinned = true
            ),
            Announcement(
                title = "Student Book Club Meetup ☕",
                content = "This Friday at 3:30 PM, the Book Club will host an open forum discussions about 'The Great Gatsby' in Room 302. Come with favorite quotes, thematic notes, and enjoy complimentary snacks!",
                date = "May 21, 2026",
                isPinned = false
            ),
            Announcement(
                title = "AP Exams Pre-Study Week ✍️",
                content = "Need a silent study space? The upper library gallery has been reserved exclusively for AP Exam study groups and collaborative syllabus reviews. Quiet hours will be strictly maintained.",
                date = "May 18, 2026",
                isPinned = false
            )
        )

        libraryDao.insertBooks(defaultBooks)
        libraryDao.insertAnnouncements(defaultAnnouncements)

        // Add some default reviews for books to make them feel active
        val defaultReviews = listOf(
            BookReview(
                bookId = "self-001",
                studentName = "Alex Rivera (Grade 11)",
                rating = 5,
                reviewText = "This completely changed how I organize my study system and track my homework! Absolutely map-shifting and easy to put into practice.",
                timestamp = System.currentTimeMillis() - 86400000L
            ),
            BookReview(
                bookId = "self-001",
                studentName = "Chloe Chen (Grade 12)",
                rating = 4,
                reviewText = "A super interesting breakdown of triggers, rewards, and habit loops. I've designed a coding routine around it.",
                timestamp = System.currentTimeMillis() - 172800000L
            ),
            BookReview(
                bookId = "lit-001",
                studentName = "Daniel Miller (Grade 10)",
                rating = 5,
                reviewText = "Beautiful prose. The tragedy and decay of the American dream is perfectly painted. Best literature book we've had.",
                timestamp = System.currentTimeMillis() - 259200000L
            ),
            BookReview(
                bookId = "sci-003",
                studentName = "Max Foster (Grade 11)",
                rating = 5,
                reviewText = "Incredible Python coursebook. The lessons on regex and folder automation saved me hours on my science fair project!",
                timestamp = System.currentTimeMillis() -  43200000L
            )
        )
        for (rev in defaultReviews) {
            libraryDao.insertReview(rev)
        }
    }
}
