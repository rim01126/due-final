package com.example.ui

import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import java.text.SimpleDateFormat
import java.util.*

const val APP_VERSION = "3.2.1"

// Dynamic Status Theme Colors (M3 Aligned High Density)
object StatusColors {
    val Paid = Color(0xFF059669)         // Emerald 600
    val Pending = Color(0xFFD97706)      // Amber 600
    val PartialPaid = Color(0xFFF97316)  // Orange 500
    val Overdue = Color(0xFFEF4444)      // Red 500
    val Critical = Color(0xFFBE123C)     // Rose 700
    
    // Background light versions for tinted badges
    val PaidBg = Color(0xFFECFDF5)
    val PendingBg = Color(0xFFFEF3C7)
    val PartialPaidBg = Color(0xFFFFF7ED)
    val OverdueBg = Color(0xFFFEF2F2)
    val CriticalBg = Color(0xFFFFF1F2)

    @Composable
    fun getStatusColor(status: String, isDark: Boolean = isSystemInDarkTheme()): Color {
        return when (status) {
            "Paid" -> if (isDark) Color(0xFF34D399) else Color(0xFF059669)
            "Pending" -> if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
            "Partial Paid", "Partial" -> if (isDark) Color(0xFFFB923C) else Color(0xFFF97316)
            "Overdue" -> if (isDark) Color(0xFFF87171) else Color(0xFFEF4444)
            "Critical" -> if (isDark) Color(0xFFFB7185) else Color(0xFFBE123C)
            else -> Color.Gray
        }
    }

