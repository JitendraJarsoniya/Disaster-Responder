package com.example.byhm.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.byhm.BuildConfig
import com.example.byhm.data.map.MapLayerType
import com.example.byhm.data.map.MapMarkerData
import com.example.byhm.data.model.AlertItem
import com.example.byhm.data.model.AlertVerificationStatus
import com.example.byhm.data.model.Resource
import com.example.byhm.theme.StatusCriticalRed
import com.example.byhm.theme.StatusSuccessGreen
import com.example.byhm.theme.StatusWarningAmber
import com.example.byhm.ui.components.AlertSeverityChip
import com.example.byhm.ui.components.ResourceAvailabilityChip
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

// Standard Humanitarian OpenStreetMap
private val OSM_HUMANITARIAN = XYTileSource(
    "OSM_HOT",
    0, 19, 256, ".png",
    arrayOf("https://a.tile.openstreetmap.fr/hot/", "https://b.tile.openstreetmap.fr/hot/"),
    "© OpenStreetMap contributors, HOT"
)

// OpenTopoMap Topographic Tile Source
private val OPEN_TOPO_MAP = XYTileSource(
    "OpenTopoMap",
    0, 17, 256, ".png",
    arrayOf("https://a.tile.opentopomap.org/", "https://b.tile.opentopomap.org/"),
    "© OpenTopoMap contributors"
)

// Real MapTiler Aerial Satellite v2 Tile Source
private val MAPTILER_SATELLITE = if (BuildConfig.MAPTILER_API_KEY.isNotBlank()) {
    XYTileSource(
        "MapTilerSatellite",
        0, 19, 256,
        ".jpg?key=${BuildConfig.MAPTILER_API_KEY}",
        arrayOf("https://api.maptiler.com/maps/satellite/"),
        "© MapTiler © OpenStreetMap contributors"
    )
} else null

