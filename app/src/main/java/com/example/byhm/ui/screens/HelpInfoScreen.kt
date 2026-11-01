package com.example.byhm.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.byhm.theme.StatusCriticalRed

@Composable
fun HelpInfoScreen(
    onBackClick: () -> Unit,
    onCallClick: (String) -> Unit
) {
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
                text = "National Emergency Helplines",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Direct 112 Call Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StatusCriticalRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NATIONAL EMERGENCY NUMBER",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "112",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Single Emergency Number for All Services in India",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onCallClick("112") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Phone, contentDescription = null, tint = StatusCriticalRed)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Call 112 Immediately", color = StatusCriticalRed, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Key Agency Helplines", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        HelplineItem("National Disaster Management Authority (NDMA)", "1078 / 011-26701728", onCallClick)
        Spacer(modifier = Modifier.height(8.dp))
        HelplineItem("Fire Control Room", "101", onCallClick)
        Spacer(modifier = Modifier.height(8.dp))
        HelplineItem("Ambulance Service", "102 / 108", onCallClick)
        Spacer(modifier = Modifier.height(8.dp))
        HelplineItem("Police Helpline", "100", onCallClick)
        Spacer(modifier = Modifier.height(8.dp))
        HelplineItem("Women Helpline", "1091", onCallClick)
    }
}

@Composable
private fun HelplineItem(
    title: String,
    phone: String,
    onCallClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = phone, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { onCallClick(phone.split(" / ")[0]) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Filled.Phone, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Call")
            }
        }
    }
}
