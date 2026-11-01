package com.example.byhm.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.byhm.data.model.AlertItem
import com.example.byhm.data.model.AlertVerificationStatus
import com.example.byhm.theme.StatusCriticalRed
import com.example.byhm.theme.StatusInfoBlue
import com.example.byhm.theme.StatusSuccessGreen
import com.example.byhm.theme.StatusWarningAmber
import com.example.byhm.ui.components.AlertSeverityChip

@Composable
fun AlertsScreen(
    alerts: List<AlertItem>,
    onAlertClick: (String) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0 = Active Warnings, 1 = Recently Updated, 2 = Historical Archive
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    val categories = listOf("ALL", "Heavy Rainfall", "Flood", "Flash Flood", "Cyclone", "Thunderstorm", "Earthquake", "Tsunami")

    val filteredAlerts = alerts.filter { alert ->
        val matchesTab = when (selectedTabIndex) {
            0 -> alert.verificationStatus == AlertVerificationStatus.PUBLISHED || alert.verificationStatus == AlertVerificationStatus.VERIFIED
            1 -> alert.version > 1 || alert.isCorroboratedByMultipleSources
            2 -> true // Historical Archive
            else -> true
        }
        val matchesQuery = searchQuery.isBlank() || alert.title.contains(searchQuery, ignoreCase = true) || alert.affectedArea.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategoryFilter == "ALL" || alert.category.contains(selectedCategoryFilter, ignoreCase = true)
        matchesTab && matchesQuery && matchesCategory
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Official Disaster Alerts & Verification",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Corroborated by IMD, NDMA, CWC & SDMA Authorities",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs Row: Active Warnings vs Recently Updated vs Historical Archive
        TabRow(selectedTabIndex = selectedTabIndex) {
            Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }) {
                Text("Active (${alerts.count { it.verificationStatus == AlertVerificationStatus.PUBLISHED }})", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }) {
                Text("Updated", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTabIndex == 2, onClick = { selectedTabIndex = 2 }) {
                Text("Archive", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar & Filter Chips
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search location or hazard type…") },
            leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth().height(46.dp),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(categories) { cat ->
                FilterChip(
                    selected = selectedCategoryFilter == cat,
                    onClick = { selectedCategoryFilter = cat },
                    label = { Text(cat) },
                    leadingIcon = if (cat == "ALL") {
                        { Icon(imageVector = Icons.Filled.FilterList, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else null
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredAlerts.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "No alerts found matching current tab / search filter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredAlerts) { alert ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAlertClick(alert.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AlertSeverityChip(severity = alert.severity)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    VerificationBadge(status = alert.verificationStatus)
                                }
                                Text(
                                    text = alert.publishedTime,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = alert.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Category: ${alert.category} (${alert.hazardSubtype ?: "Hazard"})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                if (alert.version > 1) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("v${alert.version} Updated", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(text = alert.description, style = MaterialTheme.typography.bodyMedium)

                            if (alert.isCorroboratedByMultipleSources) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Filled.VerifiedUser, contentDescription = null, tint = StatusSuccessGreen)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Corroborated by ${alert.corroboratingSources.size} Independent Official Authorities",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = StatusSuccessGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (alert.unconfirmedReason != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "⚠️ ${alert.unconfirmedReason}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusWarningAmber,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null, tint = StatusSuccessGreen)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = alert.issuingAuthority,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = StatusSuccessGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { onAlertClick(alert.id) }
                                ) {
                                    Text(
                                        text = "View Full Details",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VerificationBadge(status: AlertVerificationStatus) {
    val (bg, label) = when (status) {
        AlertVerificationStatus.PUBLISHED -> StatusSuccessGreen to "Official Warning"
        AlertVerificationStatus.VERIFIED -> StatusInfoBlue to "Verified"
        AlertVerificationStatus.UNCONFIRMED -> StatusWarningAmber to "Unconfirmed"
        AlertVerificationStatus.PENDING_VERIFICATION -> Color.Gray to "Under Review"
        AlertVerificationStatus.EXPIRED -> Color.DarkGray to "Expired"
    }

    Box(
        modifier = Modifier
            .background(color = bg, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}
