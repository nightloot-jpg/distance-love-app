package com.example.distancelove

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
                        val navController = rememberNavController()
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route ?: NavDestination.HOME.route
                        val currentDestination = NavDestination.entries.find { it.route == currentRoute } ?: NavDestination.HOME

                        Scaffold(
                            modifier = Modifier
                                .fillMaxSize()
                                .imePadding(),
                            containerColor = DarkBackground,
                            contentWindowInsets = WindowInsets.safeDrawing,
                            bottomBar = {
                                BottomNavBar(
                                    currentDestination = currentDestination,
                                    onNavigate = { dest ->
                                        if (dest.route != currentRoute) {
                                            navController.navigate(dest.route) {
                                                popUpTo(NavDestination.HOME.route) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
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
                                                Color(0xFF181014)
                                            ),
                                            radius = 1200f
                                        )
                                    )
                                    .padding(innerPadding)
                            ) {
                                NavHost(
                                    navController = navController,
                                    startDestination = NavDestination.HOME.route,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    composable(NavDestination.HOME.route) {
                                        HomeScreen(viewModel = viewModel)
                                    }
                                    composable(NavDestination.CONEXION.route) {
                                        ConexionScreen(viewModel = viewModel)
                                    }
                                    composable(NavDestination.CINE.route) {
                                        CineScreen(viewModel = viewModel)
                                    }
                                    composable(NavDestination.FEED.route) {
                                        FeedScreen(viewModel = viewModel)
                                    }
                                    composable(NavDestination.BOVEDA.route) {
                                        BovedaScreen(viewModel = viewModel)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
