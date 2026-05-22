package com.example.ui

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
                    label = { Text("Catalog", fontWeight = FontWeight.SemiBold) },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.testTag("tab_catalog")
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AutoStories, contentDescription = "My Books") },
                    label = { Text("My Books", fontWeight = FontWeight.SemiBold) },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.testTag("tab_my_books")
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Campaign, contentDescription = "News") },
                    label = { Text("News", fontWeight = FontWeight.SemiBold) },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.testTag("tab_news")
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ContactMail, contentDescription = "Profile") },
                    label = { Text("ID Card", fontWeight = FontWeight.SemiBold) },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
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

            // Dynamic view based on tab index
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> CatalogTab(viewModel)
                    1 -> MyBooksTab(viewModel)
                    2 -> CampusNewsTab(viewModel)
                    3 -> StudentCardTab(viewModel)
                }
            }
        }
    }
}

@Composable
fun SchoolHeaderPanel(studentName: String, streak: Int) {
    val initials = remember(studentName) {
        studentName.trim().split("\\s+".toRegex())
            .mapNotNull { it.firstOrNull() }
            .take(2)
            .joinToString("")
            .uppercase()
            .ifEmpty { "OH" }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Circular Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Column {
                    Text(
                        text = "Oakridge High Library",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Welcome back, $studentName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Streak icon button container
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = AcademicGold,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Styled Card following the mockup
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 36.dp, height = 48.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    )
                                )
                            )
                    )
                    Column {
                        Text(
                            text = "OFFICIAL RECORD",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Achieved $streak-Day Streak",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "STUDENT PASS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
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

    val categories = listOf("All", "Literature", "Science & Tech", "Fiction", "History", "Self-Growth")

    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Filter Block
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Styled Search Text Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("catalog_search_input"),
                placeholder = { Text("Search title, author, description...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "SearchIcon") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.updateSearchQuery("") },
                            modifier = Modifier.testTag("clear_search_button")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
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
                    BookListItem(
                        book = book,
                        onSelect = { viewModel.selectBook(book.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(book.id, !book.isFavorite) }
                    )
                }
            }
        }
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
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
                                    Text("Currently Borrowed")
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
                                        text = if (currentBook.availableCopies > 0) "Borrow Virtually (14 days)" else "No Copies Available",
                                        fontWeight = FontWeight.Bold
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
                    onProgressChange = { progress -> viewModel.updateRecordProgress(record, progress) }
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
    onProgressChange: (Int) -> Unit
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
                horizontalArrangement = Arrangement.End
            ) {
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
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
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Official Student Library Pass",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "ID: $studentId",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
