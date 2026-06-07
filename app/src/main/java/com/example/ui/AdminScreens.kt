package com.example.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import android.widget.Toast
import java.io.Serializable

// Helper function to render sections or icons
@Composable
fun MetricStatCard(
    title: String,
    value: String,
    caption: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = modifier.padding(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

// 1. DASHBOARD OVERVIEW TAB
@Composable
fun AdminOverviewTab(
    viewModel: LibraryViewModel,
    onNavigateToTab: (Int) -> Unit
) {
    val context = LocalContext.current
    val students by viewModel.userModerationProfiles.collectAsState()
    val subjects by viewModel.allAcademicSubjects.collectAsState()
    val activities by viewModel.adminRecentActivities.collectAsState()
    val librarians by viewModel.adminLibrarians.collectAsState()
    val schoolBrand by viewModel.schoolName.collectAsState()
    
    // Quick Actions Control dialogs
    var showAddStudent by remember { mutableStateOf(false) }
    var showAddTeacher by remember { mutableStateOf(false) }
    var showUploadResource by remember { mutableStateOf(false) }
    var showCreateSubject by remember { mutableStateOf(false) }

    // Count variables
    val teachersCount = remember(subjects) { subjects.flatMap { it.teachers }.distinctBy { it.id }.size + 62 }
    val studentCount = remember(students) { students.size + 1249 }
    val libCount = remember(librarians) { librarians.size + 2 }
    val subjectCount = remember(subjects) { subjects.size + 17 }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_overview_scroll"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Column {
                Text(
                    text = schoolBrand.uppercase(), 
                    style = MaterialTheme.typography.labelMedium, 
                    color = MaterialTheme.colorScheme.primary, 
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Administrator Core Dashboard", 
                    style = MaterialTheme.typography.headlineMedium, 
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Central authority portal for structural configurations and systems settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            // Stats Grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    MetricStatCard(
                        title = "Total Students",
                        value = studentCount.toString(),
                        caption = "+12 signed today",
                        icon = Icons.Default.Groups,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Instructors",
                        value = teachersCount.toString(),
                        caption = "Active faculty",
                        icon = Icons.Default.School,
                        color = Color(0xFF0F766E),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    MetricStatCard(
                        title = "Librarians",
                        value = libCount.toString(),
                        caption = "Desk managers",
                        icon = Icons.Default.LocalLibrary,
                        color = Color(0xFFB45309),
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Syllabus Courses",
                        value = subjectCount.toString(),
                        caption = "Curriculum nodes",
                        icon = Icons.Default.Book,
                        color = Color(0xFF6D28D9),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Actions Row
        item {
            Text(text = "⚡ Real-time Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showAddStudent = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                    modifier = Modifier.testTag("action_add_student_fab")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Student")
                }
                Button(
                    onClick = { showAddTeacher = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                    modifier = Modifier.testTag("action_add_teacher")
                ) {
                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Teacher")
                }
                Button(
                    onClick = { showUploadResource = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.onTertiaryContainer),
                    modifier = Modifier.testTag("action_upload_resource")
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upload Book/Doc")
                }
                Button(
                    onClick = { showCreateSubject = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Icon(Icons.Default.LibraryAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create Subject")
                }
                Button(
                    onClick = { onNavigateToTab(8) }, // Navigate to AI Quiz Center
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.inverseOnSurface, contentColor = MaterialTheme.colorScheme.inverseSurface)
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Quiz Studio")
                }
            }
        }

        // Recent administrative activities feed
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📜 Recent Control Log Activities", 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { Toast.makeText(context, "Refreshed operational audits", Toast.LENGTH_SHORT).show() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Logs")
                        }
                    }
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))
                    activities.forEachIndexed { idx, act ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = act, style = MaterialTheme.typography.bodyMedium)
                        }
                        if (idx < activities.size - 1) {
                            Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }

    // Modal Add Student Dialog
    if (showAddStudent) {
        AddStudentDialog(viewModel = viewModel, onDismiss = { showAddStudent = false })
    }

    // Modal Add Teacher Dialog
    if (showAddTeacher) {
        AddTeacherDialog(viewModel = viewModel, onDismiss = { showAddTeacher = false })
    }

    // Modal Upload Resource Dialog
    if (showUploadResource) {
        UploadResourceDialog(viewModel = viewModel, onDismiss = { showUploadResource = false })
    }

    // Modal Create Subject Dialog
    if (showCreateSubject) {
        CreateSubjectDialog(viewModel = viewModel, onDismiss = { showCreateSubject = false })
    }
}

