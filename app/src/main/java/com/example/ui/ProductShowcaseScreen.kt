package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun ProductShowcaseScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Interactive States
    var selectedFeatureIndex by remember { mutableStateOf(0) }
    var activeTimerSeconds by remember { mutableStateOf(462) } // 7:42 representation
    var selectedMockOption by remember { mutableStateOf<Int?>(null) } // null by default, 2 is mitochondrion

    // Core Theme Gradients
    val darkBackgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF070C19),
            Color(0xFF090F1F)
        )
    )

    // Countdown Timer animation simulate ticking
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            if (activeTimerSeconds > 0) activeTimerSeconds--
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(darkBackgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("product_showcase_root")
    ) {
        // SVG/HTML Simulated Grid Drawing Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val density = this.density
                    val gridWidth = 60.dp.toPx()
                    val gridHeight = 60.dp.toPx()
                    
                    // Soft teal-gold background grid lines
                    val pulseAlpha = 0.03f
                    val linePaint = Color(0xFFD4A843).copy(alpha = pulseAlpha)
                    
                    // Vertical lines
                    var x = 0f
                    while (x < size.width) {
                        drawLine(
                            color = linePaint,
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                        x += gridWidth
                    }
                    
                    // Horizontal lines
                    var y = 0f
                    while (y < size.height) {
                        drawLine(
                            color = linePaint,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                        y += gridHeight
                    }
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFD4A843), Color(0xFFF0C860))
                                ),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "N",
                            color = Color(0xFF0B1220),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Column {
                        Text(
                            text = "Nexus Library",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Uganda Academic Showcase",
                            fontSize = 9.sp,
                            color = Color(0xFF6B7A94)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF1E2A40).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Showcase",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 1. HERO SECTION ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Live-pulse Eyebrow Badge
                Row(
                    modifier = Modifier
                        .background(Color(0xFFD4A843).copy(alpha = 0.12f), RoundedCornerShape(100.dp))
                        .border(1.dp, Color(0xFFD4A843).copy(alpha = 0.25f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Eyebrow pulse dot
                    val infiniteTransition = rememberInfiniteTransition(label = "badge_pulse")
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 0.3f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse_alpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFFD4A843).copy(alpha = pulseAlpha), CircleShape)
                    )

                    Text(
                        text = "BUILT FOR UGANDAN SECONDARY SCHOOLS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD4A843),
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // main headline with gold gradient text
                Text(
                    text = "Your school's\nacademic command centre",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    lineHeight = 36.sp,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFD4A843),
                                Color(0xFFF0C860),
                                Color(0xFFFFF5D6)
                            )
                        )
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Digital curriculum vaults, UNEB exam simulators, collaborative subject forums, and enterprise library management — all in one offline-capable Android app.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFF6B7A94),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Stats Bar (S1-S6 coverage, 5 roles, offline capability, UNEB alignment)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E2A40).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(
                        "S1–S6" to "Senior Levels",
                        "5" to "User Roles",
                        "100%" to "Offline Work",
                        "UNEB" to "Aligned"
                    ).forEachIndexed { ind, (num, label) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = num,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 16.sp,
                                color = Color(0xFFD4A843)
                            )
                            Text(
                                text = label,
                                fontSize = 8.sp,
                                color = Color(0xFF6B7A94)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── 2. ROLE-BASED WORKSPACES ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFD4A843).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ROLE-BASED WORKSPACES",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD4A843)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Built for every profile in the school",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )

                Text(
                    text = "The interface adapts dynamically to your role — each user sees only their tools.",
                    fontSize = 11.sp,
                    color = Color(0xFF6B7A94),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 4 Roles Grid / Scroll
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RoleCardDisplay(
                        emoji = "🎒",
                        roleName = "Student Portal",
                        description = "Borrow textbooks, review reading progress, take simulation exams with instant scores, and build daily study habits.",
                        tags = listOf("Past Papers", "Flashcards", "Study Goals", "XP Levels"),
                        color = Color(0xFF3B82F6),
                        accentBg = Color(0xFF3B82F6).copy(alpha = 0.1f)
                    )

                    RoleCardDisplay(
                        emoji = "📐",
                        roleName = "Teacher Desk",
                        description = "Assign materials to your class Streams, compile structured quiz answers, check reading statistics, and upload syllabus updates.",
                        tags = listOf("Assignments", "Quizzes", "Resource Upload", "Verified Badges"),
                        color = Color(0xFF10B981),
                        accentBg = Color(0xFF10B981).copy(alpha = 0.1f)
                    )

                    RoleCardDisplay(
                        emoji = "📚",
                        roleName = "Librarian Terminal",
                        description = "Supervise book status logs, barcode scan items, catalog school references, view borrow histories, and manage fines.",
                        tags = listOf("Active Ledger", "Scan Barcodes", "Fine Records", "Raw XML/CSV"),
                        color = Color(0xFFF59E0B),
                        accentBg = Color(0xFFF59E0B).copy(alpha = 0.1f)
                    )

                    RoleCardDisplay(
                        emoji = "🏛️",
                        roleName = "Admin / Super Admin",
                        description = "Access analytics indicators, create student accounts, manage classes/Stream combinations, clear caches, and publish newsletters.",
                        tags = listOf("SaaS Sync Console", "Register Accounts", "Moderation Hub", "Syllabus Base"),
                        color = Color(0xFFEF4444),
                        accentBg = Color(0xFFEF4444).copy(alpha = 0.1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── 3. FEATURES PANEL WITH SIMULATOR ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1BC0A0).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "CORE CAPABILITIES Showcase",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1BC0A0)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Everything for academic success",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )

                Text(
                    text = "Tap on features to inspect their simulated layouts below.",
                    fontSize = 11.sp,
                    color = Color(0xFF6B7A94),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 6 Features Selector Tabs
                val featuresList = listOf(
                    Triple("01", "UNEB Mock Exam Simulator", "Timed simulation tests using official UNEB paper constraints, grading guidelines, and live countdown timers."),
                    Triple("02", "Curriculum Resource Vaults", "Subject-specific files categorized from Senior 1 to Senior 6 containing books, mock questions, and flashcards."),
                    Triple("03", "UCE Division Target Estimator", "Real-time point summary computations according to official aggregate values to map out tertiary pathways."),
                    Triple("04", "AI Librarian (Auden AI)", "Gemini-powered personalized teaching assistant providing custom quiz feedback and complex formula breakdowns."),
                    Triple("05", "Listen Mode (Native TTS Narrator)", "Offline voice synthesizer reads text lessons out loud supporting adjustable playback paces (0.75x - 2.0x)."),
                    Triple("06", "Enterprise Sync & Control", "Automated backup databases keeping local changes and server data fully aligned during intermittent network situations.")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    featuresList.forEachIndexed { idx, feature ->
                        Card(
                            onClick = { selectedFeatureIndex = idx },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedFeatureIndex == idx) Color(0xFF1E2A40) else Color(0xFF121B2A)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (selectedFeatureIndex == idx) Color(0xFFD4A843) else Color.White.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = feature.first,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedFeatureIndex == idx) Color(0xFFD4A843) else Color(0xFF6B7A94)
                                )
                                Text(
                                    text = feature.second,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedFeatureIndex == idx) Color.White else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }

                // Selected Feature Detail Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF121B2A), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = featuresList[selectedFeatureIndex].second,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFFD4A843)
                        )
                        Text(
                            text = featuresList[selectedFeatureIndex].third,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // HIGH FIDELITY SIMULATION COMPONENT CONTAINER
                Text(
                    text = "Live Simulated Feature Interface",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7A94).copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF121B2A), RoundedCornerShape(20.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                ) {
                    Column {
                        // Top simulated URL bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.04f))
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFFFF5F57), CircleShape))
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFFFEBC2E), CircleShape))
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFF28C840), CircleShape))
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(100.dp))
                                    .padding(vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "nexus://exam-simulator/s4-biology",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.sp,
                                    color = Color(0xFF6B7A94)
                                )
                            }
                        }

                        // Simulated Window Content
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            when (selectedFeatureIndex) {
                                0 -> {
                                    // UNEB Mock Exam Simulator UI
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = Color(0xFF1BC0A0).copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "S4 BIOLOGY · UNEB 2024",
                                                color = Color(0xFF1BC0A0),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 8.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        val minutes = activeTimerSeconds / 60
                                        val seconds = activeTimerSeconds % 60
                                        Text(
                                            text = String.format("%02d:%02d", minutes, seconds),
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFFFFB703)
                                        )
                                    }

                                    // Custom Linear Progress bar
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(2.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(0.65f) // 65% completion progress
                                                .background(Color(0xFF1BC0A0), RoundedCornerShape(2.dp))
                                        )
                                    }

                                    Text(
                                        text = "Question 2 of 3",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )

                                    Text(
                                        text = "Which cellular component is responsible for ATP synthesis through oxidative phosphorylation?",
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp,
                                        color = Color(0xFFC8D0E0)
                                    )

                                    // 4 MCQ Options
                                    val mcqOptions = listOf(
                                        "Ribosome",
                                        "Chloroplast",
                                        "Mitochondrion",
                                        "Lysosome"
                                    )

                                    mcqOptions.forEachIndexed { index, option ->
                                        val isSelected = selectedMockOption == index
                                        var progressColor = if (isSelected) Color(0xFF1BC0A0) else Color.White.copy(alpha = 0.1f)
                                        
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    if (isSelected) Color(0xFF1BC0A0).copy(alpha = 0.08f) else Color.White.copy(alpha = 0.03f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) Color(0xFF1BC0A0) else Color.White.copy(alpha = 0.08f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable { selectedMockOption = index }
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .border(1.dp, if (isSelected) Color(0xFF1BC0A0) else Color(0xFF6B7A94), CircleShape)
                                                    .background(if (isSelected) Color(0xFF1BC0A0) else Color.Transparent, CircleShape)
                                            )
                                            Text(
                                                text = "${'A' + index}. $option",
                                                fontSize = 10.sp,
                                                color = if (isSelected) Color.White else Color(0xFFC8D0E0)
                                            )
                                        }
                                    }
                                }
                                1 -> {
                                    // Curriculum Resource Vaults mockup
                                    Text(
                                        text = "📂 DIGITAL CURRICULUM VAULTS",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD4A843),
                                        fontSize = 10.sp
                                    )
                                    
                                    val resourceCards = listOf(
                                        "S4 Pure Mathematics P2" to "3.4 MB · 12 Worked solutions (2023 Paper)",
                                        "S3 Chemistry Organic Compounds" to "1.2 MB · Formulas cheat sheet & interactive flashcards",
                                        "S6 Physics Electromagnetic Induction" to "4.8 MB · 3 Audio narration lectures cached offline"
                                    )

                                    resourceCards.forEach { card ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = card.first, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                Text(text = card.second, fontSize = 8.sp, color = Color(0xFF6B7A94))
                                            }
                                            Icon(
                                                imageVector = Icons.Default.CloudDownload,
                                                contentDescription = "Offline Cache Available",
                                                tint = Color(0xFF1BC0A0),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                                2 -> {
                                    // UCE Division Target Estimator Mockup
                                    UgandanDivisionEstimatorShowcaseWidget()
                                }
                                3 -> {
                                    // AI Librarian (Auden AI) mockup
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(modifier = Modifier.size(20.dp).background(Color(0xFF9333EA), CircleShape), contentAlignment = Alignment.Center) {
                                                Text("🦉", fontSize = 10.sp)
                                            }
                                            Text("Auden AI Librarian", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF9333EA).copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0xFF9333EA).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                                .padding(10.dp)
                                        ) {
                                            Text(
                                                text = "Hello S4 class! Submit your essay skeleton or diagram layout below and I'll outline standard UNEB improvements, grading breakdowns, and point evaluations.",
                                                fontSize = 9.sp,
                                                lineHeight = 13.sp,
                                                color = Color(0xFFC8D0E0)
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth().height(28.dp).background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Ask Auden about 'Thermoregulation pathways'...", fontSize = 8.sp, color = Color(0xFF6B7A94))
                                            Icon(imageVector = Icons.Default.Send, contentDescription = "Submit", tint = Color(0xFF9333EA), modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                                4 -> {
                                    // Listen Mode (Native TTS) mockup
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Now Reading Notes: Plant Photosynthesis Labs", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Surface(color = Color(0xFFD4A843).copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                                Text("1.25x Active", color = Color(0xFFD4A843), fontSize = 7.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                            }
                                        }

                                        Text(
                                            text = "\"During cellular chloroplast excitation, light wavelength energies strike primary photosystem nodes causing immediate water molecule splitting and hydrogen ion accumulations in localized stroma membranes...\"",
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                            fontSize = 9.sp,
                                            lineHeight = 13.sp,
                                            color = Color(0xFF94A3B8)
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(onClick = {}) { Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp)) }
                                            IconButton(onClick = {}, modifier = Modifier.size(34.dp).background(Color(0xFFD4A843), CircleShape)) {
                                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF0B1220), modifier = Modifier.size(20.dp))
                                            }
                                            IconButton(onClick = {}) { Icon(imageVector = Icons.Default.SkipNext, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp)) }
                                        }
                                    }
                                }
                                else -> {
                                    // Enterprise Sync & Control mockup
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("SaaS Enterprise Security Status", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Box(modifier = Modifier.size(6.dp).background(Color(0xFF1BC0A0), CircleShape))
                                                Text("REST Sync Healthy", color = Color(0xFF1BC0A0), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Divider(color = Color.White.copy(alpha = 0.05f))

                                        val systemMetrics = listOf(
                                            "Offline Transaction Backlog" to "0 Lines cached pending sync",
                                            "Supabase DB Connection" to "Linked - Active REST secure endpoints",
                                            "System Cache Allocated" to "14.2 MB used / 250 MB Maximum limits"
                                        )

                                        systemMetrics.forEach { metric ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(text = metric.first, fontSize = 8.sp, color = Color(0xFF6B7A94))
                                                Text(text = metric.second, fontSize = 8.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── 4. OFFICIAL UNEB REFERENCE TABLE ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFB703).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "UCE GRADING LAWS",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB703)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Official National Grading Reference",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )

                Text(
                    text = "Understanding division cut-offs in the Uganda Certificate of Education (UCE) system.",
                    fontSize = 11.sp,
                    color = Color(0xFF6B7A94),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Layout Reference table
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF121B2A)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Header
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(text = "Division", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color(0xFF6B7A94), modifier = Modifier.weight(1.2f))
                            Text(text = "Aggregates", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color(0xFF6B7A94), modifier = Modifier.weight(1f))
                            Text(text = "Requirements", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color(0xFF6B7A94), modifier = Modifier.weight(1.8f))
                        }

                        Divider(color = Color.White.copy(alpha = 0.1f))

                        val divisionList = listOf(
                            Triple("Division 1", "8 – 32 pts", "Must pass English & Mathematics"),
                            Triple("Division 2", "33 – 45 pts", "Must pass English (Credit level)"),
                            Triple("Division 3", "46 – 58 pts", "Must pass at least 6 subjects content"),
                            Triple("Division 4", "59 – 72 pts", "Must pass at least 5 subjects content"),
                            Triple("Division 9", "73+ pts / Fail", "Below minimum requirements")
                        )

                        divisionList.forEach { (div, pts, req) ->
                            val color = when (div) {
                                "Division 1" -> Color(0xFF1BC0A0)
                                "Division 2" -> Color(0xFFD4A843)
                                "Division 3" -> Color(0xFF3B82F6)
                                "Division 4" -> Color(0xFF9333EA)
                                else -> Color(0xFFEF4444)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = div, fontWeight = FontWeight.Bold, fontSize = 9.5.sp, color = color, modifier = Modifier.weight(1.2f))
                                Text(text = pts, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = Color.White, modifier = Modifier.weight(1f))
                                Text(text = req, fontSize = 8.5.sp, color = Color(0xFF94A3B8), modifier = Modifier.weight(1.8f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── 5. SUBJECTS GRID ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF3B82F6).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "CURRICULUM ARCHIVE",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3B82F6)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Full UNEB Syllabus Coverage",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )

                Text(
                    text = "Providing notes, guidelines and timed simulation mock archives for UCE subjects.",
                    fontSize = 11.sp,
                    color = Color(0xFF6B7A94),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Layout Subjects Cards
                val subjectsGridItems = listOf(
                    Triple("🔬 Sciences", "Biology, Chemistry, Physics & Ag. Dynamic formulas tracker.", listOf("BIOLOGY", "CHEMISTRY", "PHYSICS")),
                    Triple("📐 Mathematics", "S1 - S4 Pure & Applied papers, complete matrices cheatsheets.", listOf("MATHS P1", "MATHS P2", "GEOMETRY")),
                    Triple("📖 Humanities", "English, Literature, Geography with live maps and History skeletal guides.", listOf("ENGLISH", "HISTORY", "GEOGRAPHY")),
                    Triple("💼 Electives / Commerce", "Commerce, Accounts, and Subsidiary ICT templates.", listOf("ACCOUNTS", "COMMERCE", "ICT")),
                    Triple("🖥️ Applied Sciences", "Computer Studies databases, project rubrics, and Fine Art outlines.", listOf("COMPUTING", "DRAWING")),
                    Triple("🌍 A-Level Bridging", "Advanced S5-S6 principal packages including GP (General Paper).", listOf("GENERAL PAPER", "A-LEVEL BIO"))
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    subjectsGridItems.forEach { (subName, subDesc, subTags) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF121B2A)),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = subName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                Text(text = subDesc, fontSize = 10.sp, color = Color(0xFF94A3B8))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    subTags.forEach { tag ->
                                        Surface(color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(4.dp)) {
                                            Text(
                                                text = tag,
                                                fontSize = 7.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF6B7A94),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── 6. TECH STACK PILLS GROUP ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF9333EA).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "SYSTEM ARCHITECTURE FLAGS",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9333EA)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "High Fidelity SDK Stack",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )

                Text(
                    text = "Active code integrations configured for performance.",
                    fontSize = 11.sp,
                    color = Color(0xFF6B7A94),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Pills Flexbox row simulator
                val techStack = listOf(
                    "Jetpack Compose" to Color(0xFF6200EE),
                    "Kotlin Coroutines" to Color(0xFF4FC3F7),
                    "Room SQLite DB" to Color(0xFF1BC0A0),
                    "Supabase REST Sync" to Color(0xFF0F8A72),
                    "Gemini AI API" to Color(0xFFD4A843),
                    "Firebase Services" to Color(0xFFFF6B6B),
                    "WorkManager Tasker" to Color(0xFF888888),
                    "Retrofit Connections" to Color(0xFF5B6FFF),
                    "Coil Caching" to Color(0xFFF0C860),
                    "Navigation Safe Keys" to Color(0xFFBB99FF),
                    "DataStore Prefs" to Color(0xFF334155)
                )

                // Layout pills wrap-like scroll
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    techStack.chunked(3).forEach { columnPills ->
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            columnPills.forEach { (techName, dotColor) ->
                                Row(
                                    modifier = Modifier
                                        .background(Color(0xFF121B2A), RoundedCornerShape(100.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(100.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(dotColor, CircleShape)
                                    )
                                    Text(
                                        text = techName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFC8D0E0)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Footer Download CTA Block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF121B2A))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.04f)))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Ready to achieve Distinction status?",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Download the Nexus educational ledger directly, collaborate on study streams, and query custom tutor outlines.",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = Color(0xFF6B7A94),
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4A843)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = "Access Login Portals Now",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0B1220),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "© 2026 Nexus Tech Solutions · Uganda Certificate of Education Alignment",
                    fontSize = 8.sp,
                    color = Color(0xFF6B7A94)
                )
            }
        }
    }
}