// Real MapTiler Hybrid (Satellite + Labels) Tile Source
private val MAPTILER_HYBRID = if (BuildConfig.MAPTILER_API_KEY.isNotBlank()) {
    XYTileSource(
        "MapTilerHybrid",
        0, 19, 256,
        ".jpg?key=${BuildConfig.MAPTILER_API_KEY}",
        arrayOf("https://api.maptiler.com/maps/hybrid/"),
        "© MapTiler © OpenStreetMap contributors"
    )
} else null

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    resources: List<Resource>,
    alerts: List<AlertItem>,
    currentMapLayer: MapLayerType = MapLayerType.STANDARD,
    userLatitude: Double? = null,
    userLongitude: Double? = null,
    onMapLayerChange: (MapLayerType) -> Unit = {},
    onResourceClick: (String) -> Unit,
    onAlertClick: (String) -> Unit,
    onRefreshClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Initialize osmdroid configuration globally with specified User-Agent
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = "DisasterResponder-Android"
    }

    var isFullScreen by remember { mutableStateOf(false) }
    var selectedLayer by remember { mutableStateOf(currentMapLayer) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var maxDistanceFilterKm by remember { mutableDoubleStateOf(50.0) }

    var showMenuOverflow by remember { mutableStateOf(false) }
    var showLayersBottomSheet by remember { mutableStateOf(false) }
    var showMoreFiltersBottomSheet by remember { mutableStateOf(false) }
    var showSatelliteKeyDialog by remember { mutableStateOf(false) }
    var isBottomPanelExpanded by remember { mutableStateOf(false) }
    var selectedMarkerData by remember { mutableStateOf<MapMarkerData?>(null) }

    // Preserve map center and zoom level across recompositions and fullscreen transitions
    var currentMapCenterLat by remember { mutableDoubleStateOf(28.6139) }
    var currentMapCenterLng by remember { mutableDoubleStateOf(77.2090) }
    var currentZoomLevel by remember { mutableDoubleStateOf(11.5) }

    val mapView = remember {
        MapView(context).apply {
            val initialTileSource = when (selectedLayer) {
                MapLayerType.STANDARD -> OSM_HUMANITARIAN
                MapLayerType.TOPOGRAPHIC -> OPEN_TOPO_MAP
                MapLayerType.SATELLITE -> MAPTILER_SATELLITE ?: OSM_HUMANITARIAN
                MapLayerType.HYBRID -> MAPTILER_HYBRID ?: OSM_HUMANITARIAN
            }
            setTileSource(initialTileSource)
            setMultiTouchControls(true)
            controller.setZoom(currentZoomLevel)
            controller.setCenter(GeoPoint(currentMapCenterLat, currentMapCenterLng))
            // Prevent parent Compose layouts from intercepting touch gestures
            setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> v.parent?.requestDisallowInterceptTouchEvent(true)
                    MotionEvent.ACTION_UP -> v.performClick()
                }
                false
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    // Filter resources
    val filteredResources = resources.filter { res ->
        val matchesQuery = searchQuery.isBlank() || res.name.contains(searchQuery, ignoreCase = true) || res.address.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedCategoryFilter) {
            "HOSPITALS" -> res.category.name == "HOSPITAL"
            "SHELTERS" -> res.category.name == "SHELTER"
            "RESCUE" -> res.category.name == "RESCUE_TEAM"
            "FOOD" -> res.category.name == "FOOD_WATER"
            else -> true
        }
        val distanceKm = calculateDistanceKm(userLatitude ?: 28.6139, userLongitude ?: 77.2090, res.latitude, res.longitude)
        val matchesDistance = distanceKm <= maxDistanceFilterKm
        matchesQuery && matchesFilter && matchesDistance
    }

    // Filter alerts - Only verified and published alerts with valid coordinates appear as public map markers
    val filteredAlerts = alerts.filter { alert ->
        val isPublishedOrVerified = alert.verificationStatus == AlertVerificationStatus.PUBLISHED || alert.verificationStatus == AlertVerificationStatus.VERIFIED
        val hasValidCoords = alert.latitude != null && alert.longitude != null
        val matchesQuery = searchQuery.isBlank() || alert.title.contains(searchQuery, ignoreCase = true) || alert.affectedArea.contains(searchQuery, ignoreCase = true)
        val matchesFilter = selectedCategoryFilter == "ALL" || selectedCategoryFilter == "ALERTS"
        val alertLat = alert.latitude ?: 28.6139
        val alertLng = alert.longitude ?: 77.2090
        val distanceKm = calculateDistanceKm(userLatitude ?: 28.6139, userLongitude ?: 77.2090, alertLat, alertLng)
        val matchesDistance = distanceKm <= maxDistanceFilterKm
        isPublishedOrVerified && hasValidCoords && matchesQuery && matchesFilter && matchesDistance
    }

    // Update map layer tile source smoothly while preserving camera position & zoom
    LaunchedEffect(selectedLayer) {
        when (selectedLayer) {
            MapLayerType.STANDARD -> mapView.setTileSource(OSM_HUMANITARIAN)
            MapLayerType.TOPOGRAPHIC -> mapView.setTileSource(OPEN_TOPO_MAP)
            MapLayerType.SATELLITE -> MAPTILER_SATELLITE?.let { mapView.setTileSource(it) } ?: mapView.setTileSource(OSM_HUMANITARIAN)
            MapLayerType.HYBRID -> MAPTILER_HYBRID?.let { mapView.setTileSource(it) } ?: mapView.setTileSource(OSM_HUMANITARIAN)
        }
        mapView.invalidate()
    }

    // Render overlays & auto-fit active markers
    LaunchedEffect(filteredResources, filteredAlerts) {
        mapView.overlays.clear()
        val allPoints = mutableListOf<GeoPoint>()

        filteredResources.forEach { res ->
            val point = GeoPoint(res.latitude, res.longitude)
            allPoints.add(point)
            val markerData = MapMarkerData(
                id = res.id,
                title = res.name,
                snippet = res.category.displayName,
                latitude = res.latitude,
                longitude = res.longitude,
                category = res.category.displayName,
                isAlert = false,
                statusOrSeverity = res.status.displayName,
                addressOrArea = res.address,
                phone = res.contactPhone
            )
            val marker = Marker(mapView).apply {
                position = point
                title = res.name
                snippet = res.category.displayName
                setOnMarkerClickListener { _, _ ->
                    selectedMarkerData = markerData
                    true
                }
            }
            mapView.overlays.add(marker)
        }

        filteredAlerts.forEach { alert ->
            if (alert.latitude != null && alert.longitude != null) {
                val point = GeoPoint(alert.latitude, alert.longitude)
                allPoints.add(point)
                val markerData = MapMarkerData(
                    id = alert.id,
                    title = alert.title,
                    snippet = alert.severity.displayName,
                    latitude = alert.latitude,
                    longitude = alert.longitude,
                    category = "Disaster Alert",
                    isAlert = true,
                    statusOrSeverity = alert.severity.displayName,
                    addressOrArea = alert.affectedArea
                )
                val marker = Marker(mapView).apply {
                    position = point
                    title = alert.title
                    snippet = alert.severity.displayName
                    setOnMarkerClickListener { _, _ ->
                        selectedMarkerData = markerData
                        true
                    }
                }
                mapView.overlays.add(marker)
            }
        }

        // Auto-fit bounding box around local active markers to avoid India-level view
        if (allPoints.isNotEmpty()) {
            val bbox = BoundingBox.fromGeoPoints(allPoints)
            mapView.post {
                mapView.zoomToBoundingBox(bbox, true, 80)
            }
        }
        mapView.invalidate()
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (!isFullScreen) {
            // A. Compact Map Toolbar
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Disaster Emergency Map",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search location…") },
                        leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.width(170.dp).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Box {
                        IconButton(onClick = { showMenuOverflow = true }) {
                            Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "Menu Options")
                        }
                        DropdownMenu(
                            expanded = showMenuOverflow,
                            onDismissRequest = { showMenuOverflow = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Refresh Map Overlays") },
                                onClick = {
                                    showMenuOverflow = false
                                    onRefreshClick()
                                },
                                leadingIcon = { Icon(Icons.Filled.Refresh, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear All Filters") },
                                onClick = {
                                    showMenuOverflow = false
                                    searchQuery = ""
                                    selectedCategoryFilter = "ALL"
                                    maxDistanceFilterKm = 50.0
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // B. Compact Category Filter Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == "ALL",
                            onClick = { selectedCategoryFilter = "ALL" },
                            label = { Text("All") },
                            leadingIcon = { Icon(imageVector = Icons.Filled.FilterList, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == "ALERTS",
                            onClick = { selectedCategoryFilter = "ALERTS" },
                            label = { Text("Alerts (${filteredAlerts.size})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == "SHELTERS",
                            onClick = { selectedCategoryFilter = "SHELTERS" },
                            label = { Text("Shelters") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == "HOSPITALS",
                            onClick = { selectedCategoryFilter = "HOSPITALS" },
                            label = { Text("Hospitals") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == "MORE",
                            onClick = { showMoreFiltersBottomSheet = true },
                            label = { Text("More Filters…") }
                        )
                    }
                }
            }
        }

        // C. STABLE ROUNDED MAP CONTAINER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = if (isFullScreen) 0.dp else 10.dp, vertical = if (isFullScreen) 0.dp else 4.dp)
                .clip(RoundedCornerShape(if (isFullScreen) 0.dp else 16.dp))
                .border(
                    width = if (isFullScreen) 0.dp else 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(if (isFullScreen) 0.dp else 16.dp)
                )
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize()
            )

            // Top Control FABs (Fullscreen Toggle & Map Layers)
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { isFullScreen = !isFullScreen },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = if (isFullScreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                        contentDescription = "Toggle Fullscreen"
                    )
                }

                SmallFloatingActionButton(
                    onClick = { showLayersBottomSheet = true },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(imageVector = Icons.Filled.Layers, contentDescription = "Map Layers")
                }
            }

            // My Location FAB (Bottom Right)
            SmallFloatingActionButton(
                onClick = {
                    val lat = userLatitude ?: 28.6139
                    val lng = userLongitude ?: 77.2090
                    mapView.controller.animateTo(GeoPoint(lat, lng), 14.0, 1000L)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
            ) {
                Icon(imageVector = Icons.Filled.MyLocation, contentDescription = "Center Current Location")
            }

            // Map Attribution & Legend Overlay (Top Right)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (selectedLayer == MapLayerType.SATELLITE || selectedLayer == MapLayerType.HYBRID) {
                        Text(
                            text = "© MapTiler © OpenStreetMap",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(StatusCriticalRed, RoundedCornerShape(4.dp)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Alerts", style = MaterialTheme.typography.labelSmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(StatusSuccessGreen, RoundedCornerShape(4.dp)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Resources", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        if (!isFullScreen) {
            // D. Expandable Collapsible Bottom Panel for Map Overlay Listings
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Collapsed Summary Header Toggle Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isBottomPanelExpanded = !isBottomPanelExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Active Map Overlays",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(${filteredAlerts.size} Alerts • ${filteredResources.size} Resources)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { isBottomPanelExpanded = !isBottomPanelExpanded },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isBottomPanelExpanded) Icons.Filled.ExpandMore else Icons.Filled.ExpandLess,
                                contentDescription = if (isBottomPanelExpanded) "Collapse Panel" else "Expand Panel"
                            )
                        }
                    }

                    // Expanded Section: Horizontal Carousel Cards
                    AnimatedVisibility(visible = isBottomPanelExpanded) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))

                            if (filteredAlerts.isEmpty() && filteredResources.isEmpty()) {
                                Text("No active markers found matching current search/filter.", style = MaterialTheme.typography.bodySmall)
                            } else {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(filteredAlerts) { alert ->
                                        Card(
                                            modifier = Modifier
                                                .width(250.dp)
                                                .clickable { onAlertClick(alert.id) },
                                            colors = CardDefaults.cardColors(containerColor = StatusCriticalRed.copy(alpha = 0.12f)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                AlertSeverityChip(severity = alert.severity)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = alert.title,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(text = alert.affectedArea, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                            }
                                        }
                                    }

                                    items(filteredResources) { res ->
                                        Card(
                                            modifier = Modifier
                                                .width(240.dp)
                                                .clickable { onResourceClick(res.id) },
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                ResourceAvailabilityChip(status = res.status)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = res.name,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(imageVector = Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text(text = res.address, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // E. More Category Filters Modal Bottom Sheet
    if (showMoreFiltersBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreFiltersBottomSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Additional Resource Filters", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                val secondaryCategories = listOf(
                    "RESCUE" to "Search & Rescue Teams",
                    "FOOD" to "Food & Clean Water Outlets",
                    "POLICE" to "Police Stations & Control Posts",
                    "FIRE" to "Fire Stations & Extinguisher Outlets",
                    "RELIEF" to "Relief Camps & Distribution Hubs"
                )

                secondaryCategories.forEach { (code, label) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                selectedCategoryFilter = code
                                showMoreFiltersBottomSheet = false
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedCategoryFilter == code) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null)
                        }
                    }
                }
            }
        }
    }

    // F. COMPACT MAP LAYER SELECTOR BOTTOM SHEET
    if (showLayersBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLayersBottomSheet = false },
            sheetState = rememberModalBottomSheetState(),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            scrimColor = Color.Black.copy(alpha = 0.35f)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 18.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Layers, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Map Tile Layer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("PERSISTED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                MapLayerType.entries.forEach { layer ->
                    val isSelected = selectedLayer == layer
                    val icon = when (layer) {
                        MapLayerType.STANDARD -> Icons.Filled.Map
                        MapLayerType.TOPOGRAPHIC -> Icons.Filled.Terrain
                        MapLayerType.SATELLITE -> Icons.Filled.Satellite
                        MapLayerType.HYBRID -> Icons.Filled.Layers
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                if (layer.isAvailable) {
                                    selectedLayer = layer
                                    onMapLayerChange(layer)
                                    showLayersBottomSheet = false
                                } else {
                                    showLayersBottomSheet = false
                                    showSatelliteKeyDialog = true
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = layer.displayName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (layer.isAvailable) MaterialTheme.colorScheme.onSurface else Color.Gray
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                }
                                layer.note?.let {
                                    Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            if (!layer.isAvailable) {
                                Box(
                                    modifier = Modifier
                                        .background(StatusWarningAmber.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Key Required", style = MaterialTheme.typography.labelSmall, color = StatusWarningAmber, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // G. SATELLITE & HYBRID KEY REQUIRED DIALOG
    if (showSatelliteKeyDialog) {
        AlertDialog(
            onDismissRequest = { showSatelliteKeyDialog = false },
            icon = { Icon(imageVector = Icons.Filled.Info, contentDescription = null, tint = StatusWarningAmber) },
            title = { Text("MapTiler API Key Setup") },
            text = {
                Text(
                    "MapTiler Satellite & Hybrid imagery requires `MAPTILER_API_KEY` configured in `local.properties`.\n\n" +
                    "Add your key to `local.properties`:\n" +
                    "MAPTILER_API_KEY=your_key_here\n\n" +
                    "Then rebuild the app to unlock MapTiler Aerial Satellite and Hybrid tile layers."
                )
            },
            confirmButton = {
                Button(
                    onClick = { showSatelliteKeyDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Got It")
                }
            }
        )
    }

    // H. Selected Marker Details Bottom Sheet
    selectedMarkerData?.let { markerData ->
        ModalBottomSheet(
            onDismissRequest = { selectedMarkerData = null },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = markerData.category, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = markerData.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Location: ${markerData.addressOrArea}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Status: ${markerData.statusOrSeverity}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)

                userLatitude?.let { uLat ->
                    userLongitude?.let { uLng ->
                        val distanceKm = calculateDistanceKm(uLat, uLng, markerData.latitude, markerData.longitude)
                        Text(text = "Distance: %.1f km away".format(distanceKm), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    markerData.phone?.let { phone ->
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Phone, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call Emergency")
                        }
                    }

                    Button(
                        onClick = {
                            selectedMarkerData = null
                            if (markerData.isAlert) {
                                onAlertClick(markerData.id)
                            } else {
                                onResourceClick(markerData.id)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("View Details")
                    }
                }
            }
        }
    }
}

private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0 // Earth radius in km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return r * c
}
