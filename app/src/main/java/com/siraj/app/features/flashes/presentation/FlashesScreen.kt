@file:kotlin.OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.media3.common.util.UnstableApi::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)

package com.siraj.app.features.flashes.presentation

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.siraj.app.core.ui.components.ErrorScreen
import com.siraj.app.domain.models.flash.Flash
import com.siraj.app.domain.models.flash.FlashPublishingState
import kotlinx.coroutines.delay
import com.siraj.app.features.community.presentation.InteractionViewModel
import com.siraj.app.features.community.presentation.SafetyViewModel
import com.siraj.app.features.community.presentation.ReportDialog
import com.siraj.app.domain.models.community.ReportTargetType
import androidx.compose.material3.ExperimentalMaterial3Api

@Composable
fun FlashesScreen(
    viewModel: FlashesViewModel,
    interactionViewModel: InteractionViewModel,
    safetyViewModel: SafetyViewModel,
    currentUserId: String,
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState(pageCount = { state.flashes.size })

    // Pagination
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage >= state.flashes.size - 2) {
            viewModel.loadFlashes()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (state.isLoading && state.flashes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        } else if (state.error != null && state.flashes.isEmpty()) {
            ErrorScreen(
                message = state.error ?: "خطأ",
                onRetry = { viewModel.loadFlashes(isRefresh = true) }
            )
        } else if (state.flashes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد ومضات حالياً", color = Color.White)
            }
        } else {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                key = { state.flashes[it].id }
            ) { page ->
                val flash = state.flashes[page]
                val isVisible = pagerState.currentPage == page

                var showReportDialog by remember { mutableStateOf(false) }

                FlashFeedItem(
                    flash = flash,
                    isVisible = isVisible,
                    isGlobalMuted = state.isMuted,
                    onToggleMute = { viewModel.toggleMute() },
                    onLike = { interactionViewModel.toggleLike(currentUserId, flash.id) { } },
                    onSave = { interactionViewModel.toggleSave(currentUserId, flash.id) { } },
                    onShare = { /* handle share */ },
                    onFollow = { interactionViewModel.toggleFollow(currentUserId, flash.creatorId) { } },
                    onReport = { showReportDialog = true },
                    onBlock = { interactionViewModel.blockUser(currentUserId, flash.creatorId) },
                    onHide = { interactionViewModel.hideContent(currentUserId, flash.id) },
                    onOpenDetails = { onNavigateToDetails(flash.id) }
                )
                
                ReportDialog(
                    showDialog = showReportDialog,
                    onDismiss = { showReportDialog = false },
                    onSubmit = { type, desc -> 
                        safetyViewModel.submitReport(
                            reporterId = currentUserId,
                            targetType = ReportTargetType.FLASH,
                            targetId = flash.id,
                            targetOwnerId = flash.creatorId,
                            reportType = type,
                            description = desc
                        )
                    }
                )
                
                LaunchedEffect(isVisible) {
                    if (isVisible) {
                        // Debounce view logging to prevent fake views when fast scrolling
                        delay(2000)
                        viewModel.logView(flash.id)
                    }
                }
            }
        }

        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                "الومضات",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { viewModel.toggleMute() }) {
                Icon(
                    imageVector = if (state.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = "Mute Toggle",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun FlashFeedItem(
    flash: Flash,
    isVisible: Boolean,
    isGlobalMuted: Boolean,
    onToggleMute: () -> Unit,
    onLike: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onFollow: () -> Unit,
    onReport: () -> Unit,
    onBlock: () -> Unit,
    onHide: () -> Unit,
    onOpenDetails: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var showPlayIcon by remember { mutableStateOf(false) }

    // Initialize player
    DisposableEffect(context, flash.videoUrl) {
        val player = ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(flash.videoUrl)))
            repeatMode = Player.REPEAT_MODE_ONE
            prepare()
        }
        exoPlayer = player

        onDispose {
            player.release()
            exoPlayer = null
        }
    }

    // Playback state based on visibility
    LaunchedEffect(isVisible, exoPlayer) {
        if (isVisible) {
            exoPlayer?.play()
            isPlaying = true
            showPlayIcon = false
        } else {
            exoPlayer?.pause()
            exoPlayer?.seekTo(0)
            isPlaying = false
        }
    }

    // Mute state
    LaunchedEffect(isGlobalMuted, exoPlayer) {
        exoPlayer?.volume = if (isGlobalMuted) 0f else 1f
    }

    // Lifecycle
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                exoPlayer?.pause()
                isPlaying = false
            } else if (event == Lifecycle.Event.ON_RESUME && isVisible) {
                exoPlayer?.play()
                isPlaying = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Video
        exoPlayer?.let { player ->
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = player
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier
                    .matchParentSize()
                    .clickable {
                        if (isPlaying) {
                            player.pause()
                            isPlaying = false
                            showPlayIcon = true
                        } else {
                            player.play()
                            isPlaying = true
                            showPlayIcon = false
                        }
                    }
            )
        }

        // Thumbnail before playing
        if (!isPlaying && exoPlayer?.playbackState != Player.STATE_READY && flash.thumbnailUrl != null) {
            AsyncImage(
                model = flash.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }

        // Play Icon Overlay
        AnimatedVisibility(
            visible = showPlayIcon,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(72.dp)
            )
        }

        // UI Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                        startY = 500f
                    )
                )
        )

        // Bottom Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.8f)
                .padding(16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Trust Card
            if (flash.sourceInfo != null && flash.publishingState == FlashPublishingState.APPROVED) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f))
                        .clickable { onOpenDetails() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.VerifiedUser,
                        contentDescription = "Verified",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "موثق: ${flash.sourceInfo.title}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = "@${flash.creatorName}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = flash.title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = flash.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Right Actions Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Image (Mock)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { /* open profile */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = "Creator", tint = Color.White)
            }

            ActionItem(
                icon = if (flash.isLikedByMe) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                tint = if (flash.isLikedByMe) Color.Red else Color.White,
                label = flash.metrics.likes.toString(),
                onClick = onLike
            )

            ActionItem(
                icon = if (flash.isSavedByMe) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                tint = if (flash.isSavedByMe) MaterialTheme.colorScheme.primary else Color.White,
                label = flash.metrics.saves.toString(),
                onClick = onSave
            )

            ActionItem(
                icon = Icons.Default.Share,
                tint = Color.White,
                label = "مشاركة",
                onClick = onShare
            )
            
            var showOptions by remember { mutableStateOf(false) }

            ActionItem(
                icon = Icons.Default.MoreVert,
                tint = Color.White,
                label = "",
                onClick = { showOptions = true }
            )

            if (showOptions) {
                ModalBottomSheet(
                    onDismissRequest = { showOptions = false }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.options), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        
                        TextButton(onClick = { showOptions = false; onFollow() }, modifier = Modifier.fillMaxWidth()) {
                            Text("متابعة @${flash.creatorName}")
                        }
                        
                        TextButton(onClick = { showOptions = false; onHide() }, modifier = Modifier.fillMaxWidth()) {
                            Text("إخفاء هذا المحتوى")
                        }
                        
                        TextButton(onClick = { showOptions = false; onReport() }, modifier = Modifier.fillMaxWidth()) {
                            Text("إبلاغ عن المحتوى", color = MaterialTheme.colorScheme.error)
                        }
                        
                        TextButton(onClick = { showOptions = false; onBlock() }, modifier = Modifier.fillMaxWidth()) {
                            Text("حظر المستخدم", color = MaterialTheme.colorScheme.error)
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(32.dp)
        )
        if (label.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White
            )
        }
    }
}
