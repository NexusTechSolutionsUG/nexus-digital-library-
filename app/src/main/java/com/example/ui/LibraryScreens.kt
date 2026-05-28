package com.example.ui

import androidx.compose.ui.draw.scale
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.delay
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LibraryDashboard(viewModel: LibraryViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val studentName by viewModel.studentName.collectAsState()
    val readingStreak by viewModel.readingStreak.collectAsState()
    val activeViewerBookId by viewModel.activeViewerBookId.collectAsState()

    if (activeViewerBookId != null) {
        UniversalFileViewerScreen(
            viewModel = viewModel,
            onClose = { viewModel.closeBookViewer() }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .shadow(8.dp)
                        .testTag("bottom_nav_bar"),
                    containerColor = MaterialTheme.colorScheme.background,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(imageVector = Icons.Default.LibraryBooks, contentDescription = "Catalog") },
                        label = { Text("Catalog", fontWeight = FontWeight.SemiBold, fontSize = 10.sp) },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.testTag("tab_catalog")
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.AutoStories, contentDescription = "My Books") },
                        label = { Text("My Books", fontWeight = FontWeight.SemiBold, fontSize = 10.sp) },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.testTag("tab_my_books")
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Support") },
                        label = { Text("AI Ask", fontWeight = FontWeight.SemiBold, fontSize = 10.sp) },
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        modifier = Modifier.testTag("tab_ai_librarian")
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Campaign, contentDescription = "News") },
                        label = { Text("News", fontWeight = FontWeight.SemiBold, fontSize = 10.sp) },
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        modifier = Modifier.testTag("tab_news")
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.ContactMail, contentDescription = "Profile") },
                        label = { Text("ID Card", fontWeight = FontWeight.SemiBold, fontSize = 10.sp) },
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        modifier = Modifier.testTag("tab_profile")
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(
                        Brush.verticalGradient(
                            colors = if (isSystemInDarkTheme()) {
                                listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF020617))
                            } else {
                                listOf(Color(0xFFFAF9F6), Color(0xFFEEF2F6), Color(0xFFE2E8F0))
                            }
                        )
                    )
            ) {
                // High School Header Row
                SchoolHeaderPanel(studentName, readingStreak)

                // High-fidelity active role changer controls
                RoleSelectorRow(viewModel)

                // Dynamic view based on tab index
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (selectedTab) {
                        0 -> CatalogTab(viewModel)
                        1 -> MyBooksTab(viewModel)
                        2 -> AILibrarianTab(viewModel)
                        3 -> CampusNewsTab(viewModel)
                        4 -> StudentCardTab(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun SchoolHeaderPanel(studentName: String, streak: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                    )
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "OAKRIDGE HIGH",
                style = MaterialTheme.typography.labelSmall,
                color = AcademicGoldLight,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = "Digital Library Space",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
        }

        // Streak status indicator
        Row(
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = "Streak",
                tint = AcademicGoldLight,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$streak-Day Streak",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==========================================
// TAB 1: CATALOG
// ==========================================

@Composable
fun CatalogTab(viewModel: LibraryViewModel) {
    val books by viewModel.books.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedBookId by viewModel.selectedBookId.collectAsState()
    
    // Core search & sort parameters
    val currentRole by viewModel.currentRole.collectAsState()
    val sortOrder by viewModel.searchSortOrder.collectAsState()
    val availOnly by viewModel.searchFilterAvailability.collectAsState()
    val downloadCache by viewModel.downloadCacheDownloaded.collectAsState()

    var showVoiceDialog by remember { mutableStateOf(false) }
    var showScanDialog by remember { mutableStateOf(false) }
    var showAddBookDialog by remember { mutableStateOf(false) }
    var showAssignDialog by remember { mutableStateOf(false) }
    var showAdminPanel by remember { mutableStateOf(false) }

    var catalogMode by remember { mutableStateOf("curriculum") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .border(getGlassBorder(isSystemInDarkTheme()), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = { catalogMode = "curriculum" },
                modifier = Modifier.weight(1f).height(36.dp).testTag("mode_curriculum_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (catalogMode == "curriculum") MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (catalogMode == "curriculum") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Class Curriculum", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Button(
                onClick = { catalogMode = "library" },
                modifier = Modifier.weight(1f).height(36.dp).testTag("mode_library_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (catalogMode == "library") MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (catalogMode == "library") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.LocalLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("General Library", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (catalogMode == "curriculum") {
            AcademicCurriculumWorkspace(viewModel)
        } else {
            val categories = listOf("All", "Literature", "Science & Tech", "Fiction", "History", "Self-Growth")

            // Search & Filter Block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Styled Search Text Field with voice and scanning shortcuts
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("catalog_search_input"),
                    placeholder = { Text("Search title, author, description...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "SearchIcon") },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.updateSearchQuery("") },
                                    modifier = Modifier.testTag("clear_search_button")
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                                }
                            }
                            IconButton(
                                onClick = { showVoiceDialog = true },
                                modifier = Modifier.testTag("voice_search_btn")
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = "Voice search", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(
                                onClick = { showScanDialog = true },
                                modifier = Modifier.testTag("barcode_scan_btn")
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = "Barcode/ISBN scan", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                    )
                )

                // Dynamic Advanced Search Options (Sorting and availability controls)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sorting triggers
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sort:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        listOf("Title", "Rating", "Year").forEach { opt ->
                            val isSortSelected = sortOrder == opt
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isSortSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .clickable { viewModel.searchSortOrder.value = opt }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(opt, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSortSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Available Copies Only switches
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Available Only", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = availOnly,
                            onCheckedChange = { viewModel.searchFilterAvailability.value = it },
                            modifier = Modifier.scale(0.7f).testTag("available_copies_switch")
                        )
                    }
                }

                // Category Chips Row
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectCategory(category) },
                            label = { Text(category, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("category_chip_$category"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                selected = isSelected,
                                enabled = true
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // --- Role Based Dashboard Panels (Librarian/Teacher Management, Admins Analytics Diagrams) ---
            AnimatedVisibility(
                visible = currentRole != UserRole.STUDENT,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                RoleAdministrationDashboard(
                    role = currentRole,
                    viewModel = viewModel,
                    onAddBookClicked = { showAddBookDialog = true },
                    onAssignClicked = { showAssignDialog = true },
                    onOpenAdminPanel = { showAdminPanel = true }
                )
            }

            // Books Grid / List container
            if (books.isEmpty()) {
                EmptyCatalogState()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(books, key = { it.id }) { book ->
                        val isCached = downloadCache.contains(book.id)
                        BookListItemWithOffline(
                            book = book,
                            isCached = isCached,
                            onSelect = { viewModel.selectBook(book.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(book.id, !book.isFavorite) },
                            onToggleCache = { viewModel.toggleOfflineDownload(book.id) }
                        )
                    }
                }
            }
        }
    }

    // --- Overlay Dialogs for Search / Voice / Barcode ---
    if (showVoiceDialog) {
        VoiceSearchDialog(
            onQueryExtracted = { query ->
                viewModel.updateSearchQuery(query)
                showVoiceDialog = false
            },
            onDismiss = { showVoiceDialog = false }
        )
    }

    if (showScanDialog) {
        BarcodeISBNScanDialog(
            viewModel = viewModel,
            onDismiss = { showScanDialog = false }
        )
    }

    if (showAddBookDialog) {
        CMSAddBookDialog(
            viewModel = viewModel,
            onDismiss = { showAddBookDialog = false }
        )
    }

    if (showAssignDialog) {
        CMSCreateAssignmentDialog(
            viewModel = viewModel,
            onDismiss = { showAssignDialog = false }
        )
    }

    if (showAdminPanel) {
        SaaSAdminControlCenterDialog(
            viewModel = viewModel,
            onDismiss = { showAdminPanel = false }
        )
    }

    // Detail Popover / Full sheet
    if (selectedBookId != null) {
        BookDetailDialog(viewModel = viewModel)
    }
}

@Composable
fun BookListItem(
    book: Book,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("book_item_card_${book.id}")
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Book Cover Image or Grad Placeholder
            BookCoverThumbnail(book = book, modifier = Modifier.size(width = 80.dp, height = 115.dp))

            Spacer(modifier = Modifier.width(16.dp))

            // Info column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(115.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Category Chip Left
                    CategoryBadge(category = book.category)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "by ${book.author}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Row with stats and status badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Academic Stars rating
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = AcademicGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f", book.rating),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Available count indicator
                    AvailableBadge(copiesLeft = book.availableCopies)
                }
            }

            // Book Favorite button
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .align(Alignment.Top)
                    .testTag("favorite_button_${book.id}")
            ) {
                Icon(
                    imageVector = if (book.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite Book",
                    tint = if (book.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun BookCoverThumbnail(book: Book, modifier: Modifier = Modifier) {
    val gradColors = when (book.category) {
        "Literature" -> listOf(ScholasticNavy, NavySlatePrimary)
        "Science & Tech" -> listOf(Color(0xFF0F766E), Color(0xFF14B8A6))
        "Fiction" -> listOf(Color(0xFF991B1B), Color(0xFFEF4444))
        "History" -> listOf(Color(0xFF78350F), Color(0xFFD97706))
        else -> listOf(Color(0xFF581C87), Color(0xFFA855F7)) // Self-Growth and default
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.verticalGradient(gradColors))
    ) {
        SubcomposeAsyncImage(
            model = book.coverUrl,
            contentDescription = "Cover for ${book.title}",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            error = {
                // Customized Fallback Cover Design overlay
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = book.category.uppercase(Locale.getDefault()),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = book.title,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 10.sp
                    )
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        )
    }
}

private val NavySlatePrimary = Color(0xFF1E293B)

@Composable
fun CategoryBadge(category: String) {
    Box(
        modifier = Modifier
            .background(
                color = when (category) {
                    "Literature" -> ScholasticNavy.copy(alpha = 0.12f)
                    "Science & Tech" -> Color(0xFF0F766E).copy(alpha = 0.12f)
                    "Fiction" -> Color(0xFF991B1B).copy(alpha = 0.11f)
                    "History" -> Color(0xFF78350F).copy(alpha = 0.12f)
                    else -> Color(0xFF581C87).copy(alpha = 0.12f)
                },
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = category,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = when (category) {
                "Literature" -> ScholasticNavy
                "Science & Tech" -> Color(0xFF0F766E)
                "Fiction" -> Color(0xFF991B1B)
                "History" -> Color(0xFF78350F)
                else -> Color(0xFF581C87)
            }
        )
    }
}

@Composable
fun AvailableBadge(copiesLeft: Int) {
    val isAvailable = copiesLeft > 0
    val bagColor = if (isAvailable) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
    val txtColor = if (isAvailable) Color(0xFF065F46) else Color(0xFF991B1B)

    Box(
        modifier = Modifier
            .background(color = bagColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = if (isAvailable) "Available ($copiesLeft)" else "Borrowed Out",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = txtColor
        )
    }
}

@Composable
fun EmptyCatalogState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FilterList,
            contentDescription = "Empty Filters",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Books Match Filter",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Try clearing search keywords or selecting 'All' category.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

// ==========================================
// POPUP: BOOK DETAILS & REVIEW
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailDialog(viewModel: LibraryViewModel) {
    val book by viewModel.selectedBook.collectAsState()
    val reviews by viewModel.selectedBookReviews.collectAsState()
    val activeBorrowRecords by viewModel.activeBorrowRecords.collectAsState()
    val studentName by viewModel.studentName.collectAsState()

    if (book == null) return

    val currentBook = book!!
    val isAlreadyBorrowed = activeBorrowRecords.any { it.bookId == currentBook.id }

    var showReviewDraft by remember { mutableStateOf(false) }
    var reviewText by remember { mutableStateOf("") }
    var reviewRating by remember { mutableStateOf(5) }

    Dialog(
        onDismissRequest = { viewModel.selectBook(null) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Book Specifications", fontWeight = FontWeight.Black) },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.selectBook(null) }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                },
                bottomBar = {
                    Surface(
                        tonalElevation = 8.dp,
                        modifier = Modifier.shadow(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Virtual In-App material reader direct preview button
                            Button(
                                onClick = {
                                    viewModel.openBookInViewer(currentBook.id)
                                    viewModel.selectBook(null) // dismiss spec dialog
                                },
                                modifier = Modifier
                                    .testTag("in_app_viewer_dialog_shortcut_btn")
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AcademicGoldText,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = "Preview online resources")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Preview Companion", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            if (isAlreadyBorrowed) {
                                Button(
                                    onClick = { },
                                    enabled = false,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Currently Borrowed", fontSize = 11.sp)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        viewModel.borrowSelectedBook(
                                            book = currentBook,
                                            onSuccess = {
                                                viewModel.selectBook(null)
                                            },
                                            onError = { errMsg -> }
                                        )
                                    },
                                    enabled = currentBook.availableCopies > 0,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("borrow_button_trigger"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(Icons.Default.BookmarkAdd, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (currentBook.availableCopies > 0) "Borrow (14d)" else "No Copies",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            FilledTonalIconButton(
                                onClick = { showReviewDraft = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("open_review_editor_button")
                            ) {
                                Icon(Icons.Default.RateReview, contentDescription = "Review book")
                            }
                        }
                    }
                }
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // Header Segment
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            BookCoverThumbnail(
                                book = currentBook,
                                modifier = Modifier
                                    .size(width = 110.dp, height = 160.dp)
                                    .shadow(4.dp, RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            Column {
                                CategoryBadge(category = currentBook.category)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = currentBook.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "by ${currentBook.author}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Published: ${currentBook.publishedYear}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = AcademicGold, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.1f Stars", currentBook.rating),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }

                    // Description Segment
                    item {
                        Column {
                            Text(
                                text = "LITERARY SYNOPSIS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentBook.description,
                                style = MaterialTheme.typography.bodyLarge,
                                lineHeight = 24.sp,
                                textAlign = TextAlign.Justify,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                            )
                        }
                    }

                    // Educational Checklist Banner
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = AcademicGold, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Reader Task Goal", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Log reviews, adjust progress slider, and achieve 100% reading to build academic stars!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }

                    // Reviews Segment Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "STUDENT CLASSROOM REVIEWS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.5.sp
                            )

                            Text(
                                text = "${reviews.size} reviews",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }

                    // Reviews list items
                    if (reviews.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No reviews yet. Be the first Oakridge student to write one!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(reviews) { review ->
                            StudentReviewItem(review)
                        }
                    }
                }
            }

            // Write review dialog popover
            if (showReviewDraft) {
                AlertDialog(
                    onDismissRequest = { showReviewDraft = false },
                    title = { Text("Draft Book Review", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("Authoring book review as student: $studentName", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            
                            // Stars selector row
                            Column {
                                Text("Your Rating:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Row(
                                    modifier = Modifier.padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    for (i in 1..5) {
                                        Icon(
                                            imageVector = if (i <= reviewRating) Icons.Default.Star else Icons.Outlined.Star,
                                            contentDescription = "$i Stars",
                                            tint = if (i <= reviewRating) AcademicGold else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clickable { reviewRating = i }
                                                .testTag("star_picker_$i")
                                        )
                                    }
                                }
                            }

                            // Review textbox
                            OutlinedTextField(
                                value = reviewText,
                                onValueChange = { reviewText = it },
                                placeholder = { Text("What did you think of the characters, arguments, complexity, or lesson?") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .testTag("review_text_input"),
                                maxLines = 4,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (reviewText.isNotBlank()) {
                                    viewModel.addStudentReview(
                                        bookId = currentBook.id,
                                        rating = reviewRating,
                                        reviewText = reviewText
                                    )
                                    reviewText = ""
                                    reviewRating = 5
                                    showReviewDraft = false
                                }
                            },
                            modifier = Modifier.testTag("submit_review_save_button")
                        ) {
                            Text("Publish Review", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showReviewDraft = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun StudentReviewItem(review: BookReview) {
    val formatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val formattedDate = formatter.format(Date(review.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.studentName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                for (i in 1..5) {
                    Icon(
                        imageVector = if (i <= review.rating) Icons.Default.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = AcademicGold,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Text(
                text = review.reviewText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                lineHeight = 20.sp
            )
        }
    }
}

// ==========================================
// TAB 2: MY BOOKS & STREAKS
// ==========================================

@Composable
fun MyBooksTab(viewModel: LibraryViewModel) {
    val activeRecords by viewModel.activeBorrowRecords.collectAsState()
    val allRecords by viewModel.allBorrowRecords.collectAsState()
    val pastRecords = allRecords.filter { it.returnDate != null }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // High School Study Motivation Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = AcademicGoldLight)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Oakridge Study Goal",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Read at least 15 minutes daily. Achieve 100% on borrowed progress to return complete!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Active Borrow Titles Header
        item {
            Text(
                text = "ACTIVE BORROW CHECKOUTS (${activeRecords.size})",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (activeRecords.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No active book checkouts",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Browse the Catalog tab to borrow standard classroom resources.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(activeRecords, key = { it.id }) { record ->
                ActiveCheckoutItem(
                    record = record,
                    onReturn = { viewModel.returnBookRecord(record) },
                    onProgressChange = { progress -> viewModel.updateRecordProgress(record, progress) },
                    onOpenResources = { viewModel.openBookInViewer(record.bookId) }
                )
            }
        }

        // Past returned books segment
        if (pastRecords.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ACADEMIC CHECKOUT HISTORY (${pastRecords.size})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(pastRecords, key = { it.id }) { record ->
                PastCheckoutItem(record)
            }
        }
    }
}

@Composable
fun ActiveCheckoutItem(
    record: BorrowRecord,
    onReturn: () -> Unit,
    onProgressChange: (Int) -> Unit,
    onOpenResources: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
    val dueStr = formatter.format(Date(record.dueDate))

    var sliderState by remember(record.id) { mutableFloatStateOf(record.readingProgress.toFloat()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("checkout_record_${record.bookId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.bookTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "by ${record.author}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Due $dueStr",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Classroom Reading Progress",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "${sliderState.toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Slider(
                value = sliderState,
                onValueChange = { sliderState = it },
                onValueChangeFinished = { onProgressChange(sliderState.toInt()) },
                valueRange = 0f..100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("progress_slider_${record.bookId}")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Digital Companion Opening Button
                Button(
                    onClick = onOpenResources,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AcademicGoldText, // high contrast gold text color
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("open_materials_${record.bookId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Read",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Read & Play Companion", fontWeight = FontWeight.Black, fontSize = 11.sp)
                }

                TextButton(
                    onClick = onReturn,
                    modifier = Modifier.testTag("return_button_${record.bookId}"),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.AssignmentReturn, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Return Book", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PastCheckoutItem(record: BorrowRecord) {
    val formatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val retDateStr = record.returnDate?.let { formatter.format(Date(it)) } ?: "Returned"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.bookTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Returned on $retDateStr",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Complete",
                    tint = SageGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Academic Return",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SageGreen
                )
            }
        }
    }
}

// ==========================================
// TAB 3: CAMPUS NEWS
// ==========================================

@Composable
fun CampusNewsTab(viewModel: LibraryViewModel) {
    val announcements by viewModel.announcements.collectAsState()

    var showDraftForm by remember { mutableStateOf(false) }
    var draftTitle by remember { mutableStateOf("") }
    var draftContent by remember { mutableStateOf("") }
    var draftPinned by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (announcements.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No Announcements Posted", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
            ) {
                items(announcements, key = { it.id }) { announcement ->
                    AnnouncementItem(announcement)
                }
            }
        }

        FloatingActionButton(
            onClick = { showDraftForm = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_announcement_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Post Suggestion")
        }

        if (showDraftForm) {
            AlertDialog(
                onDismissRequest = { showDraftForm = false },
                title = { Text("Suggest Campus Library Bulletin", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Draft a bulletin, book drive announcement, or study club alert to present on the library whiteboard.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        
                        OutlinedTextField(
                            value = draftTitle,
                            onValueChange = { draftTitle = it },
                            label = { Text("Bulletin Subject") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("announcement_title_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = draftContent,
                            onValueChange = { draftContent = it },
                            label = { Text("Detailed Information") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .testTag("announcement_content_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = draftPinned,
                                onCheckedChange = { draftPinned = it },
                                modifier = Modifier.testTag("announcement_pinned_checkbox")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pin bulletin to upper panel", fontSize = 13.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (draftTitle.isNotBlank() && draftContent.isNotBlank()) {
                                viewModel.postNewAnnouncement(draftTitle, draftContent, draftPinned)
                                draftTitle = ""
                                draftContent = ""
                                draftPinned = false
                                showDraftForm = false
                            }
                        },
                        modifier = Modifier.testTag("announcement_submit_button")
                    ) {
                        Text("Publish Bulletin")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDraftForm = false }) {
                        Text("Discard")
                    }
                }
            )
        }
    }
}

@Composable
fun AnnouncementItem(announcement: Announcement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (announcement.isPinned) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = if (announcement.isPinned) 1.5.dp else 1.dp,
            color = if (announcement.isPinned) AcademicGold.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (announcement.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = AcademicGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = announcement.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (announcement.isPinned) ScholasticNavy else MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = announcement.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = announcement.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 22.sp
            )
        }
    }
}

// ==========================================
// TAB 4: STUDENT CARD & PROFILE
// ==========================================

@Composable
fun StudentCardTab(viewModel: LibraryViewModel) {
    val studentName by viewModel.studentName.collectAsState()
    val studentId by viewModel.studentId.collectAsState()
    val streak by viewModel.readingStreak.collectAsState()
    
    val allBorrowRecords by viewModel.allBorrowRecords.collectAsState()
    val checkedOutCount = allBorrowRecords.count { it.returnDate == null }
    val returnedCount = allBorrowRecords.count { it.returnDate != null }

    var draftName by remember { mutableStateOf(studentName) }
    var draftId by remember { mutableStateOf(studentId) }
    var isEditingProfile by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        VirtualLibraryCard(studentName, studentId)

        if (!isEditingProfile) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ACADEMIC ACCOMPLISHMENTS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(count = "$checkedOutCount", title = "Active Books")
                        StatItem(count = "$returnedCount", title = "Completed")
                        StatItem(count = "$streak", title = "Streak Days")
                    }
                }
            }

            Button(
                onClick = {
                    draftName = studentName
                    draftId = studentId
                    isEditingProfile = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("edit_student_profile_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Student Card Information", fontWeight = FontWeight.Bold)
            }

            // High-fidelity Enterprise Sync, Conflict Resolution, Security, SQL Backup options
            EnterpriseSyncConsole(viewModel)
        } else {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("edit_student_form"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "UPDATE STUDENT REGISTER",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )

                    OutlinedTextField(
                        value = draftName,
                        onValueChange = { draftName = it },
                        label = { Text("Full Name & Grade") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_name_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = draftId,
                        onValueChange = { draftId = it },
                        label = { Text("Student Identifier (ID)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_id_input"),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = { isEditingProfile = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                viewModel.updateStudentProfile(draftName, draftId)
                                isEditingProfile = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_student_profile_button")
                        ) {
                            Text("Save ID Card", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VirtualLibraryCard(studentName: String, studentId: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ScholasticNavyDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "OAKRIDGE HIGH SCHOOL",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AcademicGoldLight,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Official Student Library Pass",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = AcademicGoldLight,
                    modifier = Modifier.size(28.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = studentName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "ID: $studentId",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontFamily = FontFamily.Monospace
                    )
                }

                BarcodeWidget(modifier = Modifier.size(width = 110.dp, height = 45.dp))
            }
        }
    }
}

@Composable
fun BarcodeWidget(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(4.dp))
            .padding(vertical = 4.dp, horizontal = 6.dp)
    ) {
        val width = size.width
        val height = size.height

        val stripePattern = listOf(
            2f, 4f, 1f, 3f, 4f, 2f, 1f, 5f, 2f, 1f, 3f, 2f, 4f, 1f, 3f, 2f, 1f, 3f, 5f, 1f
        )
        val totalUnits = stripePattern.sum()
        val unitWidth = width / totalUnits

        var currentX = 0f
        stripePattern.forEachIndexed { index, stripe ->
            val isBlack = index % 2 == 0
            val stripeWidth = stripe * unitWidth
            if (isBlack) {
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(currentX, 0f),
                    size = androidx.compose.ui.geometry.Size(stripeWidth, height)
                )
            }
            currentX += stripeWidth
        }
    }
}

@Composable
fun StatItem(count: String, title: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontWeight = FontWeight.Medium
        )
    }
}

// ==========================================
// 1. DEMO ROLE CONSOLE
// ==========================================
@Composable
fun RoleSelectorRow(viewModel: LibraryViewModel) {
    val currentRole by viewModel.currentRole.collectAsState()
    val roles = UserRole.values()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(getGlassColor(isSystemInDarkTheme()))
            .border(getGlassBorder(isSystemInDarkTheme()))
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "DEMO ROLE CONSOLE (Tap to Switch Permissions)",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
            letterSpacing = 0.5.sp
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(roles) { role ->
                val isSelected = currentRole == role
                val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                
                Surface(
                    onClick = { viewModel.changeRole(role) },
                    shape = RoundedCornerShape(16.dp),
                    color = bgColor,
                    contentColor = contentColor,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag("role_pill_${role.name}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = when(role) {
                            UserRole.STUDENT -> Icons.Default.Person
                            UserRole.LIBRARIAN -> Icons.Default.LocalLibrary
                            UserRole.TEACHER -> Icons.Default.School
                            UserRole.ADMIN -> Icons.Default.ManageAccounts
                            UserRole.SUPER_ADMIN -> Icons.Default.AutoMode
                        }
                        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = role.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. BOOK LIST ITEM WITH OFFLINE SUPPORT
// ==========================================
@Composable
fun BookListItemWithOffline(
    book: Book,
    isCached: Boolean,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleCache: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("book_item_card_${book.id}")
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            BookCoverThumbnail(book = book, modifier = Modifier.size(width = 80.dp, height = 115.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(115.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CategoryBadge(category = book.category)
                        
                        // Cloud Cache Offline Action Button
                        IconButton(
                            onClick = { onToggleCache() },
                            modifier = Modifier.size(24.dp).testTag("offline_cache_btn_${book.id}")
                        ) {
                            Icon(
                                imageVector = if (isCached) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                                contentDescription = "Offline Cache",
                                tint = if (isCached) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "by ${book.author}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvailableBadge(copiesLeft = book.availableCopies)
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = AcademicGoldLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = book.rating.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = { onToggleFavorite() },
                            modifier = Modifier.size(28.dp).testTag("fave_btn_${book.id}")
                        ) {
                            Icon(
                                imageVector = if (book.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (book.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. ENTERPRISE SYNC, BACKUP & SECURITY
// ==========================================
@Composable
fun EnterpriseSyncConsole(viewModel: LibraryViewModel) {
    val cloudEnabled by viewModel.cloudSyncEnabled.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val queueCount by viewModel.syncQueueCount.collectAsState()
    val conflicts by viewModel.syncConflicts.collectAsState()
    
    var securityLockActive by remember { mutableStateOf(false) }
    var mockLogsVisible by remember { mutableStateOf(false) }
    var mockAppTamperedState by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Section Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CLOUD SYNC & ENTERPRISE SECURITY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (securityLockActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ) {
                    Text(
                        text = if (securityLockActive) "ENCRYPTED" else "OFFLINE LOCAL ONLY",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (securityLockActive) Color(0xFF2E7D32) else Color(0xFFC62828),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Cloud Synchronization Module
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Text("Optional Cloud Sync", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Switch(
                            checked = cloudEnabled,
                            onCheckedChange = { viewModel.toggleCloudSync(it) },
                            modifier = Modifier.testTag("cloud_sync_switch")
                        )
                    }

                    if (cloudEnabled) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "WorkManager Status: ${syncStatus.name} (${queueCount} in queue)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (syncStatus == SyncStatus.SYNCING) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            } else {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFE8F5E9), RoundedCornerShape(4.dp))
                                        .clickable { viewModel.triggerManualSync() }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text("SYNC QUEUE NOW", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                }
                            }
                        }
                    }
                }
            }

            // Conflict Resolution Module
            if (conflicts.isNotEmpty()) {
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFB74D))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
                            Text("Local-Server Data Conflict Detected", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFFE65100))
                        }
                        conflicts.forEach { conf ->
                            Text(conf.itemTitle, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text("Local: ${conf.localValue}", fontSize = 10.sp, color = Color.Gray)
                                Text("Server: ${conf.serverValue}", fontSize = 10.sp, color = Color.Gray)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.resolveConflict(conf.id, true) },
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                                ) {
                                    Text("Local Wins", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { viewModel.resolveConflict(conf.id, false) },
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                ) {
                                    Text("Server Wins", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Enterprise Security Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Secure PIN Authorization Protection", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Switch(
                    checked = securityLockActive,
                    onCheckedChange = {
                        securityLockActive = it
                        viewModel.addNotification(
                            "Security Changed",
                            if (it) "AES Encrypted secure passcode lock activated for your user session." else "Passcode protection cleared.",
                            "announcement"
                        )
                    },
                    modifier = Modifier.testTag("passcode_switch")
                )
            }

            // Database backup & crash log panels
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        viewModel.addNotification("SQL SQLITE BACKUP", "Device state backup exported to cloud safely (backup_v10.db).", "goal")
                    },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("EXP Backup", fontSize = 10.sp)
                }
                Button(
                    onClick = {
                        viewModel.addNotification("SQL RESTORE", "Database successfully restored from archive file (May 26 revision).", "announcement")
                    },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("IMP Restore", fontSize = 10.sp)
                }
                Button(
                    onClick = { mockLogsVisible = !mockLogsVisible },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.filledTonalButtonColors()
                ) {
                    Text(if (mockLogsVisible) "Hide Logs" else "Show Logs", fontSize = 10.sp)
                }
            }

            if (mockLogsVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("[SYSTEM] Starting anti-tampering inspection package status...", color = Color.Green, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("[SUCCESS] Certificate checksum: match digital workspace.", color = Color.Green, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("[INTEGRITY] Encrypted local database structures initialized cleanly.", color = Color.Green, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("[SESSION] Token lease: Valid. Remaining TTL = 239m.", color = Color.Cyan, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("[WORKMANAGER] Async periodic jobs synced and registered: Unit.", color = Color.Yellow, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("[LOGGING] 0 fatal/crash events recorded on stack.", color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. ROLE SPECIFIC CMS / CONTROL PANELS
// ==========================================
@Composable
fun RoleAdministrationDashboard(
    role: UserRole,
    viewModel: LibraryViewModel,
    onAddBookClicked: () -> Unit,
    onAssignClicked: () -> Unit,
    onOpenAdminPanel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val icon = when(role) {
                        UserRole.LIBRARIAN -> Icons.Default.AdminPanelSettings
                        UserRole.TEACHER -> Icons.Default.AssignmentInd
                        else -> Icons.Default.Dashboard
                    }
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        text = "${role.name} ADMINISTRATION DESK",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        letterSpacing = 1.sp
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "LIVE CONSOLE",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            when (role) {
                UserRole.LIBRARIAN -> {
                    Text("As a school librarian, you can modify inventory, scan barcode codes, and append books to students catalog feeds.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onAddBookClicked,
                            modifier = Modifier.weight(1f).height(38.dp).testTag("cms_add_book_shortcut_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mock Upload Book", fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                viewModel.addNotification("Scan Simulated", "Bar code check-in complete. 1 copy of Macbeth restocked.", "goal")
                            },
                            modifier = Modifier.weight(1f).height(38.dp),
                            colors = ButtonDefaults.filledTonalButtonColors()
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Scan RESTOCK", fontSize = 11.sp)
                        }
                    }
                }
                UserRole.TEACHER -> {
                    val assignments by viewModel.teacherAssignments.collectAsState()
                    Text("As an educator, assign books with homework quizzes and monitor student progress results below:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    
                    // Create homework assignments
                    Button(
                        onClick = onAssignClicked,
                        modifier = Modifier.fillMaxWidth().height(36.dp).testTag("cms_create_assignment_shortcut")
                    ) {
                        Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Assign Reading & Interactive Quiz", fontSize = 11.sp)
                    }

                    // Display list of student homework assigned
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Active Classroom Tasks:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        assignments.take(2).forEach { asg ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(asg.title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Book: ${asg.bookTitle} | Due: ${asg.dueDate}", fontSize = 9.sp, color = Color.Gray)
                                }
                                Text("${asg.completedCount}/${asg.totalCount} Done", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                UserRole.ADMIN, UserRole.SUPER_ADMIN -> {
                    Text("School analytics dashboards. Custom vector charts mapping library metric metrics:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    
                    // Beautiful custom Jetpack Compose Canvas charts
                    Row(modifier = Modifier.fillMaxWidth().height(100.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Chart Container
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                                .padding(8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Popularity by Category", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(6.dp))
                                Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp)) {
                                    val barWidth = 14.dp.toPx()
                                    val space = 10.dp.toPx()
                                    val heights = listOf(0.85f, 0.65f, 0.45f, 0.90f)
                                    val colors = listOf(Color(0xFF3F51B5), Color(0xFFE91E63), Color(0xFF009688), Color(0xFFFF9800))
                                    
                                    heights.forEachIndexed { idx, h ->
                                        val x = idx * (barWidth + space) + 5
                                        val barHeight = size.height * h
                                        val y = size.height - barHeight
                                        drawRect(
                                            color = colors[idx],
                                            topLeft = Offset(x, y),
                                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                                        )
                                    }
                                }
                            }
                        }

                        // Circular Target Gauge
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text("Monthly Goal", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(50.dp)) {
                                    Canvas(modifier = Modifier.size(44.dp)) {
                                        drawArc(
                                            color = Color.LightGray.copy(alpha = 0.3f),
                                            startAngle = 0f,
                                            sweepAngle = 360f,
                                            useCenter = false,
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                                        )
                                        drawArc(
                                            color = Color(0xFF4CAF50),
                                            startAngle = -90f,
                                            sweepAngle = 270f,
                                            useCenter = false,
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                        )
                                    }
                                    Text("75%", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
                UserRole.STUDENT -> {
                    // Student panel placeholder
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(2.dp))
            Button(
                onClick = onOpenAdminPanel,
                modifier = Modifier.fillMaxWidth().height(36.dp).testTag("access_saas_metrics_hq_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Access SaaS Administrator Controls & Metrics HQ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// 5. MOCK INTERACTIVE ACTION OVERLAYS
// ==========================================
@Composable
fun VoiceSearchDialog(onQueryExtracted: (String) -> Unit, onDismiss: () -> Unit) {
    var waveScale by remember { mutableStateOf(1f) }
    
    // Animate wave fluctuations
    LaunchedEffect(Unit) {
        while (true) {
            waveScale = (0.6f + Math.random() * 0.8).toFloat()
            kotlinx.coroutines.delay(150)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Listening...", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Speak title or topic (e.g. Gatsby, Science)", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, textAlign = TextAlign.Center)

                // Fluctuating waveform graphic
                Row(
                    modifier = Modifier.height(60.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(40, 25, 70, 50, 80, 55, 30).forEachIndexed { index, height ->
                        val animHeight = height.dp * waveScale
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(animHeight)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp))
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onQueryExtracted("Great Gatsby") },
                        modifier = Modifier.weight(1f).testTag("voice_mock_confirm")
                    ) {
                        Text("Mock Voice")
                    }
                }
            }
        }
    }
}

@Composable
fun BarcodeISBNScanDialog(viewModel: LibraryViewModel, onDismiss: () -> Unit) {
    var isScanning by remember { mutableStateOf(true) }
    var scanLineY by remember { mutableStateOf(0f) }

    // Animate scan laser line
    LaunchedEffect(Unit) {
        while (isScanning) {
            scanLineY = if (scanLineY < 180f) scanLineY + 6f else 0f
            kotlinx.coroutines.delay(20)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Barcode Scanner Integration", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Align barcode / ISBN identifier inside the laser viewfinder:", style = MaterialTheme.typography.bodySmall, color = Color.Gray, textAlign = TextAlign.Center)

                // Laser Viewfinder Mock
                Box(
                    modifier = Modifier
                        .size(width = 240.dp, height = 180.dp)
                        .background(Color.Black, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                ) {
                    // Animating red laser strip
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawLine(
                            color = Color.Red,
                            start = Offset(0f, scanLineY),
                            end = Offset(size.width, scanLineY),
                            strokeWidth = 3f.dp.toPx()
                        )
                    }
                    
                    Text(
                        "FINDER ACTIVE",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Barcode simulation trigger
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Dismiss")
                    }
                    Button(
                        onClick = {
                            viewModel.updateSearchQuery("978-0141439518") // Hamlet ISBN
                            viewModel.addNotification("ISBN Scanned", "Autodetected Hamlet barcode catalog ID 978-0141439518.", "goal")
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).testTag("barcode_mock_success_btn")
                    ) {
                        Text("Mock Scan")
                    }
                }
            }
        }
    }
}

@Composable
fun CMSAddBookDialog(viewModel: LibraryViewModel, onDismiss: () -> Unit) {
    var txtTitle by remember { mutableStateOf("") }
    var txtAuthor by remember { mutableStateOf("") }
    var txtCategory by remember { mutableStateOf("Literature") }
    var txtDesc by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("CMS: Catalog Book Upload", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                
                OutlinedTextField(
                    value = txtTitle,
                    onValueChange = { txtTitle = it },
                    label = { Text("Book Title") },
                    modifier = Modifier.fillMaxWidth().testTag("cms_title"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = txtAuthor,
                    onValueChange = { txtAuthor = it },
                    label = { Text("Author") },
                    modifier = Modifier.fillMaxWidth().testTag("cms_author"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = txtDesc,
                    onValueChange = { txtDesc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (txtTitle.isNotBlank()) {
                                viewModel.uploadMaterial(txtTitle, MaterialType.NOTES, txtCategory, true)
                                viewModel.updateSearchQuery(txtTitle)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1.2f).testTag("cms_submit_book")
                    ) {
                        Text("Upload Catalog", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CMSCreateAssignmentDialog(viewModel: LibraryViewModel, onDismiss: () -> Unit) {
    var txtAsgTitle by remember { mutableStateOf("") }
    var txtAsgBookId by remember { mutableStateOf("1") }
    var txtInstructions by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Create Reading Task Assignment", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)

                OutlinedTextField(
                    value = txtAsgTitle,
                    onValueChange = { txtAsgTitle = it },
                    label = { Text("Assignment Header") },
                    modifier = Modifier.fillMaxWidth().testTag("asg_title"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = txtInstructions,
                    onValueChange = { txtInstructions = it },
                    label = { Text("Instructions") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (txtAsgTitle.isNotBlank()) {
                                viewModel.createAssignment(txtAsgTitle, txtAsgBookId, "To Kill a Mockingbird", "Harper Lee", "June 05", txtInstructions)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1.2f).testTag("asg_submit_btn")
                    ) {
                        Text("Publish Task", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. AI LIBRARIAN ASSISTANT TAB
// ==========================================
@Composable
fun AILibrarianTab(viewModel: LibraryViewModel) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val typingStatus by viewModel.aiIsTyping.collectAsState()
    var inputQuery by remember { mutableStateOf("") }

    var selectedAttachmentName by remember { mutableStateOf<String?>(null) }
    var selectedAttachmentMime by remember { mutableStateOf<String?>(null) }
    var showAttachmentDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // AI Intro Header CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                }
                Column {
                    Text("Librarian Auden", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Powered by Gemini 3.5 Flash Model", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat Bubble Scroll Area
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(chatMessages) { msg ->
                val isGemini = msg.sender == "gemini"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isGemini) Arrangement.Start else Arrangement.End
                ) {
                    Card(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isGemini) 4.dp else 16.dp,
                            bottomEnd = if (isGemini) 16.dp else 4.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isGemini) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (msg.attachmentName != null) {
                                Row(
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Text(msg.attachmentName, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                            
                            // Markdown Simulation parsing
                            Text(
                                text = msg.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isGemini) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                            )
                        }
                    }
                }
            }

            if (typingStatus) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        ) {
                            Text(
                                "Auden is writing...",
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick prompts helper cards (summaries, quizzes, flashcards)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 6.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .clickable { viewModel.selectQueryFromAI("Summarize Act I of Hamlet") }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Summarize Hamlet", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .clickable { viewModel.selectQueryFromAI("Create a practice comprehension quiz on To Kill a Mockingbird") }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Practice Quiz Mockingbird", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .clickable { viewModel.selectQueryFromAI("Generate some handy study flashcards on English Literary Devices") }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Literary Device Flashcards", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Active attachment indicator
        if (selectedAttachmentName != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(selectedAttachmentName ?: "", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("Ready for OCR / Prompt analysis", fontSize = 8.sp, color = Color.Gray)
                    }
                }
                IconButton(onClick = {
                    selectedAttachmentName = null
                    selectedAttachmentMime = null
                }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Clear attachment", modifier = Modifier.size(14.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Input Field and Sender actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Attachment Selector trigger
            IconButton(
                onClick = { showAttachmentDialog = true },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape).testTag("ai_attach_btn")
            ) {
                Icon(Icons.Default.AttachFile, contentDescription = "Attach file", tint = MaterialTheme.colorScheme.primary)
            }

            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                modifier = Modifier.weight(1f).testTag("ai_input_text"),
                placeholder = { Text("Ask Librarian Auden...") },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            IconButton(
                onClick = {
                    if (inputQuery.isNotBlank() || selectedAttachmentName != null) {
                        viewModel.sendChatMessage(inputQuery, selectedAttachmentName, selectedAttachmentMime)
                        inputQuery = ""
                        selectedAttachmentName = null
                        selectedAttachmentMime = null
                    }
                },
                modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape).testTag("ai_send_btn")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }

    if (showAttachmentDialog) {
        Dialog(onDismissRequest = { showAttachmentDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Classroom Document Box", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Text("Simulate uploading homework, PDFs, or photos for instant OCR conversion extraction:", fontSize = 11.sp, color = Color.Gray)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedAttachmentName = "Hamlet_Notes_Summary.pdf"
                                    selectedAttachmentMime = "application/pdf"
                                    viewModel.addNotification("OCR Loaded", "Autodetected text lines extracted from Hamlet_Notes_Summary.pdf via OCR successfully.", "goal")
                                    showAttachmentDialog = false
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.Red)
                            Text("Attach Hamlet_Notes_Summary.pdf", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedAttachmentName = "Mockingbird_Extract_Scan.png"
                                    selectedAttachmentMime = "image/png"
                                    viewModel.addNotification("OCR Extract", "OCR converted scanned picture image to text.", "goal")
                                    showAttachmentDialog = false
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = Color.Blue)
                            Text("Attach Mockingbird_Extract_Scan.png", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    TextButton(onClick = { showAttachmentDialog = false }, modifier = Modifier.align(Alignment.End)) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

// ==========================================
// SAAS GLASSMORPHISM & GENERAL STYLIZATION HOOKS
// ==========================================

@Composable
fun getGlassColor(isDark: Boolean): Color {
    return if (isDark) {
        Color(0xC010172A) // Rich Translucent Dark Slate
    } else {
        Color(0xC0FAF9F6) // Elegant Translucent Parchment White
    }
}

@Composable
fun getGlassBorder(isDark: Boolean): BorderStroke {
    return BorderStroke(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = if (isDark) {
                listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.04f))
            } else {
                listOf(Color.Black.copy(alpha = 0.14f), Color.Black.copy(alpha = 0.03f))
            }
        )
    )
}

// ==========================================
// SAAS ADMIN CONTROL CENTER & COMPLIANCE
// ==========================================

@Composable
fun SaaSAdminControlCenterDialog(
    viewModel: LibraryViewModel,
    onDismiss: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    var selectedTabState by remember { mutableStateOf("Analytics") }
    
    // SaaS State flows
    val userList by viewModel.userModerationProfiles.collectAsState()
    val overdueList by viewModel.overdueItems.collectAsState()
    val storageMetrics by viewModel.systemStorageMetrics.collectAsState()
    val booksList by viewModel.books.collectAsState()
    
    // Form fields for announcer
    var alertTitle by remember { mutableStateOf("") }
    var alertContent by remember { mutableStateOf("") }
    var targetAudience by remember { mutableStateOf("All Campus") }
    
    // Filter active student name query for user caring
    var userSearchQuery by remember { mutableStateOf("") }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("saas_admin_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = getGlassColor(isDark)),
            border = getGlassBorder(isDark)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(18.dp)) {
                // Top Header Section
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = "SaaS MULTITENANCY SYSTEM",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "Campus Logistics & Admin Suite",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_admin_dialog_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Dashboard")
                    }
                }
                
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                
                // Horizontal navigation rail of metrics tabs
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val adminTabs = listOf("Analytics", "Moderation", "Inventory", "Overdues", "Announcer", "Data & API")
                    items(adminTabs) { tab ->
                        val isSelected = selectedTabState == tab
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTabState = tab },
                            label = { Text(tab, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("admin_tab_$tab")
                        )
                    }
                }
                
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (selectedTabState) {
                        "Analytics" -> {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item {
                                    Text("CAMPUS LOGISTICS PERFORMANCE OVERVIEW", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("This panel visualizes library utilization across categories and peak checking schedules. Rendered natively using Canvas vectors.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                }
                                
                                item {
                                    // Custom high-fidelity bar chart representing category loans
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                                        border = getGlassBorder(isDark)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text("Checkouts Volumes by Department", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(14.dp))
                                            Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                                                Canvas(modifier = Modifier.fillMaxSize()) {
                                                    val bars = listOf(
                                                        "Lit" to 85f, "Fic" to 60f, "Sci" to 72f, "His" to 42f, "Gro" to 91f
                                                    )
                                                    val spacing = 24.dp.toPx()
                                                    val barWidth = 32.dp.toPx()
                                                    val totalWidth = bars.size * (barWidth + spacing) - spacing
                                                    val startX = (size.width - totalWidth) / 2
                                                    
                                                    bars.forEachIndexed { index, pair ->
                                                        val x = startX + index * (barWidth + spacing)
                                                        val barHeight = size.height * (pair.second / 100f)
                                                        val y = size.height - barHeight
                                                        drawRect(
                                                            color = ScholasticNavy.copy(alpha = 0.85f),
                                                            topLeft = Offset(x, y),
                                                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                                                        )
                                                    }
                                                }
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Lit", fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                                Text("Fic", fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                                Text("Sci", fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                                Text("His", fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                                Text("Self", fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                            }
                                        }
                                    }
                                }
                                
                                item {
                                    // Sub-grid of KPI indicators
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Card(
                                            modifier = Modifier.weight(1f),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
                                            border = getGlassBorder(isDark)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text("CACHE HEALTH", fontSize = 10.sp, color = AcademicGoldText, fontWeight = FontWeight.Bold)
                                                Text("94.2%", fontSize = 18.sp, fontWeight = FontWeight.Black)
                                                Text("Clean Offline hits", fontSize = 9.sp, color = Color.Gray)
                                            }
                                        }
                                        Card(
                                            modifier = Modifier.weight(1f),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
                                            border = getGlassBorder(isDark)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text("SYNC LATENCY", fontSize = 10.sp, color = Color(0xFF0F766E), fontWeight = FontWeight.Bold)
                                                Text("132 ms", fontSize = 18.sp, fontWeight = FontWeight.Black)
                                                Text("WorkManager queue", fontSize = 9.sp, color = Color.Gray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        "Moderation" -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text("STUDENT PROFILES MODERATION CONSOLE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                OutlinedTextField(
                                    value = userSearchQuery,
                                    onValueChange = { userSearchQuery = it },
                                    placeholder = { Text("Filter student directory...") },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("moderator_search_input"),
                                    singleLine = true
                                )
                                
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    val filteredUsers = userList.filter {
                                        it.name.contains(userSearchQuery, ignoreCase = true) || it.id.contains(userSearchQuery, ignoreCase = true)
                                    }
                                    items(filteredUsers) { user ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                                            border = getGlassBorder(isDark)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                        Surface(
                                                            color = when (user.status) {
                                                                "Active" -> Color(0xFFE8F5E9)
                                                                "Flagged" -> Color(0xFFFFF3E0)
                                                                else -> Color(0xFFFFEBEE)
                                                            },
                                                            contentColor = when (user.status) {
                                                                "Active" -> Color(0xFF2E7D32)
                                                                "Flagged" -> Color(0xFFE65100)
                                                                else -> Color(0xFFC62828)
                                                            },
                                                            shape = RoundedCornerShape(4.dp)
                                                        ) {
                                                            Text(user.status.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                                        }
                                                    }
                                                    Text("ID: ${user.id} | Email: ${user.email} | Flagged Reviews: ${user.flaggedCount}", fontSize = 10.sp, color = Color.Gray)
                                                }
                                                // Moderation buttons
                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    if (user.status != "Active") {
                                                        TextButton(
                                                            onClick = { viewModel.updateUserStatus(user.id, "Active") },
                                                            modifier = Modifier.testTag("mod_activate_${user.id}")
                                                        ) {
                                                            Text("Verify", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
                                                        }
                                                    }
                                                    if (user.status != "Suspended") {
                                                        TextButton(
                                                            onClick = { viewModel.updateUserStatus(user.id, "Suspended") },
                                                            modifier = Modifier.testTag("mod_suspend_${user.id}")
                                                        ) {
                                                            Text("Mute", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        "Inventory" -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text("OAKRIDGE INVENTORY & COPIES CONTROL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Manage catalog counts, monitor physical copies, and report/flag material damages.", fontSize = 11.sp, color = Color.Gray)
                                
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f).padding(top = 10.dp)
                                ) {
                                    items(booksList) { book ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                                            border = getGlassBorder(isDark)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(book.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text("by ${book.author} | Stock: ${book.availableCopies} avail / ${book.totalCopies} total", fontSize = 10.sp, color = Color.Gray)
                                                }
                                                
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    IconButton(
                                                        onClick = { viewModel.addNewBookCopy(book.id) },
                                                        modifier = Modifier.size(32.dp).testTag("inv_add_${book.id}")
                                                    ) {
                                                        Icon(Icons.Default.AddCircleOutline, contentDescription = "Add copy", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                                    }
                                                    IconButton(
                                                        onClick = { viewModel.flagBookDamage(book.id) },
                                                        modifier = Modifier.size(32.dp).testTag("inv_damage_${book.id}")
                                                    ) {
                                                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Report damage", tint = Color.Red, modifier = Modifier.size(20.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        "Overdues" -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text("OVERDUE COPIES & PENALTY MONITORING", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Review student materials past check-out periods, accrue automated fines, and transmit push warnings.", fontSize = 11.sp, color = Color.Gray)
                                
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f).padding(top = 10.dp)
                                ) {
                                    items(overdueList) { ov ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                                            border = getGlassBorder(isDark)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(ov.studentName, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                                    Text("Overdue Item: ${ov.bookTitle}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                                    Text("Days Overdue: ${ov.daysOverdue} days | Fines: $${String.format(Locale.US, "%.2f", ov.fineAccrued)}", fontSize = 10.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                                }
                                                
                                                Button(
                                                    onClick = { viewModel.triggerOverdueWarning(ov.id) },
                                                    modifier = Modifier.height(32.dp).testTag("warn_student_${ov.id}"),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                                                ) {
                                                    Text("Warn", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        "Announcer" -> {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item {
                                    Text("COMPOSE CAMPUS-WIDE ANNOUNCEMENT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                
                                item {
                                    OutlinedTextField(
                                        value = alertTitle,
                                        onValueChange = { alertTitle = it },
                                        placeholder = { Text("Announcement Title...") },
                                        modifier = Modifier.fillMaxWidth().testTag("ann_title_input"),
                                        singleLine = true
                                    )
                                }
                                
                                item {
                                    OutlinedTextField(
                                        value = alertContent,
                                        onValueChange = { alertContent = it },
                                        placeholder = { Text("Details and guidelines message contents...") },
                                        modifier = Modifier.fillMaxWidth().height(90.dp).testTag("ann_content_input")
                                    )
                                }
                                
                                item {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("Audience:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        val filterAudiences = listOf("All Campus", "Students Only", "Teachers Only")
                                        filterAudiences.forEach { aud ->
                                            val activeAud = targetAudience == aud
                                            FilterChip(
                                                selected = activeAud,
                                                onClick = { targetAudience = aud },
                                                label = { Text(aud, fontSize = 9.sp) }
                                            )
                                        }
                                    }
                                }
                                
                                item {
                                    Button(
                                        onClick = {
                                            if (alertTitle.isNotBlank()) {
                                                viewModel.postNewAnnouncement(alertTitle, "$alertContent (Targeted: $targetAudience)", true)
                                                alertTitle = ""
                                                alertContent = ""
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("publish_ann_btn"),
                                        enabled = alertTitle.isNotBlank()
                                    ) {
                                        Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Publish and Pin Announcement")
                                    }
                                }
                            }
                        }
                        
                        "Data & API" -> {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item {
                                    Text("CLOUD PLATFORM, STORAGE OPTIMIZATIONS & EXPORTS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
                                        border = getGlassBorder(isDark)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("Cached Media Files Distribution", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(10.dp))
                                            storageMetrics.forEach { stat ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(stat.category, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text(stat.sizeLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        Text(stat.compressionRatio, fontSize = 8.sp, color = Color.Gray)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                item {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { viewModel.compressMediaFiles() },
                                            modifier = Modifier.weight(1f).testTag("trigger_compression_btn"),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                        ) {
                                            Icon(Icons.Default.Compress, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Media Compress", fontSize = 10.sp)
                                        }
                                        Button(
                                            onClick = { viewModel.runStorageCleanup() },
                                            modifier = Modifier.weight(1f).testTag("clean_cache_btn"),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                        ) {
                                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Clear Caches", fontSize = 10.sp)
                                        }
                                    }
                                }
                                
                                item {
                                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                    Text("FUTURE SCALABILITY CONSOLE (SIS COMPLIANCE)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Connect this library to Web Dashboards, Multi-School Databases, and Student Information Systems (SIS).", fontSize = 10.sp, color = Color.Gray)
                                }
                                
                                item {
                                    Button(
                                        onClick = {
                                            viewModel.addNotification("CSV Compiled", "Generated school compliance metrics and checkout spreadsheets. Ready for SIS download.", "announcement")
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("export_csv_btn")
                                    ) {
                                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Compile & Export Audit Reports (CSV/JSON)")
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

// =========================================================================
// SECTION 5: CLASS-BASED ACADEMIC CURRICULUM WORKSPACE (HIGH FIDELITY COMPOSED)
// =========================================================================

@Composable
fun AcademicCurriculumWorkspace(viewModel: LibraryViewModel) {
    val selectedClass by viewModel.selectedClassLevel.collectAsState()
    val selectedSubjectId by viewModel.selectedSubjectId.collectAsState()
    val personalFilter by viewModel.personalAssignedSubjectsOnly.collectAsState()
    val allSubjects by viewModel.allAcademicSubjects.collectAsState()
    val curriculumActiveTab by viewModel.curriculumActiveTab.collectAsState()
    
    // Derived filtering based on personalized assigned class
    val filteredSubjects = if (personalFilter) {
        allSubjects.filter { it.classLevel == AcademicClassLevel.S3 }
    } else {
        allSubjects.filter { it.classLevel == selectedClass }
    }

    val selectedSubject = allSubjects.find { it.id == selectedSubjectId }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // PERSONALIZED HERO BOX & EXAM REMINDER COUNTDOWNS
        item {
            AcademicInteractiveHeroHeader(viewModel)
        }

        // DYNAMIC ACADEMIC TABS BAR (REVISION / MOCK EXAM SIMULATOR / LEADERBOARD ACHIEVEMENTS)
        item {
            TabRow(
                selectedTabIndex = curriculumActiveTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[curriculumActiveTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(vertical = 4.dp)
            ) {
                Tab(
                    selected = curriculumActiveTab == 0,
                    onClick = { viewModel.curriculumActiveTab.value = 0 },
                    text = { Text("Revision Workspace", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    modifier = Modifier.testTag("curric_tab_revision")
                )
                Tab(
                    selected = curriculumActiveTab == 1,
                    onClick = { viewModel.curriculumActiveTab.value = 1 },
                    text = { Text("UNEB Trial Mock", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    modifier = Modifier.testTag("curric_tab_uneb")
                )
                Tab(
                    selected = curriculumActiveTab == 2,
                    onClick = { viewModel.curriculumActiveTab.value = 2 },
                    text = { Text("Gamified Levels", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    modifier = Modifier.testTag("curric_tab_gamified")
                )
            }
        }

        if (curriculumActiveTab == 0) {
            // CLASS LEVEL SELECTOR & PERSONAL CHIP COMBOS
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Explore Oakridge Curriculum",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // My Assigned S3 Subjects filter checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .clickable { viewModel.personalAssignedSubjectsOnly.value = !personalFilter }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp))
                                .background(
                                    if (personalFilter) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(3.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (personalFilter) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                            }
                        }
                        Text("My Assigned S3 Only", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                if (!personalFilter) {
                    // LazyRow of Classes S1 to S6
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().testTag("class_bar_row"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(AcademicClassLevel.values()) { level ->
                            val isSelected = selectedClass == level
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.selectAcademicClass(level) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = level.label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (level == AcademicClassLevel.S3) {
                                        Text(
                                            "Your Class",
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) AcademicGoldLight else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Personalized Mode Active: Showing matches for Senior 3 student Alex Rivera. Toggle the filter above to browse other classes S1-S6.",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // SUBJECTS FOR SELECTED CLASS
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Core Academic Subjects",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                if (filteredSubjects.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No subjects available for this combination.", color = Color.Gray, fontSize = 11.sp)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredSubjects.forEach { subject ->
                            val isSelected = selectedSubject?.id == subject.id
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.selectAcademicSubject(subject.id) }
                                    .testTag("subj_card_${subject.id}"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = subject.name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Book,
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = subject.description,
                                        fontSize = 9.sp,
                                        color = Color.Gray,
                                        maxLines = 2,
                                        lineHeight = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                subject.teachers.firstOrNull()?.name?.takeLast(5)?.take(1) ?: "T",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text(
                                            text = subject.teachers.firstOrNull()?.name ?: "Tutor",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // CHOSEN SUBJECT WORKSPACE & INTERACTIVE RESOURCE MANAGER
        if (selectedSubject != null) {
            item {
                AcademicWorkspaceDetailModule(viewModel, selectedSubject)
            }
        }

        // SMART STUDY SUITE & PERSONALIZED PLANNER
        item {
            AcademicSmartStudyPlannerSuite(viewModel)
        }

        // REAL-TIME COMPLIANCE ANALYTICS METER
        item {
            AcademicRealTimeAnalyticsDashboard(viewModel)
        }

        } else if (curriculumActiveTab == 1) {
            item {
                UNEBExamEngineCardWorkspace(viewModel)
            }
        } else {
            item {
                UgandanScholarGamificationHub(viewModel)
            }
        }

        // FUTURE SCALABILITY SCRATCHPAD
        item {
            AcademicFutureScalabilityPortal(viewModel)
        }
    }
}

// -------------------------------------------------------------
// CHILD COMPOSABLE: INTERACTIVE ACADEMIC HERO HEADER
// -------------------------------------------------------------
@Composable
fun AcademicInteractiveHeroHeader(viewModel: LibraryViewModel) {
    val countdowns by viewModel.examCountdownList.collectAsState()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Alex's Revision Deck",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "Personalized Study Space & Exam Hub",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Class: S3", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(10.dp))

            // National UNEB & Mock exam countdown timer cards
            Text(
                "UPCOMING ACADEMIC EXAM COUNTDOWNS",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                countdowns.take(2).forEach { cd ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(cd.title, fontSize = 8.sp, fontWeight = FontWeight.Bold, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
                            Text(cd.examDate, fontSize = 7.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = Color.Red, modifier = Modifier.size(10.dp))
                                Text("${cd.daysRemaining} Days Left", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// CHILD COMPOSABLE: DETAILED SUBJECT WORKSPACE DETAIL HUB
// -------------------------------------------------------------
@Composable
fun AcademicWorkspaceDetailModule(viewModel: LibraryViewModel, subject: Subject) {
    val selectedCategory by viewModel.selectedResourceCategory.collectAsState()
    val offlineSaved by viewModel.offlineSavedResources.collectAsState()
    val activeRes by viewModel.activeAcademicResource.collectAsState()
    
    // AI states
    val sumResult by viewModel.simulatedAISummaryResult.collectAsState()
    val isGenLoading by viewModel.generatorLoading.collectAsState()
    val activeSummaryPanel by viewModel.showAISummaryPanel.collectAsState()
    
    // Quiz states
    val showQuiz by viewModel.showAIQuizSimulator.collectAsState()
    
    // Categories for subjects
    val subjectCategories = ResourceCategory.values()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Teacher assignment header block
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(subject.teachers.firstOrNull()?.name?.takeLast(5)?.take(1) ?: "T", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Column {
                        Text(subject.name + " Workspace", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Course Director: ${subject.teachers.firstOrNull()?.name ?: "Mr. Okello"}", fontSize = 9.sp, color = Color.Gray)
                    }
                }

                // AI summary / Quiz buttons triggers
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = { viewModel.showAISummaryPanel.value = !activeSummaryPanel },
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Summarizer", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = { viewModel.showAIQuizSimulator.value = !showQuiz },
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = "AI Quiz", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // AI SUMMARY SECTION EXPANDABLE
            if (activeSummaryPanel) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Fast AI Exam Companion (Gemini Inside)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            IconButton(onClick = { viewModel.showAISummaryPanel.value = false }, modifier = Modifier.size(18.dp)) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp))
                            }
                        }

                        if (sumResult == null && !isGenLoading) {
                            Text("Need a quick syllabus summary or exam pointer? Ask our simulated Gemini server to outline core topics.", fontSize = 10.sp, color = Color.Gray)
                            Button(
                                onClick = { viewModel.generateAISubjectSummary(subject.name) },
                                modifier = Modifier.fillMaxWidth().height(28.dp).testTag("gen_ai_summary_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Generate Smart Crash Summary", fontSize = 10.sp)
                            }
                        } else if (isGenLoading) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyzing syllabus matrices & past UNEB questions...", fontSize = 10.sp)
                            }
                        } else {
                            Text(sumResult ?: "", fontSize = 9.sp, lineHeight = 12.sp, modifier = Modifier.padding(vertical = 4.dp))
                            Button(
                                onClick = { viewModel.generateAISubjectSummary(subject.name) },
                                modifier = Modifier.fillMaxWidth().height(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Regenerate outline", fontSize = 9.sp)
                            }
                        }
                    }
                }
            }

            // AI ACTIVE QUIZ INTERFACE SELECTOR
            if (showQuiz) {
                AcademicQuizInteractiveDeck(viewModel)
            }

            // LEVEL CHIPS ROW FOR SUBJECT RESOURCE SELECTOR
            Text("SELECT ACADEMIC FILE CATEGORY", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    val isAllSelected = selectedCategory == null
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isAllSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.selectedResourceCategory.value = null }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("All Files", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isAllSelected) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                }

                items(subjectCategories) { cat ->
                    val isCatSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isCatSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.selectedResourceCategory.value = cat }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(cat.label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isCatSelected) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            // DISPLAY AREA FILTERS
            val filteredResources = subject.resources.filter {
                selectedCategory == null || it.category == selectedCategory
            }

            if (selectedCategory == ResourceCategory.FLASHCARDS) {
                AcademicSubjectFlashcardDeck(viewModel)
            } else if (selectedCategory == ResourceCategory.DISCUSSION_THREADS) {
                AcademicForumDiscussionDeck(viewModel, subject.id)
            } else {
                if (filteredResources.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No items loaded under this category.", color = Color.Gray, fontSize = 10.sp)
                    }
                } else {
                    filteredResources.forEach { res ->
                        val isOffline = offlineSaved.contains(res.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(0.7f)) {
                                Text(res.title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(res.category.label, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text("•", fontSize = 8.sp, color = Color.Gray)
                                    Text(res.sizeLabel, fontSize = 8.sp, color = Color.Gray)
                                    if (isOffline) {
                                        Text("•", fontSize = 8.sp, color = Color.Gray)
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(8.dp))
                                        Text("Saved Offline", fontSize = 8.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Row(modifier = Modifier.weight(0.3f), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                // Save toggle offline
                                IconButton(onClick = { viewModel.toggleOfflineSavedResource(res.id) }, modifier = Modifier.size(28.dp)) {
                                    Icon(
                                        imageVector = if (isOffline) Icons.Default.CloudQueue else Icons.Default.CloudDownload,
                                        contentDescription = "Save Offline",
                                        tint = if (isOffline) MaterialTheme.colorScheme.primary else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Button(
                                    onClick = { viewModel.selectAcademicResource(res) },
                                    modifier = Modifier.height(26.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("Open", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ACADEMIC MULTIMEDIA FILE VIEWER OVERLAY
    if (activeRes != null) {
        AcademicUniversalReaderOverlay(viewModel, activeRes!!) {
            viewModel.selectAcademicResource(null)
        }
    }
}

// -------------------------------------------------------------
// PLAYABLE INTERACTIVE ACADEMIC EXAM PREP / QUIZ SIMULATOR
// -------------------------------------------------------------
@Composable
fun AcademicQuizInteractiveDeck(viewModel: LibraryViewModel) {
    val questions by viewModel.currentQuizQuestions.collectAsState()
    val activeIdx by viewModel.currentQuizIndex.collectAsState()
    val activeScore by viewModel.currentQuizScore.collectAsState()
    val chosenAnswer by viewModel.selectedQuizAnswerIndex.collectAsState()
    val isDone by viewModel.quizCompletedStatus.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                    Text("Interactive Revision Challenge", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
                IconButton(onClick = { viewModel.showAIQuizSimulator.value = false }, modifier = Modifier.size(18.dp)) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp))
                }
            }

            if (!isDone) {
                val q = questions.getOrNull(activeIdx)
                if (q != null) {
                    // Progress bar indication
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Question ${activeIdx + 1} of ${questions.size}", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("Score: $activeScore", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        rowQuizProgressDots(activeIdx, questions.size)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(q.questionText, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        q.optionsList.forEachIndexed { idx, option ->
                            val isChosen = chosenAnswer == idx
                            val selectCompleted = chosenAnswer != null
                            val isCorrectIdx = idx == q.correctIndex

                            val rowColor = when {
                                !selectCompleted -> if (isChosen) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
                                isCorrectIdx -> Color(0xFFD1FAE5) // light green correct match
                                isChosen && !isCorrectIdx -> Color(0xFFFEE2E2) // light red wrong selection
                                else -> MaterialTheme.colorScheme.surface
                            }
                            
                            val textColor = when {
                                isCorrectIdx && selectCompleted -> Color(0xFF065F46)
                                isChosen && !isCorrectIdx && selectCompleted -> Color(0xFF991B1B)
                                else -> MaterialTheme.colorScheme.onSurface
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(rowColor, RoundedCornerShape(8.dp))
                                    .border(1.dp, if (isChosen) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .clickable(enabled = !selectCompleted) { viewModel.submitQuizAnswer(idx) }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(option, fontSize = 10.sp, color = textColor, fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal)
                                if (selectCompleted && isCorrectIdx) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(10.dp))
                                }
                            }
                        }
                    }

                    if (chosenAnswer != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text("Explanation: ${q.rationale}", fontSize = 9.sp, color = Color.DarkGray)
                        }

                        Button(
                            onClick = { viewModel.advanceQuizQuestion() },
                            modifier = Modifier.fillMaxWidth().height(28.dp).testTag("advance_quiz_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text(if (activeIdx + 1 < questions.size) "Next Question" else "Finish Challenge", fontSize = 10.sp)
                        }
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = AcademicGoldLight, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Revision Deck Completed!", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("You scored $activeScore out of ${questions.size} correct answers.", fontSize = 10.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.resetQuizModule() },
                        modifier = Modifier.height(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Retake Challenge", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun rowQuizProgressDots(active: Int, total: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 0 until total) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(
                        color = if (i <= active) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

// -------------------------------------------------------------
// INTERACTIVE COMPOSABLE: SUBJECT FLASHCARDS INTERACTIVE DECK
// -------------------------------------------------------------
@Composable
fun AcademicSubjectFlashcardDeck(viewModel: LibraryViewModel) {
    val flashcards by viewModel.activeFlashcardList.collectAsState()
    var flashIndex by remember { mutableStateOf(0) }
    var showAnswer by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Biology Revision Flashcard (${flashIndex + 1} of ${flashcards.size})", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Icon(Icons.Default.HelpCenter, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
            }

            val item = flashcards.getOrNull(flashIndex)
            if (item != null) {
                // Flashcard Core Flip Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(
                            if (showAnswer) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else Color.White,
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .clickable { showAnswer = !showAnswer }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (showAnswer) "ANSWER:" else "QUESTION:",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (showAnswer) item.answer else item.question,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (showAnswer) "(Click card to show question)" else "(Click card to reveal answer)",
                            fontSize = 7.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Cards Navigation Toggles
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(
                        onClick = {
                            if (flashIndex > 0) {
                                flashIndex--
                                showAnswer = false
                            }
                        },
                        enabled = flashIndex > 0,
                        modifier = Modifier.height(26.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("Prev", fontSize = 9.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = {
                                viewModel.addNotification("Card Flagged", "Marked flashcard as masterfully known. Reinforcements saved.", "goal")
                                if (flashIndex + 1 < flashcards.size) {
                                    flashIndex++
                                    showAnswer = false
                                }
                            },
                            modifier = Modifier.height(26.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("Know It", fontSize = 9.sp)
                        }
                    }

                    Button(
                        onClick = {
                            if (flashIndex + 1 < flashcards.size) {
                                flashIndex++
                                showAnswer = false
                            }
                        },
                        enabled = flashIndex + 1 < flashcards.size,
                        modifier = Modifier.height(26.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("Next", fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// INTERACTIVE COMPOSABLE: DISCUSSION CLASS FORUMS
// -------------------------------------------------------------
@Composable
fun AcademicForumDiscussionDeck(viewModel: LibraryViewModel, subjectId: String) {
    val discussions by viewModel.subjectDiscussionForum.collectAsState()
    var userMsgText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Subject Q&A Forum Threads", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(Color(0xFF10B981), CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("4 Students active now", fontSize = 8.sp, color = Color.Gray)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.heightIn(max = 140.dp)) {
                discussions.forEach { msg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(android.graphics.Color.parseColor(msg.avatarColorHex)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(msg.userName.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(msg.userName, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("${msg.minutesAgo}m ago", fontSize = 7.sp, color = Color.LightGray)
                            }
                            Text(msg.messageText, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 11.sp)
                        }
                    }
                }
            }

            // Quick reply input text field
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userMsgText,
                    onValueChange = { userMsgText = it },
                    placeholder = { Text("Ask teacher or classmate...", fontSize = 9.sp) },
                    modifier = Modifier.weight(1f).height(38.dp).testTag("forum_chat_field"),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 10.sp),
                    shape = RoundedCornerShape(8.dp)
                )

                Button(
                    onClick = {
                        viewModel.addForumMessage(subjectId, userMsgText)
                        userMsgText = ""
                    },
                    modifier = Modifier.height(34.dp).testTag("forum_send_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Text("Post", fontSize = 9.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// CHILD COMPOSABLE: UNIVERSAL ACADEMIC FILE READER OVERLAY (Frosted)
// -------------------------------------------------------------
@Composable
fun AcademicUniversalReaderOverlay(viewModel: LibraryViewModel, resource: AcademicResource, onClose: () -> Unit) {
    var bookmarkChecked by remember { mutableStateOf(false) }
    var scaleZoom by remember { mutableStateOf(1.0f) }
    var highlightedNoteText by remember { mutableStateOf("") }
    var savedHighlightsList by remember { mutableStateOf(listOf<String>()) }

    Dialog(onDismissRequest = onClose) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(4.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Header of document
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Text(
                            text = resource.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.width(180.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                bookmarkChecked = !bookmarkChecked
                                viewModel.addNotification("Bookmark Saved", "Pinned page marker inside: ${resource.title}.", "goal")
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (bookmarkChecked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (bookmarkChecked) AcademicGoldLight else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // AUDEN AUDIO VOICE BAR NATIVE SPEECH SYNTHESIS ENGINE
                val isTtsActive by viewModel.isTtsActive.collectAsState()
                val activeTtsRes by viewModel.activeTtsResource.collectAsState()
                val ttsSpeed by viewModel.ttsSpeed.collectAsState()
                val cachedSet by viewModel.offlineAudioCachedSet.collectAsState()
                val isScribbleSpeech = activeTtsRes == resource.id

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isTtsActive && isScribbleSpeech) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            BorderStroke(
                                1.dp,
                                if (isTtsActive && isScribbleSpeech) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                            ),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = {
                                if (isTtsActive && isScribbleSpeech) {
                                    viewModel.pauseOrStopSpeaking()
                                } else {
                                    viewModel.startSpeakingNotes(
                                        resource.id,
                                        resource.title + ". " + resource.contentSnippet
                                    )
                                }
                            },
                            modifier = Modifier.size(32.dp).testTag("tts_play_btn")
                        ) {
                            Icon(
                                imageVector = if (isTtsActive && isScribbleSpeech) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                contentDescription = "Listen Mode",
                                tint = if (isTtsActive && isScribbleSpeech) MaterialTheme.colorScheme.primary else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Column {
                            Text(
                                "Listen Mode Voice Narration™",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isTtsActive && isScribbleSpeech) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                if (isTtsActive && isScribbleSpeech) "Reading notes aloud at ${ttsSpeed}x..." else "Listen offline or adjust speed",
                                fontSize = 8.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val nextSpeed = when (ttsSpeed) {
                                    1.0f -> 1.5f
                                    1.5f -> 2.0f
                                    2.0f -> 0.75f
                                    else -> 1.0f
                                }
                                viewModel.adjustTTSSpeed(nextSpeed)
                            },
                            modifier = Modifier.size(28.dp).testTag("tts_speed_btn")
                        ) {
                            Text(
                                text = "${ttsSpeed}x",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        val isCached = cachedSet.contains(resource.id)
                        IconButton(
                            onClick = { viewModel.toggleOfflineAudioCache(resource.id) },
                            modifier = Modifier.size(28.dp).testTag("tts_cache_btn")
                        ) {
                            Icon(
                                imageVector = if (isCached) Icons.Default.CloudDone else Icons.Default.CloudDownload,
                                contentDescription = "Cache Audio",
                                tint = if (isCached) Color(0xFF10B981) else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Control widgets Zoom-in/Zoom-out
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Zoom: ${(scaleZoom * 100).toInt()}%", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { scaleZoom = (scaleZoom - 0.25f).coerceAtLeast(0.5f) },
                            modifier = Modifier.size(24.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("-", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { scaleZoom = (scaleZoom + 0.25f).coerceAtMost(2.0f) },
                            modifier = Modifier.size(24.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("+", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Page 1 of 8", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Main simulated scrolling PDF body text
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFFFCFBF9), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFEFECE8), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("OAKRIDGE ACADEMIC SYLLABUS DIRECTIVE (RESTRICTED ACCESS)", fontSize = 7.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("Topic Outline Reference Code: PE-${resource.id}", fontSize = 8.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        
                        // Body text with customizable zoom sizes
                        Text(
                            text = resource.contentSnippet.ifBlank { "Full syllabus context load complete. Core theoretical formulations inside are ready for revision blocks. Ensure highlighting active chapters." },
                            fontSize = (11 * scaleZoom).sp,
                            color = Color(0xFF1E293B),
                            lineHeight = (15 * scaleZoom).sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Note highlights saved (Slick marker):",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        
                        savedHighlightsList.forEach { text ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFEF08A), RoundedCornerShape(4.dp)) // Yellow highlighter background
                                    .padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.BorderColor, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(8.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }
                }

                // Annotator and note highlighter panel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = highlightedNoteText,
                        onValueChange = { highlightedNoteText = it },
                        placeholder = { Text("Select text or write notes...", fontSize = 9.sp) },
                        modifier = Modifier.weight(1f).height(38.dp).testTag("highlighter_input"),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 10.sp),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Button(
                        onClick = {
                            if (highlightedNoteText.isNotBlank()) {
                                savedHighlightsList = savedHighlightsList + highlightedNoteText
                                highlightedNoteText = ""
                            }
                        },
                        modifier = Modifier.height(34.dp).testTag("highlight_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBBF24)),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.BorderColor, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Highlight", fontSize = 9.sp, color = Color.Black)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// CHILD COMPOSABLE: SMART STUDY SUITE & PERSONAL REVISION PLANNER
// -------------------------------------------------------------
@Composable
fun AcademicSmartStudyPlannerSuite(viewModel: LibraryViewModel) {
    val items by viewModel.studyPlannerList.collectAsState()
    var inputTopic by remember { mutableStateOf("") }
    var inputSubject by remember { mutableStateOf("S3 Biology") }
    var durationMins by remember { mutableStateOf(30) }
    var chosenDay by remember { mutableStateOf("Sunday") }

    val daysList = listOf("Sunday", "Monday", "Wednesday", "Friday")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text("Personal Smart Revision Planner & Alarms", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Text("SCHEDULE YOUR STUDY BLOCKS TO WIN BONUS STUDY MINUTES", fontSize = 10.sp, color = Color.Gray)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items.forEach { planned ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (planned.isDone) Color(0xFFD1FAE5).copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.togglePlannerTaskCompletion(planned.id) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Checked state Indicator box
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .border(1.5.dp, if (planned.isDone) Color(0xFF059669) else Color.Gray, RoundedCornerShape(4.dp))
                                    .background(if (planned.isDone) Color(0xFF10B981) else Color.Transparent, RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (planned.isDone) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                            Column {
                                Text(
                                    text = planned.subjectName + " : " + planned.topicToCover,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = if (planned.isDone) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (planned.isDone) Color.Gray else MaterialTheme.colorScheme.onSurface
                                )
                                Text("Day: ${planned.scheduledDay} • Duration: ${planned.targetMinutes} minutes", fontSize = 8.sp, color = Color.Gray)
                            }
                        }

                        if (planned.isDone) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFD1FAE5), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("COMPLETED", fontSize = 7.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF065F46))
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Icon(Icons.Default.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(10.dp))
                                Text("Pending", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Text("QUICK SCHEDULE DIRECTIVE FORM", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

            // Direct input planner form
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = inputTopic,
                    onValueChange = { inputTopic = it },
                    placeholder = { Text("Topic e.g. Electromagnetism", fontSize = 9.sp) },
                    modifier = Modifier.weight(1f).height(38.dp).testTag("planner_topic_entry"),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 10.sp),
                    shape = RoundedCornerShape(8.dp)
                )

                // Select duration dropdown simple
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .clickable {
                            durationMins = if (durationMins == 30) 45 else if (durationMins == 45) 60 else 30
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text("$durationMins mins", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (inputTopic.isNotBlank()) {
                            viewModel.addPlannerTask(inputSubject, inputTopic, durationMins, chosenDay)
                            inputTopic = ""
                        }
                    },
                    modifier = Modifier.height(38.dp).testTag("planner_add_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", fontSize = 10.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// CHILD COMPOSABLE: REAL-TIME ACADEMIC COMPLIANCE ANALYTICS METER
// -------------------------------------------------------------
@Composable
fun AcademicRealTimeAnalyticsDashboard(viewModel: LibraryViewModel) {
    val analytics by viewModel.studyAnalytics.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text("Subject Analytics & Progress Metres", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Text("METRIC DATA COMPILED ACROSS ACTIVE SECTIONS", fontSize = 10.sp, color = Color.Gray)

            analytics.forEach { stat ->
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stat.subjectName, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("${stat.studyDurationMinutes} mins studied this term", fontSize = 8.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    // Simple progress bar visualizing assignment completion
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(stat.completeAssignmentsRatio)
                                .height(6.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp))
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Assignment progress: ${(stat.completeAssignmentsRatio * 100).toInt()}%", fontSize = 8.sp, color = Color.Gray)
                        Text("Resources opened: ${stat.resourcesOpenedCount} counts", fontSize = 8.sp, color = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

// -------------------------------------------------------------
// CHILD COMPOSABLE: FUTURE EXPANSION MOCK CONTROL BOARD
// -------------------------------------------------------------
@Composable
fun AcademicFutureScalabilityPortal(viewModel: LibraryViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "FUTURE EXTENSION MODULES (ROADMAP)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            Text("This academic system is architected to securely connect with high-stakes Ugandan exam services and internal administrative pipelines.", fontSize = 10.sp, color = Color.Gray)

            // Roadmap buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Text("UNEB API INTEGRATION", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text("Sync candidate index indices to view government grades.", fontSize = 7.sp, color = Color.Gray)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Text("CBT EXAMS LAB", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text("Computer-based timed simulations for mocks.", fontSize = 7.sp, color = Color.Gray)
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Text("PARENT FEES portal", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text("Real-time clearance checks and bank synchronizers.", fontSize = 7.sp, color = Color.Gray)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Text("TIMETABLE & COMPLIANCE", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text("Live online classes schedule and teacher attendance logs.", fontSize = 7.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

// =========================================================================
// DESIGN CORE: REALISTIC TIMED UNEB PAST PAPER EXAMINATION SUITE
// =========================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UNEBExamEngineCardWorkspace(viewModel: LibraryViewModel) {
    val examActive by viewModel.unebExamActive.collectAsState()
    val examSubmitted by viewModel.unebExamSubmitted.collectAsState()
    val timeRemaining by viewModel.unebExamTimeSeconds.collectAsState()
    val randomized by viewModel.unebQuestionsRandomized.collectAsState()
    val selectedClass by viewModel.selectedExamClassLevel.collectAsState()
    val selectedSubject by viewModel.selectedExamSubjectName.collectAsState()
    val selectedYear by viewModel.selectedExamYearString.collectAsState()
    val questions by viewModel.unebCurrentQuestions.collectAsState()
    val userAnswers by viewModel.unebUserAnswers.collectAsState()
    val history by viewModel.unebSubmittedHistory.collectAsState()

    val lastScore by viewModel.unebLastScore.collectAsState()
    val lastDivision by viewModel.unebLastDivision.collectAsState()
    val essayText by viewModel.unebEssayText.collectAsState()
    val essayAIScore by viewModel.unebEssayAIScore.collectAsState()
    val essayFeedback by viewModel.unebEssayAIFeedback.collectAsState()
    val essayEvaluating by viewModel.unebEssayEvaluating.collectAsState()

    // Real-time Countdown timer task
    LaunchedEffect(examActive) {
        if (examActive) {
            while (viewModel.unebExamTimeSeconds.value > 0 && viewModel.unebExamActive.value) {
                delay(1000)
                viewModel.unebExamTimeSeconds.value -= 1
            }
            if (viewModel.unebExamActive.value) {
                viewModel.submitUNEBExam()
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (!examActive && !examSubmitted) {
            // STEP 1: EXAM SCHEDULER & PAST PAPER SELECTOR CONFIGURATION CARD
            Card(
                modifier = Modifier.fillMaxWidth().testTag("uneb_config_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("UNEB Trial Examination Center", fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Mock CBT v4.2", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    // CLASS LEVEL CHIPS (S1 - S6)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("1. Academic Candidate Level", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AcademicClassLevel.values().forEach { level ->
                                val active = level == selectedClass
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (active) MaterialTheme.colorScheme.primary else Color(0xFFF1F5F9),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.selectedExamClassLevel.value = level }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        level.label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) Color.White else Color.DarkGray
                                    )
                                }
                            }
                        }
                    }

                    // SUBJECT CHIPS
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("2. Select Target Subject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Biology", "Mathematics", "Economics", "General ICT").forEach { subj ->
                                val active = subj == selectedSubject
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (active) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF1F5F9),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            BorderStroke(
                                                1.dp,
                                                if (active) MaterialTheme.colorScheme.primary else Color.Transparent
                                            ),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.selectedExamSubjectName.value = subj }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        subj,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) MaterialTheme.colorScheme.primary else Color.DarkGray
                                    )
                                }
                            }
                        }
                    }

                    // EXAM YEAR SELECTION
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("3. Choose Historical past Paper Year", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("2024", "2023", "2022", "2020").forEach { yr ->
                                val active = yr == selectedYear
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (active) Color(0xFFE0F2FE) else Color(0xFFF1F5F9),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.selectedExamYearString.value = yr }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "UNEB $yr",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) Color(0xFF0284C7) else Color.DarkGray
                                    )
                                }
                            }
                        }
                    }

                    // QUESTION RANDOMIZATION SWITCH
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Shuffle, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            Column {
                                Text("Question Randomization Mode", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("Shuffle test items to expand cognitive adaptability.", fontSize = 8.sp, color = Color.Gray)
                            }
                        }
                        Switch(
                            checked = randomized,
                            onCheckedChange = { viewModel.unebQuestionsRandomized.value = it },
                            modifier = Modifier.scale(0.8f).testTag("random_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // START TIMED TRIAL BUTTON
                    Button(
                        onClick = { viewModel.startTimedUNEBExam() },
                        modifier = Modifier.fillMaxWidth().height(44.dp).testTag("start_uneb_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Launch Timed UNEB Trial Exam", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // STEP 2: RUNNING TIMED TRIAL TEST CONTAINER
        if (examActive) {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("uneb_running_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF9F6)),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // LIVE STATUS BAR (TIMIER HEADLINES)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFEF2F2), RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, Color(0xFFFEE2E2)), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.WatchLater, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            Text(
                                text = "UNEB MOCK RUNNING",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFEF4444)
                            )
                        }

                        // Formatted Timer Label
                        val minutes = timeRemaining / 60
                        val seconds = timeRemaining % 60
                        val timerString = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFEF4444), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = timerString,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }

                    Text(
                        text = "Subject: Senior 4 National Mock — $selectedSubject (${selectedYear} Past Paper)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))

                    // RENDER EACH QUESTION
                    questions.forEachIndexed { idx, q ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(14.dp))
                                .border(BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)), RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${idx + 1}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Text(
                                    text = q.text,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B)
                                )
                            }

                            // Image-based Question Canvas representation!
                            if (q.hasImage) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFFFBEB), RoundedCornerShape(10.dp))
                                        .border(BorderStroke(1.dp, Color(0xFFFDE68A)), RoundedCornerShape(10.dp))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            "PLATE IX: Cell Organelle Specimen Labeled - Study the cristae structure",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD97706)
                                        )
                                        CellMitochondrionCanvas()
                                    }
                                }
                            }

                            // CHOICE CONFIGURATION DEPENDING ON Q-TYPE
                            when (q.qType) {
                                "MCQ" -> {
                                    q.options.forEachIndexed { optIdx, opt ->
                                        val chosen = userAnswers[q.id] == optIdx.toString()
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    if (chosen) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                                    else Color.Transparent,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .border(
                                                    BorderStroke(
                                                        1.dp,
                                                        if (chosen) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.3f)
                                                    ),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable { viewModel.updateUNEBUserMCQ(q.id, optIdx) }
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            RadioButton(
                                                selected = chosen,
                                                onClick = { viewModel.updateUNEBUserMCQ(q.id, optIdx) },
                                                modifier = Modifier.scale(0.8f).testTag("q_${q.id}_opt_$optIdx")
                                            )
                                            Text(opt, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                                "STRUCTURED" -> {
                                    val currentAns = userAnswers[q.id] ?: ""
                                    OutlinedTextField(
                                        value = currentAns,
                                        onValueChange = { viewModel.updateUNEBUserStructured(q.id, it) },
                                        placeholder = { Text("Type accurate scientific term (e.g. Transpiration)", fontSize = 9.sp) },
                                        modifier = Modifier.fillMaxWidth().height(42.dp).testTag("q_${q.id}_field"),
                                        textStyle = TextStyle(fontSize = 10.sp),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                                "ESSAY" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            "Section C Long Essay: Outline key bullet details below",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray
                                        )
                                        OutlinedTextField(
                                            value = essayText,
                                            onValueChange = { viewModel.unebEssayText.value = it },
                                            placeholder = { Text("Write physiological adaptations of a mammalian heart...", fontSize = 9.sp) },
                                            modifier = Modifier.fillMaxWidth().height(90.dp).testTag("essay_draft_field"),
                                            textStyle = TextStyle(fontSize = 10.sp),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // SUBMIT TRIGGER BUTTON
                    Button(
                        onClick = { viewModel.submitUNEBExam() },
                        modifier = Modifier.fillMaxWidth().height(42.dp).testTag("submit_exam_click"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Finish and Hand-In UNEB Past Paper", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // STEP 3: MOCK RESULTS & CORRECTIONS FEEDBACK
        if (examSubmitted) {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("uneb_results_container"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Header Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF10B981))
                            Text("Mock Paper Correction Suite", fontSize = 13.sp, fontWeight = FontWeight.Black)
                        }
                        IconButton(
                            onClick = { viewModel.unebExamSubmitted.value = false },
                            modifier = Modifier.size(28.dp).testTag("close_results_btn")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    // SCORECARD VIEW
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$lastScore", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
                                Text("MARKS", fontSize = 6.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f))
                            }
                        }

                        Column {
                            Text(lastDivision, fontSize = 13.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Text("Official Grading Equivalent: Senior 4 UNEB Trial Standards", fontSize = 9.sp, color = Color.Gray)
                        }
                    }

                    // AUDEN AI ESSAY CHIEF EXAMINER EVALUATION WRITER
                    if (essayText.isNotBlank()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFAF5FF), RoundedCornerShape(14.dp))
                                .border(BorderStroke(1.dp, Color(0xFFE9D5FF)), RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(14.dp))
                                    Text("Auden AI UNEB Chief Examiner Evaluation", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF6B21A8))
                                }
                                
                                if (!essayEvaluating && essayFeedback == null) {
                                    Button(
                                        onClick = { viewModel.submitUNEBEssayForAIEvaluation() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(26.dp).testTag("evaluate_essay_btn"),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text("AI Grade Outlines", fontSize = 8.sp, color = Color.White)
                                    }
                                }
                            }

                            if (essayEvaluating) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                                    Text("Consulting national examiner marking schemes...", fontSize = 8.sp, color = Color.Gray)
                                }
                            }

                            if (essayFeedback != null) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF7C3AED), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("$essayAIScore / 10", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.White)
                                    }
                                    Text("Evaluated Grade Score", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                }
                                Text(
                                    text = essayFeedback ?: "",
                                    fontSize = 9.sp,
                                    color = Color(0xFF4C1D95),
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }

                    // RECOMMENDATIONS WEAK TOPIC FLASH REVISION CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(12.dp))
                                Text("Auden Personalized Weak-Subject Adaptive Recommendation", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFF92400E))
                            }
                            Text(
                                "Based on Mock trial checks, focus on leaf water transpiration cycles and mitochondrion ATP conversions. Click Revision tab for study sessions.",
                                fontSize = 8.sp,
                                color = Color(0xFF78350F),
                                lineHeight = 11.sp
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    Text("Answer Explanations & Corrections", fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    // LIST OF QUESTIONS WRONG / ANSWERS
                    questions.forEachIndexed { idx, q ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Question ${idx + 1}: ${q.text}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            
                            when (q.qType) {
                                "MCQ" -> {
                                    val userSelIdx = userAnswers[q.id]?.toIntOrNull() ?: -1
                                    val isCorrect = userSelIdx == q.correctOptionIndex
                                    val userTextAns = if (userSelIdx >= 0) q.options[userSelIdx] else "Unanswered"
                                    val correctTextAns = q.options.getOrElse(q.correctOptionIndex) { "" }

                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(
                                            imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                            contentDescription = null,
                                            tint = if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "Your Answer: $userTextAns",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCorrect) Color(0xFF047857) else Color(0xFFB91C1C)
                                        )
                                    }
                                    if (!isCorrect) {
                                        Text("Correct Answer: $correctTextAns", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                                    }
                                }
                                "STRUCTURED" -> {
                                    val typedAns = userAnswers[q.id] ?: ""
                                    val isCorrect = typedAns.equals(q.correctTextAnswer, ignoreCase = true)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(
                                            imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                            contentDescription = null,
                                            tint = if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "Your Answer: '$typedAns'",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCorrect) Color(0xFF047857) else Color(0xFFB91C1C)
                                        )
                                    }
                                    if (!isCorrect) {
                                        Text("Acceptable Term Name: '${q.correctTextAnswer}'", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                                    }
                                }
                                "ESSAY" -> {
                                    Text("Detailed Essay Outline Handed-In successfully.", fontSize = 9.sp, color = Color.Gray)
                                }
                            }

                            // RATIONALE SUMMARY DESCRIPTION WRITER
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "Explanation & Rationale: " + q.rationaleSummary,
                                    fontSize = 8.sp,
                                    color = Color.DarkGray,
                                    lineHeight = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // STEP 4: HISTORICAL PAST PAPERS LOG BOOK (TRACKING HISTORY!)
        Card(
            modifier = Modifier.fillMaxWidth().testTag("uneb_history_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.History, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Text("Candidate Trial History Index", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                history.forEach { log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(log.subName, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("Class: ${log.classLevel} | Year: ${log.examYear} past paper", fontSize = 7.sp, color = Color.Gray)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(log.score, fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (log.divisionLabel.contains("1")) Color(0xFFD1FAE5) else Color(0xFFF1F5F9),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    log.divisionLabel,
                                    fontSize = 6.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (log.divisionLabel.contains("1")) Color(0xFF047857) else Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// CANVAS CORE: DYNAMIC HIGH RESOLUTION CELL DRAWING
// =========================================================================
@Composable
fun CellMitochondrionCanvas() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(4.dp)
            .testTag("mitochondrion_draw")
    ) {
        val outerRadiusX = size.width / 4.5f
        val outerRadiusY = size.height / 2.3f
        val centerPoint = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)

        // Draw Outer capsule membrane
        drawOval(
            color = Color(0xFFE11D48),
            topLeft = androidx.compose.ui.geometry.Offset(centerPoint.x - outerRadiusX, centerPoint.y - outerRadiusY),
            size = androidx.compose.ui.geometry.Size(outerRadiusX * 2, outerRadiusY * 2),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
        )

        // Inner folded cristae wave mapping
        val p = androidx.compose.ui.graphics.Path().apply {
            moveTo(centerPoint.x - outerRadiusX + 15f, centerPoint.y)
            quadraticTo(centerPoint.x - 50f, centerPoint.y - 30f, centerPoint.x - 30f, centerPoint.y)
            quadraticTo(centerPoint.x - 10f, centerPoint.y + 30f, centerPoint.x + 10f, centerPoint.y)
            quadraticTo(centerPoint.x + 30f, centerPoint.y - 30f, centerPoint.x + 50f, centerPoint.y)
            quadraticTo(centerPoint.x + 80f, centerPoint.y + 30f, centerPoint.x + outerRadiusX - 15f, centerPoint.y)
        }
        drawPath(path = p, color = Color(0xFF3B82F6), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))

        // Label arrows details
        drawCircle(color = Color(0xFFD97706), radius = 5f, center = androidx.compose.ui.geometry.Offset(centerPoint.x, centerPoint.y))
        drawCircle(color = Color(0xFFD97706), radius = 5f, center = androidx.compose.ui.geometry.Offset(centerPoint.x - 60f, centerPoint.y - 12f))
    }
}

// =========================================================================
// SCHOLAR GAMIFICATION SUITE MODALITY
// =========================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UgandanScholarGamificationHub(viewModel: LibraryViewModel) {
    val xpPoints by viewModel.currentXpPoints.collectAsState()
    val levelRank by viewModel.currentLevelRank.collectAsState()
    val streak by viewModel.revisionStreakCount.collectAsState()
    val focusTimerActive by viewModel.focusTimerActive.collectAsState()
    val focusTimeRemaining by viewModel.focusTimeRemainingSeconds.collectAsState()
    val syncedCalendar by viewModel.calendarRemindersSynced.collectAsState()

    // Focus session ticker LaunchedEffect
    LaunchedEffect(focusTimerActive) {
        if (focusTimerActive) {
            while (viewModel.focusTimeRemainingSeconds.value > 0 && viewModel.focusTimerActive.value) {
                delay(1000)
                viewModel.focusTimeRemainingSeconds.value -= 1
            }
            if (viewModel.focusTimerActive.value) {
                viewModel.completeFocusSession()
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // XP PROGRESS LEVEL METER CARD
        Card(
            modifier = Modifier.fillMaxWidth().testTag("gamified_status_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = AcademicGoldLight)
                        Text("Scholar XP Achievement Rank", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFEF2F2), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(12.dp))
                            Text("$streak Day Streak", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFFEF4444))
                        }
                    }
                }

                // XP Display Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(text = levelRank, fontSize = 15.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        Text(text = "XP Progress Indicator (Leveling system)", fontSize = 8.sp, color = Color.Gray)
                    }
                    Text(text = "$xpPoints / 3200 XP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Custom dynamic linear progress tracker bar
                val currentPct = (xpPoints.toFloat() / 3200f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(currentPct)
                            .height(8.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(MaterialTheme.colorScheme.primary, Color(0xFF3B82F6))
                                ),
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
        }

        // STUDY SLAYER POMODORO CONCENTRATION CLOCK
        Card(
            modifier = Modifier.fillMaxWidth().testTag("pomodoro_focus_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Watch, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                        Text("Scholar Pomodoro Focus Timer", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("+150 XP Reward", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val focusMinutes = focusTimeRemaining / 60
                    val focusSeconds = focusTimeRemaining % 60
                    val timerLabelStr = String.format(java.util.Locale.US, "%02d:%02d", focusMinutes, focusSeconds)
                    
                    Text(
                        text = timerLabelStr,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.testTag("pomodoro_time_text")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = { viewModel.toggleFocusTimer() },
                            modifier = Modifier
                                .background(if (focusTimerActive) Color(0xFFEF4444) else Color(0xFF38BDF8), RoundedCornerShape(8.dp))
                                .size(34.dp).testTag("pomodoro_toggle_btn")
                        ) {
                            Icon(
                                imageVector = if (focusTimerActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Toggle Timer",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.resetFocusTimer() },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .size(34.dp).testTag("pomodoro_reset_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // BUILT-IN CALENDAR SYNCHRONIST (WORKMANAGER + GOOGLE CALENDAR)
        Card(
            modifier = Modifier.fillMaxWidth().testTag("sync_reminders_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.Gray)
                    Column {
                        Text("Google Calendar Study Schedules", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (syncedCalendar) "Countdowns logged inside Android Calendar system!" else "Queue triggers with local WorkManager reminders",
                            fontSize = 8.sp,
                            color = Color.Gray
                        )
                    }
                }

                Button(
                    onClick = { viewModel.syncGoogleCalendarWorkManager() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (syncedCalendar) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(30.dp).testTag("calendar_sync_trigger_btn"),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(if (syncedCalendar) "Synced" else "Sync", fontSize = 9.sp, color = Color.White)
                }
            }
        }

        // TROPHIES & BADGES EXCELLENCE GALORE (BADGES WALL)
        Text("Candidate Badges & Medals", fontSize = 11.sp, fontWeight = FontWeight.Bold)

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BadgeItemWidget("streak_fame_badge", "Streak Master", "12 DAYS", Color(0xFFFEF2F2), Color(0xFFEF4444), Icons.Default.LocalFireDepartment)
            BadgeItemWidget("reader_fame_badge", "Top Reader", "S4 READS", Color(0xFFECFDF5), Color(0xFF10B981), Icons.Default.MenuBook)
            BadgeItemWidget("score_fame_badge", "Quiz Master", "DISTINCT", Color(0xFFFFFBEB), Color(0xFFD97706), Icons.Default.EmojiEvents)
            BadgeItemWidget("excellence_fame_badge", "Excellence Unit", "MEDALIST", Color(0xFFEFF6FF), Color(0xFF3B82F6), Icons.Default.WorkspacePremium)
        }
    }
}

// =========================================================================
// WIDGET DESIGN: BADGES WRITER IN COMRADESHIP LAYOUTS
// =========================================================================
@Composable
fun BadgeItemWidget(tag: String, title: String, scoreLabel: String, bg: Color, fg: Color, iconVector: ImageVector) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .testTag(tag),
        colors = CardDefaults.cardColors(containerColor = bg),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, fg.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconVector, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
            }
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Box(
                modifier = Modifier
                    .background(fg, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(scoreLabel, fontSize = 7.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
        }
    }
}


