package com.example.ui

import androidx.compose.ui.draw.scale
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
                    .background(MaterialTheme.colorScheme.background)
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

    val categories = listOf("All", "Literature", "Science & Tech", "Fiction", "History", "Self-Growth")

    Column(modifier = Modifier.fillMaxSize()) {
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
                onAssignClicked = { showAssignDialog = true }
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
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
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
    onAssignClicked: () -> Unit
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