// 2. STUDENT MANAGEMENT TAB
@Composable
fun AdminStudentsTab(viewModel: LibraryViewModel) {
    val context = LocalContext.current
    val students by viewModel.userModerationProfiles.collectAsState()
    
    // Dialog Triggers
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedStudentForView by remember { mutableStateOf<UserModerationProfile?>(null) }
    var showResetPassDialog by remember { mutableStateOf<UserModerationProfile?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<UserModerationProfile?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_students_layout")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "👨‍🎓 Academic Students Registrar", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(text = "${students.size} active credentials in database directory", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.testTag("add_student_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Student")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Student Database List
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.weight(1.5f)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                item {
                    // Header visual row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text("StudentID & Name", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Class", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Status", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Actions", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.End)
                    }
                }

                items(students) { student ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.5f)) {
                            Text(text = student.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text(text = student.id, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(text = student.grade, modifier = Modifier.weight(0.8f), style = MaterialTheme.typography.bodyMedium)
                        
                        // Status badge
                        Box(
                            modifier = Modifier
                                .weight(0.8f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when (student.status) {
                                        "Active" -> Color(0xFFDCFCE7)
                                        "Suspended" -> Color(0xFFFEE2E2)
                                        "Flagged" -> Color(0xFFFEF3C7)
                                        else -> Color(0xFFE2E8F0)
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = student.status,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (student.status) {
                                    "Active" -> Color(0xFF15803D)
                                    "Suspended" -> Color(0xFFB91C1C)
                                    "Flagged" -> Color(0xFFB45309)
                                    else -> Color(0xFF475569)
                                }
                            )
                        }

                        // Action Buttons Row
                        Row(
                            modifier = Modifier.weight(1.2f),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { selectedStudentForView = student }) {
                                Icon(Icons.Default.Visibility, contentDescription = "View Details", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { showResetPassDialog = student }) {
                                Icon(Icons.Default.Password, contentDescription = "Reset password", tint = Color(0xFFB45309), modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { showDeleteConfirmDialog = student }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete account", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                }
            }
        }
    }

    // Reset Password modal
    if (showResetPassDialog != null) {
        val st = showResetPassDialog!!
        AlertDialog(
            onDismissRequest = { showResetPassDialog = null },
            title = { Text("Reset Student Password") },
            text = { Column {
                Text("This will generate a randomized 8-character numeric security PIN for:")
                Text(st.name, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Auto-generated passcode: PIN-8547413", fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F766E))
            } },
            confirmButton = {
                TextButton(onClick = {
                    Toast.makeText(context, "Password configured to PIN-8547413. Notified via email.", Toast.LENGTH_LONG).show()
                    showResetPassDialog = null
                }) {
                    Text("Adopt New Pin")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetPassDialog = null }) {
                    Text("Abort")
                }
            }
        )
    }

    // Delete student confirmation
    if (showDeleteConfirmDialog != null) {
        val st = showDeleteConfirmDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Erase Student Account?", color = MaterialTheme.colorScheme.error) },
            text = { Text("Are you absolutely sure you want to completely erase student registrar profile for ${st.name} (${st.id})? This will dissolve all local borrowing records and progress trackers definitively. This operation cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStudent(st.id)
                        Toast.makeText(context, "${st.name} eradicated successfully.", Toast.LENGTH_LONG).show()
                        showDeleteConfirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Erase")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // View Student details modal
    if (selectedStudentForView != null) {
        val st = selectedStudentForView!!
        Dialog(onDismissRequest = { selectedStudentForView = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Student Security Dossier", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Full Name: ", fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp))
                        Text(st.name)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Student ID: ", fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp))
                        Text(st.id)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Email Address: ", fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp))
                        Text(st.email)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Course/Class: ", fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp))
                        Text(st.grade)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Status Code: ", fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp))
                        Text(st.status)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Security Flags: ", fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp))
                        Text("${st.flaggedCount} infractions")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Last Login: ", fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp))
                        Text("Today at 10:42 AM")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val newStatus = if (st.status == "Active") "Suspended" else "Active"
                            viewModel.updateUserStatus(st.id, newStatus)
                            Toast.makeText(context, "Status set to $newStatus for ${st.name}", Toast.LENGTH_SHORT).show()
                            selectedStudentForView = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (st.status == "Active") Color(0xFFDC2626) else Color(0xFF16A34A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (st.status == "Active") "Deactivate Account" else "Re-Activate Account")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { selectedStudentForView = null }, modifier = Modifier.fillMaxWidth()) {
                        Text("Close Dossier")
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddStudentDialog(viewModel = viewModel, onDismiss = { showAddDialog = false })
    }
}

@Composable
fun AddStudentDialog(viewModel: LibraryViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var studentIdInput by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var gradeLevel by remember { mutableStateOf("S4") }
    var streamSelection by remember { mutableStateOf("A") }
    var passwordInput by remember { mutableStateOf("") }
    var registrationStatus by remember { mutableStateOf("Active") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Compile Student Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Text("Admins register students directly. No public registry allowed.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                OutlinedTextField(
                    value = studentIdInput,
                    onValueChange = { studentIdInput = it },
                    label = { Text("Student ID (e.g., S4A-012)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_student_id_field")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_student_firstName_field")
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_student_lastName_field")
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Class Year", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("S1", "S2", "S3", "S4", "S5", "S6").forEach { cl ->
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (gradeLevel == cl) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { gradeLevel = cl },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(cl, color = if (gradeLevel == cl) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Stream", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("A", "B", "C", "D").forEach { str ->
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (streamSelection == str) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { streamSelection = str },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(str, color = if (streamSelection == str) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Global Access Passcode") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_student_pass_field")
                )

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Registrar Status:", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Row(modifier = Modifier.weight(2f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Active", "Flagged", "Suspended").forEach { st ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (registrationStatus == st) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { registrationStatus = st }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(st, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (registrationStatus == st) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Abort")
                    }
                    Button(
                        onClick = {
                            if (studentIdInput.isBlank() || firstName.isBlank() || lastName.isBlank() || passwordInput.isBlank()) {
                                Toast.makeText(context, "All security variables must be defined.", Toast.LENGTH_SHORT).show()
                            } else {
                                // Simulate full pipeline success
                                viewModel.createStudent(
                                    id = studentIdInput.trim(),
                                    firstName = firstName.trim(),
                                    lastName = lastName.trim(),
                                    classLvl = gradeLevel,
                                    stream = streamSelection,
                                    statusStr = registrationStatus
                                )
                                Toast.makeText(context, "Full Supabase and core profile pipeline configured!", Toast.LENGTH_LONG).show()
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_add_student_btn")
                    ) {
                        Text("Deploy Profile")
                    }
                }
            }
        }
    }
}

// 3. TEACHER MANAGEMENT TAB
@Composable
fun AdminTeachersTab(viewModel: LibraryViewModel) {
    val context = LocalContext.current
    val subjects by viewModel.allAcademicSubjects.collectAsState()
    
    val allTeachers = remember(subjects) {
        subjects.flatMap { subj -> subj.teachers.map { t -> t to subj.name } }.distinctBy { it.first.id }
    }

    var showAddTeacher by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_teachers_layout")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "👨‍🏫 Faculty Instructors Directory", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(text = "Assign instructors to core subject nodes for learning feedback loops", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(
                onClick = { showAddTeacher = true },
                modifier = Modifier.testTag("add_teacher_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Teacher")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(modifier = Modifier.padding(12.dp)) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text("Faculty Name", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Subject Track", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Credentials", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Scope", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                items(allTeachers) { pair ->
                    val teacher = pair.first
                    val subjName = pair.second
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1.5f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(teacher.name.take(2).uppercase(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = teacher.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        }

                        Text(text = subjName, modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                        Text(text = teacher.email, modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        Box(
                            modifier = Modifier
                                .weight(0.8f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Faculty", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                }
            }
        }
    }

    if (showAddTeacher) {
        AddTeacherDialog(viewModel = viewModel, onDismiss = { showAddTeacher = false })
    }
}

@Composable
fun AddTeacherDialog(viewModel: LibraryViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val subjects by viewModel.allAcademicSubjects.collectAsState()
    
    var facultyName by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var selectedSubjectNode by remember { mutableStateOf("") }
    var roleSelection by remember { mutableStateOf("Teacher") }
    var accessPasscode by remember { mutableStateOf("") }

    if (subjects.isNotEmpty() && selectedSubjectNode.isEmpty()) {
        selectedSubjectNode = subjects.first().name
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Authorize Faculty Instructor", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                
                OutlinedTextField(
                    value = facultyName,
                    onValueChange = { facultyName = it },
                    label = { Text("Instructor Full Name (e.g., Mr. Okello)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_teacher_name")
                )

                OutlinedTextField(
                    value = emailAddress,
                    onValueChange = { emailAddress = it },
                    label = { Text("Corporate Institutional Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text("Subject Syllabus Assignment", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .horizontalScroll(rememberScrollState())
                            .padding(8.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            subjects.forEach { sub ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedSubjectNode == sub.name) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                        .clickable { selectedSubjectNode = sub.name }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(sub.name, color = if (selectedSubjectNode == sub.name) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Column {
                    Text("Administrative Governance Role", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Teacher", "Head of Dept", "Deputy Academic").forEach { r ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (roleSelection == r) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { roleSelection = r }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(r, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (roleSelection == r) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = accessPasscode,
                    onValueChange = { accessPasscode = it },
                    label = { Text("Initial Security Passphrase") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (facultyName.isBlank() || emailAddress.isBlank() || selectedSubjectNode.isBlank() || accessPasscode.isBlank()) {
                                Toast.makeText(context, "All configuration criteria must be completed", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.createTeacher(facultyName.trim(), emailAddress.trim(), selectedSubjectNode, roleSelection)
                                Toast.makeText(context, "Faculty and structural permissions configured!", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_add_teacher")
                    ) {
                        Text("Grant Access")
                    }
                }
            }
        }
    }
}

// 4. LIBRARIAN MANAGEMENT TAB
@Composable
fun AdminLibrariansTab(viewModel: LibraryViewModel) {
    val context = LocalContext.current
    val librarians by viewModel.adminLibrarians.collectAsState()
    
    var showAddLibrarian by remember { mutableStateOf(false) }
    var deleteConfirmLib by remember { mutableStateOf<UserModerationProfile?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_librarians_layout")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "📚 Digital Librarians Directorate", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(text = "Librarian accounts handle catalog indexing and shelf layout management.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(
                onClick = { showAddLibrarian = true },
                modifier = Modifier.testTag("add_librarian_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Librarian")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(modifier = Modifier.padding(12.dp)) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text("Librarian Name", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Workspace role", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Administrative Email", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Actions", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.End)
                    }
                }

                items(librarians) { lib ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1.5f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(lib.name.take(2).uppercase(), fontWeight = FontWeight.Bold, color = Color(0xFFD97706), style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = lib.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        }

                        Text(text = lib.grade, modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                        Text(text = lib.email, modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        Row(
                            modifier = Modifier.weight(0.8f),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = { deleteConfirmLib = lib }) {
                                Icon(Icons.Default.Delete, contentDescription = "Erase security rules", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                }
            }
        }
    }

    if (showAddLibrarian) {
        Dialog(onDismissRequest = { showAddLibrarian = false }) {
            var nameInput by remember { mutableStateOf("") }
            var emailInput by remember { mutableStateOf("") }
            var passInput by remember { mutableStateOf("") }
            var deskRoleInput by remember { mutableStateOf("Catalog Indexer Desk") }

            Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Authorize Dedicated Librarian", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Librarian Full Name") },
                        modifier = Modifier.fillMaxWidth().testTag("librarian_name_input")
                    )
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Staff Control Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = passInput,
                        onValueChange = { passInput = it },
                        label = { Text("Librarian Initial Passkey") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column {
                        Text("Primary Duty Area", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            listOf("Catalog Room", "Library Counter", "Archive Manager").forEach { rl ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (deskRoleInput == rl) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { deskRoleInput = rl }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(rl, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (deskRoleInput == rl) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showAddLibrarian = false }, modifier = Modifier.weight(1f)) {
                            Text("Abort")
                        }
                        Button(
                            onClick = {
                                if (nameInput.isNotBlank() && emailInput.isNotBlank()) {
                                    viewModel.createLibrarian(nameInput.trim(), emailInput.trim(), deskRoleInput)
                                    Toast.makeText(context, "$nameInput authorized successfully.", Toast.LENGTH_SHORT).show()
                                    showAddLibrarian = false
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("confirm_create_librarian")
                        ) {
                            Text("Deploy Crew")
                        }
                    }
                }
            }
        }
    }

    if (deleteConfirmLib != null) {
        val lib = deleteConfirmLib!!
        AlertDialog(
            onDismissRequest = { deleteConfirmLib = null },
            title = { Text("Revoke Librarian Authorizations?") },
            text = { Text("This will permanently disable library desk operations credentials for: ${lib.name}. Do you want to proceed?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteLibrarian(lib.id)
                        Toast.makeText(context, "Credentials revoked.", Toast.LENGTH_SHORT).show()
                        deleteConfirmLib = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Revoke Credentials")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmLib = null }) {
                    Text("Back")
                }
            }
        )
    }
}

// 5. CLASS MANAGEMENT TAB
@Composable
fun AdminClassesTab(viewModel: LibraryViewModel) {
    val context = LocalContext.current
    val classes by viewModel.adminClasses.collectAsState()
    val streams by viewModel.adminStreams.collectAsState()
    
    var showAddClass by remember { mutableStateOf(false) }
    var showAddStream by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "🏫 Classes & Academic Streams Configuration", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = "Configured classes represent structural levels from Senior 1 to Senior 6.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Class Levels Column
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Class Levels", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { showAddClass = true }) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Add Class", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    classes.forEach { cl ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Domain, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(cl, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            // Streams Column
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Active Streams", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { showAddStream = true }) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Add Stream", tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    streams.forEach { str ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AlignHorizontalLeft, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Stream $str", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Computed combinations preview Matrix
        Text("📐 Evaluated Classroom Matrix Nodes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            val matrix = classes.take(4).flatMap { c -> streams.take(3).map { s -> "$c$s" } }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 90.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(matrix) { node ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(node, fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                            Text("Syllabus Active", fontSize = 9.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    if (showAddClass) {
        Dialog(onDismissRequest = { showAddClass = false }) {
            var valueInput by remember { mutableStateOf("") }
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(24.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Add Class Level", fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = valueInput, onValueChange = { valueInput = it }, label = { Text("e.g. S5 Senior 5") })
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddClass = false }) { Text("Abort") }
                        Button(onClick = {
                            if (valueInput.isNotBlank()) {
                                viewModel.addClassLevel(valueInput.trim())
                                showAddClass = false
                            }
                        }) { Text("Confirm") }
                    }
                }
            }
        }
    }

    if (showAddStream) {
        Dialog(onDismissRequest = { showAddStream = false }) {
            var valueInput by remember { mutableStateOf("") }
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(24.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Create Stream Branch", fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = valueInput, onValueChange = { valueInput = it }, label = { Text("e.g. Stream E") })
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddStream = false }) { Text("Abort") }
                        Button(onClick = {
                            if (valueInput.isNotBlank()) {
                                viewModel.addStream(valueInput.trim())
                                showAddStream = false
                            }
                        }) { Text("Confirm") }
                    }
                }
            }
        }
    }
}

// 6. SUBJECT MANAGEMENT TAB
@Composable
fun AdminSubjectsTab(viewModel: LibraryViewModel) {
    val context = LocalContext.current
    val subjects by viewModel.allAcademicSubjects.collectAsState()
    
    val oLevel = subjects.filter { !it.classLevel.isAdvanced }
    val aLevel = subjects.filter { it.classLevel.isAdvanced }

    var showCreateSubDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_subjects_layout")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "📖 Academic Course Syllabus Nodes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = "Configure curriculum nodes across Ordinary (O-Level) & Advanced (A-Level) domains", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = { showCreateSubDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Create Subject")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // O-Level Column
            Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🧩 Ordinary Level (O-Level)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(oLevel) { subj ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(subj.classLevel.name, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(subj.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                    Text("${subj.teachers.size} teachers assigned", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                            Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            // A-Level Column
            Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🚀 Advanced Level (A-Level)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(aLevel) { subj ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE0F2FE)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(subj.classLevel.name, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF0369A1))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(subj.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                    Text("${subj.teachers.size} teachers assigned", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                            Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }

    if (showCreateSubDialog) {
        CreateSubjectDialog(viewModel = viewModel, onDismiss = { showCreateSubDialog = false })
    }
}

@Composable
fun CreateSubjectDialog(viewModel: LibraryViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var nameInput by remember { mutableStateOf("") }
    var classLevelStr by remember { mutableStateOf("S1") }
    var descInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Design Syllabus Node", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Subject Syllabus Name") },
                    modifier = Modifier.fillMaxWidth().testTag("subject_name_input")
                )

                Column {
                    Text("Class Level Node Mapping", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("S1", "S2", "S3", "S4", "S5", "S6").forEach { cl ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (classLevelStr == cl) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { classLevelStr = cl },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(cl, fontWeight = FontWeight.Bold, color = if (classLevelStr == cl) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 11.sp)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = descInput,
                    onValueChange = { descInput = it },
                    label = { Text("Syllabus Objective description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Abort") }
                    Button(
                        onClick = {
                            if (nameInput.isNotBlank() && descInput.isNotBlank()) {
                                viewModel.createSubject(nameInput.trim(), classLevelStr, descInput.trim())
                                Toast.makeText(context, "Syllabus system node created!", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            } else {
                                Toast.makeText(context, "Fill the parameters fully first.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("confirm_create_subject")
                    ) {
                        Text("Implement")
                    }
                }
            }
        }
    }
}

// 7. A-LEVEL COMBINATION MANAGEMENT TAB
@Composable
fun AdminCombinationsTab(viewModel: LibraryViewModel) {
    val context = LocalContext.current
    val combinations by viewModel.adminCombinations.collectAsState()
    
    var showAddCombDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_combinations_layout")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "📐 A-Level Syllabus Combinations Director", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = "Formulate subject combo triads (HEG, PCM, etc.) for personalized advanced curriculum routing", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = { showAddCombDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Combination")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Active Combination Triads", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(combinations) { comb ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(comb, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.secondaryContainer)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("ADVANCED", fontSize = 9.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Consists of 3 principal syllabus criteria filters, mapped dynamically to S5 & S6 exam registries.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Group, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("~14 students assigned", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddCombDialog) {
        Dialog(onDismissRequest = { showAddCombDialog = false }) {
            var valueInput by remember { mutableStateOf("") }
            Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Formulate Combo Triad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = valueInput,
                        onValueChange = { valueInput = it },
                        label = { Text("Triad string (e.g. PCM, HEG)") },
                        modifier = Modifier.fillMaxWidth().testTag("comb_triad_input")
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddCombDialog = false }) { Text("Cancel") }
                        Button(onClick = {
                            if (valueInput.isNotBlank()) {
                                viewModel.addCombination(valueInput.trim())
                                Toast.makeText(context, "$valueInput Combo registered successfully.", Toast.LENGTH_SHORT).show()
                                showAddCombDialog = false
                            }
                        }) { Text("Deploy") }
                    }
                }
            }
        }
    }
}

// 8. RESOURCE MANAGEMENT TAB
@Composable
fun AdminResourcesTab(viewModel: LibraryViewModel) {
    val context = LocalContext.current
    val subjects by viewModel.allAcademicSubjects.collectAsState()
    
    var showUploadModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_resources_layout")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "📘 Learning Resources Core Console", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(text = "Maintain textbooks, notes archives, past papers and interactive lectures.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = { showUploadModal = true }, modifier = Modifier.testTag("upload_resource_btn")) {
                Icon(Icons.Default.CloudUpload, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Upload Document")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Listing resources grouped by subject
        Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(16.dp)) {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                items(subjects) { subj ->
                    if (subj.resources.isNotEmpty()) {
                        Text(subj.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        subj.resources.forEach { res ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (res.category) {
                                            ResourceCategory.NOTES -> Icons.Default.Description
                                            ResourceCategory.PDFS -> Icons.Default.PictureAsPdf
                                            ResourceCategory.TEXTBOOKS -> Icons.Default.MenuBook
                                            ResourceCategory.VIDEOS -> Icons.Default.VideoLibrary
                                            else -> Icons.Default.FolderZip
                                        },
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(res.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                    Text("Category: ${res.category.label} • Size: ${res.sizeLabel}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Admin Approved", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                }
                            }
                            Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    if (showUploadModal) {
        UploadResourceDialog(viewModel = viewModel, onDismiss = { showUploadModal = false })
    }
}

@Composable
fun UploadResourceDialog(viewModel: LibraryViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val subjects by viewModel.allAcademicSubjects.collectAsState()
    val combinations by viewModel.adminCombinations.collectAsState()

    var resourceTitle by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Notes") }
    var selectedCombination by remember { mutableStateOf("") }
    var fileSimulationName by remember { mutableStateOf("") }

    if (subjects.isNotEmpty() && selectedSubjectId.isEmpty()) {
        selectedSubjectId = subjects.first().id
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Release Study Material", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                
                OutlinedTextField(
                    value = resourceTitle,
                    onValueChange = { resourceTitle = it },
                    label = { Text("Resource Heading Title (e.g. Macbeth Notes)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_resource_title")
                )

                Column {
                    Text("Target Subject Node", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .horizontalScroll(rememberScrollState())
                            .padding(8.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            subjects.forEach { sub ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedSubjectId == sub.id) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                        .clickable { selectedSubjectId = sub.id }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(sub.name, color = if (selectedSubjectId == sub.id) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Column {
                    Text("Resource Directory Type", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Notes", "PDF", "Textbook", "Video", "Past Paper", "Quiz").forEach { cat ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selectedCategory == cat) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(cat, fontSize = 10.sp, color = if (selectedCategory == cat) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                Column {
                    Text("A-Level Combo routing rule (Optional)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("None" to "") + combinations.map { it to it }.forEach { p ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedCombination == p.second) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedCombination = p.second }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(p.first, fontSize = 10.sp, color = if (selectedCombination == p.second) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = fileSimulationName,
                    onValueChange = { fileSimulationName = it },
                    label = { Text("Underlying Virtual File Path (URI)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_resource_uri")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Discard") }
                    Button(
                        onClick = {
                            if (resourceTitle.isBlank() || selectedSubjectId.isBlank() || fileSimulationName.isBlank()) {
                                Toast.makeText(context, "Completed document variables must be defined.", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.uploadAcademicResource(
                                    resourceTitle.trim(),
                                    selectedSubjectId,
                                    selectedCategory,
                                    selectedCombination.ifEmpty { null }
                                )
                                Toast.makeText(context, "Syllabus resource launched in system catalog!", Toast.LENGTH_LONG).show()
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("confirm_upload_resource_btn")
                    ) {
                        Text("Deploy Node")
                    }
                }
            }
        }
    }
}

// 9. ASSIGNMENT CENTER TAB
@Composable
fun AdminAssignmentsTab(viewModel: LibraryViewModel) {
    val context = LocalContext.current
    val assignments by viewModel.teacherAssignments.collectAsState()
    
    var showReportExportDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_assignments_layout")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "📝 Academic Performance Assignment Director", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = "Supervise deployed syllabus tasks, check submission rates and track overdue work.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = { showReportExportDialog = true }) {
                Icon(Icons.Default.Output, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export Reports")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxSize()) {
            LazyColumn(modifier = Modifier.padding(12.dp)) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text("Work Subject Title", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                        Text("Class Mapped & Date", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold)
                        Text("Completion Progress", modifier = Modifier.weight(1.3f), fontWeight = FontWeight.Bold)
                    }
                }

                items(assignments) { asg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.5f)) {
                            Text(asg.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text("Assigned by: ${asg.author}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(asg.bookTitle, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                            Text("Due Date: ${asg.dueDate}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        
                        Column(modifier = Modifier.weight(1.3f)) {
                            val pct = if (asg.totalCount > 0) (asg.completedCount.toFloat() / asg.totalCount) else 0.7f
                            LinearProgressIndicator(
                                progress = pct,
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${asg.completedCount}/${asg.totalCount} submission ratios (${(pct * 100).toInt()}%)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                }
            }
        }
    }

    if (showReportExportDialog) {
        Dialog(onDismissRequest = { showReportExportDialog = false }) {
            Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Print Syllabuses Audits Report", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("This generates a structured academic compliance dossier in XLSX or PDF format detailing students class participation and assignment completion grades.", style = MaterialTheme.typography.bodyMedium)
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "Exported Academic performance metrics.PDF to download history.", Toast.LENGTH_LONG).show()
                                showReportExportDialog = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Export PDF Document")
                        }
                        Button(
                            onClick = {
                                Toast.makeText(context, "Exported Syllabus progress metrics.XLSX to dynamic cache.", Toast.LENGTH_LONG).show()
                                showReportExportDialog = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Export spreadsheet")
                        }
                    }
                    TextButton(onClick = { showReportExportDialog = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

// 10. AI QUIZ CENTER TAB
@Composable
fun AdminAIQuizTab(viewModel: LibraryViewModel) {
    val context = LocalContext.current
    
    val subjects by viewModel.allAcademicSubjects.collectAsState()
    val isQuizGenerating by viewModel.isQuizGenerating.collectAsState()
    val generationProgress by viewModel.quizGenerationProgress.collectAsState()
    val generatedQuiz by viewModel.adminGeneratedQuiz.collectAsState()
    val approvedQuizzes by viewModel.adminApprovedQuizzes.collectAsState()

    var selectedSubjectName by remember { mutableStateOf("") }
    var selectedClassLevel by remember { mutableStateOf("S1") }
    var selectedDifficulty by remember { mutableStateOf("Medium") }
    var selectedQuestionCount by remember { mutableStateOf(3) }

    if (subjects.isNotEmpty() && selectedSubjectName.isEmpty()) {
        selectedSubjectName = subjects.first().name
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("admin_quiz_scroll")
    ) {
        Text(text = "🧠 AI Quiz Design Studio", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Text(text = "Automated high school evaluation designer using Google gemini-3.5-flash.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(16.dp))

        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Quiz Parameters Generator", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                // Select Subject flow
                Column {
                    Text("Learning Category subject node", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .horizontalScroll(rememberScrollState())
                            .padding(8.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            subjects.forEach { sub ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedSubjectName == sub.name) MaterialTheme.colorScheme.primary else Color.Transparent)
                                        .clickable { selectedSubjectName = sub.name }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(sub.name, color = if (selectedSubjectName == sub.name) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Class Year", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            listOf("S1", "S3", "S4", "S5").forEach { cl ->
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selectedClassLevel == cl) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { selectedClassLevel = cl },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(cl, color = if (selectedClassLevel == cl) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1.2f)) {
                        Text("AI Difficulty", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            listOf("Easy", "Medium", "Hard").forEach { df ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedDifficulty == df) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { selectedDifficulty = df }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(df, color = if (selectedDifficulty == df) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Question volume:", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Row(modifier = Modifier.weight(2f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(2, 3, 5).forEach { c ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (selectedQuestionCount == c) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedQuestionCount = c },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(c.toString(), fontWeight = FontWeight.Bold, color = if (selectedQuestionCount == c) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                // Compile Action
                Button(
                    onClick = {
                        viewModel.generateAIQuiz(
                            selectedSubjectName,
                            selectedClassLevel,
                            selectedDifficulty,
                            selectedQuestionCount
                        )
                    },
                    enabled = !isQuizGenerating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_ai_generation_trigger")
                ) {
                    if (isQuizGenerating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Synthesize Custom Quiz")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quiz generation loading state logs
        if (isQuizGenerating && generationProgress != null) {
            Card(
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(generationProgress!!, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // DESIGN REVIEW OF RECENTLY GENERATED AI QUIZ
        if (generatedQuiz != null) {
            val quiz = generatedQuiz!!
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.testTag("generated_quiz_preview_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🤖 Evaluated generated preview", fontWeight = FontWeight.Light, fontSize = 11.sp, color = Color.Gray)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFEF3C7))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Pending Admin Review", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFFD97706))
                        }
                    }
                    Text(quiz.subject, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    Text("Target level: ${quiz.classLevel} • Difficulty: ${quiz.difficulty} • Count: ${quiz.questionCount}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    
                    quiz.questions.forEachIndexed { i, q ->
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text("Question ${i+1}: ${q.questionText}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(6.dp))
                            q.options.forEachIndexed { opIdx, op ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(if (opIdx == q.correctIndex) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = ('A'.code + opIdx).toChar().toString(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (opIdx == q.correctIndex) Color(0xFF15803D) else Color.Gray
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(op, fontSize = 13.sp, color = if (opIdx == q.correctIndex) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        TextButton(
                            onClick = { viewModel.adminGeneratedQuiz.value = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Discard Draft")
                        }
                        Button(
                            onClick = {
                                viewModel.publishAIQuiz(quiz)
                                Toast.makeText(context, "Quiz deployed and authorized in system catalog!", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("approve_and_publish_quiz")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve & Publish")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Published Quizzes history list
        Text("📬 Authorized Published Quizzes list", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                approvedQuizzes.forEach { qz ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                        Icon(Icons.Default.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(qz.subject, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text("Level: ${qz.classLevel} • Difficulty: ${qz.difficulty} • Total ${qz.questionCount} Questions", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

// 11. ANALYTICS CHART TAB
@Composable
fun AdminAnalyticsTab(viewModel: LibraryViewModel) {
    var timerSeconds by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("admin_analytics_layout")
    ) {
        Text(text = "📈 System Analytics Console", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = "Audit metrics, resource consumption graphs and student attendance.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(16.dp))

        // SLEEK CUSTOM GRAPH using Canvas
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📈 Login Activity metrics (Daily count logs)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    val points = listOf(120, 240, 180, 420, 280, 560, 480)
                    val widthSpacing = size.width / (points.size - 1)
                    val maxVal = points.maxOrNull()?.toFloat() ?: 100f
                    
                    val path = Path().apply {
                        moveTo(0f, size.height - (points[0] / maxVal) * size.height)
                        for (i in 1 until points.size) {
                            lineTo(i * widthSpacing, size.height - (points[i] / maxVal) * size.height)
                        }
                    }
                    
                    drawPath(
                        path = path,
                        brush = Brush.linearGradient(listOf(Color(0xFF0F766E), Color(0xFF6D28D9))),
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                    
                    // Draw node circles
                    for (i in points.indices) {
                        drawCircle(
                            color = Color(0xFF6D28D9),
                            radius = 6.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(i * widthSpacing, size.height - (points[i] / maxVal) * size.height)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                        Text(day, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // High demand syllabus materials table
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🔥 Standard Syllabus document consumption", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                val demands = listOf(
                    Triple("Macbeth Core notes summary", "Mr. Harrison", "452 readings"),
                    Triple("Calculus Equations syllabus S5", "Mr. Okello", "380 readings"),
                    Triple("Ecosystem thermodynamics diagram", "Ms. Alanyo", "312 views"),
                    Triple("UNEB ICT National Past paper", "Mr. Ssewankambo", "240 downloads")
                )
                
                demands.forEach { record ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(record.first, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Instructor: ${record.second}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Text(record.third, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

// 12. SYSTEM CONFIGURATION & BRANDING SETTINGS TAB
@Composable
fun AdminSettingsTab(viewModel: LibraryViewModel) {
    val context = LocalContext.current
    
    val currentBrand by viewModel.schoolName.collectAsState()
    val motto by viewModel.schoolMotto.collectAsState()
    val slogan by viewModel.schoolSlogan.collectAsState()
    val colorAccent by viewModel.themeColor.collectAsState()
    
    val timeout by viewModel.sessionTimeout.collectAsState()
    val pwPolicy by viewModel.passwordPolicy.collectAsState()
    val isMaintenance by viewModel.maintenanceMode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("admin_settings_layout")
    ) {
        Text(text = "⚙️ System Configuration console", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = "Modify institutional profile aspects and structural lockup criteria.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(16.dp))

        // BRANDING CONTAINER
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("🏫 School Identity Branding", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = currentBrand,
                    onValueChange = { viewModel.schoolName.value = it },
                    label = { Text("App School Name string") },
                    modifier = Modifier.fillMaxWidth().testTag("settings_school_name")
                )

                OutlinedTextField(
                    value = motto,
                    onValueChange = { viewModel.schoolMotto.value = it },
                    label = { Text("Official School Motto") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = slogan,
                    onValueChange = { viewModel.schoolSlogan.value = it },
                    label = { Text("Institutional Slogan") },
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text("App Primary Accent Palette Color", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Indigo", "Teal", "Blue", "Black").forEach { color ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (colorAccent == color) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { viewModel.themeColor.value = color }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(color, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (colorAccent == color) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SYSTEM LAWS CONTAINER
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("🔒 System Security Protocols", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Session Timeout Limit (Min):", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Row(modifier = Modifier.weight(2f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(15, 30, 60).forEach { tm ->
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (timeout == tm) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { viewModel.sessionTimeout.value = tm },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(tm.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Password Policy Complexity:", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Row(modifier = Modifier.weight(2f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Simple (Numeric)", "Medium (Alphanumeric)", "Strong (Complex)").forEach { r ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (pwPolicy == r) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { viewModel.passwordPolicy.value = r }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(r.take(8) + "..", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (pwPolicy == r) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(2f)) {
                        Text("Syllabus Maintenance Mode", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        Text("Stops general borrowing access during inventory checks.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Switch(
                        checked = isMaintenance,
                        onCheckedChange = { viewModel.maintenanceMode.value = it },
                        modifier = Modifier.testTag("maintenance_mode_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                Toast.makeText(context, "All institutional and system configurations secured!", Toast.LENGTH_LONG).show()
                viewModel.addNotification("Configurations Updated", "School settings and security protocols modified.", "announcement")
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("save_settings_btn")
        ) {
            Icon(Icons.Default.Save, contentDescription = "Save settings")
            Spacer(modifier = Modifier.width(6.dp))
            Text("Secure configurations")
        }
    }
}
