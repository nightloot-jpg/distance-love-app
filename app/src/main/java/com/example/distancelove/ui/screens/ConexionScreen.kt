package com.example.distancelove.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.distancelove.ui.components.GlassCard
import com.example.distancelove.ui.components.RoseGradientButton
import com.example.distancelove.ui.components.ScreenHeader
import com.example.distancelove.ui.theme.*
import com.example.distancelove.viewmodel.NosotrosViewModel

enum class ConexionTab(val label: String) {
    DIARIA("Diaria"),
    RETOS("Retos"),
    SECRETAS("Secretas")
}

@Composable
fun ConexionScreen(
    viewModel: NosotrosViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(ConexionTab.DIARIA) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ScreenHeader(
                title = "Conexión",
                subtitle = "Preguntas & Retos"
            )
        }

        // Segmented Tab Selector
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("conexion_tabs_selector"),
                shape = RoundedCornerShape(50),
                backgroundColor = DarkSurfaceElevated.copy(alpha = 0.9f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ConexionTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .then(
                                    if (isSelected) Modifier.background(RoseGradient)
                                    else Modifier.background(Color.Transparent)
                                )
                                .clickable { selectedTab = tab }
                                .padding(vertical = 10.dp)
                                .testTag("tab_${tab.name.lowercase()}"),
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

        when (selectedTab) {
            ConexionTab.DIARIA -> {
                item { RealDailyQuestionSection(viewModel) }
            }
            ConexionTab.RETOS -> {
                item { RealChallengesSection(viewModel) }
            }
            ConexionTab.SECRETAS -> {
                item { RealSecretQuestionsSection(viewModel) }
            }
        }
    }
}

