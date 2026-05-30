package com.lichso.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.lichso.app.data.remote.ScreenBackgroundRepository

/**
 * Composable helper that draws a server-provided background image behind [content].
 *
 * - If the server has set an image for [screenKey], it is fetched via Coil (which
 *   caches it on disk automatically — Coil uses OkHttp's cache layer).
 * - If [screenKey] has no image configured OR the network is unavailable and no
 *   cached image exists, the content is rendered on the default app background
 *   (no image drawn).
 *
 * Usage:
 * ```
 * ScreenWithBackground(screenKey = "home", repository = screenBgRepo) {
 *     // Your existing screen content here
 * }
 * ```
 */
@Composable
fun ScreenWithBackground(
    screenKey: String,
    repository: ScreenBackgroundRepository,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val backgrounds by repository.backgrounds.collectAsState()
    val imageUrl = backgrounds[screenKey]

    Box(modifier = modifier.fillMaxSize()) {
        // Draw background image if available
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.35f, // subtle overlay: 35% opacity so text remains readable
            )
        }
        // Screen content on top
        content()
    }
}

/**
 * Returns the current background URL for [screenKey], or null if not configured.
 * Useful for cases where you want to manually access the URL rather than
 * using the [ScreenWithBackground] wrapper.
 */
@Composable
fun rememberScreenBackgroundUrl(
    screenKey: String,
    repository: ScreenBackgroundRepository,
): String? {
    val backgrounds by repository.backgrounds.collectAsState()
    return backgrounds[screenKey]
}
