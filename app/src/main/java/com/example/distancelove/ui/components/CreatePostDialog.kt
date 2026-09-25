package com.example.distancelove.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.distancelove.ui.theme.*
import com.example.distancelove.viewmodel.NosotrosViewModel

@Composable
fun CreatePostDialog(
    viewModel: NosotrosViewModel,
    onDismiss: () -> Unit
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var caption by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        selectedImageUri = uri
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
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Nueva Publicación",
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

                    // Photo Picker Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .testTag("select_photo_area"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Foto seleccionada",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AddPhotoAlternate,
                                    contentDescription = "Elegir foto",
                                    tint = RosePrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "Toca para elegir una foto",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Caption
                    TextField(
                        value = caption,
                        onValueChange = { caption = it },
                        placeholder = { Text("Escribe un mensaje para tu amor…", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .testTag("post_caption_input"),
                        shape = RoundedCornerShape(18.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = RosePrimary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Location
                    TextField(
                        value = location,
                        onValueChange = { location = it },
                        placeholder = { Text("Lugar (ej: Shibuya, Tokio / Madrid)", style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = "Ubicación",
                                tint = RosePrimary,
                                modifier = Modifier.size(18.dp)
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
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Publish Button
                    RoseGradientButton(
                        onClick = {
                            selectedImageUri?.let { uri ->
                                isSubmitting = true
                                viewModel.createPost(uri, caption, location)
                                isSubmitting = false
                                onDismiss()
                            }
                        },
                        enabled = selectedImageUri != null && !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_post_button")
                    ) {
                        Text(
                            text = if (isSubmitting) "Publicando…" else "Publicar en Nuestro Feed",
                            fontWeight = FontWeight.Bold,
                            color = DarkBackground
                        )
                    }
                }
            }
        }
    }
}
