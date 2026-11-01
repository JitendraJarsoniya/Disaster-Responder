package com.example.byhm.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.byhm.data.location.LocationHelper
import com.example.byhm.data.model.AlertItem
import com.example.byhm.data.model.IncidentReport
import com.example.byhm.data.model.ResourceCategory
import com.example.byhm.data.repository.EmergencyProfileRepository
import com.example.byhm.theme.StatusCriticalRed
import com.example.byhm.theme.StatusSuccessGreen
import com.example.byhm.ui.components.AlertSeverityChip
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    incidents: List<IncidentReport>,
    alerts: List<AlertItem>,
    lastRefreshedTime: String = "Just Now",
    onCategorySelected: (ResourceCategory) -> Unit,
    onReportClick: () -> Unit,
    onMapClick: () -> Unit,
    onAiClick: (String?) -> Unit,
    onAlertsClick: () -> Unit,
    onEmergencyGuideClick: () -> Unit,
    onMyStatusClick: () -> Unit,
    onEmergencyProfileClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onTriggerSos: (Double, Double, String, String) -> Unit = { _, _, _, _ -> }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val emergencyRepo = remember { EmergencyProfileRepository(context) }
    val savedEmergencyProfile = remember { emergencyRepo.getProfile() }
    val locationHelper = remember { LocationHelper(context) }

    var liveLocationName by remember { mutableStateOf("Detecting GPS Location...") }
    var currentLat by remember { mutableDoubleStateOf(0.0) }
    var currentLng by remember { mutableDoubleStateOf(0.0) }
    var locationAccuracyMeters by remember { mutableStateOf<Float?>(null) }
    var isFetchingLocation by remember { mutableStateOf(false) }

    var showSosCountdownDialog by remember { mutableStateOf(false) }
    var sosCountdown by remember { mutableIntStateOf(3) }
    var isSosActiveCountdown by remember { mutableStateOf(false) }

    // Fetch Fresh High-Accuracy Location on Screen Load
    LaunchedEffect(Unit) {
        if (locationHelper.hasLocationPermission()) {
            isFetchingLocation = true
            val freshRes = locationHelper.fetchFreshLocation()
            freshRes.onSuccess { loc ->
                currentLat = loc.latitude
                currentLng = loc.longitude
                liveLocationName = loc.addressName
                locationAccuracyMeters = loc.accuracyMeters
            }.onFailure {
                liveLocationName = "Location Services Offline"
            }
            isFetchingLocation = false
        } else {
            liveLocationName = "Location Permission Required"
        }
    }

    // Subtle Infinite Pulsing Glow Animation for SOS Outer Ring
    val infiniteTransition = rememberInfiniteTransition()
    val sosGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.32f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // SOS Countdown Logic
    LaunchedEffect(isSosActiveCountdown) {
        if (isSosActiveCountdown) {
            sosCountdown = 3
            while (sosCountdown > 0) {
                delay(1000L)
                sosCountdown--
            }
            if (isSosActiveCountdown) {
                isSosActiveCountdown = false
                showSosCountdownDialog = false

                // Re-verify fresh GPS coordinates at moment of SOS trigger
                isFetchingLocation = true
                val freshRes = locationHelper.fetchFreshLocation()
                freshRes.onSuccess { loc ->
                    currentLat = loc.latitude
                    currentLng = loc.longitude
                    liveLocationName = loc.addressName
                    locationAccuracyMeters = loc.accuracyMeters
                }
                isFetchingLocation = false

                val profile = emergencyRepo.getProfile()
                val profileText = if (profile != null) {
                    "Name: ${profile.fullName}, Phone: ${profile.mobileNumber}, Emergency Contact: ${profile.emergencyContact}, Notes: ${profile.medicalNotes}, Photos: ${profile.photoUris.size}"
                } else {
                    "Anonymous Citizen SOS Callout."
                }

                onTriggerSos(currentLat, currentLng, liveLocationName, profileText)
                Toast.makeText(context, "🚨 EMERGENCY SOS DISPATCHED TO CONTROL ROOM!", Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Header with App Name, Real-Time Location & Last Refresh Time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Disaster Responder",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = if (isFetchingLocation) "Acquiring GPS Location..." else liveLocationName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            IconButton(
                onClick = {
                    onRefreshClick()
                    coroutineScope.launch {
                        if (locationHelper.hasLocationPermission()) {
                            isFetchingLocation = true
                            val freshRes = locationHelper.fetchFreshLocation()
                            freshRes.onSuccess { loc ->
                                currentLat = loc.latitude
                                currentLng = loc.longitude
                                liveLocationName = loc.addressName
                                locationAccuracyMeters = loc.accuracyMeters
                            }
                            isFetchingLocation = false
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh Data",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Compact Weather / Disaster Alert Card
        if (alerts.isNotEmpty()) {
            val topAlert = alerts.first()
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAlertsClick() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AlertSeverityChip(severity = topAlert.severity)
                        Text(
                            text = topAlert.publishedTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = topAlert.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = topAlert.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "No active official alerts found in $liveLocationName.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. PROMINENT CIRCULAR SOS BUTTON WITH SUBTLE PULSING GLOW ANIMATION (UNCHANGED)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .background(
                        color = StatusCriticalRed.copy(alpha = sosGlowAlpha),
                        shape = CircleShape
                    )
                    .padding(12.dp)
                    .clip(CircleShape)
                    .background(StatusCriticalRed)
                    .shadow(elevation = 8.dp, shape = CircleShape)
                    .clickable {
                        showSosCountdownDialog = true
                        isSosActiveCountdown = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SOS",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "EMERGENCY",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Tap to Trigger Emergency SOS Callout",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. FULL-WIDTH PROMINENT AI DISASTER ASSISTANT BLOCK
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAiClick(null) },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "AI Assistant Logo",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Disaster Assistant",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Get guidance and answers during emergency situations.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { onAiClick(null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Chat with AI",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. NEW: EMERGENCY PROFILE CARD (MOVED TO HOME SCREEN IMMEDIATELY BELOW AI ASSISTANT CARD)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEmergencyProfileClick() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContactPage,
                            contentDescription = "Emergency Profile",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Emergency Profile",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (savedEmergencyProfile != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Profile Saved",
                                    tint = StatusSuccessGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = if (savedEmergencyProfile != null) "Saved: ${savedEmergencyProfile.fullName} (${savedEmergencyProfile.mobileNumber})" else "Save your personal details and optional photos for emergency identification.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { onEmergencyProfileClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (savedEmergencyProfile != null) "Edit Emergency Details" else "Add Your Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 6. Quick Access Section (2x2 Grid)
        Text(
            text = "Quick Access",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickAccessCard(
                title = "Nearby Resources",
                subtitle = "Hospitals, Shelters, Food",
                icon = Icons.Filled.LocalHospital,
                modifier = Modifier.weight(1f)
            ) {
                onCategorySelected(ResourceCategory.HOSPITAL)
            }

            QuickAccessCard(
                title = "Emergency Guide",
                subtitle = "Offline Safety Protocols",
                icon = Icons.Filled.MenuBook,
                modifier = Modifier.weight(1f)
            ) {
                onEmergencyGuideClick()
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickAccessCard(
                title = "My Request Status",
                subtitle = "Track Reported Incidents",
                icon = Icons.Filled.Assignment,
                modifier = Modifier.weight(1f)
            ) {
                onMyStatusClick()
            }

            QuickAccessCard(
                title = "Interactive Map",
                subtitle = "Geospatial Markers",
                icon = Icons.Filled.Map,
                modifier = Modifier.weight(1f)
            ) {
                onMapClick()
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // SOS Emergency Confirmation & Countdown Dialog
    if (showSosCountdownDialog) {
        AlertDialog(
            onDismissRequest = {
                isSosActiveCountdown = false
                showSosCountdownDialog = false
            },
            icon = { Icon(imageVector = Icons.Filled.Warning, contentDescription = null, tint = StatusCriticalRed) },
            title = { Text("Trigger Emergency SOS Callout?") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Transmitting SOS distress signal to Command Center in:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "$sosCountdown",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = StatusCriticalRed
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isFetchingLocation) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = StatusCriticalRed)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Fetching fresh High-Accuracy GPS Location...", style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        Text(
                            text = "Location: $liveLocationName" + (locationAccuracyMeters?.let { " (Accuracy: ±%.0fm)".format(it) } ?: ""),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isSosActiveCountdown = false
                        showSosCountdownDialog = false

                        val profile = emergencyRepo.getProfile()
                        val profileText = if (profile != null) {
                            "Name: ${profile.fullName}, Phone: ${profile.mobileNumber}, Emergency Contact: ${profile.emergencyContact}, Notes: ${profile.medicalNotes}, Photos: ${profile.photoUris.size}"
                        } else {
                            "Anonymous Citizen SOS Callout."
                        }

                        onTriggerSos(currentLat, currentLng, liveLocationName, profileText)
                        Toast.makeText(context, "🚨 EMERGENCY SOS DISPATCHED TO CONTROL ROOM!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusCriticalRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Send SOS Now")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        isSosActiveCountdown = false
                        showSosCountdownDialog = false
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel SOS")
                }
            }
        )
    }
}

@Composable
private fun QuickAccessCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    fontSize = 11.sp
                )
            }
        }
    }
}
