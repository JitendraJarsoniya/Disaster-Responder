package com.example.byhm.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.byhm.data.model.Resource
import com.example.byhm.data.model.ResourceCategory
import com.example.byhm.ui.components.ResourceAvailabilityChip

@Composable
fun ResourceDirectoryScreen(
    resources: List<Resource>,
    searchQuery: String,
    selectedCategory: ResourceCategory?,
    onQueryChange: (String) -> Unit,
    onCategoryChange: (ResourceCategory?) -> Unit,
    onResourceClick: (String) -> Unit,
    getDistanceKm: (Double, Double) -> Double
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            placeholder = { Text("Search resources by name or location…") },
            leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategoryChange(null) },
                    label = { Text("All") },
                    leadingIcon = { Icon(imageVector = Icons.Filled.FilterList, contentDescription = null) }
                )
            }
            items(ResourceCategory.values()) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { onCategoryChange(cat) },
                    label = { Text(cat.displayName) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Resource List
        if (resources.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("No resources found matching your search.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(resources) { res ->
                    val distKm = getDistanceKm(res.latitude, res.longitude)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onResourceClick(res.id) },
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
                                Text(
                                    text = res.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                ResourceAvailabilityChip(status = res.status)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = res.address, style = MaterialTheme.typography.bodyMedium)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Filled.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = res.contactPhone, style = MaterialTheme.typography.bodySmall)
                                }

                                Text(
                                    text = "%.1f km away".format(distKm),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
