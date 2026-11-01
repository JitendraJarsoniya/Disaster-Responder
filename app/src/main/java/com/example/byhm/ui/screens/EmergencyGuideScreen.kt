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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.byhm.data.model.EmergencyGuide
import com.example.byhm.theme.StatusCriticalRed
import com.example.byhm.theme.StatusSuccessGreen
import com.example.byhm.theme.StatusWarningAmber

@Composable
fun EmergencyGuideScreen(
    guides: List<EmergencyGuide>,
    searchQuery: String,
    selectedCategory: String?,
    onBackClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onCategoryChange: (String?) -> Unit,
    onCall112Click: () -> Unit
) {
    val categories = listOf(
        "Flood Safety",
        "Fire Safety",
        "Earthquake Safety",
        "Severe Weather and Lightning"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header Row
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Icon(
                imageVector = Icons.Filled.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Offline Emergency Guide",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            placeholder = { Text("Search offline safety instructions…") },
            leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategoryChange(null) },
                    label = { Text("All Guides") },
                    leadingIcon = { Icon(imageVector = Icons.Filled.FilterList, contentDescription = null) }
                )
            }
            items(categories) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { onCategoryChange(cat) },
                    label = { Text(cat.replace(" and ", " & ")) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Guide Cards List
        if (guides.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No offline guides found for '$searchQuery'.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(guides) { guide ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = guide.category,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "100% OFFLINE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = guide.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Emergency Warning Callout Banner
                            Card(
                                colors = CardDefaults.cardColors(containerColor = StatusCriticalRed.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = guide.emergencyWarning,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = onCall112Click,
                                        colors = ButtonDefaults.buttonColors(containerColor = StatusCriticalRed),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Icon(imageVector = Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Call National Emergency 112", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 1. Immediate Safety Actions
                            GuideSection(
                                icon = Icons.Filled.FlashOn,
                                title = "Immediate Safety Actions",
                                items = guide.immediateActions,
                                tintColor = StatusWarningAmber
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // 2. Actions to Avoid
                            GuideSection(
                                icon = Icons.Filled.Block,
                                title = "Actions to Avoid",
                                items = guide.actionsToAvoid,
                                tintColor = StatusCriticalRed
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // 3. Evacuation & Shelter
                            GuideSection(
                                icon = Icons.Filled.DirectionsRun,
                                title = "Evacuation & Shelter Guidance",
                                items = guide.evacuationAndShelter,
                                tintColor = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // 4. After-Event Precautions
                            GuideSection(
                                icon = Icons.Filled.Shield,
                                title = "After-Event Precautions",
                                items = guide.afterEventPrecautions,
                                tintColor = StatusSuccessGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideSection(
    icon: ImageVector,
    title: String,
    items: List<String>,
    tintColor: androidx.compose.ui.graphics.Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = tintColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        items.forEach { bullet ->
            Text(
                text = "• $bullet",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 24.dp, bottom = 2.dp)
            )
        }
    }
}
