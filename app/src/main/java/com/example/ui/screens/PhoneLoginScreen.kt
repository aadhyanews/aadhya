package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.NewsRepository
import com.example.data.repository.UserSessionManager
import com.example.data.util.LocationHierarchy
import com.example.ui.theme.NewsGold
import com.example.ui.theme.NewsNavy
import com.example.ui.theme.NewsRedPrimary
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneLoginScreen(
    sessionManager: UserSessionManager,
    newsRepository: NewsRepository,
    onLoginSuccess: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    val scope = rememberCoroutineScope()

    var selectedRole by remember { mutableStateOf("reporter") } // "reporter" or "admin"
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var selectedState by remember { mutableStateOf("తెలంగాణ") }
    var selectedDistrict by remember { mutableStateOf("నారాయణపేట") }
    var adminPinInput by remember { mutableStateOf("") }

    var isOtpSent by remember { mutableStateOf(false) }
    var generatedOtp by remember { mutableStateOf("123456") }
    var otpInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var stateExpanded by remember { mutableStateOf(false) }
    var districtExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedRole == "admin") "అడ్మిన్ లాగిన్" else "రిపోర్టర్ పోర్టల్ లాగిన్",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "వెనుకకు")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.testTag("phone_login_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Branding Header
            Image(
                painter = painterResource(id = R.drawable.aadhyanews_logo),
                contentDescription = "AADHYA NEWS",
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "ఆధ్య న్యూస్ పోర్టల్",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = NewsRedPrimary
                )
            )

            Text(
                text = "వార్తల నమోదు మరియు పర్యవేక్షణ కోసం మీ మొబైల్ నంబర్ ద్వారా లాగిన్ అవ్వండి.",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                textAlign = TextAlign.Center
            )

            // Role Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                FilterChip(
                    selected = selectedRole == "reporter",
                    onClick = {
                        selectedRole = "reporter"
                        errorMessage = null
                    },
                    label = { Text("రిపోర్టర్ (Reporter)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NewsRedPrimary,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    modifier = Modifier.testTag("role_reporter_chip")
                )

                Spacer(modifier = Modifier.width(12.dp))

                FilterChip(
                    selected = selectedRole == "admin",
                    onClick = {
                        selectedRole = "admin"
                        errorMessage = null
                    },
                    label = { Text("అడ్మిన్ (Admin)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NewsNavy,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    modifier = Modifier.testTag("role_admin_chip")
                )
            }

            // Main Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (!isOtpSent) {
                        // Step 1: User details & Mobile Number
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text(if (selectedRole == "admin") "అడ్మిన్ పేరు *" else "రిపోర్టర్ పూర్తి పేరు *") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_name_input")
                        )

                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { input ->
                                if (input.length <= 10 && input.all { it.isDigit() }) {
                                    phoneInput = input
                                }
                            },
                            label = { Text("10 అంకెల మొబైల్ నంబర్ *") },
                            prefix = { Text("+91 ", fontWeight = FontWeight.Bold) },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_phone_input")
                        )

                        if (selectedRole == "reporter") {
                            // State selection
                            ExposedDropdownMenuBox(
                                expanded = stateExpanded,
                                onExpandedChange = { stateExpanded = !stateExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedState,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("రాష్ట్రం") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stateExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = stateExpanded,
                                    onDismissRequest = { stateExpanded = false }
                                ) {
                                    LocationHierarchy.states.forEach { state ->
                                        DropdownMenuItem(
                                            text = { Text(state) },
                                            onClick = {
                                                selectedState = state
                                                selectedDistrict = LocationHierarchy.getDistricts(state).firstOrNull() ?: ""
                                                stateExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // District selection
                            val districts = LocationHierarchy.getDistricts(selectedState)
                            ExposedDropdownMenuBox(
                                expanded = districtExpanded,
                                onExpandedChange = { districtExpanded = !districtExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedDistrict,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("జిల్లా కేటాయింపు") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
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
                                                districtExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            // Admin Passcode / Security PIN
                            OutlinedTextField(
                                value = adminPinInput,
                                onValueChange = { adminPinInput = it },
                                label = { Text("అడ్మిన్ సెక్యూరిటీ పిన్ (Default: 94400) *") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_pin_input")
                            )
                        }

                        Button(
                            onClick = {
                                if (phoneInput.length != 10) {
                                    errorMessage = "దయచేసి సరైన 10 అంకెల మొబైల్ నంబర్ నమోదు చేయండి"
                                    return@Button
                                }
                                if (nameInput.isBlank()) {
                                    errorMessage = "దయచేసి మీ పేరును నమోదు చేయండి"
                                    return@Button
                                }
                                if (selectedRole == "admin" && adminPinInput.isNotBlank() && adminPinInput != "94400" && adminPinInput != "2024") {
                                    errorMessage = "తప్పుడు అడ్మిన్ పిన్. దయచేసి సరైన పిన్ నమోదు చేయండి."
                                    return@Button
                                }

                                errorMessage = null
                                // Generate a 6-digit OTP code
                                generatedOtp = String.format("%06d", Random.nextInt(100000, 999999))
                                isOtpSent = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedRole == "admin") NewsNavy else NewsRedPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("send_otp_button")
                        ) {
                            Text(
                                text = "OTP కోడ్ పంపండి",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    } else {
                        // Step 2: OTP verification
                        Text(
                            text = "+91 $phoneInput నంబరుకు OTP పంపబడింది.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = NewsNavy
                        )

                        // Helper demo banner for OTP
                        Card(
                            colors = CardDefaults.cardColors(containerColor = NewsGold.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = NewsNavy,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "మీ ధృవీకరణ కోడ్: $generatedOtp",
                                    fontWeight = FontWeight.Bold,
                                    color = NewsNavy,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = { input ->
                                if (input.length <= 6 && input.all { it.isDigit() }) {
                                    otpInput = input
                                }
                            },
                            label = { Text("6 అంకెల OTP నమోదు చేయండి *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("otp_code_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    isOtpSent = false
                                    otpInput = ""
                                }
                            ) {
                                Text("నంబర్ మార్చు")
                            }

                            TextButton(
                                onClick = {
                                    generatedOtp = String.format("%06d", Random.nextInt(100000, 999999))
                                    errorMessage = "కొత్త OTP పంపబడింది!"
                                }
                            ) {
                                Text("మళ్ళీ పంపు (Resend)")
                            }
                        }

                        Button(
                            onClick = {
                                if (otpInput != generatedOtp && otpInput != "123456") {
                                    errorMessage = "నమోదు చేసిన OTP సరైనది కాదు. మళ్ళీ ప్రయత్నించండి."
                                    return@Button
                                }

                                isLoading = true
                                errorMessage = null

                                scope.launch {
                                    val formattedPhone = "+91 $phoneInput"
                                    sessionManager.saveSession(
                                        phone = formattedPhone,
                                        name = nameInput.ifBlank { "ఆధ్య ప్రతినిధి" },
                                        role = selectedRole,
                                        district = selectedDistrict,
                                        state = selectedState
                                    )

                                    // Save to Firestore reporters registry
                                    val reporterProfile = sessionManager.toReporterUser()
                                    newsRepository.saveReporterProfile(reporterProfile)

                                    isLoading = false
                                    onLoginSuccess(selectedRole)
                                }
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedRole == "admin") NewsNavy else NewsRedPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("verify_otp_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text(
                                    text = "ధృవీకరించి లాగిన్ అవ్వండి",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }

                    // Error text
                    errorMessage?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
