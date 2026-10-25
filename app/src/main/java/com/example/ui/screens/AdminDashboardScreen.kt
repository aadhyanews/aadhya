package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Article
import com.example.data.model.ReporterUser
import com.example.data.model.VideoNews
import com.example.ui.theme.NewsBreakingRed
import com.example.ui.theme.NewsGold
import com.example.ui.theme.NewsNavy
import com.example.ui.theme.NewsRedPrimary
import com.example.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val districts by viewModel.districts.collectAsStateWithLifecycle()
    val articles by viewModel.allArticles.collectAsStateWithLifecycle()
    val pendingArticles by viewModel.pendingArticles.collectAsStateWithLifecycle()
    val reporters by viewModel.reporters.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Pending, 1: Published Articles, 2: Reporters, 3: Live TV & Video, 4: Analytics
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingArticle by remember { mutableStateOf<Article?>(null) }
    var articleToDelete by remember { mutableStateOf<String?>(null) }

    // Rejection prompt dialog state
    var rejectingArticleId by remember { mutableStateOf<String?>(null) }
    var rejectionReasonInput by remember { mutableStateOf("ఆధారాలు లేవు / మరింత సమాచారం అవసరం") }

    // Live TV form
    var liveUrlInput by remember(appSettings.liveStreamUrl) { mutableStateOf(appSettings.liveStreamUrl) }
    var liveEnabledInput by remember(appSettings.liveStreamEnabled) { mutableStateOf(appSettings.liveStreamEnabled) }

    // Video form
    var videoTitleInput by remember { mutableStateOf("") }
    var videoThumbInput by remember { mutableStateOf("") }
    var videoUrlInput by remember { mutableStateOf("") }
    var videoDurationInput by remember { mutableStateOf("180") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("అడ్మిన్ డాష్‌బోర్డ్", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(NewsGold, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("ADMIN", color = NewsNavy, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "వెనుకకు")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = {
                        editingArticle = null
                        showCreateDialog = true
                    },
                    containerColor = NewsRedPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("create_article_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "కొత్త వార్త")
                }
            }
        },
        modifier = modifier.testTag("admin_dashboard_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Status banner if present
            statusMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = NewsGold.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.clearStatusMessage() }) {
                            Text("సరే")
                        }
                    }
                }
            }

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NewsRedPrimary
                    )
                }
            ) {
                val tabTitles = listOf(
                    "పెండింగ్ ఆమోదాలు (${pendingArticles.size})",
                    "లైవ్ వార్తలు (${articles.size})",
                    "రిపోర్టర్లు (${reporters.size})",
                    "లైవ్ టీవీ & వీడియోలు",
                    "విశ్లేషణ (Analytics)"
                )
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) NewsRedPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // TAB 0: PENDING APPROVALS LIST
                    if (pendingArticles.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color(0xFF2E7D32)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "పెండింగ్ వార్తలు ఏవీ లేవు!",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "రిపోర్టర్లు కొత్త వార్తలు సమర్పించినప్పుడు ఇక్కడ సమీక్షించవచ్చు.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text(
                                    text = "రిపోర్టర్ల నుండి వచ్చిన వార్తలను పరిశీలించి ఆమోదించండి:",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            items(pendingArticles, key = { it.id }) { article ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        // Reporter Info Header
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(NewsNavy.copy(alpha = 0.1f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.People, contentDescription = null, tint = NewsNavy, modifier = Modifier.size(18.dp))
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = article.authorName,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                    Text(
                                                        text = article.reporterPhone,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFFFFF3E0), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "పెండింగ్",
                                                    color = Color(0xFFE65100),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Location Hierarchy & GPS
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = NewsRedPrimary, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${article.state} > ${article.districtId} > ${article.mandal} > ${article.village}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        if (article.gpsCoordinates.isNotBlank()) {
                                            Text(
                                                text = "GPS స్థానం: ${article.gpsCoordinates}",
                                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Headline & Content
                                        Text(
                                            text = article.titleTe,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = article.contentTe,
                                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )

                                        // Media images preview if available
                                        if (article.imageUrl.isNotBlank() || article.mediaUrls.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                val urls = if (article.mediaUrls.isNotEmpty()) article.mediaUrls else listOf(article.imageUrl)
                                                items(urls) { imgUrl ->
                                                    AsyncImage(
                                                        model = ImageRequest.Builder(context).data(imgUrl).crossfade(true).build(),
                                                        contentDescription = null,
                                                        modifier = Modifier
                                                            .size(70.dp)
                                                            .clip(RoundedCornerShape(6.dp)),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = "సమర్పించిన సమయం: ${article.formattedDateTime.ifBlank { "ఇప్పుడే" }}",
                                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))
                                        Divider()
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // ACTION BUTTONS: APPROVE, REJECT, EDIT
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Edit Button
                                            TextButton(
                                                onClick = {
                                                    editingArticle = article
                                                    showCreateDialog = true
                                                }
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("సవరించు")
                                            }

                                            Spacer(modifier = Modifier.width(6.dp))

                                            // Reject Button
                                            OutlinedButton(
                                                onClick = {
                                                    rejectingArticleId = article.id
                                                },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NewsRedPrimary)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("తిరస్కరించు")
                                            }

                                            Spacer(modifier = Modifier.width(6.dp))

                                            // Approve Button
                                            Button(
                                                onClick = {
                                                    viewModel.approveArticle(article.id)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("ఆమోదించు (Publish)")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: ALL LIVE ARTICLES LIST
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ప్రచురించిన మరియు ఆమోదించబడిన వార్తలు",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                OutlinedButton(onClick = { viewModel.seedInitialDatabase() }) {
                                    Text("ప్రారంభ డేటా నింపు")
                                }
                            }
                        }

                        items(articles, key = { it.id }) { art ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (art.status == "published" || art.status == "approved") Color(0xFF2E7D32) else Color.Gray,
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (art.status == "published" || art.status == "approved") "లైవ్ ప్రచురితం" else art.status,
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Row {
                                            if (art.isBreaking) {
                                                Text("• బ్రేకింగ్ ", color = NewsBreakingRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                            if (art.isFeatured) {
                                                Text("• ఫీచర్డ్", color = NewsGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = art.titleTe,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "రిపోర్టర్: ${art.authorName} • వీక్షణలు: ${art.viewCount}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(onClick = {
                                            editingArticle = art
                                            showCreateDialog = true
                                        }) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("సవరించు")
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        TextButton(
                                            onClick = { articleToDelete = art.id },
                                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("తొలగించు")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: REPORTERS MANAGEMENT
                    if (reporters.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "నమోదైన రిపోర్టర్లు ఇంకా లేరు",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "రిపోర్టర్ పోర్టల్ ద్వారా లాగిన్ అయిన రిపోర్టర్లు ఇక్కడ జాబితా చేయబడతారు.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Text(
                                    text = "రిపోర్టర్ల అనుమతులు మరియు నిర్వహణ (Enable/Disable Access):",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            items(reporters, key = { it.id }) { reporter ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = reporter.name,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            if (reporter.status == "active") Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                                            RoundedCornerShape(4.dp)
                                                        )
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = if (reporter.status == "active") "యాక్టివ్" else "నిలిపివేయబడింది",
                                                        color = if (reporter.status == "active") Color(0xFF2E7D32) else NewsRedPrimary,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "ఫోన్: ${reporter.phone} • కేటాయింపు: ${reporter.assignedDistrict}",
                                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            )
                                            Text(
                                                text = "రిజిస్ట్రేషన్: ${reporter.registeredDate.ifBlank { "ఇటీవల" }}",
                                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                                            )
                                        }

                                        // Toggle Reporter Access Switch
                                        Switch(
                                            checked = reporter.status == "active",
                                            onCheckedChange = { active ->
                                                viewModel.toggleReporterAccess(reporter.id, active)
                                            },
                                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF2E7D32))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: LIVE TV & VIDEO MANAGEMENT
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "లైవ్ టీవీ స్ట్రీమ్ కాన్ఫిగరేషన్",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NewsNavy)
                        )

                        OutlinedTextField(
                            value = liveUrlInput,
                            onValueChange = { liveUrlInput = it },
                            label = { Text("లైవ్ స్ట్రీమ్ URL (.m3u8 లేదా .mp4)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = liveEnabledInput, onCheckedChange = { liveEnabledInput = it })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("లైవ్ ప్రసారాన్ని యాప్‌లో ప్రత్యక్షంగా ప్రారంభించు (Enable Live)")
                        }

                        Button(
                            onClick = {
                                viewModel.updateLiveStreamSettings(liveUrlInput, liveEnabledInput)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NewsRedPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("సెట్టింగ్స్ సేవ్ చేయండి")
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "కొత్త వీడియో వార్తను జోడించండి",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NewsNavy)
                        )

                        OutlinedTextField(
                            value = videoTitleInput,
                            onValueChange = { videoTitleInput = it },
                            label = { Text("తెలుగు వీడియో శీర్షిక *") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = videoThumbInput,
                            onValueChange = { videoThumbInput = it },
                            label = { Text("థంబ్‌నెయిల్ ఇమేజ్ URL *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = videoUrlInput,
                            onValueChange = { videoUrlInput = it },
                            label = { Text("వీడియో ఫైల్ URL (.mp4 / .m3u8) *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                if (videoTitleInput.isNotBlank() && videoUrlInput.isNotBlank()) {
                                    val vid = VideoNews(
                                        titleTe = videoTitleInput,
                                        thumbnailUrl = videoThumbInput.ifBlank { "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=800" },
                                        videoUrl = videoUrlInput,
                                        durationSeconds = videoDurationInput.toLongOrNull() ?: 180L,
                                        isPublished = true
                                    )
                                    viewModel.createVideo(vid) {
                                        videoTitleInput = ""
                                        videoThumbInput = ""
                                        videoUrlInput = ""
                                    }
                                }
                            },
                            enabled = !isSubmitting,
                            colors = ButtonDefaults.buttonColors(containerColor = NewsNavy),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("వీడియోను ప్రచురించు")
                        }
                    }
                }

                4 -> {
                    // TAB 4: ANALYTICS
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "వార్తా పోర్టల్ సమగ్ర గణాంకాలు",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NewsNavy)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AnalyticsCard(title = "లైవ్ వార్తలు", value = articles.size.toString(), modifier = Modifier.weight(1f))
                            AnalyticsCard(title = "పెండింగ్ వార్తలు", value = pendingArticles.size.toString(), modifier = Modifier.weight(1f))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AnalyticsCard(title = "రిపోర్టర్లు", value = reporters.size.toString(), modifier = Modifier.weight(1f))
                            AnalyticsCard(title = "మొత్తం వీక్షణలు", value = articles.sumOf { it.viewCount }.toString(), modifier = Modifier.weight(1f))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AnalyticsCard(title = "వర్గాలు", value = categories.size.toString(), modifier = Modifier.weight(1f))
                            AnalyticsCard(title = "జిల్లాలు", value = districts.size.toString(), modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    // REJECTION REASON PROMPT DIALOG
    rejectingArticleId?.let { artId ->
        AlertDialog(
            onDismissRequest = { rejectingArticleId = null },
            title = { Text("వార్తను తిరస్కరించండి", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("రిపోర్టర్‌కు తెలియజేయడానికి తిరస్కరణ కారణాన్ని పేర్కొనండి:")
                    OutlinedTextField(
                        value = rejectionReasonInput,
                        onValueChange = { rejectionReasonInput = it },
                        label = { Text("కారణం (Rejection Reason)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rejectArticle(artId, rejectionReasonInput) {
                            rejectingArticleId = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("తిరస్కరించు (Confirm Reject)")
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectingArticleId = null }) {
                    Text("రద్దు")
                }
            }
        )
    }

    // Article Create/Edit Dialog
    if (showCreateDialog) {
        ArticleEditorDialog(
            initialArticle = editingArticle,
            categories = categories,
            districts = districts,
            onDismiss = { showCreateDialog = false },
            onSave = { articleToSave, isNew ->
                viewModel.saveArticle(articleToSave, isNew) {
                    showCreateDialog = false
                }
            }
        )
    }

    // Delete confirmation dialog
    articleToDelete?.let { artId ->
        AlertDialog(
            onDismissRequest = { articleToDelete = null },
            title = { Text("వార్తను తొలగించాలా?", fontWeight = FontWeight.Bold) },
            text = { Text("ఈ వార్తను శాశ్వతంగా తొలగించడం జరుగుతుంది. నిర్ధారించండి.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteArticle(artId)
                        articleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("తొలగించు")
                }
            },
            dismissButton = {
                TextButton(onClick = { articleToDelete = null }) {
                    Text("రద్దు")
                }
            }
        )
    }
}

@Composable
fun AnalyticsCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = NewsRedPrimary
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleEditorDialog(
    initialArticle: Article?,
    categories: List<com.example.data.model.Category>,
    districts: List<com.example.data.model.District>,
    onDismiss: () -> Unit,
    onSave: (Article, Boolean) -> Unit
) {
    var titleInput by remember { mutableStateOf(initialArticle?.titleTe ?: "") }
    var contentInput by remember { mutableStateOf(initialArticle?.contentTe ?: "") }
    var imageUrlInput by remember { mutableStateOf(initialArticle?.imageUrl ?: "") }
    var captionInput by remember { mutableStateOf(initialArticle?.imageCaption ?: "") }
    var authorInput by remember { mutableStateOf(initialArticle?.authorName ?: "ఆధ్య ప్రతినిధి") }
    var categoryIdInput by remember { mutableStateOf(initialArticle?.categoryId ?: "latest") }
    var districtIdInput by remember { mutableStateOf(initialArticle?.districtId ?: "") }
    var stateInput by remember { mutableStateOf(initialArticle?.state ?: "తెలంగాణ") }
    var mandalInput by remember { mutableStateOf(initialArticle?.mandal ?: "") }
    var villageInput by remember { mutableStateOf(initialArticle?.village ?: "") }
    var isBreakingInput by remember { mutableStateOf(initialArticle?.isBreaking ?: false) }
    var isFeaturedInput by remember { mutableStateOf(initialArticle?.isFeatured ?: false) }
    var statusInput by remember { mutableStateOf(if (initialArticle?.status == "pending") "approved" else (initialArticle?.status ?: "published")) }
    var videoUrlInput by remember { mutableStateOf(initialArticle?.videoUrl ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialArticle == null) "కొత్త వార్తను రాయండి" else "వార్తను సవరించండి",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("తెలుగు శీర్షిక (Headline) *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = contentInput,
                    onValueChange = { contentInput = it },
                    label = { Text("పూర్తి వార్తా వివరాలు (Article Body) *") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = imageUrlInput,
                    onValueChange = { imageUrlInput = it },
                    label = { Text("కవర్ ఫోటో URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = authorInput,
                    onValueChange = { authorInput = it },
                    label = { Text("రిపోర్టర్ పేరు") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("వర్గం: ", fontWeight = FontWeight.Bold)
                    Text(categories.find { it.id == categoryIdInput }?.nameTe ?: categoryIdInput)
                }

                // Checkboxes
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isBreakingInput, onCheckedChange = { isBreakingInput = it })
                    Text("బ్రేకింగ్ న్యూస్")
                    Spacer(modifier = Modifier.width(16.dp))
                    Checkbox(checked = isFeaturedInput, onCheckedChange = { isFeaturedInput = it })
                    Text("ప్రధాన వార్త")
                }

                // Status selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = statusInput == "published" || statusInput == "approved",
                        onCheckedChange = { statusInput = if (it) "approved" else "draft" }
                    )
                    Text("లైవ్ ఫీడ్‌లో ప్రచురించు (Publish)")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titleInput.isNotBlank() && contentInput.isNotBlank()) {
                        val art = Article(
                            id = initialArticle?.id ?: "",
                            titleTe = titleInput,
                            contentTe = contentInput,
                            imageUrl = imageUrlInput.ifBlank { initialArticle?.imageUrl ?: "https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=800" },
                            mediaUrls = initialArticle?.mediaUrls ?: emptyList(),
                            imageCaption = captionInput,
                            authorName = authorInput,
                            categoryId = categoryIdInput,
                            state = stateInput,
                            districtId = districtIdInput,
                            mandal = mandalInput,
                            village = villageInput,
                            gpsCoordinates = initialArticle?.gpsCoordinates ?: "",
                            reporterPhone = initialArticle?.reporterPhone ?: "",
                            status = statusInput,
                            isBreaking = isBreakingInput,
                            isFeatured = isFeaturedInput,
                            videoUrl = videoUrlInput,
                            viewCount = initialArticle?.viewCount ?: 0L,
                            formattedDateTime = initialArticle?.formattedDateTime ?: ""
                        )
                        onSave(art, initialArticle == null)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NewsRedPrimary)
            ) {
                Text("భద్రపరచు (Save & Publish)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("రద్దు")
            }
        }
    )
}
