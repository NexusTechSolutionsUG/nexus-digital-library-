package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ReadingChallenge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StudentProgressDashboardScreen(viewModel: LibraryViewModel) {
    var dashboardSubTab by remember { mutableIntStateOf(0) } // 0 = Challenges, 1 = Bulletin Whiteboard

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Dual-Tab Slider Selector (Challenges vs legacy School Bulletin)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isSystemInDarkTheme()) Color(0xFF1E1B4B).copy(alpha = 0.4f) else Color.White,
                    RoundedCornerShape(12.dp)
                )
                .border(
                    1.dp,
                    if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.1f) else BorderGray,
                    RoundedCornerShape(12.dp)
                )
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (dashboardSubTab == 0) {
                            if (isSystemInDarkTheme()) ScholasticNavyLight.copy(alpha = 0.15f) else SoftGray
                        } else Color.Transparent
                    )
                    .clickable { dashboardSubTab = 0 }
                    .padding(vertical = 10.dp)
                    .testTag("dashboard_subtab_challenges"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (dashboardSubTab == 0) AcademicGoldLight else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Challenges",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dashboardSubTab == 0) {
                            if (isSystemInDarkTheme()) Color.White else ScholasticNavy
                        } else Color.Gray
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (dashboardSubTab == 1) {
                            if (isSystemInDarkTheme()) ScholasticNavyLight.copy(alpha = 0.15f) else SoftGray
                        } else Color.Transparent
                    )
                    .clickable { dashboardSubTab = 1 }
                    .padding(vertical = 10.dp)
                    .testTag("dashboard_subtab_bulletin"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (dashboardSubTab == 1) AcademicGoldLight else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "School Whiteboard",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dashboardSubTab == 1) {
                            if (isSystemInDarkTheme()) Color.White else ScholasticNavy
                        } else Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Subtab rendering
        Box(modifier = Modifier.weight(1f)) {
            if (dashboardSubTab == 0) {
                ReadingChallengesTab(viewModel = viewModel)
            } else {
                CampusNewsTab(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ReadingChallengesTab(viewModel: LibraryViewModel) {
    val context = LocalContext.current
    val xpPoints by viewModel.currentXpPoints.collectAsState()
    val levelRank by viewModel.currentLevelRank.collectAsState()
    val readingStreak by viewModel.readingStreak.collectAsState()
    val allBorrowRecords by viewModel.allBorrowRecords.collectAsState()
    val challenges by viewModel.readingChallenges.collectAsState()
    val dailyLog by viewModel.dailyReadingLog.collectAsState()

    val totalBooksCompleted = remember(allBorrowRecords) {
        allBorrowRecords.count { it.returnDate != null }
    }

    val badgesCount = remember(challenges) {
        challenges.count { it.isClaimed }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // XP PROGRESS LEVEL METER CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSystemInDarkTheme()) Color(0xFF1E1B4B).copy(alpha = 0.5f) else Color.White
                ),
                border = BorderStroke(
                    1.dp,
                    if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.15f) else BorderGray
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ACADEMIC RANK",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AcademicGoldLight,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = levelRank,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSystemInDarkTheme()) Color.White else ScholasticNavy
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    ScholarGoldBrush(),
                                    CircleShape
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Stars,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$xpPoints XP",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Simulated Levelling targets: level up tier is 3200 XP
                    val targetXP = 3200
                    val currentRatio = (xpPoints.toFloat() / targetXP.toFloat()).coerceIn(0f, 1f)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "XP Progress Indicator (Leveling system)",
                            fontSize = 8.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "${(currentRatio * 100).toInt()}% towards UNEB Champion Elite",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.8f) else ScholasticNavy
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Custom dynamic linear progress tracker bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(
                                if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.1f) else BorderGray,
                                CircleShape
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(currentRatio)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(ScholasticNavy, SageGreen)
                                    ),
                                    CircleShape
                                )
                        )
                    }
                }
            }
        }

        // MILESTONE STATS WRAPPER IN A ROW
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Streak Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(90.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSystemInDarkTheme()) Color(0xFF1E1B4B).copy(alpha = 0.3f) else Color.White
                    ),
                    border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.08f) else BorderGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = "Reading Streak",
                            tint = Color(0xFFFF5722),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$readingStreak Days",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Reading Streak",
                            fontSize = 8.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Books Completed Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(90.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSystemInDarkTheme()) Color(0xFF1E1B4B).copy(alpha = 0.3f) else Color.White
                    ),
                    border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.08f) else BorderGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SageGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$totalBooksCompleted Books",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Returns Completed",
                            fontSize = 8.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Badges Won Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(90.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSystemInDarkTheme()) Color(0xFF1E1B4B).copy(alpha = 0.3f) else Color.White
                    ),
                    border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.08f) else BorderGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$badgesCount Badges",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Milestones Unlocked",
                            fontSize = 8.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // INTERACTIVE DAILY STUDY TRACKER CALENDAR
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSystemInDarkTheme()) Color(0xFF1E1B4B).copy(alpha = 0.4f) else Color.White
                ),
                border = BorderStroke(
                    1.dp,
                    if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.12f) else BorderGray
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DAILY HABIT TRACKER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AcademicGoldText,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "High School Study Check-in",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = ScholasticNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tick each day you read academic literature. Achieve 5 days to secure your 'Steady Scholar' badge!",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Weekly days Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        days.forEach { day ->
                            val isLogged = dailyLog[day] ?: false
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.toggleDailyReadingLog(day) }
                                    .padding(4.dp)
                                    .testTag("log_click_$day")
                            ) {
                                Text(
                                    text = day,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isLogged) ScholasticNavy else Color.Gray,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(
                                            if (isLogged) SageGreen.copy(alpha = 0.2f) else Color.Transparent,
                                            CircleShape
                                        )
                                        .border(
                                            1.5.dp,
                                            if (isLogged) SageGreen else Color.Gray.copy(alpha = 0.4f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isLogged) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Checked",
                                            tint = if (isSystemInDarkTheme()) SageGreen else Color(0xFF0F766E),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(Color.Gray.copy(alpha = 0.3f), CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // SECTION SECTION: CHALLENGES LIST
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE SYLLABUS CHALLENGES (${challenges.size})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.5.sp
                )

                Text(
                    text = "Join to earn Study XP",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        items(challenges, key = { it.id }) { challenge ->
            ChallengeItemCard(
                challenge = challenge,
                onJoin = { viewModel.joinReadingChallenge(challenge.id) },
                onClaim = { viewModel.claimChallengeReward(challenge.id) }
            )
        }
    }
}

@Composable
fun ChallengeItemCard(
    challenge: ReadingChallenge,
    onJoin: () -> Unit,
    onClaim: () -> Unit
) {
    val progressRatio = (challenge.currentValue.toFloat() / challenge.targetValue.toFloat()).coerceIn(0f, 1f)
    val isCompleted = challenge.currentValue >= challenge.targetValue

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .testTag("challenge_card_${challenge.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSystemInDarkTheme()) {
                if (challenge.isJoined && !challenge.isClaimed) Color(0xFF2E1065).copy(alpha = 0.2f) else Color(0xFF1E1B4B).copy(alpha = 0.3f)
            } else {
                if (challenge.isJoined && !challenge.isClaimed) Color(0xFFFAF5FF) else Color.White
            }
        ),
        border = BorderStroke(
            1.dp,
            if (challenge.isJoined && !challenge.isClaimed) {
                AcademicGoldLight.copy(alpha = 0.4f)
            } else {
                if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.08f) else BorderGray
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Category tag + Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Chip Tag
                Box(
                    modifier = Modifier
                        .background(
                            when (challenge.category) {
                                "Literature" -> Color(0xFF2563EB).copy(alpha = 0.15f)
                                "Science" -> Color(0xFFD946EF).copy(alpha = 0.15f)
                                "Streak" -> Color(0xFFFF5722).copy(alpha = 0.15f)
                                else -> Color.Gray.copy(alpha = 0.15f)
                            },
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = challenge.category.uppercase(),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = when (challenge.category) {
                            "Literature" -> Color(0xFF2563EB)
                            "Science" -> Color(0xFFD946EF)
                            "Streak" -> Color(0xFFFF5722)
                            else -> Color.Gray
                        }
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (challenge.holdsGoldBadge) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = "Gold Badge Reward",
                            tint = AccentGold,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 2.dp)
                        )
                        Text(
                            text = "Milestone Badge",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGold,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.08f) else SoftGray,
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "+${challenge.xpReward} XP",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcademicGoldText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title and Description
            Text(
                text = challenge.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSystemInDarkTheme()) Color.White else ScholasticNavy
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = challenge.description,
                fontSize = 11.sp,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar or Join Button Block
            if (!challenge.isJoined) {
                Button(
                    onClick = onJoin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("join_btn_${challenge.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ScholasticNavy,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Join Challenge", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                // Is Joined - show progress or Claim Button
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (challenge.isClaimed) "Completed" else if (isCompleted) "Target Achieved!" else "In Progress",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (challenge.isClaimed) Color.Gray else if (isCompleted) SageGreen else AcademicGoldText
                        )

                        Text(
                            text = when (challenge.targetType) {
                                "STREAK" -> "${challenge.currentValue} / ${challenge.targetValue} Days Logged"
                                "XP" -> "${challenge.currentValue} / ${challenge.targetValue} XP Earned"
                                else -> "${challenge.currentValue}% Complete"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.8f) else ScholasticNavy
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (challenge.isClaimed) {
                        // Fully completed & claimed
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SageGreen.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .border(1.dp, SageGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Claimed",
                                tint = SageGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Milestone Verified & XP Claimed",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSystemInDarkTheme()) SageGreen else Color(0xFF0F766E)
                            )
                        }
                    } else if (isCompleted) {
                        // Complete but unclaimed! Glow Claim button
                        Button(
                            onClick = onClaim,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("claim_btn_${challenge.id}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentGold,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Claim Reward",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CLAIM +${challenge.xpReward} XP REWARD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    } else {
                        // In progress - progress bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(
                                    if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.1f) else BorderGray,
                                    CircleShape
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressRatio)
                                    .fillMaxHeight()
                                    .background(ScholarGoldBrush(), CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScholarGoldBrush(): Brush {
    return Brush.horizontalGradient(
        colors = listOf(AcademicGoldText, AcademicGoldLight)
    )
}
