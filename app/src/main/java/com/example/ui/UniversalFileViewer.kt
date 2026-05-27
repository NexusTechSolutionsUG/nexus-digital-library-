package com.example.ui

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.widget.VideoView
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.theme.AcademicGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.roundToInt

// Format details matching the request's scope
enum class FileFormat {
    PDF, DOCX, TXT, RTF, EPUB, PPTX, XLSX, // Docs
    JPG, PNG, WEBP, GIF, SVG,              // Images
    MP4, WEBM,                             // Videos
    MP3, WAV                               // Audio
}

data class EducationalFile(
    val id: String,
    val name: String,
    val format: FileFormat,
    val size: String,
    val virtualUrl: String,
    val contentPages: List<String> = emptyList(),
    val isCompanion: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalFileViewerScreen(
    viewModel: LibraryViewModel,
    onClose: () -> Unit
) {
    val book by viewModel.activeViewingBook.collectAsState()
    val progressState by viewModel.activeBookProgress.collectAsState()
    val bookmarks by viewModel.bookmarksForActiveBook.collectAsState()
    val highlights by viewModel.highlightsForActiveBook.collectAsState()
    val annotations by viewModel.annotationsForActiveBook.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (book == null) return
    val activeBook = book!!

    // Local file loader state (supports TXT, MP3, MP4, JPG, PNG etc.)
    var localFileUri by remember { mutableStateOf<Uri?>(null) }
    var localFileName by remember { mutableStateOf<String?>(null) }
    var localFileContent by remember { mutableStateOf<String?>(null) }
    var localFileFormat by remember { mutableStateOf<FileFormat?>(null) }

    // Resolve virtual companion files related to the selected book
    val companionFiles = remember(activeBook.id) {
        getCompanionFilesForBook(activeBook)
    }

    var selectedFile by remember { mutableStateOf<EducationalFile?>(companionFiles.firstOrNull()) }
    var selectedTabState by remember { mutableStateOf("Viewer") } // "Viewer", "Highlights_Bookmarks", "Annotations"
    
    // Viewer view states
    var currentPage by remember(selectedFile?.id) { mutableIntStateOf(0) }
    LaunchedEffect(progressState) {
        if (progressState != null) {
            currentPage = progressState!!.lastPage.coerceIn(0, (selectedFile?.contentPages?.size ?: 1) - 1)
        }
    }

    var isFullscreen by remember { mutableStateOf(false) }
    var scaleTheme by remember { mutableStateOf("midnight") } // "light", "sepia", "midnight"
    var isDownloadingState by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var speedMetric by remember { mutableStateOf("0 KB/s") }

    // Media States
    var isMediaPlaying by remember { mutableStateOf(false) }
    var mediaPlaybackSpeed by remember { mutableFloatStateOf(1f) }
    var subtitleText by remember { mutableStateOf("") }
    var audioProgressSec by remember { mutableIntStateOf(0) }
    val audioDurationSec = 180

    // Annotation sketching states
    var currentDrawColor by remember { mutableStateOf(Color.Red) }
    var currentBrushSize by remember { mutableFloatStateOf(6f) }
    var activeInkStrokes = remember { mutableStateListOf<List<Offset>>() }
    var currentInkPoints = remember { mutableStateListOf<Offset>() }
    var activeNotes = remember { mutableStateListOf<Pair<Offset, String>>() }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var notePosition by remember { mutableStateOf(Offset.Zero) }
    var tempNoteText by remember { mutableStateOf("") }

    // Highlight text state
    var textSelectionState by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var highlightColorHex by remember { mutableStateOf("#FFEB3B") } // Academic Gold

    // Picture-in-Picture simulator overlay
    var isSimulatedPiPActive by remember { mutableStateOf(false) }

    // Activity launcher for choosing direct real offline files from disk
    val diskFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            localFileUri = uri
            val resolver = context.contentResolver
            val nameIndex = try {
                val cursor = resolver.query(uri, null, null, null, null)
                cursor?.use {
                    val idx = it.getColumnIndex("_display_name")
                    if (idx >= 0 && it.moveToFirst()) it.getString(idx) else null
                }
            } catch (e: Exception) {
                null
            } ?: "Discovered_Resource.txt"
            
            localFileName = nameIndex
            val extension = nameIndex.substringAfterLast('.', "").uppercase()
            localFileFormat = try {
                FileFormat.valueOf(extension)
            } catch (e: Exception) {
                if (nameIndex.endsWith(".txt", ignoreCase = true)) FileFormat.TXT
                else if (nameIndex.endsWith(".mp3", ignoreCase = true)) FileFormat.MP3
                else if (nameIndex.endsWith(".mp4", ignoreCase = true)) FileFormat.MP4
                else if (nameIndex.endsWith(".png", ignoreCase = true)) FileFormat.PNG
                else if (nameIndex.endsWith(".jpg", ignoreCase = true) || nameIndex.endsWith(".jpeg", ignoreCase = true)) FileFormat.JPG
                else FileFormat.TXT
            }

            // Real physical TXT helper
            if (localFileFormat == FileFormat.TXT) {
                try {
                    val inputStream = resolver.openInputStream(uri)
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val stringBuilder = StringBuilder()
                    var line: String? = reader.readLine()
                    var linesCount = 0
                    while (line != null && linesCount < 60) {
                        stringBuilder.append(line).append("\n")
                        line = reader.readLine()
                        linesCount++
                    }
                    localFileContent = stringBuilder.toString()
                    inputStream?.close()
                } catch (e: Exception) {
                    localFileContent = "Error parsing local text content."
                }
            } else {
                localFileContent = null
            }

            // Map standard format
            selectedFile = EducationalFile(
                id = "disk-file",
                name = nameIndex,
                format = localFileFormat ?: FileFormat.TXT,
                size = "Local Storage File",
                virtualUrl = uri.toString(),
                contentPages = if (localFileFormat == FileFormat.TXT) listOf(localFileContent ?: "") else listOf("Local file loaded successfully.")
            )
            currentPage = 0
        }
    }

    // Dynamic annotation reload when page flips or selected file shifts
    LaunchedEffect(currentPage, selectedFile?.id, annotations) {
        activeInkStrokes.clear()
        activeNotes.clear()
        val anno = annotations.find { it.page == currentPage }
        if (anno != null) {
            // Reconstruct strokes & notes
            if (anno.drawStrokesJson.isNotBlank()) {
                val strokeList = deserializeStrokes(anno.drawStrokesJson)
                activeInkStrokes.addAll(strokeList)
            }
            if (anno.typedNote.isNotBlank()) {
                val noteList = deserializeNotes(anno.typedNote)
                activeNotes.addAll(noteList)
            }
        }
    }

    // Helper: auto subtitle tracking for MP4 video format
    LaunchedEffect(isMediaPlaying, currentPage) {
        if (isMediaPlaying && selectedFile?.format == FileFormat.MP4) {
            var counter = 0
            while (isMediaPlaying) {
                counter = (counter + 1) % 40
                subtitleText = when {
                    counter < 10 -> "[Prof. Reynolds]: Let us analyze the structural elements of text."
                    counter < 20 -> "[Prof. Reynolds]: The theme of betrayal acts as the pivotal climax."
                    counter < 30 -> "[Prof. Reynolds]: Pay close attention to Chapter 5 notations."
                    else -> "[Prof. Reynolds]: This aligns directly with your final mid-term syllabus."
                }
                delay(1000)
            }
        } else {
            subtitleText = ""
        }
    }

    // Helper: auto timer tracking for MP3 audio progress
    LaunchedEffect(isMediaPlaying) {
        if (isMediaPlaying && (selectedFile?.format == FileFormat.MP3 || selectedFile?.format == FileFormat.WAV)) {
            while (isMediaPlaying) {
                delay(1000)
                if (audioProgressSec < audioDurationSec) {
                    audioProgressSec++
                } else {
                    isMediaPlaying = false
                }
            }
        }
    }

    // Helper function to simulate cached offline downloader with metrics
    fun triggerSimulatedCacheDownload(targetFile: EducationalFile) {
        scope.launch {
            isDownloadingState = true
            downloadProgress = 0f
            var byteAccumulated = 0f
            val targetSize = if (targetFile.size.contains("MB")) {
                targetFile.size.removeSuffix(" MB").toFloat()
            } else {
                0.8f
            }
            
            while (downloadProgress < 1f) {
                delay(120)
                downloadProgress += 0.15f
                byteAccumulated += targetSize * 0.15f
                speedMetric = "${String.format(Locale.getDefault(), "%.1f", (2.5f + Math.random() * 3))} MB/s"
            }
            downloadProgress = 1f
            delay(150)
            isDownloadingState = false
            selectedFile = targetFile
            currentPage = 0
        }
    }

    Surface(
        color = when (scaleTheme) {
            "light" -> Color(0xFFF9F9FB)
            "sepia" -> Color(0xFFFDF6E3)
            else -> Color(0xFF121214)
        },
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (isSimulatedPiPActive) {
            // Simple Pip visual floating window
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
            ) {
                Card(
                    modifier = Modifier
                        .size(190.dp, 130.dp)
                        .padding(8.dp)
                        .shadow(12.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            imageVector = Icons.Default.OndemandVideo,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.Center)
                        )
                        IconButton(
                            onClick = { isSimulatedPiPActive = false },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(28.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close PiP", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        Text(
                            text = "Lecture PiP Companion",
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .fillMaxWidth()
                                .padding(2.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                AnimatedVisibility(
                    visible = !isFullscreen,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = activeBook.title,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = if (scaleTheme == "light") Color(0xFF1E2022) else Color.White
                                )
                                Text(
                                    text = "In-App Universal Resources",
                                    fontSize = 12.sp,
                                    color = if (scaleTheme == "light") Color.Gray else Color.LightGray
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onClose) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = if (scaleTheme == "light") Color.Black else Color.White
                                )
                            }
                        },
                        actions = {
                            // Disk Icon to pick physical resource
                            IconButton(
                                onClick = { diskFileLauncher.launch("*/*") },
                                modifier = Modifier.testTag("hardware_file_picker")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = "Open Local File",
                                    tint = if (scaleTheme == "light") Color.Black else Color.White
                                )
                            }
                            
                            // Dark Mode Selector Button
                            IconButton(onClick = {
                                scaleTheme = when (scaleTheme) {
                                    "light" -> "sepia"
                                    "sepia" -> "midnight"
                                    else -> "light"
                                }
                            }) {
                                Icon(
                                    imageVector = when (scaleTheme) {
                                        "light" -> Icons.Default.LightMode
                                        "sepia" -> Icons.Outlined.Lightbulb
                                        else -> Icons.Default.DarkMode
                                    },
                                    contentDescription = "Cycle theme context",
                                    tint = if (scaleTheme == "light") Color.Black else Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = when (scaleTheme) {
                                "light" -> Color.White
                                "sepia" -> Color(0xFFF0E4C3)
                                else -> Color(0xFF1C1C22)
                            }
                        ),
                        modifier = Modifier.topBarSegmentBorder()
                    )
                }
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = !isFullscreen,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    BottomAppBar(
                        containerColor = when (scaleTheme) {
                            "light" -> Color.White
                            "sepia" -> Color(0xFFF0E4C3)
                            else -> Color(0xFF1C1C22)
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { selectedTabState = "Viewer" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedTabState == "Viewer") AcademicGold else Color.Transparent,
                                    contentColor = if (selectedTabState == "Viewer") Color.Black else (if (scaleTheme == "light") Color.Black else Color.White)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Resource View", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { selectedTabState = "Highlights_Bookmarks" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedTabState == "Highlights_Bookmarks") AcademicGold else Color.Transparent,
                                    contentColor = if (selectedTabState == "Highlights_Bookmarks") Color.Black else (if (scaleTheme == "light") Color.Black else Color.White)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Icon(Icons.Default.Bookmarks, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Highlights & Bookmarks", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { selectedTabState = "Annotations" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedTabState == "Annotations") AcademicGold else Color.Transparent,
                                    contentColor = if (selectedTabState == "Annotations") Color.Black else (if (scaleTheme == "light") Color.Black else Color.White)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Icon(Icons.Default.ModeEdit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pen Ink Note", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Resource Picker selector bar (companion list of files: EPUB, PDF, video lecture, audio companion)
                    AnimatedVisibility(visible = !isFullscreen) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (scaleTheme == "light") Color(0xFFF0F1F4) else Color(0xFF15151A))
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 10.dp, horizontal = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            companionFiles.forEach { file ->
                                val isChosen = selectedFile?.id == file.id
                                ResourceChip(
                                    file = file,
                                    isChosen = isChosen,
                                    onClick = {
                                        // Trigger caching animation for simulation of huge resources
                                        triggerSimulatedCacheDownload(file)
                                    }
                                )
                            }
                        }
                    }

                    // Caching/Downloading screen
                    if (isDownloadingState) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Offline-Caching Asset File...",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (scaleTheme == "light") Color.Black else Color.White
                                )
                                LinearProgressIndicator(
                                    progress = { downloadProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    color = AcademicGold,
                                    trackColor = Color.Gray.copy(alpha = 0.3f)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${(downloadProgress * 100).roundToInt()}% Cached",
                                        fontSize = 12.sp,
                                        color = AcademicGold
                                    )
                                    Text(
                                        text = speedMetric,
                                        fontSize = 12.sp,
                                        color = if (scaleTheme == "light") Color.Gray else Color.LightGray
                                    )
                                }
                            }
                        }
                    } else if (selectedFile == null) {
                        EmptyFileState(scaleTheme)
                    } else {
                        // Core Panel switcher based on lower tabs
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            when (selectedTabState) {
                                "Viewer" -> {
                                    MainInteractiveViewer(
                                        file = selectedFile!!,
                                        currentPage = currentPage,
                                        totalPages = selectedFile!!.contentPages.size,
                                        scaleTheme = scaleTheme,
                                        isFullscreen = isFullscreen,
                                        onToggleFullscreen = { isFullscreen = !isFullscreen },
                                        onPageChange = { newPage ->
                                            currentPage = newPage
                                            viewModel.savePageProgress(activeBook.id, newPage, selectedFile!!.contentPages.size)
                                        },
                                        // Media Parameters
                                        isPlaySelected = isMediaPlaying,
                                        onChangePlayState = { isMediaPlaying = it },
                                        playbackSpeed = mediaPlaybackSpeed,
                                        onChangePlaybackSpeed = { mediaPlaybackSpeed = it },
                                        subtitleText = subtitleText,
                                        audioProgressSeconds = audioProgressSec,
                                        onChangeAudioProgress = { audioProgressSec = it },
                                        onPiPTurn = { isSimulatedPiPActive = true },
                                        // Highlight selections
                                        textSelection = textSelectionState,
                                        onTextSelectionChange = { textSelectionState = it },
                                        highlightColor = highlightColorHex,
                                        onChangeHighlightColor = { highlightColorHex = it },
                                        onSaveHighlight = { selectedTxt ->
                                            viewModel.addHighlight(
                                                bookId = activeBook.id,
                                                page = currentPage,
                                                text = selectedTxt,
                                                colorHex = highlightColorHex
                                            )
                                            textSelectionState = null
                                        }
                                    )
                                }
                                "Highlights_Bookmarks" -> {
                                    HighlightsAndBookmarksTab(
                                        bookId = activeBook.id,
                                        currentPage = currentPage,
                                        totalPageCount = selectedFile!!.contentPages.size,
                                        bookmarks = bookmarks,
                                        highlights = highlights,
                                        scaleTheme = scaleTheme,
                                        onJumpToPage = { targetPage ->
                                            currentPage = targetPage
                                            selectedTabState = "Viewer"
                                        },
                                        onAddBookmark = { noteTxt ->
                                            viewModel.addBookmark(activeBook.id, currentPage, noteTxt)
                                        },
                                        onDeleteBookmark = { idx ->
                                            viewModel.deleteBookmark(idx)
                                        },
                                        onDeleteHighlight = { idx ->
                                            viewModel.deleteHighlight(idx)
                                        }
                                    )
                                }
                                "Annotations" -> {
                                    // Custom visual pen sketching page layout
                                    SketchAnnotationLayout(
                                        bookId = activeBook.id,
                                        currentPage = currentPage,
                                        selectedFile = selectedFile!!,
                                        scaleTheme = scaleTheme,
                                        currentDrawColor = currentDrawColor,
                                        currentBrushSize = currentBrushSize,
                                        onBrushColorChange = { currentDrawColor = it },
                                        onBrushSizeChange = { currentBrushSize = it },
                                        activeInkStrokes = activeInkStrokes,
                                        currentInkPoints = currentInkPoints,
                                        activeNotes = activeNotes,
                                        onAddPinnedNote = { pt, txt ->
                                            activeNotes.add(pt to txt)
                                        },
                                        onSaveAnnotations = {
                                            // Save into database representation
                                            val strokesJson = serializeStrokes(activeInkStrokes)
                                            val notesJson = serializeNotes(activeNotes)
                                            viewModel.addAnnotation(
                                                bookId = activeBook.id,
                                                page = currentPage,
                                                strokesJson = strokesJson,
                                                typedNote = notesJson
                                            )
                                        },
                                        onClearAll = {
                                            activeInkStrokes.clear()
                                            currentInkPoints.clear()
                                            activeNotes.clear()
                                        }
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

// Single tab button with modern icons and highlights
@Composable
fun ResourceChip(
    file: EducationalFile,
    isChosen: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isChosen) AcademicGold else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val textColor = if (isChosen) Color.Black else MaterialTheme.colorScheme.onSurface
    val icon = when (file.format) {
        FileFormat.PDF -> Icons.Outlined.PictureAsPdf
        FileFormat.EPUB -> Icons.Outlined.ChromeReaderMode
        FileFormat.MP4, FileFormat.WEBM -> Icons.Outlined.Movie
        FileFormat.MP3, FileFormat.WAV -> Icons.Outlined.AudioFile
        else -> Icons.Outlined.InsertDriveFile
    }

    Row(
        modifier = Modifier
            .shadow(if (isChosen) 4.dp else 1.dp, RoundedCornerShape(20.dp))
            .background(containerColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = if (isChosen) Color.Black else AcademicGold, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = file.name, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "(${file.size})", color = textColor.copy(alpha = 0.6f), fontSize = 9.sp)
    }
}

@Composable
fun EmptyFileState(scaleTheme: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CloudQueue,
                contentDescription = null,
                tint = AcademicGold,
                modifier = Modifier.size(56.dp)
            )
            Text(
                text = "No Resource Selected",
                color = if (scaleTheme == "light") Color.Black else Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Select one of the digital educational formats above, or load an offline text file from your workspace.",
                color = Color.Gray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Interactive Fullscreen/Zoom document viewer, includes highlighting text
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainInteractiveViewer(
    file: EducationalFile,
    currentPage: Int,
    totalPages: Int,
    scaleTheme: String,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    onPageChange: (Int) -> Unit,
    // Media Controls
    isPlaySelected: Boolean,
    onChangePlayState: (Boolean) -> Unit,
    playbackSpeed: Float,
    onChangePlaybackSpeed: (Float) -> Unit,
    subtitleText: String,
    audioProgressSeconds: Int,
    onChangeAudioProgress: (Int) -> Unit,
    onPiPTurn: () -> Unit,
    // Text Highlight Controls
    textSelection: Pair<Int, Int>?,
    onTextSelectionChange: (Pair<Int, Int>?) -> Unit,
    highlightColor: String,
    onChangeHighlightColor: (String) -> Unit,
    onSaveHighlight: (String) -> Unit
) {
    val pageColors = when (scaleTheme) {
        "light" -> Triple(Color.White, Color.Black, Color.DarkGray)
        "sepia" -> Triple(Color(0xFFFDF6E3), Color(0xFF586E75), Color(0xFF657B83))
        else -> Triple(Color(0xFF15151A), Color(0xFFE0E0E6), Color.LightGray)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pageColors.first)
            .pointerInput(Unit) {
                // Tapping background toggles mock fullscreen mode for focus immersive reading
                detectTapGestures(
                    onDoubleTap = { onToggleFullscreen() }
                )
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (file.format == FileFormat.MP4 || file.format == FileFormat.WEBM) {
                // Interactive Simulated MP4 Lecturer tutorial view
                VideoLecturerSimulator(
                    isPlaying = isPlaySelected,
                    onChangePlayState = onChangePlayState,
                    speed = playbackSpeed,
                    onChangeSpeed = onChangePlaybackSpeed,
                    subtitleText = subtitleText,
                    onPiPToggle = onPiPTurn,
                    isFullscreen = isFullscreen,
                    onToggleFullscreen = onToggleFullscreen,
                    colors = pageColors
                )
            } else if (file.format == FileFormat.MP3 || file.format == FileFormat.WAV) {
                // Audiobook podcast overlay companion
                AudiobookPodcastSimulator(
                    isPlaying = isPlaySelected,
                    onChangePlayState = onChangePlayState,
                    progressSec = audioProgressSeconds,
                    onChangeProgress = onChangeAudioProgress,
                    speed = playbackSpeed,
                    onChangeSpeed = onChangePlaybackSpeed,
                    colors = pageColors
                )
            } else {
                // Document pages PDF, DOCX, TXT, EPUB, scanned PNG, PPTX slides, XLSX spreadsheet
                ZoomableBox(modifier = Modifier.weight(1f)) { scale, offset ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (file.format == FileFormat.PPTX) {
                                // Slide format layout style
                                AcademicSlideViewer(file, currentPage, pageColors)
                            } else if (file.format == FileFormat.XLSX) {
                                // Grid Sheet format
                                SchoolGradebookSheet(pageColors)
                            } else if (file.format == FileFormat.PNG || file.format == FileFormat.JPG || file.format == FileFormat.WEBP) {
                                // High res Scanned book plate / image
                                HighResImagePage(file, pageColors)
                            } else {
                                // Dynamic text parser for EPUB / TXT / DOCX / RTF / PDF
                                BookPagesTextEngine(
                                    file = file,
                                    currentPage = currentPage,
                                    colors = pageColors,
                                    textSelection = textSelection,
                                    onTextSelectionChange = onTextSelectionChange,
                                    onSaveHighlight = onSaveHighlight
                                )
                            }
                        }
                    }
                }
            }

            // Controls overlay row for page flipping
            if (!isFullscreen && file.format != FileFormat.MP4 && file.format != FileFormat.MP3 && file.format != FileFormat.WAV && file.format != FileFormat.XLSX) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (currentPage > 0) onPageChange(currentPage - 1) },
                        enabled = currentPage > 0
                    ) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Prev page", tint = AcademicGold)
                    }

                    Text(
                        text = "Slide/Page ${currentPage + 1} of $totalPages",
                        color = pageColors.second,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { if (currentPage < totalPages - 1) onPageChange(currentPage + 1) },
                        enabled = currentPage < totalPages - 1
                    ) {
                        Icon(Icons.Default.ArrowForwardIos, contentDescription = "Next page", tint = AcademicGold)
                    }
                }
            }
        }

        // Fullscreen indicator guide tag or dynamic highlighter tool overlay floating
        if (textSelection != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .shadow(8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Highlight Selected:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    
                    // Yellow
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF176))
                            .border(if (highlightColor == "#FFF176") 2.dp else 0.dp, Color.Black, CircleShape)
                            .clickable { onChangeHighlightColor("#FFF176") }
                    )
                    // Green
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFAED581))
                            .border(if (highlightColor == "#AED581") 2.dp else 0.dp, Color.Black, CircleShape)
                            .clickable { onChangeHighlightColor("#AED581") }
                    )
                    // Pink
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF06292))
                            .border(if (highlightColor == "#F06292") 2.dp else 0.dp, Color.Black, CircleShape)
                            .clickable { onChangeHighlightColor("#F06292") }
                    )

                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(
                        onClick = {
                            val paragraphText = file.contentPages.getOrNull(currentPage) ?: "Interesting passage"
                            val start = textSelection.first.coerceIn(0, paragraphText.length)
                            val end = textSelection.second.coerceIn(0, paragraphText.length)
                            val excerpt = paragraphText.substring(Math.min(start, end), Math.max(start, end))
                            onSaveHighlight(excerpt)
                        }
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold, color = AcademicGold)
                    }
                }
            }
        }
    }
}

