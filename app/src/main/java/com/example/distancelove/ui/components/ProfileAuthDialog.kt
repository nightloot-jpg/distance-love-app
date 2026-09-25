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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.distancelove.ui.theme.*
import com.example.distancelove.viewmodel.NosotrosViewModel

enum class AuthMode {
    PROFILE, LOGIN, REGISTER
}

@Composable
fun ProfileAuthDialog(
    viewModel: NosotrosViewModel,
    onDismiss: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()

    var mode by remember { mutableStateOf(if (currentUser != null) AuthMode.PROFILE else AuthMode.LOGIN) }

    // Edit Profile form state
    var editFullName by remember(currentUser) { mutableStateOf(currentUser?.fullName.orEmpty()) }
    var editCity by remember(currentUser) { mutableStateOf(currentUser?.city.orEmpty()) }
    var editStatus by remember(currentUser) { mutableStateOf(currentUser?.status.orEmpty()) }
    var editBio by remember(currentUser) { mutableStateOf(currentUser?.bio.orEmpty()) }
    var isEditing by remember { mutableStateOf(false) }

    // Login & Register Form states
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var regUsername by remember { mutableStateOf("") }
    var regFullName by remember { mutableStateOf("") }
    var regCity by remember { mutableStateOf("Madrid") }

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
                            text = when (mode) {
                                AuthMode.PROFILE -> "Perfil & Cuenta"
                                AuthMode.LOGIN -> "Iniciar Sesión"
                                AuthMode.REGISTER -> "Crear Cuenta"
                            },
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

                    if (authError != null) {
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

                    when (mode) {
                        AuthMode.PROFILE -> {
                            currentUser?.let { user ->
                                // Avatar with upload badge
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clickable {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
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
                                            label = { Text("Nombre completo") },
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

                                // Quick Partner Switcher for testing real bilateral DB syncing
                                Text(
                                    text = "Cambiar de usuario en este dispositivo:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    allUsers.forEach { u ->
                                        val isCurrent = u.id == user.id
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(16.dp))
                                                .then(
                                                    if (isCurrent) Modifier.background(RoseGradient)
                                                    else Modifier.background(DarkSurfaceElevated)
                                                )
                                                .clickable { viewModel.switchUser(u) }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = u.fullName,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isCurrent) DarkBackground else TextPrimary
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                TextButton(onClick = { mode = AuthMode.REGISTER }) {
                                    Text("+ Registrar nueva cuenta", color = RosePrimary, fontSize = 12.sp)
                                }
                            }
                        }

                        AuthMode.LOGIN -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                TextField(
                                    value = emailInput,
                                    onValueChange = { emailInput = it },
                                    label = { Text("Correo electrónico") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = customFieldColors()
                                )

                                TextField(
                                    value = passwordInput,
                                    onValueChange = { passwordInput = it },
                                    label = { Text("Contraseña") },
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = customFieldColors()
                                )

                                RoseGradientButton(
                                    onClick = {
                                        viewModel.login(emailInput, passwordInput) {
                                            mode = AuthMode.PROFILE
                                        }
                                    },
                                    enabled = !isAuthLoading && emailInput.isNotBlank() && passwordInput.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (isAuthLoading) "Entrando…" else "Iniciar Sesión",
                                        fontWeight = FontWeight.Bold,
                                        color = DarkBackground
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    TextButton(onClick = { mode = AuthMode.REGISTER }) {
                                        Text("¿No tienes cuenta? Regístrate aquí", color = RosePrimary, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        AuthMode.REGISTER -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TextField(
                                    value = regFullName,
                                    onValueChange = { regFullName = it },
                                    label = { Text("Tu nombre") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = customFieldColors()
                                )

                                TextField(
                                    value = regUsername,
                                    onValueChange = { regUsername = it },
                                    label = { Text("Nombre de usuario") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = customFieldColors()
                                )

                                TextField(
                                    value = emailInput,
                                    onValueChange = { emailInput = it },
                                    label = { Text("Correo electrónico") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = customFieldColors()
                                )

                                TextField(
                                    value = passwordInput,
                                    onValueChange = { passwordInput = it },
                                    label = { Text("Contraseña") },
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = customFieldColors()
                                )

                                TextField(
                                    value = regCity,
                                    onValueChange = { regCity = it },
                                    label = { Text("Ciudad actual") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = customFieldColors()
                                )

                                RoseGradientButton(
                                    onClick = {
                                        viewModel.register(
                                            email = emailInput,
                                            pass = passwordInput,
                                            username = regUsername,
                                            fullName = regFullName,
                                            city = regCity,
                                            tz = "Europe/Madrid"
                                        ) {
                                            mode = AuthMode.PROFILE
                                        }
                                    },
                                    enabled = !isAuthLoading && emailInput.isNotBlank() && passwordInput.isNotBlank() && regFullName.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (isAuthLoading) "Creando…" else "Crear Cuenta",
                                        fontWeight = FontWeight.Bold,
                                        color = DarkBackground
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    TextButton(onClick = { mode = AuthMode.LOGIN }) {
                                        Text("¿Ya tienes cuenta? Inicia sesión", color = RosePrimary, fontSize = 12.sp)
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
