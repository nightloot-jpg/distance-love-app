package com.example.distancelove.ui.screens

import android.view.MotionEvent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.distancelove.data.CountdownTime
import com.example.distancelove.data.UserProfile
import com.example.distancelove.data.local.NoteEntity
import com.example.distancelove.ui.components.GlassCard
import com.example.distancelove.ui.components.ProfileAuthDialog
import com.example.distancelove.ui.components.ScreenHeader
import com.example.distancelove.ui.components.SmartImage
import com.example.distancelove.ui.theme.*
import com.example.distancelove.viewmodel.NosotrosViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HomeScreen(
    viewModel: NosotrosViewModel,
    modifier: Modifier = Modifier
) {
    val currentTime by viewModel.currentTime.collectAsState()
    val currentUser by viewModel.currentUserProfile.collectAsState()
    val partner by viewModel.partnerProfile.collectAsState()
    val countdown: CountdownTime = remember(currentTime) { viewModel.getReunionCountdown() }
    val isHoldingHeart by viewModel.isHoldingHeart.collectAsState()
    val heartRipples by viewModel.heartRipples.collectAsState()
    val notes by viewModel.notes.collectAsState()

    var newNoteText by remember { mutableStateOf("") }
    var showProfileDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ScreenHeader(
                    title = "Nosotros",
                    subtitle = "Espacio compartido",
                    trailingContent = {
                        IconButton(
                            onClick = { showProfileDialog = true },
                            modifier = Modifier.testTag("open_profile_button")
                        ) {
                            if (currentUser != null) {
                                SmartImage(
                                    model = currentUser?.avatarUrl ?: "feed3",
                                    contentDescription = "Mi Perfil",
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, RosePrimary, CircleShape)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.AccountCircle,
                                    contentDescription = "Cuenta",
                                    tint = RosePrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                )
            }

            // Real Profiles of User and Partner
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    currentUser?.let { user ->
                        val cityTime = remember(currentTime, user.timeZone) {
                            viewModel.formatCityTime(user.timeZone)
                        }

                        RealProfileGlassCard(
                            user = user,
                            cityTime = cityTime,
                            isMe = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (partner != null) {
                        val p = partner!!
                        val partnerTime = remember(currentTime, p.timeZone) {
                            viewModel.formatCityTime(p.timeZone)
                        }

                        RealProfileGlassCard(
                            user = p,
                            cityTime = partnerTime,
                            isMe = false,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        // Unlinked Partner invite placeholder card
                        GlassCard(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showProfileDialog = true }
                                .testTag("link_partner_card"),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxHeight(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PersonAdd,
                                    contentDescription = "Vincular pareja",
                                    tint = RosePrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Vincular pareja",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Toca para compartir código",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Reunion Countdown
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reunion_countdown_card"),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "PRÓXIMO REENCUENTRO · 18 OCT",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${countdown.days}",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 64.sp,
                                brush = RoseGradient,
                                fontFamily = FontFamily.Serif
                            ),
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "días para volver a vernos",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${countdown.hours}h",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Text(
                                text = "${countdown.minutes}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Text(
                                text = "${countdown.seconds}s",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // Live Pulsing Heartbeat Button
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "HeartbeatAnimation")
                    val beatScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = if (isHoldingHeart) 1.18f else 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(450, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "beatScale"
                    )

                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .testTag("heartbeat_container"),
                        contentAlignment = Alignment.Center
                    ) {
                        heartRipples.forEach { _ ->
                            RippleRing()
                        }

                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .scale(if (isHoldingHeart) beatScale else 1f)
                                .clip(CircleShape)
                                .background(RoseGradient)
                                .pointerInteropFilter { motionEvent ->
                                    when (motionEvent.action) {
                                        MotionEvent.ACTION_DOWN -> {
                                            viewModel.startHeartbeat()
                                            true
                                        }
                                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                            viewModel.stopHeartbeat()
                                            true
                                        }
                                        else -> false
                                    }
                                }
                                .testTag("heartbeat_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = "Enviar latido",
                                tint = DarkBackground,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isHoldingHeart) "Sintiendo vuestro latido en directo…" else "Mantén pulsado para enviar tu latido",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isHoldingHeart) RosePrimary else TextMuted
                    )
                }
            }

            // Real Blackboard / Notes Section
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("blackboard_section"),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Pizarrón",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Input Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextField(
                                value = newNoteText,
                                onValueChange = { newNoteText = it },
                                placeholder = {
                                    Text(
                                        "Nota o recordatorio…",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(50),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = DarkSurfaceElevated,
                                    unfocusedContainerColor = DarkSurfaceElevated,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = RosePrimary,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (newNoteText.isNotBlank()) {
                                            viewModel.addNote(newNoteText)
                                            newNoteText = ""
                                        }
                                    }
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("note_input_field")
                            )

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(RoseGradient)
                                    .clickable {
                                        if (newNoteText.isNotBlank()) {
                                            viewModel.addNote(newNoteText)
                                            newNoteText = ""
                                        }
                                    }
                                    .testTag("add_note_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Añadir nota",
                                    tint = DarkBackground,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Real Notes List
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            notes.forEach { note ->
                                NoteItemRow(
                                    note = note,
                                    onToggle = { viewModel.toggleNote(note) },
                                    onDelete = { viewModel.deleteNote(note.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showProfileDialog) {
            ProfileAuthDialog(
                viewModel = viewModel,
                onDismiss = { showProfileDialog = false }
            )
        }
    }
}

@Composable
private fun RealProfileGlassCard(
    user: UserProfile,
    cityTime: String,
    isMe: Boolean,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.testTag(if (isMe) "my_profile_card" else "partner_profile_card"),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SmartImage(
                    model = user.avatarUrl,
                    contentDescription = user.fullName,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, RosePrimary, CircleShape)
                )
                Column {
                    Text(
                        text = user.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen)
                        )
                        Text(
                            text = user.status,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = cityTime,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 32.sp,
                    fontFamily = FontFamily.Serif
                ),
                fontWeight = FontWeight.Bold
            )

            Text(
                text = user.city,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (user.weatherIcon == "sun") Icons.Filled.WbSunny else Icons.Filled.WaterDrop,
                        contentDescription = "Clima",
                        tint = RosePrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = user.weatherTemp,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (user.batteryLevel > 50) Icons.Filled.BatteryFull else Icons.Filled.BatteryAlert,
                        contentDescription = "Batería",
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${user.batteryLevel}%",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun RippleRing() {
    val infiniteTransition = rememberInfiniteTransition(label = "RippleAnim")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleScale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleAlpha"
    )

    Box(
        modifier = Modifier
            .size(96.dp)
            .scale(scale)
            .clip(CircleShape)
            .border(2.dp, RosePrimary.copy(alpha = alpha), CircleShape)
    )
}

@Composable
private fun NoteItemRow(
    note: NoteEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated.copy(alpha = 0.7f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("note_item_${note.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .border(1.5.dp, RosePrimary, CircleShape)
                .background(if (note.isDone) RosePrimary else Color.Transparent)
                .clickable { onToggle() }
                .testTag("toggle_note_${note.id}"),
            contentAlignment = Alignment.Center
        ) {
            if (note.isDone) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Completada",
                    tint = DarkBackground,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = note.text,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (note.isDone) TextDecoration.LineThrough else TextDecoration.None,
                color = if (note.isDone) TextMuted else TextPrimary,
                fontSize = 14.sp
            )
            Text(
                text = note.authorName,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = TextMuted
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .size(32.dp)
                .testTag("delete_note_${note.id}")
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Borrar",
                tint = TextSubtle,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