    @Composable
    fun getStatusBgColor(status: String, isDark: Boolean = isSystemInDarkTheme()): Color {
        return when (status) {
            "Paid" -> if (isDark) Color(0xFF064E3B) else Color(0xFFECFDF5)
            "Pending" -> if (isDark) Color(0xFF78350F) else Color(0xFFFEF3C7)
            "Partial Paid", "Partial" -> if (isDark) Color(0xFF7C2D12) else Color(0xFFFFF7ED)
            "Overdue" -> if (isDark) Color(0xFF7F1D1D) else Color(0xFFFEF2F2)
            "Critical" -> if (isDark) Color(0xFF881337) else Color(0xFFFFF1F2)
            else -> if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmApp(viewModel: AppViewModel) {
    val isLoggedIn by viewModel.isLoggedInState.collectAsState()
    val currentRoute by viewModel.currentRouteState.collectAsState()
    val isGujarati by viewModel.languageState.collectAsState()
    val isDarkTheme by viewModel.darkThemeState.collectAsState()
    val currentRole by viewModel.currentRoleState.collectAsState()
    val staffName by viewModel.staffNameState.collectAsState()
    val selectedCustomerId by viewModel.selectedCustomerIdState.collectAsState()
    var showSupabaseInfoDialog by remember { mutableStateOf(false) }
    var showMpinSettingsDialog by remember { mutableStateOf(false) }
    
    val currentLang = if (isGujarati == Lang.GU) Lang.GU else Lang.EN
    val languageContextState = remember(currentLang) {
        LanguageContextState(
            lang = currentLang,
            switchLanguage = { newLang ->
                viewModel.languageState.value = newLang
            }
        )
    }

    CompositionLocalProvider(LocalLanguageContext provides languageContextState) {
        val lang = LocalLanguageContext.current.lang

        if (!isLoggedIn) {
            LoginScreen(viewModel, lang)
        } else {
            // Main Scaffold Layout
            Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_phone_world_logo),
                            contentDescription = "Logo",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(28.dp).padding(end = 6.dp)
                        )
                        Column {
                            Text(
                                text = "Phone World",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "v$APP_VERSION • $staffName ($currentRole)",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
                    // Profile Status Badge (Owner and current logged-in identity)
                    Row(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { showMpinSettingsDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Role Mode",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (currentRole == "Owner") "Owner" else currentRole,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Supabase Sync Action Button
                    val syncStatus by viewModel.syncStatusState.collectAsState()
                    IconButton(
                        onClick = { showSupabaseInfoDialog = true },
                        modifier = Modifier.testTag("supabase_sync_button")
                    ) {
                        val iconTint = when (syncStatus) {
                            "Syncing" -> MaterialTheme.colorScheme.primary
                            "Success" -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                            "Failed" -> MaterialTheme.colorScheme.error
                            "Not Configured" -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        
                        val iconVector = when (syncStatus) {
                            "Syncing" -> Icons.Default.CloudSync
                            "Success" -> Icons.Default.CloudDone
                            "Failed" -> Icons.Default.CloudOff
                            else -> Icons.Default.CloudQueue
                        }

                        Icon(
                            imageVector = iconVector,
                            contentDescription = "Sync status: $syncStatus",
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // English/Gujarati Toggle
                    IconButton(
                        onClick = {
                            viewModel.languageState.value = if (isGujarati == Lang.EN) Lang.GU else Lang.EN
                        },
                        modifier = Modifier.testTag("lang_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Language",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Dark/Light Theme Switching
                    IconButton(
                        onClick = { viewModel.darkThemeState.value = !isDarkTheme },
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = "Toggle Dark Mode",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Secure Session Logout Button
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
            ) {
                val destinations = listOf(
                    Triple("dashboard", AppStrings.dashboard(lang), Icons.Default.Dashboard),
                    Triple("customers", AppStrings.customerModule(lang), Icons.Default.People),
                    Triple("referrals", AppStrings.referrals(lang), Icons.Default.CardGiftcard),
                    Triple("staff", AppStrings.staffManagement(lang), Icons.Default.Group),
                    Triple("reports", AppStrings.reports(lang), Icons.Default.Assessment)
                )

                destinations.forEach { (route, label, icon) ->
                    val selected = currentRoute == route || (route == "customers" && selectedCustomerId != null)
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            viewModel.selectedCustomerIdState.value = null
                            viewModel.currentRouteState.value = route
                        },
                        icon = { Icon(imageVector = icon, contentDescription = label) },
                        label = {
                            Text(
                                text = label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 11.sp
                            )
                        },
                        modifier = Modifier.testTag("nav_item_$route")
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            AnimatedContent(
                targetState = if (selectedCustomerId != null) "customer_detail" else currentRoute,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ScreenNavigation"
            ) { targetRoute ->
                when (targetRoute) {
                    "dashboard" -> DashboardScreen(viewModel, lang)
                    "customers" -> CustomersScreen(viewModel, lang)
                    "customer_detail" -> {
                        val custId = selectedCustomerId
                        if (custId != null) {
                            CustomerDetailScreen(viewModel, custId, lang)
                        } else {
                            Box(modifier = Modifier.fillMaxSize())
                        }
                    }
                    "referrals" -> ReferralsScreen(viewModel, lang)
                    "staff" -> StaffScreen(viewModel, lang)
                    "reports" -> ReportsScreen(viewModel, lang)
                    "critical_zone" -> CriticalZoneScreen(viewModel, lang)
                    else -> DashboardScreen(viewModel, lang)
                }
            }
        }
    }

    val whatsAppReminderData by viewModel.whatsAppReminderDialogData.collectAsState()
    whatsAppReminderData?.let { data ->
        WhatsAppReminderDialog(
            data = data,
            viewModel = viewModel,
            lang = lang,
            onDismiss = { viewModel.whatsAppReminderDialogData.value = null }
        )
    }

    if (showSupabaseInfoDialog) {
        Dialog(onDismissRequest = { showSupabaseInfoDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                val scrollState = rememberScrollState()
                val syncStatus by viewModel.syncStatusState.collectAsState()
                val syncError by viewModel.syncErrorState.collectAsState()
                val isConfigured = viewModel.isSupabaseConfigured

                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isConfigured) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                            contentDescription = "Supabase",
                            tint = if (isConfigured) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Supabase Server Sync",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isConfigured) "Cross-Device Cloud Database Connected" else "Supabase Not Configured",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isConfigured) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Connection parameters
                    Text(text = "CONNECTION METADATA", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Database Sync:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = if (isConfigured) "ENABLED" else "DISABLED (Local Model)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isConfigured) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                            )
                        }
                        
                        val maskedUrl = if (SupabaseClient.supabaseUrl.length > 25) {
                            SupabaseClient.supabaseUrl.take(15) + "..." + SupabaseClient.supabaseUrl.takeLast(10)
                        } else {
                            SupabaseClient.supabaseUrl
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Service URL:", style = MaterialTheme.typography.bodyMedium)
                            Text(maskedUrl, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        val keyStatus = if (!isConfigured) "Placeholder / Inactive" else "Active Token"
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Anon Key:", style = MaterialTheme.typography.bodyMedium)
                            Text(keyStatus, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = if (isConfigured) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Sync state indicators
                    Text(text = "CURRENT SYNC STATUS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Status State:", style = MaterialTheme.typography.bodyMedium)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (syncStatus == "Syncing") {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            }
                            Text(
                                text = syncStatus.uppercase(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = when (syncStatus) {
                                    "Syncing" -> MaterialTheme.colorScheme.primary
                                    "Success" -> Color(0xFF4CAF50)
                                    "Failed" -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }

                    // Render Sync Errors if any
                    if (syncError != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Last Error:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = syncError ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    // Troubleshooting Instructions depending on the connection state
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    
                    Text(
                        text = "CONFIGURATION RESOLUTION MANUAL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!isConfigured) {
                        Text(
                            text = "Your local application model is currently falling back to on-device Room/SQLite storage since credentials are still placeholders.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text("1. Create/open `.env` file in the project's root folder.", style = MaterialTheme.typography.bodySmall)
                            Text("2. Populate with active credentials:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E1E1E), RoundedCornerShape(4.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "SUPABASE_URL=https://your-id.supabase.co\nSUPABASE_ANON_KEY=your_actual_key",
                                    color = Color(0xFF4CAF50),
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 10.sp)
                                )
                            }
                            Text("3. Run: `gradle clean` & `gradle assembleDebug` to trigger regeneration of the BuildConfig fields.", style = MaterialTheme.typography.bodySmall)
                            Text("4. Execute `schema.sql` script in Supabase's SQL Editor to establish all 11 required tables.", style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "✓ App successfully loaded keys from packaging. If sync fails, check standard checklist:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4CAF50)
                            )
                            Text("• Internet connection must be active, with DNS accessibility on the emulator/phone.", style = MaterialTheme.typography.bodySmall)
                            Text("• Validate that you executed the total SQL schema from `/supabase_and_web/schema.sql` inside your Supabase project's database.", style = MaterialTheme.typography.bodySmall)
                            Text("• In case of RLS problems, check `/supabase_and_web/rls_policies.sql` parameters.", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showSupabaseInfoDialog = false }) {
                            Text("Dismiss")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = { viewModel.syncWithSupabase() },
                            enabled = isConfigured && syncStatus != "Syncing",
                            modifier = Modifier.testTag("supabase_sync_now_dialog_button")
                        ) {
                            Text(if (syncStatus == "Syncing") "Syncing..." else "Sync Now")
                        }
                    }
                }
            }
        }
    }

    if (showMpinSettingsDialog) {
        MpinSettingsDialog(
            viewModel = viewModel,
            lang = lang,
            onDismiss = { showMpinSettingsDialog = false }
        )
    }
}
}
}

// ==========================================
// BUSINESS WORKSPACE SECURE GATEWAY
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: AppViewModel, lang: Lang) {
    val isDarkTheme by viewModel.darkThemeState.collectAsState()
    val loginError by viewModel.loginErrorState.collectAsState()
    
    var mobileInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Utilities row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // English/Gujarati Toggle
                    TextButton(
                        onClick = {
                            viewModel.languageState.value = if (lang == Lang.EN) Lang.GU else Lang.EN
                        },
                        modifier = Modifier.testTag("login_lang_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Language",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (lang == Lang.EN) "ગુજરાતી" else "English",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    // Theme Toggle
                    IconButton(
                        onClick = { viewModel.darkThemeState.value = !isDarkTheme },
                        modifier = Modifier.testTag("login_theme_toggle")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Beautiful Custom Logo
                Card(
                    modifier = Modifier.size(72.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = "App Logo",
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // App Branding Text
                Text(
                    text = if (lang == Lang.EN) "Phone World CRM" else "ફોન વર્લ્ડ સીઆરએમ",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = if (lang == Lang.EN) "Secure Business Administration Portal" else "સુરક્ષિત બિઝનેસ વહીવટી પોર્ટલ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mobile Input Field
                OutlinedTextField(
                    value = mobileInput,
                    onValueChange = { mobileInput = it },
                    label = { Text(if (lang == Lang.EN) "Mobile / Login ID" else "મોબાઇલ / લોગિન આઈડી") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = "Phone") },
                    modifier = Modifier.fillMaxWidth().testTag("login_mobile_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                // Password Input Field
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text(if (lang == Lang.EN) "Password" else "પાસવર્ડ") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Password") },
                    trailingIcon = {
                        val image = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = "Toggle Password")
                        }
                    },
                    visualTransformation = if (isPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                // Login Error Warning
                if (loginError != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = loginError ?: "",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Login Submit Button
                Button(
                    onClick = {
                        viewModel.loginWithCredentials(mobileInput, passwordInput)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("login_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (lang == Lang.EN) "Access CRM Space" else "લૉગિન કરો",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Subtitle Info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = if (lang == Lang.EN) {
                            "Note: Only authorized Owners can initialize this workspace. Use pre-configured owner digits to log in."
                        } else {
                            "નોંધ: ફક્ત અધિકૃત માલિકો જ આ વર્કસ્પેસ શરૂ કરી શકે છે. લોગ ઇન કરવા માટે પૂર્વ-રૂપરેખાંકિત માલિક નંબરોનો ઉપયોગ કરો."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ==========================================
// SCREEN 1: THE BEAUTIFUL DASHBOARD
// ==========================================
@Composable
fun DashboardScreen(viewModel: AppViewModel, lang: Lang) {
    val lang = LocalLanguageContext.current.lang
    val customers by viewModel.customersList.collectAsState()
    val dues by viewModel.duesList.collectAsState()
    val payments by viewModel.paymentsList.collectAsState()
    val followups by viewModel.followupsList.collectAsState()
    val referrals by viewModel.customerReferralsList.collectAsState()
    val activityLogs by viewModel.activityLogsList.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current

    // Local dialog display trigger states for quick actions
    var showAddCustomerDialogFromDash by remember { mutableStateOf(false) }
    var showQuickPaymentPicker by remember { mutableStateOf(false) }
    var showQuickFollowupPicker by remember { mutableStateOf(false) }
    
    var selectedCustomerForFollowup by remember { mutableStateOf<Customer?>(null) }
    var selectedCustomerForPayment by remember { mutableStateOf<Customer?>(null) }

    // Calculate core statistics
    val totalC = customers.size
    val activeC = customers.count { it.status == "Active" || it.status == "Pending" || it.status == "Overdue" }
    val totalPending = customers.sumOf { it.pendingAmount }
    val totalCollected = payments.sumOf { it.amountPaid }
    
    // Today metrics
    val todayStr = viewModel.repository.getTodayDateString()
    val calendarTom = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    val tomorrowStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendarTom.time)

    val todayCollection = payments.filter { it.paymentDate == todayStr }.sumOf { it.amountPaid }
    val todayFollowUps = followups.count { it.nextFollowUpDate == todayStr && it.status != "Completed" }
    val tomorrowFollowUps = followups.count { it.nextFollowUpDate == tomorrowStr && it.status != "Completed" }

    val overdueCount = customers.count { it.status == "Overdue" }
    val criticalCount = customers.count { it.status == "Critical" }
    val criticalPendingAmt = customers.filter { it.status == "Critical" }.sumOf { it.pendingAmount }
    val referralCount = referrals.size

    val staffName by viewModel.staffNameState.collectAsState()
    val currentRole by viewModel.currentRoleState.collectAsState()
    val monogram = staffName.take(2).uppercase().ifEmpty { "OW" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // High Density Styled Workspace Header View
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Phone World",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, fontSize = 22.sp),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = (if (lang == Lang.EN) "MANAGER DASHBOARD" else "મેનેજર ડેશબોર્ડ") + " • $currentRole ($staffName)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                )
            }
            
            // Styled Avatar Emblem
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = monogram,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        // Critical zone alarm alert panel
        if (criticalCount > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSystemInDarkTheme()) Color(0xFF31050F) else Color(0xFFFFF1F2))
                    .border(
                        width = 1.dp,
                        color = if (isSystemInDarkTheme()) Color(0xFF4C0519) else Color(0xFFFFE4E6),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        viewModel.filterStatusState.value = "Critical"
                        viewModel.currentRouteState.value = "customers"
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE11D48)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (lang == Lang.EN) "CRITICAL ZONE" else "ક્રિટિકલ ઝોન",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = if (isSystemInDarkTheme()) Color(0xFFFDA4AF) else Color(0xFF881337),
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = if (lang == Lang.EN) "$criticalCount High-risk accounts overdue" else "$criticalCount ખૂબ જ જોખમી ગ્રાકકો બાકી છે",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSystemInDarkTheme()) Color(0xFFFDA4AF) else Color(0xFFBE123C)
                            )
                        )
                    }
                }
                Button(
                    onClick = {
                        viewModel.filterStatusState.value = "Critical"
                        viewModel.currentRouteState.value = "customers"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp).testTag("act_now_btn")
                ) {
                    Text(
                        text = if (lang == Lang.EN) "ACT NOW" else "એક્શન લો",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Daily Alerts: Overdue Installment dues older than 30 days
        val duesOlderThan30Days = dues.filter {
            it.dueStatus != "Paid" && calculateDaysPassed(it.dueDate) >= 30
        }
        if (duesOlderThan30Days.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSystemInDarkTheme()) Color(0xFF2E2300) else Color(0xFFFEF3C7))
                    .border(
                        width = 1.dp,
                        color = if (isSystemInDarkTheme()) Color(0xFF533A00) else Color(0xFFFDE68A),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        viewModel.currentRouteState.value = "reports"
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFD97706)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notification",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (lang == Lang.EN) "DAILY ALERTS: OVERDUE DUES (>30 DAYS)" else "દૈનિક ચેતવણી: બાકી સાયરન (>૩૦ દિવસ)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = if (isSystemInDarkTheme()) Color(0xFFFDE68A) else Color(0xFF78350F),
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = if (lang == Lang.EN) 
                                "${duesOlderThan30Days.size} installments are pending for >30 days!" 
                                else "${duesOlderThan30Days.size} હપ્તા ૩૦ દિવસથી વધુ સમયથી બાકી છે!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSystemInDarkTheme()) Color(0xFFFDE68A) else Color(0xFF92400E)
                            )
                        )
                    }
                }
                Button(
                    onClick = {
                        viewModel.currentRouteState.value = "reports"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp).testTag("act_view_overdue_btn")
                ) {
                    Text(
                        text = if (lang == Lang.EN) "VIEW ALL" else "બધા જુઓ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Core Financial Metrics Cards Section
        Text(
            text = if (lang == Lang.EN) "FINANCIAL OVERVIEW" else "નાણાકીય વિહંગાવલોકન",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DashboardMetricCard(
                title = AppStrings.totalPendingAmount(lang),
                value = "₹${totalPending.toInt()}",
                color = StatusColors.Overdue,
                icon = Icons.Default.HourglassEmpty,
                modifier = Modifier.weight(1f)
            )
            DashboardMetricCard(
                title = AppStrings.totalCollectedAmount(lang),
                value = "₹${totalCollected.toInt()}",
                color = StatusColors.Paid,
                icon = Icons.Default.CheckCircle,
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DashboardMetricCard(
                title = AppStrings.todayCollection(lang),
                value = "₹${todayCollection.toInt()}",
                color = StatusColors.PartialPaid,
                icon = Icons.Default.TrendingUp,
                modifier = Modifier.weight(1f)
            )
            DashboardMetricCard(
                title = if (lang == Lang.EN) "Total Customers" else "કુલ ગ્રાહકો",
                value = "$totalC",
                color = MaterialTheme.colorScheme.primary,
                icon = Icons.Default.People,
                modifier = Modifier.weight(1f)
            )
        }

        // Fast Action Buttons Panel for High Density Staff Workflow
        Text(
            text = if (lang == Lang.EN) "QUICK REAL-TIME ACTIONS" else "ઝડપી કાર્યો",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Add Customer Button
            Button(
                onClick = { showAddCustomerDialogFromDash = true },
                modifier = Modifier.weight(1f).height(44.dp).testTag("dash_add_cust_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(if (lang == Lang.EN) "Add Customer" else "નવો ગ્રાહક", fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
            
            // Take Payment
            Button(
                onClick = { showQuickPaymentPicker = true },
                modifier = Modifier.weight(1f).height(44.dp).testTag("dash_quick_pay_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSystemInDarkTheme()) Color(0xFF064E3B) else Color(0xFFECFDF5),
                    contentColor = if (isSystemInDarkTheme()) Color(0xFF34D399) else Color(0xFF047857)
                )
            ) {
                Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(if (lang == Lang.EN) "Take Payment" else "પેમેન્ટ લોગ", fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
            
            // Add Followup
            Button(
                onClick = { showQuickFollowupPicker = true },
                modifier = Modifier.weight(1f).height(44.dp).testTag("dash_quick_fol_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSystemInDarkTheme()) Color(0xFF78350F) else Color(0xFFFEF3C7),
                    contentColor = if (isSystemInDarkTheme()) Color(0xFFFBBF24) else Color(0xFFB45309)
                )
            ) {
                Icon(Icons.Default.AddComment, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(if (lang == Lang.EN) "Add Follow-up" else "નોંધ ઉમેરો", fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Add Staff Option (Visible only to owner)
            if (currentRole == "Owner") {
                Button(
                    onClick = { viewModel.currentRouteState.value = "staff" },
                    modifier = Modifier.weight(1f).height(44.dp).testTag("dash_add_staff_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.onTertiaryContainer)
                ) {
                    Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Staff", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }

            // Critical Zone screen routing
            Button(
                onClick = { viewModel.currentRouteState.value = "critical_zone" },
                modifier = Modifier.weight(1f).height(44.dp).testTag("dash_critical_zone_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFECE0DF).copy(alpha = 0.5f), contentColor = Color(0xFFE11D48))
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFE11D48))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Critical Zone", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, color = Color(0xFFE11D48))
            }

            // Referral screen routing
            Button(
                onClick = { viewModel.currentRouteState.value = "referrals" },
                modifier = Modifier.weight(1f).height(44.dp).testTag("dash_referral_reminder_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
            ) {
                Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Referrals", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }



        // Overdue & Recovery Performance Bar Graph (Strict Tailwind High Density Spec)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == Lang.EN) "COLLECTION PERFORMANCE" else "કલેક્શન કામગીરી",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = if (lang == Lang.EN) "LAST 7 DAYS" else "છેલ્લા ૭ દિવસ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                
                val dailyData = remember(payments) {
                    (0..6).map { daysAgo ->
                        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
                        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
                        val shortDateLabel = SimpleDateFormat("dd/MM", Locale.US).format(cal.time)
                        val dailyAmount = payments.filter { it.paymentDate == dateStr }.sumOf { it.amountPaid }
                        Pair(shortDateLabel, dailyAmount)
                    }.reversed()
                }
                
                val maxAmount = remember(dailyData) {
                    val max = dailyData.maxOf { it.second }
                    if (max <= 0.0) 1000.0 else max
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    dailyData.forEach { (label, amt) ->
                        val heightFraction = (amt / maxAmount).toFloat().coerceIn(0.02f, 1f)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (amt > 0) "₹${if(amt >= 1000) (amt/1000).toInt().toString()+"k" else amt.toInt()}" else "",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(20.dp)
                                    .fillMaxHeight(heightFraction)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(
                                        if (amt > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Urgent Action Indicators Section
        Text(
            text = if (lang == Lang.EN) "ACTION RECOVERY SIGNALS" else "રિકવરી અને એક્શન સિગ્નલ",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DashboardTaskCard(
                label = AppStrings.todayFollowUps(lang),
                count = "$todayFollowUps",
                subText = if (lang == Lang.EN) "Due Today" else "આજે કરવાના",
                color = StatusColors.PartialPaid,
                modifier = Modifier.weight(1f)
            )
            DashboardTaskCard(
                label = AppStrings.tomorrowFollowUps(lang),
                count = "$tomorrowFollowUps",
                subText = if (lang == Lang.EN) "Scheduled" else "નક્કી કરેલા",
                color = StatusColors.Pending,
                modifier = Modifier.weight(1f)
            )
            DashboardTaskCard(
                label = AppStrings.criticalCustomers(lang),
                count = "$criticalCount",
                subText = "₹${criticalPendingAmt.toInt()}",
                color = StatusColors.Critical,
                modifier = Modifier.weight(1f)
            )
        }

        // Dynamic High-Priority Action Items List (Today's Follow-ups)
        Text(
            text = if (lang == Lang.EN) "ACTION ITEMS (TODAY'S RECOVERIES)" else "આજના કલેક્શન ફોલો-અપ",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
        )

        val todayFollowups = remember(followups) {
            followups.filter { it.nextFollowUpDate == todayStr && it.status != "Completed" }
        }

        if (todayFollowups.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (lang == Lang.EN) "All caught up for today!" else "આજના બધા ફોલો-અપ પૂર્ણ છે!",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                todayFollowups.take(5).forEach { f ->
                    val cust = customers.firstOrNull { it.id == f.customerId }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Monogram badge
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = f.customerName.take(2).uppercase().ifEmpty { "C" },
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = f.customerName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = if (lang == Lang.EN) {
                                            "Dues: ₹${cust?.pendingAmount?.toInt() ?: 0} • Mob: ${cust?.mobileNumber ?: ""}"
                                        } else {
                                            "બાકી: ₹${cust?.pendingAmount?.toInt() ?: 0} • ફોન: ${cust?.mobileNumber ?: ""}"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // WhatsApp reminder direct trigger
                                IconButton(
                                    onClick = {
                                        val amt = cust?.pendingAmount ?: 0.0
                                        viewModel.whatsAppReminderDialogData.value = WhatsAppReminderData(
                                            customerName = f.customerName,
                                            mobileNumber = cust?.mobileNumber ?: "",
                                            amount = amt,
                                            dueDate = todayStr
                                        )
                                    },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSystemInDarkTheme()) Color(0xFF064E3B) else Color(0xFFECFDF5)),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "WhatsApp",
                                        tint = if (isSystemInDarkTheme()) Color(0xFF34D399) else Color(0xFF059669),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                // Phone Call button
                                IconButton(
                                    onClick = {
                                        viewModel.logActivity("Call", "Initiated voice call to: ${f.customerName}")
                                        try {
                                            val number = cust?.mobileNumber ?: ""
                                            if (number.isNotEmpty()) {
                                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                                    data = android.net.Uri.parse("tel:$number")
                                                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                context.startActivity(intent)
                                            }
                                        } catch (e: Exception) {
                                            // Handle dial exceptions gracefully
                                        }
                                    },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Call",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                // Quick Log Note
                                IconButton(
                                    onClick = {
                                        selectedCustomerForFollowup = cust
                                    }  ,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSystemInDarkTheme()) Color(0xFF78350F) else Color(0xFFFEF3C7)),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddComment,
                                        contentDescription = "Log Note",
                                        tint = if (isSystemInDarkTheme()) Color(0xFFFBBF24) else Color(0xFFB45309),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Audit Trails/Activity logs
        Text(
            text = AppStrings.recentActivities(lang),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
        )
        if (activityLogs.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = if (lang == Lang.EN) "No recent activity logged." else "હજુ સુધી કોઈ પ્રવૃત્તિ નોંધાઈ નથી.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    activityLogs.take(5).forEach { log ->
                        val dateString = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(log.timestamp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (log.actionType) {
                                            "Payment" -> StatusColors.Paid
                                            "Create" -> MaterialTheme.colorScheme.primary
                                            "Followup" -> StatusColors.PartialPaid
                                            else -> Color.Gray
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "[$dateString] ",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Text(
                                text = "${log.staffName}: ",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = log.description,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Phone World CRM",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = "Version $APP_VERSION",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
    }

    // Modal forms / Dialog rendering for Dash activities
    if (showAddCustomerDialogFromDash) {
        AddCustomerDialog(
            viewModel = viewModel,
            lang = lang,
            onDismiss = { showAddCustomerDialogFromDash = false },
            onSave = { name, mob, altM, addr, city, prod, price, pending, notes, refType, refName, invoiceNo, modelDet, dueDays ->
                viewModel.addCustomer(name, mob, altM, addr, city, prod, price, pending, notes, refType, refName, invoiceNo, modelDet, dueDays)
                showAddCustomerDialogFromDash = false
                android.widget.Toast.makeText(context, "Customer created successfully!", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showQuickPaymentPicker) {
        QuickCustomerPickerDialog(
            customers = customers,
            lang = lang,
            title = if (lang == Lang.EN) "Select Customer to Log Payment" else "પેમેન્ટ માટે ગ્રાહક પસંદ કરો",
            onDismiss = { showQuickPaymentPicker = false },
            onSelected = { customer ->
                showQuickPaymentPicker = false
                val activeDue = dues.firstOrNull { it.customerId == customer.id && it.dueStatus != "Paid" }
                if (activeDue != null) {
                    selectedCustomerForPayment = customer
                } else {
                    android.widget.Toast.makeText(context, "No unpaid/pending dues found for ${customer.customerName}.", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    if (showQuickFollowupPicker) {
        QuickCustomerPickerDialog(
            customers = customers,
            lang = lang,
            title = if (lang == Lang.EN) "Select Customer to Log Follow-up" else "ફોલો-અપ માટે ગ્રાહક પસંદ કરો",
            onDismiss = { showQuickFollowupPicker = false },
            onSelected = { customer ->
                showQuickFollowupPicker = false
                selectedCustomerForFollowup = customer
            }
        )
    }

    // Secondary Dialog launches
    val paymentCustomer = selectedCustomerForPayment
    if (paymentCustomer != null) {
        val activeDue = dues.firstOrNull { it.customerId == paymentCustomer.id && it.dueStatus != "Paid" }
        if (activeDue != null) {
            CollectPaymentDialog(
                due = activeDue,
                lang = lang,
                onDismiss = { selectedCustomerForPayment = null },
                onSave = { amount, mode, notes ->
                    viewModel.collectPayment(paymentCustomer.id, activeDue.id, amount, mode, notes)
                    selectedCustomerForPayment = null
                    android.widget.Toast.makeText(context, "Collected ₹${amount.toInt()} from ${paymentCustomer.customerName}!", android.widget.Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    val followupCustomer = selectedCustomerForFollowup
    if (followupCustomer != null) {
        AddFollowupDialog(
            lang = lang,
            onDismiss = { selectedCustomerForFollowup = null },
            onSave = { notes, nextDate, promiseDate, status ->
                viewModel.addFollowup(followupCustomer.id, followupCustomer.customerName, notes, nextDate, promiseDate, status)
                selectedCustomerForFollowup = null
                android.widget.Toast.makeText(context, "Follow-up logged successfully!", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun QuickCustomerPickerDialog(
    customers: List<Customer>,
    lang: Lang,
    title: String,
    onDismiss: () -> Unit,
    onSelected: (Customer) -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = remember(search, customers) {
        customers.filter {
            it.customerName.contains(search, ignoreCase = true) ||
            it.mobileNumber.contains(search) ||
            it.cityVillage.contains(search, ignoreCase = true)
        }.take(10)
    }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text(if (lang == Lang.EN) "Search customer..." else "શોધો...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                LazyColumn(modifier = Modifier.heightIn(max = 240.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (filtered.isEmpty()) {
                        item {
                            Text(
                                text = if (lang == Lang.EN) "No customers found" else "ગ્રાહક મળ્યા નથી",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    } else {
                        items(filtered.size) { index ->
                            val customer = filtered[index]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSelected(customer) }
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = customer.customerName, fontWeight = FontWeight.Bold)
                                    Text(text = "Mob: ${customer.mobileNumber} • ${customer.cityVillage}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(text = "₹${customer.pendingAmount.toInt()}", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black))
                            }
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text(if (lang == Lang.EN) "Cancel" else "રદ કરો")
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardMetricCard(title: String, value: String, color: Color, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(imageVector = icon, contentDescription = null, tint = color.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = color)
            )
        }
    }
}

@Composable
fun DashboardTaskCard(label: String, count: String, subText: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = color))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subText, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}


// ==========================================
// SCREEN 2: THE ROBUST CUSTOMERS DIRECTORY
// ==========================================
/**
 * Utility function to check if a customer has a pending or incomplete follow-up
 * scheduled for today or has already passed.
 */
fun getOverdueOrTodayFollowUp(
    customerId: Int,
    followups: List<PaymentFollowup>,
    todayStr: String
): PaymentFollowup? {
    return followups.firstOrNull { f ->
        f.customerId == customerId &&
        f.status != "Completed" &&
        f.status != "Paid" &&
        f.nextFollowUpDate.isNotEmpty() &&
        f.nextFollowUpDate <= todayStr
    }
}

@Composable
fun CustomersScreen(viewModel: AppViewModel, lang: Lang) {
    val customers by viewModel.customersList.collectAsState()
    val searchQuery by viewModel.searchCustomerQueryState.collectAsState()
    val filterStatus by viewModel.filterStatusState.collectAsState()
    val currentRole by viewModel.currentRoleState.collectAsState()
    val followups by viewModel.followupsList.collectAsState()
    val todayStr = remember { viewModel.repository.getTodayDateString() }
    
    var showAddDialog by remember { mutableStateOf(false) }

    // Filtration Logic
    val filteredCustomers = customers.filter { customer ->
        val matchesSearch = customer.customerName.contains(searchQuery, ignoreCase = true) ||
                customer.mobileNumber.contains(searchQuery) ||
                customer.cityVillage.contains(searchQuery, ignoreCase = true)
        
        val matchesStatus = if (filterStatus == "All") true else customer.status.equals(filterStatus, ignoreCase = true)
        matchesSearch && matchesStatus
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_customer_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = AppStrings.addCustomer(lang))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = AppStrings.customerModule(lang),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black)
            )

            // Search Bar Component
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchCustomerQueryState.value = it },
                label = { Text(if (lang == Lang.EN) "Search customer name, phone, or city..." else "ગ્રાહક, ફોન અથવા ગામ શોધો...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_customer_input"),
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchCustomerQueryState.value = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                }
            )

            // Filter status Chips Scroll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statuses = listOf("All", "Pending", "Overdue", "Critical", "Paid")
                statuses.forEach { status ->
                    val selected = filterStatus == status
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.filterStatusState.value = status },
                        label = { Text(text = if (status == "All") (if (lang == Lang.EN) "All" else "બધા") else status) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (status != "All") StatusColors.getStatusColor(status) else MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Results summary list
            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Group, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (lang == Lang.EN) "No matching customers found." else "કોઈ ગ્રાહક મળ્યા નથી.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCustomers) { customer ->
                        val overdueOrTodayFollowUp = getOverdueOrTodayFollowUp(customer.id, followups, todayStr)
                        val hasFollowUpDue = overdueOrTodayFollowUp != null
                        val dueFollowUpDate = overdueOrTodayFollowUp?.nextFollowUpDate ?: ""

                        CustomerListItem(
                            customer = customer,
                            lang = lang,
                            hasFollowUpDue = hasFollowUpDue,
                            dueFollowUpDate = dueFollowUpDate,
                            todayStr = todayStr
                        ) {
                            viewModel.selectedCustomerIdState.value = customer.id
                        }
                    }
                }
            }
        }
    }

    // Modal dialog to Add Customer
    if (showAddDialog) {
        AddCustomerDialog(
            viewModel = viewModel,
            lang = lang,
            onDismiss = { showAddDialog = false },
            onSave = { name, mob, altM, addr, city, prod, price, pending, notes, refType, refName, invoiceNo, modelDet, dueDays ->
                viewModel.addCustomer(name, mob, altM, addr, city, prod, price, pending, notes, refType, refName, invoiceNo, modelDet, dueDays)
                showAddDialog = false
            }
        )
    }
}

fun exportStaffReferralsToCsv(
    context: android.content.Context,
    staffName: String,
    customers: List<Customer>,
    referrals: List<CustomerReferral>,
    dues: List<Due>
) {
    val staffReferrals = if (staffName == "All Staff" || staffName == "બધા સ્ટાફ") {
        referrals.filter { it.referredByType.equals("Staff", ignoreCase = true) || it.referredByType.equals("Owner", ignoreCase = true) }
    } else {
        referrals.filter { 
            (it.referredByType.equals("Staff", ignoreCase = true) || it.referredByType.equals("Owner", ignoreCase = true)) && 
            it.referrerName.equals(staffName, ignoreCase = true)
        }
    }
    
    val staffCustIds = staffReferrals.map { it.customerId }.toSet()
    val referredCusts = customers.filter { it.id in staffCustIds }
    
    val csvHeader = "Staff Member,Referred Customer,Phone,Product,Purchase Date,Invoice Number,Total Bill (Rs),Pending Amount (Rs),Due Status\n"
    val csvBody = StringBuilder()
    
    for (cust in referredCusts) {
        val custDues = dues.filter { it.customerId == cust.id && it.dueStatus != "Paid" }
        val pendingTotal = custDues.sumOf { it.dueAmount }
        
        val actualReferrer = staffReferrals.find { it.customerId == cust.id }?.referrerName ?: staffName
        
        val nameEscaped = cust.customerName.replace(",", " ")
        val phoneEscaped = cust.mobileNumber.replace(",", " ")
        val productEscaped = cust.productPurchased.replace(",", " ")
        val dateEscaped = cust.purchaseDate.replace(",", " ")
        val invoiceEscaped = cust.invoiceNumber.ifEmpty { "N/A" }.replace(",", " ")
        val statusEscaped = cust.status.replace(",", " ")
        
        csvBody.append("\"$actualReferrer\",\"$nameEscaped\",\"$phoneEscaped\",\"$productEscaped\",\"$dateEscaped\",\"$invoiceEscaped\",${cust.totalBillAmount},$pendingTotal,\"$statusEscaped\"\n")
    }
    
    val csvContent = csvHeader + csvBody.toString()
    val cacheDir = context.cacheDir
    val fileName = "Staff_${staffName.replace(" ", "_").replace("[^a-zA-Z0-9]".toRegex(), "")}_Referrals.csv"
    val file = java.io.File(cacheDir, fileName)
    
    try {
        file.writeText(csvContent, Charsets.UTF_8)
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "com.example.fileprovider",
            file
        )
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "$staffName's Referral Performance Report")
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(intent, "Share Staff Referrals Summary"))
    } catch (e: Exception) {
        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Staff Referrals CSV", csvContent)
        clipboard.setPrimaryClip(clip)
        android.widget.Toast.makeText(context, "Export error. CSV copied to Clipboard!", android.widget.Toast.LENGTH_LONG).show()
    }
}

fun exportReferralsDueToCsv(
    context: android.content.Context,
    customers: List<Customer>,
    referrals: List<CustomerReferral>,
    dues: List<Due>
) {
    val csvHeader = "Referrer Name,Referrer Type,Referred Customer,Phone,Product,Total Bill (Rs),Pending Due Amount (Rs),Due Status\n"
    val csvBody = StringBuilder()
    
    for (ref in referrals) {
        val cust = customers.find { it.id == ref.customerId } ?: continue
        val custDues = dues.filter { it.customerId == cust.id && it.dueStatus != "Paid" }
        val pendingTotal = custDues.sumOf { it.dueAmount }
        
        val nameEscaped = cust.customerName.replace(",", " ")
        val phoneEscaped = cust.mobileNumber.replace(",", " ")
        val productEscaped = cust.productPurchased.replace(",", " ")
        val referrerNameEscaped = ref.referrerName.replace(",", " ")
        val referrerTypeEscaped = ref.referredByType.replace(",", " ")
        val statusEscaped = cust.status.replace(",", " ")
        
        csvBody.append("\"$referrerNameEscaped\",\"$referrerTypeEscaped\",\"$nameEscaped\",\"$phoneEscaped\",\"$productEscaped\",${cust.totalBillAmount},$pendingTotal,\"$statusEscaped\"\n")
    }
    
    val csvContent = csvHeader + csvBody.toString()
    val cacheDir = context.cacheDir
    val fileName = "Referred_Customers_Outstanding_Dues.csv"
    val file = java.io.File(cacheDir, fileName)
    
    try {
        file.writeText(csvContent, Charsets.UTF_8)
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "com.example.fileprovider",
            file
        )
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Referred Customers Outstanding Dues Report")
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(intent, "Share Referred Dues summary"))
    } catch (e: Exception) {
        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Referred Dues CSV", csvContent)
        clipboard.setPrimaryClip(clip)
        android.widget.Toast.makeText(context, "Export error. CSV copied to Clipboard!", android.widget.Toast.LENGTH_LONG).show()
    }
}

fun exportAgingDuesToCsv(
    context: android.content.Context,
    dues: List<Due>,
    customers: List<Customer>,
    minDays: Int
) {
    val filteredDues = dues.filter { d ->
        d.dueStatus != "Paid" && calculateDaysPassed(d.dueDate) >= minDays
    }
    
    val csvHeader = "Customer Name,Phone,Purchased Product,Purchase Date,Outstanding Due (Rs),Due Date,Days Elapsed,Notes\n"
    val csvBody = StringBuilder()
    for (d in filteredDues) {
        val cust = customers.find { it.id == d.customerId }
        val phone = cust?.mobileNumber ?: ""
        val prod = cust?.productPurchased ?: ""
        val purchDate = cust?.purchaseDate ?: ""
        val daysElapsed = calculateDaysPassed(d.dueDate)
        
        val escapedName = d.customerName.replace(",", " ")
        val escapedPhone = phone.replace(",", " ")
        val escapedProd = prod.replace(",", " ")
        val escapedPurchDate = purchDate.replace(",", " ")
        val escapedDueDate = d.dueDate.replace(",", " ")
        val escapedNotes = d.notes.replace(",", " ").replace("\n", " ")
        
        csvBody.append("$escapedName,$escapedPhone,$escapedProd,$escapedPurchDate,${d.dueAmount},$escapedDueDate,$daysElapsed,$escapedNotes\n")
    }
    
    val csvContent = csvHeader + csvBody.toString()
    
    val cacheDir = context.cacheDir
    val fileName = "Dues_Over_${minDays}_Days_Report.csv"
    val file = java.io.File(cacheDir, fileName)
    
    try {
        file.writeText(csvContent)
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "com.example.fileprovider",
            file
        )
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Dues Aging Report (> $minDays Days)")
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(intent, "Share Aging Report via..."))
    } catch (e: Exception) {
        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Dues Report CSV", csvContent)
        clipboard.setPrimaryClip(clip)
        android.widget.Toast.makeText(context, "Export error. CSV copied to Clipboard!", android.widget.Toast.LENGTH_LONG).show()
    }
}

fun calculateDaysPassed(dateString: String): Long {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = sdf.parse(dateString) ?: return 0L
        val currentDate = sdf.parse(sdf.format(Date())) ?: return 0L
        val diff = currentDate.time - date.time
        diff / (24 * 60 * 60 * 1000)
    } catch (e: Exception) {
        0L
    }
}

@Composable
fun CustomerListItem(
    customer: Customer,
    lang: Lang,
    hasFollowUpDue: Boolean = false,
    dueFollowUpDate: String = "",
    todayStr: String = "",
    onClick: () -> Unit
) {
    val borderStroke = if (hasFollowUpDue) {
        val isToday = dueFollowUpDate == todayStr
        val highlightColor = if (isToday) StatusColors.Pending else StatusColors.Overdue
        BorderStroke(1.5.dp, highlightColor)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }

    val containerColor = if (hasFollowUpDue) {
        val isToday = dueFollowUpDate == todayStr
        val highlightBg = if (isToday) StatusColors.PendingBg.copy(alpha = 0.25f) else StatusColors.OverdueBg.copy(alpha = 0.25f)
        highlightBg
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("customer_card_${customer.id}"),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = borderStroke
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = customer.customerName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Styled Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StatusColors.getStatusBgColor(customer.status))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = when (customer.status) {
                                "Paid" -> AppStrings.paid(lang)
                                "Pending" -> AppStrings.pending(lang)
                                "Overdue" -> AppStrings.overdue(lang)
                                "Critical" -> AppStrings.critical(lang)
                                else -> customer.status
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = StatusColors.getStatusColor(customer.status)
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = customer.mobileNumber, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(imageVector = Icons.Default.Place, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = customer.cityVillage, style = MaterialTheme.typography.bodySmall)
                }

                if (customer.productPurchased.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${AppStrings.productPurchased(lang)}: ${customer.productPurchased}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                    )
                }

                // If this customer has an overdue or today's follow-up, render a highlight badge
                if (hasFollowUpDue && dueFollowUpDate.isNotEmpty()) {
                    val isToday = dueFollowUpDate == todayStr
                    val badgeBg = if (isToday) StatusColors.PendingBg else StatusColors.OverdueBg
                    val badgeTextColor = if (isToday) StatusColors.Pending else StatusColors.Overdue
                    val badgeText = if (isToday) AppStrings.followUpDueToday(lang) else AppStrings.followUpOverdueDays(lang)
                    val badgeIcon = if (isToday) Icons.Default.Schedule else Icons.Default.Warning

                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = badgeIcon,
                                contentDescription = null,
                                size = 12.dp,
                                tint = badgeTextColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$badgeText: $dueFollowUpDate",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = badgeTextColor
                                )
                            )
                        }
                    }
                }

                // Purchase Date and Days Passed
                val daysPassed = calculateDaysPassed(customer.purchaseDate)
                val purchaseLabel = if (lang == Lang.EN) "Purchase Date" else "ખરીદી તારીખ"
                val daysPassedLabel = if (lang == Lang.EN) "days passed" else "દિવસો પસાર થયા"
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$purchaseLabel: ${customer.purchaseDate} ($daysPassed $daysPassedLabel)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (lang == Lang.EN) "Pending" else "બાકી",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "₹${customer.pendingAmount.toInt()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = if (customer.pendingAmount > 0) StatusColors.getStatusColor(customer.status) else StatusColors.Paid
                    )
                )
            }
        }
    }
}


// Icon wrapper helper
@Composable
fun Icon(imageVector: ImageVector, contentDescription: String?, size: androidx.compose.ui.unit.Dp, tint: Color) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = tint,
        modifier = Modifier.size(size)
    )
}

// Dialog Component for adding new Customer
@Composable
fun AddCustomerDialog(
    viewModel: AppViewModel,
    lang: Lang,
    onDismiss: () -> Unit,
    onSave: (name: String, mob: String, altM: String, addr: String, city: String, prod: String, price: Double, pending: Double, notes: String, refType: String, refName: String, invoiceNo: String, modelDet: String, dueDays: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var altMobile by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var product by remember { mutableStateOf("") }
    var modelDetail by remember { mutableStateOf("") }
    var invoiceNumber by remember { mutableStateOf("") }
    var billText by remember { mutableStateOf("") }
    var pendingText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var dueDaysInput by remember { mutableStateOf("30") }
    
    // Referral Selection
    var refType by remember { mutableStateOf("Direct") }
    var refName by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = AppStrings.addCustomer(lang),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(AppStrings.customerName(lang)) },
                    modifier = Modifier.fillMaxWidth().testTag("cust_name_field")
                )

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text(AppStrings.mobileNumber(lang)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("cust_mobile_field")
                )

                OutlinedTextField(
                    value = altMobile,
                    onValueChange = { altMobile = it },
                    label = { Text(AppStrings.alternateMobile(lang)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(AppStrings.address(lang)) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text(AppStrings.cityVillage(lang)) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = product,
                    onValueChange = { product = it },
                    label = { Text(AppStrings.productPurchased(lang)) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = modelDetail,
                    onValueChange = { modelDetail = it },
                    label = { Text(AppStrings.modelDetail(lang)) },
                    modifier = Modifier.fillMaxWidth().testTag("cust_model_detail_field")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = billText,
                        onValueChange = { billText = it },
                        label = { Text(AppStrings.totalBillAmount(lang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = pendingText,
                        onValueChange = { pendingText = it },
                        label = { Text(AppStrings.pendingAmount(lang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                if ((pendingText.toDoubleOrNull() ?: 0.0) > 0.0) {
                    OutlinedTextField(
                        value = dueDaysInput,
                        onValueChange = { dueDaysInput = it },
                        label = { Text(if (lang == Lang.EN) "Initial Installment Due Period (Days)" else "પ્રથમ હપ્તાની નિયત મુદત (દિવસો)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("due_days_field")
                    )
                }

                OutlinedTextField(
                    value = invoiceNumber,
                    onValueChange = { invoiceNumber = it },
                    label = { Text(AppStrings.invoiceNumber(lang)) },
                    modifier = Modifier.fillMaxWidth().testTag("cust_invoice_field")
                )

                // Referral Source picker
                Text(text = AppStrings.selectReferrer(lang), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val types = listOf("Direct", "Owner", "Staff", "Existing Customer", "External Person")
                    types.forEach { type ->
                        FilterChip(
                            selected = refType == type,
                            onClick = { 
                                refType = type 
                                refName = when (type) {
                                    "Owner" -> "Rais Memon (Owner)"
                                    else -> ""
                                }
                            },
                            label = { Text(type, fontSize = 11.sp) }
                        )
                    }
                }

                if (refType != "Direct") {
                    var dropdownExpanded by remember { mutableStateOf(false) }
                    val staffList by viewModel.staffMemberList.collectAsState()
                    val referralPersons by viewModel.referralPersonsList.collectAsState()
                    val customersList by viewModel.customersList.collectAsState()

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = refName,
                            onValueChange = { refName = it },
                            label = { 
                                val labelText = when (refType) {
                                    "Owner" -> if (lang == Lang.EN) "Owner Name" else "માલિકનું નામ"
                                    "Staff" -> if (lang == Lang.EN) "Select Staff Member" else "સ્ટાફ નક્કી કરો"
                                    "Existing Customer" -> if (lang == Lang.EN) "Select Existing Customer" else "ચાલુ ગ્રાહક નક્કી કરો"
                                    "External Person" -> if (lang == Lang.EN) "Select External Referrer" else "બહારના રેફરર નક્કી કરો"
                                    else -> AppStrings.referrerName(lang)
                                }
                                Text(labelText)
                            },
                            readOnly = (refType == "Owner"),
                            trailingIcon = {
                                if (refType != "Owner") {
                                    IconButton(onClick = { dropdownExpanded = true }) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Expand Options"
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().clickable(enabled = refType != "Owner") {
                                dropdownExpanded = true
                            }
                        )

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            when (refType) {
                                "Staff" -> {
                                    if (staffList.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text(if (lang == Lang.EN) "No staff members found" else "કોઈ સ્ટાફ સભ્યો નથી") },
                                            onClick = { dropdownExpanded = false }
                                        )
                                    } else {
                                        staffList.forEach { staff ->
                                            DropdownMenuItem(
                                                text = { Text(staff.name) },
                                                onClick = {
                                                    refName = staff.name
                                                    dropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                                "Existing Customer" -> {
                                    if (customersList.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text(if (lang == Lang.EN) "No existing customers found" else "કોઈ ગ્રાહકો મળ્યા નથી") },
                                            onClick = { dropdownExpanded = false }
                                        )
                                    } else {
                                        customersList.forEach { cust ->
                                            DropdownMenuItem(
                                                text = { Text("${cust.customerName} (${cust.mobileNumber})") },
                                                onClick = {
                                                    refName = cust.customerName
                                                    dropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                                "External Person" -> {
                                    if (referralPersons.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text(if (lang == Lang.EN) "No external referrers found" else "બાહ્ય રેફરર મળ્યા નથી") },
                                            onClick = { dropdownExpanded = false }
                                        )
                                    } else {
                                        referralPersons.forEach { refPerson ->
                                            DropdownMenuItem(
                                                text = { Text("${refPerson.fullName} (${refPerson.city})") },
                                                onClick = {
                                                    refName = refPerson.fullName
                                                    dropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(AppStrings.notes(lang)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(AppStrings.cancel(lang))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotEmpty() && mobile.isNotEmpty()) {
                                onSave(
                                    name, mobile, altMobile, address, city, product,
                                    billText.toDoubleOrNull() ?: 0.0,
                                    pendingText.toDoubleOrNull() ?: 0.0,
                                    notes, refType, refName, invoiceNumber, modelDetail,
                                    dueDaysInput.toIntOrNull() ?: 30
                                )
                            }
                        },
                        modifier = Modifier.testTag("cust_save_btn")
                    ) {
                        Text(AppStrings.save(lang))
                    }
                }
            }
        }
    }
}


// ==========================================
// SCREEN 3: DETAILED CUSTOMER PROFILE
// ==========================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomerDetailScreen(viewModel: AppViewModel, customerId: Int, lang: Lang) {
    val lang = LocalLanguageContext.current.lang
    val customerState by viewModel.getCustomerByIdFlow(customerId).collectAsState(initial = null)
    val dues by viewModel.getDuesForCustomer(customerId).collectAsState(initial = emptyList<Due>())
    val payments by viewModel.getPaymentsForCustomer(customerId).collectAsState(initial = emptyList<PaymentEntry>())
    val followups by viewModel.getFollowupsForCustomer(customerId).collectAsState(initial = emptyList<PaymentFollowup>())
    val currentRole by viewModel.currentRoleState.collectAsState()

    // Dialog toggle states
    var showCollectPaymentDialog by remember { mutableStateOf(false) }
    var selectedDueToCollect by remember { mutableStateOf<Due?>(null) }
    var showAddDueDialog by remember { mutableStateOf(false) }
    var showAddFollowupDialog by remember { mutableStateOf(false) }
    var pendingDeleteAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var deleteConfirmMessage by remember { mutableStateOf("") }

    val customer = customerState ?: return

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = { viewModel.selectedCustomerIdState.value = null }
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                text = AppStrings.customerName(lang),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            
            // Delete customer (Owner Only)
            if (currentRole == "Owner") {
                IconButton(
                    onClick = {
                        deleteConfirmMessage = if (lang == Lang.EN) 
                            "This will permanently delete ${customer.customerName} along with all due schedules and history." 
                            else "${customer.customerName} ના તમામ હપ્તા અને ઇતિહાસ સાથે કાયમી ધોરણે કાઢી નાખવામાં આવશે."
                        pendingDeleteAction = {
                            viewModel.deleteCustomer(customer)
                        }
                    },
                    modifier = Modifier.testTag("delete_customer_button")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = StatusColors.Critical)
                }
            }
        }

        // Customer Quick Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = customer.customerName,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StatusColors.getStatusBgColor(customer.status))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = customer.status,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = StatusColors.getStatusColor(customer.status)
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = customer.mobileNumber, fontWeight = FontWeight.Bold)
                    if (customer.alternateMobileNumber.isNotEmpty()) {
                        Text(text = " / ${customer.alternateMobileNumber}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "${customer.address}, ${customer.cityVillage}")
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = if (lang == Lang.EN) "Purchased product" else "ખરીદેલ ફોન", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = customer.productPurchased, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        if (customer.modelDetail.isNotEmpty()) {
                            Text(
                                text = "(${customer.modelDetail})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                        Text(text = if (lang == Lang.EN) "Total Bill" else "કુલ બિલ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "₹${customer.totalBillAmount.toInt()}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                        if (customer.invoiceNumber.isNotEmpty()) {
                            Text(
                                text = "Inv: #${customer.invoiceNumber}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = if (lang == Lang.EN) "Purchase Date" else "ખરીદી તારીખ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = customer.purchaseDate, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                        Text(text = if (lang == Lang.EN) "Days Passed" else "દિવસો પસાર થયા", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val days = calculateDaysPassed(customer.purchaseDate)
                        Text(text = "$days " + (if (lang == Lang.EN) "days" else "દિવસો"), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary))
                    }
                }
            }
        }

        // LARGE ACTION SHORTCUT BUTTONS FOR GENERAL USAGE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Call Shortcut
            Button(
                onClick = {
                    viewModel.logActivity("Call", "Initiated voice call to: ${customer.customerName}")
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("shortcut_call_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text(AppStrings.call(lang), fontSize = 12.sp)
            }

            // WhatsApp Direct Template reminder sending trigger
            Button(
                onClick = {
                    val activeDue = dues.firstOrNull { it.dueStatus != "Paid" }
                    val amount = activeDue?.dueAmount ?: customer.pendingAmount
                    val date = activeDue?.dueDate ?: viewModel.repository.getTodayDateString()
                    
                    viewModel.whatsAppReminderDialogData.value = WhatsAppReminderData(
                        customerName = customer.customerName,
                        mobileNumber = customer.mobileNumber,
                        amount = amount,
                        dueDate = date
                    )
                },
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp)
                    .testTag("shortcut_whatsapp_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)) // WhatsApp Green
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text(AppStrings.whatsApp(lang), fontSize = 12.sp, color = Color.White)
            }

            // Add installment due directly
            Button(
                onClick = { showAddDueDialog = true },
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp)
                    .testTag("shortcut_add_due_btn")
            ) {
                Icon(imageVector = Icons.Default.Receipt, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(AppStrings.addDue(lang), fontSize = 11.sp, maxLines = 1)
            }
        }

        // DUES LEDGER SECTION
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = AppStrings.dueManagement(lang),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "${dues.count { it.dueStatus != "Paid" }} Inst. Pending",
                style = MaterialTheme.typography.bodySmall,
                color = StatusColors.Overdue
            )
        }

        if (dues.isEmpty()) {
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(if (lang == Lang.EN) "No installments pending." else "કોઈ બાકી હપ્તો જોવા મળ્યો નથી.")
                }
            }
        } else {
            val duesByInvoice = dues.groupBy { it.invoiceNumber.trim() }
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                duesByInvoice.forEach { (invNo, invoiceDues) ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Section Header for Invoice
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = "Invoice",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (invNo.isEmpty()) {
                                        if (lang == Lang.EN) "General Dues (No Invoice)" else "સામાન્ય હપ્તા (કોઈ ભરતિયું નથી)"
                                    } else {
                                        if (lang == Lang.EN) "Invoice Wise: #$invNo" else "ભરતિયું વાઇઝ: #$invNo"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // List dues for this invoice group
                        invoiceDues.forEach { due ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = "₹${due.dueAmount.toInt()}",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = StatusColors.getStatusColor(due.dueStatus))
                                                )
                                                if (due.notes.isNotEmpty()) {
                                                    Text(
                                                        text = "(${due.notes})",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                            val daysDiff = calculateDaysPassed(due.dueDate)
                                            val daysPassedText = when {
                                                daysDiff > 0 -> {
                                                    if (lang == Lang.EN) " ($daysDiff days passed)" else " ($daysDiff દિવસો વીતી ગયા)"
                                                }
                                                daysDiff < 0 -> {
                                                    val d = kotlin.math.abs(daysDiff)
                                                    if (lang == Lang.EN) " ($d days left)" else " ($d દિવસો બાકી)"
                                                }
                                                else -> {
                                                    if (lang == Lang.EN) " (Today)" else " (આજે)"
                                                }
                                            }
                                            Text(
                                                text = "Due Date: ${due.dueDate}$daysPassedText",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = if (daysDiff > 0) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (daysDiff > 0) StatusColors.Overdue else MaterialTheme.colorScheme.onSurface
                                                )
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Status tag inside
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(StatusColors.getStatusBgColor(due.dueStatus))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(due.dueStatus, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = StatusColors.getStatusColor(due.dueStatus)))
                                            }
                                            
                                            Spacer(modifier = Modifier.width(8.dp))

                                            // Collect Action if not fully paid
                                            if (due.dueStatus != "Paid") {
                                                Button(
                                                    onClick = {
                                                        selectedDueToCollect = due
                                                        showCollectPaymentDialog = true
                                                    },
                                                    modifier = Modifier.height(36.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = StatusColors.Paid)
                                                ) {
                                                    Text(AppStrings.collectPayment(lang), fontSize = 10.sp)
                                                }
                                            }

                                            // Delete Due (Owner Only)
                                            if (currentRole == "Owner") {
                                                IconButton(onClick = {
                                                    deleteConfirmMessage = if (lang == Lang.EN) 
                                                        "Are you sure you want to delete this installment of ₹${due.dueAmount.toInt()}? This will alter total pending balances." 
                                                        else "શું તમે ખરેખર ₹${due.dueAmount.toInt()} નો આ હપ્તો કાઢી નાખવા માંગો છો? આનાથી કુલ બાકી રકમો બદલાઈ જશે."
                                                    pendingDeleteAction = {
                                                        viewModel.deleteDue(due.id, customerId)
                                                    }
                                                }) {
                                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = StatusColors.Critical)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // PAST PAYMENTS HISTORY
        Text(
            text = if (lang == Lang.EN) "Receipt Settlement History" else "ચુકવણી મેળવેલ ઇતિહાસ",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        if (payments.isEmpty()) {
            Text(text = if (lang == Lang.EN) "No payments collected yet." else "હજી સુધી કોઈ ચુકવણી મળી નથી.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    payments.forEach { payment ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "₹${payment.amountPaid.toInt()} collected via ${payment.paymentMode}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(text = "Date: ${payment.paymentDate} • By ${payment.collectedBy}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Success", tint = StatusColors.Paid)
                        }
                        if (payment != payments.last()) {
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }

        // FOLLOW-UP TIMELINE SECTION
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = AppStrings.followUps(lang),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Button(
                onClick = { showAddFollowupDialog = true },
                modifier = Modifier.height(34.dp).testTag("add_followup_shortcut_btn")
            ) {
                Text(AppStrings.addFollowup(lang), fontSize = 10.sp)
            }
        }

        if (followups.isEmpty()) {
            Text(text = if (lang == Lang.EN) "No follow-up notes written." else "કોઈ નોંધ લખવામાં આવી નથી.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                followups.forEach { f ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = f.notes, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(text = f.status, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Next Contact: ${f.nextFollowUpDate}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (f.promiseToPayDate.isNotEmpty()) {
                                    Text(text = "PTP: ${f.promiseToPayDate}", style = MaterialTheme.typography.bodySmall, color = StatusColors.PartialPaid, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(text = "Logged by ${f.staffName} on ${f.followUpDate}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    }

    // Modal forms
    val dueToCollect = selectedDueToCollect
    if (showCollectPaymentDialog && dueToCollect != null) {
        CollectPaymentDialog(
            due = dueToCollect,
            lang = lang,
            onDismiss = { showCollectPaymentDialog = false },
            onSave = { amount, mode, notes ->
                viewModel.collectPayment(customerId, dueToCollect.id, amount, mode, notes)
                showCollectPaymentDialog = false
                selectedDueToCollect = null
            }
        )
    }

    if (showAddDueDialog) {
        AddDueDialog(
            lang = lang,
            onDismiss = { showAddDueDialog = false },
            onSave = { amount, dueDate, notes, invoiceNumber ->
                viewModel.addDue(customerId, customer.customerName, amount, dueDate, notes, invoiceNumber)
                showAddDueDialog = false
            }
        )
    }

    if (showAddFollowupDialog) {
        AddFollowupDialog(
            lang = lang,
            onDismiss = { showAddFollowupDialog = false },
            onSave = { notes, nextDate, promiseDate, status ->
                viewModel.addFollowup(customerId, customer.customerName, notes, nextDate, promiseDate, status)
                showAddFollowupDialog = false
            }
        )
    }

    pendingDeleteAction?.let { action ->
        DeleteMpinConfirmDialog(
            onConfirm = action,
            onDismiss = { pendingDeleteAction = null },
            lang = lang,
            viewModel = viewModel,
            message = deleteConfirmMessage
        )
    }
}

// Collections form Dialog
@Composable
fun CollectPaymentDialog(
    due: Due,
    lang: Lang,
    onDismiss: () -> Unit,
    onSave: (amount: Double, mode: String, notes: String) -> Unit
) {
    var amountText by remember { mutableStateOf(due.dueAmount.toString()) }
    var selectedMode by remember { mutableStateOf("UPI") }
    var notes by remember { mutableStateOf("") }

    val modes = listOf("Cash", "UPI", "Card", "Finance", "Other")

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(AppStrings.collectPayment(lang), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(text = "Paying for Due of ₹${due.dueAmount.toInt()}", style = MaterialTheme.typography.bodySmall)

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(AppStrings.amountPaid(lang)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("payment_amount_input")
                )

                Text(AppStrings.paymentMode(lang), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    modes.forEach { mode ->
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = { selectedMode = mode },
                            label = { Text(mode) }
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(AppStrings.notes(lang)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(AppStrings.cancel(lang)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (amt > 0.0) {
                                onSave(amt, selectedMode, notes)
                            }
                        },
                        modifier = Modifier.testTag("payment_save_btn")
                    ) {
                        Text(AppStrings.save(lang))
                    }
                }
            }
        }
    }
}

// Installment dues form dialog
@Composable
fun AddDueDialog(
    lang: Lang,
    onDismiss: () -> Unit,
    onSave: (amount: Double, dueDate: String, notes: String, invoiceNumber: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var dueDateText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var invoiceNumber by remember { mutableStateOf("") }

    // Estimate due date as today + 30 days
    LaunchedEffect(Unit) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 30)
        dueDateText = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(AppStrings.addDue(lang), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(AppStrings.dueAmount(lang)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("added_due_amount_input")
                )

                OutlinedTextField(
                    value = invoiceNumber,
                    onValueChange = { invoiceNumber = it },
                    label = { Text(if (lang == Lang.EN) "Invoice Number" else "ભરતિયું નંબર (Invoice No)") },
                    modifier = Modifier.fillMaxWidth().testTag("added_due_invoice_input")
                )

                OutlinedTextField(
                    value = dueDateText,
                    onValueChange = { dueDateText = it },
                    label = { Text(AppStrings.dueDate(lang) + " (yyyy-MM-dd)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(AppStrings.notes(lang)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(AppStrings.cancel(lang)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (amt > 0.0 && dueDateText.isNotEmpty()) {
                                onSave(amt, dueDateText, notes, invoiceNumber)
                            }
                        },
                        modifier = Modifier.testTag("added_due_save_btn")
                    ) {
                        Text(AppStrings.save(lang))
                    }
                }
            }
        }
    }
}

// Followups Form Dialog
@Composable
fun AddFollowupDialog(
    lang: Lang,
    onDismiss: () -> Unit,
    onSave: (notes: String, nextDate: String, promiseDate: String, status: String) -> Unit
) {
    var notes by remember { mutableStateOf("") }
    var nextDateText by remember { mutableStateOf("") }
    var PromisePayDateText by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("Pending") }

    val nextDaysList = listOf("Pending", "Completed", "No Response", "Promised", "Paid")

    LaunchedEffect(Unit) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 3) // Contact in 3 days
        nextDateText = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, 3)
        PromisePayDateText = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(AppStrings.addFollowup(lang), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(AppStrings.notes(lang)) },
                    modifier = Modifier.fillMaxWidth().testTag("follow_notes_input")
                )

                OutlinedTextField(
                    value = nextDateText,
                    onValueChange = { nextDateText = it },
                    label = { Text(AppStrings.nextFollowup(lang) + " (yyyy-MM-dd)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = PromisePayDateText,
                    onValueChange = { PromisePayDateText = it },
                    label = { Text(AppStrings.promiseToPay(lang) + " (yyyy-MM-dd) Optional") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(if (lang == Lang.EN) "Follow-Up Result" else "ફોલો-અપ સ્થિતિ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    nextDaysList.forEach { status ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = { selectedStatus = status },
                            label = { Text(status) }
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(AppStrings.cancel(lang)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (notes.isNotEmpty() && nextDateText.isNotEmpty()) {
                                onSave(notes, nextDateText, PromisePayDateText, selectedStatus)
                            }
                        },
                        modifier = Modifier.testTag("follow_save_btn")
                    ) {
                        Text(AppStrings.save(lang))
                    }
                }
            }
        }
    }
}


// ==========================================
// SCREEN 4: DETATED REFERRAL CRM
// ==========================================
@Composable
fun ReferralsScreen(viewModel: AppViewModel, lang: Lang) {
    val persons by viewModel.referralPersonsList.collectAsState()
    val customers by viewModel.customersList.collectAsState()
    val referrals by viewModel.customerReferralsList.collectAsState()
    val currentRole by viewModel.currentRoleState.collectAsState()
    val dues by viewModel.duesList.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var showAddReferrerDialog by remember { mutableStateOf(false) }
    var selectedProfileReferrer by remember { mutableStateOf<ReferralPerson?>(null) }
    var activeReferrerReminderData by remember { mutableStateOf<ReferralPerson?>(null) }
    var pendingDeleteAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var deleteConfirmMessage by remember { mutableStateOf("") }

    val activeReferrer = activeReferrerReminderData
    if (activeReferrer != null) {
        val refereeCusts = customers.filter { c ->
            referrals.any { r -> r.customerId == c.id && r.referrerName == activeReferrer.fullName }
        }
        ReferrerReminderDialog(
            referrer = activeReferrer,
            refereeCustomers = refereeCusts,
            dues = dues,
            viewModel = viewModel,
            lang = lang,
            onDismiss = { activeReferrerReminderData = null }
        )
    }

    val activeProfile = selectedProfileReferrer
    if (activeProfile != null) {
        ReferralProfile(
            referrer = activeProfile,
            viewModel = viewModel,
            lang = lang,
            onBack = { selectedProfileReferrer = null }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = AppStrings.refersTitle(lang),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black)
            )
            Button(
                onClick = { showAddReferrerDialog = true },
                modifier = Modifier.testTag("add_referrer_btn")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(AppStrings.addReferrer(lang))
            }
        }

        // Referral Dashboard KPIs block
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(text = "${persons.size}", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer))
                    Text(text = "Total Referrers", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }

                VerticalDivider(modifier = Modifier.height(40.dp).padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f))

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(text = "${referrals.size}", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer))
                    Text(text = "Customers Referred", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        // Referral list of players
        Text(
            text = if (lang == Lang.EN) "Affiliate Referral Directory" else "રેફરલ વ્યક્તિઓની યાદી",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        if (persons.isEmpty()) {
            Text(text = "No referral persons populated.")
        } else {
            persons.forEach { person ->
                val refereeCustomers = customers.filter { c ->
                    referrals.any { r -> r.customerId == c.id && r.referrerName == person.fullName }
                }
                val totalS = refereeCustomers.sumOf { it.totalBillAmount }

                // Dynamically fetch from the helper method
                val metrics = viewModel.getReferralMetricsForPerson(person.fullName)
                val pendS = metrics.pendingAmount
                val criticalRefereeCustomers = metrics.criticalCustomers

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("referrer_card_${person.id}")
                        .clickable { selectedProfileReferrer = person },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = person.fullName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text(text = "Mob: ${person.mobileNumber} • ${person.city}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            
                            Row {
                                // Dynamic Call Button
                                IconButton(onClick = {
                                    viewModel.logActivity("Call", "Called Referral Person: ${person.fullName}")
                                    try {
                                        val parseUri = android.net.Uri.parse("tel:${person.mobileNumber}")
                                        context.startActivity(android.content.Intent(android.content.Intent.ACTION_DIAL, parseUri).apply { flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK })
                                    } catch (e: Exception) {}
                                }) {
                                    Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
                                }

                                // WhatsApp Statement and Reminder Trigger Dialog
                                IconButton(
                                    onClick = {
                                        activeReferrerReminderData = person
                                    },
                                    modifier = Modifier.testTag("whatsapp_ref_btn_${person.id}")
                                ) {
                                    Icon(imageVector = Icons.Default.Send, contentDescription = "WhatsApp Reminder Alert", tint = Color(0xFF25D366))
                                }
                                
                                if (currentRole == "Owner") {
                                    IconButton(onClick = {
                                        deleteConfirmMessage = if (lang == Lang.EN) 
                                            "Are you sure you want to delete referrer ${person.fullName}? This will remove their record from referrals listing." 
                                            else "શું તમે ખરેખર રેફરર ${person.fullName} ને કાઢી નાખવા માંગો છો? આનાથી તેમની વિગત રેફરલ લિસ્ટમાંથી દૂર થઈ જશે."
                                        pendingDeleteAction = {
                                            viewModel.deleteReferralPerson(person.id)
                                        }
                                    }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = StatusColors.Critical)
                                    }
                                }
                            }
                        }

                        if (criticalRefereeCustomers.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFE4E6))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Critical Warning",
                                    tint = Color(0xFFE11D48),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🚨 Critical Dues: ${criticalRefereeCustomers.size} (" + criticalRefereeCustomers.joinToString { it.customerName } + ")",
                                    color = Color(0xFFBE123C),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Referred: ${refereeCustomers.size}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                Text(text = "Pending: ₹${pendS.toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = StatusColors.Critical))
                            }
                            TextButton(
                                onClick = { selectedProfileReferrer = person },
                                modifier = Modifier.testTag("view_ref_profile_btn_${person.id}")
                            ) {
                                Text("Profile →", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
    }

    if (showAddReferrerDialog) {
        AddReferrerDialog(
            lang = lang,
            onDismiss = { showAddReferrerDialog = false },
            onSave = { name, mobile, address, city, notes ->
                viewModel.addReferralPerson(name, mobile, address, city, notes)
                showAddReferrerDialog = false
            }
        )
    }

    pendingDeleteAction?.let { action ->
        DeleteMpinConfirmDialog(
            onConfirm = action,
            onDismiss = { pendingDeleteAction = null },
            lang = lang,
            viewModel = viewModel,
            message = deleteConfirmMessage
        )
    }
}

@Composable
fun AddReferrerDialog(
    lang: Lang,
    onDismiss: () -> Unit,
    onSave: (name: String, mobile: String, address: String, city: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(AppStrings.addReferrer(lang), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth().testTag("ref_name_input")
                )

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Mobile Phone") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address Area") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes/Supplier Context") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(AppStrings.cancel(lang)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotEmpty() && mobile.isNotEmpty()) {
                                onSave(name, mobile, address, city, notes)
                            }
                        },
                        modifier = Modifier.testTag("ref_save_btn")
                    ) {
                        Text(AppStrings.save(lang))
                    }
                }
            }
        }
    }
}


// ==========================================
// REFERRAL PROFILE SCREEN
// ==========================================
@Composable
fun ReferralProfile(
    referrer: ReferralPerson,
    viewModel: AppViewModel,
    lang: Lang,
    onBack: () -> Unit
) {
    val customers by viewModel.customersList.collectAsState()
    val referrals by viewModel.customerReferralsList.collectAsState()
    val dues by viewModel.duesList.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var showReferrerReminderDialog by remember { mutableStateOf(false) }

    // Referee customers referred by this person
    val refereeCustomers = remember(customers, referrals, referrer) {
        customers.filter { c ->
            referrals.any { r -> r.customerId == c.id && r.referrerName == referrer.fullName }
        }
    }

    // Dynamic metrics using the helper function
    val metrics = remember(referrer, customers, referrals, dues) {
        viewModel.getReferralMetricsForPerson(referrer.fullName)
    }

    val totalSales = remember(refereeCustomers) {
        refereeCustomers.sumOf { it.totalBillAmount }
    }

    if (showReferrerReminderDialog) {
        ReferrerReminderDialog(
            referrer = referrer,
            refereeCustomers = refereeCustomers,
            dues = dues,
            viewModel = viewModel,
            lang = lang,
            onDismiss = { showReferrerReminderDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("ref_profile_back_btn")) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (lang == Lang.EN) "Referral Profile" else "રેફરલ પ્રોફાઇલ",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
            )
        }

        // Referrer Contact Detail Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = referrer.fullName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = referrer.mobileNumber, style = MaterialTheme.typography.bodyMedium)
                }

                if (referrer.city.isNotEmpty() || referrer.address.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = listOfNotNull(referrer.address.takeIf { it.isNotEmpty() }, referrer.city.takeIf { it.isNotEmpty() }).joinToString(", "),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (referrer.notes.isNotEmpty()) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Text(
                        text = "Notes: ${referrer.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Action card for Report Export and Notification Center
        Card(
            modifier = Modifier.fillMaxWidth().testTag("referral_report_notification_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (lang == Lang.EN) "Referral Reports & Notifications" else "રેફરલ રિપોર્ટ્સ અને સૂચનાઓ",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (lang == Lang.EN) 
                        "Send a detailed statement including customer phone numbers, outstanding dues, dates, and invoice numbers." 
                        else "ગ્રાહકના ફોન નંબર, બાકી હપ્તો, તારીખ અને ઇન્વોઇસ નંબર દર્શાવતી સવિસ્તર વિગત રેફરલને મોકલો.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            showReferrerReminderDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        modifier = Modifier.weight(1f).height(40.dp).testTag("send_referrer_all_due_whatsapp")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (lang == Lang.EN) "Remind via WA" else "WA દ્વારા જણાવો", fontSize = 11.sp, color = Color.White)
                    }
                    OutlinedButton(
                        onClick = {
                            shareReferralCsvReport(context, referrer, refereeCustomers, dues)
                        },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f).height(40.dp).testTag("share_referrer_excel_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (lang == Lang.EN) "Share Excel (CSV)" else "એક્સેલ (CSV) શેર કરો", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Highlights/KPI Statistics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Count
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${refereeCustomers.size}", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black))
                    Text(text = "Referred", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            // Sales
            Card(
                modifier = Modifier.weight(1.2f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "₹${totalSales.toInt()}", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black), color = MaterialTheme.colorScheme.primary)
                    Text(text = "Total Sales", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            // Pending
            Card(
                modifier = Modifier.weight(1.3f),
                colors = CardDefaults.cardColors(containerColor = if (metrics.criticalCount > 0) Color(0xFFFFF1F2) else MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, if (metrics.criticalCount > 0) Color(0xFFE11D48).copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "₹${metrics.pendingAmount.toInt()}", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black), color = if (metrics.criticalCount > 0) Color(0xFFE11D48) else StatusColors.Critical)
                    Text(text = "Pending Dues", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // List Header
        Text(
            text = if (lang == Lang.EN) "Referred Customer Accounts" else "ભલામણ કરેલ ગ્રાહકોની યાદી",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        if (refereeCustomers.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(text = "No referred customers recorded yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            refereeCustomers.forEach { customer ->
                // Calculate their pending dues from linked dues table
                val custDues = dues.filter { it.customerId == customer.id && it.dueStatus != "Paid" && it.dueAmount > 0.0 }
                val calculatedPendingAmt = custDues.sumOf { it.dueAmount }

                val earliestDue = custDues.minByOrNull { it.dueDate }
                val dueDateVal = earliestDue?.dueDate ?: customer.purchaseDate

                // Determine if this referee customer has critical overdue (>= 60 days)
                val isCrit = custDues.any { viewModel.repository.getDaysDifference(it.dueDate) >= 60 }

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("ref_profile_customer_card_${customer.id}"),
                    border = BorderStroke(1.dp, if (isCrit) Color(0xFFE11D48).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = if (isCrit) Color(0xFFFFF1F2).copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = customer.customerName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    if (isCrit) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFFFE4E6))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "60+ OVERDUE",
                                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFBE123C), fontWeight = FontWeight.Black)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "Mobile: ${customer.mobileNumber} • ${customer.cityVillage}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // High-contrast Status tag
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (calculatedPendingAmt > 0) StatusColors.getStatusBgColor("Pending") else StatusColors.getStatusBgColor("Paid"))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (calculatedPendingAmt > 0) "PENDING" else "PAID",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (calculatedPendingAmt > 0) StatusColors.getStatusColor("Pending") else StatusColors.getStatusColor("Paid")
                                    )
                                )
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Financial stats for this customer
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Total Bill: ₹${customer.totalBillAmount.toInt()}", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = "Pending Dues: ₹${calculatedPendingAmt.toInt()}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = if (calculatedPendingAmt > 0) StatusColors.Critical else StatusColors.Paid
                            )
                        }

                        if (custDues.isNotEmpty()) {
                            Text(text = "Outstanding Installments:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            custDues.forEach { due ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(text = "• ₹${due.dueAmount.toInt()} due by ${due.dueDate}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        if (due.invoiceNumber.trim().isNotEmpty()) {
                                            Text(text = "(Inv: #${due.invoiceNumber})", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary))
                                        }
                                    }
                                    Text(text = due.dueStatus, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = if (due.dueStatus == "Critical") Color(0xFFE11D48) else Color.DarkGray)
                                }
                            }
                        } else {
                            Text(text = "No outstanding installments pending.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF059669))
                        }

                        if (calculatedPendingAmt > 0) {
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. WhatsApp Customer directly
                                Button(
                                    onClick = {
                                        viewModel.whatsAppReminderDialogData.value = WhatsAppReminderData(
                                            customerName = customer.customerName,
                                            mobileNumber = customer.mobileNumber,
                                            amount = calculatedPendingAmt,
                                            dueDate = dueDateVal
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                    modifier = Modifier.weight(1f).height(38.dp).testTag("whatsapp_ref_cust_btn_${customer.id}"),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Send, contentDescription = "WhatsApp Customer", modifier = Modifier.size(14.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (lang == Lang.EN) "Remind Customer" else "ગ્રાહકને જણાવો", fontSize = 11.sp, color = Color.White)
                                }

                                // 2. Remind Referrer about this specific customer's due
                                Button(
                                    onClick = {
                                        val msg = if (lang == Lang.EN) {
                                            "Hello ${referrer.fullName}, regarding the customer you referred, ${customer.customerName}: they have a pending due amount of ₹${calculatedPendingAmt.toInt()}. Could you please request them to clear their due of ₹${calculatedPendingAmt.toInt()} at Phone World? Thank you."
                                        } else {
                                            "નમસ્તે ${referrer.fullName}, તમે રેફર કરાયેલ ગ્રાહક ${customer.customerName} નો બાકી હપ્તો ₹${calculatedPendingAmt.toInt()} છે. કૃપા કરીને તેમને વહેલી તકે ચુકવણી કરવા જણાવશો. આભાર, ફોન વર્લ્ડ."
                                        }

                                        viewModel.logActivity("ReferralReminder", "Reminded Referrer ${referrer.fullName} about ${customer.customerName}'s due")

                                        try {
                                            val uri = "https://api.whatsapp.com/send?phone=${referrer.mobileNumber}&text=${android.net.Uri.encode(msg)}"
                                            context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(uri)).apply { flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK })
                                        } catch (e: Exception) {}
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.weight(1f).height(38.dp).testTag("remind_referrer_for_cust_btn_${customer.id}"),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Campaign, contentDescription = "Remind Referrer", modifier = Modifier.size(14.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (lang == Lang.EN) "Remind Referrer" else "રેફરરને રીમાઇન્ડ કરો", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// SCREEN 5: STAFF COLLECTION LEADERBOARD
// ==========================================
@Composable
fun StaffScreen(viewModel: AppViewModel, lang: Lang) {
    val staff by viewModel.staffMemberList.collectAsState()
    val payments by viewModel.paymentsList.collectAsState()
    val customers by viewModel.customersList.collectAsState()
    val rPersons by viewModel.referralPersonsList.collectAsState()
    val followups by viewModel.followupsList.collectAsState()
    val currentRole by viewModel.currentRoleState.collectAsState()

    var showAddStaffDialog by remember { mutableStateOf(false) }
    var selectedStaffForEdit by remember { mutableStateOf<StaffMember?>(null) }
    var pendingDeleteAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var deleteConfirmMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = AppStrings.staffTitle(lang),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black)
            )
            
            if (currentRole == "Owner") {
                Button(
                    onClick = {
                        selectedStaffForEdit = null
                        showAddStaffDialog = true
                    },
                    modifier = Modifier.testTag("add_staff_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(AppStrings.addStaff(lang))
                }
            }
        }

        // Leaderboard Scorecard Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Staff Achievement Board", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                
                // Top Collector Calculations
                val collectorLeader = payments.groupBy { it.collectedBy }
                    .maxByOrNull { ent -> ent.value.sumOf { it.amountPaid } }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "🏆 Top Collector:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text(
                        text = if (collectorLeader != null) "${collectorLeader.key} (₹${collectorLeader.value.sumOf { it.amountPaid }.toInt()})" else "None",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                // Top Follow-up Performer
                val followupLeader = followups.groupBy { it.staffName }
                    .maxByOrNull { ent -> ent.value.size }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "📞 Top Follow-Up Caller:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text(
                        text = if (followupLeader != null) "${followupLeader.key} (${followupLeader.value.size} Calls)" else "None",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // Staff listing
        staff.forEach { member ->
            val collectedSum = payments.filter { it.collectedBy == member.name }.sumOf { it.amountPaid }
            val followDone = followups.count { it.staffName == member.name }

            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(
                    containerColor = if (member.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = member.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (member.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (member.role == "Owner") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(text = member.role, style = MaterialTheme.typography.labelSmall)
                            }
                            
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (member.isActive) Color(0xFFECFDF5) else Color(0xFFFEF2F2))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (member.isActive) "Active" else "Disabled",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (member.isActive) Color(0xFF059669) else Color(0xFFEF4444),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                        
                        Text(text = "Phone: ${member.mobile}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (member.email.isNotEmpty()) {
                            Text(text = "Email: ${member.email}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(text = "Collected: ₹${collectedSum.toInt()}", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = StatusColors.Paid))
                            Text(text = "Followups: $followDone", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = StatusColors.PartialPaid))
                        }
                    }

                    if (currentRole == "Owner") {
                        Row {
                            IconButton(onClick = {
                                selectedStaffForEdit = member
                                showAddStaffDialog = true
                            }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Staff", tint = MaterialTheme.colorScheme.primary)
                            }
                            
                            if (member.role != "Owner") {
                                IconButton(onClick = {
                                    deleteConfirmMessage = if (lang == Lang.EN) 
                                        "Are you sure you want to delete staff member ${member.name}? All historical collection entries logged under their name will be retained, but they will not be able to log in." 
                                        else "શું તમે ખરેખર સ્ટાફ મેમ્બર ${member.name} ને કાઢી નાખવા માંગો છો? તેમના નામ હેઠળ સાચવેલા તમામ કલેક્શન આંકડા રહેશે પણ તેઓ લોગિન નહીં કરી શકે."
                                    pendingDeleteAction = {
                                        viewModel.deleteStaffMember(member.id)
                                    }
                                }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = StatusColors.Critical)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddStaffDialog) {
        AddStaffDialog(
            lang = lang,
            editingStaff = selectedStaffForEdit,
            onDismiss = { showAddStaffDialog = false; selectedStaffForEdit = null },
            onSave = { name, mobile, email, password, role, active, id ->
                viewModel.addStaffMember(name, mobile, email, password, role, active, id)
                showAddStaffDialog = false
                selectedStaffForEdit = null
            }
        )
    }

    pendingDeleteAction?.let { action ->
        DeleteMpinConfirmDialog(
            onConfirm = action,
            onDismiss = { pendingDeleteAction = null },
            lang = lang,
            viewModel = viewModel,
            message = deleteConfirmMessage
        )
    }
}

@Composable
fun AddStaffDialog(
    lang: Lang,
    editingStaff: StaffMember?,
    onDismiss: () -> Unit,
    onSave: (name: String, mobile: String, email: String, password: String, role: String, active: Boolean, id: Int) -> Unit
) {
    var name by remember { mutableStateOf(editingStaff?.name ?: "") }
    var mobile by remember { mutableStateOf(editingStaff?.mobile ?: "") }
    var email by remember { mutableStateOf(editingStaff?.email ?: "") }
    var password by remember { mutableStateOf(editingStaff?.password ?: "") }
    var selectedRole by remember { mutableStateOf(editingStaff?.role ?: "Staff") }
    var isActive by remember { mutableStateOf(editingStaff?.isActive ?: true) }

    val isEditMode = editingStaff != null

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isEditMode) "Modify Staff Credentials" else AppStrings.addStaff(lang),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth().testTag("staff_name_input")
                )

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Mobile Phone") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password Connection Code") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = "Assign Role", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectedRole == "Staff", onClick = { selectedRole = "Staff" })
                    Text("Staff Member")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = selectedRole == "Owner", onClick = { selectedRole = "Owner" })
                    Text("Owner Admin")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isActive, onCheckedChange = { isActive = it })
                    Text("Activate Account Credentials")
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(AppStrings.cancel(lang)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotEmpty() && mobile.isNotEmpty()) {
                                onSave(name, mobile, email, password, selectedRole, isActive, editingStaff?.id ?: 0)
                            }
                        },
                        modifier = Modifier.testTag("staff_save_btn")
                    ) {
                        Text(AppStrings.save(lang))
                    }
                }
            }
        }
    }
}


// ==========================================
// SCREEN 6: SYSTEM REPORTS & SETTLEMENTS LEDGER
// ==========================================
@Composable
fun ReportsScreen(viewModel: AppViewModel, lang: Lang) {
    val payments by viewModel.paymentsList.collectAsState()
    val followups by viewModel.followupsList.collectAsState()
    val reminders by viewModel.reminderLogsList.collectAsState()
    val dues by viewModel.duesList.collectAsState()
    val customers by viewModel.customersList.collectAsState()
    val staffList by viewModel.staffMemberList.collectAsState()
    val customerReferrals by viewModel.customerReferralsList.collectAsState()
    
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf("Payments") } // "Payments", "Followups", "Reminders", "Dues Aging", "Referrals & Staff"
    var agingFilterDays by remember { mutableStateOf(30) } // 30, 60, 180
    var selectedStaffName by remember { mutableStateOf("All Staff") }

    var showTemplateConfigDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = AppStrings.reports(lang),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black)
            )
            
            // Configure Templates (Owner Only button)
            Button(
                onClick = { showTemplateConfigDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.testTag("config_template_btn")
            ) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (lang == Lang.EN) "Templates" else "ટેમ્પલેટ્સ", fontSize = 11.sp, color = Color.White)
            }
        }

        // Segmented Tabs with horizontal scrolling for compactness on mobile views
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val tabs = listOf("Payments", "Followups", "Reminders", "Dues Aging", "Referrals & Staff")
            tabs.forEach { tab ->
                val selected = activeTab == tab
                val localizedTab = when (tab) {
                    "Payments" -> if (lang == Lang.EN) "Payments" else "ચુકવણીઓ"
                    "Followups" -> if (lang == Lang.EN) "Followups" else "ફોલોઅપ"
                    "Reminders" -> if (lang == Lang.EN) "Reminders" else "રિમાઇન્ડર્સ"
                    "Dues Aging" -> if (lang == Lang.EN) "Dues Aging" else "હપ્તા બાકી"
                    "Referrals & Staff" -> if (lang == Lang.EN) "Referrals & Staff" else "રેફરલ્સ અને સ્ટાફ"
                    else -> tab
                }
                Button(
                    onClick = { activeTab = tab },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.height(38.dp).testTag("tab_$tab")
                ) {
                    Text(localizedTab, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Export Actions row (Only for standard logging tabs)
        if (activeTab != "Referrals & Staff") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Live $activeTab records table", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                
                Button(
                    onClick = {
                        viewModel.logActivity("Export", "Exported $activeTab Report successfully.")
                    },
                    modifier = Modifier.height(34.dp).testTag("export_btn")
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(AppStrings.exportReport(lang), fontSize = 10.sp)
                }
            }
        }

        // Audit logs output based on selected tab
        when (activeTab) {
            "Payments" -> {
                if (payments.isEmpty()) {
                    Text("No collection logs recorded.")
                } else {
                    payments.forEach { p ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = p.customerName, fontWeight = FontWeight.Bold)
                                    Text(text = "₹${p.amountPaid.toInt()}", fontWeight = FontWeight.Black, color = StatusColors.Paid)
                                }
                                Text(text = "Date: ${p.paymentDate} • via ${p.paymentMode}", style = MaterialTheme.typography.bodySmall)
                                Text(text = "Collected by: ${p.collectedBy}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }

            "Followups" -> {
                if (followups.isEmpty()) {
                    Text("No followups recorded in database.")
                } else {
                    followups.forEach { f ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = f.customerName, fontWeight = FontWeight.Bold)
                                    Text(text = f.status, color = StatusColors.getStatusColor(f.status), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Text(text = "Notes: ${f.notes}", style = MaterialTheme.typography.bodySmall)
                                Text(text = "Scheduled: ${f.nextFollowUpDate}", style = MaterialTheme.typography.bodySmall)
                                Text(text = "Logged by: ${f.staffName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }

            "Reminders" -> {
                if (reminders.isEmpty()) {
                    Text("No WhatsApp reminder log dispatches recorded.")
                } else {
                    reminders.forEach { r ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = r.customerName, fontWeight = FontWeight.Bold)
                                    Text(text = "Disp: ${r.sentDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(text = r.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                Text(text = "Dispatched by: ${r.sentBy}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }

            "Dues Aging" -> {
                val filteredDues = dues.filter { d ->
                    d.dueStatus != "Paid" && calculateDaysPassed(d.dueDate) >= agingFilterDays
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (lang == Lang.EN) "Filter Installments by Pending Days" else "બાકી દિવસો દ્વારા બાકી રકમ ફિલ્ટર કરો",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(30, 60, 180).forEach { days ->
                            val selected = agingFilterDays == days
                            FilterChip(
                                selected = selected,
                                onClick = { agingFilterDays = days },
                                label = { Text(if (lang == Lang.EN) "> $days Days" else "> $days દિવસ") },
                                modifier = Modifier.weight(1f).testTag("aging_chip_$days")
                            )
                        }
                    }

                    // Direct Interactive Excel Download card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (lang == Lang.EN) "EXCEL EXPORT (CSV)" else "એક્સેલ નિકાસ (CSV)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = if (lang == Lang.EN) 
                                    "Found ${filteredDues.size} overdue items > $agingFilterDays days." 
                                    else "$agingFilterDays દિવસથી વધુના ${filteredDues.size} બાકી મળ્યા.",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                exportAgingDuesToCsv(context, dues, customers, agingFilterDays)
                                viewModel.logActivity("Aging Export", "Exported dues > $agingFilterDays days to Excel CSV.")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("export_aging_excel_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Excel (CSV)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (filteredDues.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (lang == Lang.EN) 
                                    "Awesome! No dues are pending for more than $agingFilterDays days." 
                                    else "બધું બરાબર છે! $agingFilterDays દિવસથી વધુનો કોઈ હપ્તો બાકી નથી.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        filteredDues.forEach { d ->
                            val parsedDays = calculateDaysPassed(d.dueDate)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = d.customerName, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "₹${d.dueAmount.toInt()}", 
                                            fontWeight = FontWeight.Black, 
                                            color = if (parsedDays >= 60) StatusColors.Critical else StatusColors.Overdue
                                        )
                                    }
                                    
                                    val cust = customers.find { it.id == d.customerId }
                                    cust?.let { c ->
                                        Text(
                                            text = "${if (lang == Lang.EN) "Phone" else "ફોન"}: ${c.mobileNumber} • ${if (lang == Lang.EN) "Product" else "ઉત્પાદન"}: ${c.productPurchased}", 
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(), 
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Due Date: ${d.dueDate}", 
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = if (lang == Lang.EN) "$parsedDays days passed" else "$parsedDays દિવસો વીતી ગયા", 
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                            color = if (parsedDays >= 60) StatusColors.Critical else StatusColors.Overdue
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "Referrals & Staff" -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Card 0: Staff Referral Leaderboard
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("staff_leaderboard_card"),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFEAB308), // Gold
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (lang == Lang.EN) "Staff Referral Leaderboard" else "સ્ટાફ રેફરલ લીડરબોર્ડ",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (lang == Lang.EN) "RANKINGS" else "રેન્કિંગ્સ",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }

                            Text(
                                text = if (lang == Lang.EN)
                                    "A real-time overview of our top-performing staff and admins based on the total active customer accounts they referred to the CRM."
                                    else "ગ્રાહકોને CRM માં લાવવાના આધારે આપણા સ્ટાફ સભ્યો અને એડમિન્સનું રીઅલ-ટાઇમ પ્રદર્શન પત્રક.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Leaderboard entries computation
                            val leaderboardEntries = remember(staffList, customerReferrals) {
                                val allStaffNames = (
                                    staffList.map { it.name } + 
                                    customerReferrals.filter { it.referredByType.equals("Staff", ignoreCase = true) || it.referredByType.equals("Owner", ignoreCase = true) }.map { it.referrerName } +
                                    listOf("Rais Memon (Owner)", "Rais Memon")
                                ).distinct().filter { it.isNotEmpty() && !it.equals("Direct", ignoreCase = true) }

                                allStaffNames.map { name ->
                                    val count = customerReferrals.count { 
                                        (it.referredByType.equals("Staff", ignoreCase = true) || it.referredByType.equals("Owner", ignoreCase = true)) && 
                                        it.referrerName.equals(name, ignoreCase = true)
                                    }
                                    name to count
                                }.sortedByDescending { it.second }
                            }

                            if (leaderboardEntries.isEmpty() || leaderboardEntries.all { it.second == 0 }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (lang == Lang.EN) "No staff referral entries found yet." else "હજી સુધી કોઈ સ્ટાફ રેફરલ એન્ટ્રી મળી નથી.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                val maxReferrals = leaderboardEntries.maxOf { it.second }.toFloat().coerceAtLeast(1f)
                                
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    leaderboardEntries.take(10).forEachIndexed { index, (name, count) ->
                                        val rank = index + 1
                                        val rankColor = when (rank) {
                                            1 -> Color(0xFFEAB308) // Gold
                                            2 -> Color(0xFF94A3B8) // Silver
                                            3 -> Color(0xFFCD7F32) // Bronze
                                            else -> MaterialTheme.colorScheme.outline
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (rank <= 3) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                    else Color.Transparent
                                                )
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Rank Badge
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(rankColor)
                                                    .padding(2.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "$rank",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (rank <= 3) Color.White else MaterialTheme.colorScheme.onSurface
                                                    )
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            // Name and progress bar
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = name,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                // Relative Linear Progress Indicator
                                                val progress = count / maxReferrals
                                                LinearProgressIndicator(
                                                    progress = progress,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(6.dp)
                                                        .clip(RoundedCornerShape(3.dp)),
                                                    color = if (rank == 1) Color(0xFFEAB308) else MaterialTheme.colorScheme.primary,
                                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(16.dp))

                                            // Count Badge
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = if (lang == Lang.EN) "$count Refs" else "$count રેફરલ્સ",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.Black,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Card 1: Staff Member referred customers report
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("staff_referred_card"),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (lang == Lang.EN) "Staff Referral Excel Reports" else "સ્ટાફ રેફરલ એક્સેલ રિપોર્ટ્સ",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                text = if (lang == Lang.EN)
                                    "Select any staff member or owner admin below to export an Excel sheet listing all customers registered as their referrals."
                                    else "કોઈપણ સ્ટાફ સભ્ય અથવા ઓનર એડમિન પસંદ કરો અને તેમના દ્વારા લાવવામાં આવેલા ગ્રાહકોની વિગતવાર યાદી એક્સેલ ફાઈલ રૂપે એક્સપોર્ટ કરો.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = if (lang == Lang.EN) "Choose Staff/Owner:" else "સ્ટાફ/ઓનર પસંદ કરો:",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )

                            val staffOptions = remember(staffList) { 
                                listOf("All Staff", "Rais Memon (Owner)") + staffList.map { it.name }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                staffOptions.distinct().forEach { sName ->
                                    val isSelected = selectedStaffName == sName
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedStaffName = sName },
                                        label = { Text(sName, fontSize = 11.sp) },
                                        modifier = Modifier.testTag("staff_chip_$sName")
                                    )
                                }
                            }

                            val matchedReferrals = remember(selectedStaffName, customerReferrals) {
                                if (selectedStaffName == "All Staff" || selectedStaffName == "બધા સ્ટાફ") {
                                    customerReferrals.filter { it.referredByType.equals("Staff", ignoreCase = true) || it.referredByType.equals("Owner", ignoreCase = true) }
                                } else {
                                    customerReferrals.filter { 
                                        (it.referredByType.equals("Staff", ignoreCase = true) || it.referredByType.equals("Owner", ignoreCase = true)) && 
                                        it.referrerName.equals(selectedStaffName, ignoreCase = true)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (lang == Lang.EN) 
                                        "Found: ${matchedReferrals.size} customers" 
                                        else "મળ્યા: ${matchedReferrals.size} ગ્રાહકો",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )

                                Button(
                                    onClick = {
                                        exportStaffReferralsToCsv(context, selectedStaffName, customers, customerReferrals, dues)
                                        viewModel.logActivity("Staff Referrals Export", "Exported referrals report for staff $selectedStaffName to Excel CSV.")
                                    },
                                    modifier = Modifier.testTag("export_staff_referrals_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (lang == Lang.EN) "Export CSV" else "નિકાસ CSV", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Card 2: Referred Customer Outstanding Dues
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("referred_dues_card"),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (lang == Lang.EN) "Referred Customer Total Dues" else "રેફરલ ગ્રાહકોના બાકી હપ્તા",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            Text(
                                text = if (lang == Lang.EN)
                                    "Export a complete master list of all referred customers across all sources (Staff, Owners, External Referrers) with their outstanding/pending dues details."
                                    else "તમામ સ્રોતો (સ્ટાફ, માલિકો, બાહ્ય સંદર્ભકર્તાઓ) પરના તમામ રેફરલ ગ્રાહકોની તેમના બાકી હપ્તાની વિગતવાર યાદી એક્સેલ નિકાસ કરો.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Let's count how many total referrals are waiting/due
                            val totalReferredDueCount = remember(customerReferrals, customers, dues) {
                                customerReferrals.count { ref ->
                                    val cust = customers.find { it.id == ref.customerId }
                                    cust != null && dues.any { it.customerId == cust.id && it.dueStatus != "Paid" }
                                }
                            }

                            val totalReferredPendingAmount = remember(customerReferrals, customers, dues) {
                                customerReferrals.sumOf { ref ->
                                    val cust = customers.find { it.id == ref.customerId }
                                    if (cust != null) {
                                        dues.filter { it.customerId == cust.id && it.dueStatus != "Paid" }.sumOf { it.dueAmount }
                                    } else 0.0
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (lang == Lang.EN) "Pending Referral Accounts: $totalReferredDueCount" else "બાકી ખાતા: $totalReferredDueCount",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Total Due: ₹${totalReferredPendingAmount.toInt()}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = StatusColors.Overdue)
                                )
                            }

                            Button(
                                onClick = {
                                    exportReferralsDueToCsv(context, customers, customerReferrals, dues)
                                    viewModel.logActivity("Referred Dues Export", "Exported referred customer outstanding dues report successfully.")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.fillMaxWidth().testTag("export_referred_dues_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (lang == Lang.EN) "Export Referred Dues Excel (CSV)" else "રેફરલ બાકી પત્રક એક્સેલ નિકાસ (CSV)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTemplateConfigDialog) {
        TemplateConfigDialog(
            viewModel = viewModel,
            lang = lang,
            onDismiss = { showTemplateConfigDialog = false }
        )
    }
}

// Dialog Component for template management
@Composable
fun TemplateConfigDialog(viewModel: AppViewModel, lang: Lang, onDismiss: () -> Unit) {
    val engTemp by viewModel.englishTemplateState.collectAsState()
    val gujTemp by viewModel.gujaratiTemplateState.collectAsState()

    var activeEngText by remember { mutableStateOf(engTemp) }
    var activeGujText by remember { mutableStateOf(gujTemp) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (lang == Lang.EN) "Configure Reminder Templates" else "રિમાઇન્ડર ટેમ્પલેટ્સ રૂપરેખાંકિત કરો",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Placeholder Variables:\n{customer_name}, {due_amount}, {due_date}, {shop_name}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    lineHeight = 12.sp
                )

                OutlinedTextField(
                    value = activeEngText,
                    onValueChange = { activeEngText = it },
                    label = { Text(AppStrings.englishTemplateTitle(lang)) },
                    modifier = Modifier.fillMaxWidth().height(120.dp).testTag("eng_template_field")
                )

                OutlinedTextField(
                    value = activeGujText,
                    onValueChange = { activeGujText = it },
                    label = { Text(AppStrings.gujaratiTemplateTitle(lang)) },
                    modifier = Modifier.fillMaxWidth().height(120.dp).testTag("guj_template_field")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(AppStrings.cancel(lang)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.englishTemplateState.value = activeEngText
                            viewModel.gujaratiTemplateState.value = activeGujText
                            onDismiss()
                        },
                        modifier = Modifier.testTag("template_save_btn")
                    ) {
                        Text(AppStrings.save(lang))
                    }
                }
            }
        }
    }
}

data class WhatsAppReminderData(
    val customerName: String,
    val mobileNumber: String,
    val amount: Double,
    val dueDate: String
)

@Composable
fun WhatsAppReminderDialog(
    data: WhatsAppReminderData,
    viewModel: AppViewModel,
    lang: Lang,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val englishTemplate by viewModel.englishTemplateState.collectAsState()
    val gujaratiTemplate by viewModel.gujaratiTemplateState.collectAsState()
    
    var selectedLanguage by remember { mutableStateOf("English") }
    
    // Compute customized message based on selected template language
    val templateText = if (selectedLanguage == "English") englishTemplate else gujaratiTemplate
    val populatedMessage = templateText
        .replace("{customer_name}", data.customerName)
        .replace("{due_amount}", data.amount.toInt().toString())
        .replace("{due_date}", data.dueDate)
        .replace("{shop_name}", "Phone World")
        
    var editableMessage by remember(populatedMessage) { mutableStateOf(populatedMessage) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (lang == Lang.EN) "Send WhatsApp Reminder" else "વોટ્સએપ રીમાઇન્ડર મોકલો",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = if (lang == Lang.EN) "Select Template Language:" else "ટેમ્પલેટ ભાષા પસંદ કરો:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedLanguage == "English",
                        onClick = { selectedLanguage = "English" },
                        label = { Text("English", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedLanguage == "Gujarati",
                        onClick = { selectedLanguage = "Gujarati" },
                        label = { Text("ગુજરાતી (Gujarati)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.weight(1.2f)
                    )
                }
                
                Text(
                    text = if (lang == Lang.EN) "Message Preview (Editable):" else "સંદેશ પૂર્વાવલોકન (સુધારી શકાય તેવું):",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                OutlinedTextField(
                    value = editableMessage,
                    onValueChange = { editableMessage = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("whatsapp_editable_message"),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    maxLines = 6
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(AppStrings.cancel(lang))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            // Log the activity to local/network DB
                            viewModel.sendWhatsAppReminder(0, data.customerName, data.amount, data.dueDate, selectedLanguage == "Gujarati")
                            
                            // Open actual WhatsApp intent!
                            try {
                                val uri = "https://api.whatsapp.com/send?phone=${data.mobileNumber}&text=${android.net.Uri.encode(editableMessage)}"
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse(uri)
                                ).apply {
                                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        modifier = Modifier.testTag("whatsapp_send_msg_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (lang == Lang.EN) "Send via WhatsApp" else "વોટ્સએપ દ્વારા મોકલો", color = Color.White)
                    }
                }
            }
        }
    }
}

fun shareReferralCsvReport(
    context: android.content.Context,
    referrer: ReferralPerson,
    customers: List<Customer>,
    dues: List<Due>
) {
    try {
        // Construct CSV content (RFC 4180 compliant)
        val csvHeader = "Customer Name,Phone Number,Due Amount,Due Date,Invoice Number,Status,Product Purchased\n"
        val csvBody = StringBuilder()
        
        customers.forEach { customer ->
            val custDues = dues.filter { it.customerId == customer.id && it.dueStatus != "Paid" && it.dueAmount > 0.0 }
            val totalPending = custDues.sumOf { it.dueAmount }
            val earliestDue = custDues.minByOrNull { it.dueDate }
            val dueDateVal = earliestDue?.dueDate ?: customer.purchaseDate
            val invoiceNoVal = customer.invoiceNumber.ifEmpty { "N/A" }
            
            // Format quotes to escape commas and special characters
            val nameEscaped = customer.customerName.replace("\"", "\"\"")
            val phoneEscaped = customer.mobileNumber.replace("\"", "\"\"")
            val invoiceEscaped = invoiceNoVal.replace("\"", "\"\"")
            val statusEscaped = if (totalPending > 0) "Pending" else "Paid"
            val productEscaped = customer.productPurchased.replace("\"", "\"\"")
            
            csvBody.append("\"$nameEscaped\",\"$phoneEscaped\",$totalPending,\"$dueDateVal\",\"$invoiceEscaped\",\"$statusEscaped\",\"$productEscaped\"\n")
        }
        
        val fullCsv = csvHeader + csvBody.toString()
        
        // Write to CACHE directory
        val fileName = "Referrals_${referrer.fullName.replace(" ", "_")}_Report.csv"
        val file = java.io.File(context.cacheDir, fileName)
        file.writeText(fullCsv, Charsets.UTF_8)
        
        // Get FileProvider URI
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "com.example.fileprovider",
            file
        )
        
        // Launch Android Sharesheet
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Referrals Report - ${referrer.fullName}")
            putExtra(android.content.Intent.EXTRA_TEXT, "Attached is the referral customers status spreadsheet report for ${referrer.fullName}.")
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        context.startActivity(android.content.Intent.createChooser(intent, "Share Referrals Report"))
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Error sharing report: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun ReferrerReminderDialog(
    referrer: ReferralPerson,
    refereeCustomers: List<Customer>,
    dues: List<Due>,
    viewModel: AppViewModel,
    lang: Lang,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedLanguage by remember { mutableStateOf("English") }
    
    // Compute total outstanding
    val totalPending = refereeCustomers.sumOf { customer ->
        val custDues = dues.filter { it.customerId == customer.id && it.dueStatus != "Paid" && it.dueAmount > 0.0 }
        custDues.sumOf { it.dueAmount }
    }
    
    // Build initial message based on selected language
    val populatedMessage = remember(referrer, refereeCustomers, dues, selectedLanguage) {
        val sb = StringBuilder()
        if (selectedLanguage == "English") {
            sb.append("Hello ${referrer.fullName},\n\n")
            sb.append("Here is the update regarding the customers you referred to Phone World:\n\n")
            
            refereeCustomers.forEachIndexed { index, customer ->
                val custDues = dues.filter { it.customerId == customer.id && it.dueStatus != "Paid" && it.dueAmount > 0.0 }
                val totalPendingCust = custDues.sumOf { it.dueAmount }
                val earliestDue = custDues.minByOrNull { it.dueDate }
                val dueDateVal = earliestDue?.dueDate ?: customer.purchaseDate
                val invoiceNoVal = customer.invoiceNumber.ifEmpty { "N/A" }
                
                sb.append("${index + 1}. ${customer.customerName}\n")
                sb.append("   • Phone: ${customer.mobileNumber}\n")
                sb.append("   • Due: ₹${totalPendingCust.toInt()}\n")
                sb.append("   • Date: $dueDateVal\n")
                sb.append("   • Inv No: $invoiceNoVal\n")
                if (customer.productPurchased.isNotEmpty()) {
                    sb.append("   • Item: ${customer.productPurchased}\n")
                }
                sb.append("\n")
            }
            
            sb.append("Total Outstandings: ₹${totalPending.toInt()}\n\n")
            sb.append("Could you please assist them in clearing their pending dues? Thank you!\nPhone World")
        } else {
            sb.append("નમસ્તે ${referrer.fullName},\n\n")
            sb.append("તમારા દ્વારા રેફર કરાયેલા ગ્રાહકોના બાકી હપ્તાની સવિસ્તર વિગત નીચે મુજબ છે:\n\n")
            
            refereeCustomers.forEachIndexed { index, customer ->
                val custDues = dues.filter { it.customerId == customer.id && it.dueStatus != "Paid" && it.dueAmount > 0.0 }
                val totalPendingCust = custDues.sumOf { it.dueAmount }
                val earliestDue = custDues.minByOrNull { it.dueDate }
                val dueDateVal = earliestDue?.dueDate ?: customer.purchaseDate
                val invoiceNoVal = customer.invoiceNumber.ifEmpty { "નથી" }
                
                // Gujarati numbers helper
                val indexStr = (index + 1).toString()
                    .replace('1', '૧')
                    .replace('2', '૨')
                    .replace('3', '૩')
                    .replace('4', '૪')
                    .replace('5', '૫')
                    .replace('6', '૬')
                    .replace('7', '૭')
                    .replace('8', '૮')
                    .replace('9', '૯')
                    .replace('0', '૦')
                
                sb.append("$indexStr. ${customer.customerName}\n")
                sb.append("   • ફોન: ${customer.mobileNumber}\n")
                sb.append("   • બાકી હપ્તો: ₹${totalPendingCust.toInt()}\n")
                sb.append("   • તારીખ: $dueDateVal\n")
                sb.append("   • ઇન્વોઇસ: $invoiceNoVal\n")
                if (customer.productPurchased.isNotEmpty()) {
                    sb.append("   • ઉપકરણ: ${customer.productPurchased}\n")
                }
                sb.append("\n")
            }
            
            sb.append("કુલ બાકી રકમ: ₹${totalPending.toInt()}\n\n")
            sb.append("કૃપા કરીને તેમને વહેલી તકે હપ્તો ચૂકવવા જણાવવા વિનંતી છે.\nઆભાર, ફોન વર્લ્ડ")
        }
        sb.toString()
    }
    
    var editableMessage by remember(populatedMessage) { mutableStateOf(populatedMessage) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (lang == Lang.EN) "Referrer Statement & Reminder" else "રેફરર સ્ટેટમેન્ટ અને રીમાઇન્ડર",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = if (lang == Lang.EN) "Select Statement Language:" else "સ્ટેટમેન્ટ ભાષા પસંદ કરો:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedLanguage == "English",
                        onClick = { selectedLanguage = "English" },
                        label = { Text("English", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedLanguage == "Gujarati",
                        onClick = { selectedLanguage = "Gujarati" },
                        label = { Text("ગુજરાતી (Gujarati)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.weight(1.2f)
                    )
                }
                
                Text(
                    text = if (lang == Lang.EN) "Message Content (Editable):" else "સંદેશ વિગત (સુધારી શકાય તેવું):",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                OutlinedTextField(
                    value = editableMessage,
                    onValueChange = { editableMessage = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .testTag("referrer_editable_statement"),
                    textStyle = MaterialTheme.typography.bodySmall,
                    maxLines = 15
                )
                
                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            shareReferralCsvReport(context, referrer, refereeCustomers, dues)
                        },
                        modifier = Modifier.weight(1f).testTag("referrer_share_csv_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (lang == Lang.EN) "Share CSV" else "CSV શેર", fontSize = 11.sp)
                    }
                    
                    Button(
                        onClick = {
                            // Log the activity to local/network DB
                            viewModel.sendReferralWhatsAppReminder(referrer.id, referrer.fullName, editableMessage)
                            
                            // Open actual WhatsApp intent!
                            try {
                                val uri = "https://api.whatsapp.com/send?phone=${referrer.mobileNumber}&text=${android.net.Uri.encode(editableMessage)}"
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse(uri)
                                ).apply {
                                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        modifier = Modifier.weight(1.2f).testTag("referrer_send_wa_button")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(14.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (lang == Lang.EN) "Send via WA" else "WA દ્વારા મોકલો", fontSize = 11.sp, color = Color.White)
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(AppStrings.cancel(lang))
                }
            }
        }
    }
}

// ==========================================
// OUTSTANDING EMERGENCY CRITICAL ZONE SCREEN
// ==========================================
@Composable
fun CriticalZoneScreen(viewModel: AppViewModel, lang: Lang) {
    val customers by viewModel.customersList.collectAsState()
    val dues by viewModel.duesList.collectAsState()
    val followups by viewModel.followupsList.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var selectedDueToCollect by remember { mutableStateOf<Due?>(null) }
    var showCollectPaymentDialog by remember { mutableStateOf(false) }

    // Filter customers and dues that are critical (not fully paid AND dueDate >= 60 days before today)
    val criticalItems = remember(customers, dues) {
        val list = mutableListOf<Triple<Customer, Due, Long>>()
        dues.forEach { due ->
            if (due.dueStatus != "Paid" && due.dueAmount > 0.0) {
                val daysDiff = viewModel.repository.getDaysDifference(due.dueDate)
                if (daysDiff >= 60) {
                    val customer = customers.find { it.id == due.customerId }
                    if (customer != null) {
                        list.add(Triple(customer, due, daysDiff))
                    }
                }
            }
        }
        list.sortedByDescending { it.third } // sort by days overdue descending
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.currentRouteState.value = "dashboard" }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = if (lang == Lang.EN) "Emergency Critical Zone" else "ક્રિટિકલ બાકી રકમ ઝોન",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = Color(0xFFE11D48)
                )
            }
        }

        // Summary Statistics Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) Color(0xFF31050F) else Color(0xFFFFF1F2)),
            border = BorderStroke(1.dp, Color(0xFFE11D48).copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "🚨 High-Risk Outstanding Legal Recoveries",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE11D48),
                    style = MaterialTheme.typography.titleMedium
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Accounts Overdue 60+ Days:", style = MaterialTheme.typography.bodyMedium, color = if (isSystemInDarkTheme()) Color(0xFFFDA4AF) else Color(0xFF881337))
                    Text(text = "${criticalItems.size}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE11D48))
                }
                val totalCriticalAmt = criticalItems.sumOf { it.second.dueAmount }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Total Critical Balance:", style = MaterialTheme.typography.bodyMedium, color = if (isSystemInDarkTheme()) Color(0xFFFDA4AF) else Color(0xFF881337))
                    Text(text = "₹${totalCriticalAmt.toInt()}", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, color = Color(0xFFE11D48))
                }
            }
        }

        if (criticalItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Safe zone: No outstanding dues exceed the 60-day threshold.", textAlign = TextAlign.Center)
            }
        } else {
            criticalItems.forEach { (customer, due, days) ->
                // Find last follow-up note
                val lastFol = followups.filter { it.customerId == customer.id }.maxByOrNull { it.followUpDate }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE11D48).copy(alpha = 0.4f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = customer.customerName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Mobile: ${customer.mobileNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFE4E6))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$days Days Overdue",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFBE123C))
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Pending Dues", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "₹${due.dueAmount.toInt()}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = Color(0xFFE11D48))
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Due Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = due.dueDate, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }

                        if (lastFol != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(text = "Last Follow-up (${lastFol.followUpDate}):", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                    Text(text = lastFol.notes, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        } else {
                            Text(text = "No prior follow-ups logged for this critical file.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Actions: Call, WhatsApp, Take Payment
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Call Action
                            Button(
                                onClick = {
                                    viewModel.logActivity("Call", "Called Critical Customer: ${customer.customerName}")
                                    try {
                                        val parseUri = android.net.Uri.parse("tel:${customer.mobileNumber}")
                                        context.startActivity(android.content.Intent(android.content.Intent.ACTION_DIAL, parseUri).apply { flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK })
                                    } catch (e: Exception) {}
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(imageVector = Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Call", fontSize = 12.sp)
                            }

                            // WhatsApp Action
                            Button(
                                onClick = {
                                    viewModel.whatsAppReminderDialogData.value = WhatsAppReminderData(
                                        customerName = customer.customerName,
                                        mobileNumber = customer.mobileNumber,
                                        amount = due.dueAmount,
                                        dueDate = due.dueDate
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.2f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = "WhatsApp", modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WhatsApp", fontSize = 11.sp, color = Color.White)
                            }

                            // Take Payment Action
                            Button(
                                onClick = {
                                    selectedDueToCollect = due
                                    showCollectPaymentDialog = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.3f).testTag("critical_take_payment_${customer.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusColors.Paid)
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Payment", modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Payment", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    val dueToCollect = selectedDueToCollect
    if (showCollectPaymentDialog && dueToCollect != null) {
        val cust = customers.find { it.id == dueToCollect.customerId }
        if (cust != null) {
            CollectPaymentDialog(
                due = dueToCollect,
                lang = lang,
                onDismiss = { showCollectPaymentDialog = false; selectedDueToCollect = null },
                onSave = { amount, mode, notes ->
                    viewModel.collectPayment(cust.id, dueToCollect.id, amount, mode, notes)
                    showCollectPaymentDialog = false
                    selectedDueToCollect = null
                    android.widget.Toast.makeText(context, "Collected ₹${amount.toInt()}!", android.widget.Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MpinSettingsDialog(
    viewModel: AppViewModel,
    lang: Lang,
    onDismiss: () -> Unit
) {
    val currentMpin by viewModel.mpinState.collectAsState()
    var currentPinInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (lang == Lang.EN) "Security MPIN Settings" else "સિક્યુરિટી MPIN સેટિંગ્સ",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = if (lang == Lang.EN) "This MPIN is required to confirm deleting customers, staff, or dues to prevent accidental data loss." else "આકસ્મિક ડેટા ખોવાઈ જતો અટકાવવા માટે ગ્રાહકો, સ્ટાફ અથવા હપ્તા કાઢી નાખવાની ખાતરી કરવા માટે આ MPIN જરૂરી છે.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = currentPinInput,
                    onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 8) currentPinInput = it },
                    label = { Text(if (lang == Lang.EN) "Current MPIN" else "ચાલુ MPIN") },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("settings_current_mpin")
                )

                OutlinedTextField(
                    value = newPinInput,
                    onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 8) newPinInput = it },
                    label = { Text(if (lang == Lang.EN) "New MPIN (4-8 digits)" else "નવો MPIN (૪ થી ૮ અંકો)") },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("settings_new_mpin")
                )

                OutlinedTextField(
                    value = confirmPinInput,
                    onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 8) confirmPinInput = it },
                    label = { Text(if (lang == Lang.EN) "Confirm New MPIN" else "નવા MPIN ની પુષ્ટિ કરો") },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("settings_confirm_mpin")
                )

                if (errorMessage.isNotEmpty()) {
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }

                if (successMessage.isNotEmpty()) {
                    Text(text = successMessage, color = Color(0xFF4CAF50), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(AppStrings.cancel(lang))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            errorMessage = ""
                            successMessage = ""
                            if (currentPinInput != currentMpin) {
                                errorMessage = if (lang == Lang.EN) "Current MPIN is incorrect!" else "ચાલુ MPIN ખોટો છે!"
                            } else if (newPinInput.length < 4) {
                                errorMessage = if (lang == Lang.EN) "New MPIN must be at least 4 digits!" else "નવો MPIN ઓછામાં ઓછો ૪ અંકનો હોવો જોઈએ!"
                            } else if (newPinInput != confirmPinInput) {
                                errorMessage = if (lang == Lang.EN) "New MPIN inputs do not match!" else "નવા MPIN સરખા નથી!"
                            } else {
                                viewModel.updateMpin(newPinInput)
                                successMessage = if (lang == Lang.EN) "MPIN updated successfully!" else "MPIN સફળતાપૂર્વક અપડેટ થયો!"
                                currentPinInput = ""
                                newPinInput = ""
                                confirmPinInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("save_mpin_btn")
                    ) {
                        Text(if (lang == Lang.EN) "Save MPIN" else "MPIN સાચવો")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteMpinConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    lang: Lang,
    viewModel: AppViewModel,
    message: String = ""
) {
    var pinText by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }
    val correctMpin by viewModel.mpinState.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = StatusColors.Critical,
                    modifier = Modifier.size(40.dp)
                )

                Text(
                    text = if (lang == Lang.EN) "Confirm Deletion with MPIN" else "MPIN સાથે કાઢી નાખવાની પુષ્ટિ કરો",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                if (message.isNotEmpty()) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                OutlinedTextField(
                    value = pinText,
                    onValueChange = { input ->
                        if (input.length <= 8 && input.all { it.isDigit() }) {
                            pinText = input
                            hasError = false
                        }
                    },
                    label = { Text(if (lang == Lang.EN) "Enter Secure MPIN" else "સિક્યુરિટી MPIN દાખલ કરો") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                    ),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    isError = hasError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("delete_mpin_input"),
                    trailingIcon = {
                        if (hasError) {
                            Icon(imageVector = Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                if (hasError) {
                    Text(
                        text = if (lang == Lang.EN) "Incorrect MPIN! Try again." else "ખોટો MPIN! ફરીથી પ્રયાસ કરો.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Text(
                    text = if (lang == Lang.EN) "Hint: Default MPIN is 1234. Change it in Workspace settings." else "સૂચના: ડિફોલ્ટ MPIN 1234 છે. તેને સેટિંગ્સમાં બદલો.",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(AppStrings.cancel(lang))
                    }

                    Button(
                        onClick = {
                            if (pinText == correctMpin) {
                                onConfirm()
                                onDismiss()
                            } else {
                                hasError = true
                                pinText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusColors.Critical),
                        modifier = Modifier.weight(1f).testTag("delete_confirm_with_mpin_btn")
                    ) {
                        Text(if (lang == Lang.EN) "Verify & Delete" else "ચકાસો અને કાઢી નાખો", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
