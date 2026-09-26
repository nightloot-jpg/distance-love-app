package com.example.distancelove.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.distancelove.data.StoryItem
import com.example.distancelove.data.local.PostEntity
import com.example.distancelove.ui.components.CreatePostDialog
import com.example.distancelove.ui.components.GlassCard
import com.example.distancelove.ui.components.ScreenHeader
import com.example.distancelove.ui.components.SmartImage
import com.example.distancelove.ui.theme.*
import com.example.distancelove.viewmodel.NosotrosViewModel
import kotlinx.coroutines.delay

enum class FeedLayoutMode {
    FEED, GRID
}

@Composable
fun FeedScreen(
    viewModel: NosotrosViewModel,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.feedPosts.collectAsState()
    val stories by viewModel.stories.collectAsState()
    val activeStory by viewModel.activeStory.collectAsState()
    var layoutMode by remember { mutableStateOf(FeedLayoutMode.FEED) }
    var showCreatePostDialog by remember { mutableStateOf(false) }

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
                    title = "Nuestro Feed",
                    subtitle = "Solo para dos",
                    trailingContent = {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(RoseGradient)
                                .clickable { showCreatePostDialog = true }
                                .padding(8.dp)
                                .testTag("create_post_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AddAPhoto,
                                contentDescription = "Subir foto",
                                tint = DarkBackground,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )
            }

            // Stories Tray + Layout Switcher
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        stories.forEach { story ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { viewModel.openStory(story) }
                                    .testTag("story_avatar_${story.name.lowercase()}")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(RoseGradient)
                                        .padding(3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SmartImage(
                                        model = story.imageResName,
                                        contentDescription = story.name,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .border(2.dp, DarkBackground, CircleShape)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = story.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Layout Mode Switcher Glass Pill
                    GlassCard(
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("layout_mode_switcher")
                    ) {
                        Row(
                            modifier = Modifier.padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .then(
                                        if (layoutMode == FeedLayoutMode.FEED) Modifier.background(RoseGradient)
                                        else Modifier.background(Color.Transparent)
                                    )
                                    .clickable { layoutMode = FeedLayoutMode.FEED }
                                    .padding(8.dp)
                                    .testTag("feed_mode_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ViewAgenda,
                                    contentDescription = "Feed",
                                    tint = if (layoutMode == FeedLayoutMode.FEED) DarkBackground else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .then(
                                        if (layoutMode == FeedLayoutMode.GRID) Modifier.background(RoseGradient)
                                        else Modifier.background(Color.Transparent)
                                    )
                                    .clickable { layoutMode = FeedLayoutMode.GRID }
                                    .padding(8.dp)
                                    .testTag("grid_mode_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.GridView,
                                    contentDescription = "Cuadrícula",
                                    tint = if (layoutMode == FeedLayoutMode.GRID) DarkBackground else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Real Feed Posts or Grid View
            if (layoutMode == FeedLayoutMode.FEED) {
                if (posts.isEmpty()) {
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Aún no hay publicaciones compartidas. ¡Toca el icono de la cámara para subir vuestra primera foto!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                } else {
                    items(posts, key = { it.id }) { post ->
                        RealFeedPostCard(
                            post = post,
                            viewModel = viewModel
                        )
                    }
                }
            } else {
                item {
                    // 3x3 Grid
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp)),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val displayCount = maxOf(posts.size, 9)
                        val rows = (displayCount + 2) / 3

                        for (row in 0 until minOf(rows, 3)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (col in 0 until 3) {
                                    val index = row * 3 + col
                                    if (posts.isNotEmpty()) {
                                        val p = posts[index % posts.size]
                                        SmartImage(
                                            model = p.imageUri,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(4.dp))
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .background(DarkSurfaceElevated)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        activeStory?.let { story ->
            StoryViewerDialog(
                story = story,
                onDismiss = { viewModel.closeStory() }
            )
        }

        if (showCreatePostDialog) {
            CreatePostDialog(
                viewModel = viewModel,
                onDismiss = { showCreatePostDialog = false }
            )
        }
    }
}

@Composable
private fun RealFeedPostCard(
    post: PostEntity,
    viewModel: NosotrosViewModel
) {
    val isLiked by viewModel.isPostLikedFlow(post.id).collectAsState(initial = false)
    val likeCount by viewModel.getLikeCountFlow(post.id).collectAsState(initial = 0)
    val comments by viewModel.getCommentsFlow(post.id).collectAsState(initial = emptyList())

    var commentsOpen by remember { mutableStateOf(false) }
    var newCommentText by remember { mutableStateOf("") }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("post_card_${post.id}"),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SmartImage(
                    model = post.authorAvatar,
                    contentDescription = post.authorName,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.dp, RosePrimary, CircleShape)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.authorName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "Ubicación",
                            tint = RosePrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = post.location,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp
                        )
                    }
                }

                Text(
                    text = post.timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            SmartImage(
                model = post.imageUri,
                contentDescription = post.caption,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (!isLiked) viewModel.toggleLike(post.id)
                            }
                        )
                    }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clickable { viewModel.toggleLike(post.id) }
                            .testTag("like_post_${post.id}")
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Me gusta",
                            tint = if (isLiked) RoseAccent else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (likeCount > 0) "$likeCount ❤️" else "0",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clickable { commentsOpen = !commentsOpen }
                            .testTag("toggle_comments_${post.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChatBubbleOutline,
                            contentDescription = "Comentarios",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${comments.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                if (post.caption.isNotBlank()) {
                    Text(
                        text = "${post.authorName} ${post.caption}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }

                AnimatedVisibility(visible = commentsOpen) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        comments.forEach { comment ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = comment.authorName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = comment.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextField(
                                value = newCommentText,
                                onValueChange = { newCommentText = it },
                                placeholder = { Text("Comentario íntimo…", style = MaterialTheme.typography.bodyMedium) },
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
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = {
                                    if (newCommentText.isNotBlank()) {
                                        viewModel.addComment(post.id, newCommentText)
                                        newCommentText = ""
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Enviar comentario",
                                    tint = RosePrimary,
                                    modifier = Modifier.size(18.dp)
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
private fun StoryViewerDialog(
    story: StoryItem,
    onDismiss: () -> Unit
) {
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(story) {
        while (progress < 1f) {
            delay(50)
            progress += 0.015f
        }
        onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = RosePrimary,
                    trackColor = DarkSurfaceElevated
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = story.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cerrar",
                            tint = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                SmartImage(
                    model = story.imageResName,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                )
            }
        }
    }
}