@Composable
private fun RoleCardDisplay(
    emoji: String,
    roleName: String,
    description: String,
    tags: List<String>,
    color: Color,
    accentBg: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121B2A)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(accentBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 16.sp)
                }
                Text(
                    text = roleName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }

            Text(
                text = description,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                color = Color(0xFF94A3B8)
            )

            // Tags
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tags.forEach { tag ->
                    Surface(
                        color = accentBg,
                        border = BorderStroke(0.5.dp, color.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = tag,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = color,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UgandanDivisionEstimatorShowcaseWidget() {
    var englishArg by remember { mutableStateOf(1) }
    var mathsArg by remember { mutableStateOf(2) }
    var biologyArg by remember { mutableStateOf(1) }
    var physicsArg by remember { mutableStateOf(3) }
    var chemistryArg by remember { mutableStateOf(2) }
    var geographyArg by remember { mutableStateOf(2) }

    fun labelForGrade(grade: Int): String = when (grade) {
        1 -> "D1"
        2 -> "D2"
        3 -> "C3"
        4 -> "C4"
        5 -> "C5"
        6 -> "C6"
        7 -> "P7"
        8 -> "P8"
        else -> "F9"
    }

    val totalPoints = englishArg + mathsArg + biologyArg + physicsArg + chemistryArg + geographyArg + 1 + 1 // static 8 subjects simulation
    val passesMathsAndEnglish = englishArg <= 6 && mathsArg <= 6
    val division = when {
        totalPoints <= 32 && passesMathsAndEnglish -> "Division 1"
        totalPoints <= 45 && englishArg <= 8 -> "Division 2"
        totalPoints <= 58 -> "Division 3"
        totalPoints <= 72 -> "Division 4"
        else -> "Division 9"
    }

    val divisionColor = when (division) {
        "Division 1" -> Color(0xFF1BC0A0)
        "Division 2" -> Color(0xFFD4A843)
        "Division 3" -> Color(0xFF3B82F6)
        "Division 4" -> Color(0xFF9333EA)
        else -> Color(0xFFEF4444)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Interactive Target Points Calculator",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD4A843)
        )

        val subjects = listOf(
            "English" to { englishArg },
            "Maths" to { mathsArg },
            "Biology" to { biologyArg },
            "Physics" to { physicsArg },
            "Chemistry" to { chemistryArg },
            "Geography" to { geographyArg }
        )

        subjects.forEachIndexed { sIdx, (name, getter) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = name, fontSize = 9.sp, color = Color(0xFF94A3B8), modifier = Modifier.width(60.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "-",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFD4A843),
                        modifier = Modifier
                            .clickable {
                                val cur = getter()
                                if (cur > 1) {
                                    when (sIdx) {
                                        0 -> englishArg--
                                        1 -> mathsArg--
                                        2 -> biologyArg--
                                        3 -> physicsArg--
                                        4 -> chemistryArg--
                                        5 -> geographyArg--
                                    }
                                }
                            }
                            .padding(horizontal = 8.dp)
                    )

                    Text(
                        text = labelForGrade(getter()),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.width(24.dp),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "+",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFD4A843),
                        modifier = Modifier
                            .clickable {
                                val cur = getter()
                                if (cur < 9) {
                                    when (sIdx) {
                                        0 -> englishArg++
                                        1 -> mathsArg++
                                        2 -> biologyArg++
                                        3 -> physicsArg++
                                        4 -> chemistryArg++
                                        5 -> geographyArg++
                                    }
                                }
                            }
                            .padding(horizontal = 8.dp)
                    )
                }
            }
        }

        Divider(color = Color.White.copy(alpha = 0.05f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(8.dp))
                .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Total Aggregates: $totalPoints pts", fontSize = 9.sp, color = Color(0xFF94A3B8))
                Text(
                    text = if (passesMathsAndEnglish) "Core subjects passed" else "English or Math Warning",
                    fontSize = 7.5.sp,
                    color = if (passesMathsAndEnglish) Color(0xFF1BC0A0) else Color(0xFFEF4444)
                )
            }

            Surface(color = divisionColor, shape = RoundedCornerShape(4.dp)) {
                Text(
                    text = division,
                    color = Color(0xFF070C19),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
