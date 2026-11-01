package com.example.byhm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.example.byhm.data.model.AlertItem
import com.example.byhm.theme.StatusSuccessGreen
import com.example.byhm.ui.components.AlertSeverityChip

@Composable
fun AlertDetailScreen(
    alert: AlertItem?,
    onBackClick: () -> Unit,
    onOpenSourceUrl: (String) -> Unit,
    onViewOnMap: (Double, Double) -> Unit
) {
    if (alert == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Alert record not found.")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Official Alert Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AlertSeverityChip(severity = alert.severity)
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("v${alert.version} Advisory", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                if (alert.hindiTitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = alert.hindiTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null, tint = StatusSuccessGreen)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Issuing Authority: ${alert.issuingAuthority}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = StatusSuccessGreen
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Category: ${alert.category} (${alert.hazardSubtype ?: "General Hazard"}) • Severity: ${alert.officialSeverity ?: "Official Advisory"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Published: ${alert.publishedTime} • ${alert.validUntil}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Affected Area: ${alert.affectedArea}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (alert.corroboratingSources.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Corroborating Official Authorities:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = StatusSuccessGreen)
                    alert.corroboratingSources.forEach { source ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                            Icon(imageVector = Icons.Filled.VerifiedUser, contentDescription = null, tint = StatusSuccessGreen, modifier = Modifier.padding(end = 4.dp))
                            Text("• ${source.sourceAuthority.fullName} (${source.lastCheckedTimestamp})", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Full Official Advisory Text:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alert.description,
                    style = MaterialTheme.typography.bodyLarge
                )

                if (alert.hindiDescription != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "आधिकारिक हिंदी पाठ (Official Hindi Text):",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = alert.hindiDescription,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                if (alert.safetyInstructions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Safety Instructions & Precautions:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    alert.safetyInstructions.forEach { instruction ->
                        Text(
                            text = "• $instruction",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onOpenSourceUrl(alert.sourceUrl) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Filled.OpenInNew, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Official Source")
            }

            if (alert.latitude != null && alert.longitude != null) {
                OutlinedButton(
                    onClick = { onViewOnMap(alert.latitude, alert.longitude) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Map, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View on Map")
                }
            }
        }
    }
}
