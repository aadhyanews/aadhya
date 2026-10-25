package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.repository.AuthRepository
import com.example.data.repository.UserSessionManager
import com.example.ui.theme.NewsGold
import com.example.ui.theme.NewsNavy
import com.example.ui.theme.NewsRedPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    authRepository: AuthRepository,
    sessionManager: UserSessionManager,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToReporter: () -> Unit,
    onNavigateToPhoneLogin: () -> Unit,
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser by authRepository.currentUser.collectAsStateWithLifecycle()
    val session by sessionManager.session.collectAsStateWithLifecycle()
    val isUserAdmin = authRepository.isUserAdmin() || session.isAdmin

    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    var isSigningIn by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "మరిన్ని ఎంపికలు",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.testTag("more_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Branding Card
            Card(
                colors = CardDefaults.cardColors(containerColor = NewsNavy),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.aadhyanews_logo),
                        contentDescription = "AADHYA NEWS",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "ఆధ్య న్యూస్",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                        )
                        Text(
                            text = "ప్రజల గొంతుక... ప్రజల వార్త...",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f))
                        )
                        Text(
                            text = "వెర్షన్ 1.0 (తెలుగు ఎడిషన్)",
                            style = MaterialTheme.typography.labelSmall.copy(color = NewsGold)
                        )
                    }
                }
            }

            // Theme Settings Section
            Text(
                text = "యాప్ రూపకల్పన",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NewsRedPrimary)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = NewsRedPrimary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "డార్క్ థీమ్ (రాత్రి మోడ్)",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { onToggleDarkTheme(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = NewsRedPrimary)
                        )
                    }
                }
            }

            // App Permissions Section
            Text(
                text = "యాప్ అనుమతులు",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NewsRedPrimary)
            )

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
                        Text(
                            text = "లొకేషన్, కెమెరా & నోటిఫికేషన్లు",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "వార్తల ప్రసారం మరియు GPS నిర్ధారణ కోసం",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    Button(
                        onClick = onRequestPermissions,
                        colors = ButtonDefaults.buttonColors(containerColor = NewsNavy),
                        modifier = Modifier.testTag("check_permissions_button")
                    ) {
                        Text("అనుమతించు", fontSize = 12.sp)
                    }
                }
            }

            // Reporter & Admin Management Section
            Text(
                text = "రిపోర్టర్ & అడ్మిన్ పోర్టల్",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NewsRedPrimary)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (!session.isLoggedIn && currentUser == null) {
                        Text(
                            text = "వార్తలు రాయడానికి రిపోర్టర్ లేదా అడ్మిన్ లాగిన్ చేయండి.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Primary Phone OTP Login Button
                        Button(
                            onClick = onNavigateToPhoneLogin,
                            colors = ButtonDefaults.buttonColors(containerColor = NewsRedPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_otp_login_button")
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("మొబైల్ OTP తో లాగిన్ అవ్వండి", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Secondary Google Sign-in option
                        OutlinedButton(
                            onClick = {
                                isSigningIn = true
                                authError = null
                                authRepository.signInWithGoogle(
                                    activity = context as Activity,
                                    scope = scope,
                                    onSuccess = { isSigningIn = false },
                                    onError = { err ->
                                        isSigningIn = false
                                        authError = err
                                    },
                                    onCancelled = { isSigningIn = false }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("google_sign_in_button")
                        ) {
                            if (isSigningIn) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = NewsNavy,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Google సైన్ ఇన్ (అడ్మిన్)")
                            }
                        }
                    } else {
                        // User is logged in via Phone OTP or Google
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (session.isLoggedIn) session.name else (currentUser?.displayName ?: "యూజర్"),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (session.isLoggedIn) "${session.phone} • ${session.assignedDistrict}" else (currentUser?.email ?: ""),
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isUserAdmin) NewsGold else NewsRedPrimary.copy(alpha = 0.15f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isUserAdmin) "అడ్మిన్ (Admin)" else "రిపోర్టర్ (Reporter)",
                                    color = if (isUserAdmin) NewsNavy else NewsRedPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isUserAdmin) {
                                Button(
                                    onClick = onNavigateToAdmin,
                                    colors = ButtonDefaults.buttonColors(containerColor = NewsNavy),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("admin_dashboard_button")
                                ) {
                                    Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("అడ్మిన్ డాష్‌బోర్డ్")
                                }
                            }

                            Button(
                                onClick = onNavigateToReporter,
                                colors = ButtonDefaults.buttonColors(containerColor = NewsRedPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reporter_dashboard_button")
                            ) {
                                Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("రిపోర్టర్ పోర్టల్")
                            }

                            OutlinedButton(
                                onClick = {
                                    sessionManager.logout()
                                    authRepository.signOut(scope) { }
                                }
                            ) {
                                Text("లాగౌట్")
                            }
                        }
                    }

                    authError?.let { err ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Information & Policy Section
            Text(
                text = "సమాచారం & చట్టపరమైన వివరాలు",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NewsRedPrimary)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.Info,
                        title = "ఆధ్య న్యూస్ గురించి",
                        onClick = { showAboutDialog = true }
                    )
                    Divider()
                    SettingsRowItem(
                        icon = Icons.Default.PrivacyTip,
                        title = "గోప్యతా విధానం (Privacy Policy)",
                        onClick = { showPrivacyDialog = true }
                    )
                    Divider()
                    SettingsRowItem(
                        icon = Icons.Default.Description,
                        title = "నిబంధనలు & షరతులు (Terms of Use)",
                        onClick = { showTermsDialog = true }
                    )
                    Divider()
                    SettingsRowItem(
                        icon = Icons.Default.Call,
                        title = "సంప్రదించండి (Contact Us)",
                        onClick = { showContactDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialogs
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("ఆధ్య న్యూస్ గురించి", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "ఆధ్య న్యూస్ అనేది నిష్పాక్షికమైన మరియు సమగ్రమైన తెలుగు వార్తా సమాచార వేదిక. తెలంగాణ, ఆంధ్రప్రదేశ్, హైదరాబాద్, నారాయణపేట, మహబూబ్ నగర్, కొడంగల్ తదితర ప్రాంతాల ప్రజా సమస్యలు, సంక్షేమ కార్యక్రమాలు, రాజకీయ, క్రీడా, సినీ విశేషాలను ప్రత్యక్షంగా పాఠకులకు అందిస్తుంది."
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("సరే") }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("గోప్యతా విధానం", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "ఆధ్య న్యూస్ వినియోగదారుల గోప్యతను గౌరవిస్తుంది. మీ బుక్‌మార్క్‌లు మరియు ఖాతా సమాచారం Firebase క్లౌడ్ ద్వారా భద్రపరచబడుతుంది. మేము మీ అనుమతి లేకుండా ఎలాంటి వ్యక్తిగత వివరాలను మూడో పక్షంతో పంచుకోము."
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text("అంగీకరిస్తున్నాను") }
            }
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("నిబంధనలు & షరతులు", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "ఈ యాప్‌లోని సమస్త వార్తలు, చిత్రాలు మరియు వీడియో సమాచారం ఆధ్య న్యూస్ కు చెందినవి లేదా అధీకృత భాగస్వామ్యంతో ప్రచురించబడినవి. అనుమతి లేకుండా వాణిజ్య ప్రయోజనాల కోసం పునఃప్రచురించడం నిషేధం."
                )
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) { Text("సరే") }
            }
        )
    }

    if (showContactDialog) {
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            title = { Text("సంప్రదించండి", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("ఆధ్య న్యూస్ నెట్‌వర్క్ కార్యాలయం")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("ఇమెయిల్: srisahasramedicals@gmail.com")
                    Text("ఫోన్ / వాట్సాప్: +91 94400 12345")
                    Text("హైదరాబాద్ / పాలమూరు, తెలంగాణ")
                }
            },
            confirmButton = {
                TextButton(onClick = { showContactDialog = false }) { Text("ముగించు") }
            }
        )
    }
}

@Composable
fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NewsNavy,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
    }
}
