package com.example.byhm

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.byhm.data.model.IncidentCategory
import com.example.byhm.data.model.UserRole
import com.example.byhm.data.remote.AiApiClient
import com.example.byhm.data.remote.OfficialDisasterService
import com.example.byhm.data.remote.SosTriggerRequestDto
import com.example.byhm.data.repository.AuthState
import com.example.byhm.theme.BYHMTheme
import com.example.byhm.ui.components.AppBottomBar
import com.example.byhm.ui.components.AppTopBar
import com.example.byhm.ui.components.LocationRationaleDialog
import com.example.byhm.ui.navigation.Screen
import com.example.byhm.ui.screens.AdminDashboardScreen
import com.example.byhm.ui.screens.AiAssistantScreen
import com.example.byhm.ui.screens.AlertDetailScreen
import com.example.byhm.ui.screens.AlertsScreen
import com.example.byhm.ui.screens.DashboardScreen
import com.example.byhm.ui.screens.EmergencyGuideScreen
import com.example.byhm.ui.screens.EmergencyProfileScreen
import com.example.byhm.ui.screens.HelpInfoScreen
import com.example.byhm.ui.screens.IncidentReportScreen
import com.example.byhm.ui.screens.LoginScreen
import com.example.byhm.ui.screens.MapScreen
import com.example.byhm.ui.screens.MoreScreen
import com.example.byhm.ui.screens.MyReportsScreen
import com.example.byhm.ui.screens.ResourceDetailScreen
import com.example.byhm.ui.screens.ResourceDirectoryScreen
import com.example.byhm.ui.screens.ResponderDashboardScreen
import com.example.byhm.ui.screens.SettingsScreen
import com.example.byhm.ui.screens.SplashScreen
import com.example.byhm.ui.screens.WelcomeScreen
import com.example.byhm.ui.viewmodel.AiViewModel
import com.example.byhm.ui.viewmodel.AlertViewModel
import com.example.byhm.ui.viewmodel.EmergencyGuideViewModel
import com.example.byhm.ui.viewmodel.IncidentViewModel
import com.example.byhm.ui.viewmodel.MainViewModel
import com.example.byhm.ui.viewmodel.ResourceViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val resourceViewModel: ResourceViewModel by viewModels()
    private val incidentViewModel: IncidentViewModel by viewModels()
    private val alertViewModel: AlertViewModel by viewModels()
    private val aiViewModel: AiViewModel by viewModels()
    private val emergencyGuideViewModel: EmergencyGuideViewModel by viewModels()

    private val officialDisasterService = OfficialDisasterService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Load official SACHET & IMD alerts from National Alert Intelligence System on launch
        lifecycleScope.launch {
            val officialResult = officialDisasterService.fetchLiveOfficialAlerts()
            officialResult.onSuccess { liveAlerts ->
                liveAlerts.forEach { alertViewModel.publishAlert(it) }
            }
        }

        setContent {
            val currentThemeMode by mainViewModel.themeMode.collectAsState()
            val currentLanguage by mainViewModel.language.collectAsState()
            val savedMapLayer by mainViewModel.savedMapLayer.collectAsState()

            BYHMTheme(themeMode = currentThemeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    val currentUser by mainViewModel.currentUser.collectAsState()
                    val authState by mainViewModel.authState.collectAsState()
                    val userRole = currentUser?.role ?: UserRole.RESIDENT

                    val resources by resourceViewModel.filteredResources.collectAsState()
                    val searchQuery by resourceViewModel.searchQuery.collectAsState()
                    val selectedCategory by resourceViewModel.selectedCategory.collectAsState()

                    val incidents by incidentViewModel.incidents.collectAsState()
                    val alerts by alertViewModel.alerts.collectAsState()

                    val aiMessages by aiViewModel.messages.collectAsState()
                    val aiInputText by aiViewModel.inputText.collectAsState()
                    val aiIsGenerating by aiViewModel.isGenerating.collectAsState()
                    val aiErrorState by aiViewModel.errorState.collectAsState()
                    val aiAttachmentName by aiViewModel.selectedAttachmentName.collectAsState()

                    val guideItems by emergencyGuideViewModel.filteredGuides.collectAsState()
                    val guideSearchQuery by emergencyGuideViewModel.searchQuery.collectAsState()
                    val guideCategory by emergencyGuideViewModel.selectedCategory.collectAsState()

                    var isLoginLoading by remember { mutableStateOf(false) }
                    var loginErrorMessage by remember { mutableStateOf<String?>(null) }

                    // Determine initial start destination dynamically: Splash if user signed in, Welcome if unauthenticated
                    val initialStartDestination = remember {
                        val hasUser = try { FirebaseAuth.getInstance().currentUser != null } catch (e: Exception) { false }
                        if (hasUser) Screen.Splash.route else Screen.Welcome.route
                    }

                    // Seamless Session Restoration & Flash-Free Dashboard Navigation
                    LaunchedEffect(authState, currentUser) {
                        when (authState) {
                            is AuthState.AUTHENTICATED -> {
                                val user = (authState as AuthState.AUTHENTICATED).profile
                                if (user.role == UserRole.ADMIN || user.role == UserRole.RESPONDER) {
                                    if (currentRoute == Screen.Splash.route || currentRoute == Screen.Welcome.route || currentRoute == Screen.Login.route) {
                                        navController.navigate(Screen.Dashboard.route) {
                                            popUpTo(Screen.Splash.route) { inclusive = true }
                                            popUpTo(Screen.Welcome.route) { inclusive = true }
                                        }
                                    }
                                }
                            }
                            is AuthState.UNAUTHENTICATED -> {
                                if (currentRoute == Screen.Splash.route) {
                                    navController.navigate(Screen.Welcome.route) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                }
                            }
                            else -> {}
                        }
                    }

                    // Public Mode Location Permission Flow
                    var showLocationRationaleDialog by remember { mutableStateOf(false) }

                    val locationPermissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions()
                    ) { permissions ->
                        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
                        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
                        if (fineGranted || coarseGranted) {
                            Toast.makeText(this@MainActivity, "Location enabled for nearby emergency search", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity, "Location access denied. Manual location search is active.", Toast.LENGTH_LONG).show()
                        }
                    }

                    // Request permission ONLY when user is in Public Mode and enters Dashboard
                    LaunchedEffect(userRole, currentRoute) {
                        if (userRole == UserRole.RESIDENT && currentRoute == Screen.Dashboard.route) {
                            val hasFine = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            val hasCoarse = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            if (!hasFine && !hasCoarse) {
                                showLocationRationaleDialog = true
                            }
                        }
                    }

                    if (showLocationRationaleDialog) {
                        LocationRationaleDialog(
                            onConfirmPermissionRequest = {
                                showLocationRationaleDialog = false
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    )
                                )
                            },
                            onDismissRequest = {
                                showLocationRationaleDialog = false
                                Toast.makeText(this@MainActivity, "Manual location selection active.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    // Activity Result Launchers for Multilingual Speech Recognition & File Attachments
                    val speechLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()
                    ) { result ->
                        if (result.resultCode == Activity.RESULT_OK) {
                            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
                            if (!spokenText.isNullOrBlank()) {
                                aiViewModel.setInputText(spokenText)
                                Toast.makeText(this@MainActivity, "Voice Recognized", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    val filePickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri: Uri? ->
                        uri?.let { fileUri ->
                            var fileName = "Attached_Document"
                            contentResolver.query(fileUri, null, null, null, null)?.use { cursor ->
                                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                if (nameIndex != -1 && cursor.moveToFirst()) {
                                    fileName = cursor.getString(nameIndex)
                                }
                            }
                            aiViewModel.attachFile(fileName, fileUri.toString())
                            Toast.makeText(this@MainActivity, "Attached: $fileName", Toast.LENGTH_SHORT).show()
                        }
                    }

                    val showBar = currentRoute != Screen.Splash.route && currentRoute != Screen.Welcome.route && currentRoute != Screen.Login.route && currentRoute != Screen.AiAssistant.route

                    Scaffold(
                        topBar = {
                            if (showBar) {
                                AppTopBar(currentRole = userRole)
                            }
                        },
                        bottomBar = {
                            if (showBar) {
                                AppBottomBar(
                                    currentRoute = currentRoute,
                                    onNavigate = { screen ->
                                        if (screen.route == Screen.Dashboard.route) {
                                            navController.navigate(Screen.Dashboard.route) {
                                                popUpTo(Screen.Dashboard.route) { inclusive = false }
                                                launchSingleTop = true
                                            }
                                        } else {
                                            navController.navigate(screen.route) {
                                                popUpTo(Screen.Dashboard.route) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = initialStartDestination,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable(Screen.Splash.route) {
                                SplashScreen()
                            }

                            composable(Screen.Welcome.route) {
                                WelcomeScreen(
                                    onExploreClicked = {
                                        mainViewModel.setGuestMode()
                                        navController.navigate(Screen.Dashboard.route) {
                                            popUpTo(Screen.Welcome.route) { inclusive = true }
                                        }
                                    },
                                    onLoginClicked = {
                                        navController.navigate(Screen.Login.route)
                                    }
                                )
                            }

                            composable(Screen.Login.route) {
                                LoginScreen(
                                    onSignInWithEmail = { email, pass ->
                                        isLoginLoading = true
                                        loginErrorMessage = null
                                        mainViewModel.signInWithEmail(email, pass) { result ->
                                            isLoginLoading = false
                                            result.onSuccess { profile ->
                                                Toast.makeText(this@MainActivity, "Signed in as ${profile.role.displayName}", Toast.LENGTH_SHORT).show()
                                                navController.navigate(Screen.Dashboard.route) {
                                                    popUpTo(Screen.Welcome.route) { inclusive = true }
                                                    popUpTo(Screen.Login.route) { inclusive = true }
                                                }
                                            }.onFailure { err ->
                                                loginErrorMessage = err.localizedMessage ?: "Authentication failed."
                                            }
                                        }
                                    },
                                    onGuestMode = {
                                        mainViewModel.setGuestMode()
                                        navController.navigate(Screen.Dashboard.route) {
                                            popUpTo(Screen.Welcome.route) { inclusive = true }
                                        }
                                    },
                                    isLoading = isLoginLoading,
                                    errorMessage = loginErrorMessage
                                )
                            }

                            composable(Screen.Dashboard.route) {
                                when (userRole) {
                                    UserRole.RESIDENT -> DashboardScreen(
                                        incidents = incidents,
                                        alerts = alerts,
                                        onCategorySelected = { cat ->
                                            resourceViewModel.setCategory(cat)
                                            navController.navigate(Screen.ResourceDirectory.route)
                                        },
                                        onReportClick = { navController.navigate(Screen.IncidentReport.route) },
                                        onMapClick = { navController.navigate(Screen.Map.route) },
                                        onAiClick = { prompt ->
                                            if (!prompt.isNullOrBlank()) {
                                                aiViewModel.setInputText(prompt)
                                            }
                                            navController.navigate(Screen.AiAssistant.route)
                                        },
                                        onAlertsClick = { navController.navigate(Screen.Alerts.route) },
                                        onEmergencyGuideClick = { navController.navigate(Screen.EmergencyGuide.route) },
                                        onMyStatusClick = { navController.navigate(Screen.MyReports.route) },
                                        onEmergencyProfileClick = { navController.navigate(Screen.EmergencyProfile.route) },
                                        onRefreshClick = {
                                            Toast.makeText(this@MainActivity, "Refreshed official disaster data", Toast.LENGTH_SHORT).show()
                                        },
                                        onTriggerSos = { lat, lng, loc, profileText ->
                                            // 1. Submit incident report to local Room DB & ViewModels so Admin Dashboard & My Reports get the new Gwalior SOS event in real-time!
                                            incidentViewModel.submitReport(
                                                reporterId = currentUser?.uid ?: "sos_distress",
                                                reporterName = if (currentUser?.name != null && currentUser?.name != "Guest User") currentUser!!.name else "🚨 Emergency SOS Callout",
                                                category = IncidentCategory.OTHER,
                                                description = "EMERGENCY SOS CALLOUT: $profileText",
                                                locationName = loc,
                                                latitude = lat,
                                                longitude = lng
                                            )

                                            // 2. Also log in FastAPI backend sos_events table asynchronously
                                            lifecycleScope.launch {
                                                try {
                                                    AiApiClient.apiService.triggerSosCallout(
                                                        SosTriggerRequestDto(
                                                            locationName = loc,
                                                            latitude = lat,
                                                            longitude = lng,
                                                            emergencyProfileText = profileText
                                                        )
                                                    )
                                                } catch (e: Exception) {
                                                    // Logged in Room DB above
                                                }
                                            }
                                        }
                                    )
                                    UserRole.RESPONDER -> ResponderDashboardScreen(
                                        incidents = incidents,
                                        responderName = currentUser?.name ?: "Responder",
                                        onUpdateStatus = { id, status, name, notes ->
                                            val inc = incidents.find { it.id == id }
                                            if (inc != null) {
                                                incidentViewModel.updateStatus(inc, status, name, notes)
                                                Toast.makeText(this@MainActivity, "Incident status updated!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                    UserRole.ADMIN -> AdminDashboardScreen(
                                        incidents = incidents,
                                        adminName = currentUser?.name ?: "Admin",
                                        onVerifyIncident = { id, status, name, notes ->
                                            val inc = incidents.find { it.id == id }
                                            if (inc != null) {
                                                incidentViewModel.updateStatus(inc, status, name, notes)
                                                Toast.makeText(this@MainActivity, "Report verified by Admin!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        onPublishAsAlert = { publishedAlert ->
                                            alertViewModel.publishAlert(publishedAlert)
                                            Toast.makeText(this@MainActivity, "Published as Public Disaster Alert!", Toast.LENGTH_SHORT).show()
                                        },
                                        onTriggerSourceSync = {
                                            Toast.makeText(this@MainActivity, "Manual sync triggered across official adapters!", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }

                            composable(Screen.ResourceDirectory.route) {
                                ResourceDirectoryScreen(
                                    resources = resources,
                                    searchQuery = searchQuery,
                                    selectedCategory = selectedCategory,
                                    onQueryChange = { resourceViewModel.setSearchQuery(it) },
                                    onCategoryChange = { resourceViewModel.setCategory(it) },
                                    onResourceClick = { id ->
                                        navController.navigate(Screen.ResourceDetail.createRoute(id))
                                    },
                                    getDistanceKm = { lat, lng -> resourceViewModel.getDistanceKm(lat, lng) }
                                )
                            }

                            composable(Screen.ResourceDetail.route) { backStackEntry ->
                                val resId = backStackEntry.arguments?.getString("resourceId") ?: ""
                                val resource = resourceViewModel.getResourceById(resId)
                                ResourceDetailScreen(
                                    resource = resource,
                                    onBackClick = { navController.popBackStack() },
                                    onCallClick = { phone ->
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                        startActivity(intent)
                                    },
                                    onNavigateClick = { lat, lng ->
                                        val gmmIntentUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                        startActivity(mapIntent)
                                    }
                                )
                            }

                            composable(Screen.Map.route) {
                                MapScreen(
                                    resources = resources,
                                    alerts = alerts,
                                    currentMapLayer = savedMapLayer,
                                    userLatitude = 26.2183, // Real Gwalior Coordinates for map testing if GPS not fixed
                                    userLongitude = 78.1828,
                                    onMapLayerChange = { mainViewModel.setSavedMapLayer(it) },
                                    onResourceClick = { id ->
                                        navController.navigate(Screen.ResourceDetail.createRoute(id))
                                    },
                                    onAlertClick = { id ->
                                        navController.navigate(Screen.AlertDetail.createRoute(id))
                                    },
                                    onRefreshClick = {
                                        Toast.makeText(this@MainActivity, "Map overlay refreshed", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }

                            composable(Screen.IncidentReport.route) {
                                IncidentReportScreen(
                                    reporterName = currentUser?.name ?: "Anonymous User",
                                    onSubmitReport = { cat, desc, loc, lat, lng ->
                                        incidentViewModel.submitReport(
                                            reporterId = currentUser?.uid ?: "anon",
                                            reporterName = currentUser?.name ?: "Anonymous User",
                                            category = cat,
                                            description = desc,
                                            locationName = loc,
                                            latitude = lat,
                                            longitude = lng
                                        )
                                        Toast.makeText(this@MainActivity, "Incident Report Submitted!", Toast.LENGTH_SHORT).show()
                                        navController.navigate(Screen.MyReports.route)
                                    },
                                    onViewMyReports = { navController.navigate(Screen.MyReports.route) }
                                )
                            }

                            composable(Screen.More.route) {
                                MoreScreen(
                                    userProfile = currentUser,
                                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                                    onNavigateToAlerts = { navController.navigate(Screen.Alerts.route) },
                                    onNavigateToEmergencyGuide = { navController.navigate(Screen.EmergencyGuide.route) },
                                    onNavigateToHelp = { navController.navigate(Screen.HelpInfo.route) }
                                )
                            }

                            composable(Screen.EmergencyProfile.route) {
                                EmergencyProfileScreen(
                                    onBackClick = { navController.popBackStack() }
                                )
                            }

                            composable(Screen.EmergencyGuide.route) {
                                EmergencyGuideScreen(
                                    guides = guideItems,
                                    searchQuery = guideSearchQuery,
                                    selectedCategory = guideCategory,
                                    onBackClick = { navController.popBackStack() },
                                    onQueryChange = { emergencyGuideViewModel.setSearchQuery(it) },
                                    onCategoryChange = { emergencyGuideViewModel.setCategory(it) },
                                    onCall112Click = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))
                                        startActivity(intent)
                                    }
                                )
                            }

                            composable(Screen.HelpInfo.route) {
                                HelpInfoScreen(
                                    onBackClick = { navController.popBackStack() },
                                    onCallClick = { phone ->
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                        startActivity(intent)
                                    }
                                )
                            }

                            composable(Screen.MyReports.route) {
                                MyReportsScreen(incidents = incidents)
                            }

                            composable(Screen.Alerts.route) {
                                AlertsScreen(
                                    alerts = alerts,
                                    onAlertClick = { alertId ->
                                        navController.navigate(Screen.AlertDetail.createRoute(alertId))
                                    }
                                )
                            }

                            composable(Screen.AlertDetail.route) { backStackEntry ->
                                val alertId = backStackEntry.arguments?.getString("alertId") ?: ""
                                val alertItem = alerts.find { it.id == alertId }
                                AlertDetailScreen(
                                    alert = alertItem,
                                    onBackClick = { navController.popBackStack() },
                                    onOpenSourceUrl = { url ->
                                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        startActivity(browserIntent)
                                    },
                                    onViewOnMap = { lat, lng ->
                                        navController.navigate(Screen.Map.route)
                                    }
                                )
                            }

                            composable(Screen.AiAssistant.route) {
                                AiAssistantScreen(
                                    messages = aiMessages,
                                    inputText = aiInputText,
                                    isGenerating = aiIsGenerating,
                                    errorState = aiErrorState,
                                    selectedAttachmentName = aiAttachmentName,
                                    onInputTextChange = { aiViewModel.setInputText(it) },
                                    onSendMessage = { aiViewModel.sendMessage() },
                                    onAttachFileClick = {
                                        filePickerLauncher.launch("*/*")
                                    },
                                    onMicClick = {
                                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak in Hindi or English / हिंदी या अंग्रेजी में बोलें...")
                                        }
                                        try {
                                            speechLauncher.launch(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(this@MainActivity, "Speech recognition unavailable on this device", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onRemoveAttachment = { aiViewModel.removeAttachment() },
                                    onClearChat = { aiViewModel.clearChat() },
                                    onRetry = { aiViewModel.retryLastMessage() }
                                )
                            }

                            composable(Screen.Settings.route) {
                                SettingsScreen(
                                    userProfile = currentUser,
                                    currentThemeMode = currentThemeMode,
                                    currentLanguage = currentLanguage,
                                    onBackClick = { navController.popBackStack() },
                                    onThemeModeChange = { mainViewModel.setThemeMode(it) },
                                    onLanguageChange = { mainViewModel.setLanguage(it) },
                                    onSwitchRole = { role ->
                                        Toast.makeText(this@MainActivity, "Demo role switching disabled. Sign in with approved Firebase Admin/Responder account.", Toast.LENGTH_LONG).show()
                                    },
                                    onLogout = {
                                        mainViewModel.logout()
                                        navController.navigate(Screen.Welcome.route) {
                                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                                            popUpTo(Screen.Splash.route) { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
