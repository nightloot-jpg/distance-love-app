package com.example.distancelove.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.distancelove.ui.theme.*
import com.example.distancelove.viewmodel.NosotrosViewModel

@Composable
fun ProfileAuthDialog(
    viewModel: NosotrosViewModel,
    onDismiss: () -> Unit
) {
    val currentUser by viewModel.currentUserProfile.collectAsState()
    val partner by viewModel.partnerProfile.collectAsState()
    val couple by viewModel.coupleInfo.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var editFullName by remember(currentUser) { mutableStateOf(currentUser?.fullName.orEmpty()) }
    var editCity by remember(currentUser) { mutableStateOf(currentUser?.city.orEmpty()) }
    var editStatus by remember(currentUser) { mutableStateOf(currentUser?.status.orEmpty()) }
    var editBio by remember(currentUser) { mutableStateOf(currentUser?.bio.orEmpty()) }

    var pairCodeInput by remember { mutableStateOf("") }
    var generatedCode by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateAvatarFromUri(it) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground.copy(alpha = 0.92f))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp)
                    .clip(RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header with Close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mi Perfil",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Serif,
                                fontSize = 22.sp
                            ),
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Cerrar")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!authError.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DestructiveRed.copy(alpha = 0.15f))
                                .border(1.dp, DestructiveRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = authError.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = DestructiveRed,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    currentUser?.let { user ->
                        // Avatar with upload action
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .testTag("change_avatar_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            SmartImage(
                                model = user.avatarUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(86.dp)
                                    .clip(CircleShape)
                                    .border(2.5.dp, RosePrimary, CircleShape)
                            )

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(RoseGradient)
                                    .align(Alignment.BottomEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PhotoCamera,
                                    contentDescription = "Cambiar foto",
                                    tint = DarkBackground,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = user.fullName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (isEditing) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TextField(
                                    value = editFullName,
                                    onValueChange = { editFullName = it },
                                    label = { Text("Nombre") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = customFieldColors()
                                )
                                TextField(
                                    value = editCity,
                                    onValueChange = { editCity = it },
                                    label = { Text("Ciudad") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = customFieldColors()
                                )
                                TextField(
                                    value = editStatus,
                                    onValueChange = { editStatus = it },
                                    label = { Text("Estado (ej: Libre, Trabajando)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = customFieldColors()
                                )
                                TextField(
                                    value = editBio,
                                    onValueChange = { editBio = it },
                                    label = { Text("Biografía") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = customFieldColors()
                                )

                                RoseGradientButton(
                                    onClick = {
                                        viewModel.updateProfile(
                                            fullName = editFullName,
                                            city = editCity,
                                            timeZone = user.timeZone,
                                            status = editStatus,
                                            bio = editBio
                                        )
                                        isEditing = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Guardar Cambios", fontWeight = FontWeight.Bold, color = DarkBackground)
                                }
                            }
                        } else {
                            GlassCard(
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(text = "📍 Ciudad: ${user.city}", style = MaterialTheme.typography.bodyMedium)
                                    Text(text = "🕒 Zona horaria: ${user.timeZone}", style = MaterialTheme.typography.bodyMedium)
                                    Text(text = "💭 Estado: ${user.status}", style = MaterialTheme.typography.bodyMedium)
                                    if (user.bio.isNotBlank()) {
                                        Text(text = "✨ ${user.bio}", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = { isEditing = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(50),
                                border = androidx.compose.foundation.BorderStroke(1.dp, RosePrimary)
                            ) {
                                Text("Editar Perfil", color = RosePrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Real Couple Status & Pairing Section
                        Text(
                            text = "Vuestra Pareja",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (partner != null) {
                            GlassCard(
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    SmartImage(
                                        model = partner?.avatarUrl ?: "feed2",
                                        contentDescription = partner?.fullName,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, RosePrimary, CircleShape)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = partner?.fullName ?: "Pareja",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "📍 ${partner?.city ?: "Ciudad"} · ${partner?.status ?: "Libre"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Filled.Favorite,
                                        contentDescription = "Vinculados",
                                        tint = RoseAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        } else {
                            // Unlinked couple pairing card
                            GlassCard(
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Aún no has vinculado a tu pareja.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMuted
                                    )

                                    if (generatedCode != null) {
                                        Text(
                                            text = "Comparte este código con tu pareja: $generatedCode",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = RosePrimary
                                        )
                                    } else {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.createCoupleInviteCode { code ->
                                                    generatedCode = code
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(50),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, RosePrimary)
                                        ) {
                                            Text("Generar código de invitación", color = RosePrimary)
                                        }
                                    }

                                    Divider(color = DarkCardBorder)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        TextField(
                                            value = pairCodeInput,
                                            onValueChange = { pairCodeInput = it },
                                            placeholder = { Text("Pegar código (ej: LOVE-1234)", fontSize = 12.sp) },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            colors = customFieldColors()
                                        )

                                        RoseGradientButton(
                                            onClick = {
                                                if (pairCodeInput.isNotBlank()) {
                                                    viewModel.linkCoupleWithCode(pairCodeInput)
                                                }
                                            },
                                            enabled = pairCodeInput.isNotBlank() && !isAuthLoading
                                        ) {
                                            Text("Vincular", fontWeight = FontWeight.Bold, color = DarkBackground)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Real Logout Button
                        Button(
                            onClick = {
                                viewModel.logout()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DestructiveRed.copy(alpha = 0.2f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DestructiveRed.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("logout_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ExitToApp,
                                contentDescription = "Cerrar sesión",
                                tint = DestructiveRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cerrar Sesión",
                                color = DestructiveRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun customFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = DarkSurfaceElevated,
    unfocusedContainerColor = DarkSurfaceElevated,
    focusedIndicatorColor = RosePrimary,
    unfocusedIndicatorColor = Color.Transparent,
    cursorColor = RosePrimary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedLabelColor = RosePrimary,
    unfocusedLabelColor = TextMuted
)
