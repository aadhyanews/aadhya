package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Article
import com.example.data.model.VideoNews
import com.example.ui.components.ArticleCard
import com.example.ui.components.BreakingNewsTicker
import com.example.ui.components.CategoryFilterChipRow
import com.example.ui.components.FeaturedCarousel
import com.example.ui.components.LatestVideosSection
import com.example.ui.components.LiveTvHomeCard
import com.example.ui.components.TopNewsAppBar
import com.example.ui.theme.NewsGold
import com.example.ui.theme.NewsNavy
import com.example.ui.theme.NewsRedPrimary
import com.example.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    isAdminUser: Boolean,
    onArticleClick: (Article) -> Unit,
    onVideoClick: (VideoNews) -> Unit,
    onSearchClick: () -> Unit,
    onLiveTvClick: () -> Unit,
    onVideosClick: () -> Unit,
    onAdminClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val districts by viewModel.districts.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedDistrict by viewModel.selectedDistrict.collectAsStateWithLifecycle()
    val breakingArticles by viewModel.breakingNews.collectAsStateWithLifecycle()
    val featuredArticles by viewModel.featuredNews.collectAsStateWithLifecycle()
    val articles by viewModel.articles.collectAsStateWithLifecycle()
    val videos by viewModel.videos.collectAsStateWithLifecycle()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    // Trending articles sorted by viewCount
    val trendingArticles = articles.filter { it.viewCount > 0 }.sortedByDescending { it.viewCount }.take(3)

    Scaffold(
        topBar = {
            Column {
                TopNewsAppBar(
                    onSearchClick = onSearchClick,
                    onLiveTvClick = onLiveTvClick,
                    onAdminClick = onAdminClick,
                    isAdminUser = isAdminUser
                )
                BreakingNewsTicker(
                    breakingArticles = breakingArticles,
                    onArticleClick = onArticleClick
                )
            }
        },
        modifier = modifier.testTag("home_screen")
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Category Filter Chips
                item {
                    CategoryFilterChipRow(
                        categories = categories,
                        selectedCategoryId = selectedCategory,
                        onCategorySelected = { viewModel.selectCategory(it) }
                    )
                }

                // District Quick Filter Bar
                item {
                    DistrictFilterRow(
                        districts = districts,
                        selectedDistrict = selectedDistrict,
                        onDistrictSelected = { viewModel.selectDistrict(it) }
                    )
                }

                // Live TV Banner Card
                item {
                    LiveTvHomeCard(onWatchLiveClick = onLiveTvClick)
                }

                // Featured Carousel (shown when on latest category or all)
                if (selectedCategory == "latest" && featuredArticles.isNotEmpty()) {
                    item {
                        FeaturedCarousel(
                            featuredArticles = featuredArticles,
                            onArticleClick = onArticleClick
                        )
                    }
                }

                // Trending News Section
                if (trendingArticles.isNotEmpty() && selectedCategory == "latest") {
                    item {
                        TrendingSectionHeader()
                    }
                    items(trendingArticles, key = { "trend_${it.id}" }) { article ->
                        ArticleCard(
                            article = article,
                            isBookmarked = bookmarkedIds.contains(article.id),
                            onArticleClick = { onArticleClick(article) },
                            onBookmarkToggle = { viewModel.toggleBookmark(article) },
                            onShareClick = { shareArticle(context, article) }
                        )
                    }
                }

                // Latest Video News Section
                if (videos.isNotEmpty()) {
                    item {
                        LatestVideosSection(
                            videos = videos,
                            onVideoClick = onVideoClick,
                            onViewAllClick = onVideosClick
                        )
                    }
                }

                // Latest News Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(18.dp)
                                    .background(NewsRedPrimary, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedDistrict.isNotBlank()) "జిల్లా వార్తలు" else "తాజా వార్తలు",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = NewsRedPrimary
                            )
                        }
                    }
                }

                // Empty State
                if (articles.isEmpty() && !isRefreshing) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "ప్రస్తుతానికి వార్తలు అందుబాటులో లేవు",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { viewModel.refresh() }
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("మళ్లీ ప్రయత్నించండి")
                            }
                        }
                    }
                }

                // Articles Feed
                items(articles, key = { it.id }) { article ->
                    ArticleCard(
                        article = article,
                        isBookmarked = bookmarkedIds.contains(article.id),
                        onArticleClick = { onArticleClick(article) },
                        onBookmarkToggle = { viewModel.toggleBookmark(article) },
                        onShareClick = { shareArticle(context, article) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun DistrictFilterRow(
    districts: List<com.example.data.model.District>,
    selectedDistrict: String,
    onDistrictSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "జిల్లాలు",
            tint = NewsRedPrimary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "జిల్లాలు:",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        districts.forEach { dist ->
            val isSelected = dist.id == selectedDistrict
            FilterChip(
                selected = isSelected,
                onClick = { onDistrictSelected(dist.id) },
                label = {
                    Text(
                        text = dist.nameTe,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NewsNavy,
                    selectedLabelColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun TrendingSectionHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.TrendingUp,
            contentDescription = null,
            tint = NewsGold,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "ట్రెండింగ్ వార్తలు",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = NewsNavy
            )
        )
    }
}

fun shareArticle(context: Context, article: Article) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_SUBJECT,
            "ఆధ్య న్యూస్: ${article.titleTe}"
        )
        putExtra(
            Intent.EXTRA_TEXT,
            "${article.titleTe}\n\n${article.contentTe.take(200)}...\n\nమరిన్ని వార్తలకు ఆధ్య న్యూస్ యాప్ చూడండి: https://aadhyanews.com/article/${article.id}"
        )
    }
    context.startActivity(Intent.createChooser(shareIntent, "వార్తను షేర్ చేయండి"))
}
