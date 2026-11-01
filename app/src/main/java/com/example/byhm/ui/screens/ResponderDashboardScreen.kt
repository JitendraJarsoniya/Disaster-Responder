package com.example.byhm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.byhm.data.model.IncidentReport
import com.example.byhm.data.model.IncidentStatus
import com.example.byhm.theme.StatusSuccessGreen
import com.example.byhm.theme.StatusWarningAmber
import com.example.byhm.ui.components.IncidentStatusChip

@Composable
fun ResponderDashboardScreen(
    incidents: List<IncidentReport>,
    responderName: String,
    onUpdateStatus: (String, IncidentStatus, String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Responder Command Dashboard",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Assigned Incident Dispatch & Status Updates",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(incidents) { inc ->
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

                        Text(text = inc.description, style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Location: ${inc.locationName}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { onUpdateStatus(inc.id, IncidentStatus.IN_PROGRESS, responderName, "En route to location") },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusWarningAmber),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("In Progress")
                            }

                            Button(
                                onClick = { onUpdateStatus(inc.id, IncidentStatus.RESOLVED, responderName, "Incident resolved and area secured") },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusSuccessGreen),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Mark Resolved")
                            }
                        }
                    }
                }
            }
        }
    }
}
