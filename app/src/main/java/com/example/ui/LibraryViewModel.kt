package com.example.ui

import android.app.Application
import android.speech.tts.TextToSpeech
import java.util.Locale
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = LibraryDatabase.getDatabase(application)
    val repository = LibraryRepository(db.libraryDao)

    // User session state
    val studentName = MutableStateFlow("Alex Rivera")
    val studentId = MutableStateFlow("StudentID-2026-HSL")
    val readingStreak = MutableStateFlow(5) // Default academic reading streak

    // Profile detail states
    val classLevel = MutableStateFlow("S3")
    val streamName = MutableStateFlow("Stream A")
    val combination = MutableStateFlow<String?>(null)

    // Multi-role State
    val currentRole = MutableStateFlow(UserRole.STUDENT)

    private fun deriveAcademicProfile(studentId: String): Triple<String, String, String?> {
        val cleanId = studentId.toUpperCase().trim()
        
        // Match standard format e.g. S4B-101, S5PEM-102
        val classPart = when {
            cleanId.contains("S1") || cleanId.startsWith("S1") -> "S1"
            cleanId.contains("S2") || cleanId.startsWith("S2") -> "S2"
            cleanId.contains("S3") || cleanId.startsWith("S3") -> "S3"
            cleanId.contains("S4") || cleanId.startsWith("S4") -> "S4"
            cleanId.contains("S5") || cleanId.startsWith("S5") -> "S5"
            cleanId.contains("S6") || cleanId.startsWith("S6") -> "S6"
            else -> "S3" // Default fallback
        }
        
        // Determine stream or combination
        val isALevel = classPart == "S5" || classPart == "S6"
        val streamPart = if (isALevel) {
            when {
                cleanId.contains("ARTS") || cleanId.contains("HEG") -> "Arts"
                cleanId.contains("MEG") -> "Arts"
                else -> "Science"
            }
        } else {
            val streamChar = when {
                cleanId.contains("B") -> 'B'
                cleanId.contains("C") -> 'C'
                cleanId.contains("D") -> 'D'
                else -> 'A'
            }
            "Stream $streamChar"
        }
        
        val combinationPart = if (isALevel) {
            when {
                cleanId.contains("HEG") -> "HEG"
                cleanId.contains("MEG") -> "MEG"
                else -> "PEM" // Default A-level combo
            }
        } else {
            null
        }
        
        return Triple(classPart, streamPart, combinationPart)
    }

    fun setSessionUser(
        firstName: String?,
        lastName: String?,
        email: String?,
        roleStr: String?,
        classLevelVal: String? = null,
        streamNameVal: String? = null,
        combinationVal: String? = null
    ) {
        val fullName = listOfNotNull(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
            .ifBlank { email?.substringBefore("@") } ?: "School User"
        studentName.value = fullName
        
        val mappedRole = when (roleStr?.uppercase()) {
            "STUDENT" -> UserRole.STUDENT
            "LIBRARIAN" -> UserRole.LIBRARIAN
            "TEACHER" -> UserRole.TEACHER
            "ADMIN" -> UserRole.ADMIN
            "SUPER_ADMIN" -> UserRole.SUPER_ADMIN
            else -> UserRole.STUDENT
        }
        currentRole.value = mappedRole

        var finalId = "StudentID-2026-HSL"
        if (mappedRole == UserRole.STUDENT && email != null) {
            val prefs = getApplication<Application>().getSharedPreferences("nexus_auth_prefs", android.content.Context.MODE_PRIVATE)
            val lookedUpId = prefs.getString("email_to_id_$email", null) ?: email.substringBefore("@").uppercase()
            studentId.value = lookedUpId
            finalId = lookedUpId
        } else {
            studentId.value = email ?: "StudentID-2026-HSL"
            finalId = email ?: "StudentID-2026-HSL"
        }

        // Initialize academic profile attributes for students
        if (mappedRole == UserRole.STUDENT) {
            val parsedProfile = deriveAcademicProfile(finalId)
            
            val finalClass = if (!classLevelVal.isNullOrBlank()) classLevelVal else parsedProfile.first
            val finalStream = if (!streamNameVal.isNullOrBlank()) streamNameVal else parsedProfile.second
            
            val isOLevel = finalClass in listOf("S1", "S2", "S3", "S4")
            val finalCombination = if (isOLevel) {
                null
            } else {
                if (!combinationVal.isNullOrBlank()) combinationVal else (parsedProfile.third ?: "PEM")
            }
            
            classLevel.value = finalClass
            streamName.value = finalStream
            combination.value = finalCombination

            // Automatically select academicClassLevel
            val classEnum = when (finalClass) {
                "S1" -> AcademicClassLevel.S1
                "S2" -> AcademicClassLevel.S2
                "S3" -> AcademicClassLevel.S3
                "S4" -> AcademicClassLevel.S4
                "S5" -> AcademicClassLevel.S5
                "S6" -> AcademicClassLevel.S6
                else -> AcademicClassLevel.S3
            }
            selectedClassLevel.value = classEnum

            // Automatically set selectedSubjectId to first subject of student's class
            val match = allAcademicSubjects.value.firstOrNull { it.classLevel == classEnum }
            if (match != null) {
                selectedSubjectId.value = if (!isOLevel && finalCombination != null) {
                    allAcademicSubjects.value.firstOrNull { 
                        it.classLevel == classEnum && it.name.contains(finalCombination, ignoreCase = true) 
                    }?.id ?: match.id
                } else {
                    match.id
                }
            }
        }
    }

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

    // SaaS Administration States
    val userModerationProfiles = MutableStateFlow<List<UserModerationProfile>>(listOf(
        UserModerationProfile("u1", "Alex Rivera", "Grade 11", "Active", 0, "alex.rivera@nexustech.edu"),
        UserModerationProfile("u2", "Marcus Chen", "Grade 12", "Flagged", 2, "marcus.chen@nexustech.edu"),
        UserModerationProfile("u3", "Sarah Jenkins", "Grade 10", "Active", 0, "sarah.j@nexustech.edu"),
        UserModerationProfile("u4", "Taylor Vance", "Grade 11", "Suspended", 5, "taylor.vance@nexustech.edu"),
        UserModerationProfile("u5", "Emily Rodriguez", "Grade 12", "Active", 1, "emily.r@nexustech.edu")
    ))

    val overdueItems = MutableStateFlow<List<OverdueItem>>(listOf(
        OverdueItem("ov1", "Marcus Chen", "To Kill a Mockingbird", 4, 2.00, "marcus.chen@nexustech.edu"),
        OverdueItem("ov2", "Taylor Vance", "Macbeth", 9, 4.50, "taylor.vance@nexustech.edu"),
        OverdueItem("ov3", "Jordan Blake", "1984", 2, 1.00, "jordan.b@nexustech.edu")
    ))

    val systemStorageMetrics = MutableStateFlow<List<SystemStorageMetric>>(listOf(
        SystemStorageMetric("EPUB Books", "4.2 MB Cached (3 files)", 3, "2.1:1 Compressed"),
        SystemStorageMetric("PDF Notes", "3.8 MB Cached (5 files)", 5, "1.8:1 Compressed"),
        SystemStorageMetric("Video Lectures", "25.6 MB Cached (2 files)", 2, "3.5:1 Highly Compressed"),
        SystemStorageMetric("Audio Companions", "12.1 MB Cached (3 files)", 3, "1.5:1 Standard")
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
        searchFilterAvailability,
        currentRole,
        classLevel,
        combination
    ) { allBooks, query, category, sort, availOnly, role, level, comb ->
        allBooks.filter { book ->
            // Academic level and combination-specific filtering for students
            if (role == UserRole.STUDENT) {
                val isOLevel = level in listOf("S1", "S2", "S3", "S4")
                if (isOLevel) {
                    when (level) {
                        "S1", "S2" -> {
                            // Hide advanced physics and cosmology for junior students
                            book.id != "sci-001" 
                        }
                        else -> true
                    }
                } else {
                    // A-Level (S5-S6): Filter strictly by combination!
                    when (comb) {
                        "PEM" -> {
                            // Physics, Economics, Math
                            // Show Science & Tech, Self-Growth, Sapiens
                            book.category in listOf("Science & Tech", "Self-Growth") || book.id == "his-002"
                        }
                        "MEG" -> {
                            // Math, Economics, Geography
                            // Show Science & Tech, Self-Growth
                            book.category in listOf("Science & Tech", "Self-Growth")
                        }
                        "HEG" -> {
                            // History, Economics, Geography
                            // Show History, Self-Growth, Literature
                            book.category in listOf("History", "Self-Growth", "Literature")
                        }
                        else -> true
                    }
                }
            } else {
                true
            }
        }.filter { book ->
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

    // ==========================================
    // CLASS-BASED ACADEMIC CURRICULUM STATES
    // ==========================================

    val allAcademicSubjects = MutableStateFlow<List<Subject>>(listOf(
        // S1 Subjects
        Subject(
            id = "s1-math",
            name = "S1 Mathematics",
            classLevel = AcademicClassLevel.S1,
            teachers = listOf(TeacherProfile("t_lule", "Mr. Lule", "", "Mathematics", 14, 0, "m.lule@nexustech.edu")),
            resources = listOf(
                AcademicResource("s1-math-n1", "Prime Numbers & Factors Notes", ResourceCategory.NOTES, "450 KB", "Prime numbers are positive integers greater than 1 that only have factors 1 and itself.", "math_notes_prime", "Mr. Lule"),
                AcademicResource("s1-math-t1", "Introductory Algebra Textbook", ResourceCategory.TEXTBOOKS, "12.5 MB", "Comprehensive guide covering variables, equations, coefficients and graphs.", "math_textbook_s1", "Mr. Lule")
            ),
            description = "High school introduction covering primes, integers, simple equations, operations and linear shapes."
        ),
        Subject(
            id = "s1-ict",
            name = "S1 General ICT",
            classLevel = AcademicClassLevel.S1,
            teachers = listOf(TeacherProfile("t_ss", "Mr. Ssewankambo", "", "ICT", 24, 1, "ss.ict@nexustech.edu")),
            resources = listOf(
                AcademicResource("s1-ict-n1", "Modern Computer Hardware Notes", ResourceCategory.NOTES, "850 KB", "Computer hardware consists of input, processing, storage memory and output channels.", "ict_hardware_notes", "Mr. Ssewankambo")
            ),
            description = "Foundational information technology skills, physical components and MS Office suite overview."
        ),
        
        // S3 Subjects
        Subject(
            id = "s3-bio",
            name = "S3 Biology",
            classLevel = AcademicClassLevel.S3,
            teachers = listOf(TeacherProfile("t_ala", "Ms. Alanyo", "", "Biology", 32, 2, "g.alanyo@nexustech.edu")),
            resources = listOf(
                AcademicResource("s3-bio-p1", "UNEB Past Paper 2022 Biology P1", ResourceCategory.PAST_PAPERS, "1.2 MB", "National final examinations covering aerobic cycles and photosynthesis charts.", "uneb_bio_2022_pdf", "Ms. Alanyo"),
                AcademicResource("s3-bio-n1", "Cellular Respiration Teacher Handout", ResourceCategory.NOTES, "680 KB", "Active chemical conversion processes breaking down glucose for energy creation in human mitochondria.", "bio_notes_resp", "Ms. Alanyo"),
                AcademicResource("s3-bio-d1", "Standard Animal Cell Diagram", ResourceCategory.DIAGRAMS, "1.8 MB", "Labeled schematic identifying Cell membrane, Nucleus, Ribosome, and Golgi complexes. Direct vector visual.", "diagram_animal_cell", "Ms. Alanyo"),
                AcademicResource("s3-bio-v1", "Ecosystem Energy Pathways Lecture", ResourceCategory.VIDEOS, "18.2 MB", "A high-fidelity mini video lecture explaining thermodynamics and food pyramids.", "video_respiration_lecture", "Ms. Alanyo", 540)
            ),
            description = "Detailed cell components, enzymes catalysis, aerobic processes, ecosystems structure, and plant transport."
        ),
        Subject(
            id = "s3-math",
            name = "S3 Mathematics",
            classLevel = AcademicClassLevel.S3,
            teachers = listOf(TeacherProfile("t_oke", "Mr. Okello", "", "Mathematics", 45, 1, "j.okello@nexustech.edu")),
            resources = listOf(
                AcademicResource("s3-math-n1", "Quadratics & Parabolic Functions Slides", ResourceCategory.NOTES, "510 KB", "Finding solutions of degree 2 equations using factorization methodology and general formula.", "math_notes_quadratics", "Mr. Okello"),
                AcademicResource("s3-math-p1", "Nexus Tech Term 1 Practice Paper", ResourceCategory.PAST_PAPERS, "920 KB", "Algebraic fractions, coordinate planes intersecting, matrix transformations exercises.", "midterm_math_s3", "Mr. Okello")
            ),
            description = "Expanding standard algebra concepts, quadratic formulas, quadratic coordinates, and indices."
        ),
 
        // S4 Subjects
        Subject(
            id = "s4-math",
            name = "S4 Mathematics",
            classLevel = AcademicClassLevel.S4,
            teachers = listOf(TeacherProfile("t_oke", "Mr. Okello", "", "Mathematics", 45, 1, "j.okello@nexustech.edu")),
            resources = listOf(
                AcademicResource("s4-math-p1", "UNEB Final Exam 2023 Paper 1", ResourceCategory.PAST_PAPERS, "1.6 MB", "Complete official government-grade questions on trigonometry and standard matrices.", "uneb_math_2023_pdf", "Mr. Okello")
            ),
            description = "Candidate level integrations, descriptive statistics, vectors, coordinates geometries and circles."
        ),
 
        // S6 Subjects
        Subject(
            id = "s6-econ",
            name = "S6 Economics (PEM/MEG/HEG)",
            classLevel = AcademicClassLevel.S6,
            teachers = listOf(TeacherProfile("t_nam", "Ms. Namusoke", "", "Economics", 38, 3, "f.namusoke@nexustech.edu")),
            resources = listOf(
                AcademicResource("s6-econ-n1", "Inflation & Business Cycles Notes", ResourceCategory.NOTES, "1.1 MB", "Macroeconomics trends detailing demand-pull versus cost-push inflationary pressure.", "econ_notes_inflation", "Ms. Namusoke"),
                AcademicResource("s6-econ-p1", "UNEB S6 Pure Economics Paper 1", ResourceCategory.PAST_PAPERS, "2.4 MB", "High stakes essay items explaining central bank monetary policy controls.", "uneb_econ_s6_pdf", "Ms. Namusoke")
            ),
            description = "Macroeconomic modeling, inflation indexing, balance of trade balances and fiscal planning systems."
        ),
        Subject(
            id = "s6-sub-ict",
            name = "S6 Subsidiary ICT",
            classLevel = AcademicClassLevel.S6,
            teachers = listOf(TeacherProfile("t_ss", "Mr. Ssewankambo", "", "ICT", 24, 1, "ss.ict@nexustech.edu")),
            resources = listOf(
                AcademicResource("s6-ict-t1", "Subsidiary ICT Practice Handbook", ResourceCategory.TEXTBOOKS, "3.2 MB", "Advanced spreadsheet formulations, database modeling and systems integrity checklists.", "sub_ict_guide_s6", "Mr. Ssewankambo")
            ),
            description = "Computing systems troubleshooting, algorithms basics, simple databases and networking protocols."
        ),
        // S5 Subjects
        Subject(
            id = "s5-math-pem",
            name = "S5 Mathematics (PEM/MEG)",
            classLevel = AcademicClassLevel.S5,
            teachers = listOf(TeacherProfile("t_oke", "Mr. Okello", "", "Mathematics", 45, 1, "j.okello@nexustech.edu")),
            resources = listOf(
                AcademicResource("s5-math-n1", "Trigonometric Formulations & Proofs", ResourceCategory.NOTES, "1.4 MB", "Advanced trigonometry notes covering product-to-sum expansion equations.", "math_trig_s5", "Mr. Okello")
            ),
            description = "Advanced calculus foundations, analytical trigonometry, probability vectors, and linear equations."
        ),
        Subject(
            id = "s5-phys-pem",
            name = "S5 Physics (PEM)",
            classLevel = AcademicClassLevel.S5,
            teachers = listOf(TeacherProfile("t_phy", "Mr. Kigozi", "", "Physics", 29, 2, "g.kigozi@nexustech.edu")),
            resources = listOf(
                AcademicResource("s5-phys-n1", "Quantum Mechanics & Wave Theory Notes", ResourceCategory.NOTES, "2.2 MB", "Advanced atomic orbits and photoelectronic effects equations.", "physics_notes_quantum", "Mr. Kigozi")
            ),
            description = "Modern wave theory mechanics, thermodynamics, fluid dynamics and electrostatic fields."
        ),
        Subject(
            id = "s5-geog-heg",
            name = "S5 Geography (MEG/HEG)",
            classLevel = AcademicClassLevel.S5,
            teachers = listOf(TeacherProfile("t_geo", "Mrs. Mugisha", "", "Geography", 31, 0, "m.mugisha@nexustech.edu")),
            resources = listOf(
                AcademicResource("s5-geog-p1", "East Africa Topographical Mapping Guide", ResourceCategory.PAST_PAPERS, "3.1 MB", "Analysis of coordinate systems and practical mapping contours.", "geography_map_east_africa", "Mrs. Mugisha")
            ),
            description = "Physical geography contours, geomorphology formations, and economic trading channels."
        ),
        Subject(
            id = "s5-history-heg",
            name = "S5 History (HEG)",
            classLevel = AcademicClassLevel.S5,
            teachers = listOf(TeacherProfile("t_his", "Mr. Mukasa", "", "History", 40, 3, "s.mukasa@nexustech.edu")),
            resources = listOf(
                AcademicResource("s5-history-n1", "Modern African Decolonization Processes", ResourceCategory.NOTES, "2.5 MB", "Chronology of the rise of nationalist movements post-WWII.", "history_notes_african_decolonization", "Mr. Mukasa")
            ),
            description = "In-depth history of African political evolution, East African colonial dynamics, and international relations."
        ),
        // Additional S6 Subjects for combinations
        Subject(
            id = "s6-phys-pem",
            name = "S6 Physics (PEM)",
            classLevel = AcademicClassLevel.S6,
            teachers = listOf(TeacherProfile("t_phy", "Mr. Kigozi", "", "Physics", 29, 2, "g.kigozi@nexustech.edu")),
            resources = listOf(
                AcademicResource("s6-phys-n1", "Electromagnetism & AC Circuit Theory", ResourceCategory.NOTES, "1.8 MB", "Formulas and derivations for alternating currents and electric motors.", "physics_notes_ac_circuits", "Mr. Kigozi")
            ),
            description = "Candidate curriculum wave theory, magnetic field physics, atomic model developments and electronics."
        ),
        Subject(
            id = "s6-history-heg",
            name = "S6 History (HEG)",
            classLevel = AcademicClassLevel.S6,
            teachers = listOf(TeacherProfile("t_his", "Mr. Mukasa", "", "History", 40, 3, "s.mukasa@nexustech.edu")),
            resources = listOf(
                AcademicResource("s6-history-n1", "Cold War & International Sanctions", ResourceCategory.NOTES, "1.9 MB", "Global politics, military treaty alliances, and geopolitical shifts.", "history_notes_cold_war", "Mr. Mukasa")
            ),
            description = "Global modern histories, treaty organizations, and international conflicts analysis."
        )
    ))

    // Academic Navigation selections
    val selectedClassLevel = MutableStateFlow(AcademicClassLevel.S3)
    val selectedSubjectId = MutableStateFlow<String?>("s3-bio")
    val selectedResourceCategory = MutableStateFlow<ResourceCategory?>(null) // null means "All"
    val personalAssignedSubjectsOnly = MutableStateFlow(true) // Filter to student's assigned subjects (S3)

    // Interactive study state triggers
    val activeRevisionPlannerSlot = MutableStateFlow<String?>(null)
    val showAIQuizSimulator = MutableStateFlow(false)
    val showAISummaryPanel = MutableStateFlow(false)
    val activeBookmarkedPages = MutableStateFlow<Set<String>>(emptySet()) // bookmarked resource items

    // Study planners & countdowns
    val offlineSavedResources = MutableStateFlow<Set<String>>(emptySet())
    val examCountdownList = MutableStateFlow<List<ExamCountdown>>(listOf(
        ExamCountdown("c1", "S3 End of Term Assessments", "June 15, 2026", 18),
        ExamCountdown("c2", "S4 National UNEB Math P1", "October 20, 2026", 124),
        ExamCountdown("c3", "S6 Final General Paper A-Level", "November 05, 2026", 140)
    ))

    val studyPlannerList = MutableStateFlow<List<StudyPlannerItem>>(listOf(
        StudyPlannerItem("p1", "S3 Biology", "Review mitochondria diagram & markers", 45, "Sunday", false),
        StudyPlannerItem("p2", "S3 Mathematics", "Practice solving quadratics worksheet", 30, "Wednesday", true),
        StudyPlannerItem("p3", "S6 Economics", "ReadMs. Namusoke's inflation slides", 40, "Monday", false)
    ))

    val studyAnalytics = MutableStateFlow<List<AcademicUsageMetric>>(listOf(
        AcademicUsageMetric("S3 Biology", 180, 0.85f, 12, System.currentTimeMillis() - 2 * 3600 * 1000),
        AcademicUsageMetric("S3 Mathematics", 120, 0.70f, 6, System.currentTimeMillis() - 4 * 3600 * 1000),
        AcademicUsageMetric("S6 Economics", 90, 0.50f, 4, System.currentTimeMillis() - 24 * 3600 * 1000),
        AcademicUsageMetric("S1 General ICT", 40, 0.95f, 3, System.currentTimeMillis() - 48 * 3600 * 1000)
    ))

    // Interactive quiz simulations
    val currentQuizQuestions = MutableStateFlow<List<RevisionQuestion>>(listOf(
        RevisionQuestion("q1", "Which of the following cellular components is called the powerhouse of the cell?", listOf("Ribosome", "Chloroplast", "Mitochondrion", "Lysosome"), 2, "Mitochondria produce cellular ATP through the citric acid cycle."),
        RevisionQuestion("q2", "In biology, what primary process is responsible for producing energy without oxygen?", listOf("Aerobic respiration", "Anaerobic respiration", "Photosynthesis", "Catalysis"), 1, "Anaerobic respiration converts glucose into energy/lactic acid in the absence of oxygen."),
        RevisionQuestion("q3", "What is the green photosynthetic pigment found inside chloroplast organelles?", listOf("Carotene", "Chlorophyll", "Xanthophyll", "Anthocyanin"), 1, "Chlorophyll absorbs red and blue light spectra for carbon fixation reaction chains.")
    ))
    val currentQuizIndex = MutableStateFlow(0)
    val currentQuizScore = MutableStateFlow(0)
    val selectedQuizAnswerIndex = MutableStateFlow<Int?>(null)
    val quizCompletedStatus = MutableStateFlow(false)

    // Interactive flashcards S3 Bio
    val activeFlashcardList = MutableStateFlow<List<StudyFlashcard>>(listOf(
        StudyFlashcard("f1", "What does plant cell wall consist of?", "Cellulose provides mechanical structure & rigour."),
        StudyFlashcard("f2", "Which organelle carries out photolysis of water molecules?", "Thylakoid membranes inside the chloroplast, powered by photons."),
        StudyFlashcard("f3", "What biological catalysts speed up key internal body reactions?", "Enzymes that fit specific substrates into active sites under precise thermal range."),
        StudyFlashcard("f4", "Name the cellular sugar compound manufactured during carbon fixation?", "Glucose which is polymerised into starch storage molecules.")
    ))

    // Subject discussion threads map
    val subjectDiscussionForum = MutableStateFlow<List<ForumPost>>(listOf(
        ForumPost("fm1", "Sarah Jenkins", "STUDENT", "#0F766E", "Ms. Alanyo, can we get the marking guide for the 2022 past paper?", 15),
        ForumPost("fm2", "Ms. Alanyo", "TEACHER", "#0284C7", "Hi Sarah! I am compiling the marking guide with custom vectors. Stay tuned tonight!", 10),
        ForumPost("fm3", "Alex Rivera", "STUDENT", "#6366F1", "Thanks Ms. Alanyo! Reviewing the glycolysis diagrams now.", 2)
    ))

    // Selected Resource Details (PDF document simulated overlay)
    val activeAcademicResource = MutableStateFlow<AcademicResource?>(null)

    // --- ACADEMIC OPERATIONS ---

    fun selectAcademicClass(classLevel: AcademicClassLevel) {
        selectedClassLevel.value = classLevel
        // Automatically default first matching subject
        val firstSub = allAcademicSubjects.value.firstOrNull { it.classLevel == classLevel }
        selectedSubjectId.value = firstSub?.id
        selectedResourceCategory.value = null
        addNotification("Class Selected", "Viewing curriculum materials for ${classLevel.label}.", "announcement")
    }

    fun selectAcademicSubject(subjId: String) {
        selectedSubjectId.value = subjId
        selectedResourceCategory.value = null
        
        // Subject-specific quizzes and flashcards corresponding directly to class level & combinations!
        val targetSubject = allAcademicSubjects.value.find { it.id == subjId }
        val subjectName = targetSubject?.name ?: "Curriculum"
        
        // Build customized dynamic question list based on the subject!
        val newQuestions = when {
            subjId.contains("math") -> listOf(
                RevisionQuestion("mq1", "What is the degree and roots of the quadratic function f(x) = x^2 - 5x + 6?", listOf("Degree 1: roots x=2,3", "Degree 2: roots x=2,3", "Degree 2: roots x=-2,-3", "Degree 3: roots x=1,6"), 1, "A quadratic equation always has degree 2. Factoring x^2 - 5x + 6 gives (x-2)(x-3)=0."),
                RevisionQuestion("mq2", "Find the value of limit as x approaches 0 of sin(x)/x.", listOf("0", "Undefined", "1", "Infinity"), 2, "By L'Hospital's rule or basic trigonometric limit theorem, limit as x->0 of sin(x)/x equals 1."),
                RevisionQuestion("mq3", "What is the sum of interior angles of a regular hexagon?", listOf("360 degrees", "540 degrees", "720 degrees", "180 degrees"), 2, "Using the formula (n-2)*180, we get (6-2)*180 = 4*180 = 720 degrees.")
            )
            subjId.contains("phys") -> listOf(
                RevisionQuestion("pq1", "Which of the following describes the quantum nature of electromagnetic radiation?", listOf("Wave theory only", "Ray theory only", "Wave-particle duality", "Geometrical refraction"), 2, "Electromagnetic radiation exhibits both wave and particle behaviors (photoelectric effect)."),
                RevisionQuestion("pq2", "State the SI unit of magnetic flux density.", listOf("Tesla (T)", "Weber (Wb)", "Farad (F)", "Henry (H)"), 0, "Tesla is the SI unit of magnetic flux density. One Tesla equals one Weber per square meter."),
                RevisionQuestion("pq3", "In an AC circuit, what component causes current to lag voltage by exactly 90 degrees?", listOf("Pure capacitor", "Pure resistor", "Pure inductor", "Silicon diode"), 2, "An inductor stores energy in magnetic fields, causing current to lag voltage by 90 degrees.")
            )
            subjId.contains("econ") -> listOf(
                RevisionQuestion("eq1", "What type of inflation is caused by a persistent increase in national aggregate demand?", listOf("Cost-push inflation", "Demand-pull inflation", "Hyperinflation", "Imported inflation"), 1, "Demand-pull inflation occurs when aggregate demand outpaces aggregate supply in an economy."),
                RevisionQuestion("eq2", "Which curve shows the relationship between tax rates and total tax revenue?", listOf("Gini curve", "Lorenz curve", "Phillips curve", "Laffer curve"), 3, "The Laffer curve suggests there is an optimum tax rate that maximizes total government revenue.")
            )
            subjId.contains("geog") -> listOf(
                RevisionQuestion("gq1", "What index is commonly used to measure the rate of tectonic folding and faulting?", listOf("Richter Scale", "Mercalli Scale", "Orogeny Coefficient", "None of the above"), 0, "The Richter scale measures earthquake magnitude released along fault lines during tectonic movements."),
                RevisionQuestion("gq2", "Which geographic contour layout indicates a steep cliff-like precipice?", listOf("Spaced out parallel contours", "Intersecting contour lines", "Very closely packed contour lines", "Concentric circle contours"), 2, "Closely packed contour lines indicate a high gradient cliff or steep mountain rise.")
            )
            subjId.contains("history") -> listOf(
                RevisionQuestion("hq1", "Which major treaty officially brought WWII conflict, decolonization, and League of Nation reorganizations to a close?", listOf("Treaty of Versailles", "Paris Peace Treaties (1947)", "Yalta Agreement", "Berlin Act (1884)"), 1, "The Paris Peace Treaties of 1947 established borders, reparations, and colonies distribution post-WWII."),
                RevisionQuestion("hq2", "In which year did Uganda successfully secure full Independence from British colonial rule?", listOf("1958", "1961", "1962", "1966"), 2, "Uganda attained independence on October 9, 1962, under Milton Obote and Kabaka Mutesa II.")
            )
            subjId.contains("ict") -> listOf(
                RevisionQuestion("iq1", "Which network protocol is primarily responsible for assigning dynamic IP addresses to home/school machines?", listOf("HTTP", "DHCP", "FTP", "SMTP"), 1, "Dynamic Host Configuration Protocol (DHCP) automatically manages IP addresses on active subnets."),
                RevisionQuestion("iq2", "What character must always prefix any advanced computing formulation block in Microsoft Excel?", listOf("$", "#", "=", "@"), 2, "An equals sign (=) tells spreadsheet engines that the following text represents an active formula.")
            )
            else -> listOf(
                RevisionQuestion("q1", "Which cell powerhouse organelle coordinates energy operations?", listOf("Ribosome", "Chloroplast", "Mitochondrion", "Lysosome"), 2, "Mitochondria produce cellular ATP through the citric acid cycle."),
                RevisionQuestion("q2", "What is the process of generating chemical energy without oxygen support?", listOf("Aerobic", "Anaerobic", "Photosynthesis", "Refraction"), 1, "Anaerobic respiration breaks down glucose in the absence of oxygen."),
                RevisionQuestion("q3", "What pigment processes solar energy in chloroplasts?", listOf("Carotene", "Chlorophyll", "Xanthophyll", "Enzymes"), 1, "Chlorophyll captures red & blue light spectra for plant energy creation cycles.")
            )
        }
        currentQuizQuestions.value = newQuestions
        currentQuizIndex.value = 0
        currentQuizScore.value = 0
        selectedQuizAnswerIndex.value = null
        quizCompletedStatus.value = false
        
        // Subject-specific dynamic flashcards
        val newFlashcards = when {
            subjId.contains("math") -> listOf(
                StudyFlashcard("f1", "What are the coordinates of the turning point of y = ax^2 + bx + c?", "x = -b / (2a). Substitute x back into formula to solve y vertex."),
                StudyFlashcard("f2", "Define Prime Number.", "An integer greater than 1 with exactly two divisors: 1 and itself.")
            )
            subjId.contains("phys") -> listOf(
                StudyFlashcard("f1", "What is Faraday's law of induction?", "The induced electromotive force in a closed circuit is equal to the negative rate of change of magnetic flux."),
                StudyFlashcard("f2", "Define a Photon.", "A discrete packet or bundle of quantum electromagnetic energy.")
            )
            subjId.contains("econ") -> listOf(
                StudyFlashcard("f1", "What is Fiscal Policy?", "The use of taxation and government spending to influence level of economic activity."),
                StudyFlashcard("f2", "Identify the Gini Coefficient range.", "0 representing absolute equality and 1 representing absolute inequality.")
            )
            subjId.contains("geog") -> listOf(
                StudyFlashcard("f1", "Define a Rift Valley.", "A linear-shaped lowland between highlands or mountain ranges created by geologic faults.")
            )
            subjId.contains("history") -> listOf(
                StudyFlashcard("f1", "Define Decolonization.", "The undoing of colonialism, where a nation establishes sovereign independence over its territory.")
            )
            else -> listOf(
                StudyFlashcard("f1", "What does plant cell wall consist of?", "Cellulose provides mechanical structure & rigour."),
                StudyFlashcard("f2", "Which organelle carries out photolysis of water molecules?", "Thylakoid membranes inside the chloroplast, powered by photons.")
            )
        }
        activeFlashcardList.value = newFlashcards

        // Trigger simulation of analytics log
        studyAnalytics.value = studyAnalytics.value.map {
            if (it.subjectName == subjectName) {
                it.copy(
                    studyDurationMinutes = it.studyDurationMinutes + 5,
                    resourcesOpenedCount = it.resourcesOpenedCount + 1,
                    lastAccessedTimestamp = System.currentTimeMillis()
                )
            } else it
        }
    }

    fun toggleOfflineSavedResource(resourceId: String) {
        val current = offlineSavedResources.value
        if (current.contains(resourceId)) {
            offlineSavedResources.value = current - resourceId
            addNotification("Resource Offline Removed", "Deleted item $resourceId from local cache storage.", "goal")
        } else {
            offlineSavedResources.value = current + resourceId
            addNotification("Downloaded Offline", "Saved companion file to secure offline space. 100% accessible anywhere.", "goal")
        }
    }

    fun selectAcademicResource(res: AcademicResource?) {
        activeAcademicResource.value = res
        if (res != null) {
            // Toast / Notification trigger and analytics addition
            val trackName = allAcademicSubjects.value.find { subj -> 
                subj.resources.any { it.id == res.id }
            }?.name ?: "Curriculum Core"
            
            studyAnalytics.value = studyAnalytics.value.map {
                if (it.subjectName == trackName) {
                    it.copy(
                        resourcesOpenedCount = it.resourcesOpenedCount + 1,
                        lastAccessedTimestamp = System.currentTimeMillis()
                    )
                } else it
            }
        }
    }

    fun submitQuizAnswer(choiceIndex: Int) {
        selectedQuizAnswerIndex.value = choiceIndex
        val activeQ = currentQuizQuestions.value.getOrNull(currentQuizIndex.value)
        if (activeQ != null && choiceIndex == activeQ.correctIndex) {
            currentQuizScore.value = currentQuizScore.value + 1
        }
    }

    fun advanceQuizQuestion() {
        val nextIdx = currentQuizIndex.value + 1
        if (nextIdx < currentQuizQuestions.value.size) {
            currentQuizIndex.value = nextIdx
            selectedQuizAnswerIndex.value = null
        } else {
            quizCompletedStatus.value = true
            // Complete assignment notification integration
            addNotification("Academic Quiz Finalized", "Scored ${currentQuizScore.value}/${currentQuizQuestions.value.size} points on the interactive biology module.", "goal")
        }
    }

    fun resetQuizModule() {
        currentQuizIndex.value = 0
        currentQuizScore.value = 0
        selectedQuizAnswerIndex.value = null
        quizCompletedStatus.value = false
    }

    fun addPlannerTask(subj: String, topic: String, minutes: Int, scheduledDay: String) {
        val newItem = StudyPlannerItem(
            id = "planned_${System.currentTimeMillis()}",
            subjectName = subj,
            topicToCover = topic,
            targetMinutes = minutes,
            scheduledDay = scheduledDay,
            isDone = false
        )
        studyPlannerList.value = studyPlannerList.value + newItem
        addNotification("Revision Planner Created", "Added $topic to $scheduledDay study schedule ($minutes mins).", "goal")
    }

    fun togglePlannerTaskCompletion(id: String) {
        studyPlannerList.value = studyPlannerList.value.map {
            if (it.id == id) {
                val nextState = !it.isDone
                if (nextState) {
                    // Update analytics study time!
                    studyAnalytics.value = studyAnalytics.value.map { stat ->
                        if (stat.subjectName == it.subjectName) {
                            stat.copy(
                                studyDurationMinutes = stat.studyDurationMinutes + it.targetMinutes,
                                completeAssignmentsRatio = (stat.completeAssignmentsRatio + 0.05f).coerceIn(0f, 1.0f)
                            )
                        } else stat
                    }
                    addNotification("Goal Reached!", "Earned ${it.targetMinutes} minutes study bonus on ${it.subjectName}.", "goal")
                }
                it.copy(isDone = nextState)
            } else it
        }
    }

    fun addForumMessage(subjId: String, text: String) {
        if (text.isNotBlank()) {
            val newMsg = ForumPost(
                id = "fm_${System.currentTimeMillis()}",
                userName = studentName.value,
                userRole = "STUDENT",
                avatarColorHex = "#4F46E5",
                messageText = text,
                minutesAgo = 0
            )
            subjectDiscussionForum.value = subjectDiscussionForum.value + newMsg
        }
    }

    // AI summary simulation per subject
    val simulatedAISummaryResult = MutableStateFlow<String?>(null)
    val generatorLoading = MutableStateFlow(false)

    fun generateAISubjectSummary(subjName: String) {
        viewModelScope.launch {
            generatorLoading.value = true
            simulatedAISummaryResult.value = null
            kotlinx.coroutines.delay(1200) // simulated network delay
            simulatedAISummaryResult.value = """
                **AI REVISION CO-PILOT CRASH SUMMARY — $subjName**
                
                1. **High Yield Syllabus Core**:
                   Focus intensively on basic mechanism structures, enzymatic reaction formulas and regulatory feedback loop matrices. Past UNEB papers indicate standard questions (65%) target comparative diagrams.
                
                2. **Common Culprit Misunderstandings**:
                   - Confusing cellular respiration inputs with photosynthetic gas exchange coefficients.
                   - Failing to justify total structural surface area-to-volume ratio scaling limitations.
                   
                3. **Active Exam Action Points**:
                   Practice labeling diagrams under 3 minutes. Memorize secondary catalyst thermal threshold conditions ($subjName - optimal: 37.5°C).
            """.trimIndent()
            generatorLoading.value = false
        }
    }

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

            // Build dynamic system instruction based on student profile
            val nameVal = studentName.value
            val classVal = classLevel.value ?: "S3"
            val streamVal = streamName.value ?: ""
            val combinationVal = combination.value
            val combinationText = if (!combinationVal.isNullOrBlank()) "with combination $combinationVal" else ""
            
            val dynamicSystemInstruction = """
                You are Auden, a hyper-intelligent, friendly AI High School Librarian at Nexus Tech High School. 
                You are assisting student $nameVal, who is in Class $classVal, Stream $streamVal $combinationText.
                Ensure that all study recommendations, curriculum advice, book summaries, interactive revision, spelling quizzes, or educational flashcards align exactly with the appropriate syllabus level of a Class $classVal student $combinationText. 
                Never reference higher or lower level curriculum standards unless requested. Speak directly to $nameVal in a motivating and highly professional academic companion tone.
            """.trimIndent()

            // Send to Gemini Rest Service
            val responseText = GeminiApiService.generateContent(chatMessages.value, systemInstruction = dynamicSystemInstruction)
            
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

    // --- SaaS Admin Operations ---
    fun updateUserStatus(id: String, status: String) {
        userModerationProfiles.value = userModerationProfiles.value.map {
            if (it.id == id) it.copy(status = status) else it
        }
        addNotification("User Moderated", "Updated status of user to $status.", "announcement")
    }

    fun addNewBookCopy(bookId: String) {
        viewModelScope.launch {
            val list = books.value
            list.find { it.id == bookId }?.let { book ->
                val updatedCopies = book.availableCopies + 1
                val updatedTotal = book.totalCopies + 1
                repository.updateBookCopies(bookId, updatedCopies, updatedTotal)
                addNotification("Inventory Expanded", "Increased stock for ${book.title}.", "announcement")
            }
        }
    }

    fun flagBookDamage(bookId: String) {
        viewModelScope.launch {
            val list = books.value
            list.find { it.id == bookId }?.let { book ->
                if (book.availableCopies > 0) {
                    val updatedCopies = book.availableCopies - 1
                    repository.updateBookCopies(bookId, updatedCopies, book.totalCopies)
                    addNotification("Inventory Report", "Flagged 1 copy of ${book.title} as damaged/removed.", "announcement")
                }
            }
        }
    }

    fun triggerOverdueWarning(overdueId: String) {
        val alert = overdueItems.value.find { it.id == overdueId }
        if (alert != null) {
            addNotification(
                "Warning Dispatched",
                "Sent immediate overdue push notification and fine reminder ($${String.format(java.util.Locale.US, "%.2f", alert.fineAccrued)}) to ${alert.studentName}.",
                "due_date"
            )
        }
    }

    fun runStorageCleanup() {
        viewModelScope.launch {
            systemStorageMetrics.value = systemStorageMetrics.value.map {
                it.copy(sizeLabel = "0 B (Cleaned)", itemCount = 0)
            }
            downloadCacheDownloaded.value = emptySet()
            addNotification("Storage Cleanup", "Emptied student companion cache files completely.", "goal")
        }
    }

    fun compressMediaFiles() {
        viewModelScope.launch {
            systemStorageMetrics.value = systemStorageMetrics.value.map {
                val currentSize = it.sizeLabel.substringBefore(" MB").toDoubleOrNull() ?: 2.0
                it.copy(
                    sizeLabel = "${String.format(java.util.Locale.US, "%.1f", currentSize / 2.5)} MB",
                    compressionRatio = "4.0:1 (Ultra Compressed)"
                )
            }
            addNotification("Media Compression", "Downsampled video lectures and applied aggressive JPEG layout compression across materials.", "announcement")
        }
    }

    // =========================================================================
    // NATIVE ANDROID SPEECH ENGINE & LISTEN MODE
    // =========================================================================
    private var tts: TextToSpeech? = null
    val isTtsActive = MutableStateFlow(false)
    val ttsSpeed = MutableStateFlow(1.0f)
    val textBeingSpoken = MutableStateFlow("")
    val activeTtsResource = MutableStateFlow<String?>(null)
    val offlineAudioCachedSet = MutableStateFlow<Set<String>>(emptySet())
    val subjectPlaylistsList = MutableStateFlow<Map<String, List<String>>>(mapOf(
        "S3 Biology" to listOf("Respiratory Cycle Narration Theory", "Photosynthesis Reaction Pathways", "Plant Evaporator Transpiration Study"),
        "S4 Mathematics" to listOf("Quadratic Formula Proof Guide", "Determinant and Matrix Resolution Lecture", "Pythagoras Euclidean Proof Notes")
    ))

    fun ttsInitialize() {
        if (tts == null) {
            try {
                tts = TextToSpeech(getApplication()) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        tts?.language = Locale.US
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("LibraryViewModel", "TTS Init Error", e)
            }
        }
    }

    fun startSpeakingNotes(resId: String, text: String) {
        ttsInitialize()
        if (text.isNotBlank()) {
            textBeingSpoken.value = text
            activeTtsResource.value = resId
            isTtsActive.value = true
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "AudenSpeechEngine")
            addNotification("Listen Mode Active", "Speech Synthesizer reading biological notes aloud.", "goal")
        }
    }

    fun pauseOrStopSpeaking() {
        tts?.stop()
        isTtsActive.value = false
        textBeingSpoken.value = ""
        activeTtsResource.value = null
    }

    fun adjustTTSSpeed(speed: Float) {
        ttsSpeed.value = speed
        tts?.setSpeechRate(speed)
        val currentText = textBeingSpoken.value
        val currentRes = activeTtsResource.value
        if (isTtsActive.value && currentText.isNotBlank() && currentRes != null) {
            tts?.speak(currentText, TextToSpeech.QUEUE_FLUSH, null, "AudenSpeechEngine")
        }
    }

    fun toggleOfflineAudioCache(resId: String) {
        val current = offlineAudioCachedSet.value.toMutableSet()
        if (current.contains(resId)) {
            current.remove(resId)
            addNotification("Voice Track Removed", "Offline biological audio cache purged.", "goal")
        } else {
            current.add(resId)
            addNotification("Audio Copied Offline", "Audio narration cached 100% locally.", "goal")
        }
        offlineAudioCachedSet.value = current
    }

    // =========================================================================
    // REVISION TIMER SUITE & GAMIFICATION (XP / BADGING)
    // =========================================================================
    val focusTimerActive = MutableStateFlow(false)
    val focusTimeRemainingSeconds = MutableStateFlow(1500) // 25:00 focus
    val revisionStreakCount = MutableStateFlow(12) // Ugandan Student Streak
    val currentXpPoints = MutableStateFlow(2850)
    val currentLevelRank = MutableStateFlow("S4 Eagle Candidate")
    val calendarRemindersSynced = MutableStateFlow(false)

    fun toggleFocusTimer() {
        focusTimerActive.value = !focusTimerActive.value
        if (focusTimerActive.value) {
            addNotification("Focus Block Commenced", "Slayer Pomodoro countdown started (25:00). Focus entirely.", "goal")
        }
    }

    fun resetFocusTimer() {
        focusTimerActive.value = false
        focusTimeRemainingSeconds.value = 1500
    }

    fun completeFocusSession() {
        focusTimerActive.value = false
        focusTimeRemainingSeconds.value = 1500
        currentXpPoints.value += 150
        addNotification("Pomodoro Achievement!", "Finished 25-minute study cycle! Boosted +150 XP.", "goal")
        checkLevelProgression()
    }

    fun syncGoogleCalendarWorkManager() {
        viewModelScope.launch {
            calendarRemindersSynced.value = true
            addNotification("Google Calendar Synced", "Exam countdowns, lessons registered with Android Calendar Manager.", "announcement")
        }
    }

    fun checkLevelProgression() {
        if (currentXpPoints.value >= 3200) {
            currentLevelRank.value = "UNEB Champion Elite"
            addNotification("Rank Promotion!", "Level Up! You attained legendary 'UNEB Champion Elite' rank.", "goal")
        }
    }

    // =========================================================================
    // DISCUSSION & COLLABORATION VERIFICATIONS
    // =========================================================================
    val forumUploadedPDFs = MutableStateFlow<List<String>>(listOf("S4_Biology_2024_Mock_Draft.pdf", "Trigonometric_Rules_Cheat_Workbook.pdf"))
    val forumSpamReports = MutableStateFlow<Set<String>>(emptySet())
    val forumAdminVerifiedPosts = MutableStateFlow<Set<String>>(emptySet())

    fun reportForumSpam(postId: String) {
        val current = forumSpamReports.value.toMutableSet()
        current.add(postId)
        forumSpamReports.value = current
        addNotification("Item Flagged", "Post reported. Pushed to Teacher Moderation Queue.", "due_date")
    }

    fun verifyForumPost(postId: String) {
        val current = forumAdminVerifiedPosts.value.toMutableSet()
        current.add(postId)
        forumAdminVerifiedPosts.value = current
        addNotification("Post Certified", "Verified student response, added study badge citation.", "announcement")
    }

    // =========================================================================
    // UNEB NATIONAL MOCK EXAMINATION ENGINE
    // =========================================================================
    val selectedExamClassLevel = MutableStateFlow(AcademicClassLevel.S4)
    val selectedExamSubjectName = MutableStateFlow("Biology")
    val selectedExamYearString = MutableStateFlow("2024")
    val unebExamActive = MutableStateFlow(false)
    val unebExamSubmitted = MutableStateFlow(false)
    val unebExamTimeSeconds = MutableStateFlow(600) // 10 minutes counting down
    val unebQuestionsRandomized = MutableStateFlow(false)
    val unebCurrentQuestions = MutableStateFlow<List<UNEBQuestion>>(emptyList())
    val unebUserAnswers = MutableStateFlow<Map<String, String>>(emptyMap())
    val unebSubmittedHistory = MutableStateFlow<List<UNEBExamHistoryItem>>(listOf(
        UNEBExamHistoryItem("h1", "S4 Chemistry", "2023", "Senior 4", "38 / 50 Marks", 76, "Division 2", System.currentTimeMillis() - 48*3600*1000),
        UNEBExamHistoryItem("h2", "S4 Mathematics", "2022", "Senior 4", "44 / 50 Marks", 88, "Division 1 (Distinction)", System.currentTimeMillis() - 120*3600*1000)
    ))

    val unebLastScore = MutableStateFlow(0)
    val unebLastDivision = MutableStateFlow("Division 1")
    val unebEssayText = MutableStateFlow("")
    val unebEssayAIScore = MutableStateFlow<Int?>(null)
    val unebEssayAIFeedback = MutableStateFlow<String?>(null)
    val unebEssayEvaluating = MutableStateFlow(false)
    val curriculumActiveTab = MutableStateFlow(0) // 0: Revision Dashboard, 1: UNEB Exam Simulator, 2: Gamification Hub

    fun startTimedUNEBExam() {
        val subj = selectedExamSubjectName.value
        val yr = selectedExamYearString.value
        val questions = getMockQuestionsFor(subj, yr)
        unebCurrentQuestions.value = if (unebQuestionsRandomized.value) questions.shuffled() else questions
        unebUserAnswers.value = emptyMap()
        unebLastScore.value = 0
        unebLastDivision.value = "Division 9 (Fail)"
        unebEssayText.value = ""
        unebEssayAIScore.value = null
        unebEssayAIFeedback.value = null
        unebExamTimeSeconds.value = 600
        unebExamActive.value = true
        unebExamSubmitted.value = false
        addNotification("Exam Started", "S4 $subj timed national trial has commenced.", "announcement")
    }

    fun updateUNEBUserMCQ(qId: String, optionIndex: Int) {
        val answers = unebUserAnswers.value.toMutableMap()
        answers[qId] = optionIndex.toString()
        unebUserAnswers.value = answers
    }

    fun updateUNEBUserStructured(qId: String, answer: String) {
        val answers = unebUserAnswers.value.toMutableMap()
        answers[qId] = answer
        unebUserAnswers.value = answers
    }

    fun submitUNEBExam() {
        if (!unebExamActive.value) return
        unebExamActive.value = false
        unebExamSubmitted.value = true
        
        var score = 0
        val total = unebCurrentQuestions.value.size
        
        unebCurrentQuestions.value.forEach { q ->
            if (q.qType == "MCQ") {
                val userAns = unebUserAnswers.value[q.id]?.toIntOrNull()
                if (userAns != null && userAns == q.correctOptionIndex) {
                    score += 10
                }
            } else if (q.qType == "STRUCTURED") {
                val userAns = unebUserAnswers.value[q.id]?.trim() ?: ""
                if (userAns.equals(q.correctTextAnswer, ignoreCase = true)) {
                    score += 10
                }
            }
        }
        
        if (unebEssayText.value.isNotBlank()) {
            score += 8
        }
        
        val maxScorePossible = (total * 10).coerceAtLeast(30)
        val scorePct = ((score.toFloat() / maxScorePossible) * 100).toInt().coerceIn(0, 100)
        
        val div = when {
            scorePct >= 80 -> "Division 1 (Distinction)"
            scorePct >= 60 -> "Division 2 (Credit)"
            scorePct >= 45 -> "Division 3 (Pass)"
            scorePct >= 35 -> "Division 4 (Pass)"
            else -> "Division 9 (Fail)"
        }
        
        unebLastScore.value = score
        unebLastDivision.value = div
        
        val newHistoryItem = UNEBExamHistoryItem(
            id = "h_${System.currentTimeMillis()}",
            subName = "S4 " + selectedExamSubjectName.value,
            examYear = selectedExamYearString.value,
            classLevel = selectedExamClassLevel.value.label,
            score = "$score / $maxScorePossible Marks",
            scorePct = scorePct,
            divisionLabel = div,
            timestamp = System.currentTimeMillis()
        )
        unebSubmittedHistory.value = listOf(newHistoryItem) + unebSubmittedHistory.value
        currentXpPoints.value += score * 10
        addNotification("Mock Exam Evaluated", "Scored $score/$maxScorePossible ($div) on past paper simulation.", "goal")
        checkLevelProgression()
    }

    fun submitUNEBEssayForAIEvaluation() {
        if (unebEssayText.value.isBlank()) return
        viewModelScope.launch {
            unebEssayEvaluating.value = true
            val systemPrompt = "You are an official Chief Examiner of the Uganda National Examinations Board (UNEB). Grade the student's physics, math or biology essay outline out of 10. Give sharp constructive critiques, grading breakdown and exact UNEB standards adjustments."
            val essayPrompt = unebCurrentQuestions.value.find { it.qType == "ESSAY" }?.text ?: ""
            val userPrompt = "Subject: ${selectedExamSubjectName.value}\nEssay Prompt: $essayPrompt\nStudent Outline Draft:\n${unebEssayText.value}"
            
            val response = GeminiApiService.generateContent(
                history = listOf(ChatMessage("essay_eval", "user", userPrompt)),
                systemInstruction = systemPrompt
            )
            
            unebEssayAIScore.value = 8
            unebEssayAIFeedback.value = response
            unebEssayEvaluating.value = false
            
            addNotification("AI Evaluator Finished", "Auden UNEB Chief Examiner issued rating & rubric criticisms.", "goal")
        }
    }

    fun getMockQuestionsFor(subjectName: String, year: String): List<UNEBQuestion> {
        return when (subjectName) {
            "Biology" -> {
                listOf(
                    UNEBQuestion(
                        id = "ub1",
                        qType = "MCQ",
                        text = "Which of the following cellular components is the major site of cellular energy (ATP) synthesis, labeled as the power generator?",
                        options = listOf("Cytoplasm", "Chloroplast", "Mitochondrion", "Lysosome"),
                        correctOptionIndex = 2,
                        rationaleSummary = "Mitochondria produce cellular ATP through the citric acid cycle and oxidative phosphorylation sequences.",
                        hasImage = true,
                        imageName = "mitochondrion_cell"
                    ),
                    UNEBQuestion(
                        id = "ub2",
                        qType = "STRUCTURED",
                        text = "Identify the major physical process by which plants release water vapor through stomata cells into the atmosphere.",
                        correctTextAnswer = "Transpiration",
                        rationaleSummary = "Transpiration is the evaporation of water from plants, primarily through the stomatal openings of leaves, driving the transpirational stream.",
                    ),
                    UNEBQuestion(
                        id = "ub3",
                        qType = "ESSAY",
                        text = "Section C Essay: Explain the physiological adaptations of a mammalian heart to its dual-circulatory function. Discuss structural thicker walls on the left ventricle.",
                        rationaleSummary = "A model UNEB answer covers: Thicker left ventricle muscular wall to resist higher systemic pressure; tricuspid & bicuspid valves preventing reverse backflow; sinoatrial node coordination."
                    )
                )
            }
            "Mathematics" -> {
                listOf(
                    UNEBQuestion(
                        id = "um1",
                        qType = "MCQ",
                        text = "Solve the quadratic equation x^2 - 5x + 6 = 0. Find the valid solutions.",
                        options = listOf("x = 1, x = 6", "x = 2, x = 3", "x = -2, x = -3", "x = -1, x = 5"),
                        correctOptionIndex = 1,
                        rationaleSummary = "Factoring gives (x - 2)(x - 3) = 0, which yields solutions x = 2 and x = 3.",
                    ),
                    UNEBQuestion(
                        id = "um2",
                        qType = "STRUCTURED",
                        text = "Find the determinant of the 2x2 matrix: A = [[4, 2], [1, 3]].",
                        correctTextAnswer = "10",
                        rationaleSummary = "Det(A) = (4 * 3) - (2 * 1) = 12 - 2 = 10.",
                    ),
                    UNEBQuestion(
                        id = "um3",
                        qType = "ESSAY",
                        text = "Section C Proofs: State and prove Pythagoras\' Theorem using algebra allocations or similar Euclidean geometric constructs.",
                        rationaleSummary = "The proof involves dissecting a large square of side length (a+b) into four right-angled triangles of side lengths a, b, c and an inner square of area c^2."
                    )
                )
            }
            "Economics" -> {
                listOf(
                    UNEBQuestion(
                        id = "ue1",
                        qType = "MCQ",
                        text = "Which measure is most effective for a government battling severe demand-pull inflation?",
                        options = listOf("Lowering income tax rates", "Expanding commercial credit", "Increasing central bank interest rates", "Expanding treasury funding"),
                        correctOptionIndex = 2,
                        rationaleSummary = "Increasing interest rates curbs money supply and loan acquisition, cooling down total demand.",
                    ),
                    UNEBQuestion(
                        id = "ue2",
                        qType = "STRUCTURED",
                        text = "What economics term describes a sustained, general increase in prices across the entire economy?",
                        correctTextAnswer = "Inflation",
                        rationaleSummary = "Inflation is defined as the persistent upward trend in the general level of costs of commodities and services over a period of time.",
                    ),
                    UNEBQuestion(
                        id = "ue3",
                        qType = "ESSAY",
                        text = "Essay: Discuss the fiscal reformations necessary to improve Uganda's domestic tax revenue collections and reduce foreign aid reliance.",
                        rationaleSummary = "Key solutions: formalizing the informal sectors, broadening the direct tax bases, automated customs clearance systems (URA E-Tax implementations), and expanding public trusts."
                    )
                )
            }
            else -> {
                listOf(
                    UNEBQuestion(
                        id = "ui1",
                        qType = "MCQ",
                        text = "Which unit of the computer system is primarily responsible for performing calculations and comparisons?",
                        options = listOf("RAM unit", "Control Unit", "CU Decoder", "Arithmetic Logic Unit (ALU)"),
                        correctOptionIndex = 3,
                        rationaleSummary = "The ALU handles mathematical computations and basic logical operations inside the CPU.",
                    ),
                    UNEBQuestion(
                        id = "ui2",
                        qType = "STRUCTURED",
                        text = "State the acronym for the permanent computer chip memory that stores bootstrapping boot orders.",
                        correctTextAnswer = "ROM",
                        rationaleSummary = "Read-Only Memory (ROM) remains intact when powered off, preserving basic startup routines.",
                    ),
                    UNEBQuestion(
                        id = "ui3",
                        qType = "ESSAY",
                        text = "Section C Systems Design: Propose a comprehensive cyber security framework for a primary school library. Analyze access controls.",
                        rationaleSummary = "A comprehensive security framework covers physical security, encryption, and strict automated user logs."
                    )
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            android.util.Log.e("LibraryViewModel", "TTS Shutdown Error", e)
        }
    }
}

data class UNEBQuestion(
    val id: String,
    val qType: String,
    val text: String,
    val options: List<String> = emptyList(),
    val correctOptionIndex: Int = -1,
    val correctTextAnswer: String = "",
    val rationaleSummary: String = "",
    val hasImage: Boolean = false,
    val imageName: String = ""
) : java.io.Serializable

data class UNEBExamHistoryItem(
    val id: String,
    val subName: String,
    val examYear: String,
    val classLevel: String,
    val score: String,
    val scorePct: Int,
    val divisionLabel: String,
    val timestamp: Long
) : java.io.Serializable

