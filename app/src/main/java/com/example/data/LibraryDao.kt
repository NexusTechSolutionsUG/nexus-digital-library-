package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {

    // --- Users ---
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    // --- Books ---
    @Query("SELECT * FROM books")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE title LIKE :query OR author LIKE :query OR category LIKE :query")
    fun searchBooks(query: String): Flow<List<Book>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book)

    @Update
    suspend fun updateBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)

    // --- Borrow Records ---
    @Query("SELECT * FROM borrow_records ORDER BY id DESC")
    fun getAllBorrowRecords(): Flow<List<BorrowRecord>>

    @Query("SELECT * FROM borrow_records WHERE studentId = :studentId ORDER BY id DESC")
    fun getBorrowRecordsForStudent(studentId: String): Flow<List<BorrowRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBorrowRecord(record: BorrowRecord)

    @Update
    suspend fun updateBorrowRecord(record: BorrowRecord)

    // --- Digital Materials ---
    @Query("SELECT * FROM digital_materials ORDER BY id DESC")
    fun getAllDigitalMaterials(): Flow<List<DigitalMaterial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDigitalMaterial(material: DigitalMaterial)

    @Delete
    suspend fun deleteDigitalMaterial(material: DigitalMaterial)

    // --- Announcements ---
    @Query("SELECT * FROM announcements ORDER BY isPinned DESC, id DESC")
    fun getAllAnnouncements(): Flow<List<Announcement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: Announcement)

    @Delete
    suspend fun deleteAnnouncement(announcement: Announcement)
}
