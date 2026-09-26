package com.example.distancelove.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.distancelove.ui.components.GlassCard
import com.example.distancelove.ui.components.ScreenHeader
import com.example.distancelove.ui.theme.*
import com.example.distancelove.viewmodel.NosotrosViewModel
import kotlin.math.roundToInt

enum class VaultTab(val label: String) {
    MATCH("Desire Match"),
    RECUERDOS("Recuerdos")
}

@Composable
fun BovedaScreen(
    viewModel: NosotrosViewModel,
    modifier: Modifier = Modifier
) {
    val isUnlocked by viewModel.vaultUnlocked.collectAsState()

    if (isUnlocked) {
        UnlockedVaultContent(viewModel = viewModel, modifier = modifier)
    } else {
        VaultPinUnlockScreen(viewModel = viewModel, modifier = modifier)
    }
}

@Composable
private fun VaultPinUnlockScreen(
    viewModel: NosotrosViewModel,
    modifier: Modifier = Modifier
) {
    val pinInput by viewModel.vaultPinInput.collectAsState()
    val isPinError by viewModel.vaultPinError.collectAsState()
    val isScanning by viewModel.isBiometricScanning.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Lock Icon Circle
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(RoseGradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Bóveda Bloqueada",
                tint = DarkBackground,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Bóveda Íntima",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 32.sp,
                fontFamily = FontFamily.Serif
            ),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Introduce vuestro PIN (demo: 1402)",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 4 PIN Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 4) {
                val isFilled = i < pinInput.length
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .border(
                            width = 1.5.dp,
                            color = if (isPinError) DestructiveRed else RosePrimary,
                            shape = CircleShape
                        )
                        .background(
                            when {
                                isPinError -> DestructiveRed
                                isFilled -> RosePrimary
                                else -> Color.Transparent
                            }
                        )
                )
            }
        }

        if (isPinError) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "PIN incorrecto",
                style = MaterialTheme.typography.labelSmall,
                color = DestructiveRed
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // 3x4 Number Keypad
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("BIO", "0", "DEL")
        )

        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    row.forEach { key ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(DarkSurfaceElevated.copy(alpha = 0.8f))
                                .border(1.dp, DarkCardBorder, CircleShape)
                                .clickable {
                                    when (key) {
                                        "BIO" -> viewModel.scanBiometrics()
                                        "DEL" -> viewModel.deleteVaultDigit()
                                        else -> viewModel.enterVaultDigit(key)
                                    }
                                }
                                .testTag("pin_key_$key"),
                            contentAlignment = Alignment.Center
                        ) {
                            when (key) {
                                "BIO" -> {
                                    Icon(
                                        imageVector = Icons.Filled.Fingerprint,
                                        contentDescription = "Huella dactilar",
                                        tint = if (isScanning) RoseAccent else RosePrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                "DEL" -> {
                                    Icon(
                                        imageVector = Icons.Filled.Backspace,
                                        contentDescription = "Borrar",
                                        tint = TextMuted,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                else -> {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.displayMedium.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontSize = 24.sp
                                        ),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isScanning) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Verificando huella…",
                style = MaterialTheme.typography.labelSmall,
                color = RosePrimary
            )
        }
    }
}

@Composable
private fun UnlockedVaultContent(
    viewModel: NosotrosViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(VaultTab.MATCH) }
    val matchFlash by viewModel.matchFlash.collectAsState()

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
                    title = "Bóveda",
                    subtitle = "Solo vosotros dos",
                    trailingContent = {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(50))
                                .clickable { viewModel.lockVault() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("lock_vault_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "Bloquear",
                                    tint = RosePrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Bloquear",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                )
            }

            // Subtabs: Desire Match / Recuerdos
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                    backgroundColor = DarkSurfaceElevated.copy(alpha = 0.9f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        VaultTab.entries.forEach { tab ->
                            val isSelected = currentTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isSelected) RoseGradient else androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)))
                                    .clickable { currentTab = tab }
                                    .padding(vertical = 10.dp)
                                    .testTag("vault_tab_${tab.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isSelected) DarkBackground else TextMuted,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            if (currentTab == VaultTab.MATCH) {
                item { RealDesireMatchSection(viewModel) }
            } else {
                item { RealVoiceRecuerdosSection(viewModel) }
            }
        }

        // Match Flash Celebration Overlay
        AnimatedVisibility(
            visible = matchFlash != null,
            enter = fadeIn() + scaleIn(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground.copy(alpha = 0.85f))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Match",
                            tint = RoseAccent,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "¡Es un match!",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontFamily = FontFamily.Serif,
                                brush = RoseGradient
                            ),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = matchFlash.orEmpty(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RealDesireMatchSection(viewModel: NosotrosViewModel) {
    val currentIndex by viewModel.currentDesireIndex.collectAsState()
    val matches by viewModel.matchedDesires.collectAsState()
    val currentCard = viewModel.desireCards.getOrNull(currentIndex)

    var offsetX by remember { mutableStateOf(0f) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Doble ciega: solo se revela si ambos decís que sí.",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        )

        // Desire Swipe Card Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .testTag("desire_card_container"),
            contentAlignment = Alignment.Center
        ) {
            if (currentCard != null) {
                val rotation = (offsetX / 20f).coerceIn(-20f, 20f)

                GlassCard(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset { IntOffset(offsetX.roundToInt(), 0) }
                        .rotate(rotation)
                        .draggable(
                            orientation = Orientation.Horizontal,
                            state = rememberDraggableState { delta ->
                                offsetX += delta
                            },
                            onDragStopped = {
                                if (offsetX > 150f) {
                                    viewModel.decideDesireCard(true)
                                } else if (offsetX < -150f) {
                                    viewModel.decideDesireCard(false)
                                }
                                offsetX = 0f
                            }
                        )
                        .testTag("current_desire_card"),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = "Deseo",
                                tint = RosePrimary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = currentCard.text,
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 26.sp
                                ),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Desliza → sí · ← no",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }

                        if (offsetX > 60f) {
                            Text(
                                text = "SÍ",
                                color = SuccessGreen,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(20.dp)
                                    .align(Alignment.TopStart)
                            )
                        }
                        if (offsetX < -60f) {
                            Text(
                                text = "NO",
                                color = DestructiveRed,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(20.dp)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                }
            } else {
                GlassCard(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Has visto todas las cartas de hoy ✨",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // Decision Action Buttons
        if (currentCard != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkCardBorder, CircleShape)
                        .clickable { viewModel.decideDesireCard(false) }
                        .testTag("desire_reject_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "No",
                        tint = TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(32.dp))

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(RoseGradient)
                        .clickable { viewModel.decideDesireCard(true) }
                        .testTag("desire_accept_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Sí",
                        tint = DarkBackground,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Matches Section from Database
        Text(
            text = "Vuestros matches",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (matches.isEmpty()) {
            Text(
                text = "Aún no hay coincidencias reveladas.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                matches.forEach { matchEntity ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "💞", fontSize = 16.sp)
                            Text(
                                text = matchEntity.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RealVoiceRecuerdosSection(viewModel: NosotrosViewModel) {
    val voiceMemories by viewModel.voiceMemories.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(DarkSurfaceElevated)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = "Cifrado",
                tint = SuccessGreen,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Cifrado de extremo a extremo · solo vuestros dispositivos",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }

        voiceMemories.forEach { memory ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("voice_memory_${memory.id}"),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(RoseGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Reproducir",
                            tint = DarkBackground,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = memory.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${memory.authorName} · ${memory.duration}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Privado",
                        tint = RosePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Text(
            text = "Bóveda cifrada",
            style = MaterialTheme.typography.titleMedium,
            color = TextMuted
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (i in 0 until 3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurfaceElevated.copy(alpha = 0.6f))
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Espacio bloqueado",
                        tint = TextSubtle,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
