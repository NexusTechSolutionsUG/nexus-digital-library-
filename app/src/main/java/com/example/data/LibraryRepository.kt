package com.example.data

import kotlinx.coroutines.flow.Flow

class LibraryRepository(private val libraryDao: LibraryDao) {

    val allUsers: Flow<List<User>> = libraryDao.getAllUsers()
    val allBooks: Flow<List<Book>> = libraryDao.getAllBooks()
    val allBorrowRecords: Flow<List<BorrowRecord>> = libraryDao.getAllBorrowRecords()
    val allDigitalMaterials: Flow<List<DigitalMaterial>> = libraryDao.getAllDigitalMaterials()
    val allAnnouncements: Flow<List<Announcement>> = libraryDao.getAllAnnouncements()

    fun searchBooks(query: String): Flow<List<Book>> = libraryDao.searchBooks("%$query%")

    fun getBorrowRecordsForStudent(studentId: String): Flow<List<BorrowRecord>> =
        libraryDao.getBorrowRecordsForStudent(studentId)

    suspend fun getUserById(userId: String): User? = libraryDao.getUserById(userId)

    suspend fun insertUser(user: User) = libraryDao.insertUser(user)
    suspend fun updateUser(user: User) = libraryDao.updateUser(user)

    suspend fun insertBook(book: Book) = libraryDao.insertBook(book)
    suspend fun updateBook(book: Book) = libraryDao.updateBook(book)
    suspend fun deleteBook(book: Book) = libraryDao.deleteBook(book)

    suspend fun insertBorrowRecord(record: BorrowRecord) = libraryDao.insertBorrowRecord(record)
    suspend fun updateBorrowRecord(record: BorrowRecord) = libraryDao.updateBorrowRecord(record)

    suspend fun insertDigitalMaterial(material: DigitalMaterial) = libraryDao.insertDigitalMaterial(material)
    suspend fun deleteDigitalMaterial(material: DigitalMaterial) = libraryDao.deleteDigitalMaterial(material)

    suspend fun insertAnnouncement(announcement: Announcement) = libraryDao.insertAnnouncement(announcement)
    suspend fun deleteAnnouncement(announcement: Announcement) = libraryDao.deleteAnnouncement(announcement)

