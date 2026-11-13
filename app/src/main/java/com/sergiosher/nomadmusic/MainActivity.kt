package com.sergiosher.nomadmusic

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.compose.rememberAsyncImagePainter
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.TimeUnit

private const val R2 = "https://pub-bc56dda8007f481cb5889b9da87f41f4.r2.dev"
private val song = MediaItem.Builder()
    .setUri("$R2/VN20261109_075848.m4a")
    .setMediaMetadata(MediaMetadata.Builder()
        .setTitle("ERASER — 8 BIT MUSIC")
        .setArtist("SERGIO SHER NOMAD")
        .setArtworkUri(android.net.Uri.parse("$R2/file_0000000000588210b62e449c0d4413be.png"))
        .build())
    .build()

class MainActivity : ComponentActivity() {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val controller = mutableStateOf<MediaController?>(null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val token = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val future = MediaController.Builder(this, token).buildAsync()
        controllerFuture = future
        future.addListener({
            try { controller.value = future.get() } catch (_: Exception) { controller.value = null }
        }, ContextCompat.getMainExecutor(this))
        setContent { MusicScreen(controller.value) }
    }
    override fun onDestroy() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        super.onDestroy()
    }
}

private fun durationText(ms: Long): String {
    if (ms < 0 || ms == androidx.media3.common.C.TIME_UNSET) return "0:00"
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms)
    return "%d:%02d".format(seconds / 60, seconds % 60)
}

@Composable
private fun MusicScreen(player: MediaController?) {
    var playing by remember { mutableStateOf(false) }
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var error by remember { mutableStateOf<String?>(null) }
    var loaded by remember { mutableStateOf(false) }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) { playing = isPlaying }
            override fun onPlaybackStateChanged(playbackState: Int) {
                duration = player?.duration?.takeIf { it > 0 } ?: 0L
            }
            override fun onPlayerError(e: PlaybackException) { error = e.localizedMessage ?: "Ошибка аудиофайла" }
        }
        player?.addListener(listener)
        if (player != null) {
            loaded = player.mediaItemCount > 0
            playing = player.isPlaying
            duration = player.duration.takeIf { it > 0 } ?: 0L
        }
        onDispose { player?.removeListener(listener) }
    }
    LaunchedEffect(player) {
        while (player != null) {
            position = player.currentPosition.coerceAtLeast(0)
            duration = player.duration.takeIf { it > 0 } ?: 0L
            kotlinx.coroutines.delay(500)
        }
    }
    MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFFFFC857), background = Color(0xFF101018), surface = Color(0xFF20202B))) {
        Scaffold { insets ->
            Column(Modifier.fillMaxSize().padding(insets).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text("SERGIO SHER NOMAD", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Text("MUSIC LIBRARY", style = MaterialTheme.typography.titleMedium)
                Card(onClick = {
                    if (player != null) {
                        error = null
                        if (!loaded || player.mediaItemCount == 0) {
                            player.setMediaItem(song)
                            player.prepare()
                            loaded = true
                        }
                        player.play()
                    }
                }) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Image(rememberAsyncImagePainter("$R2/file_0000000000588210b62e449c0d4413be.png"), contentDescription = "ERASER cover", modifier = Modifier.size(88.dp), contentScale = ContentScale.Crop)
                        Spacer(Modifier.width(12.dp))
                        Column { Text("ERASER — 8 BIT MUSIC", style = MaterialTheme.typography.titleMedium); Text("SERGIO SHER NOMAD", style = MaterialTheme.typography.bodySmall) }
                    }
                }
                Spacer(Modifier.weight(1f))
                Card(shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("СЕЙЧАС ИГРАЕТ", color = MaterialTheme.colorScheme.primary)
                        Text("ERASER — 8 BIT MUSIC", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        Slider(value = position.coerceIn(0L, duration.coerceAtLeast(1L)).toFloat(), onValueChange = { position = it.toLong() }, onValueChangeFinished = { player?.seekTo(position) }, valueRange = 0f..duration.coerceAtLeast(1L).toFloat(), enabled = player != null && duration > 0)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(durationText(position)); Text(durationText(duration)) }
                        Button(enabled = player != null, onClick = {
                            player ?: return@Button
                            error = null
                            if (playing) player.pause() else {
                                if (!loaded || player.mediaItemCount == 0) {
                                    player.setMediaItem(song); player.prepare(); loaded = true
                                }
                                player.play()
                            }
                        }) { Text(if (playing) "⏸ Пауза" else "▶ Воспроизвести") }
                    }
                }
                if (player == null) Text("Подключение к плееру…")
                error?.let { Text("Ошибка воспроизведения: $it", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}
