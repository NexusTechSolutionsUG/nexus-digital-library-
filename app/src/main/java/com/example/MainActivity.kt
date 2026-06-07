package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.LibraryDashboard
import com.example.ui.LibraryViewModel
import com.example.ui.auth.AuthEntryScreen
import com.example.ui.auth.AuthState
import com.example.ui.auth.AuthViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup highly robust uncaught exception handler to prevent and clear corrupt login/state crash loops
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("MainActivity", "Uncaught runtime crash on thread: ${thread.name}", throwable)
            try {
                // Clear active session to break out of any potential cached auth or database crash loops
                getSharedPreferences("nexus_auth_prefs", android.content.Context.MODE_PRIVATE)
                    .edit()
                    .remove("saved_session")
                    .apply()
                deleteSharedPreferences("nexus_secure_auth_prefs")
            } catch (ex: Exception) {
                // Ignore silently in crash path
            }
            // Delegate to the standard default handler so the system can gracefully log, track, and handle the crash
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable)
            } else {
                android.os.Process.killProcess(android.os.Process.myPid())
                java.lang.System.exit(10)
            }
        }

        try {
            // Setup edge-to-edge drawing with safety fallback
            enableEdgeToEdge()
        } catch (e: Throwable) {
            android.util.Log.e("MainActivity", "enableEdgeToEdge initialization error", e)
        }
        
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val authViewModel: AuthViewModel = viewModel()
                    val authState by authViewModel.authState.collectAsState()

                    when (val state = authState) {
                        is AuthState.Authenticated -> {
                            val viewModel: LibraryViewModel = viewModel()
                            val user = state.session.user
                            val metadata = user?.userMetadata

                            LaunchedEffect(user) {
                                viewModel.setSessionUser(
                                    firstName = metadata?.firstName,
                                    lastName = metadata?.lastName,
                                    email = user?.email,
                                    roleStr = metadata?.role
                                )
                            }

                            LibraryDashboard(
                                viewModel = viewModel,
                                onLogout = { authViewModel.logout() }
                            )
                        }
                        else -> {
                            AuthEntryScreen(viewModel = authViewModel)
                        }
                    }
                }
            }
        }
    }
}