    suspend fun preseedIfEmpty() {
        val existingAdmin = libraryDao.getUserById("admin")
        if (existingAdmin == null) {
            // Seed Users
            libraryDao.insertUser(User("student1", "Jamie Vance", "123456", "STUDENT", "Grade 11", 5))
            libraryDao.insertUser(User("student2", "Sam Adams", "123456", "STUDENT", "Grade 12", 2))
            libraryDao.insertUser(User("teacher1", "Dr. Helen", "123456", "TEACHER", "Science Dept"))
            libraryDao.insertUser(User("admin", "Librarian Jamie", "123456", "LIBRARIAN", "Admin"))

            // Seed Books
            libraryDao.insertBook(Book(
                title = "The Great Gatsby",
                author = "F. Scott Fitzgerald",
                category = "Literature",
                copies = 5,
                available = 4,
                pdfUrl = "https://example.com/gatsby.pdf",
                shelfLocation = "Shelf C-4",
                description = "An exquisite story about wealth, love, and the American Dream in the Roaring Twenties.",
                coverColorHex = "#D0BCFF"
            ))
            libraryDao.insertBook(Book(
                title = "Biology I: Ecosystems",
                author = "Dr. Robert Winston",
                category = "Biology",
                copies = 3,
                available = 2,
                pdfUrl = "https://example.com/biology_ecosystems.pdf",
                shelfLocation = "Shelf B-2",
                description = "Comprehensive guide to high school biology, concentrating on ecosystem models, energy flow, and bio-cycles.",
                coverColorHex = "#98FB98"
            ))
            libraryDao.insertBook(Book(
                title = "The Odyssey",
                author = "Homer",
                category = "Literature",
                copies = 4,
                available = 1,
                pdfUrl = "",
                shelfLocation = "Shelf C-1",
                description = "An ancient Greek epic poem detailing Odysseus' 10-year journey home to Ithaca.",
                coverColorHex = "#FFB4AB"
            ))
            libraryDao.insertBook(Book(
                title = "Organic Chemistry Fundamentals",
                author = "Prof. Jane Miller",
                category = "Chemistry",
                copies = 2,
                available = 2,
                pdfUrl = "https://example.com/org_chemistry.pdf",
                shelfLocation = "Shelf B-3",
                description = "Core organic reactions, nomenclature, and carbon chain formations explained for high school scholars.",
                coverColorHex = "#FFC0CB"
            ))
            libraryDao.insertBook(Book(
                title = "Introduction to Calculus",
                author = "Newton & Leibniz",
                category = "Mathematics",
                copies = 10,
                available = 10,
                pdfUrl = "https://example.com/intro_calculus.pdf",
                shelfLocation = "Shelf A-1",
                description = "Mastering limits, derivatives, integration, and mathematical series with high school level equations.",
                coverColorHex = "#F0E68C"
            ))
            libraryDao.insertBook(Book(
                title = "Classical Physics Essentials",
                author = "Isaac Newton",
                category = "Physics",
                copies = 3,
                available = 3,
                pdfUrl = "",
                shelfLocation = "Shelf A-2",
                description = "Basic Mechanics, Force Laws, Wave Energy, and Classical Optics with solved work examples.",
                coverColorHex = "#DDA0DD"
            ))

            // Seed Digital Materials
            libraryDao.insertDigitalMaterial(DigitalMaterial(
                title = "2025 Physics Past Paper Unit 1",
                category = "Physics",
                type = "Past paper",
                fileUrl = "https://example.com/phys2025.pdf",
                description = "Full terminal paper with detailed marking guide from District Joint Examinations."
            ))
            libraryDao.insertDigitalMaterial(DigitalMaterial(
                title = "Macbeth Study Guide and Scene Exploded",
                category = "Literature",
                type = "Notes",
                fileUrl = "https://example.com/macbeth_guide.pdf",
                description = "Soliloquy translations and act-by-act character dynamic breakdowns."
            ))
            libraryDao.insertDigitalMaterial(DigitalMaterial(
                title = "Spring Term Physics Lab Manual",
                category = "Physics",
                type = "Notes",
                fileUrl = "https://example.com/phys_lab.pdf",
                description = "Required procedures for the terminal lab practicals."
            ))
            libraryDao.insertDigitalMaterial(DigitalMaterial(
                title = "School Chronicle - Spring Edition",
                category = "General",
                type = "Magazine",
                fileUrl = "https://example.com/spring_mag.pdf",
                description = "Interviews with sports captains and academic quiz winners."
            ))

            // Seed Borrow Records
            libraryDao.insertBorrowRecord(BorrowRecord(
                studentId = "student1",
                studentName = "Jamie Vance",
                bookId = 1,
                bookTitle = "The Great Gatsby",
                borrowDate = "2026-05-10",
                returnDate = "2026-05-24",
                status = "APPROVED"
            ))
            libraryDao.insertBorrowRecord(BorrowRecord(
                studentId = "student1",
                studentName = "Jamie Vance",
                bookId = 3,
                bookTitle = "The Odyssey",
                borrowDate = "2026-05-02",
                returnDate = "2026-05-16",
                status = "OVERDUE"
            ))

            // Seed Announcements
            libraryDao.insertAnnouncement(Announcement(
                title = "Annual Science Fair Books Reservation",
                content = "Books in the Science Shelf A and B are reserved for research during biology class. High demand items are limited to 3-day short-term loans.",
                timestamp = "2026-05-20",
                isPinned = true
            ))
            libraryDao.insertAnnouncement(Announcement(
                title = "New Chemistry past papers uploaded",
                content = "S4 Chem joint exam papers from 2023, 2024, and 2025 are now online in the E-Material section. Download them for offline study.",
                timestamp = "2026-05-21",
                isPinned = false
            ))
            libraryDao.insertAnnouncement(Announcement(
                title = "Library closed on Friday",
                content = "Due to regional educator workshop assemblies, the physical library tables will close on Friday. Digital reading remains fully active.",
                timestamp = "2026-05-22",
                isPinned = false
            ))
        }
    }
}