@Composable
private fun RealDailyQuestionSection(viewModel: NosotrosViewModel) {
    val currentUser by viewModel.currentUserProfile.collectAsState()
    val partner by viewModel.partnerProfile.collectAsState()
    val dailyAnswers by viewModel.dailyAnswers.collectAsState()
    val dailyChats by viewModel.dailyChats.collectAsState()

    val myAnswer = remember(dailyAnswers, currentUser) {
        dailyAnswers.find { it.userId == currentUser?.id }
    }
    val partnerAnswer = remember(dailyAnswers, currentUser) {
        dailyAnswers.find { it.userId != currentUser?.id }
    }

    val isRevealed = myAnswer != null
    var userDraftAnswer by remember { mutableStateOf("") }
    var chatDraft by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("daily_question_card"),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "PREGUNTA DEL DÍA",
                    style = MaterialTheme.typography.labelSmall,
                    color = RosePrimary,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "¿Qué pequeño gesto mío te hizo sentir más querida esta semana?",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontSize = 24.sp
                    ),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("user_response_card"),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Tu respuesta",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (myAnswer != null) {
                    Text(
                        text = myAnswer.answer,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                } else {
                    TextField(
                        value = userDraftAnswer,
                        onValueChange = { userDraftAnswer = it },
                        placeholder = { Text("Escribe con el corazón…", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("user_answer_input"),
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

                    Spacer(modifier = Modifier.height(12.dp))

                    RoseGradientButton(
                        onClick = { viewModel.submitDailyAnswer(userDraftAnswer) },
                        enabled = userDraftAnswer.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_daily_answer_button")
                    ) {
                        Text(
                            text = "Enviar y revelar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DarkBackground
                        )
                    }
                }
            }
        }

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("partner_response_card"),
            shape = RoundedCornerShape(28.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Respuesta de ${partner?.fullName ?: "tu pareja"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = partnerAnswer?.answer ?: "Esperando la respuesta de tu pareja…",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.blur(if (isRevealed && partnerAnswer != null) 0.dp else 12.dp)
                    )
                }

                if (!isRevealed) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(DarkSurface.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(50))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Bloqueado",
                                tint = RosePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Responde para desbloquear",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isRevealed,
            enter = fadeIn() + slideInVertically()
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_chat_section"),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Comentad vuestras respuestas",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        dailyChats.forEach { chatMsg ->
                            val isMe = chatMsg.senderId == currentUser?.id
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentWidth(if (isMe) Alignment.End else Alignment.Start)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(18.dp))
                                        .then(
                                            if (isMe) Modifier.background(RoseGradient)
                                            else Modifier.background(DarkSurfaceElevated)
                                        )
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = chatMsg.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isMe) DarkBackground else TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = chatDraft,
                            onValueChange = { chatDraft = it },
                            placeholder = { Text("Mensaje…", style = MaterialTheme.typography.bodyMedium) },
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
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (chatDraft.isNotBlank()) {
                                        viewModel.sendDailyChat(chatDraft)
                                        chatDraft = ""
                                    }
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("daily_chat_input")
                        )

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(RoseGradient)
                                .clickable {
                                    if (chatDraft.isNotBlank()) {
                                        viewModel.sendDailyChat(chatDraft)
                                        chatDraft = ""
                                    }
                                }
                                .testTag("send_daily_chat_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Enviar",
                                tint = DarkBackground,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RealChallengesSection(viewModel: NosotrosViewModel) {
    val challenges by viewModel.challenges.collectAsState()
    val partner by viewModel.partnerProfile.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        challenges.forEach { challenge ->
            val isBothDone = challenge.meDone && challenge.partnerDone

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("challenge_card_${challenge.id}"),
                shape = RoundedCornerShape(24.dp),
                borderColor = if (isBothDone) RoseAccent.copy(alpha = 0.6f) else DarkCardBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(DarkSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (challenge.iconName) {
                                    "ChefHat" -> Icons.Filled.Restaurant
                                    "Camera" -> Icons.Filled.CameraAlt
                                    "Music" -> Icons.Filled.MusicNote
                                    else -> Icons.Filled.NightsStay
                                },
                                contentDescription = challenge.title,
                                tint = RosePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = challenge.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = challenge.desc,
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ChallengeBadge(label = "Tú", isDone = challenge.meDone)
                            ChallengeBadge(label = partner?.fullName ?: "Pareja", isDone = challenge.partnerDone)
                        }

                        if (isBothDone) {
                            Text(
                                text = "¡Completado! 💞",
                                style = MaterialTheme.typography.labelSmall,
                                color = RosePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.toggleChallenge(challenge, isMe = true) },
                                shape = RoundedCornerShape(50),
                                border = androidx.compose.foundation.BorderStroke(1.dp, RosePrimary),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("toggle_challenge_${challenge.id}")
                            ) {
                                Text(
                                    text = if (challenge.meDone) "Deshacer" else "Hecho",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RosePrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeBadge(label: String, isDone: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isDone) RosePrimary else DarkSurfaceElevated)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$label ${if (isDone) "✓" else "…"}",
            style = MaterialTheme.typography.labelSmall,
            color = if (isDone) DarkBackground else TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun RealSecretQuestionsSection(viewModel: NosotrosViewModel) {
    val secretQuestions by viewModel.secretQuestions.collectAsState()
    val currentUser by viewModel.currentUserProfile.collectAsState()
    var draftSecret by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("create_secret_question_card"),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.VisibilityOff,
                        contentDescription = "Secreto",
                        tint = RosePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Crea una pregunta secreta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = draftSecret,
                        onValueChange = { draftSecret = it },
                        placeholder = {
                            Text(
                                "Solo tu amor podrá verla…",
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
                                if (draftSecret.isNotBlank()) {
                                    viewModel.addSecretQuestion(draftSecret)
                                    draftSecret = ""
                                }
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("secret_question_input")
                    )

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(RoseGradient)
                            .clickable {
                                if (draftSecret.isNotBlank()) {
                                    viewModel.addSecretQuestion(draftSecret)
                                    draftSecret = ""
                                }
                            }
                            .testTag("add_secret_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Añadir",
                            tint = DarkBackground,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        secretQuestions.forEach { sq ->
            val isMyQuestion = sq.authorId == currentUser?.id

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("secret_card_${sq.id}"),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "DE ${sq.authorName.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = sq.questionText,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = when {
                            sq.isAnswered -> "Respondida ✓"
                            isMyQuestion -> "Esperando respuesta…"
                            else -> "Toca para responder"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = RosePrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