// Audiobook waveform simulation layout and slider playback
@Composable
fun AudiobookPodcastSimulator(
    isPlaying: Boolean,
    onChangePlayState: (Boolean) -> Unit,
    progressSec: Int,
    onChangeProgress: (Int) -> Unit,
    speed: Float,
    onChangeSpeed: (Float) -> Unit,
    colors: Triple<Color, Color, Color>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp),
            colors = CardDefaults.cardColors(containerColor = colors.first.copy(alpha = 0.9f)),
            border = BorderStroke(1.dp, AcademicGold.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Audiobook Headset icon
                Icon(
                    imageVector = Icons.Default.Headset,
                    contentDescription = null,
                    tint = AcademicGold,
                    modifier = Modifier.size(64.dp)
                )

                Text(
                    text = "Professional Audiobook Companion",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = colors.second
                )

                Text(
                    text = "Chapter 2: Literary Dialects and Accent Contexts",
                    fontSize = 12.sp,
                    color = colors.third
                )

                // Simulated Interactive Waveform
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0..24) {
                        val barHeight = remember { (10..40).random() }
                        val motionFactor = if (isPlaying) Math.sin((progressSec + i).toDouble()).toFloat() * 10 else 0f
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(((barHeight + motionFactor).coerceIn(4f, 48f)).dp)
                                .background(
                                    if (i * (180 / 25) < progressSec) AcademicGold else colors.third.copy(
                                        alpha = 0.3f
                                    ), RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }

                // Slider duration mechanics
                Slider(
                    value = progressSec.toFloat(),
                    onValueChange = { onChangeProgress(it.toInt()) },
                    valueRange = 0f..180f,
                    colors = SliderDefaults.colors(
                        thumbColor = AcademicGold,
                        activeTrackColor = AcademicGold
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "%02d:%02d", progressSec / 60, progressSec % 60),
                        fontSize = 11.sp,
                        color = colors.second
                    )
                    Text(text = "03:00", fontSize = 11.sp, color = colors.second)
                }

                // Controls row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Backward
                    IconButton(onClick = { onChangeProgress((progressSec - 15).coerceIn(0, 180)) }) {
                        Icon(Icons.Default.FastRewind, contentDescription = "Rewind", tint = colors.second)
                    }

                    // Play/Pause
                    IconButton(
                        onClick = { onChangePlayState(!isPlaying) },
                        modifier = Modifier
                            .size(56.dp)
                            .background(AcademicGold, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Trigger play",
                            tint = Color.Black
                        )
                    }

                    // Forward 15s
                    IconButton(onClick = { onChangeProgress((progressSec + 15).coerceIn(0, 180)) }) {
                        Icon(Icons.Default.FastForward, contentDescription = "Forward link", tint = colors.second)
                    }
                }

                // Speed buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Speed:", fontSize = 11.sp, color = colors.third)
                    listOf(1f, 1.25f, 1.5f, 2f).forEach { spd ->
                        TextButton(
                            onClick = { onChangeSpeed(spd) },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (speed == spd) AcademicGold else colors.third
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(text = "${spd}x", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// High resolution images and pinch gestures
@Composable
fun HighResImagePage(file: EducationalFile, colors: Triple<Color, Color, Color>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.first.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = file.virtualUrl,
            contentDescription = "Ebook page content illustration",
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Fit
        )
    }
}

// Slides layout PPTX
@Composable
fun AcademicSlideViewer(file: EducationalFile, pageIndex: Int, colors: Triple<Color, Color, Color>) {
    val text = file.contentPages.getOrNull(pageIndex) ?: "Welcome, Student of Literary Arts."
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.6f)
            .shadow(4.dp),
        colors = CardDefaults.cardColors(containerColor = colors.first.copy(alpha = 0.9f)),
        border = BorderStroke(2.dp, AcademicGold),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top slide theme banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACADEMIC PRESENTATION: SLIDE ${pageIndex + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AcademicGold,
                        letterSpacing = 1.2.sp
                    )
                    Icon(Icons.Default.Slideshow, contentDescription = null, tint = AcademicGold.copy(alpha = 0.5f))
                }

                // Core content body
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.second,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Slide metadata
                Text(
                    text = "High School Lecture Prep Material Series • Confidential School Copy",
                    fontSize = 9.sp,
                    color = colors.third,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// XLSX spreadsheet simulator
@Composable
fun SchoolGradebookSheet(colors: Triple<Color, Color, Color>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp)
            .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = colors.first.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AcademicGold.copy(alpha = 0.15f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.GridOn, contentDescription = null, tint = AcademicGold, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Gradebook_Lab2_Calculations.xlsx", fontWeight = FontWeight.Black, fontSize = 12.sp, color = colors.second)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Rows
            val headers = listOf("ID", "Student Name", "Lab 1 (40%)", "Lab 2 (60%)", "Total Grade")
            val students = listOf(
                listOf("001", "Alex Rivera", "95", "88", "90.8%"),
                listOf("002", "Chloe Chen", "90", "92", "91.2%"),
                listOf("003", "Daniel Miller", "85", "80", "82.0%"),
                listOf("004", "Max Foster", "92", "94", "93.2%"),
                listOf("005", "Emma Vance", "88", "90", "89.2%")
            )

            // Header Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                headers.forEach { h ->
                    Text(
                        text = h,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = colors.second,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Divider(color = colors.third.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 6.dp))

            students.forEach { std ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    std.forEach { cell ->
                        Text(
                            text = cell,
                            fontSize = 11.sp,
                            color = colors.second,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// Immersive video simulator layout with controls overlays (Speed controls, PiP and Rotate landscape ratios)
@Composable
fun VideoLecturerSimulator(
    isPlaying: Boolean,
    onChangePlayState: (Boolean) -> Unit,
    speed: Float,
    onChangeSpeed: (Float) -> Unit,
    subtitleText: String,
    onPiPToggle: () -> Unit,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    colors: Triple<Color, Color, Color>
) {
    var isLandscapeAspect by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(if (isLandscapeAspect) 2.1f else 1.33f)
            .background(Color.Black)
            .shadow(6.dp)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Simple animation loop screen
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top overlay bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COMPANION LECTURE SCREEN [Offline-Cached]",
                    color = AcademicGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // PiP
                    IconButton(onClick = onPiPToggle, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.PictureInPicture, contentDescription = "PiP Link", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    // Rotate
                    IconButton(onClick = { isLandscapeAspect = !isLandscapeAspect }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.ScreenRotation, contentDescription = "Landscape flip", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Central icon / animation loop
            IconButton(
                onClick = { onChangePlayState(!isPlaying) },
                modifier = Modifier
                    .size(56.dp)
                    .background(AcademicGold.copy(alpha = 0.82f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Trigger media status",
                    tint = Color.Black
                )
            }

            // Subtitle text engine
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (subtitleText.isNotEmpty()) {
                    Text(
                        text = subtitleText,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Playback bar and Speed Control
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Speed: ${speed}x", color = Color.White, fontSize = 9.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(1f, 1.5f, 2f).forEach { sp ->
                            Text(
                                text = "${sp}x",
                                color = if (speed == sp) AcademicGold else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .clickable { onChangeSpeed(sp) }
                                    .padding(horizontal = 4.dp)
                                    .testTag("playback_speed_$sp")
                            )
                        }
                    }
                }
            }
        }
    }
}

// Reading Text Pages Engine (EPUB / DOCX / TXT / RTF / PDF) with Highlight selection detection
@Composable
fun BookPagesTextEngine(
    file: EducationalFile,
    currentPage: Int,
    colors: Triple<Color, Color, Color>,
    textSelection: Pair<Int, Int>?,
    onTextSelectionChange: (Pair<Int, Int>?) -> Unit,
    onSaveHighlight: (String) -> Unit
) {
    val paragraph = file.contentPages.getOrNull(currentPage) ?: "No companion texts available in this virtual format."
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        AcademicBookStampHeader(title = file.name, pageLabel = (currentPage + 1).toString(), color = colors.third)

        Spacer(modifier = Modifier.height(16.dp))

        // Document parsed contents with mock highlights / selections simulated
        SelectionSimulatorLayout(
            paragraph = paragraph,
            colors = colors,
            textSelection = textSelection,
            onTextSelectionChange = onTextSelectionChange
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectionSimulatorLayout(
    paragraph: String,
    colors: Triple<Color, Color, Color>,
    textSelection: Pair<Int, Int>?,
    onTextSelectionChange: (Pair<Int, Int>?) -> Unit
) {
    // We render the paragraph. If the user clicks any word, we can select a block
    // To keep it clean and interactive, let's render the text split into words that students can tap!
    val words = remember(paragraph) { paragraph.split(" ") }

    Text(
        text = "Tap words to toggle high-contrast highlighter selection range (Kindle style):",
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = AcademicGold,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start)
    ) {
        words.forEachIndexed { index, word ->
            val isSelected = textSelection != null && index >= textSelection.first && index <= textSelection.second
            Box(
                modifier = Modifier
                    .background(
                        color = if (isSelected) AcademicGold.copy(alpha = 0.5f) else Color.Transparent,
                        shape = RoundedCornerShape(2.dp)
                    )
                    .clickable {
                        if (textSelection == null) {
                            onTextSelectionChange(Pair(index, index))
                        } else {
                            val first = textSelection.first
                            if (index == first) {
                                onTextSelectionChange(null)
                            } else if (index < first) {
                                onTextSelectionChange(Pair(index, first))
                            } else {
                                onTextSelectionChange(Pair(first, index))
                            }
                        }
                    }
                    .padding(2.dp)
            ) {
                Text(
                    text = word,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Normal,
                    color = colors.second,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
fun AcademicBookStampHeader(title: String, pageLabel: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "E-BOOK RESOURCE: $title",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = color.copy(alpha = 0.6f)
        )
        Text(
            text = "PAGE $pageLabel",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = color.copy(alpha = 0.6f)
        )
    }
    Divider(color = color.copy(alpha = 0.15f), thickness = 1.dp, modifier = Modifier.padding(top = 4.dp))
}

// Annotation Sketch canvas overlay where coordinates are drawn, typed, saved dynamically
@Composable
fun SketchAnnotationLayout(
    bookId: String,
    currentPage: Int,
    selectedFile: EducationalFile,
    scaleTheme: String,
    currentDrawColor: Color,
    currentBrushSize: Float,
    onBrushColorChange: (Color) -> Unit,
    onBrushSizeChange: (Float) -> Unit,
    activeInkStrokes: MutableList<List<Offset>>,
    currentInkPoints: MutableList<Offset>,
    activeNotes: List<Pair<Offset, String>>,
    onAddPinnedNote: (Offset, String) -> Unit,
    onSaveAnnotations: () -> Unit,
    onClearAll: () -> Unit
) {
    val pageColors = when (scaleTheme) {
        "light" -> Triple(Color.White, Color.Black, Color.DarkGray)
        "sepia" -> Triple(Color(0xFFFDF6E3), Color(0xFF586E75), Color(0xFF657B83))
        else -> Triple(Color(0xFF15151A), Color(0xFFE0E0E6), Color.LightGray)
    }

    var showKeyboardNoteDialog by remember { mutableStateOf(false) }
    var inputNotePosition by remember { mutableStateOf(Offset.Zero) }
    var typedNoteText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pageColors.first)
    ) {
        // Overlay standard page representation beneath
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .align(Alignment.Center)
        ) {
            Text(
                text = "PEN & NOTES OVERLAY ANNOTATOR",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = AcademicGold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Interactive Drawing Ink Canvas Mode. Drag fingers/mouse below to sketch on pages. Tap anywhere to plant a persistent keyboard comment tag. Click Save to log into SQLite.",
                fontSize = 11.sp,
                color = pageColors.third
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            // Text excerpt page matching
            Text(
                text = selectedFile.contentPages.getOrNull(currentPage) ?: "Literary excerpt paragraph",
                style = MaterialTheme.typography.bodyMedium,
                color = pageColors.second.copy(alpha = 0.4f),
                fontFamily = FontFamily.Serif
            )
        }

        // Active drawing ink canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentInkPoints.add(offset)
                        },
                        onDrag = { change, dragAmount ->
                            currentInkPoints.add(change.position)
                        },
                        onDragEnd = {
                            if (currentInkPoints.isNotEmpty()) {
                                activeInkStrokes.add(currentInkPoints.toList())
                                currentInkPoints.clear()
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { tapOffset ->
                            inputNotePosition = tapOffset
                            showKeyboardNoteDialog = true
                        }
                    )
                }
        ) {
            // Draw completed lines
            activeInkStrokes.forEach { stroke ->
                val path = Path().apply {
                    if (stroke.isNotEmpty()) {
                        moveTo(stroke[0].x, stroke[0].y)
                        for (i in 1 until stroke.size) {
                            lineTo(stroke[i].x, stroke[i].y)
                        }
                    }
                }
                drawPath(path = path, color = currentDrawColor, style = Stroke(width = currentBrushSize, cap = StrokeCap.Round))
            }

            // Draw current active stroke
            if (currentInkPoints.isNotEmpty()) {
                val path = Path().apply {
                    moveTo(currentInkPoints[0].x, currentInkPoints[0].y)
                    for (i in 1 until currentInkPoints.size) {
                        lineTo(currentInkPoints[i].x, currentInkPoints[i].y)
                    }
                }
                drawPath(path = path, color = currentDrawColor, style = Stroke(width = currentBrushSize, cap = StrokeCap.Round))
            }
        }

        // Floating Note comment pins
        activeNotes.forEach { (pt, note) ->
            Box(
                modifier = Modifier
                    .offset(x = (pt.x / 2.72f).dp, y = (pt.y / 2.72f).dp) // simple responsive coordinate scaling
                    .background(AcademicGold, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Comment, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = note, color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Editor Toolbar (Floating at Bottom)
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .shadow(12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Color choices
                listOf(Color.Red, Color.Green, Color.Blue, Color.Cyan).forEach { clr ->
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(clr)
                            .border(if (currentDrawColor == clr) 2.dp else 0.dp, Color.White, CircleShape)
                            .clickable { onBrushColorChange(clr) }
                    )
                }
                
                Spacer(modifier = Modifier.width(4.dp))
                
                // Clear Brush Icon
                FilledTonalIconButton(onClick = onClearAll, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear ink", modifier = Modifier.size(16.dp))
                }

                // Save Annotation to Room DB
                Button(
                    onClick = onSaveAnnotations,
                    colors = ButtonDefaults.buttonColors(containerColor = AcademicGold),
                    modifier = Modifier.testTag("save_annotation_button")
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save to DB", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Black)
                }
            }
        }

        if (showKeyboardNoteDialog) {
            Dialog(onDismissRequest = { showKeyboardNoteDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Plant Typed Class Note Annotation",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = typedNoteText,
                            onValueChange = { typedNoteText = it },
                            placeholder = { Text("E.g., Key Exam revision question...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showKeyboardNoteDialog = false }) {
                                Text("Cancel")
                            }
                            TextButton(
                                onClick = {
                                    if (typedNoteText.isNotBlank()) {
                                        onAddPinnedNote(inputNotePosition, typedNoteText)
                                        typedNoteText = ""
                                    }
                                    showKeyboardNoteDialog = false
                                }
                            ) {
                                Text("Add Pin", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Dialog segment to show bookmarks and saved extracts SQLite queries list
@Composable
fun HighlightsAndBookmarksTab(
    bookId: String,
    currentPage: Int,
    totalPageCount: Int,
    bookmarks: List<BookBookmark>,
    highlights: List<BookHighlight>,
    scaleTheme: String,
    onJumpToPage: (Int) -> Unit,
    onAddBookmark: (String) -> Unit,
    onDeleteBookmark: (Int) -> Unit,
    onDeleteHighlight: (Int) -> Unit
) {
    val pageColors = when (scaleTheme) {
        "light" -> Triple(Color.White, Color.Black, Color.DarkGray)
        "sepia" -> Triple(Color(0xFFFDF6E3), Color(0xFF586E75), Color(0xFF657B83))
        else -> Triple(Color(0xFF15151A), Color(0xFFE0E0E6), Color.LightGray)
    }

    var showAddBookmarkDialog by remember { mutableStateOf(false) }
    var bookmarkNoteInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pageColors.first)
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 72.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SAVED STUDY REVISIONS",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = AcademicGold
                    )

                    Button(
                        onClick = { showAddBookmarkDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AcademicGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Bookmark", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Text(
                    text = "ACTIVE BOOKMARKS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = pageColors.second
                )
            }

            if (bookmarks.isEmpty()) {
                item {
                    Text(
                        text = "No study revisions bookmarked yet. Flip to a page and add a bookmark above.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(bookmarks) { bmk ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onJumpToPage(bmk.page) },
                        colors = CardDefaults.cardColors(containerColor = pageColors.first.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Page ${bmk.page + 1}", fontWeight = FontWeight.Bold, color = AcademicGold)
                                Text(bmk.note, fontSize = 12.sp, color = pageColors.second)
                            }
                            IconButton(onClick = { onDeleteBookmark(bmk.id) }) {
                                Icon(Icons.Default.DeleteForever, contentDescription = "Delete bookmark", tint = Color.Red)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "SAVED PARAGRAPH HIGHLIGHTS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = pageColors.second
                )
            }

            if (highlights.isEmpty()) {
                item {
                    Text(
                        text = "No excerpts highlighted yet. Select words on the document viewer to highlighted and click Save.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(highlights) { high ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onJumpToPage(high.page) },
                        colors = CardDefaults.cardColors(containerColor = pageColors.first.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Page ${high.page + 1}", fontWeight = FontWeight.Bold, color = AcademicGold)
                                IconButton(onClick = { onDeleteHighlight(high.id) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete highlight", tint = Color.Red, modifier = Modifier.size(20.dp))
                                }
                            }
                            Text(
                                text = "\"${high.text}\"",
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Serif,
                                color = pageColors.second,
                                modifier = Modifier
                                    .background(Color(android.graphics.Color.parseColor(high.colorHex)).copy(alpha = 0.35f))
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }

        if (showAddBookmarkDialog) {
            Dialog(onDismissRequest = { showAddBookmarkDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Add Bookmark at Page ${currentPage + 1}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = bookmarkNoteInput,
                            onValueChange = { bookmarkNoteInput = it },
                            placeholder = { Text("Study notes: Revision for quiz chapter...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showAddBookmarkDialog = false }) {
                                Text("Cancel")
                            }
                            TextButton(
                                onClick = {
                                    if (bookmarkNoteInput.isNotBlank()) {
                                        onAddBookmark(bookmarkNoteInput)
                                        bookmarkNoteInput = ""
                                    }
                                    showAddBookmarkDialog = false
                                }
                            ) {
                                Text("Bookmark", fontWeight = FontWeight.Bold, color = AcademicGold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Companion lists creator mapping standard files
private fun getCompanionFilesForBook(book: Book): List<EducationalFile> {
    return listOf(
        EducationalFile(
            id = "${book.id}-pdf",
            name = "Textbook Edition.pdf",
            format = FileFormat.PDF,
            size = "2.4 MB",
            virtualUrl = book.coverUrl,
            contentPages = listOf(
                "Chapter 1 Overview: High-school academic principles. In this foundational curriculum phase, we explore the depth of language structures, metaphors, and narrative design paradigms. It forms the basis of critical essay compilation.",
                "The evolution of modern society highlights various themes. Prominent authors emphasize the friction between technology expansion and classic human connection. These sections are crucial for the senior exam boards.",
                "When preparing study notes, separate logical ideas. Use margins to plant comments. Identify keywords, highlight crucial dates, and summarize paragraphs."
            )
        ),
        EducationalFile(
            id = "${book.id}-epub",
            name = "Digital Edition.epub",
            format = FileFormat.EPUB,
            size = "1.1 MB",
            virtualUrl = book.coverUrl,
            contentPages = listOf(
                "Digital Epub Flowing Content. Flip fluidly between chapters. EPUB is optimized for screens and devices with dynamic scaling.",
                "Ebook resources are offline-cached. Perfect for school bus rides and remote study hours."
            )
        ),
        EducationalFile(
            id = "${book.id}-docx",
            name = "Home Syllabus.docx",
            format = FileFormat.DOCX,
            size = "120 KB",
            virtualUrl = book.coverUrl,
            contentPages = listOf("Syllabus details for ${book.title}. Make sure to complete the reading schedule on the school boards before the first assessment segment.")
        ),
        EducationalFile(
            id = "${book.id}-lecture",
            name = "Lecture Brief.mp4",
            format = FileFormat.MP4,
            size = "14.5 MB",
            virtualUrl = "videoloop"
        ),
        EducationalFile(
            id = "${book.id}-audiobook",
            name = "Audio Podcast.mp3",
            format = FileFormat.MP3,
            size = "8.2 MB",
            virtualUrl = "audioloop"
        ),
        EducationalFile(
            id = "${book.id}-pres",
            name = "Syllabus Slides.pptx",
            format = FileFormat.PPTX,
            size = "3.8 MB",
            virtualUrl = "",
            contentPages = listOf(
                "Goal: High School Excellence\n\n1. Active Reading Habit\n2. Maintain consistent book borrow records\n3. Engage with class discussion panels.",
                "Core Syllabus Focus:\n\n• Analysis techniques\n• Note structuring using SQLite\n• Review submission guidelines for assignments"
            )
        ),
        EducationalFile(
            id = "${book.id}-scanned",
            name = "Scanned Original.jpg",
            format = FileFormat.JPG,
            size = "1.5 MB",
            virtualUrl = book.coverUrl
        ),
        EducationalFile(
            id = "${book.id}-lab",
            name = "Lab Sheets.xlsx",
            format = FileFormat.XLSX,
            size = "45 KB",
            virtualUrl = ""
        )
    )
}

// Simple coordinate custom drawing serialization
private fun serializeStrokes(strokes: List<List<Offset>>): String {
    return strokes.joinToString(separator = "|") { stroke ->
        stroke.joinToString(separator = ";") { pt -> "${pt.x},${pt.y}" }
    }
}

private fun deserializeStrokes(str: String): List<List<Offset>> {
    if (str.isBlank()) return emptyList()
    return try {
        str.split("|").map { strokeStr ->
            strokeStr.split(";").map { ptStr ->
                val coords = ptStr.split(",")
                Offset(coords[0].toFloat(), coords[1].toFloat())
            }
        }
    } catch (e: Exception) {
        emptyList()
    }
}

private fun serializeNotes(notes: List<Pair<Offset, String>>): String {
    return notes.joinToString(separator = "|") { (pt, text) -> "${pt.x},${pt.y}:$text" }
}

private fun deserializeNotes(str: String): List<Pair<Offset, String>> {
    if (str.isBlank()) return emptyList()
    return try {
        str.split("|").map { noteStr ->
            val parts = noteStr.split(":")
            val coords = parts[0].split(",")
            Offset(coords[0].toFloat(), coords[1].toFloat()) to parts[1]
        }
    } catch (e: Exception) {
        emptyList()
    }
}

@Composable
fun MetricDivider() {
    Divider(color = Color.Gray.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
fun Modifier.topBarSegmentBorder() = this.shadow(1.dp)

@Composable
fun ZoomableBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(scale: Float, offset: Offset) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        offset += offsetChange
        if (scale == 1f) {
            offset = Offset.Zero
        }
    }
    Box(
        modifier = modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
            .transformable(state = state)
    ) {
        content(scale, offset)
    }
}

