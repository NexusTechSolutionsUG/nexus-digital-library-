package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
        
        // Setup edge-to-edge drawing
        enableEdgeToEdge()
        
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
                            viewModel.setSessionUser(
                                firstName = metadata?.firstName,
                                lastName = metadata?.lastName,
                                email = user?.email,
                                roleStr = metadata?.role
                            )
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
