package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {
    // Books
    @Query("SELECT * FROM books ORDER BY title ASC")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    fun getBookByIdFlow(id: String): Flow<Book?>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    suspend fun getBookById(id: String): Book?

    @Query("SELECT * FROM books WHERE category = :category ORDER BY title ASC")
    fun getBooksByCategory(category: String): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchBooks(query: String): Flow<List<Book>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<Book>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book)

    @Update
    suspend fun updateBook(book: Book)

    @Query("UPDATE books SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE books SET availableCopies = :available WHERE id = :id")
    suspend fun updateAvailableCopies(id: String, available: Int)

    // Borrow Records
    @Query("SELECT * FROM borrow_records ORDER BY borrowDate DESC")
    fun getAllBorrowRecords(): Flow<List<BorrowRecord>>

    @Query("SELECT * FROM borrow_records WHERE returnDate IS NULL ORDER BY dueDate ASC")
    fun getActiveBorrowRecords(): Flow<List<BorrowRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBorrowRecord(record: BorrowRecord): Long

    @Update
    suspend fun updateBorrowRecord(record: BorrowRecord)

    // Reviews
    @Query("SELECT * FROM book_reviews WHERE bookId = :bookId ORDER BY timestamp DESC")
    fun getReviewsForBook(bookId: String): Flow<List<BookReview>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: BookReview)

    // Announcements
    @Query("SELECT * FROM announcements ORDER BY isPinned DESC, id DESC")
    fun getAllAnnouncements(): Flow<List<Announcement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncements(announcements: List<Announcement>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: Announcement)

    // --- NEW VIEWER OPERATIONS ---

    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId LIMIT 1")
    fun getReadingProgressFlow(bookId: String): Flow<ReadingProgress?>

    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId LIMIT 1")
    suspend fun getReadingProgress(bookId: String): ReadingProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingProgress(progress: ReadingProgress)

    @Query("SELECT * FROM book_bookmarks WHERE bookId = :bookId ORDER BY page ASC")
    fun getBookmarksForBook(bookId: String): Flow<List<BookBookmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookBookmark)

    @Query("DELETE FROM book_bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: Int)

    @Query("SELECT * FROM book_highlights WHERE bookId = :bookId ORDER BY page ASC, timestamp DESC")
    fun getHighlightsForBook(bookId: String): Flow<List<BookHighlight>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlight(highlight: BookHighlight)

    @Query("DELETE FROM book_highlights WHERE id = :id")
    suspend fun deleteHighlight(id: Int)

    @Query("SELECT * FROM book_annotations WHERE bookId = :bookId ORDER BY page ASC")
    fun getAnnotationsForBook(bookId: String): Flow<List<BookAnnotation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnotation(annotation: BookAnnotation)

    @Query("DELETE FROM book_annotations WHERE id = :id")
    suspend fun deleteAnnotation(id: Int)
}

