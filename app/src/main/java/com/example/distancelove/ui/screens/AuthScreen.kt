package com.example.distancelove.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.distancelove.ui.components.GlassCard
import com.example.distancelove.ui.components.RoseGradientButton
import com.example.distancelove.ui.theme.*
import com.example.distancelove.viewmodel.NosotrosViewModel

enum class AuthScreenMode {
    LOGIN, REGISTER, FORGOT_PASSWORD
}

@Composable
fun AuthScreen(
    viewModel: NosotrosViewModel,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(AuthScreenMode.LOGIN) }

    val authError by viewModel.authError.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Madrid") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
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
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Brand
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(RoseGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "MyEntik",
                        tint = DarkBackground,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "MyEntik",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontSize = 30.sp,
                        brush = RoseGradient
                    ),
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = when (mode) {
                        AuthScreenMode.LOGIN -> "Inicia sesión en vuestro espacio"
                        AuthScreenMode.REGISTER -> "Crea vuestra cuenta compartida"
                        AuthScreenMode.FORGOT_PASSWORD -> "Recuperación de contraseña"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Error alert box
                if (!authError.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DestructiveRed.copy(alpha = 0.15f))
                            .border(1.dp, DestructiveRed.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = authError.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = DestructiveRed,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Success status alert box
                if (!statusMessage.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SuccessGreen.copy(alpha = 0.15f))
                            .border(1.dp, SuccessGreen.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = statusMessage.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = SuccessGreen,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                when (mode) {
                    AuthScreenMode.LOGIN -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Correo electrónico") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Filled.Email, contentDescription = null, tint = RosePrimary, modifier = Modifier.size(18.dp))
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                modifier = Modifier.fillMaxWidth().testTag("login_email_input"),
                                colors = authFieldColors()
                            )

                            TextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Contraseña") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = RosePrimary, modifier = Modifier.size(18.dp))
                                },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (email.isNotBlank() && password.isNotBlank()) {
                                            viewModel.login(email, password)
                                        }
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
                                colors = authFieldColors()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = "¿Has olvidado tu contraseña?",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RosePrimary,
                                    modifier = Modifier
                                        .clickable {
                                            viewModel.clearAuthMessages()
                                            mode = AuthScreenMode.FORGOT_PASSWORD
                                        }
                                        .padding(vertical = 4.dp)
                                        .testTag("forgot_password_button")
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            RoseGradientButton(
                                onClick = { viewModel.login(email, password) },
                                enabled = !isAuthLoading && email.isNotBlank() && password.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().testTag("login_submit_button")
                            ) {
                                if (isAuthLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = DarkBackground,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Iniciar Sesión",
                                        fontWeight = FontWeight.Bold,
                                        color = DarkBackground
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                TextButton(
                                    onClick = {
                                        viewModel.clearAuthMessages()
                                        mode = AuthScreenMode.REGISTER
                                    }
                                ) {
                                    Text("¿No tienes cuenta? Regístrate aquí", color = RosePrimary, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    AuthScreenMode.REGISTER -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Tu nombre") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = RosePrimary, modifier = Modifier.size(18.dp))
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("register_fullname_input"),
                                colors = authFieldColors()
                            )

                            TextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("Nombre de usuario (@)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("register_username_input"),
                                colors = authFieldColors()
                            )

                            TextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Correo electrónico") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Filled.Email, contentDescription = null, tint = RosePrimary, modifier = Modifier.size(18.dp))
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                modifier = Modifier.fillMaxWidth().testTag("register_email_input"),
                                colors = authFieldColors()
                            )

                            TextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Contraseña (mín. 6 caracteres)") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = RosePrimary, modifier = Modifier.size(18.dp))
                                },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                                modifier = Modifier.fillMaxWidth().testTag("register_password_input"),
                                colors = authFieldColors()
                            )

                            TextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("Ciudad actual") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("register_city_input"),
                                colors = authFieldColors()
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            RoseGradientButton(
                                onClick = {
                                    viewModel.register(
                                        email = email,
                                        pass = password,
                                        username = username,
                                        fullName = fullName,
                                        city = city,
                                        tz = "Europe/Madrid"
                                    )
                                },
                                enabled = !isAuthLoading && email.isNotBlank() && password.length >= 6 && fullName.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().testTag("register_submit_button")
                            ) {
                                if (isAuthLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = DarkBackground,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Crear Cuenta",
                                        fontWeight = FontWeight.Bold,
                                        color = DarkBackground
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                TextButton(
                                    onClick = {
                                        viewModel.clearAuthMessages()
                                        mode = AuthScreenMode.LOGIN
                                    }
                                ) {
                                    Text("¿Ya tienes cuenta? Inicia sesión", color = RosePrimary, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    AuthScreenMode.FORGOT_PASSWORD -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Introduce tu correo y te enviaremos las instrucciones para restablecer tu contraseña.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )

                            TextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Correo electrónico") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Filled.Email, contentDescription = null, tint = RosePrimary, modifier = Modifier.size(18.dp))
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                                modifier = Modifier.fillMaxWidth().testTag("forgot_email_input"),
                                colors = authFieldColors()
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            RoseGradientButton(
                                onClick = { viewModel.sendPasswordReset(email) },
                                enabled = !isAuthLoading && email.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().testTag("send_recovery_button")
                            ) {
                                if (isAuthLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = DarkBackground,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Enviar Enlace",
                                        fontWeight = FontWeight.Bold,
                                        color = DarkBackground
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                TextButton(
                                    onClick = {
                                        viewModel.clearAuthMessages()
                                        mode = AuthScreenMode.LOGIN
                                    }
                                ) {
                                    Text("Volver al inicio de sesión", color = RosePrimary, fontSize = 13.sp)
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
private fun authFieldColors() = TextFieldDefaults.colors(
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
