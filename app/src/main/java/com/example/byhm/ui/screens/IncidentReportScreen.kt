package com.example.byhm.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.byhm.data.model.IncidentCategory
import com.example.byhm.theme.StatusCriticalRed
import com.example.byhm.theme.StatusWarningAmber
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentReportScreen(
    reporterName: String,
    onSubmitReport: (IncidentCategory, String, String, Double, Double) -> Unit,
    onViewMyReports: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(IncidentCategory.FLOOD) }
    var descriptionText by remember { mutableStateOf("") }
    var locationText by remember { mutableStateOf("") }
    var latitude by remember { mutableDoubleStateOf(0.0) }
    var longitude by remember { mutableDoubleStateOf(0.0) }
    var isExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLocating by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                        locationText = "Current GPS Location Captured"
                        errorMessage = null
                    } else {
                        errorMessage = "Could not obtain GPS location. Please enter manually."
                    }
                    isLocating = false
                }.addOnFailureListener {
                    errorMessage = "Location service error. Please enter manually."
                    isLocating = false
                }
            } catch (e: SecurityException) {
                errorMessage = "Location permission revoked."
                isLocating = false
            }
        } else {
            errorMessage = "Location permission denied. Please type your location manually."
            isLocating = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Submit Incident Report",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Warning banner
        Card(
            colors = CardDefaults.cardColors(containerColor = StatusWarningAmber.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.Info, contentDescription = null, tint = StatusWarningAmber)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reports are reviewed by authorized staff. Please provide factual details.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Disaster Category Dropdown
        Text("Incident Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))

        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = !isExpanded }
        ) {
            OutlinedTextField(
                value = selectedCategory.displayName,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }
            ) {
                IncidentCategory.entries.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat.displayName) },
                        onClick = {
                            selectedCategory = cat
                            isExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Location text
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Incident Location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            IconButton(
                onClick = {
                    isLocating = true
                    val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    
                    if (hasFine || hasCoarse) {
                        try {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                                if (location != null) {
                                    latitude = location.latitude
                                    longitude = location.longitude
                                    locationText = "Current GPS Location Captured"
                                    errorMessage = null
                                } else {
                                    errorMessage = "Could not obtain GPS location. Please enter manually."
                                }
                                isLocating = false
                            }
                        } catch (e: SecurityException) {
                            isLocating = false
                        }
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }
            ) {
                Icon(imageVector = Icons.Filled.MyLocation, contentDescription = "Use GPS", tint = MaterialTheme.colorScheme.primary)
            }
        }
        
        Spacer(modifier = Modifier.height(2.dp))
        
        OutlinedTextField(
            value = locationText,
            onValueChange = { locationText = it },
            placeholder = { Text("Enter address or use GPS button") },
            leadingIcon = { Icon(imageVector = Icons.Filled.LocationOn, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description text
        Text("Incident Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = descriptionText,
            onValueChange = { descriptionText = it },
            placeholder = { Text("Describe the situation, severity, number of stranded people, hazards…") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(10.dp)
        )

        errorMessage?.let { err ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = err, color = StatusCriticalRed, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (descriptionText.isBlank()) {
                    errorMessage = "Please enter a description of the incident."
                } else if (locationText.isBlank() && latitude == 0.0) {
                    errorMessage = "Please enter a location or use GPS."
                } else {
                    errorMessage = null
                    onSubmitReport(selectedCategory, descriptionText, locationText, latitude, longitude)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StatusCriticalRed)
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (isLocating) "Locating..." else "Submit Report to Control Room", 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold
            )
        }
    }
}
