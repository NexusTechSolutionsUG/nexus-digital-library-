package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Announcement
import com.example.data.Book
import com.example.data.BorrowRecord
import com.example.data.DigitalMaterial

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LibraryDashboard(viewModel: LibraryViewModel) {
    val screen by viewModel.currentScreen.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                fadeIn() with fadeOut()
            },
            label = "screen_transition"
        ) { targetScreen ->
            when (targetScreen) {
                Screen.LOGIN -> LoginScreen(viewModel)
                Screen.DASHBOARD -> StudentDashboard(viewModel)
                Screen.BOOK_DETAILS -> BookDetailsScreen(viewModel)
                Screen.DIGITAL_READER -> DigitalReaderScreen(viewModel)
                Screen.QR_PASSPORT -> QrPassportScreen(viewModel)
                Screen.NOTIFICATIONS -> NotificationsScreen(viewModel)
                Screen.AI_STUDY -> AiStudyCoachScreen(viewModel)
                Screen.ADMIN_DASHBOARD -> AdminDashboard(viewModel)
                else -> LoginScreen(viewModel)
            }
        }
    }
}

// --- LOGIN SCREEN ---
@Composable
fun LoginScreen(viewModel: LibraryViewModel) {
    var selectedRole by remember { mutableStateOf("STUDENT") } // STUDENT, LIBRARIAN
    var idInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }

    // Register details
    var regName by remember { mutableStateOf("") }
    var regClass by remember { mutableStateOf("Grade 11") }

    LaunchedEffect(selectedRole) {
        // Pre-populate standard credentials for easy demo evaluation
        if (selectedRole == "STUDENT") {
            idInput = "student1"
            passwordInput = "123456"
        } else {
            idInput = "admin"
            passwordInput = "123456"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo representation
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "OAKRIDGE HIGH",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Text(
            text = "Digital Library Portal",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Role Card Selection Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("STUDENT" to "Student Access", "LIBRARIAN" to "Librarian Admin").forEach { (role, label) ->
                val isSelected = selectedRole == role
                Button(
                    onClick = { selectedRole = role },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("role_${role.lowercase()}"),
                    shape = RoundedCornerShape(10.dp),
                    elevation = null,
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text(text = label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dynamic Form Header
        Text(
            text = if (isRegistering) "Create Student Account" else "Sign In to Continue",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Index / Reference ID Input
        OutlinedTextField(
            value = idInput,
            onValueChange = { idInput = it },
            label = { Text("Reference ID / Index Code") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("id_input"),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
            shape = RoundedCornerShape(14.dp)
        )

        if (isRegistering) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = regName,
                onValueChange = { regName = it },
                label = { Text("Full Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("name_input"),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                shape = RoundedCornerShape(14.dp)
            )

            if (selectedRole == "STUDENT") {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = regClass,
                    onValueChange = { regClass = it },
                    label = { Text("Class Group") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("class_input"),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Password Input
        OutlinedTextField(
            value = passwordInput,
            onValueChange = { passwordInput = it },
            label = { Text("App Code / Pin") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("password_input"),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            shape = RoundedCornerShape(14.dp)
        )

        if (resultMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = resultMessage,
                color = if (resultMessage.contains("successful", ignoreCase = true) || resultMessage.contains("complete", ignoreCase = true)) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main action Button
        Button(
            onClick = {
                if (isRegistering) {
                    viewModel.register(idInput, regName, regClass, selectedRole, passwordInput) { success, msg ->
                        resultMessage = msg
                    }
                } else {
                    viewModel.login(idInput, passwordInput) { success, msg ->
                        resultMessage = msg
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("login_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = if (isRegistering) "Register & Log In" else "Sign In",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Switch Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isRegistering) "Already registered?" else "First time using portal?",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
            TextButton(
                onClick = {
                    isRegistering = !isRegistering
                    resultMessage = ""
                },
                modifier = Modifier.testTag("switch_action")
            ) {
                Text(
                    text = if (isRegistering) "Sign In" else "Create Account",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// --- STUDENT HOME DASHBOARD ---
@Composable
fun StudentDashboard(viewModel: LibraryViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val notifications by viewModel.sysNotifications.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0 = Home, 1 = Catalog, 2 = My Shelf

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.LibraryBooks, contentDescription = "Catalog") },
                    label = { Text("Catalog", fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.Bookmarks, contentDescription = "Shelf") },
                    label = { Text("Library Shelf", fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header panel always visible
            StudentHeaderPanel(
                studentName = currentUser?.name ?: "Student",
                streakValue = currentUser?.streak ?: 1,
                onLogout = { viewModel.logout() },
                onNotifications = { viewModel.navigateTo(Screen.NOTIFICATIONS) }
            )

            // Dynamic Tab Views
            when (activeTab) {
                0 -> StudentHomeTab(viewModel)
                1 -> StudentCatalogTab(viewModel)
                2 -> StudentShelfTab(viewModel)
            }
        }
    }
}

@Composable
fun StudentHeaderPanel(studentName: String, streakValue: Int, onLogout: () -> Unit, onNotifications: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Circle avatar representation
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = studentName.firstOrNull()?.toString()?.uppercase() ?: "S",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column {
                Text(
                    text = "Oakridge Central Library",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = studentName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Streak counter badge
            IconButton(
                onClick = {},
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape).size(36.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = streakValue.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Notifications
            IconButton(
                onClick = onNotifications,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape).size(36.dp)
            ) {
                Icon(Icons.Default.Notifications, contentDescription = "Alerts", modifier = Modifier.size(18.dp))
            }

            // Log out
            IconButton(
                onClick = onLogout,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape).size(36.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Log out", modifier = Modifier.size(18.dp))
            }
        }
    }
}

// HOME TAB (Student view)
@Composable
fun StudentHomeTab(viewModel: LibraryViewModel) {
    val announcements by viewModel.allAnnouncements.collectAsStateWithLifecycle()
    val digitalMaterials by viewModel.allDigitalMaterials.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcome Bulletin Block
        Card(
            modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )

                Column {
                    Text(
                        text = "Need Study Help?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Ask our Library AI Tutor questions about Physics, Biology, or Literature books instantly.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.navigateTo(Screen.AI_STUDY) },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Connect AI Tutor", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Quick Utilities Rows
        Text("Library Utilities", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Passport Reader Card
            ElevatedCard(
                onClick = { viewModel.navigateTo(Screen.QR_PASSPORT) },
                modifier = Modifier.weight(1f).height(100.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("My Scan ID", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Passport QR", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // AI Hub Card
            ElevatedCard(
                onClick = { viewModel.navigateTo(Screen.AI_STUDY) },
                modifier = Modifier.weight(1f).height(100.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = Color(0xFFFBBF24))
                    Text("AI Coach", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Syllabus Assist", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // E-Challenge Reading Area
        Text("Digital Reading Material", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(digitalMaterials) { material ->
                Card(
                    modifier = Modifier
                        .width(180.dp)
                        .clickable { viewModel.selectMaterial(material) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                            Text(
                                text = material.type.uppercase(),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = material.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = material.category,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, size = 12.dp, tint = MaterialTheme.colorScheme.primary)
                            Text("Open in Reader", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Bulletins
        Text("Pinned Bulletins", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
        announcements.forEach { ann ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (ann.isPinned) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (ann.isPinned) {
                                Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            }
                            Text(text = ann.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Text(text = ann.timestamp, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = ann.content, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// CATALOG TAB
@Composable
fun StudentCatalogTab(viewModel: LibraryViewModel) {
    val books by viewModel.filteredBooks.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val subjectFilter by viewModel.selectedSubject.collectAsStateWithLifecycle()
    val availabilityFilter by viewModel.selectedAvailability.collectAsStateWithLifecycle()

    val subjects = listOf("All", "Literature", "Mathematics", "Physics", "Chemistry", "Biology")
    val availabilities = listOf("All", "Available", "Checked Out")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Search catalog title or author...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_input"),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp)
        )

        // Subject Badges horizontal scroll
        Text("Filter by Domain", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(subjects) { subject ->
                val isSelected = subjectFilter == subject
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectedSubject.value = subject },
                    label = { Text(subject, fontSize = 12.sp) }
                )
            }
        }

        // Availability filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(availabilities) { av ->
                val isSelected = availabilityFilter == av
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectedAvailability.value = av },
                    label = { Text(av, fontSize = 12.sp) }
                )
            }
        }

        // Results Catalog Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Catalog Books (${books.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        if (books.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.LibraryBooks, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    Text("No matching books found", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Try adjusting search keywords or target subject.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(books) { book ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectBook(book) }
                            .testTag("book_item_${book.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Custom painted book spine aesthetic
                            Box(
                                modifier = Modifier
                                    .size(width = 44.dp, height = 64.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(android.graphics.Color.parseColor(book.coverColorHex))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Book, contentDescription = null, tint = Color.Black.copy(alpha = 0.3f))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                    Text(
                                        text = book.category,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                Text(
                                    text = book.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = book.author,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                if (book.available > 0) {
                                    Text("Available", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text("${book.available} cap.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    Text("Loans Out", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// MY SHELF TAB
@Composable
fun StudentShelfTab(viewModel: LibraryViewModel) {
    val studentRecords by viewModel.studentBorrowRecords.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("My Borrowed Books Shelf", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        Text("Your live circulation physical and reference logs.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        if (studentRecords.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, size = 48.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    Text("Your shelf is empty", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Select a book from catalog to request a loan.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(studentRecords) { record ->
                    val isPending = record.status == "PENDING"
                    val isApproved = record.status == "APPROVED"
                    val isOverdue = record.status == "OVERDUE"
                    val isReturned = record.status == "RETURNED"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isOverdue -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                                isPending -> MaterialTheme.colorScheme.surfaceVariant
                                else -> MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = BorderStroke(
                            1.dp,
                            when {
                                isOverdue -> MaterialTheme.colorScheme.error
                                isPending -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = record.bookTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Badge(
                                    containerColor = when {
                                        isOverdue -> MaterialTheme.colorScheme.error
                                        isPending -> MaterialTheme.colorScheme.secondaryContainer
                                        isReturned -> MaterialTheme.colorScheme.surfaceVariant
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                ) {
                                    Text(
                                        text = record.status,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Borrowed", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(record.borrowDate, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Deadline Return", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = record.returnDate,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- BOOK DETAILS SCREEN ---
@Composable
fun BookDetailsScreen(viewModel: LibraryViewModel) {
    val book by viewModel.selectedBook.collectAsStateWithLifecycle()

    book?.let { b ->
        val bookColor = Color(android.graphics.Color.parseColor(b.coverColorHex))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
        ) {
            // Header Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.selectBook(null); viewModel.navigateTo(Screen.DASHBOARD) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text("Book Profile", fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.size(24.dp)) // Spacer
            }

            // Cover and Spine block representation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                bookColor.copy(alpha = 0.4f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .size(width = 140.dp, height = 200.dp)
                        .shadow(8.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = bookColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.15f)),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = b.category.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = b.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = b.author,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Details metadata
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        text = b.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "By ${b.author}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Divider()

                // Shelf Location, Available info blocks
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Location card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Shelf Position", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(b.shelfLocation, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    // Available count card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Status", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (b.available > 0) "${b.available} of ${b.copies} left" else "All Loaned Out",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (b.available > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // Description Box
                Column {
                    Text("About", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = b.description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }

                // PDF Available Notice
                if (b.pdfUrl.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text("E-book Option Available", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            Text("Start immediately with digital read-mode.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action request button
                Button(
                    onClick = { viewModel.requestBookBorrow(b) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("borrow_button"),
                    enabled = b.available > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (b.available > 0) "Request 14-Day Physical Loan" else "No physical copies left",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

// --- DIGITAL READER SCREEN ---
@Composable
fun DigitalReaderScreen(viewModel: LibraryViewModel) {
    val material by viewModel.selectedMaterial.collectAsStateWithLifecycle()

    material?.let { m ->
        var currentScrollPercent by remember { mutableStateOf(0f) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Topbar Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.selectMaterial(null); viewModel.navigateTo(Screen.DASHBOARD) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(m.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Subject: ${m.category} | ${m.type}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { viewModel.addNotification("Downloaded offline reference document.") }) {
                    Icon(Icons.Default.Download, contentDescription = "Download")
                }
            }

            // Horizontal Reading progress bar indicator
            LinearProgressIndicator(
                progress = currentScrollPercent,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            // Scrollable Reader simulator body representation
            val docScrollState = rememberScrollState()
            LaunchedEffect(docScrollState.value, docScrollState.maxValue) {
                if (docScrollState.maxValue > 0) {
                    currentScrollPercent = docScrollState.value.toFloat() / docScrollState.maxValue.toFloat()
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(docScrollState)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header style
                Text(
                    text = m.title.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Oakridge High digital publishing services. Copyrighted material for study purposes.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Chapter Contents Mockup Text
                listOf(
                    "CHAPTER I: CORE FOUNDATIONS & MODELS" to "All natural frameworks depend directly on core dynamic mechanics. Physical bodies obey force equations (F=ma). Similarly, biological entities form elaborate symbiotic relationships that recycle atomic elements like carbon and nitrogen.",
                    "CHAPTER II: CASE STUDY & JOINT EXAMS" to "When reviewing past district papers, focus strongly on practical setups. In physics labs, adjust resistance values slowly and record current fluctuations twice. In literature, compare Hamlet’s internal doubt with Macbeth’s unchecked ambition.",
                    "CHAPTER III: ESSENTIAL CONCEPTS & SUMMARY" to "A complete formula list is provided below. Ensure you can derive the kinetic equations. For literature, note how F. Scott Fitzgerald uses environmental cues (like the green light) to represent ideological aspirations."
                ).forEach { (title, paragraph) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(paragraph, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "--- End of Live Material Cache ---",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// --- QR SCANNER / PASSPORT SCREEN ---
@Composable
fun QrPassportScreen(viewModel: LibraryViewModel) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()

    user?.let { u ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = { viewModel.navigateTo(Screen.DASHBOARD) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("My Digital Passport", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterVertically))
            }

            // QR Code Box (Drawn customly on Canvas to represent a real scanner QR)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "SCAN FOR QUICK Restock / Check-in",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                // The Passport QR Code canvas block representation
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val size = this.size.width
                        val numBlocks = 11
                        val blockSize = size / numBlocks

                        // Draw corner anchors
                        listOf(
                            Offset(0f, 0f),
                            Offset(size - blockSize * 3, 0f),
                            Offset(0f, size - blockSize * 3)
                        ).forEach { pos ->
                            drawRect(
                                color = Color.Black,
                                topLeft = pos,
                                size = Size(blockSize * 3, blockSize * 3)
                            )
                            drawRect(
                                color = Color.White,
                                topLeft = Offset(pos.x + blockSize, pos.y + blockSize),
                                size = Size(blockSize, blockSize)
                            )
                        }

                        // Simulated random binary grids
                        val points = listOf(
                            4 to 1, 5 to 2, 8 to 4, 3 to 5, 6 to 5, 7 to 5, 2 to 7, 5 to 7, 8 to 7, 10 to 7,
                            4 to 8, 7 to 8, 9 to 8, 1 to 9, 3 to 10, 6 to 10
                        )
                        points.forEach { (col, row) ->
                            drawRect(
                                color = Color.Black,
                                topLeft = Offset(col * blockSize, row * blockSize),
                                size = Size(blockSize, blockSize)
                            )
                        }
                    }
                }

                Text(
                    text = u.id,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            // User Info Badge
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = u.name, fontWeight = FontWeight.ExtraBold)
                        Text(text = "Group: ${u.schoolClass}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                        Text(
                            text = u.role,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Button(
                onClick = { viewModel.navigateTo(Screen.DASHBOARD) },
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text("Done")
            }
        }
    }
}

// --- BULLETIN / NOTIFICATIONS SCREEN ---
@Composable
fun NotificationsScreen(viewModel: LibraryViewModel) {
    val systemNotes by viewModel.sysNotifications.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { viewModel.navigateTo(Screen.DASHBOARD) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text("System Alerts", fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = { viewModel.addNotification("Alert stack cleared locally.") }) {
                Text("Simulate Notification")
            }
        }

        if (systemNotes.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No recent alerts")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(systemNotes) { note ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text(note, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Delivered: Just Now", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- AI STUDY COACH SCREEN ---
@Composable
fun AiStudyCoachScreen(viewModel: LibraryViewModel) {
    val aiResponse by viewModel.aiResponse.collectAsStateWithLifecycle()
    val isLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val history by viewModel.aiHistory.collectAsStateWithLifecycle()

    var coachPrompt by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // Back Topbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo(Screen.DASHBOARD) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("AI Study Coach", fontWeight = FontWeight.Bold)
                Text("Oakridge Library assistant powered by Gemini", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // History container list
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            if (history.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Welcome to AI Coach Service", fontWeight = FontWeight.Bold)
                    Text(
                        text = "Examples you can ask:\n• 'Explain character values in Macbeth'\n• 'Summarize Chemistry ecosystem cycles'\n• 'Calculate Classical physics kinematics'",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(history) { (msg, isUser) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                modifier = Modifier.widthIn(max = 280.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = if (isUser) "Student" else "Tutor AI",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = msg,
                                        fontSize = 13.sp,
                                        color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input control row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = coachPrompt,
                onValueChange = { coachPrompt = it },
                placeholder = { Text("Ask study tutor questions...") },
                modifier = Modifier.weight(1f).testTag("ai_input_text"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            IconButton(
                onClick = {
                    viewModel.askAiTutor(coachPrompt)
                    coachPrompt = ""
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    .size(48.dp)
                    .testTag("ai_send_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

// --- ADMIN / LIBRARIAN DASHBOARD ---
@Composable
fun AdminDashboard(viewModel: LibraryViewModel) {
    var adminSection by remember { mutableStateOf(0) } // 0 = Statistics, 1 = Books, 2 = Loans, 3 = Bulletins

    val totalBooks by viewModel.totalBooksCount.collectAsStateWithLifecycle()
    val activeLoans by viewModel.activeLoansCount.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingLoansCount.collectAsStateWithLifecycle()
    val overdueCount by viewModel.overdueLoansCount.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = adminSection == 0,
                    onClick = { adminSection = 0 },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Stats") },
                    label = { Text("Stats") }
                )
                NavigationBarItem(
                    selected = adminSection == 1,
                    onClick = { adminSection = 1 },
                    icon = { Icon(Icons.Default.PostAdd, contentDescription = "Add Book") },
                    label = { Text("Inventory") }
                )
                NavigationBarItem(
                    selected = adminSection == 2,
                    onClick = { adminSection = 2 },
                    icon = { Icon(Icons.Default.AssignmentReturned, contentDescription = "Pending Rules") },
                    label = { Text("Loans") }
                )
                NavigationBarItem(
                    selected = adminSection == 3,
                    onClick = { adminSection = 3 },
                    icon = { Icon(Icons.Default.Campaign, contentDescription = "Bulletins") },
                    label = { Text("Alerts") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Admin Top Header representing profile
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                        Text("LI", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Admin Dashboard", fontWeight = FontWeight.ExtraBold)
                        Text("Central Librarian Hub", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = { viewModel.logout() }) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                }
            }

            // Dynamic Body sections
            when (adminSection) {
                0 -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Overall Statistics", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

                    // Cards Grid representation
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Total Volumes", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(totalBooks.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Active Handouts", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(activeLoans.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Pending Approvals", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = pendingCount.toString(),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (pendingCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Overdue Notices", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = overdueCount.toString(),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (overdueCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Simulated Charts representation
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Loans Activity Trends", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val points = listOf(20f, 40f, 35f, 60f, 55f, 80f, 95f)
                                val spacing = canvasWidth / (points.size - 1)

                                val pathPoints = points.mapIndexed { idx, value ->
                                    Offset(idx * spacing, canvasHeight - (value * canvasHeight / 100f))
                                }

                                pathPoints.zipWithNext { p1, p2 ->
                                    drawLine(
                                        color = Color(0xFFD0BCFF),
                                        start = p1,
                                        end = p2,
                                        strokeWidth = 6f
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> AdminInventoryTab(viewModel)
                2 -> AdminLoansTab(viewModel)
                3 -> AdminAlertsTab(viewModel)
            }
        }
    }
}

@Composable
fun AdminInventoryTab(viewModel: LibraryViewModel) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Shelf A-1") }
    var copies by remember { mutableStateOf("3") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Literature") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Register New Book", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Book Title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Author Name") }, modifier = Modifier.fillMaxWidth())

        // Categories select
        Text("Book Domain Category", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Literature", "Mathematics", "Physics", "Chemistry").forEach { cat ->
                val isSel = selectedCategory == cat
                FilterChip(selected = isSel, onClick = { selectedCategory = cat }, label = { Text(cat, fontSize = 11.sp) })
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = copies,
                onValueChange = { copies = it },
                label = { Text("Physical Copies") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Shelf Area (Code)") },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Brief Book Description") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2
        )

        Button(
            onClick = {
                val copyNum = copies.toIntOrNull() ?: 3
                viewModel.addBook(title, author, selectedCategory, copyNum, location, description, "#D0BCFF")
                title = ""
                author = ""
                description = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add to catalog database")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Upload Digital Material", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        var matTitle by remember { mutableStateOf("") }
        var matType by remember { mutableStateOf("PDF") }
        var matCategory by remember { mutableStateOf("Physics") }

        OutlinedTextField(value = matTitle, onValueChange = { matTitle = it }, label = { Text("Material/Title") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("PDF", "Notes", "Past paper", "Magazine").forEach { type ->
                val isSel = matType == type
                FilterChip(selected = isSel, onClick = { matType = type }, label = { Text(type, fontSize = 11.sp) })
            }
        }

        Button(
            onClick = {
                viewModel.uploadDigitalMaterial(matTitle, matCategory, matType, "Pre-requisite high school study material.")
                matTitle = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Publish Digital Document")
        }
    }
}

@Composable
fun AdminLoansTab(viewModel: LibraryViewModel) {
    val records by viewModel.allBorrowRecords.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Borrowed Handouts Queue", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

        if (records.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No loan records found.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(records) { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(record.bookTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Stud ID: ${record.studentId} | Name: ${record.studentName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Badge(
                                    containerColor = when (record.status) {
                                        "PENDING" -> MaterialTheme.colorScheme.secondaryContainer
                                        "RETURNED" -> MaterialTheme.colorScheme.surfaceVariant
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                ) {
                                    Text(record.status, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (record.status == "PENDING") {
                                    Button(
                                        onClick = { viewModel.approveBorrow(record) },
                                        modifier = Modifier.weight(1f).height(36.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Approve Handout", fontSize = 11.sp)
                                    }
                                } else if (record.status == "APPROVED" || record.status == "OVERDUE") {
                                    Button(
                                        onClick = { viewModel.markReturned(record) },
                                        modifier = Modifier.weight(1f).height(36.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Mark Restocked", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAlertsTab(viewModel: LibraryViewModel) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isPinned by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Pin Bulletins Alert", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Bulletin Subject") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Details Content") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pin to top bulletin boards.")
            Switch(checked = isPinned, onCheckedChange = { isPinned = it })
        }

        Button(
            onClick = {
                viewModel.addAnnouncement(title, content, isPinned)
                title = ""
                content = ""
                isPinned = false
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Broadband announcement notification")
        }
    }
}
