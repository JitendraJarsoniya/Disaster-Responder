package com.example.byhm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.byhm.data.model.AlertSeverity
import com.example.byhm.data.model.AvailabilityStatus
import com.example.byhm.data.model.IncidentStatus
import com.example.byhm.data.model.SyncState
import com.example.byhm.theme.StatusCriticalRed
import com.example.byhm.theme.StatusInfoBlue
import com.example.byhm.theme.StatusSuccessGreen
import com.example.byhm.theme.StatusWarningAmber

@Composable
fun StatusChip(
    text: String,
    backgroundColor: Color,
    textColor: Color = Color.White
) {
    Box(
        modifier = Modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun IncidentStatusChip(status: IncidentStatus) {
    val (bg, txt) = when (status) {
        IncidentStatus.SUBMITTED -> StatusInfoBlue to Color.White
        IncidentStatus.UNDER_REVIEW -> StatusWarningAmber to Color.White
        IncidentStatus.VERIFIED -> StatusSuccessGreen to Color.White
        IncidentStatus.ASSIGNED -> StatusInfoBlue to Color.White
        IncidentStatus.IN_PROGRESS -> StatusWarningAmber to Color.White
        IncidentStatus.RESOLVED -> StatusSuccessGreen to Color.White
        IncidentStatus.REJECTED -> StatusCriticalRed to Color.White
    }
    StatusChip(text = status.displayName, backgroundColor = bg, textColor = txt)
}

@Composable
fun ResourceAvailabilityChip(status: AvailabilityStatus) {
    val (bg, txt) = when (status) {
        AvailabilityStatus.AVAILABLE -> StatusSuccessGreen to Color.White
        AvailabilityStatus.LIMITED -> StatusWarningAmber to Color.White
        AvailabilityStatus.FULL -> StatusCriticalRed to Color.White
        AvailabilityStatus.UNKNOWN -> Color.Gray to Color.White
    }
    StatusChip(text = status.displayName, backgroundColor = bg, textColor = txt)
}

@Composable
fun AlertSeverityChip(severity: AlertSeverity) {
    val (bg, txt) = when (severity) {
        AlertSeverity.CRITICAL -> StatusCriticalRed to Color.White
        AlertSeverity.HIGH -> StatusWarningAmber to Color.White
        AlertSeverity.MEDIUM -> StatusInfoBlue to Color.White
        AlertSeverity.INFO -> Color.DarkGray to Color.White
    }
    StatusChip(text = severity.displayName, backgroundColor = bg, textColor = txt)
}

@Composable
fun SyncStatusChip(syncState: SyncState) {
    val (icon, color) = when (syncState) {
        SyncState.SAVED_LOCALLY -> Icons.Filled.CloudOff to Color.Gray
        SyncState.PENDING_SYNC -> Icons.Filled.Sync to StatusInfoBlue
        SyncState.SYNCED -> Icons.Filled.CloudDone to StatusSuccessGreen
        SyncState.FAILED -> Icons.Filled.ErrorOutline to StatusCriticalRed
    }
    
    Box(
        modifier = Modifier
            .background(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = syncState.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
