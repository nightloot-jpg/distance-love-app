package com.example.distancelove.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.distancelove.R
import java.io.File

@Composable
fun SmartImage(
    model: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    if (model.startsWith("/") || model.startsWith("file://") || model.startsWith("content://") || model.startsWith("http")) {
        AsyncImage(
            model = if (model.startsWith("/")) File(model) else model,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        val resId = when (model) {
            "feed1" -> R.drawable.feed1
            "feed2" -> R.drawable.feed2
            "feed3" -> R.drawable.feed3
            else -> R.drawable.feed2
        }
        Image(
            painter = painterResource(id = resId),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}
