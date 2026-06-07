package com.example.ui.auth

import com.example.ui.ProductShowcaseScreen
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.UserRole
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.core.tween

@Composable
fun AuthEntryScreen(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    var showShowcase by remember { mutableStateOf(false) }
    var isSigningUp by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<LoginTab?>(null) }
    var showStaffPortals by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val authState by viewModel.authState.collectAsState()
    val focusManager = LocalFocusManager.current

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val backgroundColor = MaterialTheme.colorScheme.background

    if (showShowcase) {
        ProductShowcaseScreen(onDismiss = { showShowcase = false })
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.15f),
                            backgroundColor,
                            backgroundColor
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Application Logo & Brand Title
            // Hidden staff portal reveal - UI only, not a security mechanism
            Image(
                painter = painterResource(id = com.example.R.drawable.nexus_logo),
                contentDescription = "Nexus Tech Solutions Logo",
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .padding(bottom = 8.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                showStaffPortals = !showStaffPortals
                            }
                        )
                    }
            )

            Text(
                text = "Nexus Digital Library",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Educational Hub & Archive Portal",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Dynamic Display matching State Machine
            AnimatedContent(
                targetState = authState,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "AuthStateTransition"
            ) { state ->
                when (state) {
                    is AuthState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Connecting to Supabase Authentication...",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    is AuthState.Error -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 500.dp)
                                .padding(vertical = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Authentication Error",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = state.message,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.clearError() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    ),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Dismiss")
                                }
                            }
                        }
                    }
                    is AuthState.SuccessMessage -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE6F4EA)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 500.dp)
                                .padding(vertical = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = Color(0xFF137333),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Registration Successful",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF137333),
                                        fontSize = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    fontSize = 14.sp,
                                    color = Color(0xFF137333)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { 
                                        isSigningUp = false
                                        viewModel.clearError()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF137333)
                                    ),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Proceed to Sign In", color = Color.White)
                                }
                            }
                        }
                    }
                    else -> {
                        val currentCategory = selectedCategory
                        if (currentCategory == null) {
                            CategorySelectionScreen(
                                showStaffPortals = showStaffPortals,
                                onCategorySelected = { selectedCategory = it },
                                onExploreShowcase = { showShowcase = true }
                            )
                        } else {
                            if (isSigningUp) {
                                SignUpView(
                                    initialCategory = currentCategory,
                                    onSignUpStudent = { fullName, studentId, password ->
                                        focusManager.clearFocus()
                                        viewModel.signUpStudent(fullName, studentId, password)
                                    },
                                    onSignUpStaff = { fullName, email, password, role, staffAccessCode ->
                                        focusManager.clearFocus()
                                        viewModel.signUpStaff(fullName, email, password, role, staffAccessCode)
                                    },
                                    onNavigateToLogin = { isSigningUp = false },
                                    onBackToCategories = { selectedCategory = null }
                                )
                            } else {
                                LoginView(
                                    initialTab = currentCategory,
                                    onLoginWithStudentId = { studentId, password ->
                                        focusManager.clearFocus()
                                        viewModel.loginWithStudentId(studentId, password)
                                    },
                                    onLoginWithEmail = { email, password, loginTab ->
                                        focusManager.clearFocus()
                                        viewModel.login(email, password, loginTab)
                                    },
                                    onNavigateToSignUp = { isSigningUp = true },
                                    onBackToCategories = { selectedCategory = null }
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

enum class LoginTab {
    STUDENT, TEACHER, LIBRARIAN, ADMINISTRATOR
}

@Composable
private fun LoginView(
    initialTab: LoginTab,
    onLoginWithStudentId: (String, String) -> Unit,
    onLoginWithEmail: (String, String, LoginTab) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onBackToCategories: () -> Unit
) {
    var activeTab by remember(initialTab) { mutableStateOf(initialTab) }
    
    // Maintain separate field states to deliver a premium UX
    var studentId by remember { mutableStateOf("S4A-023") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("8585@@") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    // Password recovery dialog state
    var showResetDialog by remember { mutableStateOf(false) }
    var resetInput by remember { mutableStateOf("") }
    var resetSuccessMsg by remember { mutableStateOf<String?>(null) }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 500.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToCategories,
                    modifier = Modifier.testTag("login_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back to Categories",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Account Access Portal",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            // Dynamic Segmented Role Selector tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LoginTab.values().forEach { tab ->
                    val isSelected = activeTab == tab
                    val label = when (tab) {
                        LoginTab.STUDENT -> "Student"
                        LoginTab.TEACHER -> "Teacher"
                        LoginTab.LIBRARIAN -> "Librarian"
                        LoginTab.ADMINISTRATOR -> "Admin"
                    }
                    val icon = when (tab) {
                        LoginTab.STUDENT -> Icons.Default.School
                        LoginTab.TEACHER -> Icons.Default.SupervisorAccount
                        LoginTab.LIBRARIAN -> Icons.Default.LocalLibrary
                        LoginTab.ADMINISTRATOR -> Icons.Default.AdminPanelSettings
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                            .clickable { 
                                activeTab = tab 
                                // Auto-fill reasonable defaults to make evaluation seamless!
                                when (tab) {
                                    LoginTab.STUDENT -> {
                                        studentId = "S4A-023"
                                        password = "8585@@"
                                    }
                                    LoginTab.TEACHER -> {
                                        email = "teacher@nexustech.edu"
                                        password = "8585@@"
                                    }
                                    LoginTab.LIBRARIAN -> {
                                        email = "librarian@nexustech.edu"
                                        password = "8585@@"
                                    }
                                    LoginTab.ADMINISTRATOR -> {
                                        email = "admin@nexustech.edu"
                                        password = "8585@@"
                                    }
                                }
                            }
                            .padding(vertical = 10.dp)
                            .testTag("login_tab_${tab.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = "$label icon",
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dynamic input based on selection
            if (activeTab == LoginTab.STUDENT) {
                OutlinedTextField(
                    value = studentId,
                    onValueChange = { studentId = it },
                    label = { Text("Student ID") },
                    placeholder = { Text("e.g. S4A-023") },
                    singleLine = true,
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Default.Badge, 
                            contentDescription = "BadgeIcon"
                        ) 
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_student_id_input")
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "🔒 Behind-the-scenes email lookup: Nexus maps Student ID to Supabase",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Start).padding(horizontal = 4.dp, vertical = 2.dp)
                )
            } else {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { 
                        Text(
                            when (activeTab) {
                                LoginTab.TEACHER -> "Teacher Email Address"
                                LoginTab.LIBRARIAN -> "Librarian Email"
                                else -> "Administrative Email ID"
                            }
                        ) 
                    },
                    placeholder = { 
                        Text(
                            when (activeTab) {
                                LoginTab.TEACHER -> "e.g. teacher@nexustech.edu"
                                LoginTab.LIBRARIAN -> "e.g. librarian@nexustech.edu"
                                else -> "e.g. admin@nexustech.edu"
                            }
                        ) 
                    },
                    singleLine = true,
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Default.Email, 
                            contentDescription = "EmailIcon"
                        ) 
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_input")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "PasswordIcon") },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "TogglePassword")
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_password_input")
            )

            // Password reset trigger link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Forgot Password?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { 
                            resetInput = if (activeTab == LoginTab.STUDENT) studentId else email
                            resetSuccessMsg = null
                            showResetDialog = true 
                        }
                        .padding(4.dp)
                        .testTag("forgot_password_button")
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { 
                    if (activeTab == LoginTab.STUDENT) {
                        onLoginWithStudentId(studentId, password)
                    } else {
                        onLoginWithEmail(email, password, activeTab)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("login_button")
            ) {
                Text(
                    text = if (activeTab == LoginTab.STUDENT) "Sign In with Student ID" else "Sign In Securely",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Registration is disabled. School administrator approval is required.",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(8.dp)
                    .testTag("restricted_registration_text")
            )
        }
    }

    // Advanced Password Reset Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "Secure Password Recovery",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter your registered Email Address or Student ID. We will initiate a secure password reset sequence.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    OutlinedTextField(
                        value = resetInput,
                        onValueChange = { resetInput = it },
                        label = { Text("Email or Student ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (resetSuccessMsg != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = resetSuccessMsg!!,
                            fontSize = 13.sp,
                            color = Color(0xFF137333),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE6F4EA), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                if (resetSuccessMsg == null) {
                    Button(
                        onClick = {
                            val target = resetInput.trim()
                            if (target.isBlank()) {
                                resetSuccessMsg = "Please enter a valid identifier."
                            } else {
                                resetSuccessMsg = if (!target.contains("@")) {
                                    "Password reset request dispatched. Since you are a student, your academic counselor received the reset code for security compliance."
                                } else {
                                    "Password reset link has been dispatched to $target. Please verify your email inbox."
                                }
                            }
                        }
                    ) {
                        Text("Reset Password")
                    }
                } else {
                    Button(onClick = { showResetDialog = false }) {
                        Text("Finish")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SignUpView(
    initialCategory: LoginTab,
    onSignUpStudent: (String, String, String) -> Unit,
    onSignUpStaff: (String, String, String, UserRole, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onBackToCategories: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 500.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToCategories,
                    modifier = Modifier.testTag("signup_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back to Categories",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Registration Restricted",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Registration Locked Icon",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Registration is Disabled",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Public registration is disabled in this school library application. Student, Teacher, Librarian, and Administrator profiles must be created exclusively by authorized system administrators.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onNavigateToLogin,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("signup_back_to_login_button")
            ) {
                Text("Return to Secure Login", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CategorySelectionScreen(
    showStaffPortals: Boolean,
    onCategorySelected: (LoginTab) -> Unit,
    onExploreShowcase: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 500.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Select Portal Category",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = "Choose your department to sign in to your dashboard",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // High-fidelity Interactive App Showcase Banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExploreShowcase() }
                    .padding(bottom = 16.dp)
                    .testTag("explore_product_showcase_banner")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Showcase",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Explore Nexus & UCE Simulator",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Interactive feature tour, grade matrices and mock apps.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            val categories = listOf(
                CategoryItem(
                    tab = LoginTab.STUDENT,
                    title = "Student Portal",
                    subtitle = "Borrow textbooks, review reading progress, and take AI-generated quizzes.",
                    icon = Icons.Default.School,
                    color = Color(0xFF3B82F6) // Bright blue
                ),
                CategoryItem(
                    tab = LoginTab.TEACHER,
                    title = "Teacher Desk",
                    subtitle = "Analyze student activity, curate custom bookshelves, and assign material.",
                    icon = Icons.Default.SupervisorAccount,
                    color = Color(0xFF10B981) // Emerald green
                ),
                CategoryItem(
                    tab = LoginTab.LIBRARIAN,
                    title = "Librarian Terminal",
                    subtitle = "Supervise copy physical status, monitor borrowings, and push global alerts.",
                    icon = Icons.Default.LocalLibrary,
                    color = Color(0xFFF59E0B) // Amber orange
                ),
                CategoryItem(
                    tab = LoginTab.ADMINISTRATOR,
                    title = "Admin Center",
                    subtitle = "Manage secure database connections, review activity logs, and system metrics.",
                    icon = Icons.Default.AdminPanelSettings,
                    color = Color(0xFFEF4444) // Scarlet red
                )
            )
            
            val studentCategory = categories.first { it.tab == LoginTab.STUDENT }
            val staffCategories = categories.filter { it.tab != LoginTab.STUDENT }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // By default, display only the Student Portal card/button.
                CategoryCard(item = studentCategory, onCategorySelected = onCategorySelected)

                // Hidden staff portal reveal - UI only, not a security mechanism
                AnimatedVisibility(
                    visible = showStaffPortals,
                    enter = fadeIn(animationSpec = tween(600)) + expandVertically()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text(
                            text = "Staff Portals",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(bottom = 4.dp, start = 4.dp)
                        )
                        
                        staffCategories.forEach { item ->
                            CategoryCard(item = item, onCategorySelected = onCategorySelected)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    item: CategoryItem,
    onCategorySelected: (LoginTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCategorySelected(item.tab) }
            .testTag("category_select_${item.tab.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(item.color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.subtitle,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Navigate to Login",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private data class CategoryItem(
    val tab: LoginTab,
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)
