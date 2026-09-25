package com.example.distancelove

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.distancelove.data.AuthState
import com.example.distancelove.ui.components.BottomNavBar
import com.example.distancelove.ui.components.NavDestination
import com.example.distancelove.ui.screens.*
import com.example.distancelove.ui.theme.DarkBackground
import com.example.distancelove.ui.theme.NosotrosTheme
import com.example.distancelove.ui.theme.RosePrimary
import com.example.distancelove.viewmodel.NosotrosViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: NosotrosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NosotrosTheme {
                val authState by viewModel.authState.collectAsState()

                when (val state = authState) {
                    is AuthState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(DarkBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = null,
                                    tint = RosePrimary,
                                    modifier = Modifier.size(48.dp)
                                )
                                CircularProgressIndicator(
                                    color = RosePrimary,
                                    strokeWidth = 2.5.dp
                                )
                            }
                        }
                    }

                    is AuthState.Unauthenticated, is AuthState.Error -> {
                        AuthScreen(viewModel = viewModel)
                    }

                    is AuthState.Authenticated -> {
                        var currentDestination by remember { mutableStateOf(NavDestination.HOME) }

                        BackHandler(enabled = currentDestination != NavDestination.HOME) {
                            currentDestination = NavDestination.HOME
                        }

                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            containerColor = DarkBackground,
                            contentWindowInsets = WindowInsets.statusBars,
                            bottomBar = {
                                BottomNavBar(
                                    currentDestination = currentDestination,
                                    onNavigate = { dest -> currentDestination = dest }
                                )
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color(0x38551E2E),
                                                Color(0x181014)
                                            ),
                                            radius = 1200f
                                        )
                                    )
                                    .padding(innerPadding)
                            ) {
                                when (currentDestination) {
                                    NavDestination.HOME -> HomeScreen(viewModel = viewModel)
                                    NavDestination.CONEXION -> ConexionScreen(viewModel = viewModel)
                                    NavDestination.CINE -> CineScreen(viewModel = viewModel)
                                    NavDestination.FEED -> FeedScreen(viewModel = viewModel)
                                    NavDestination.BOVEDA -> BovedaScreen(viewModel = viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
