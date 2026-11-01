package com.example.byhm.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.byhm.data.model.AlertItem
import com.example.byhm.data.model.AlertSeverity
import com.example.byhm.data.model.IncidentCategory
import com.example.byhm.data.model.IncidentReport
import com.example.byhm.data.model.IncidentStatus
import com.example.byhm.domain.PriorityLevel
import com.example.byhm.domain.RescuePriorityCalculator
import com.example.byhm.theme.StatusCriticalRed
import com.example.byhm.theme.StatusInfoBlue
import com.example.byhm.theme.StatusSuccessGreen
import com.example.byhm.theme.StatusWarningAmber
import com.example.byhm.ui.components.IncidentStatusChip

@Composable
fun AdminDashboardScreen(
    incidents: List<IncidentReport>,
    adminName: String,
    onVerifyIncident: (String, IncidentStatus, String, String) -> Unit,
    onPublishAsAlert: (AlertItem) -> Unit,
    onTriggerSourceSync: () -> Unit = {}
) {
    var isSourceMonitorExpanded by remember { mutableStateOf(false) } // Default COLLAPSED
    var selectedRejectingIncidentId by remember { mutableStateOf<String?>(null) }
    var rejectionReason by remember { mutableStateOf("Duplicate Report") }
    var customNotes by remember { mutableStateOf("") }

    val rejectionOptions = listOf(
        "Duplicate Report",
        "Invalid Information",
        "Unrelated Report",
        "Insufficient Information",
        "Other / Misleading"
    )

    val sortedIncidents = remember(incidents) { incidents.sortedByDescending { it.id } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Dashboard Header Section
        item {
            Column {
                Text(
                    text = "Administrator Management Console",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Source Health Monitoring, Priority Explainer & Alert Publishing",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. Collapsible Official Source Health & Synchronization Monitor Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isSourceMonitorExpanded = !isSourceMonitorExpanded }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Header Row: Title, Compact Status Summary, and Expand Toggle Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Filled.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("National Source Health Monitor", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("4 Connected · 2 Awaiting Key", style = MaterialTheme.typography.labelSmall, color = StatusSuccessGreen, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        IconButton(
                            onClick = { isSourceMonitorExpanded = !isSourceMonitorExpanded },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isSourceMonitorExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = if (isSourceMonitorExpanded) "Collapse Monitor" else "Expand Monitor"
                            )
                        }
                    }

                    // Expanded Details Section
                    AnimatedVisibility(visible = isSourceMonitorExpanded) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Official Adapter Statuses:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                OutlinedButton(
                                    onClick = onTriggerSourceSync,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(imageVector = Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Manual Sync", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val sources = listOf(
                                "IMD Meteorological Dept" to "CONNECTED (Mausam API)",
                                "NDMA SACHET Portal" to "CONNECTED (Sachet API)",
                                "CWC Flood Forecast" to "CONNECTED (Hydro Feed)",
                                "SDMA State Authorities" to "CONNECTED (Pan-India)",
                                "INCOIS Ocean Tsunami" to "AWAITING AUTHORIZED KEY",
                                "NDRF Command Center" to "AWAITING AUTHORIZED KEY"
                            )

                            sources.forEach { (name, status) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                    Text(
                                        text = status,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (status.startsWith("CONNECTED")) StatusSuccessGreen else StatusWarningAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Incident Reports Section Header
        item {
            Text(
                text = "Community Incident Reports (${sortedIncidents.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // 4. Incident / SOS Report Cards List
        if (sortedIncidents.isEmpty()) {
            item {
                Text("No user incident reports submitted yet.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            items(sortedIncidents, key = { it.id }) { inc ->
                val priorityEval = RescuePriorityCalculator.calculatePriority(inc)
                val isRejected = inc.status == IncidentStatus.REJECTED

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = inc.category.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            IncidentStatusChip(status = inc.status)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Priority Explainer Badge
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PriorityBadge(level = priorityEval.level, score = priorityEval.score)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = inc.description, style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Reporter: ${inc.reporterName} • Location: ${inc.locationName}", style = MaterialTheme.typography.bodySmall)

                        Spacer(modifier = Modifier.height(10.dp))

                        // Priority Breakdown Reasons List
                        Text("Priority Explainer Reasons:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        priorityEval.reasons.forEach { reason ->
                            Text("• $reason", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Responsive Action Buttons (2 Rows to avoid label wrapping or fragmentation)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { onVerifyIncident(inc.id, IncidentStatus.VERIFIED, adminName, "Verified by Admin review") },
                                    enabled = !isRejected,
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccessGreen),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Verify")
                                }

                                Button(
                                    onClick = { selectedRejectingIncidentId = inc.id },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusCriticalRed),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Reject Report")
                                }
                            }

                            if (!isRejected) {
                                Button(
                                    onClick = {
                                        val publishedAlert = AlertItem(
                                            id = "pub_alert_${inc.id}",
                                            title = "Verified Community Report: ${inc.category.displayName} in ${inc.locationName}",
                                            description = inc.description,
                                            severity = when (inc.category) {
                                                IncidentCategory.FLOOD, IncidentCategory.FIRE -> AlertSeverity.HIGH
                                                IncidentCategory.EARTHQUAKE, IncidentCategory.CYCLONE -> AlertSeverity.CRITICAL
                                                else -> AlertSeverity.MEDIUM
                                            },
                                            issuingAuthority = "Verified Community Report (Admin Validated)",
                                            affectedArea = inc.locationName,
                                            publishedTime = "Just Now",
                                            validUntil = "Valid for 12 Hours",
                                            isVerifiedOfficial = true,
                                            sourceType = "Verified Community Report",
                                            sourceUrl = "https://disaster-responder.app/reports/${inc.id}",
                                            latitude = inc.latitude,
                                            longitude = inc.longitude
                                        )
                                        onPublishAsAlert(publishedAlert)
                                        onVerifyIncident(inc.id, IncidentStatus.ASSIGNED, adminName, "Published as Public Alert")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusInfoBlue),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Publish as Public Alert")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Rejection Confirmation Dialog
    selectedRejectingIncidentId?.let { incidentId ->
        AlertDialog(
            onDismissRequest = { selectedRejectingIncidentId = null },
            icon = { Icon(imageVector = Icons.Filled.Cancel, contentDescription = null, tint = StatusCriticalRed) },
            title = { Text("Reject Incident Report") },
            text = {
                Column {
                    Text("Select reason for rejecting this report:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    rejectionOptions.forEach { option ->
                        FilterChip(
                            selected = rejectionReason == option,
                            onClick = { rejectionReason = option },
                            label = { Text(option) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customNotes,
                        onValueChange = { customNotes = it },
                        placeholder = { Text("Additional notes (optional)...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val fullNotes = if (customNotes.isNotBlank()) "Rejected: $rejectionReason ($customNotes)" else "Rejected: $rejectionReason"
                        onVerifyIncident(incidentId, IncidentStatus.REJECTED, adminName, fullNotes)
                        selectedRejectingIncidentId = null
                        customNotes = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusCriticalRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Confirm Reject")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { selectedRejectingIncidentId = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PriorityBadge(level: PriorityLevel, score: Int) {
    val bg = when (level) {
        PriorityLevel.CRITICAL -> StatusCriticalRed
        PriorityLevel.HIGH -> StatusWarningAmber
        PriorityLevel.MEDIUM -> StatusInfoBlue
        PriorityLevel.LOW -> StatusSuccessGreen
    }

    Box(
        modifier = Modifier
            .background(color = bg, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = "${level.displayName} (Score: $score)",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}
