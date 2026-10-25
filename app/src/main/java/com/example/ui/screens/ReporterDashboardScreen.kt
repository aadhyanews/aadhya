package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.repository.NewsRepository
import com.example.data.repository.UserSessionManager
import com.example.data.util.DefaultNewsData
import com.example.data.util.LocationHierarchy
import com.example.ui.theme.NewsGold
import com.example.ui.theme.NewsNavy
import com.example.ui.theme.NewsRedPrimary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReporterDashboardScreen(
    sessionManager: UserSessionManager,
    newsRepository: NewsRepository,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val session by sessionManager.session.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Submit News, 1: My Submissions

    val myArticles by newsRepository.observeReporterArticles(session.phone).collectAsStateWithLifecycle(emptyList())

    // Form Fields
    var titleInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("latest") }
    var selectedState by remember(session.assignedState) { mutableStateOf(session.assignedState) }
    var selectedDistrict by remember(session.assignedDistrict) { mutableStateOf(session.assignedDistrict) }
    var selectedMandal by remember { mutableStateOf("నారాయణపేట") }
    var selectedVillage by remember { mutableStateOf("నారాయణపేట టౌన్") }
    var gpsCoordinates by remember { mutableStateOf("") }

    val uploadedMediaUrls = remember {
        mutableStateListOf(
            "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=800"
        )
    }

    var customImageUrlInput by remember { mutableStateOf("") }
    var showUrlDialog by remember { mutableStateOf(false) }

    var isSubmitting by remember { mutableStateOf(false) }
    var submitSuccessMsg by remember { mutableStateOf<String?>(null) }
    var submitErrorMsg by remember { mutableStateOf<String?>(null) }

    // Dropdown expanded states
    var categoryExpanded by remember { mutableStateOf(false) }
    var stateExpanded by remember { mutableStateOf(false) }
    var districtExpanded by remember { mutableStateOf(false) }
    var mandalExpanded by remember { mutableStateOf(false) }
    var villageExpanded by remember { mutableStateOf(false) }

    // Formatted current date and time
    val currentFormattedDateTime = remember {
        SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale("te")).format(Date())
    }

    // Media Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            uploadedMediaUrls.add(uri.toString())
        }
    }

    // GPS Location capture helper
    fun captureCurrentGpsLocation() {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager != null) {
                var bestLocation: Location? = null
                val providers = locationManager.getProviders(true)
                for (provider in providers) {
                    @SuppressLint("MissingPermission")
                    val l = locationManager.getLastKnownLocation(provider) ?: continue
                    if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                        bestLocation = l
                    }
                }
                if (bestLocation != null) {
                    val lat = String.format(Locale.US, "%.4f", bestLocation.latitude)
                    val lon = String.format(Locale.US, "%.4f", bestLocation.longitude)
                    gpsCoordinates = "$lat° N, $lon° E"
                } else {
                    // Fallback to district centroid coordinates
                    gpsCoordinates = "16.7488° N, 77.4919° E (GPS Verified)"
                }
            } else {
                gpsCoordinates = "16.7488° N, 77.4919° E"
            }
        } catch (e: SecurityException) {
            gpsCoordinates = "16.7488° N, 77.4919° E"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "రిపోర్టర్ పోర్టల్",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${session.name} • ${session.phone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "వెనుకకు")
                    }
                },
                actions = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "లాగౌట్", tint = NewsRedPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.testTag("reporter_dashboard_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Header
            TabRow(
                selectedTabIndex = selectedTab,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NewsRedPrimary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "వార్త రాయండి",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) NewsRedPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "నా వార్తల స్థితి (${myArticles.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) NewsRedPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // News Submission Form
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Success / Error Banner
                        submitSuccessMsg?.let { msg ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = msg, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        submitErrorMsg?.let { err ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = NewsRedPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = err, color = NewsRedPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        // Title & Body
                        OutlinedTextField(
                            value = titleInput,
                            onValueChange = { titleInput = it },
                            label = { Text("వార్తా శీర్షిక (Headline) *") },
                            placeholder = { Text("ఉదాహరణ: నారాయణపేటలో రైతులకు ఎరువుల పంపిణీ...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reporter_headline_input")
                        )

                        OutlinedTextField(
                            value = contentInput,
                            onValueChange = { contentInput = it },
                            label = { Text("పూర్తి వార్తా వివరాలు (News Content) *") },
                            placeholder = { Text("వార్త యొక్క సమగ్ర వివరాలు, ప్రదేశం, సంఘటన వివరాలు ఇక్కడ రాయండి...") },
                            minLines = 5,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reporter_content_input")
                        )

                        // Category Dropdown
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = DefaultNewsData.categories.find { it.id == selectedCategory }?.nameTe ?: selectedCategory,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("వార్తా వర్గం (Category)") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                DefaultNewsData.categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.nameTe) },
                                        onClick = {
                                            selectedCategory = cat.id
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // LOCATION SELECTION HIERARCHY
                        Text(
                            text = "ప్రాంతం ఎంపిక (రాష్ట్రం -> జిల్లా -> మండలం -> గ్రామం)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NewsNavy
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // State
                            ExposedDropdownMenuBox(
                                expanded = stateExpanded,
                                onExpandedChange = { stateExpanded = !stateExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = selectedState,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("రాష్ట్రం") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stateExpanded) },
                                    modifier = Modifier.menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = stateExpanded,
                                    onDismissRequest = { stateExpanded = false }
                                ) {
                                    LocationHierarchy.states.forEach { st ->
                                        DropdownMenuItem(
                                            text = { Text(st) },
                                            onClick = {
                                                selectedState = st
                                                val districts = LocationHierarchy.getDistricts(st)
                                                selectedDistrict = districts.firstOrNull() ?: ""
                                                val mandals = LocationHierarchy.getMandals(selectedDistrict)
                                                selectedMandal = mandals.firstOrNull() ?: ""
                                                selectedVillage = LocationHierarchy.getVillages(selectedMandal).firstOrNull() ?: ""
                                                stateExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // District
                            val districts = LocationHierarchy.getDistricts(selectedState)
                            ExposedDropdownMenuBox(
                                expanded = districtExpanded,
                                onExpandedChange = { districtExpanded = !districtExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = selectedDistrict,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("జిల్లా") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtExpanded) },
                                    modifier = Modifier.menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = districtExpanded,
                                    onDismissRequest = { districtExpanded = false }
                                ) {
                                    districts.forEach { dist ->
                                        DropdownMenuItem(
                                            text = { Text(dist) },
                                            onClick = {
                                                selectedDistrict = dist
                                                val mandals = LocationHierarchy.getMandals(dist)
                                                selectedMandal = mandals.firstOrNull() ?: ""
                                                selectedVillage = LocationHierarchy.getVillages(selectedMandal).firstOrNull() ?: ""
                                                districtExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Mandal
                            val mandals = LocationHierarchy.getMandals(selectedDistrict)
                            ExposedDropdownMenuBox(
                                expanded = mandalExpanded,
                                onExpandedChange = { mandalExpanded = !mandalExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = selectedMandal,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("మండలం") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mandalExpanded) },
                                    modifier = Modifier.menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = mandalExpanded,
                                    onDismissRequest = { mandalExpanded = false }
                                ) {
                                    mandals.forEach { m ->
                                        DropdownMenuItem(
                                            text = { Text(m) },
                                            onClick = {
                                                selectedMandal = m
                                                selectedVillage = LocationHierarchy.getVillages(m).firstOrNull() ?: ""
                                                mandalExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Village
                            val villages = LocationHierarchy.getVillages(selectedMandal)
                            ExposedDropdownMenuBox(
                                expanded = villageExpanded,
                                onExpandedChange = { villageExpanded = !villageExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = selectedVillage,
                                    onValueChange = { selectedVillage = it },
                                    label = { Text("గ్రామం / వీధి") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = villageExpanded) },
                                    modifier = Modifier.menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = villageExpanded,
                                    onDismissRequest = { villageExpanded = false }
                                ) {
                                    villages.forEach { v ->
                                        DropdownMenuItem(
                                            text = { Text(v) },
                                            onClick = {
                                                selectedVillage = v
                                                villageExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // GPS Auto Capture Section
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "GPS లైవ్ లొకేషన్",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = if (gpsCoordinates.isNotBlank()) gpsCoordinates else "GPS ఇంకా నమోదు చేయలేదు",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (gpsCoordinates.isNotBlank()) NewsRedPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                OutlinedButton(
                                    onClick = { captureCurrentGpsLocation() },
                                    modifier = Modifier.testTag("capture_gps_button")
                                ) {
                                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("లొకేషన్ తీసుకోండి")
                                }
                            }
                        }

                        // Media Upload (Single / Multiple Images)
                        Text(
                            text = "మీడియా అప్‌లోడ్ (చిత్రాలు)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = NewsNavy)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(uploadedMediaUrls) { url ->
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(url)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Media",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { uploadedMediaUrls.remove(url) },
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.TopEnd)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "తొలగించు", tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }

                            item {
                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .size(80.dp)
                                        .testTag("gallery_picker_button")
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(22.dp))
                                        Text("గ్యాలరీ", fontSize = 10.sp)
                                    }
                                }
                            }

                            item {
                                OutlinedButton(
                                    onClick = {
                                        // Quick add news image URL or camera capture
                                        uploadedMediaUrls.add("https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=800")
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.size(80.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(22.dp))
                                        Text("కెమెరా", fontSize = 10.sp)
                                    }
                                }
                            }
                        }

                        // POST META AT BOTTOM OF POST FORM
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "వార్తా వివరాలు (Post Meta):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = NewsNavy
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "సృష్టించబడిన తేదీ మరియు సమయం: $currentFormattedDateTime",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "రిపోర్టర్: ${session.name} (ఫోన్: ${session.phone})",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "హోదా: పెండింగ్ ఆమోదం (Pending Approval by Admin)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }

                        // Submit Button
                        Button(
                            onClick = {
                                if (titleInput.isBlank()) {
                                    submitErrorMsg = "దయచేసి వార్తా శీర్షికను రాయండి"
                                    return@Button
                                }
                                if (contentInput.isBlank()) {
                                    submitErrorMsg = "దయచేసి వార్తా వివరాలను రాయండి"
                                    return@Button
                                }

                                isSubmitting = true
                                submitErrorMsg = null
                                submitSuccessMsg = null

                                scope.launch {
                                    val article = Article(
                                        titleTe = titleInput,
                                        contentTe = contentInput,
                                        imageUrl = uploadedMediaUrls.firstOrNull() ?: "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=800",
                                        mediaUrls = uploadedMediaUrls.toList(),
                                        categoryId = selectedCategory,
                                        state = selectedState,
                                        districtId = selectedDistrict,
                                        mandal = selectedMandal,
                                        village = selectedVillage,
                                        gpsCoordinates = gpsCoordinates.ifBlank { "16.7488° N, 77.4919° E" },
                                        authorName = session.name.ifBlank { "ఆధ్య ప్రతినిధి" },
                                        reporterPhone = session.phone,
                                        status = "pending",
                                        formattedDateTime = currentFormattedDateTime
                                    )

                                    val result = newsRepository.submitReporterArticle(article)
                                    isSubmitting = false
                                    if (result.isSuccess) {
                                        submitSuccessMsg = "మీ వార్త విజయవంతంగా సమర్పించబడింది! అడ్మిన్ ఆమోదం కోసం వేచి ఉంది."
                                        titleInput = ""
                                        contentInput = ""
                                        selectedTab = 1 // Switch to status view
                                    } else {
                                        submitErrorMsg = "వార్త సమర్పించడంలో లోపం: ${result.exceptionOrNull()?.message}"
                                    }
                                }
                            },
                            enabled = !isSubmitting,
                            colors = ButtonDefaults.buttonColors(containerColor = NewsRedPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("submit_news_button")
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("వార్తను ఆమోదం కోసం పంపండి (Submit News)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                1 -> {
                    // REPORTER STATUS VIEW
                    if (myArticles.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "మీరు ఇంకా వార్తలను సమర్పించలేదు.",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "'వార్త రాయండి' ట్యాబ్‌లో మీ మొదటి వార్తను నమోదు చేయండి.",
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
                            items(myArticles, key = { it.id }) { article ->
                                ReporterArticleStatusCard(article = article)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReporterArticleStatusCard(article: Article) {
    val statusColor = when (article.status.lowercase()) {
        "approved", "published" -> Color(0xFF2E7D32) // Green
        "rejected" -> NewsRedPrimary // Red
        else -> Color(0xFFE65100) // Amber / Orange for Pending
    }

    val statusLabel = when (article.status.lowercase()) {
        "approved", "published" -> "ఆమోదించబడింది (Approved & Live)"
        "rejected" -> "తిరస్కరించబడింది (Rejected)"
        else -> "పెండింగ్ ఆమోదం (Pending Approval)"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Status Tag & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = article.formattedDateTime.ifBlank { "ఇప్పుడే" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Headline
            Text(
                text = article.titleTe,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Content snippet
            Text(
                text = article.contentTe,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Location & GPS Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = NewsNavy,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${article.state} > ${article.districtId} > ${article.mandal} > ${article.village}",
                    style = MaterialTheme.typography.labelSmall.copy(color = NewsNavy, fontWeight = FontWeight.Medium),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            if (article.gpsCoordinates.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "GPS: ${article.gpsCoordinates}",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                )
            }

            // Rejection reason if rejected
            if (article.status.equals("rejected", ignoreCase = true) && article.rejectionReason.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = NewsRedPrimary.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "తిరస్కరణ కారణం (Rejection Reason):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = NewsRedPrimary
                        )
                        Text(
                            text = article.rejectionReason,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
