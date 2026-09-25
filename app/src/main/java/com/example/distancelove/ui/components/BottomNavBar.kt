package com.example.distancelove.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.distancelove.ui.theme.*

enum class NavDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    HOME("home", "Hogar", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    CONEXION("conexion", "Conexión", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder, "nav_conexion"),
    CINE("cine", "Cine", Icons.Filled.Movie, Icons.Outlined.Movie, "nav_cine"),
    FEED("feed", "Feed", Icons.Filled.PhotoLibrary, Icons.Outlined.PhotoLibrary, "nav_feed"),
    BOVEDA("boveda", "Bóveda", Icons.Filled.Lock, Icons.Outlined.Lock, "nav_boveda")
}

@Composable
fun BottomNavBar(
    currentDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(DarkSurface.copy(alpha = 0.92f))
                .border(1.dp, DarkCardBorder, RoundedCornerShape(32.dp))
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavDestination.entries.forEach { dest ->
                    val isSelected = currentDestination == dest
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) RosePrimary else TextMuted,
                        label = "NavColor"
                    )

                    val interactionSource = remember { MutableInteractionSource() }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(
                                interactionSource = interactionSource,
                                indication = ripple(color = RosePrimary.copy(alpha = 0.3f))
                            ) { onNavigate(dest) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag(dest.tag)
                    ) {
                        Icon(
                            imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                            contentDescription = dest.label,
                            tint = contentColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dest.label,
                            fontSize = 10.sp,
                            color = contentColor,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
