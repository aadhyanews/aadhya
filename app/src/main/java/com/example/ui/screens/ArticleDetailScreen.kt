package com.example.ui.screens

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.Article
import com.example.ui.components.ArticleCard
import com.example.ui.theme.NewsNavy
import com.example.ui.theme.NewsRedPrimary
import com.example.ui.viewmodel.ArticleDetailViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    viewModel: ArticleDetailViewModel,
    onBackClick: () -> Unit,
    onRelatedArticleClick: (Article) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val article by viewModel.article.collectAsStateWithLifecycle()
    val isBookmarked by viewModel.isBookmarked.collectAsStateWithLifecycle()
    val fontScale by viewModel.fontSizeScale.collectAsStateWithLifecycle()
    val relatedArticles by viewModel.relatedArticles.collectAsStateWithLifecycle()

    BackHandler { onBackClick() }

    // Text to Speech
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isSpeaking by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("te", "IN")
            }
        }
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "వార్తా వివరాలు",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "వెనుకకు"
                        )
                    }
                },
                actions = {
                    article?.let { art ->
                        IconButton(onClick = { viewModel.toggleBookmark() }) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (isBookmarked) "సేవ్ చేసినది తొలగించు" else "సేవ్ చేయండి",
                                tint = if (isBookmarked) NewsRedPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { shareArticle(context, art) }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "షేర్ చేయండి"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.testTag("article_detail_screen")
    ) { paddingValues ->
        if (article == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NewsRedPrimary)
            }
        } else {
            val art = article!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Headline
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = art.titleTe,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = (22 * fontScale).sp,
                                lineHeight = (30 * fontScale).sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = art.authorName,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = NewsRedPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "ఆధ్య న్యూస్ నెట్‌వర్క్",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }

                            // Text resizing & TTS audio tools
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // TTS button
                                OutlinedIconButton(
                                    onClick = {
                                        if (isSpeaking) {
                                            tts?.stop()
                                            isSpeaking = false
                                        } else {
                                            val textToRead = "${art.titleTe}. ${art.contentTe}"
                                            tts?.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, "article_read")
                                            isSpeaking = true
                                        }
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                                        contentDescription = "వినండి",
                                        tint = if (isSpeaking) NewsRedPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Font size controls
                                OutlinedIconButton(
                                    onClick = { viewModel.decreaseFontSize() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Text("A-", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                OutlinedIconButton(
                                    onClick = { viewModel.increaseFontSize() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Text("A+", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Cover Image
                item {
                    if (art.imageUrl.isNotBlank()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            AsyncImage(
                                model = art.imageUrl,
                                contentDescription = art.titleTe,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(230.dp),
                                contentScale = ContentScale.Crop
                            )
                            if (art.imageCaption.isNotBlank()) {
                                Text(
                                    text = art.imageCaption,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Full Content
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                        Text(
                            text = art.contentTe,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = (17 * fontScale).sp,
                                lineHeight = (28 * fontScale).sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Divider()

                        Spacer(modifier = Modifier.height(16.dp))

                        // Share Banner Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "ఈ వార్త మీకు నచ్చిందా?",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "మీ మిత్రులతో వాట్సాప్‌లో పంచుకోండి",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                                FilledTonalButton(
                                    onClick = { shareArticle(context, art) }
                                ) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("షేర్")
                                }
                            }
                        }
                    }
                }

                // Related Articles
                if (relatedArticles.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Text(
                                text = "సంబంధిత వార్తలు",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NewsRedPrimary
                                )
                            )
                        }
                    }

                    items(relatedArticles, key = { "rel_${it.id}" }) { rel ->
                        ArticleCard(
                            article = rel,
                            isBookmarked = false,
                            onArticleClick = { onRelatedArticleClick(rel) },
                            onBookmarkToggle = { },
                            onShareClick = { shareArticle(context, rel) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}
