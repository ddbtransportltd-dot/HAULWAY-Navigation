package com.czerman.haulway_navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import android.os.Bundle
import android.os.Looper
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.Manifest
import android.view.View
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast
import android.speech.tts.TextToSpeech
import java.util.Locale
import java.util.Date
import java.text.SimpleDateFormat
import android.webkit.WebChromeClient
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextFieldDefaults
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

private val MapBackground = Color(0xFFE7E8E5)
private val RoadColor = Color(0xFFFAFAF8)
private val MainGrey = Color(0xFF555B60)
private val AccentBlue = Color(0xFF48B9E8)
private val AccentOrange = Color(0xFFFF9638)
private val AccentRed = Color(0xFFE34D4D)
private val White = Color.White
private val Dark = Color(0xFF25292D)

data class HaulwayMenuItem(
    val title: String,
    val icon: ImageVector
)

private val haulwayMenu = listOf(
    HaulwayMenuItem("SEARCH", Icons.Default.Search),
    HaulwayMenuItem("HOME", Icons.Default.Home),
    HaulwayMenuItem("WORK", Icons.Default.Work),
    HaulwayMenuItem("MY PLACES", Icons.Default.Place),
    HaulwayMenuItem("MY ROUTES", Icons.Default.Route),
    HaulwayMenuItem("POI", Icons.Default.LocationOn),
    HaulwayMenuItem("VEHICLE", Icons.Default.LocalShipping),
    HaulwayMenuItem("SETTINGS", Icons.Default.Settings),
    HaulwayMenuItem("SOUND", Icons.Default.VolumeUp),
    HaulwayMenuItem("APPEARENCE", Icons.Default.Palette)
)

private object RoutePlanStore {
    var coordinatesJson by mutableStateOf<String?>(null)
    var summary by mutableStateOf<String?>(null)
    var steps by mutableStateOf<List<String>>(emptyList())
    var maneuvers by mutableStateOf<List<RouteManeuver>>(emptyList())
    var origin by mutableStateOf("")
    var destination by mutableStateOf("")
    var totalDistanceMetres by mutableStateOf(0.0)
    var totalDurationSeconds by mutableStateOf(0.0)
}

private data class RouteManeuver(
    val label: String,
    val roadName: String,
    val latitude: Double,
    val longitude: Double,
    val routeDistanceMetres: Double
)

private data class RouteResult(
    val coordinatesJson: String,
    val distanceText: String,
    val durationText: String,
    val steps: List<String>,
    val maneuvers: List<RouteManeuver>,
    val waypointCoordinatesJson: String,
    val legSummaries: List<Pair<String, String>> = emptyList(),
    val totalDistanceMetres: Double = 0.0,
    val totalDurationSeconds: Double = 0.0
)

private fun distanceMetres(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
    return earthRadius * 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
}

private fun distanceAlongRouteMetres(
    currentLat: Double, currentLon: Double,
    targetLat: Double, targetLon: Double,
    coordinatesJson: String?
): Double? {
    if (coordinatesJson.isNullOrBlank()) return null
    return try {
        val points = org.json.JSONArray(coordinatesJson)
        if (points.length() < 2) return null
        val route = ArrayList<Pair<Double, Double>>(points.length())
        for (i in 0 until points.length()) {
            val point = points.getJSONArray(i)
            route.add(point.getDouble(1) to point.getDouble(0)) // latitude, longitude
        }
        fun nearestIndex(lat: Double, lon: Double): Int {
            var bestIndex = 0
            var bestDistance = Double.MAX_VALUE
            route.forEachIndexed { index, point ->
                val distance = distanceMetres(lat, lon, point.first, point.second)
                if (distance < bestDistance) { bestDistance = distance; bestIndex = index }
            }
            return bestIndex
        }
        val currentIndex = nearestIndex(currentLat, currentLon)
        val targetIndex = nearestIndex(targetLat, targetLon)
        if (targetIndex <= currentIndex) {
            distanceMetres(currentLat, currentLon, targetLat, targetLon)
        } else {
            var total = distanceMetres(currentLat, currentLon, route[currentIndex].first, route[currentIndex].second)
            for (i in currentIndex until targetIndex) {
                total += distanceMetres(route[i].first, route[i].second, route[i + 1].first, route[i + 1].second)
            }
            total + distanceMetres(route[targetIndex].first, route[targetIndex].second, targetLat, targetLon)
        }
    } catch (_: Exception) { null }
}

private fun distanceToRouteMetres(latitude: Double, longitude: Double, coordinatesJson: String?): Double? {
    if (coordinatesJson.isNullOrBlank()) return null
    return try {
        val points = org.json.JSONArray(coordinatesJson)
        if (points.length() < 2) return null
        // Local equirectangular projection is sufficiently accurate for short GPS-to-road distances.
        val latScale = 111_320.0
        val lonScale = 111_320.0 * kotlin.math.cos(Math.toRadians(latitude))
        var best = Double.MAX_VALUE
        for (i in 0 until points.length() - 1) {
            val a = points.getJSONArray(i)
            val b = points.getJSONArray(i + 1)
            val ax = (a.getDouble(1) - latitude) * latScale
            val ay = (a.getDouble(0) - longitude) * lonScale
            val bx = (b.getDouble(1) - latitude) * latScale
            val by = (b.getDouble(0) - longitude) * lonScale
            val dx = bx - ax
            val dy = by - ay
            val length2 = dx * dx + dy * dy
            val t = if (length2 <= 0.001) 0.0 else ((-ax * dx - ay * dy) / length2).coerceIn(0.0, 1.0)
            val px = ax + t * dx
            val py = ay + t * dy
            best = minOf(best, kotlin.math.sqrt(px * px + py * py))
        }
        best.takeIf { it.isFinite() }
    } catch (_: Exception) { null }
}

private fun formatNavigationDistance(metres: Double): String = when {
    metres >= 1609.344 -> String.format(java.util.Locale.UK, "%.1f mi", metres / 1609.344)
    metres >= 160 -> "${(metres / 10).toInt() * 10} m"
    else -> "${(metres / 5).toInt() * 5} m"
}

private fun resolveHaulwayCurrentLocation(context: Context): Pair<Double, Double> {
    val fine = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarse = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    check(fine || coarse) { "Location permission is required to use CURRENT LOCATION. Enable location permission for HAULWAY and try again." }
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
    val candidates = listOf(android.location.LocationManager.GPS_PROVIDER, android.location.LocationManager.NETWORK_PROVIDER)
        .filter { provider -> try { manager.isProviderEnabled(provider) } catch (_: Exception) { false } }
        .mapNotNull { provider -> try { manager.getLastKnownLocation(provider) } catch (_: SecurityException) { null } }
    val location = candidates.maxByOrNull { it.time }
        ?: throw IllegalStateException("Current GPS position is not available yet. Open the MAP, wait for your blue location marker, then try again.")
    val ageMs = (System.currentTimeMillis() - location.time).coerceAtLeast(0L)
    check(ageMs <= 10 * 60 * 1000L) { "GPS position is older than 10 minutes. Wait for HAULWAY to update your location and try again." }
    return location.longitude to location.latitude
}

private suspend fun calculateRoadRoute(
    locations: List<String>,
    strategy: String,
    context: Context,
    liveOrigin: Pair<Double, Double>? = null
): RouteResult = withContext(Dispatchers.IO) {
    require(locations.size >= 2) { "Add at least a start and destination" }
    val apiKey = BuildConfig.ORS_API_KEY.trim()
    check(apiKey.isNotEmpty()) {
        "Missing ORS API key. Add ORS_API_KEY=your_key to the project local.properties file and sync Gradle."
    }

    // Read the currently selected, independently saved vehicle profile.
    val vehiclePrefs = context.getSharedPreferences("haulway_truck_profile", Context.MODE_PRIVATE)
    val selectedProfile = vehiclePrefs.getInt("selectedProfile", 0).coerceAtLeast(0)
    val profileTypes = (vehiclePrefs.getString("profileTypes", "ARTIC|RIGID|VAN|WAG & DRAG|BUS") ?: "ARTIC|RIGID|VAN|WAG & DRAG|BUS")
        .split("|").filter { it.isNotBlank() }
    val vehicleProfile = profileTypes.getOrNull(selectedProfile) ?: "ARTIC"
    fun defaultDimension(type: String, field: String): String = when (type) {
        "ARTIC" -> when (field) { "height" -> "4.00"; "width" -> "2.55"; else -> "16.50" }
        "RIGID" -> when (field) { "height" -> "3.80"; "width" -> "2.55"; else -> "9.20" }
        "VAN" -> when (field) { "height" -> "2.60"; "width" -> "2.20"; else -> "6.20" }
        "WAG & DRAG" -> when (field) { "height" -> "4.00"; "width" -> "2.55"; else -> "18.75" }
        else -> when (field) { "height" -> "3.70"; "width" -> "2.55"; else -> "13.50" }
    }
    val heightM = (vehiclePrefs.getString("profile_${selectedProfile}_height", defaultDimension(vehicleProfile, "height"))
        ?: defaultDimension(vehicleProfile, "height")).toDoubleOrNull() ?: 4.0
    val widthM = (vehiclePrefs.getString("profile_${selectedProfile}_width", defaultDimension(vehicleProfile, "width"))
        ?: defaultDimension(vehicleProfile, "width")).toDoubleOrNull() ?: 2.55
    val lengthM = (vehiclePrefs.getString("profile_${selectedProfile}_length", defaultDimension(vehicleProfile, "length"))
        ?: defaultDimension(vehicleProfile, "length")).toDoubleOrNull() ?: 16.5
    val defaultWeights = when (vehicleProfile) {
        "ARTIC" -> "44000" to "11500"
        "RIGID" -> "26000" to "10000"
        "VAN" -> "7500" to "2500"
        "WAG & DRAG" -> "44000" to "10000"
        else -> "18000" to "8000"
    }
    val grossKg = (vehiclePrefs.getString("profile_${selectedProfile}_gross", defaultWeights.first)
        ?: defaultWeights.first).toDoubleOrNull() ?: defaultWeights.first.toDouble()
    val axleKg = (vehiclePrefs.getString("profile_${selectedProfile}_axle", defaultWeights.second)
        ?: defaultWeights.second).toDoubleOrNull() ?: defaultWeights.second.toDouble()
    val hazmat = (vehiclePrefs.getString("loadType", "General") ?: "General").equals("Hazardous", ignoreCase = true)

    fun geocode(query: String): Pair<Double, Double> {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val connection = (URL("https://nominatim.openstreetmap.org/search?q=$encoded&format=jsonv2&limit=1").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("User-Agent", "HAULWAY-Navigation/1.0 (Android route planner)")
            connectTimeout = 15000
            readTimeout = 15000
        }
        try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            check(code in 200..299) { "Address search failed (HTTP $code)" }
            val results = org.json.JSONArray(body)
            check(results.length() > 0) { "Address not found: $query" }
            val first = results.getJSONObject(0)
            return first.getString("lon").toDouble() to first.getString("lat").toDouble()
        } finally { connection.disconnect() }
    }

    val points = mutableListOf<Pair<Double, Double>>()
    locations.forEachIndexed { index, place ->
        if (place.equals("CURRENT LOCATION", ignoreCase = true)) {
            points.add(liveOrigin ?: resolveHaulwayCurrentLocation(context))
        } else {
            if (index > 0) kotlinx.coroutines.delay(1100) // Public Nominatim usage policy.
            points.add(geocode(place))
        }
    }

    val coordinates = org.json.JSONArray().apply {
        points.forEach { (lon, lat) -> put(org.json.JSONArray().put(lon).put(lat)) }
    }
    val restrictions = JSONObject().apply {
        put("length", lengthM)
        put("width", widthM)
        put("height", heightM)
        put("weight", grossKg / 1000.0)
        put("axleload", axleKg / 1000.0)
        put("hazmat", hazmat)
    }
    val options = JSONObject().apply {
        put("vehicle_type", "hgv")
        put("profile_params", JSONObject().put("restrictions", restrictions))
        if (strategy == "AVOID") {
            put("avoid_features", org.json.JSONArray().put("ferries").put("tollways"))
        }
    }
    val requestBody = JSONObject().apply {
        put("coordinates", coordinates)
        put("preference", if (strategy == "SHORTEST") "shortest" else "fastest")
        put("instructions", true)
        put("instructions_format", "text")
        put("geometry", true)
        put("geometry_simplify", false)
        put("options", options)
    }

    val url = "https://api.heigit.org/openrouteservice/v2/directions/driving-hgv/geojson"
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        doOutput = true
        connectTimeout = 20000
        readTimeout = 45000
        setRequestProperty("Authorization", apiKey)
        setRequestProperty("Content-Type", "application/json; charset=utf-8")
        setRequestProperty("Accept", "application/geo+json, application/json")
        setRequestProperty("User-Agent", "HAULWAY-Navigation/1.0")
    }
    try {
        connection.outputStream.use { it.write(requestBody.toString().toByteArray(Charsets.UTF_8)) }
        val responseCode = connection.responseCode
        val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
        val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        check(responseCode in 200..299) {
            val apiMessage = try { JSONObject(body).optJSONObject("error")?.optString("message") ?: body } catch (_: Exception) { body }
            when (responseCode) {
                401, 403 -> "HeiGIT rejected the API key or Directions access (HTTP $responseCode). Check the key and enabled service in your dashboard."
                429 -> "HeiGIT API quota or rate limit reached (HTTP 429). Try again later."
                else -> "HGV route request failed (HTTP $responseCode): ${apiMessage.take(240)}"
            }
        }
        val root = JSONObject(body)
        val features = root.optJSONArray("features") ?: org.json.JSONArray()
        check(features.length() > 0) { "HeiGIT returned no HGV route." }
        val feature = features.getJSONObject(0)
        val geometry = feature.getJSONObject("geometry")
        val coords = geometry.getJSONArray("coordinates")
        val properties = feature.getJSONObject("properties")
        val summary = properties.optJSONObject("summary") ?: JSONObject()
        val segments = properties.optJSONArray("segments") ?: org.json.JSONArray()
        val steps = mutableListOf<String>()
        val maneuvers = mutableListOf<RouteManeuver>()
        val legSummaries = mutableListOf<Pair<String, String>>()
        var cumulativeMetres = 0.0
        for (i in 0 until segments.length()) {
            val segment = segments.getJSONObject(i)
            val segmentDistance = segment.optDouble("distance", 0.0)
            val segmentSeconds = segment.optDouble("duration", 0.0)
            val distanceLabel = if (segmentDistance >= 1609.344) String.format(java.util.Locale.UK, "%.1f mi", segmentDistance / 1609.344) else "${segmentDistance.toInt()} m"
            val timeLabel = if (segmentSeconds >= 3600) "${(segmentSeconds / 3600).toInt()}h ${((segmentSeconds % 3600) / 60).toInt()}m" else "${(segmentSeconds / 60).toInt()}m"
            legSummaries.add(distanceLabel to timeLabel)
            val segmentSteps = segment.optJSONArray("steps") ?: org.json.JSONArray()
            for (j in 0 until segmentSteps.length()) {
                val step = segmentSteps.getJSONObject(j)
                val instruction = step.optString("instruction").ifBlank { step.optString("name", "Continue") }
                val roadName = step.optString("name").ifBlank { "Road" }
                val stepMetres = step.optDouble("distance", 0.0)
                val dist = if (stepMetres >= 1000) String.format(java.util.Locale.UK, "%.1f km", stepMetres / 1000) else "${stepMetres.toInt()} m"
                val wayPoints = step.optJSONArray("way_points")
                val coordinateIndex = wayPoints?.optInt(0, -1) ?: -1
                if (coordinateIndex in 0 until coords.length()) {
                    val point = coords.getJSONArray(coordinateIndex)
                    maneuvers.add(RouteManeuver(
                        label = instruction, roadName = roadName,
                        latitude = point.getDouble(1), longitude = point.getDouble(0),
                        routeDistanceMetres = cumulativeMetres
                    ))
                }
                if (steps.size < 40) steps.add("$dist · $instruction")
                cumulativeMetres += stepMetres
            }
        }
        val metres = summary.optDouble("distance", 0.0)
        val seconds = summary.optDouble("duration", 0.0)
        check(metres > 0.0 && seconds > 0.0) { "HeiGIT returned an incomplete route summary." }
        val distance = String.format(java.util.Locale.UK, "%.1f mi", metres / 1609.344)
        val duration = "${(seconds / 3600).toInt()} h ${((seconds % 3600) / 60).toInt()} min"
        RouteResult(coords.toString(), distance, duration, steps, maneuvers, coordinates.toString(), legSummaries, metres, seconds)
    } finally { connection.disconnect() }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableImmersiveMode()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S &&
            checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), 4102)
        }

        setContent {
            var startupComplete by remember { mutableStateOf(false) }
            if (startupComplete) {
                HaulwayMapScreen()
            } else {
                HaulwayStartupScreen(onFinished = { startupComplete = true })
            }
        }
    }

    // Keep the current Compose navigation session alive when the tablet rotates.
    // Handling these configuration changes here prevents Activity recreation,
    // which would otherwise restart the navigation UI and reset in-memory progress.
    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        enableImmersiveMode()
    }

    private fun enableImmersiveMode() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enableImmersiveMode()
    }
}


@Composable
private fun HaulwayStartupScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    var progress by remember { mutableIntStateOf(8) }
    var status by remember { mutableStateOf("STARTING HAULWAY…") }

    // Keep the branded artwork visible while only lightweight, real startup checks run.
    // Do not create and destroy a dummy WebView here: it delays launch and does not warm
    // the actual map instance shown later.
    LaunchedEffect(Unit) {
        val startupStartedAt = android.os.SystemClock.elapsedRealtime()
        try {
            status = "READING VEHICLE AND APP SETTINGS"
            withContext(Dispatchers.IO) {
                context.getSharedPreferences("haulway_settings", Context.MODE_PRIVATE).all
                context.getSharedPreferences("haulway_truck_profile", Context.MODE_PRIVATE).all
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            progress = 45

            status = "CHECKING LOCATION PERMISSIONS"
            val fine = androidx.core.content.ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val coarse = androidx.core.content.ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            context.getSharedPreferences("haulway_startup", Context.MODE_PRIVATE)
                .edit().putBoolean("location_permission_available", fine || coarse).apply()
            progress = 72

            status = "CHECKING MAP CONNECTION"
            val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val activeNetwork = connectivity.activeNetwork
            val hasNetwork = activeNetwork != null &&
                    connectivity.getNetworkCapabilities(activeNetwork)?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            context.getSharedPreferences("haulway_startup", Context.MODE_PRIVATE)
                .edit().putBoolean("network_available_at_start", hasNetwork).apply()
            progress = 88

            status = "PREPARING NAVIGATION DISPLAY"
            check(context.resources.getIdentifier("haulway_splash_background", "drawable", context.packageName) != 0)
            progress = 100
            status = if (hasNetwork) "HAULWAY READY" else "READY — OFFLINE MAP"
        } catch (_: Exception) {
            status = "STARTING WITH LIMITED FEATURES"
            progress = 100
        }
        // Keep the branded intro on screen long enough to read; this is a minimum
        // presentation time, not simulated progress. The bar itself follows completed checks.
        val remaining = 3600L - (android.os.SystemClock.elapsedRealtime() - startupStartedAt)
        if (remaining > 0) kotlinx.coroutines.delay(remaining)
        onFinished()
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress.toFloat() / 100f,
        animationSpec = tween(durationMillis = 2500),
        label = "startupProgress"
    )
    BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xFF030A12))) {
        Image(
            painter = painterResource(R.drawable.haulway_splash_background),
            contentDescription = "HAULWAY truck navigation",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Live progress replaces the static bar removed from the background artwork.
        val barHeight = maxHeight * 0.047f
        Box(
            Modifier.align(Alignment.BottomCenter)
                .fillMaxWidth(0.46f).height(maxHeight * 0.068f)
                .offset(y = -(maxHeight * 0.12f))
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xF20A1723))
                .padding(horizontal = 8.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier.fillMaxWidth().height(barHeight * 0.72f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF142B3B))
            )
            Box(
                Modifier.align(Alignment.CenterStart).fillMaxWidth(animatedProgress)
                    .height(barHeight * 0.72f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF00A9FF), Color(0xFF00D5FF))))
            )
        }
        Text(
            "${(animatedProgress * 100).toInt()}%",
            modifier = Modifier.align(Alignment.BottomCenter).offset(y = -(maxHeight * 0.074f)),
            color = Color(0xFFEAF7FF), fontSize = 11.sp, fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp
        )
        // This label is rendered by Compose (not baked into the JPG), so its
        // position can be tuned independently and remains at the bottom edge.
        Text(
            text = "LOADING MAPS AND ROUTES...",
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(bottom = maxHeight * 0.022f),
            color = Color(0xFFD8E9F4),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 2.0.sp,
            maxLines = 1
        )
    }
}

@Composable
fun HaulwayMapScreen() {
    var activeScreen by remember { mutableStateOf("HOME") }
    when (activeScreen) {
        "HOME" -> HaulwayHomeScreen(onNavigate = { activeScreen = it })
        "MAP" -> DrivingNavigationScreen(onNavigate = { activeScreen = it })
        "360 STREET VIEW" -> HaulwayStreetImageryScreen(onBack = { activeScreen = "HOME" })
        else -> HaulwaySectionScreen(
            section = activeScreen,
            onBack = { activeScreen = "MAP" },
            onNavigate = { activeScreen = it }
        )
    }
}

private fun maneuverIconFor(instruction: String): ImageVector {
    val text = instruction.lowercase(Locale.UK)
    return when {
        "roundabout" in text -> Icons.Default.RoundaboutRight
        "uturn" in text || "u-turn" in text -> Icons.Default.AltRoute
        "left" in text -> Icons.Default.TurnLeft
        "right" in text -> Icons.Default.TurnRight
        "straight" in text || "continue" in text -> Icons.Default.ArrowUpward
        else -> Icons.Default.Navigation
    }
}

@Composable
private fun DrivingNavigationScreen(onNavigate: (String) -> Unit) {
    val appContext: Context = LocalContext.current
    val mapSearchScope = rememberCoroutineScope()
    var zoom by remember { mutableIntStateOf(0) }
    var showMapSearch by remember { mutableStateOf(false) }
    var mapSearchText by remember { mutableStateOf("") }
    var mapSearchResults by remember { mutableStateOf<List<MapSearchResult>>(emptyList()) }
    var mapSearchLoading by remember { mutableStateOf(false) }
    var mapSearchError by remember { mutableStateOf<String?>(null) }
    var selectedMapSearchLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var mapFollowRequest by remember { mutableIntStateOf(0) }
    var mapStyleRequest by remember { mutableIntStateOf(0) }
    var headingUp by remember { mutableStateOf(true) }
    var followEnabled by remember { mutableStateOf(true) }
    var tilt3D by remember { mutableStateOf(true) }
    var remainingDistanceMetres by remember { mutableStateOf<Double?>(null) }
    var remainingDurationSeconds by remember { mutableStateOf<Double?>(null) }
    var clockText by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.UK).format(Date())) }
    LaunchedEffect(Unit) { while (true) { clockText = SimpleDateFormat("HH:mm", Locale.UK).format(Date()); kotlinx.coroutines.delay(1000) } }
    var soundOn by remember { mutableStateOf(true) }
    var liveSpeedMph by remember { mutableStateOf<Double?>(null) }
    var liveLatitude by remember { mutableStateOf<Double?>(null) }
    var liveLongitude by remember { mutableStateOf<Double?>(null) }
    var liveBearing by remember { mutableStateOf<Float?>(null) }
    var liveAccuracy by remember { mutableStateOf<Float?>(null) }
    var activeManeuverIndex by remember { mutableIntStateOf(0) }
    var distanceToManeuver by remember { mutableStateOf<Double?>(null) }
    var offRoute by remember { mutableStateOf(false) }
    var offRouteReadings by remember { mutableIntStateOf(0) }
    var rerouteInProgress by remember { mutableStateOf(false) }
    var lastRerouteAt by remember { mutableLongStateOf(0L) }
    var speechEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var speechReady by remember { mutableStateOf(false) }
    var lastSpokenStage by remember { mutableStateOf("") }
    DisposableEffect(appContext) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(appContext.applicationContext) { status ->
            speechReady = status == TextToSpeech.SUCCESS
            if (status == TextToSpeech.SUCCESS) engine?.language = Locale.UK
        }
        speechEngine = engine
        onDispose {
            engine?.stop()
            engine?.shutdown()
            speechEngine = null
            speechReady = false
        }
    }
    LaunchedEffect(RoutePlanStore.coordinatesJson) {
        activeManeuverIndex = 0
        distanceToManeuver = null
        lastSpokenStage = ""
        offRoute = false
        offRouteReadings = 0
    }
    var hasLocationPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    androidx.core.content.ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        hasLocationPermission = result.values.any { it }
    }
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    // Show vehicle speed only when Android supplies a real GPS speed reading.
    DisposableEffect(hasLocationPermission) {
        val fineGranted = androidx.core.content.ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = androidx.core.content.ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val locationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        val sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val displayRotation = (appContext.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager).defaultDisplay.rotation
        var latestGpsSpeedMps = 0f
        var smoothedSpeedMph: Double? = null
        var lastAcceptedLocation: android.location.Location? = null
        val orientationListener = object : SensorEventListener {
            private val rotationMatrix = FloatArray(9)
            private val screenRotationMatrix = FloatArray(9)
            private val orientation = FloatArray(3)
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR || latestGpsSpeedMps >= 1.5f) return
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val axes = when (displayRotation) {
                    android.view.Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
                    android.view.Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
                    android.view.Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
                    else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
                }
                SensorManager.remapCoordinateSystem(rotationMatrix, axes.first, axes.second, screenRotationMatrix)
                SensorManager.getOrientation(screenRotationMatrix, orientation)
                val heading = ((Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f)
                liveBearing = heading
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        rotationSensor?.let { sensorManager.registerListener(orientationListener, it, SensorManager.SENSOR_DELAY_UI) }
        val gpsProviderEnabled = fineGranted && try {
            locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
        } catch (_: Exception) { false }
        val listener = object : android.location.LocationListener {
            override fun onLocationChanged(location: android.location.Location) {
                // When GNSS is available, never let a coarse network fix overwrite it.
                // Network location is only a fallback when the device has no GPS provider.
                if (gpsProviderEnabled && location.provider != android.location.LocationManager.GPS_PROVIDER) return
                val ageMs = (System.currentTimeMillis() - location.time).coerceAtLeast(0L)
                if (ageMs > 6_000L) return
                val accuracy = if (location.hasAccuracy()) location.accuracy else 999f
                if (accuracy > 50f) { liveAccuracy = accuracy; return }
                val previous = lastAcceptedLocation
                if (previous != null) {
                    val elapsed = ((location.time - previous.time).coerceAtLeast(1L)) / 1000.0
                    val jumpSpeed = distanceMetres(previous.latitude, previous.longitude, location.latitude, location.longitude) / elapsed
                    if (elapsed < 8.0 && jumpSpeed > 55.0) return
                }
                lastAcceptedLocation = android.location.Location(location)
                latestGpsSpeedMps = if (location.hasSpeed()) location.speed else if (previous != null) {
                    (distanceMetres(previous.latitude, previous.longitude, location.latitude, location.longitude) /
                        ((location.time - previous.time).coerceAtLeast(1L) / 1000.0)).toFloat().coerceIn(0f, 55f)
                } else 0f
                val rawSpeedMph = (latestGpsSpeedMps * 2.236936).coerceAtLeast(0.0)
                smoothedSpeedMph = smoothedSpeedMph?.let { it * 0.65 + rawSpeedMph * 0.35 } ?: rawSpeedMph
                liveSpeedMph = smoothedSpeedMph
                liveLatitude = location.latitude
                liveLongitude = location.longitude
                if (location.hasBearing() && latestGpsSpeedMps >= 1.5f) liveBearing = location.bearing
                liveAccuracy = accuracy
                val activeRouteJson = RoutePlanStore.coordinatesJson
                if (!activeRouteJson.isNullOrBlank() && RoutePlanStore.totalDistanceMetres > 0.0) {
                    try {
                        val routePoints = org.json.JSONArray(activeRouteJson)
                        if (routePoints.length() >= 2) {
                            val endPoint = routePoints.getJSONArray(routePoints.length() - 1)
                            val remaining = distanceAlongRouteMetres(
                                location.latitude, location.longitude,
                                endPoint.getDouble(1), endPoint.getDouble(0), activeRouteJson
                            ) ?: distanceMetres(location.latitude, location.longitude, endPoint.getDouble(1), endPoint.getDouble(0))
                            val boundedRemaining = remaining.coerceIn(0.0, RoutePlanStore.totalDistanceMetres)
                            remainingDistanceMetres = boundedRemaining
                            val ratio = (boundedRemaining / RoutePlanStore.totalDistanceMetres).coerceIn(0.0, 1.0)
                            remainingDurationSeconds = RoutePlanStore.totalDurationSeconds * ratio
                        }
                    } catch (_: Exception) { }
                } else {
                    remainingDistanceMetres = null
                    remainingDurationSeconds = null
                }
                val distanceFromRoute = distanceToRouteMetres(location.latitude, location.longitude, RoutePlanStore.coordinatesJson)
                if (distanceFromRoute != null && accuracy <= 45f) {
                    if (distanceFromRoute > maxOf(75.0, accuracy.toDouble() * 1.8)) offRouteReadings++ else offRouteReadings = 0
                    if (offRouteReadings >= 3) offRoute = true
                    if (distanceFromRoute < maxOf(35.0, accuracy.toDouble())) { offRoute = false; offRouteReadings = 0 }
                    // Recalculate from the live position to the saved destination. Never wait
                    // for the driver to return to the original route. Throttle API requests.
                    val now = System.currentTimeMillis()
                    if (offRoute && !rerouteInProgress && now - lastRerouteAt > 20_000L &&
                        RoutePlanStore.destination.isNotBlank() && BuildConfig.ORS_API_KEY.isNotBlank()) {
                        rerouteInProgress = true
                        lastRerouteAt = now
                        mapSearchScope.launch {
                            try {
                                val updated = calculateRoadRoute(
                                    listOf("CURRENT LOCATION", RoutePlanStore.destination),
                                    "FASTEST", appContext, location.longitude to location.latitude
                                )
                                RoutePlanStore.coordinatesJson = updated.coordinatesJson
                                RoutePlanStore.summary = "${updated.distanceText} · ${updated.durationText}"
                                RoutePlanStore.totalDistanceMetres = updated.totalDistanceMetres
                                RoutePlanStore.totalDurationSeconds = updated.totalDurationSeconds
                                remainingDistanceMetres = updated.totalDistanceMetres
                                remainingDurationSeconds = updated.totalDurationSeconds
                                RoutePlanStore.steps = updated.steps
                                RoutePlanStore.maneuvers = updated.maneuvers
                                RoutePlanStore.origin = "CURRENT LOCATION"
                                activeManeuverIndex = 0
                                offRoute = false
                                offRouteReadings = 0
                                lastSpokenStage = ""
                            } catch (error: Exception) {
                                // Keep the warning visible and allow a later retry.
                                lastRerouteAt = System.currentTimeMillis() - 15_000L
                            } finally { rerouteInProgress = false }
                        }
                    }
                } else {
                    offRouteReadings = 0
                    if (RoutePlanStore.coordinatesJson == null) offRoute = false
                }
                val maneuvers = RoutePlanStore.maneuvers
                if (maneuvers.isNotEmpty()) {
                    var index = activeManeuverIndex.coerceIn(0, maneuvers.lastIndex)
                    fun distanceTo(target: RouteManeuver): Double =
                        distanceAlongRouteMetres(
                            location.latitude, location.longitude,
                            target.latitude, target.longitude,
                            RoutePlanStore.coordinatesJson
                        ) ?: distanceMetres(
                            location.latitude, location.longitude,
                            target.latitude, target.longitude
                        )
                    while (index < maneuvers.lastIndex && distanceTo(maneuvers[index]) < 35.0) index++
                    activeManeuverIndex = index
                    val remainingMetres = distanceTo(maneuvers[index]).coerceAtLeast(0.0)
                    distanceToManeuver = remainingMetres
                    // Announce each approach stage once per maneuver. The spoken distance is
                    // deliberately approximate; the visual distance remains continuously updated.
                    // One clear spoken approach cue at 500 m; avoid stacked NOW/300 m prompts.
                    val stage = if (remainingMetres <= 500.0) "500" else ""
                    val speechKey = "$index:$stage"
                    if (soundOn && speechReady && stage.isNotEmpty() && speechKey != lastSpokenStage) {
                        val target = maneuvers[index]
                        val action = target.label.lowercase(Locale.UK)
                        val road = target.roadName.takeIf { it.isNotBlank() }?.let { " onto $it" } ?: ""
                        val message = "In 500 metres, $action$road"
                        speechEngine?.speak(message, TextToSpeech.QUEUE_FLUSH, null, speechKey)
                        lastSpokenStage = speechKey
                    }
                } else {
                    distanceToManeuver = null
                    activeManeuverIndex = 0
                    lastSpokenStage = ""
                }
            }
            @Deprecated("Deprecated in Android") override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) { liveSpeedMph = null }
        }
        if (fineGranted || coarseGranted) {
            try {
                val providersToUse = buildList {
                    if (gpsProviderEnabled) {
                        add(android.location.LocationManager.GPS_PROVIDER)
                    } else if (locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
                        // Coarse network positioning is explicitly a fallback, not a competing source.
                        add(android.location.LocationManager.NETWORK_PROVIDER)
                    }
                }
                // Only seed from a fresh cached fix; never centre the truck on a minutes-old position.
                val cached = providersToUse.mapNotNull { provider -> try { locationManager.getLastKnownLocation(provider) } catch (_: SecurityException) { null } }
                    .filter { System.currentTimeMillis() - it.time in 0..30_000L }
                    .maxByOrNull { it.time }
                if (cached != null) listener.onLocationChanged(cached)
                providersToUse.forEach { provider ->
                    val isGps = provider == android.location.LocationManager.GPS_PROVIDER
                    locationManager.requestLocationUpdates(provider, if (isGps) 1000L else 3000L, if (isGps) 0.5f else 10f, listener, Looper.getMainLooper())
                }
            } catch (_: SecurityException) { liveSpeedMph = null }
        }
        onDispose {
            try { locationManager.removeUpdates(listener) } catch (_: SecurityException) { }
            sensorManager.unregisterListener(orientationListener)
        }
    }
    var selectedNav by remember { mutableStateOf("MAP") }
    val panel = Color(0xE9161D23)
    val border = Color(0x665B7382)
    val blue = Color(0xFF078FE0)

    Column(Modifier.fillMaxSize().background(Color(0xFF080E13)).padding(5.dp)) {
        Row(
            Modifier.fillMaxWidth().height(30.dp).padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("HAUL", color = White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Text("WAY", color = Color(0xFF168FE8), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.weight(1f))
            Text(clockText, color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("HGV NAVIGATION", color = Color(0xFF9FB0BC), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
        Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            // Map canvas and overlays occupy most of the display.
            Box(Modifier.weight(0.65f).fillMaxHeight().clip(RoundedCornerShape(12.dp))) {
                RealNavigationMap(zoom, liveLatitude, liveLongitude, liveBearing, liveAccuracy, mapFollowRequest, mapStyleRequest, headingUp, followEnabled, tilt3D, selectedMapSearchLocation)

                // Address search is directly accessible from the map, without leaving navigation.
                Surface(
                    // Keep SEARCH below the manoeuvre banner so it is never hidden behind it.
                    Modifier.align(Alignment.TopStart).padding(start = 12.dp, top = 68.dp).size(44.dp).clickable {
                        showMapSearch = true
                        mapSearchError = null
                        selectedMapSearchLocation = null
                    },
                    shape = RoundedCornerShape(9.dp), color = Color(0xE9161D23),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF168FE8)),
                    shadowElevation = 5.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Search, contentDescription = "Search address or postcode", tint = White, modifier = Modifier.size(25.dp))
                    }
                }

                if (offRoute) {
                    Surface(
                        Modifier.align(Alignment.TopCenter).padding(top = 12.dp, start = 12.dp, end = 12.dp),
                        shape = RoundedCornerShape(8.dp), color = Color(0xF2A94B12),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB15C))
                    ) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (rerouteInProgress) "OFF ROUTE — finding a new HGV route…" else "OFF ROUTE — recalculation will retry automatically", color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Single speed cluster. The old duplicate mini trip/ETA panel is removed.
                Column(Modifier.align(Alignment.CenterStart).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.Start) {
                    Surface(
                        modifier = Modifier.size(54.dp), shape = CircleShape, color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(4.dp, Color(0xFFD93636)),
                        shadowElevation = 7.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("—", color = Color(0xFF20252A), fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Surface(shape = RoundedCornerShape(7.dp), color = Color(0xE9161D23), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF526575))) {
                        Text(
                            liveSpeedMph?.let { String.format(Locale.UK, "%.0f mph", it) } ?: "GPS —",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            color = White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Voice control sits near the top-right of the map, as in the Vision 2 reference.
                Surface(
                    Modifier.align(Alignment.TopEnd).padding(top = 12.dp, end = 12.dp).size(38.dp).clickable { soundOn = !soundOn },
                    shape = CircleShape, color = panel, border = androidx.compose.foundation.BorderStroke(1.dp, border)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(if (soundOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            if (soundOn) "Voice guidance on" else "Voice guidance off", tint = White)
                    }
                }

                // Map controls on lower-right edge of map.
                Column(Modifier.align(Alignment.CenterEnd).padding(end = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(Modifier.size(38.dp).clickable { mapStyleRequest++ }, shape = RoundedCornerShape(8.dp), color = panel) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Layers, "Switch road / satellite map", tint = White) }
                    }
                    Surface(Modifier.size(38.dp).clickable { headingUp = !headingUp }, shape = RoundedCornerShape(8.dp), color = if (headingUp) Color(0xFF087CCB) else panel) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Explore, if (headingUp) "Vehicle marker follows compass heading; map stays north-up" else "Vehicle marker points north; map stays north-up", tint = White) }
                    }
                    Surface(Modifier.size(38.dp).clickable {
                        followEnabled = true
                        mapFollowRequest++
                    }, shape = RoundedCornerShape(8.dp), color = Color(0xFF087CCB)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.MyLocation, "Follow vehicle / recenter map", tint = White) }
                    }
                    Surface(Modifier.size(38.dp).clickable { tilt3D = !tilt3D }, shape = RoundedCornerShape(8.dp), color = if (tilt3D) Color(0xFF087CCB) else panel) {
                        Box(contentAlignment = Alignment.Center) { Text(if (tilt3D) "3D" else "2D", color = White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold) }
                    }
                    Surface(Modifier.size(38.dp).clickable { zoom = (zoom + 1).coerceAtMost(6) }, shape = RoundedCornerShape(8.dp), color = panel) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Add, "Zoom in", tint = White) }
                    }
                    Surface(Modifier.size(38.dp).clickable { zoom = (zoom - 1).coerceAtLeast(-5) }, shape = RoundedCornerShape(8.dp), color = panel) {
                        Box(contentAlignment = Alignment.Center) { Text("−", color = White, fontSize = 27.sp, fontWeight = FontWeight.Medium) }
                    }
                }
                // The live vehicle marker is drawn by the map only after a real GPS fix.
            }

            // Compact right-hand information rail: upcoming turns, parking and services.
            Column(
                Modifier.weight(0.35f).fillMaxHeight().clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0B1117)).padding(5.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(Modifier.fillMaxWidth().height(22.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SignalCellularAlt, null, tint = Color.LightGray, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(5.dp)); Icon(Icons.Default.Wifi, null, tint = Color.LightGray, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(5.dp)); Icon(Icons.Default.BatteryFull, null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                }
                if (RoutePlanStore.maneuvers.isNotEmpty()) {
                    val maneuver = RoutePlanStore.maneuvers.getOrNull(activeManeuverIndex)
                    if (maneuver != null) {
                        val distanceLabel = distanceToManeuver?.let(::formatNavigationDistance) ?: "—"
                        NavigationStepCard(
                            maneuverIconFor(maneuver.label), distanceLabel,
                            maneuver.label + if (maneuver.roadName.isNotBlank()) " · ${maneuver.roadName}" else "",
                            blue
                        )
                    }
                } else {
                    NavigationStepCard(Icons.Default.Navigation, "—", "Waiting for route", blue)
                }
                MapQuickActionCard(
                    icon = Icons.Default.LocalParking, title = "Truck parking", subtitle = "Find nearby parking",
                    onClick = { onNavigate("TRUCK PARKING") }
                )
                MapQuickActionCard(
                    icon = Icons.Default.LocalGasStation, title = "Services", subtitle = "Fuel, food & facilities",
                    onClick = { onNavigate("SERVICES & POI") }
                )
                Spacer(Modifier.weight(1f))
                Text("DESTINATION", color = Color(0xFF91A2AE), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
                Text(
                    if (RoutePlanStore.coordinatesJson != null) RoutePlanStore.destination else "No destination set",
                    color = White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(4.dp), maxLines = 2
                )
                RoutePlanStore.summary?.takeIf { RoutePlanStore.coordinatesJson != null }?.let {
                    val distanceLabel = remainingDistanceMetres?.let(::formatNavigationDistance)
                        ?: RoutePlanStore.summary?.substringBefore(" · ") ?: "—"
                    val secondsLeft = remainingDurationSeconds
                    val etaLabel = if (secondsLeft != null) {
                        val mins = (secondsLeft / 60.0).toInt().coerceAtLeast(0)
                        "${mins / 60} h ${mins % 60} min"
                    } else RoutePlanStore.summary?.substringAfter(" · ", "—") ?: "—"
                    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), color = Color(0xFF123149), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF168FE8))) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 7.dp, vertical = 9.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("DISTANCE LEFT", color = Color(0xFF9FC5DC), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(distanceLabel, color = White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                            Spacer(Modifier.width(5.dp))
                            Column(Modifier.weight(1f)) {
                                Text("ETA / DURATION", color = Color(0xFF9FC5DC), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(etaLabel, color = White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        // Keep a visible gap between the map/info workspace and bottom navigation tiles.
        Spacer(Modifier.height(8.dp))

        // Persistent six-tab navigation bar restored below the driving map.
        Row(
            Modifier.fillMaxWidth().height(43.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val tabs = listOf(
                Triple("Home", Icons.Default.Home, "HOME"),
                Triple("Map", Icons.Default.Map, "MAP"),
                Triple("Route", Icons.Default.Route, "ROUTE PLANNER"),
                Triple("Services", Icons.Default.Build, "SERVICES & POI"),
                Triple("Traffic", Icons.Default.Warning, "TRAFFIC"),
                Triple("More", Icons.Default.MoreHoriz, "SETTINGS")
            )
            tabs.forEach { tab ->
                val selected = tab.third == "MAP"
                Surface(
                    Modifier.weight(1f).fillMaxHeight().clickable {
                        if (!selected) onNavigate(tab.third)
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = if (selected) Color(0xFF087CCB) else Color(0xE91B252E),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0x554F6472))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(tab.second, contentDescription = tab.first, tint = White, modifier = Modifier.size(20.dp))
                        Text(tab.first, color = White, fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                    }
                }
            }
        }
    }

    if (showMapSearch) {
        // Global search-as-you-type: debounce input and refresh address / place suggestions automatically.
        LaunchedEffect(mapSearchText, showMapSearch) {
            val query = mapSearchText.trim()
            if (query.length < 3) {
                mapSearchResults = emptyList()
                mapSearchError = null
                mapSearchLoading = false
            } else {
                kotlinx.coroutines.delay(450)
                mapSearchLoading = true
                mapSearchError = null
                try {
                    val results = searchMapPlaces(query, liveLatitude, liveLongitude)
                    if (mapSearchText.trim() == query) {
                        mapSearchResults = results
                        mapSearchError = if (results.isEmpty()) "No matching addresses found" else null
                    }
                } catch (e: Exception) {
                    if (mapSearchText.trim() == query) mapSearchError = e.localizedMessage ?: "Address search failed"
                } finally {
                    if (mapSearchText.trim() == query) mapSearchLoading = false
                }
            }
        }
        Dialog(onDismissRequest = { showMapSearch = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.78f),
                color = Color(0xFF18232C), shape = RoundedCornerShape(14.dp), tonalElevation = 12.dp
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, null, tint = Color(0xFF48B9E8))
                        Spacer(Modifier.width(8.dp))
                        Text("SEARCH MAP", color = White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("Find an address, town or postcode", color = Color(0xFFB7C4CD), fontSize = 12.sp)
                    OutlinedTextField(
                        value = mapSearchText, onValueChange = { mapSearchText = it; mapSearchError = null },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        placeholder = { Text("Start typing a postcode, street or address", color = Color(0xFF82919B)) },
                        label = { Text("Address or postcode") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = White, unfocusedTextColor = White,
                            focusedBorderColor = Color(0xFF48B9E8), unfocusedBorderColor = Color(0xFF53616D),
                            focusedLabelColor = Color(0xFF70C9FF), unfocusedLabelColor = Color(0xFFB7C4CD), cursorColor = White
                        )
                    )
                    Button(
                        onClick = {
                            if (mapSearchText.isNotBlank()) {
                                mapSearchLoading = true; mapSearchError = null
                                mapSearchScope.launch {
                                    try {
                                        mapSearchResults = searchMapPlaces(mapSearchText.trim(), liveLatitude, liveLongitude)
                                        if (mapSearchResults.isEmpty()) mapSearchError = "No matching addresses found"
                                    } catch (e: Exception) { mapSearchError = e.localizedMessage ?: "Address search failed" }
                                    finally { mapSearchLoading = false }
                                }
                            }
                        }, enabled = mapSearchText.trim().length >= 3 && !mapSearchLoading,
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF087CCB))
                    ) {
                        if (mapSearchLoading) CircularProgressIndicator(Modifier.size(16.dp), color = White, strokeWidth = 2.dp)
                        else Icon(Icons.Default.Search, null, tint = White)
                        Spacer(Modifier.width(7.dp)); Text("SEARCH", color = White, fontWeight = FontWeight.Bold)
                    }
                    mapSearchError?.let { Text(it, color = Color(0xFFFF9B8F), fontSize = 12.sp) }
                    if (mapSearchResults.isNotEmpty()) {
                        Text("SELECT A DESTINATION — HAULWAY WILL CALCULATE THE HGV ROUTE", color = Color(0xFF70C9FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Column(Modifier.fillMaxWidth().heightIn(max = 220.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            mapSearchResults.forEach { place ->
                                Surface(Modifier.fillMaxWidth().clickable {
                                    // Selecting a map-search suggestion must calculate a real HGV route,
                                    // not merely move the map camera to the searched coordinate.
                                    mapSearchLoading = true
                                    mapSearchError = null
                                    mapSearchScope.launch {
                                        try {
                                            check(liveLatitude != null && liveLongitude != null) {
                                                "GPS position is not ready. Wait for the blue vehicle marker, then select the address again."
                                            }
                                            val calculated = calculateRoadRoute(
                                                listOf("CURRENT LOCATION", place.name),
                                                "FASTEST",
                                                appContext
                                            )
                                            RoutePlanStore.coordinatesJson = calculated.coordinatesJson
                                            RoutePlanStore.summary = "${calculated.distanceText} · ${calculated.durationText}"
                                            RoutePlanStore.totalDistanceMetres = calculated.totalDistanceMetres
                                            RoutePlanStore.totalDurationSeconds = calculated.totalDurationSeconds
                remainingDistanceMetres = calculated.totalDistanceMetres
                                            remainingDurationSeconds = calculated.totalDurationSeconds
                                            RoutePlanStore.steps = calculated.steps
                                            RoutePlanStore.maneuvers = calculated.maneuvers
                                            RoutePlanStore.origin = "CURRENT LOCATION"
                                            RoutePlanStore.destination = place.name
                                            // The route renderer frames the complete route itself.
                                            selectedMapSearchLocation = null
                                            mapSearchResults = emptyList()
                                            showMapSearch = false
                                        } catch (e: Exception) {
                                            mapSearchError = e.localizedMessage ?: "Could not calculate HGV route"
                                        } finally {
                                            mapSearchLoading = false
                                        }
                                    }
                                }, color = Color(0xFF26333D), shape = RoundedCornerShape(7.dp)) {
                                    Text(place.name, color = White, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                                }
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Text("CLOSE", color = Color(0xFF70C9FF), fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { showMapSearch = false }.padding(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MapQuickActionCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(54.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp), color = Color(0xE91B242C),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0x554F606D))
    ) {
        Row(Modifier.padding(horizontal = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = Color(0xFF087CCB), shape = RoundedCornerShape(4.dp), modifier = Modifier.size(32.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = White, modifier = Modifier.size(19.dp)) }
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(subtitle, color = Color(0xFFD0D7DD), fontSize = 9.sp, maxLines = 2)
            }
        }
    }
}

@Composable
private fun NavigationStepCard(icon: ImageVector, distance: String, destination: String, accent: Color, isParking: Boolean = false) {
    Surface(Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(6.dp), color = Color(0xE91B242C), border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0x554F606D))) {
        Row(Modifier.padding(horizontal = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            if (isParking) {
                Surface(color = Color(0xFF087CCB), shape = RoundedCornerShape(3.dp), modifier = Modifier.size(26.dp)) {
                    Box(contentAlignment = Alignment.Center) { Text("P", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                }
            } else Icon(icon, null, tint = White, modifier = Modifier.size(23.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text(distance, color = White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(destination, color = Color(0xFFD0D7DD), fontSize = 10.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun RealNavigationMap(zoom: Int, latitude: Double?, longitude: Double?, bearing: Float?, accuracy: Float?, followRequest: Int, mapStyleRequest: Int, headingUp: Boolean, followEnabled: Boolean, tilt3D: Boolean, searchLocation: Pair<Double, Double>?) {
    val context = LocalContext.current
    var pageReady by remember { mutableStateOf(false) }
    var lastSentRoute by remember { mutableStateOf<String?>(null) }
    var lastSentZoom by remember { mutableStateOf<Int?>(null) }
    var lastSentFollowRequest by remember { mutableStateOf<Int?>(null) }
    var lastSentMapStyleRequest by remember { mutableStateOf<Int?>(null) }
    val mapPrefs = remember { context.getSharedPreferences("haulway_settings_v10", Context.MODE_PRIVATE) }
    var lastSentAppearance by remember { mutableStateOf<String?>(null) }
    var lastSentHeadingUp by remember { mutableStateOf<Boolean?>(null) }
    var lastSentFollowEnabled by remember { mutableStateOf<Boolean?>(null) }
    var lastSentTilt3D by remember { mutableStateOf<Boolean?>(null) }
    var lastSentSearchLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    AndroidView(
        modifier = Modifier.fillMaxSize().background(Color(0xFF101B24)),
        factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(android.graphics.Color.rgb(16, 27, 36))
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.setGeolocationEnabled(true)
                settings.loadsImagesAutomatically = true
                settings.blockNetworkImage = false
                settings.allowContentAccess = true
                settings.javaScriptCanOpenWindowsAutomatically = false
                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
                settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                settings.setSupportZoom(false)
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView, url: String?, favicon: android.graphics.Bitmap?) {
                        pageReady = false
                        lastSentZoom = null
                        lastSentRoute = null
                        android.util.Log.i("HAULWAY_MAP", "Map page started: $url")
                    }
                    override fun onPageFinished(view: WebView, url: String?) {
                        pageReady = true
                        android.util.Log.i("HAULWAY_MAP", "Map document ready: $url")
                        view.evaluateJavascript("(function(){return 'doc='+document.readyState+', leaflet='+typeof window.L+', map='+!!document.getElementById('map')})()") { result ->
                            android.util.Log.i("HAULWAY_MAP", "Page diagnostic: $result")
                            view.evaluateJavascript("(function(){var m=document.getElementById('map'),t=document.getElementById('tiles'),w=document.getElementById('tileworld');return 'viewport='+m.clientWidth+'x'+m.clientHeight+',tiles='+t.children.length+',world='+getComputedStyle(w).transform})()") { geometry ->
                                android.util.Log.i("HAULWAY_MAP", "Render geometry: $geometry")
                            }
                        }
                    }
                    override fun onReceivedError(view: WebView, request: android.webkit.WebResourceRequest, error: android.webkit.WebResourceError) {
                        android.util.Log.e("HAULWAY_MAP", "Web resource error ${error.errorCode}: ${error.description} (${request.url})")
                    }
                    override fun onReceivedHttpError(view: WebView, request: android.webkit.WebResourceRequest, response: android.webkit.WebResourceResponse) {
                        android.util.Log.w("HAULWAY_MAP", "HTTP ${response.statusCode} ${request.url}")
                    }
                    override fun onRenderProcessGone(view: WebView, detail: android.webkit.RenderProcessGoneDetail): Boolean {
                        pageReady = false
                        android.util.Log.e("HAULWAY_MAP", "WebView renderer exited; crashed=${detail.didCrash()}")
                        return true
                    }
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                        val level = when (consoleMessage?.messageLevel()) {
                            android.webkit.ConsoleMessage.MessageLevel.ERROR -> android.util.Log.ERROR
                            android.webkit.ConsoleMessage.MessageLevel.WARNING -> android.util.Log.WARN
                            else -> android.util.Log.INFO
                        }
                        android.util.Log.println(level, "HAULWAY_MAP", "JS ${consoleMessage?.message()} (${consoleMessage?.sourceId()}:${consoleMessage?.lineNumber()})")
                        return true
                    }
                    override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: android.webkit.GeolocationPermissions.Callback?) {
                        val fine = androidx.core.content.ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        val coarse = androidx.core.content.ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        callback?.invoke(origin, fine || coarse, false)
                    }
                }
                android.util.Log.i("HAULWAY_MAP", "Creating WebView and loading embedded map HTML")
                addOnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                    android.util.Log.i("HAULWAY_MAP", "WebView layout size: ${right-left}x${bottom-top}")
                    if (right > left && bottom > top) {
                        post { evaluateJavascript("window.haulwayResize && window.haulwayResize()", null) }
                    }
                }
                // A synthetic .local hostname triggers DNS lookups for favicon.ico on Android WebView.
                // Use the Android asset origin instead; all map libraries and tile URLs are absolute HTTPS URLs.
                loadDataWithBaseURL("file:///android_asset/", NAVIGATION_MAP_HTML, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            // Do not reload the WebView on every Compose recomposition: that discarded
            // the live map, GPS marker and route overlay, leaving only the map background.
            if (pageReady) {
                if (lastSentZoom != zoom) {
                    webView.evaluateJavascript("window.haulwaySetZoom && window.haulwaySetZoom($zoom)", null)
                    lastSentZoom = zoom
                }
                val routeJson = RoutePlanStore.coordinatesJson
                if (routeJson != null && routeJson != lastSentRoute) {
                    val safeJson = JSONObject.quote(routeJson)
                    webView.evaluateJavascript("window.haulwaySetRoute && window.haulwaySetRoute($safeJson)", null)
                    lastSentRoute = routeJson
                }
                if (latitude != null && longitude != null) {
                    val heading = bearing?.toDouble() ?: Double.NaN
                    val accuracyMetres = accuracy?.toDouble() ?: 18.0
                    webView.evaluateJavascript("window.haulwaySetVehicleLocation && window.haulwaySetVehicleLocation($latitude,$longitude,$heading,$accuracyMetres)", null)
                }
                if (followRequest > 0 && lastSentFollowRequest != followRequest) {
                    webView.evaluateJavascript("window.haulwayFollowRequest && window.haulwayFollowRequest($followRequest)", null)
                    lastSentFollowRequest = followRequest
                }
                if (mapStyleRequest > 0 && lastSentMapStyleRequest != mapStyleRequest) {
                    webView.evaluateJavascript("window.haulwaySetMapStyleRequest && window.haulwaySetMapStyleRequest($mapStyleRequest)", null)
                    lastSentMapStyleRequest = mapStyleRequest
                }
                val appearance = mapPrefs.getString("map_mode", "Auto") ?: "Auto"
                val selectedStyle = mapPrefs.getString("map_style", "Voyager") ?: "Voyager"
                val appearanceKey = "$appearance|$selectedStyle"
                if (lastSentAppearance != appearanceKey) {
                    webView.evaluateJavascript("window.haulwayApplyAppearance && window.haulwayApplyAppearance(${JSONObject.quote(appearance)},${JSONObject.quote(selectedStyle)})", null)
                    lastSentAppearance = appearanceKey
                }
                if (lastSentHeadingUp != headingUp) {
                    webView.evaluateJavascript("window.haulwaySetHeadingUp && window.haulwaySetHeadingUp(${if (headingUp) "true" else "false"})", null)
                    lastSentHeadingUp = headingUp
                }
                if (lastSentFollowEnabled != followEnabled) {
                    webView.evaluateJavascript("window.haulwaySetFollowMode && window.haulwaySetFollowMode(${if (followEnabled) "true" else "false"})", null)
                    lastSentFollowEnabled = followEnabled
                }
                if (lastSentTilt3D != tilt3D) {
                    webView.evaluateJavascript("window.haulwaySetTilt3D && window.haulwaySetTilt3D(${if (tilt3D) "true" else "false"})", null)
                    lastSentTilt3D = tilt3D
                }
                if (searchLocation != null && searchLocation != lastSentSearchLocation) {
                    webView.evaluateJavascript("window.haulwaySearchLocation && window.haulwaySearchLocation(${searchLocation.first},${searchLocation.second})", null)
                    lastSentSearchLocation = searchLocation
                }
            }
        }
    )
}

private val NAVIGATION_MAP_HTML = """
<!doctype html><html><head><meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no"><meta name="theme-color" content="#17232b"><link rel="icon" href="data:,"><style>
*{box-sizing:border-box}html,body{height:100%;width:100%;min-height:100%;margin:0;overflow:hidden;background:#17232b;font-family:Arial,sans-serif}#map{position:fixed;inset:0;width:100vw;height:100vh;min-height:100%;overflow:hidden;background:#17232b;touch-action:none}#mapscene{position:absolute;inset:0;transform:none;transform-origin:50% 50%;will-change:transform}#tiles{position:absolute;inset:0;overflow:hidden}#tileworld{position:absolute;left:50%;top:50%;transform-origin:0 0}.tile{position:absolute;width:256px;height:256px;border:0;user-select:none;-webkit-user-drag:none;backface-visibility:hidden;image-rendering:auto}.tile.failed{opacity:0}#overlay{position:absolute;inset:0;width:100%;height:100%;z-index:4;pointer-events:none;overflow:visible}#roadmarkers{position:absolute;inset:0;z-index:5;pointer-events:none;overflow:hidden}#trafficmarkers{position:absolute;inset:0;z-index:7;pointer-events:none;overflow:hidden}.traffic-marker{position:absolute;transform:translate(-50%,-50%);width:25px;height:25px;border:2px solid #fff;border-radius:50%;display:flex;align-items:center;justify-content:center;color:#fff;font:bold 12px Arial;box-shadow:0 2px 7px #000d;text-shadow:0 1px 2px #000}.traffic-marker.accident,.traffic-marker.brokenDownVehicle{background:#d92736}.traffic-marker.jam{background:#e58a19}.traffic-marker.roadworks,.traffic-marker.laneClosed,.traffic-marker.roadClosed{background:#ef6c24}.traffic-marker.fog,.traffic-marker.rain,.traffic-marker.ice,.traffic-marker.wind,.traffic-marker.flooding,.traffic-marker.dangerousConditions{background:#287eae}.traffic-marker.other{background:#7253a6}.road-marker{position:absolute;transform:translate(-50%,-50%);display:flex;align-items:center;justify-content:center;font:bold 10px Arial;filter:drop-shadow(0 2px 3px #000c);white-space:nowrap}.road-marker.stop{width:31px;height:31px;background:#e31b23;color:#fff;border:2px solid #fff;clip-path:polygon(30% 0,70% 0,100% 30%,100% 70%,70% 100%,30% 100%,0 70%,0 30%)}.road-marker.giveway{width:0;height:0;border-left:16px solid transparent;border-right:16px solid transparent;border-top:29px solid #e21b2d;filter:drop-shadow(0 0 2px #111)}.road-marker.giveway:after{content:'';position:absolute;top:-27px;left:-13px;border-left:13px solid transparent;border-right:13px solid transparent;border-top:23px solid #fff}.road-marker.speed{width:32px;height:32px;border-radius:50%;background:#fff;border:5px solid #d71920;color:#111;font-size:12px}.road-marker.camera{width:29px;height:29px;border-radius:50%;background:#f3d21b;border:2px solid #111;color:#111;font-size:15px}.road-marker.signal{width:18px;height:35px;border-radius:5px;background:#20252a;border:2px solid #eee;display:flex;flex-direction:column;gap:2px}.road-marker.signal:before,.road-marker.signal:after{content:'';width:8px;height:8px;border-radius:50%;background:#687078}.road-marker.signal span{width:8px;height:8px;border-radius:50%;background:#687078}.road-marker.camera{background:#f3d21b}.road-marker.parking,.road-marker.fuel,.road-marker.truckpark,.road-marker.poi{min-width:25px;height:25px;padding:0 4px;border:2px solid #fff;border-radius:4px;background:#087ccb;color:#fff;font-size:14px}.road-marker.fuel{background:#167b48}.road-marker.truckpark{background:#7650a8;font-size:10px}.road-marker.poi{background:#344b5d;font-size:11px}.road-marker-label{position:absolute;top:100%;left:50%;transform:translateX(-50%);margin-top:2px;padding:2px 4px;border-radius:3px;background:#081018dd;color:#fff;font:9px Arial}#mapstatus{position:absolute;z-index:10;left:50%;top:50%;transform:translate(-50%,-50%);padding:11px 15px;border:1px solid #168fe8;border-radius:8px;background:rgba(8,16,22,.94);color:#fff;font:600 12px Arial;text-align:center;max-width:85%;pointer-events:none;box-shadow:0 5px 24px #0008}.vehicle{position:absolute;z-index:5;width:28px;height:36px;margin:-18px 0 0 -14px;filter:drop-shadow(0 4px 5px #000f);pointer-events:none;transform-origin:50% 50%}.vehicle:before{content:'';position:absolute;left:1px;top:1px;border-left:13px solid transparent;border-right:13px solid transparent;border-bottom:34px solid #078ee8;filter:drop-shadow(0 0 2px #fff) drop-shadow(0 2px 0 #00131f)}.vehicle:after{content:'';position:absolute;left:9px;top:10px;border-left:5px solid transparent;border-right:5px solid transparent;border-bottom:17px solid #fff}.attribution{position:absolute;right:4px;bottom:3px;padding:2px 4px;background:#081018aa;color:#dce5eb;font-size:9px;z-index:6}
</style></head><body><div id="map"><div id="mapscene"><div id="tiles"><div id="tileworld"></div></div><div id="roadmarkers"></div><div id="trafficmarkers"></div><svg id="overlay"><polyline id="routecasing" fill="none" stroke="#062d4e" stroke-width="11" stroke-linecap="round" stroke-linejoin="round"/><polyline id="route" fill="none" stroke="#168fe8" stroke-width="6" stroke-linecap="round" stroke-linejoin="round"/><circle id="routeStart" r="8" fill="#35a65b" stroke="#fff" stroke-width="3" style="display:none"/><circle id="routeEnd" r="8" fill="#e34b4b" stroke="#fff" stroke-width="3" style="display:none"/></svg></div><div id="vehicle" class="vehicle" style="display:none"></div><div id="mapstatus">LOADING ROAD MAP…</div><div class="attribution">© CARTO · © OpenStreetMap data · HAULWAY Dark</div></div>
<script>
(function(){'use strict';
const root=document.getElementById('map'),scene=document.getElementById('mapscene'),world=document.getElementById('tileworld'),status=document.getElementById('mapstatus'),overlay=document.getElementById('overlay'),vehicle=document.getElementById('vehicle'),roadMarkers=document.getElementById('roadmarkers'),trafficMarkers=document.getElementById('trafficmarkers');
console.log('HAULWAY native tile map script started');
 const CARTO_API_KEY=${JSONObject.quote(BuildConfig.CARTO_API_KEY)};
window.addEventListener('error',e=>console.error('Map JS error: '+e.message+' line '+e.lineno));
const cartoProviders=[
 {name:'HAULWAY Dark',url:(z,x,y)=>`https://${'$'}{['a','b','c','d'][(x+y)%4]}.basemaps.cartocdn.com/dark_all/${'$'}{z}/${'$'}{x}/${'$'}{y}@2x.png?key=${'$'}{encodeURIComponent(CARTO_API_KEY)}`,attr:'© CARTO · © OpenStreetMap data'},
 {name:'HAULWAY Voyager',url:(z,x,y)=>`https://${'$'}{['a','b','c','d'][(x+y)%4]}.basemaps.cartocdn.com/rastertiles/voyager/${'$'}{z}/${'$'}{x}/${'$'}{y}@2x.png?key=${'$'}{encodeURIComponent(CARTO_API_KEY)}`,attr:'© CARTO · © OpenStreetMap data'},
 {name:'HAULWAY Light',url:(z,x,y)=>`https://${'$'}{['a','b','c','d'][(x+y)%4]}.basemaps.cartocdn.com/light_all/${'$'}{z}/${'$'}{x}/${'$'}{y}@2x.png?key=${'$'}{encodeURIComponent(CARTO_API_KEY)}`,attr:'© CARTO · © OpenStreetMap data'}
];
const openStreetMapProvider={name:'OpenStreetMap fallback',url:(z,x,y)=>`https://${'$'}{['a','b','c'][(x+y)%3]}.tile.openstreetmap.org/${'$'}{z}/${'$'}{x}/${'$'}{y}.png`,attr:'© OpenStreetMap contributors'};
/* CARTO requires its own Basemaps key. Do not request CARTO tiles with an empty key:
   they return HTTP 200 with an API KEY REQUIRED watermark. */
const providers=CARTO_API_KEY ? [...cartoProviders,openStreetMapProvider] : [openStreetMapProvider];
let provider=0,center={lat:53.4808,lon:-2.2426},renderedCenter={lat:53.4808,lon:-2.2426},zoom=12,tiles=[],tileCache=new Map(),routeCoords=[],lastVehicle=null,targetVehicle=null,vehicleAnimFrom=null,vehicleAnimStarted=0,vehicleAnimationFrame=0,autoFollow=true,firstFix=true,headingUp=true,tilt3D=true,drag=null,failed=0,loaded=0,providerTimer=null;
const clamp=(v,a,b)=>Math.max(a,Math.min(b,v));
function worldPoint(lat,lon,z){const n=256*Math.pow(2,z),x=(lon+180)/360*n,s=Math.sin(lat*Math.PI/180),y=(.5-Math.log((1+s)/(1-s))/(4*Math.PI))*n;return{x,y};}
function geoPoint(x,y,z){const n=256*Math.pow(2,z),lon=x/n*360-180,lat=Math.atan(Math.sinh(Math.PI*(1-2*y/n)))*180/Math.PI;return{lat,lon};}
function size(){return{w:root.clientWidth,h:root.clientHeight};}
let lastRenderSize='',renderQueued=false,renderGeneration=0;
let roadFeatures=[],roadFetchAt=0,roadFetchCenter=null,roadFetchBusy=false,trafficFeatures=[];
const overpassEndpoints=['https://overpass-api.de/api/interpreter','https://overpass.kumi.systems/api/interpreter'];
function roadFeatureKind(t){if(t.highway==='stop')return 'stop';if(t.highway==='give_way')return 'giveway';if(t.highway==='traffic_signals')return 'signal';if(t.highway==='speed_camera'||t.enforcement==='maxspeed')return 'camera';if(t.maxspeed)return 'speed';if(t.amenity==='parking'&&(/hgv|truck|lorry/i.test((t.access||'')+' '+(t.hgv||'')+' '+(t.name||'')+' '+(t.parking||''))))return 'truckpark';if(t.amenity==='parking'||t.highway==='rest_area'||t.highway==='services')return 'parking';if(t.amenity==='fuel')return 'fuel';if(t.amenity||t.shop||t.tourism||t.leisure)return 'poi';return null;}
function roadFeatureLabel(k,t){if(k==='stop')return 'STOP';if(k==='giveway'||k==='signal')return '';if(k==='speed')return String(t.maxspeed).replace(/[^0-9]/g,'').slice(0,3)||'!';if(k==='camera')return '⌁';if(k==='truckpark')return 'HGV';if(k==='parking')return 'P';if(k==='fuel')return 'F';return t.amenity==='toilets'?'WC':t.shop==='car_repair'?'🔧':t.amenity==='car_wash'?'W':t.amenity==='weighbridge'?'WGT':t.shop?'SHOP':t.tourism?'INFO':t.leisure?'REC':'•';}
function refreshRoadData(){const now=Date.now();if(zoom<13||roadFetchBusy||now-roadFetchAt<45000)return;if(roadFetchCenter&&Math.abs(center.lat-roadFetchCenter.lat)<0.008&&Math.abs(center.lon-roadFetchCenter.lon)<0.008)return;roadFetchAt=now;roadFetchCenter={lat:center.lat,lon:center.lon};roadFetchBusy=true;const box=(center.lat-0.012)+','+(center.lon-0.018)+','+(center.lat+0.012)+','+(center.lon+0.018);const q='[out:json][timeout:15];(node[highway~"^(stop|give_way|traffic_signals|speed_camera)$"]('+box+');node[enforcement="maxspeed"]('+box+');node[maxspeed]('+box+');way[maxspeed]('+box+');node[amenity~"^(parking|fuel|toilets|restaurant|fast_food|car_wash|weighbridge)$"]('+box+');way[amenity~"^(parking|fuel|restaurant|fast_food)$"]('+box+');node[highway~"^(rest_area|services)$"]('+box+');way[highway~"^(rest_area|services)$"]('+box+');node[amenity]('+box+');way[amenity]('+box+');node[shop]('+box+');way[shop]('+box+');node[tourism]('+box+');node[leisure]('+box+'););out center;';let endpoint=0;const request=()=>fetch(overpassEndpoints[endpoint],{method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded;charset=UTF-8'},body:'data='+encodeURIComponent(q)}).then(r=>{if(!r.ok)throw Error('Overpass HTTP '+r.status);return r.json();}).then(data=>{const seen=new Set();roadFeatures=(data.elements||[]).map(el=>{const t=el.tags||{},kind=roadFeatureKind(t),lat=el.lat??(el.center&&el.center.lat),lon=el.lon??(el.center&&el.center.lon);if(!kind||!Number.isFinite(lat)||!Number.isFinite(lon))return null;const key=kind+':'+lat.toFixed(5)+':'+lon.toFixed(5);if(seen.has(key))return null;seen.add(key);return{lat,lon,kind,label:roadFeatureLabel(kind,t),name:t.name||'',maxspeed:t.maxspeed||''};}).filter(Boolean).slice(0,180);drawRoadMarkers();console.log('OSM road overlay loaded: '+roadFeatures.length+' features');}).catch(err=>{if(endpoint<overpassEndpoints.length-1){endpoint++;return request();}console.warn('OSM road overlay unavailable: '+err.message);}).finally(()=>{roadFetchBusy=false;});request();}
function drawRoadMarkers(){roadMarkers.textContent='';const sz=size();for(const f of roadFeatures){const p=screenPoint(f.lat,f.lon);if(p.x < -35||p.y < -40||p.x>sz.w+35||p.y>sz.h+40)continue;const el=document.createElement('div');el.className='road-marker '+f.kind;el.style.left=p.x+'px';el.style.top=p.y+'px';if(f.kind==='signal'){const lamp=document.createElement('span');el.appendChild(lamp);}else if(f.kind!=='giveway'){el.textContent=f.label;}el.title=f.kind==='speed'?'Speed limit '+f.maxspeed:(f.name||f.kind);if(f.name||f.kind==='speed'){const label=document.createElement('small');label.className='road-marker-label';label.textContent=f.kind==='speed'?f.maxspeed:(f.name.length>24?f.name.slice(0,22)+'…':f.name);el.appendChild(label);}roadMarkers.appendChild(el);}}
function drawTrafficMarkers(){trafficMarkers.textContent='';const sz=size();for(const f of trafficFeatures){const p=screenPoint(f.latitude,f.longitude);if(p.x < -35||p.y < -40||p.x>sz.w+35||p.y>sz.h+40)continue;const kind=String(f.category||'other').replace(/[^a-z]/gi,'');const el=document.createElement('div');el.className='traffic-marker '+(kind||'other');el.style.left=p.x+'px';el.style.top=p.y+'px';el.textContent=kind==='accident'?'!' : kind==='jam'?'≈' : kind==='roadworks'?'⚒' : kind==='brokendownvehicle'?'B' : '!';el.title=[f.category,f.description,f.road].filter(Boolean).join(' · ');trafficMarkers.appendChild(el);}}
window.haulwaySetTrafficIncidents=function(json){try{const raw=JSON.parse(json);trafficFeatures=Array.isArray(raw)?raw.filter(f=>Number.isFinite(Number(f.latitude))&&Number.isFinite(Number(f.longitude))&&Math.abs(Number(f.latitude))<=90&&Math.abs(Number(f.longitude))<=180):[];drawTrafficMarkers();console.log('TomTom incidents displayed on map: '+trafficFeatures.length);}catch(e){console.error('Traffic marker data error: '+e);}};

function scheduleRender(){if(renderQueued)return;renderQueued=true;requestAnimationFrame(()=>{renderQueued=false;render();});}
function showStatus(t){status.textContent=t;status.style.display='block';}
window.haulwayResize=function(){const s=size();if(s.w>0&&s.h>0){const key=s.w+'x'+s.h;if(key!==lastRenderSize){lastRenderSize=key;render();}}};
const mapResizeObserver=new ResizeObserver(()=>window.haulwayResize());
mapResizeObserver.observe(root);
function render(){
 const generation=++renderGeneration,sz=size(),c=worldPoint(center.lat,center.lon,zoom),anchorY=autoFollow?sz.h*.70:sz.h*.50,left=c.x-sz.w/2,top=c.y-anchorY;
 try{const nw=geoPoint(left,top,zoom),se=geoPoint(left+sz.w,top+sz.h,zoom);if(window.HaulwayViewportBridge&&window.HaulwayViewportBridge.onViewportBounds)window.HaulwayViewportBridge.onViewportBounds(JSON.stringify({west:nw.lon,north:nw.lat,east:se.lon,south:se.lat,zoom:zoom}));}catch(e){console.warn('Viewport bounds bridge unavailable: '+e);}
 renderedCenter={lat:center.lat,lon:center.lon};world.style.left='0px';world.style.top='0px';world.style.transform='none';
 const wanted=new Set(),nextTiles=[];let pending=0,loadedNow=0,failedNow=0;
 const x0=Math.floor(left/256)-1,y0=Math.floor(top/256)-1,x1=Math.floor((left+sz.w)/256)+1,y1=Math.floor((top+sz.h)/256)+1,max=Math.pow(2,zoom);
 function finish(){if(generation!==renderGeneration||pending>0)return;
   if(failedNow>0&&loadedNow===0&&provider<providers.length-1){provider++;showStatus('TRYING '+providers[provider].name.toUpperCase()+'…');render();return;}
   // Retain only visible tiles after the replacement set is ready. This avoids blank frames.
   for(const [key,node] of tileCache){if(!wanted.has(key)){node.remove();tileCache.delete(key);}}
   if(loadedNow===0&&failedNow>0)showStatus('MAP UNAVAILABLE — CHECK INTERNET CONNECTION');
   console.log('Map tile layer ready: provider='+providers[provider].name+', '+loadedNow+' loaded, '+failedNow+' failed; cached='+tileCache.size);
 }
 for(let y=y0;y<=y1;y++)for(let x=x0;x<=x1;x++){
   if(y<0||y>=max)continue;const xx=((x%max)+max)%max,key=provider+'|'+zoom+'|'+xx+'|'+y;wanted.add(key);
   let im=tileCache.get(key),isNew=!im;
   if(!im){im=document.createElement('img');im.className='tile';im.draggable=false;im.dataset.tileKey=key;tileCache.set(key,im);}
   if(im.complete&&im.naturalWidth>0){loadedNow++;}
   else if(im.complete&&im.naturalWidth===0&&im.src){failedNow++;}
   else {
     pending++;
     im.onload=()=>{if(generation!==renderGeneration)return;pending--;loadedNow++;im.classList.remove('failed');finish();};
     im.onerror=()=>{if(generation!==renderGeneration)return;pending--;failedNow++;im.classList.add('failed');finish();};
   }
   im.style.left=Math.round(x*256-left)+'px';im.style.top=Math.round(y*256-top)+'px';im.style.zIndex='2';
   if(im.parentNode!==world)world.appendChild(im);
   if(isNew)im.src=providers[provider].url(zoom,xx,y);
   nextTiles.push(im);
 }
 tiles=nextTiles;
 if(providerTimer)clearTimeout(providerTimer);
 providerTimer=setTimeout(()=>{if(generation!==renderGeneration)return;if(pending>0&&loadedNow===0&&provider<providers.length-1){provider++;showStatus('TRYING '+providers[provider].name.toUpperCase()+'…');render();}else if(loadedNow===0){showStatus('MAP UNAVAILABLE — CHECK INTERNET CONNECTION');}},9000);
 finish();drawOverlays();
}
function screenPoint(lat,lon){const p=worldPoint(lat,lon,zoom),c=worldPoint(center.lat,center.lon,zoom),s=size();return{x:p.x-c.x+s.w/2,y:p.y-c.y+(autoFollow?s.h*.70:s.h*.50)};}
function applyMapOrientation(){scene.style.transform=tilt3D&&autoFollow?'perspective(1100px) rotateX(48deg) scale(1.34)':'none';scene.style.transformOrigin='50% 70%';scene.style.perspective='none';if(lastVehicle&&Number.isFinite(lastVehicle.bearing)){vehicle.style.transform=headingUp?`rotate(${'$'}{lastVehicle.bearing}deg)`:'rotate(0deg)';}else{vehicle.style.transform='rotate(0deg)';}}
function drawOverlays(){const s=size();overlay.setAttribute('viewBox',`0 0 ${'$'}{s.w} ${'$'}{s.h}`);for(const id of ['routecasing','route'])document.getElementById(id).setAttribute('points',routeCoords.map(c=>{const p=screenPoint(c[0],c[1]);return `${'$'}{p.x},${'$'}{p.y}`}).join(' '));const start=document.getElementById('routeStart'),end=document.getElementById('routeEnd');if(routeCoords.length>1){const a=screenPoint(routeCoords[0][0],routeCoords[0][1]),b=screenPoint(routeCoords[routeCoords.length-1][0],routeCoords[routeCoords.length-1][1]);start.setAttribute('cx',a.x);start.setAttribute('cy',a.y);end.setAttribute('cx',b.x);end.setAttribute('cy',b.y);start.style.display='block';end.style.display='block';}else{start.style.display='none';end.style.display='none';}if(lastVehicle){const p=autoFollow?{x:s.w/2,y:s.h*.70}:screenPoint(lastVehicle.lat,lastVehicle.lon);vehicle.style.display='block';vehicle.style.left=p.x+'px';vehicle.style.top=p.y+'px';}applyMapOrientation();drawRoadMarkers();drawTrafficMarkers();refreshRoadData(); }
function setCenter(lat,lon,follow=false){center={lat:clamp(lat,-85,85),lon:lon};if(follow)autoFollow=true;render();}
window.haulwaySetZoom=function(offset){const next=clamp(12+Number(offset||0),3,19);if(next!==zoom){zoom=next;scheduleRender();}};
window.haulwaySetRoute=function(json){try{const raw=JSON.parse(json);routeCoords=raw.map(c=>[Number(c[1]),Number(c[0])]).filter(c=>Number.isFinite(c[0])&&Number.isFinite(c[1]));if(routeCoords.length<2)throw Error('Route geometry empty');if(autoFollow&&lastVehicle){center={lat:lastVehicle.lat,lon:lastVehicle.lon};zoom=Math.max(zoom,16);}else{const lats=routeCoords.map(c=>c[0]),lons=routeCoords.map(c=>c[1]);center={lat:(Math.min(...lats)+Math.max(...lats))/2,lon:(Math.min(...lons)+Math.max(...lons))/2};const span=Math.max(Math.abs(Math.max(...lats)-Math.min(...lats)),Math.abs(Math.max(...lons)-Math.min(...lons)));zoom=clamp(Math.floor(Math.log2(360/Math.max(span,.002)))-1,5,16);}scheduleRender();}catch(e){showStatus('ROUTE DISPLAY ERROR');console.error('Route error '+e);}};
window.haulwayClearRoute=function(){routeCoords=[];center={lat:53.4808,lon:-2.2426};zoom=12;autoFollow=false;scheduleRender();};
function animateVehicleFrame(now){
 if(!targetVehicle)return;
 const duration=900,progress=clamp((now-vehicleAnimStarted)/duration,0,1),from=vehicleAnimFrom||targetVehicle;
 const lonDelta=((targetVehicle.lon-from.lon+540)%360)-180;
 let bearing=targetVehicle.bearing;
 if(Number.isFinite(from.bearing)&&Number.isFinite(targetVehicle.bearing)){const d=((targetVehicle.bearing-from.bearing+540)%360)-180;bearing=(from.bearing+d*progress+360)%360;}
 lastVehicle={lat:from.lat+(targetVehicle.lat-from.lat)*progress,lon:from.lon+lonDelta*progress,bearing,accuracy:targetVehicle.accuracy};
 if(autoFollow){
   center={lat:lastVehicle.lat,lon:lastVehicle.lon};
   const base=worldPoint(renderedCenter.lat,renderedCenter.lon,zoom),current=worldPoint(center.lat,center.lon,zoom),dx=base.x-current.x,dy=base.y-current.y;
   if(!drag){world.style.transform='translate3d('+dx+'px,'+dy+'px,0)';}
 }
 drawOverlays();
 if(progress<1){vehicleAnimationFrame=requestAnimationFrame(animateVehicleFrame);}
 else{vehicleAnimationFrame=0;vehicleAnimFrom=targetVehicle;}
}
window.haulwaySetVehicleLocation=function(lat,lon,bearing,accuracy){
 if(!Number.isFinite(lat)||!Number.isFinite(lon))return;
 const next={lat,lon,bearing:Number.isFinite(bearing)?bearing:null,accuracy};
 if(!lastVehicle){
   lastVehicle=next;targetVehicle=next;vehicleAnimFrom=next;
   if(autoFollow){center={lat:clamp(lat,-85,85),lon};if(firstFix){zoom=Math.max(zoom,16);firstFix=false;}}
   scheduleRender();drawOverlays();return;
 }
 if(vehicleAnimationFrame)cancelAnimationFrame(vehicleAnimationFrame);
 vehicleAnimFrom=lastVehicle;targetVehicle=next;vehicleAnimStarted=performance.now();
 vehicleAnimationFrame=requestAnimationFrame(animateVehicleFrame);
};
window.haulwaySearchLocation=function(lat,lon){if(!Number.isFinite(lat)||!Number.isFinite(lon))return;autoFollow=false;center={lat:clamp(lat,-85,85),lon:lon};zoom=Math.max(zoom,16);scheduleRender();};
window.haulwayFollowRequest=function(){autoFollow=true;if(lastVehicle){const oldZoom=zoom;center={lat:lastVehicle.lat,lon:lastVehicle.lon};zoom=Math.max(zoom,16);if(zoom!==oldZoom){scheduleRender();}else{const a=worldPoint(renderedCenter.lat,renderedCenter.lon,zoom),b=worldPoint(center.lat,center.lon,zoom);world.style.transform='translate3d('+(a.x-b.x)+'px,'+(a.y-b.y)+'px,0)';drawOverlays();}}applyMapOrientation();};
window.haulwaySetFollowMode=function(enabled){autoFollow=!!enabled;if(autoFollow&&lastVehicle){center={lat:lastVehicle.lat,lon:lastVehicle.lon};zoom=Math.max(zoom,16);scheduleRender();}else{drawOverlays();}applyMapOrientation();};
window.haulwaySetTilt3D=function(enabled){tilt3D=!!enabled;applyMapOrientation();};
window.haulwayApplyAppearance=function(mode,style){
 if(!CARTO_API_KEY){console.warn('Map appearance saved; CARTO key is not present in this APK');return;}
 const effective=(mode==='Auto')?(new Date().getHours()>=19||new Date().getHours()<7?'Dark Matter':'Voyager'):((mode==='Night')?'Dark Matter':'Positron');
 const chosen=effective;
 const target=chosen==='Dark Matter'?0:(chosen==='Voyager'?1:2);
 if(provider!==target){provider=target;const attr=document.querySelector('.attribution');if(attr)attr.textContent=providers[provider].attr+' · '+providers[provider].name;showStatus('LOADING '+providers[provider].name.toUpperCase()+'…');render();}
};
window.haulwaySetMapStyleRequest=function(){
 if(!CARTO_API_KEY){showStatus('CARTO API KEY MISSING — ADD CARTO_API_KEY TO local.properties OR GITHUB SECRET');console.warn('Layers unavailable: CARTO_API_KEY was empty at build time');return;}
 // The Layers control cycles only the three CARTO basemaps. OSM is fallback-only.
 provider=(provider+1)%cartoProviders.length;
 const attr=document.querySelector('.attribution');if(attr)attr.textContent=providers[provider].attr+' · '+providers[provider].name;
 showStatus('LOADING '+providers[provider].name.toUpperCase()+'…');render();
};
window.haulwaySetHeadingUp=function(enabled){headingUp=!!enabled;applyMapOrientation();};
let panFrame=0,panVelocity={x:0,y:0},lastPointer={x:0,y:0,t:0},panOffset={x:0,y:0};
function applyPanTransform(){
 const tr=`translate3d(${'$'}{panOffset.x}px,${'$'}{panOffset.y}px,0)`;
 world.style.transform=tr;overlay.style.transform=tr;vehicle.style.transform=tr+' '+(headingUp&&lastVehicle&&Number.isFinite(lastVehicle.bearing)?`rotate(${'$'}{lastVehicle.bearing}deg)`:'rotate(0deg)');
}
function commitPan(){
 if(panFrame){cancelAnimationFrame(panFrame);panFrame=0;}
 if(panOffset.x===0&&panOffset.y===0)return;
 const a=worldPoint(center.lat,center.lon,zoom);
 center=geoPoint(a.x-panOffset.x,a.y-panOffset.y,zoom);
 panOffset={x:0,y:0};world.style.transform='none';overlay.style.transform='none';
 vehicle.style.transform=headingUp&&lastVehicle&&Number.isFinite(lastVehicle.bearing)?`rotate(${'$'}{lastVehicle.bearing}deg)`:'rotate(0deg)';
 render();
}
root.addEventListener('pointerdown',e=>{
 if(panFrame){cancelAnimationFrame(panFrame);panFrame=0;}
 drag={startX:e.clientX,startY:e.clientY};panOffset={x:0,y:0};panVelocity={x:0,y:0};
 lastPointer={x:e.clientX,y:e.clientY,t:performance.now()};root.setPointerCapture(e.pointerId);autoFollow=false;applyMapOrientation();
});
root.addEventListener('pointermove',e=>{
 if(!drag)return;
 const now=performance.now(),dt=Math.max(1,now-lastPointer.t),dx=e.clientX-lastPointer.x,dy=e.clientY-lastPointer.y;
 panVelocity={x:dx/dt,y:dy/dt};lastPointer={x:e.clientX,y:e.clientY,t:now};
 panOffset={x:e.clientX-drag.startX,y:e.clientY-drag.startY};applyPanTransform();
});
function finishDrag(e){
 if(!drag)return;drag=null;
 if(Math.abs(panOffset.x)+Math.abs(panOffset.y)<3){panOffset={x:0,y:0};return;}
 let vx=panVelocity.x*16,vy=panVelocity.y*16;
 const step=()=>{panOffset.x+=vx;panOffset.y+=vy;vx*=0.90;vy*=0.90;applyPanTransform();
  if(Math.hypot(vx,vy)>0.32){panFrame=requestAnimationFrame(step);}else{panFrame=0;commitPan();}
 };
 panFrame=requestAnimationFrame(step);
}
root.addEventListener('pointerup',finishDrag);root.addEventListener('pointercancel',finishDrag);
root.addEventListener('wheel',e=>{e.preventDefault();zoom=clamp(zoom+(e.deltaY<0?1:-1),3,19);scheduleRender();},{passive:false});
window.addEventListener('resize',()=>{const s=size();if(s.w>0&&s.h>0){const key=s.w+'x'+s.h;if(key!==lastRenderSize){lastRenderSize=key;render();}else{drawOverlays();}}});
console.log('Map engine ready: built-in tile renderer; zoom='+zoom);
function waitForMapSize(){const s=size();if(s.w>0&&s.h>0){lastRenderSize=s.w+'x'+s.h;render();console.log('Map viewport ready: '+lastRenderSize);}else{requestAnimationFrame(waitForMapSize);}}
requestAnimationFrame(waitForMapSize);
})();
</script></body></html>
""".trimIndent()

@Composable
private fun DeviceStatusHeader(modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var clock by remember { mutableStateOf(java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())) }
    var wifiConnected by remember { mutableStateOf(false) }
    var bluetoothEnabled by remember { mutableStateOf(false) }
    var locationEnabled by remember { mutableStateOf(false) }
    var batteryPercent by remember { mutableIntStateOf(100) }
    var charging by remember { mutableStateOf(false) }
    var cellularConnected by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            clock = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
            try {
                val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
                val caps = cm.activeNetwork?.let { cm.getNetworkCapabilities(it) }
                wifiConnected = caps?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) == true
                cellularConnected = caps?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) == true
            } catch (_: Exception) { wifiConnected = false; cellularConnected = false }
            try {
                @Suppress("DEPRECATION")
                bluetoothEnabled = android.bluetooth.BluetoothAdapter.getDefaultAdapter()?.isEnabled == true
            } catch (_: SecurityException) { bluetoothEnabled = false }
            try {
                val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
                locationEnabled = lm.isLocationEnabled
            } catch (_: Exception) { locationEnabled = false }
            try {
                val battery = context.registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
                if (battery != null) {
                    val level = battery.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
                    val scale = battery.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, 100)
                    if (level >= 0 && scale > 0) batteryPercent = (level * 100 / scale).coerceIn(0, 100)
                    val status = battery.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1)
                    charging = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == android.os.BatteryManager.BATTERY_STATUS_FULL
                }
            } catch (_: Exception) { }
            kotlinx.coroutines.delay(2000)
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Row(Modifier.align(Alignment.CenterStart), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Route, contentDescription = null, tint = Color(0xFFF0F4F7), modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(6.dp))
            Text("HAULWAY", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.2.sp)
        }
        Text(clock, modifier = Modifier.align(Alignment.Center), color = Color(0xFFF2F5F7), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (locationEnabled) Icon(Icons.Default.LocationOn, contentDescription = "Location enabled", tint = Color.White, modifier = Modifier.size(12.dp))
            if (wifiConnected) Icon(Icons.Default.Wifi, contentDescription = "Wi-Fi connected", tint = Color.White, modifier = Modifier.size(13.dp))
            else if (cellularConnected) Icon(Icons.Default.SignalCellularAlt, contentDescription = "Mobile network connected", tint = Color.White, modifier = Modifier.size(13.dp))
            if (bluetoothEnabled) Icon(Icons.Default.Bluetooth, contentDescription = "Bluetooth enabled", tint = Color.White, modifier = Modifier.size(12.dp))
            Icon(if (charging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                contentDescription = "Battery $batteryPercent percent", tint = Color.White, modifier = Modifier.size(15.dp))
            Text("$batteryPercent%", color = Color(0xFFE8EDF1), fontSize = 9.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun HaulwayHomeScreen(onNavigate: (String) -> Unit) {
    val items = listOf(
        Triple("Navigate", "Find the best truck route", Icons.Default.Navigation) to "MAP",
        Triple("360° Street View", "Preview the destination", Icons.Default.Visibility) to "360 STREET VIEW",
        Triple("Route Planner", "Plan multi-stop routes", Icons.Default.Place) to "ROUTE PLANNER",
        Triple("Truck Profile", "Configure dimensions & limits", Icons.Default.LocalShipping) to "TRUCK PROFILE",
        Triple("Truck Parking", "Find safe truck parks", Icons.Default.Place) to "TRUCK PARKING",
        Triple("Services & POI", "Fuel, food, repairs...", Icons.Default.LocalGasStation) to "SERVICES & POI",
        Triple("Traffic & Alerts", "Roadworks, restrictions...", Icons.Default.Warning) to "TRAFFIC",
        Triple("My Places", "Saved locations", Icons.Default.Star) to "MY PLACES",
        Triple("History", "Recent routes", Icons.Default.History) to "HISTORY",
        Triple("Settings", "App and map settings", Icons.Default.Settings) to "SETTINGS"
    )
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF090D11)).padding(horizontal = 8.dp, vertical = 7.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(15.dp))
                .background(Color(0xFF17232D))
        ) {
            // Real photographic background stored in app/src/main/res/drawable-nodpi.
            Image(
                painter = painterResource(id = R.drawable.haulway_home_background),
                contentDescription = "HAULWAY truck on the road",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Light darkening layer: keep the truck and sunset visible behind the glass tiles.
            Column(Modifier.fillMaxSize().background(Color(0x12050A10)).padding(horizontal = 10.dp, vertical = 7.dp)) {
                DeviceStatusHeader(modifier = Modifier.fillMaxWidth().height(34.dp))
                Spacer(Modifier.height(5.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    items.chunked(3).forEach { rowItems ->
                        Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            rowItems.forEach { (item, target) ->
                                Surface(
                                    modifier = Modifier.weight(1f).fillMaxHeight().clickable { onNavigate(target) },
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.Transparent,
                                    shadowElevation = 3.dp,
                                    border = androidx.compose.foundation.BorderStroke(0.6.dp, Color(0x55788C99))
                                ) {
                                    Column(
                                        Modifier.fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color(0xF01B2731),
                                                        Color(0xF0141E27),
                                                        Color(0xF20D151D)
                                                    )
                                                )
                                            )
                                            .padding(horizontal = 3.dp, vertical = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(item.third, contentDescription = item.first, tint = when(item.first) {
                                            "Route Planner" -> Color(0xFFFF7838)
                                            "Traffic & Alerts" -> Color(0xFFE34D4D)
                                            "Truck Parking" -> Color(0xFF1687D8)
                                            else -> Color(0xFFF0F3F5)
                                        }, modifier = Modifier.size(38.dp))
                                        Spacer(Modifier.height(6.dp))
                                        Text(item.first, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                        Spacer(Modifier.height(3.dp))
                                        Text(item.second, color = Color(0xFFD4DCE3), fontSize = 10.sp, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(5.dp))
                Row(Modifier.fillMaxWidth().height(43.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    val nav = listOf(Triple("Home", Icons.Default.Home, "HOME"), Triple("Map", Icons.Default.Map, "MAP"), Triple("Route", Icons.Default.Route, "ROUTE PLANNER"), Triple("Services", Icons.Default.Build, "SERVICES & POI"), Triple("Traffic", Icons.Default.Warning, "TRAFFIC"), Triple("More", Icons.Default.Menu, "SETTINGS"))
                    nav.forEachIndexed { index, entry ->
                        Column(
                            Modifier.weight(1f).fillMaxHeight()
                                .clip(RoundedCornerShape(5.dp))
                                .background(
                                    if (index == 0) Brush.verticalGradient(
                                        listOf(Color(0xFF087FD9), Color(0xFF0753A0))
                                    ) else Brush.verticalGradient(
                                        listOf(Color(0xE91A2630), Color(0xED0D1720))
                                    )
                                )
                                .clickable { onNavigate(entry.third) },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(entry.second, contentDescription = entry.first, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.height(1.dp))
                            Text(entry.first, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutePlannerMapView(routeJson: String?, incidents: List<HaulwayTrafficIncident> = emptyList(), onViewportBounds: (String) -> Unit = {}) {
    val viewportCallback = onViewportBounds
    var mapView by remember { mutableStateOf<WebView?>(null) }
    var pageReady by remember { mutableStateOf(false) }
    var lastRoute by remember { mutableStateOf<String?>(null) }
    var lastSentIncidents by remember { mutableStateOf<String?>(null) }
    val incidentJson = remember(incidents) {
        org.json.JSONArray().apply {
            incidents.filter { it.latitude != null && it.longitude != null }.forEach { incident ->
                put(JSONObject().apply {
                    put("id", incident.id)
                    put("category", incident.category)
                    put("description", incident.description)
                    put("road", incident.road)
                    put("severity", incident.severity)
                    put("delaySeconds", incident.delaySeconds)
                    put("latitude", incident.latitude!!)
                    put("longitude", incident.longitude!!)
                })
            }
        }.toString()
    }
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(android.graphics.Color.rgb(23, 35, 43))
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onViewportBounds(json: String) {
                        android.os.Handler(Looper.getMainLooper()).post { viewportCallback(json) }
                    }
                }, "HaulwayViewportBridge")
                settings.loadsImagesAutomatically = true
                settings.blockNetworkImage = false
                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
                settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) {
                        pageReady = true
                        android.util.Log.i("HAULWAY_ROUTE_MAP", "Shared map engine ready")
                        val route = routeJson
                        if (route.isNullOrBlank()) {
                            view.evaluateJavascript("window.haulwayClearRoute && window.haulwayClearRoute()", null)
                            lastRoute = null
                        } else {
                            view.evaluateJavascript("window.haulwaySetRoute && window.haulwaySetRoute(${JSONObject.quote(route)})", null)
                            lastRoute = route
                        }
                        view.evaluateJavascript("window.haulwaySetTrafficIncidents && window.haulwaySetTrafficIncidents(${JSONObject.quote(incidentJson)})", null)
                        lastSentIncidents = incidentJson
                    }
                    override fun onReceivedError(view: WebView, request: android.webkit.WebResourceRequest, error: android.webkit.WebResourceError) {
                        android.util.Log.e("HAULWAY_ROUTE_MAP", "Map resource error ${error.errorCode}: ${error.description} ${request.url}")
                    }
                }
                mapView = this
                loadDataWithBaseURL("file:///android_asset/", NAVIGATION_MAP_HTML, "text/html", "UTF-8", null)
            }
        },
        update = { view ->
            if (pageReady && mapView === view && routeJson != lastRoute) {
                if (routeJson.isNullOrBlank()) {
                    view.evaluateJavascript("window.haulwayClearRoute && window.haulwayClearRoute()", null)
                } else {
                    view.evaluateJavascript("window.haulwaySetRoute && window.haulwaySetRoute(${JSONObject.quote(routeJson)})", null)
                }
                lastRoute = routeJson
            }
            if (pageReady && mapView === view && incidentJson != lastSentIncidents) {
                view.evaluateJavascript("window.haulwaySetTrafficIncidents && window.haulwaySetTrafficIncidents(${JSONObject.quote(incidentJson)})", null)
                lastSentIncidents = incidentJson
            }
        }
    )
}

@Composable
private fun RoutePlannerPanel(onRouteReady: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val stops = remember { mutableStateListOf("", "") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<RouteResult?>(null) }
    var strategy by remember { mutableStateOf("FASTEST") }
    var addStopText by remember { mutableStateOf("") }
    var showAddStop by remember { mutableStateOf(false) }
    var editingStopIndex by remember { mutableIntStateOf(-1) }
    var addressSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedAddress by remember { mutableStateOf(false) }
    var searchingAddresses by remember { mutableStateOf(false) }
    var addressSearchError by remember { mutableStateOf<String?>(null) }
    var dragRemainder by remember { mutableFloatStateOf(0f) }
    val dragThreshold = with(androidx.compose.ui.platform.LocalDensity.current) { 62.dp.toPx() }
    var showBreakMenu by remember { mutableStateOf(false) }
    var editingBreakIndex by remember { mutableIntStateOf(-1) }
    val totalBreakMinutes = stops.sumOf { stop -> breakMinutesFromStop(stop) }
    val scope = rememberCoroutineScope()
    val panel = Color(0xE911171D)
    val rowBg = Color(0xFF202A32)
    val muted = Color(0xFFAEBBC5)
    val blue = Color(0xFF087ED1)
    val green = Color(0xFF399B59)

    Row(modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // Equal-width 50:50 layout for the stop list and map.
        Column(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(10.dp)).background(panel).padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth().height(38.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("My Route", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text("${stops.size} STOPS", color = muted, fontSize = 13.sp)
            }
            stops.forEachIndexed { index, stop ->
                val isBreak = stop.startsWith("BREAK|", ignoreCase = true)
                val breakMinutes = breakMinutesFromStop(stop)
                val physicalStopIndex = stops.take(index).count { !it.startsWith("BREAK|", ignoreCase = true) }
                val leg = result?.legSummaries?.getOrNull(physicalStopIndex - 1)
                val isDestination = !isBreak && index == stops.lastIndex
                val subtitle = when {
                    isBreak -> "${breakMinutes} min break"
                    stop.isBlank() && index == 0 -> "Tap to choose start location"
                    stop.isBlank() && isDestination -> "Tap to choose destination"
                    physicalStopIndex == 0 -> "Start location"
                    isDestination -> "Destination${leg?.let { " · ${it.first}, ${it.second}" } ?: ""}"
                    leg != null -> "${leg.first} · ${leg.second}"
                    else -> "Via stop · hold to move"
                }
                val canReorderStop = index > 0 && index < stops.lastIndex
                val canRemoveStop = index > 0 && index < stops.lastIndex
                val stopDragModifier = Modifier.pointerInput(stop, stops.size) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { dragRemainder = 0f },
                        onDragEnd = { dragRemainder = 0f },
                        onDragCancel = { dragRemainder = 0f }
                    ) { change, amount ->
                        dragRemainder += amount.y
                        val currentIndex = stops.indexOf(stop)
                        if (dragRemainder > dragThreshold && currentIndex >= 1 && currentIndex < stops.lastIndex) {
                            val moved = stops[currentIndex]
                            stops[currentIndex] = stops[currentIndex + 1]
                            stops[currentIndex + 1] = moved
                            result = null
                            dragRemainder = 0f
                        } else if (dragRemainder < -dragThreshold && currentIndex > 1 && currentIndex <= stops.lastIndex - 1) {
                            val moved = stops[currentIndex]
                            stops[currentIndex] = stops[currentIndex - 1]
                            stops[currentIndex - 1] = moved
                            result = null
                            dragRemainder = 0f
                        }
                    }
                }
                RoutePlannerStopRow(
                    number = index + 1,
                    title = if (isBreak) "Break" else if (stop.isBlank()) if (index == 0) "Choose start location" else "Choose destination" else if (stop.equals("CURRENT LOCATION", true)) "Current location (GPS)" else stop.substringBefore(","),
                    subtitle = subtitle,
                    highlighted = index > 0 && index < stops.lastIndex,
                    canMove = canReorderStop,
                    canRemove = canRemoveStop,
                    modifier = stopDragModifier,
                    onEdit = {
                        if (isBreak) {
                            editingBreakIndex = index
                            showBreakMenu = true
                        } else {
                            editingStopIndex = index
                            addStopText = stop
                            selectedAddress = false
                            addressSuggestions = emptyList()
                            showAddStop = true
                        }
                    },
                    onRemove = {
                        if (index > 0 && index < stops.lastIndex) {
                            stops.removeAt(index)
                            result = null
                        }
                    }
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Surface(Modifier.weight(1f).height(44.dp).clickable { editingStopIndex = -1; addStopText = ""; selectedAddress = false; addressSuggestions = emptyList(); showAddStop = true }, color = Color(0xFF303B44), shape = RoundedCornerShape(7.dp)) {
                    Row(Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
                        Text("Add Stop", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Box(Modifier.weight(1f)) {
                    Surface(Modifier.fillMaxWidth().height(44.dp).clickable { editingBreakIndex = -1; showBreakMenu = true }, color = Color(0xFF303B44), shape = RoundedCornerShape(7.dp)) {
                        Row(Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Coffee, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text("Add Break", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    androidx.compose.material3.DropdownMenu(expanded = showBreakMenu, onDismissRequest = { showBreakMenu = false }) {
                        listOf(15, 30, 45).forEach { minutes ->
                            androidx.compose.material3.DropdownMenuItem(text = { Text("${minutes} minutes") }, onClick = {
                                // Each break is its own route-list item. Selecting from Add Break
                                // inserts another break; selecting an existing Break row edits only that row.
                                if (editingBreakIndex in stops.indices && stops[editingBreakIndex].startsWith("BREAK|", true)) {
                                    stops[editingBreakIndex] = "BREAK|$minutes"
                                } else {
                                    stops.add(stops.lastIndex, "BREAK|$minutes")
                                }
                                editingBreakIndex = -1
                                result = null
                                showBreakMenu = false
                            })
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))

        }

        // Right map and route summary.
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(Modifier.fillMaxWidth().height(51.dp).clip(RoundedCornerShape(7.dp)).background(Color(0xE90A1015)).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CenterFocusStrong, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("HAULWAY", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("11:24", color = Color.White, fontSize = 13.sp)
                Spacer(Modifier.width(7.dp))
                listOf("FASTEST", "SHORTEST", "AVOID").forEach { option ->
                    val selected = strategy == option
                    Surface(Modifier.weight(1f).padding(horizontal = 1.dp).clickable { strategy = option; result = null },
                        color = if (selected) blue else Color(0xFF26313A), shape = RoundedCornerShape(5.dp)) {
                        Text(option.lowercase().replaceFirstChar { it.uppercase() }, color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp), maxLines = 1)
                    }
                }
            }
            Surface(Modifier.weight(1f).fillMaxWidth(), color = Color(0xFF142536), shape = RoundedCornerShape(7.dp)) {
                RoutePlannerMapView(result?.coordinatesJson)
            }
            Row(Modifier.fillMaxWidth().height(90.dp).clip(RoundedCornerShape(7.dp)).background(Color(0xE90D151B)).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(result?.let { durationWithBreaks(it.durationText, totalBreakMinutes) } ?: "Calculate route", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(result?.distanceText ?: "Route distance will appear here", color = muted, fontSize = 13.sp)
                }
                Button(onClick = {
                    if (result != null) { onRouteReady() }
                    else {
                        if (stops.size < 2 || stops.any { it.isBlank() }) { error = "Enter a start and destination"; return@Button }
                        loading = true; error = null
                        scope.launch {
                            try {
                                val addressStops = stops.filterNot { it.startsWith("BREAK|", ignoreCase = true) }
                                val calculated = calculateRoadRoute(addressStops, strategy, context)
                                RoutePlanStore.coordinatesJson = calculated.coordinatesJson
                                RoutePlanStore.summary = "${calculated.distanceText} · ${durationWithBreaks(calculated.durationText, totalBreakMinutes)}"
                                RoutePlanStore.totalDistanceMetres = calculated.totalDistanceMetres
                                RoutePlanStore.totalDurationSeconds = calculated.totalDurationSeconds + totalBreakMinutes * 60.0
                                RoutePlanStore.steps = calculated.steps
                                RoutePlanStore.maneuvers = calculated.maneuvers
                                RoutePlanStore.origin = stops.first(); RoutePlanStore.destination = stops.last()
                                result = calculated
                            } catch (e: Exception) { error = e.localizedMessage ?: "Could not calculate route" }
                            finally { loading = false }
                        }
                    }
                }, enabled = !loading, modifier = Modifier.width(158.dp).height(64.dp), colors = ButtonDefaults.buttonColors(containerColor = green), shape = RoundedCornerShape(6.dp), contentPadding = PaddingValues(4.dp)) {
                    if (loading) CircularProgressIndicator(Modifier.size(17.dp), color = Color.White, strokeWidth = 2.dp) else Text(if (result == null) "Calculate" else "Start", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (error != null) Text(error!!, color = Color(0xFFFF8A80), fontSize = 9.sp, maxLines = 2)
            if (strategy == "AVOID") Text("HGV routing avoids ferries and toll roads where mapped data permits.", color = Color(0xFFFFD9A6), fontSize = 9.sp, maxLines = 2)
            Text("Standard car routing preview only — not HGV-legal routing.", color = Color(0xFFFFD9A6), fontSize = 8.sp, maxLines = 1)
        }
    }

    // Address entry is a modal overlay, so the stop list cannot cover the field or confirmation button.
    if (showAddStop) {
        Dialog(
            onDismissRequest = { showAddStop = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.82f),
                color = Color(0xFF18232C),
                shape = RoundedCornerShape(14.dp),
                tonalElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(if (editingStopIndex >= 0) "EDIT ROUTE LOCATION" else "ADD ROUTE STOP", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Search for an address, town or postcode, then select a matching result.", color = muted, fontSize = 13.sp)
                    if (editingStopIndex == 0) {
                        Button(
                            onClick = { addStopText = "CURRENT LOCATION"; selectedAddress = true; addressSuggestions = emptyList(); addressSearchError = null },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF246B45))
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(7.dp))
                            Text("USE CURRENT LOCATION (GPS)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    OutlinedTextField(
                        value = addStopText,
                        onValueChange = { addStopText = it; selectedAddress = false; addressSuggestions = emptyList() },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Place or postcode") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = blue,
                            unfocusedBorderColor = Color(0xFF53616D),
                            focusedLabelColor = Color(0xFF70C9FF),
                            unfocusedLabelColor = muted,
                            cursorColor = Color.White
                        )
                    )
                    Button(onClick = {
                        if (addStopText.isNotBlank()) scope.launch {
                            searchingAddresses = true; addressSearchError = null; addressSuggestions = emptyList()
                            try { addressSuggestions = searchRoutePlaces(addStopText.trim()); if (addressSuggestions.isEmpty()) addressSearchError = "No matching places found" }
                            catch (e: Exception) { addressSearchError = e.localizedMessage ?: "Address search failed" }
                            finally { searchingAddresses = false }
                        }
                    }, enabled = addStopText.isNotBlank() && !searchingAddresses, modifier = Modifier.fillMaxWidth().height(42.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF263B4A))) {
                        if (searchingAddresses) CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp) else Icon(Icons.Default.Search, null, tint = Color.White)
                        Spacer(Modifier.width(6.dp)); Text("SEARCH ADDRESS", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    if (addressSearchError != null) Text(addressSearchError!!, color = Color(0xFFFF9B8F), fontSize = 12.sp)
                    if (addressSuggestions.isNotEmpty()) {
                        Text("SELECT A MATCHING ADDRESS", color = Color(0xFF73C9FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Column(Modifier.fillMaxWidth().heightIn(max = 180.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            addressSuggestions.forEach { suggestion -> Surface(Modifier.fillMaxWidth().clickable { addStopText = suggestion; selectedAddress = true; addressSuggestions = emptyList() }, color = Color(0xFF26333D), shape = RoundedCornerShape(6.dp)) { Text(suggestion, color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(9.dp)) } }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { showAddStop = false; editingStopIndex = -1; addressSuggestions = emptyList() },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF303B44))
                        ) { Text("CANCEL", color = Color.White) }
                        Button(
                            onClick = {
                                if (addStopText.isNotBlank()) {
                                    if (editingStopIndex >= 0 && editingStopIndex < stops.size) {
                                        stops[editingStopIndex] = addStopText.trim()
                                    } else {
                                        stops.add(stops.lastIndex, addStopText.trim())
                                    }
                                    editingStopIndex = -1
                                    addStopText = ""
                                    addressSuggestions = emptyList()
                                    selectedAddress = false
                                    showAddStop = false
                                    result = null
                                }
                            },
                            enabled = addStopText.isNotBlank() && selectedAddress,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = blue)
                        ) { Text(if (editingStopIndex >= 0) "SAVE LOCATION" else "ADD STOP", color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

private data class MapSearchResult(val name: String, val latitude: Double, val longitude: Double)

private suspend fun searchMapPlaces(
    query: String,
    biasLatitude: Double? = null,
    biasLongitude: Double? = null
): List<MapSearchResult> = withContext(Dispatchers.IO) {
    // Photon is designed for type-ahead search and returns street/house/postcode components.
    val encoded = URLEncoder.encode(query, "UTF-8")
    val bias = if (biasLatitude != null && biasLongitude != null)
        "&lat=$biasLatitude&lon=$biasLongitude&zoom=14&location_bias_scale=0.2" else ""
    val connection = (URL("https://photon.komoot.io/api/?q=$encoded&limit=10&lang=en$bias").openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"; setRequestProperty("User-Agent", "HAULWAY-Navigation/1.0 (Android map autocomplete)"); connectTimeout = 12000; readTimeout = 15000
    }
    try {
        val root = org.json.JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
        val features = root.optJSONArray("features") ?: org.json.JSONArray()
        val results = (0 until features.length()).mapNotNull { index ->
            val feature = features.getJSONObject(index)
            val coordinates = feature.optJSONObject("geometry")?.optJSONArray("coordinates") ?: return@mapNotNull null
            if (coordinates.length() < 2) return@mapNotNull null
            val lon = coordinates.optDouble(0, Double.NaN)
            val lat = coordinates.optDouble(1, Double.NaN)
            val props = feature.optJSONObject("properties") ?: org.json.JSONObject()
            val parts = listOf(
                props.optString("housenumber"), props.optString("name"), props.optString("street"),
                props.optString("district"), props.optString("city"), props.optString("county"),
                props.optString("postcode"), props.optString("country")
            ).filter { it.isNotBlank() }.distinct()
            val label = parts.joinToString(", ")
            if (lat.isFinite() && lon.isFinite() && label.isNotBlank()) MapSearchResult(label, lat, lon) else null
        }
        results.distinctBy { "${it.name.lowercase(Locale.UK)}|${it.latitude}|${it.longitude}" }
    } finally { connection.disconnect() }
}

private suspend fun searchRoutePlaces(query: String): List<String> = withContext(Dispatchers.IO) {
    val encoded = URLEncoder.encode(query, "UTF-8")
    val connection = (URL("https://nominatim.openstreetmap.org/search?q=$encoded&format=jsonv2&addressdetails=1&limit=5").openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"; setRequestProperty("User-Agent", "HAULWAY-Navigation/1.0 (Android route planner)"); connectTimeout = 12000; readTimeout = 15000
    }
    try { val array = org.json.JSONArray(connection.inputStream.bufferedReader().use { it.readText() }); (0 until array.length()).map { array.getJSONObject(it).optString("display_name") }.filter { it.isNotBlank() } }
    finally { connection.disconnect() }
}

private fun breakMinutesFromStop(stop: String): Int =
    if (stop.startsWith("BREAK|", ignoreCase = true)) stop.substringAfter('|').toIntOrNull()?.coerceIn(1, 240) ?: 0 else 0

private fun durationWithBreaks(base: String, breakMinutes: Int): String {
    val match = Regex("(\\d+)\\s*h(?:\\s*(\\d+)\\s*min)?|(\\d+)\\s*min", RegexOption.IGNORE_CASE).find(base) ?: return base
    val hours = match.groupValues[1].toIntOrNull() ?: 0
    val minutes = match.groupValues[2].toIntOrNull() ?: match.groupValues[3].toIntOrNull() ?: 0
    val total = hours * 60 + minutes + breakMinutes
    return "${total / 60} h ${total % 60} min"
}

@Composable
private fun RoutePlannerStopRow(number: Int, title: String, subtitle: String, highlighted: Boolean, canMove: Boolean, canRemove: Boolean, modifier: Modifier = Modifier, onEdit: () -> Unit, onRemove: () -> Unit) {
    Row(modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(7.dp)).background(Color(0xFF202A32)).clickable(onClick = onEdit).padding(horizontal = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(color = if (highlighted) Color(0xFF168BD7) else Color(0xFF398F58), shape = CircleShape, modifier = Modifier.size(32.dp)) {
            Box(contentAlignment = Alignment.Center) { Text("$number", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
        }
        Spacer(Modifier.width(6.dp))
        Column(Modifier.weight(1f).padding(vertical = 5.dp), verticalArrangement = Arrangement.Center) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(subtitle, color = Color(0xFFAEBBC5), fontSize = 12.sp, maxLines = 1)
        }
        if (canMove) Icon(Icons.Default.DragHandle, contentDescription = "Hold and drag to reorder", tint = Color(0xFFB8C5CF), modifier = Modifier.size(22.dp))
        if (canRemove) Icon(Icons.Default.Close, contentDescription = "Remove stop", tint = Color(0xFFD6E0E8), modifier = Modifier.size(18.dp).clickable(onClick = onRemove))
    }
}

@Composable
private fun TruckProfileEditor(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("haulway_truck_profile", Context.MODE_PRIVATE) }
    var profiles by remember { mutableStateOf((prefs.getString("profileTypes", "ARTIC|RIGID|VAN|WAG & DRAG|BUS") ?: "ARTIC|RIGID|VAN|WAG & DRAG|BUS").split("|").filter { it.isNotBlank() }) }
    var selectedProfile by remember { mutableIntStateOf(prefs.getInt("selectedProfile", 0).coerceIn(0, (profiles.size - 1).coerceAtLeast(0))) }
    var addProfileMenu by remember { mutableStateOf(false) }
    fun defaultDimension(type: String, field: String): String = when (type) {
        "ARTIC" -> when(field) { "height" -> "4.00"; "width" -> "2.55"; else -> "16.50" }
        "RIGID" -> when(field) { "height" -> "3.80"; "width" -> "2.55"; else -> "9.20" }
        "VAN" -> when(field) { "height" -> "2.60"; "width" -> "2.20"; else -> "6.20" }
        "WAG & DRAG" -> when(field) { "height" -> "4.00"; "width" -> "2.55"; else -> "18.75" }
        else -> when(field) { "height" -> "3.70"; "width" -> "2.55"; else -> "13.50" }
    }
    val currentType = profiles.getOrElse(selectedProfile) { "ARTIC" }
    var vehicleType by remember { mutableStateOf(prefs.getString("vehicleType", "Articulated") ?: "Articulated") }
    var trailerType by remember { mutableStateOf(prefs.getString("trailerType", "Standard") ?: "Standard") }
    var height by remember { mutableStateOf(prefs.getString("profile_${selectedProfile}_height", defaultDimension(currentType, "height")) ?: defaultDimension(currentType, "height")) }
    var width by remember { mutableStateOf(prefs.getString("profile_${selectedProfile}_width", defaultDimension(currentType, "width")) ?: defaultDimension(currentType, "width")) }
    var length by remember { mutableStateOf(prefs.getString("profile_${selectedProfile}_length", defaultDimension(currentType, "length")) ?: defaultDimension(currentType, "length")) }
    var grossWeight by remember { mutableStateOf(prefs.getString("grossWeight", "44000") ?: "44000") }
    var axleWeight by remember { mutableStateOf(prefs.getString("axleWeight", "11500") ?: "11500") }
    var maxSpeed by remember { mutableStateOf(prefs.getString("maxSpeed", "56") ?: "56") }
    var loadType by remember { mutableStateOf(prefs.getString("loadType", "General") ?: "General") }
    var saved by remember { mutableStateOf(false) }
    var menu by remember { mutableStateOf("") }
    val fieldBg = Color(0xFF202A32)
    val panel = Color(0xE90C1319)
    val blue = Color(0xFF0799E8)
    val muted = Color(0xFFB9C6D0)

    @Composable fun CompactValue(label: String, value: String, onValue: (String) -> Unit, modifier: Modifier = Modifier.fillMaxWidth()) {
        Column(modifier.padding(end = 5.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, color = muted, fontSize = 12.sp, maxLines = 1)
            androidx.compose.material3.OutlinedTextField(
                value = value, onValueChange = { newValue ->
                    onValue(newValue)
                    val key = when (label) {
                        "Total Weight (kg)" -> "profile_${selectedProfile}_gross"
                        "Axle Weight (kg)" -> "profile_${selectedProfile}_axle"
                        "Height (m)" -> "profile_${selectedProfile}_height"
                        "Width (m)" -> "profile_${selectedProfile}_width"
                        "Length (m)" -> "profile_${selectedProfile}_length"
                        else -> label
                    }
                    prefs.edit().putString(key, newValue).apply()
                    saved = true
                }, singleLine = true,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = blue, unfocusedBorderColor = Color(0xFF36434D), cursorColor = Color.White),
                shape = RoundedCornerShape(5.dp)
            )
        }
    }
    @Composable fun Choice(label: String, value: String, options: List<String>) {
        Box(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().clickable { menu = if (menu == label) "" else label }, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(label, color = muted, fontSize = 12.sp, maxLines = 1)
                Row(Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(5.dp)).background(fieldBg).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(value, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f), maxLines = 1)
                    Icon(Icons.Default.ArrowDropDown, null, tint = Color.White, modifier = Modifier.size(17.dp))
                }
            }
            androidx.compose.material3.DropdownMenu(expanded = menu == label, onDismissRequest = { menu = "" }) {
                options.forEach { option -> androidx.compose.material3.DropdownMenuItem(text = { Text(option) }, onClick = {
                    when(label) {
                        "Vehicle Type" -> { vehicleType = option; prefs.edit().putString("vehicleType", option).apply() }
                        "Trailer Type" -> { trailerType = option; prefs.edit().putString("trailerType", option).apply() }
                        "Load Type" -> { loadType = option; prefs.edit().putString("loadType", option).apply() }
                        "Max Speed" -> { maxSpeed = option.substringBefore(" mph"); prefs.edit().putString("maxSpeed", maxSpeed).apply() }
                    }
                    saved = true; menu = ""
                }) }
            }
        }
    }

    Row(modifier.fillMaxSize().background(Color(0xFF0B1218)).padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        // Left: saved vehicle profiles. The list scrolls independently so users can keep adding profiles.
        Column(Modifier.weight(0.26f).fillMaxHeight().clip(RoundedCornerShape(9.dp)).background(Color(0xE90A1117)).padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("MY VEHICLES", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                profiles.forEachIndexed { index, type ->
                    val typeIndex = index + 1
                    val weightDefaults = when(type) { "ARTIC" -> Pair("44000", "11500"); "RIGID" -> Pair("26000", "10000"); "VAN" -> Pair("7500", "2500"); "WAG & DRAG" -> Pair("44000", "10000"); else -> Pair("18000", "8000") }
                    Surface(color = if (selectedProfile == index) Color(0xFF123B55) else Color(0xFF121B22), shape = RoundedCornerShape(6.dp), border = androidx.compose.foundation.BorderStroke(if(selectedProfile==index) 1.dp else 0.5.dp, if(selectedProfile==index) blue else Color(0xFF303B44))) {
                        Row(Modifier.fillMaxWidth().heightIn(min = 76.dp).padding(horizontal = 6.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Row(Modifier.weight(1f).clickable {
                                selectedProfile = index
                                prefs.edit().putInt("selectedProfile", index).apply()
                                vehicleType = when(type) { "ARTIC" -> "Articulated"; "RIGID" -> "Rigid"; "VAN" -> "Van"; "WAG & DRAG" -> "Drawbar"; else -> "Bus" }
                                height = prefs.getString("profile_${index}_height", defaultDimension(type, "height")) ?: defaultDimension(type, "height")
                                width = prefs.getString("profile_${index}_width", defaultDimension(type, "width")) ?: defaultDimension(type, "width")
                                length = prefs.getString("profile_${index}_length", defaultDimension(type, "length")) ?: defaultDimension(type, "length")
                                grossWeight = prefs.getString("profile_${index}_gross", weightDefaults.first) ?: weightDefaults.first
                                axleWeight = prefs.getString("profile_${index}_axle", weightDefaults.second) ?: weightDefaults.second
                                trailerType = if(type == "ARTIC" || type == "WAG & DRAG") "Standard" else "None"
                                loadType = "General"; maxSpeed = if(type == "VAN" || type == "BUS") "70" else "56"
                                prefs.edit().putString("vehicleType", vehicleType).putString("trailerType", trailerType).putString("grossWeight", grossWeight).putString("axleWeight", axleWeight).putString("maxSpeed", maxSpeed).putString("loadType", loadType).apply()
                            }.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(when(type) { "ARTIC", "WAG & DRAG", "RIGID" -> Icons.Default.LocalShipping; "VAN" -> Icons.Default.DirectionsCar; else -> Icons.Default.DirectionsBus }, null, tint = if(selectedProfile==index) Color(0xFFBDE8FF) else Color(0xFF9EABB5), modifier = Modifier.size(23.dp))
                                Spacer(Modifier.width(6.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("TRUCK $typeIndex", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(type, color = muted, fontSize = 10.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                }
                            }
                            androidx.compose.material3.IconButton(onClick = {
                                if (profiles.size > 1) {
                                    val oldSelected = selectedProfile
                                    val edit = prefs.edit()
                                    val fields = listOf("height", "width", "length", "gross", "axle")
                                    // Compact the index-based saved settings so remaining profiles retain their own values.
                                    for (sourceIndex in (index + 1) until profiles.size) {
                                        for (field in fields) {
                                            val value = prefs.getString("profile_${sourceIndex}_$field", null)
                                            if (value != null) edit.putString("profile_${sourceIndex - 1}_$field", value) else edit.remove("profile_${sourceIndex - 1}_$field")
                                        }
                                    }
                                    for (field in fields) edit.remove("profile_${profiles.lastIndex}_$field")
                                    profiles = profiles.toMutableList().also { it.removeAt(index) }
                                    selectedProfile = when {
                                        oldSelected > index -> oldSelected - 1
                                        oldSelected == index -> (index - 1).coerceAtLeast(0)
                                        else -> oldSelected
                                    }.coerceIn(0, profiles.lastIndex)
                                    edit.putString("profileTypes", profiles.joinToString("|")).putInt("selectedProfile", selectedProfile).apply()
                                    val selectedType = profiles[selectedProfile]
                                    val selectedWeights = when(selectedType) { "ARTIC" -> Pair("44000", "11500"); "RIGID" -> Pair("26000", "10000"); "VAN" -> Pair("7500", "2500"); "WAG & DRAG" -> Pair("44000", "10000"); else -> Pair("18000", "8000") }
                                    vehicleType = when(selectedType) { "ARTIC" -> "Articulated"; "RIGID" -> "Rigid"; "VAN" -> "Van"; "WAG & DRAG" -> "Drawbar"; else -> "Bus" }
                                    height = prefs.getString("profile_${selectedProfile}_height", defaultDimension(selectedType, "height")) ?: defaultDimension(selectedType, "height")
                                    width = prefs.getString("profile_${selectedProfile}_width", defaultDimension(selectedType, "width")) ?: defaultDimension(selectedType, "width")
                                    length = prefs.getString("profile_${selectedProfile}_length", defaultDimension(selectedType, "length")) ?: defaultDimension(selectedType, "length")
                                    grossWeight = prefs.getString("profile_${selectedProfile}_gross", selectedWeights.first) ?: selectedWeights.first
                                    axleWeight = prefs.getString("profile_${selectedProfile}_axle", selectedWeights.second) ?: selectedWeights.second
                                    edit.putString("vehicleType", vehicleType).putString("grossWeight", grossWeight).putString("axleWeight", axleWeight).apply()
                                } else {
                                    Toast.makeText(context, "Keep at least one vehicle profile", Toast.LENGTH_SHORT).show()
                                }
                            }, modifier = Modifier.size(34.dp)) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete $type profile", tint = Color(0xFFFF8A80), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
            Box {
                Surface(Modifier.fillMaxWidth().height(44.dp).clickable { addProfileMenu = true }, color = Color(0xFF202A32), shape = RoundedCornerShape(5.dp), border = androidx.compose.foundation.BorderStroke(0.7.dp, Color(0xFF52606B))) {
                    Row(Modifier.padding(horizontal = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(5.dp)); Text("Add Profile", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                androidx.compose.material3.DropdownMenu(expanded = addProfileMenu, onDismissRequest = { addProfileMenu = false }) {
                    listOf("ARTIC", "RIGID", "VAN", "WAG & DRAG", "BUS").forEach { type ->
                        androidx.compose.material3.DropdownMenuItem(text = { Text(type) }, onClick = {
                            profiles = profiles + type
                            selectedProfile = profiles.lastIndex
                            vehicleType = when(type) { "ARTIC" -> "Articulated"; "RIGID" -> "Rigid"; "VAN" -> "Van"; "WAG & DRAG" -> "Drawbar"; else -> "Bus" }
                            height = defaultDimension(type, "height"); width = defaultDimension(type, "width"); length = defaultDimension(type, "length")
                            grossWeight = when(type) { "ARTIC" -> "44000"; "RIGID" -> "26000"; "VAN" -> "7500"; "WAG & DRAG" -> "44000"; else -> "18000" }
                            axleWeight = when(type) { "ARTIC" -> "11500"; "RIGID" -> "10000"; "VAN" -> "2500"; "WAG & DRAG" -> "10000"; else -> "8000" }
                            trailerType = if(type == "ARTIC" || type == "WAG & DRAG") "Standard" else "None"
                            loadType = "General"; maxSpeed = if(type == "VAN" || type == "BUS") "70" else "56"
                            prefs.edit().putString("profileTypes", profiles.joinToString("|")).putInt("selectedProfile", selectedProfile)
                                .putString("profile_${selectedProfile}_height",height).putString("profile_${selectedProfile}_width",width).putString("profile_${selectedProfile}_length",length)
                                .putString("profile_${selectedProfile}_gross",grossWeight).putString("profile_${selectedProfile}_axle",axleWeight)
                                .putString("vehicleType", vehicleType).putString("trailerType", trailerType).putString("grossWeight", grossWeight).putString("axleWeight", axleWeight).putString("maxSpeed", maxSpeed).putString("loadType", loadType).apply()
                            addProfileMenu = false
                        })
                    }
                }
            }
        }

        // Right workspace: truck silhouette and measurements, then vehicle configuration.
        Column(Modifier.weight(0.74f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Approved photorealistic 3D vehicle banner; the selected profile chooses its matching vehicle.
            Surface(Modifier.fillMaxWidth().weight(0.35f), color = Color(0xFF111A21), shape = RoundedCornerShape(9.dp)) {
                BoxWithConstraints(Modifier.fillMaxSize()) {
                    val vehicleArt = when (currentType) {
                        "ARTIC" -> R.drawable.haulway_truck_3d_1
                        "RIGID" -> R.drawable.haulway_truck_3d_2
                        "VAN" -> R.drawable.haulway_truck_3d_3
                        "WAG & DRAG" -> R.drawable.haulway_truck_3d_4
                        else -> R.drawable.haulway_truck_3d_5
                    }
                    Image(
                        painter = painterResource(vehicleArt),
                        contentDescription = when (selectedProfile) { 0 -> "Articulated HGV 3D dimensions"; 1 -> "Rigid truck 3D dimensions"; else -> "Van 3D dimensions" },
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(9.dp)),
                        contentScale = ContentScale.FillBounds
                    )
                    // Dynamic values cover the sample numbers in the artwork and always mirror the editable fields.
                    Text("H  $height m", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, style = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(Color.Black, androidx.compose.ui.geometry.Offset(1f, 2f), 4f)), modifier = Modifier.align(Alignment.CenterStart).offset(x = maxWidth * 0.01f, y = (-4).dp))
                    Text("L  $length m", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, style = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(Color.Black, androidx.compose.ui.geometry.Offset(1f, 2f), 4f)), modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-1).dp))
                    Text("W  $width m", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, style = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(Color.Black, androidx.compose.ui.geometry.Offset(1f, 2f), 4f)), modifier = Modifier.align(Alignment.BottomEnd).padding(end = 4.dp, bottom = 1.dp))
                }
            }
            Row(Modifier.fillMaxWidth().weight(0.65f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1.05f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    CompactValue("Total Weight (kg)", grossWeight, { grossWeight=it })
                    CompactValue("Axle Weight (kg)", axleWeight, { axleWeight=it })
                    Row(Modifier.fillMaxWidth()) {
                        CompactValue("Height (m)", height, { height=it }, Modifier.weight(1f))
                        CompactValue("Width (m)", width, { width=it }, Modifier.weight(1f))
                    }
                    CompactValue("Length (m)", length, { length=it })
                }
                Column(Modifier.weight(0.95f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Choice("Vehicle Type", vehicleType, listOf("Articulated", "Rigid", "Van", "Drawbar") )
                    Choice("Trailer Type", trailerType, listOf("Standard", "Refrigerated", "Curtainsider", "Flatbed", "Tanker", "None"))
                    Choice("Load Type", loadType, listOf("General", "Hazardous", "Abnormal", "Refrigerated", "Livestock"))
                    Choice("Max Speed", "$maxSpeed mph (${((maxSpeed.toIntOrNull() ?: 56) * 1.609).toInt()} km/h)", listOf("56 mph", "60 mph", "70 mph"))
                    Spacer(Modifier.height(2.dp))
                }
            }
        }
    }
}

private data class HaulwayTrafficIncident(
    val id: String,
    val category: String,
    val severity: String,
    val description: String,
    val road: String,
    val delaySeconds: Int,
    val latitude: Double?,
    val longitude: Double?
)

private data class HaulwayTrafficSnapshot(
    val incidents: List<HaulwayTrafficIncident>,
    val fetchedAt: Long,
    val trafficModelId: String?
)

private suspend fun fetchHaulwayTrafficIncidents(context: Context, scope: String, mapBoundsJson: String? = null): HaulwayTrafficSnapshot =
    withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.TOMTOM_TRAFFIC_API_KEY.trim()
        check(apiKey.isNotEmpty()) {
            "TomTom Traffic API key is missing. Add TOMTOM_TRAFFIC_API_KEY to GitHub Actions secrets or local.properties."
        }

        val routeJson = RoutePlanStore.coordinatesJson
        if (scope == "ROUTE") {
            check(!routeJson.isNullOrBlank()) { "Calculate an active route before requesting traffic along the route." }
        }
        val routeBounds = if (scope == "ROUTE" && !routeJson.isNullOrBlank()) {
            val coords = org.json.JSONArray(routeJson)
            val points = (0 until coords.length()).mapNotNull { index ->
                val point = coords.optJSONArray(index) ?: return@mapNotNull null
                if (point.length() < 2) null else point.getDouble(0) to point.getDouble(1)
            }
            if (points.isNotEmpty()) {
                val centerLon = (points.minOf { it.first } + points.maxOf { it.first }) / 2.0
                val centerLat = (points.minOf { it.second } + points.maxOf { it.second }) / 2.0
                val halfLon = ((points.maxOf { it.first } - points.minOf { it.first }) / 2.0 + 0.08).coerceAtMost(0.45)
                val halfLat = ((points.maxOf { it.second } - points.minOf { it.second }) / 2.0 + 0.08).coerceAtMost(0.45)
                listOf(centerLon - halfLon, centerLat - halfLat, centerLon + halfLon, centerLat + halfLat)
            } else null
        } else null

        val mapBounds = if (scope == "MAP") {
            check(!mapBoundsJson.isNullOrBlank()) { "Map viewport is not ready yet. Wait for the map to finish loading, then refresh traffic." }
            val bounds = JSONObject(mapBoundsJson)
            val west = bounds.getDouble("west")
            val north = bounds.getDouble("north")
            val east = bounds.getDouble("east")
            val south = bounds.getDouble("south")
            check(west.isFinite() && east.isFinite() && north.isFinite() && south.isFinite() && north > south) { "The visible map area is invalid. Move or zoom the map and try again." }
            val lonSpan = if (east >= west) east - west else (180.0 - west) + (east + 180.0)
            val latSpan = north - south
            val approxAreaKm2 = latSpan * 111.0 * lonSpan * 111.0 * kotlin.math.cos(Math.toRadians((north + south) / 2.0)).coerceAtLeast(0.1)
            check(approxAreaKm2 <= 10000.0) { "The visible map area is too large for a single live traffic request. Zoom in and refresh again." }
            listOf(west, south, east, north)
        } else null
        val bbox = mapBounds ?: routeBounds ?: run {
            val location = resolveHaulwayCurrentLocation(context)
            val lon = location.first
            val lat = location.second
            val latPad = 0.22
            val lonPad = 0.30 / maxOf(0.25, kotlin.math.cos(Math.toRadians(lat)))
            listOf(lon - lonPad, lat - latPad, lon + lonPad, lat + latPad)
        }
        val bboxText = bbox.joinToString(",") { String.format(Locale.US, "%.6f", it) }
        val endpoint = "https://api.tomtom.com/maps/orbis/traffic/incidents/details?apiVersion=2&bbox=$bboxText&timeValidity=present"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 12000
            readTimeout = 18000
            setRequestProperty("TomTom-Api-Key", apiKey)
            setRequestProperty("TomTom-Api-Version", "2")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Accept-Language", "en-GB")
            setRequestProperty("Attributes", "incidents(type,geometry(type,coordinates),properties(id,iconCategory,magnitudeOfDelay,events(description),from,to,roadNumbers,delayInSeconds,timeValidity,lastReportTime))")
            setRequestProperty("User-Agent", "HAULWAY-Navigation/1.0")
        }
        try {
            val code = connection.responseCode
            val body = (if (code in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            check(code in 200..299) {
                when (code) {
                    401, 403 -> "TomTom rejected the API key or Traffic API access (HTTP $code). Check your TomTom project."
                    429 -> "TomTom Traffic API quota/rate limit reached. Try again later."
                    else -> "TomTom Traffic API error (HTTP $code): ${body.take(180)}"
                }
            }
            val root = JSONObject(body)
            val array = root.optJSONArray("incidents") ?: org.json.JSONArray()
            val incidents = (0 until array.length()).mapNotNull { index ->
                val item = array.optJSONObject(index) ?: return@mapNotNull null
                val props = item.optJSONObject("properties") ?: return@mapNotNull null
                val geometry = item.optJSONObject("geometry")
                val coordinates = geometry?.optJSONArray("coordinates")
                val firstPoint = if (geometry?.optString("type") == "Point") coordinates else coordinates?.optJSONArray(0)
                val events = props.optJSONArray("events")
                val description = events?.optJSONObject(0)?.optString("description").orEmpty()
                val roads = props.optJSONArray("roadNumbers")
                val roadNumbers = (0 until (roads?.length() ?: 0)).mapNotNull { roads?.optString(it) }.filter { it.isNotBlank() }
                HaulwayTrafficIncident(
                    id = props.optString("id", "incident-$index"),
                    category = props.optString("iconCategory", "unknown"),
                    severity = props.optString("magnitudeOfDelay", "unknown"),
                    description = description.ifBlank { props.optString("iconCategory", "Traffic incident") },
                    road = roadNumbers.joinToString(", ").ifBlank {
                        listOf(props.optString("from"), props.optString("to")).filter { it.isNotBlank() }.joinToString(" → ")
                    },
                    delaySeconds = props.optInt("delayInSeconds", 0),
                    latitude = if (firstPoint != null && firstPoint.length() >= 2) firstPoint.optDouble(1).takeIf { it.isFinite() } else null,
                    longitude = if (firstPoint != null && firstPoint.length() >= 2) firstPoint.optDouble(0).takeIf { it.isFinite() } else null
                )
            }
            HaulwayTrafficSnapshot(incidents, System.currentTimeMillis(), connection.getHeaderField("TrafficModelID"))
        } finally {
            connection.disconnect()
        }
    }

@Composable
private fun TrafficVisionPanel(modifier: Modifier = Modifier) {
    data class TrafficLayer(val name: String, val group: String, val enabledByDefault: Boolean = true)
    val layers = listOf(
        TrafficLayer("Live traffic flow", "TRAFFIC"),
        TrafficLayer("Traffic jams & queues", "TRAFFIC"),
        TrafficLayer("Slow traffic", "TRAFFIC"),
        TrafficLayer("Accidents & collisions", "INCIDENTS"),
        TrafficLayer("Vehicle breakdowns", "INCIDENTS"),
        TrafficLayer("Police / emergency incidents", "INCIDENTS"),
        TrafficLayer("Obstacles & debris", "INCIDENTS"),
        TrafficLayer("Roadworks", "ROAD STATUS"),
        TrafficLayer("Lane closures", "ROAD STATUS"),
        TrafficLayer("Full road closures", "ROAD STATUS"),
        TrafficLayer("Temporary traffic lights", "ROAD STATUS"),
        TrafficLayer("Bridge closures", "ROAD STATUS"),
        TrafficLayer("Flooding on roads", "WEATHER"),
        TrafficLayer("Snow & ice", "WEATHER"),
        TrafficLayer("Strong wind", "WEATHER"),
        TrafficLayer("Fog / poor visibility", "WEATHER"),
        TrafficLayer("Severe weather warnings", "WEATHER"),
        TrafficLayer("Height restrictions", "HGV RESTRICTIONS"),
        TrafficLayer("Weight restrictions", "HGV RESTRICTIONS"),
        TrafficLayer("Width restrictions", "HGV RESTRICTIONS"),
        TrafficLayer("Length restrictions", "HGV RESTRICTIONS"),
        TrafficLayer("Low bridges", "HGV RESTRICTIONS"),
        TrafficLayer("Axle / vehicle restrictions", "HGV RESTRICTIONS"),
        TrafficLayer("Low emission zones", "CHARGES & CONTROL", false),
        TrafficLayer("Toll roads & charges", "CHARGES & CONTROL", false),
        TrafficLayer("Congestion charge zones", "CHARGES & CONTROL", false),
        TrafficLayer("Speed cameras", "SAFETY"),
        TrafficLayer("Average speed cameras", "SAFETY"),
        TrafficLayer("Railway crossings", "SAFETY"),
        TrafficLayer("Ferries & bridge status", "TRAVEL"),
        TrafficLayer("Truck parking & rest areas", "TRAVEL", false),
        TrafficLayer("Fuel stops", "TRAVEL", false)
    )
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("haulway_traffic_layers", Context.MODE_PRIVATE) }
    val enabled = remember {
        mutableStateListOf(*layers.mapIndexed { index, item ->
            prefs.getBoolean("layer_$index", item.enabledByDefault)
        }.toTypedArray())
    }
    var searchText by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf("ALL") }
    var selectedScope by remember { mutableStateOf("NEAR ME") }
    var trafficSnapshot by remember { mutableStateOf<HaulwayTrafficSnapshot?>(null) }
    var trafficLoading by remember { mutableStateOf(false) }
    var trafficError by remember { mutableStateOf<String?>(null) }
    var mapViewportBounds by remember { mutableStateOf<String?>(null) }
    val trafficScope = rememberCoroutineScope()
    val groups = listOf("ALL", "TRAFFIC", "INCIDENTS", "ROAD STATUS", "WEATHER", "HGV RESTRICTIONS", "CHARGES & CONTROL", "SAFETY", "TRAVEL")
    val visibleLayers = layers.mapIndexed { index, layer -> index to layer }.filter { (_, layer) ->
        (selectedGroup == "ALL" || layer.group == selectedGroup) &&
            layer.name.contains(searchText, ignoreCase = true)
    }
    val displayedIncidents = trafficSnapshot?.incidents?.filter { incident ->
        when (incident.category.lowercase()) {
            "accident" -> enabled[3]
            "jam" -> enabled[1] || enabled[2]
            "roadworks" -> enabled[7]
            "laneclosed" -> enabled[8]
            "roadclosed" -> enabled[9]
            "brokendownvehicle" -> enabled[4]
            "fog" -> enabled[15]
            "rain", "flooding", "ice", "wind", "dangerousconditions" -> enabled[12] || enabled[13] || enabled[14] || enabled[16]
            else -> enabled[0] || enabled[5] || enabled[6]
        }
    } ?: emptyList()
    val activeCount = enabled.count { it }
    val muted = Color(0xFF9AAAB7)
    val panel = Color(0xFF151E27)
    val tile = Color(0xFF202C37)
    val blue = Color(0xFF168DD2)
    Row(modifier = modifier.background(Color(0xFF0D141B))) {
        Column(
            modifier = Modifier.weight(0.34f).fillMaxHeight()
                .background(panel).verticalScroll(rememberScrollState())
                .padding(horizontal = 9.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("TRAFFIC CONTROL", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("$activeCount LAYERS ENABLED", color = AccentBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Surface(shape = RoundedCornerShape(5.dp), color = if (trafficSnapshot != null) Color(0xFF174A35) else Color(0xFF49351E)) {
                    Text(if (trafficSnapshot != null) "LIVE CONNECTED" else "LIVE READY", color = if (trafficSnapshot != null) Color(0xFF8CE0AE) else Color(0xFFFFC46B), fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp))
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("MAP", "ROUTE", "NEAR ME").forEach { scope ->
                    Surface(
                        modifier = Modifier.weight(1f).clickable { selectedScope = scope },
                        shape = RoundedCornerShape(5.dp),
                        color = if (selectedScope == scope) blue else tile
                    ) {
                        Text(scope, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 7.dp))
                    }
                }
            }
            Surface(color = if (trafficError != null) Color(0xFF422629) else Color(0xFF173247), shape = RoundedCornerShape(7.dp)) {
                Column(Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (trafficSnapshot != null) Icons.Default.CloudDone else Icons.Default.CloudDownload, contentDescription = null,
                            tint = if (trafficSnapshot != null) Color(0xFF8CE0AE) else Color(0xFF8BD5FF), modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(if (trafficSnapshot != null) "TOMTOM LIVE TRAFFIC" else "TOMTOM TRAFFIC CONNECTION", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        trafficError ?: trafficSnapshot?.let { "${it.incidents.size} verified incidents · refreshed ${SimpleDateFormat("HH:mm", Locale.UK).format(Date(it.fetchedAt))}" }
                            ?: "Add a TomTom Traffic API key to load verified live incidents. No demo incidents are shown.",
                        color = Color(0xFFE5EDF3), fontSize = 8.sp, lineHeight = 11.sp
                    )
                    Button(
                        onClick = {
                            if (!trafficLoading) {
                                trafficLoading = true
                                trafficError = null
                                trafficScope.launch {
                                    try {
                                        trafficSnapshot = fetchHaulwayTrafficIncidents(context, selectedScope, mapViewportBounds)
                                    } catch (error: Exception) {
                                        trafficError = error.message ?: "Traffic feed could not be loaded."
                                    } finally {
                                        trafficLoading = false
                                    }
                                }
                            }
                        },
                        enabled = !trafficLoading,
                        modifier = Modifier.fillMaxWidth().height(31.dp),
                        contentPadding = PaddingValues(2.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF087DBA), disabledContainerColor = Color(0xFF45525D))
                    ) {
                        if (trafficLoading) CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                        else Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(if (trafficLoading) "LOADING LIVE DATA…" else "REFRESH LIVE TRAFFIC", fontSize = 8.sp)
                    }
                }
            }
            BasicTextField(
                value = searchText,
                onValueChange = { searchText = it },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp),
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(tile).padding(horizontal = 9.dp, vertical = 10.dp),
                decorationBox = { inner ->
                    if (searchText.isEmpty()) Text("Search traffic layers…", color = muted, fontSize = 11.sp)
                    inner()
                }
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Button(onClick = {
                    layers.indices.forEach { index -> enabled[index] = true; prefs.edit().putBoolean("layer_$index", true).apply() }
                }, modifier = Modifier.weight(1f).height(32.dp), contentPadding = PaddingValues(2.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF176B9D))) {
                    Text("SELECT ALL", fontSize = 8.sp)
                }
                Button(onClick = {
                    layers.indices.forEach { index -> enabled[index] = false; prefs.edit().putBoolean("layer_$index", false).apply() }
                }, modifier = Modifier.weight(1f).height(32.dp), contentPadding = PaddingValues(2.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF39434C))) {
                    Text("CLEAR ALL", fontSize = 8.sp)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                groups.forEach { group ->
                    val count = if (group == "ALL") layers.size else layers.count { it.group == group }
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { selectedGroup = group },
                        shape = RoundedCornerShape(5.dp),
                        color = if (selectedGroup == group) Color(0xFF17496A) else Color.Transparent
                    ) {
                        Row(Modifier.padding(horizontal = 7.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(group, color = if (selectedGroup == group) Color.White else muted, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text(count.toString(), color = muted, fontSize = 8.sp)
                        }
                    }
                }
            }
            visibleLayers.forEach { (index, layer) ->
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 38.dp)
                        .clip(RoundedCornerShape(6.dp)).background(tile)
                        .clickable { enabled[index] = !enabled[index]; prefs.edit().putBoolean("layer_$index", enabled[index]).apply() }
                        .padding(start = 7.dp, end = 2.dp, top = 3.dp, bottom = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (layer.group) {
                            "INCIDENTS" -> Icons.Default.CarCrash
                            "ROAD STATUS" -> Icons.Default.Construction
                            "WEATHER" -> Icons.Default.Cloud
                            "HGV RESTRICTIONS" -> Icons.Default.LocalShipping
                            "CHARGES & CONTROL" -> Icons.Default.Eco
                            "SAFETY" -> Icons.Default.Speed
                            "TRAVEL" -> Icons.Default.LocalParking
                            else -> Icons.Default.Traffic
                        },
                        contentDescription = null, tint = AccentBlue, modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(layer.name, color = Color.White, fontSize = 9.sp, lineHeight = 11.sp, modifier = Modifier.weight(1f))
                    androidx.compose.material3.Switch(
                        checked = enabled[index],
                        onCheckedChange = { enabled[index] = it; prefs.edit().putBoolean("layer_$index", it).apply() },
                        modifier = Modifier.scale(0.62f).width(36.dp),
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            checkedThumbColor = Color.White, checkedTrackColor = blue,
                            uncheckedThumbColor = Color(0xFFCBD3DA), uncheckedTrackColor = Color(0xFF53616C)
                        )
                    )
                }
            }
            trafficSnapshot?.let { snapshot ->
                Text("LIVE INCIDENTS · ${snapshot.incidents.size}", color = Color(0xFF8BD5FF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                if (displayedIncidents.isEmpty()) {
                    Text("No incidents match the enabled layers in this area.", color = muted, fontSize = 8.sp)
                } else displayedIncidents.take(12).forEach { incident ->
                    Surface(color = Color(0xFF263440), shape = RoundedCornerShape(6.dp)) {
                        Column(Modifier.fillMaxWidth().padding(7.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(incident.category.replaceFirstChar { it.uppercase() }, color = Color(0xFFFFC46B), fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text(incident.severity.uppercase(), color = Color.White, fontSize = 7.sp)
                            }
                            Text(incident.description, color = Color.White, fontSize = 9.sp, lineHeight = 11.sp)
                            if (incident.road.isNotBlank()) Text(incident.road, color = muted, fontSize = 8.sp)
                            if (incident.delaySeconds > 0) Text("Reported delay: ${incident.delaySeconds / 60} min", color = Color(0xFFFFC46B), fontSize = 8.sp)
                        }
                    }
                }
            }
            Text("FILTERS ARE SAVED ON THIS DEVICE", color = muted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
        Box(Modifier.weight(0.66f).fillMaxHeight().padding(start = 3.dp)) {
            RoutePlannerMapView(routeJson = null, incidents = displayedIncidents, onViewportBounds = { mapViewportBounds = it })
            Column(Modifier.align(Alignment.TopStart).padding(9.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(color = Color(0xE619252F), shape = RoundedCornerShape(7.dp)) {
                    Column(Modifier.padding(horizontal = 9.dp, vertical = 7.dp)) {
                        Text("TRAFFIC OVERVIEW", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(when (selectedScope) {
                            "ROUTE" -> "Scope: active route area"
                            "NEAR ME" -> "Scope: around current GPS position"
                            else -> if (mapViewportBounds != null) "Scope: visible map area" else "Scope: waiting for map viewport"
                        }, color = muted, fontSize = 8.sp)
                    }
                }
                Surface(color = Color(0xE619252F), shape = RoundedCornerShape(7.dp)) {
                    Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("MAP LEGEND", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        listOf("Traffic flow" to Color(0xFFFFB74D), "Accident / incident" to Color(0xFFE45A5A), "Roadworks / closure" to Color(0xFFFF8A3D), "HGV restriction" to Color(0xFFB58AE8), "Weather hazard" to Color(0xFF65BDEB)).forEach { item ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(7.dp).clip(CircleShape).background(item.second))
                                Spacer(Modifier.width(5.dp))
                                Text(item.first, color = Color.White, fontSize = 8.sp)
                            }
                        }
                        Text("Legend only — no incident markers are fabricated.", color = Color(0xFFFFC46B), fontSize = 7.sp)
                    }
                }
            }
            Surface(
                modifier = Modifier.align(Alignment.BottomEnd).padding(9.dp),
                color = Color(0xE619252F), shape = RoundedCornerShape(7.dp)
            ) {
                Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("DATA STATUS", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(if (trafficSnapshot != null) "Traffic feed: TomTom live" else "Traffic feed: awaiting API key", color = if (trafficSnapshot != null) Color(0xFF8CE0AE) else Color(0xFFFFC46B), fontSize = 8.sp)
                    Text("Map tiles: existing basemap unchanged", color = muted, fontSize = 8.sp)
                    Text("Last update: ${trafficSnapshot?.let { SimpleDateFormat("HH:mm:ss", Locale.UK).format(Date(it.fetchedAt)) } ?: "not available"}", color = muted, fontSize = 8.sp)
                }
            }
        }
    }
}
@Composable
private fun TrafficBottomNavigation(
    section: String,
    blue: Color,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    Row(Modifier.fillMaxWidth().height(58.dp).background(Color(0xFF101419)).padding(horizontal = 5.dp, vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        listOf("HOME", "MAP", "ROUTE", "SERVICES", "TRAFFIC", "MORE").forEach { nav ->
            val selected = nav == "TRAFFIC"
            Surface(Modifier.weight(1f).fillMaxHeight().clickable {
                when (nav) {
                    "MAP" -> onBack()
                    "HOME" -> onNavigate("HOME")
                    "ROUTE" -> onNavigate("ROUTE PLANNER")
                    "SERVICES" -> onNavigate("SERVICES & POI")
                    "TRAFFIC" -> onNavigate("TRAFFIC")
                    else -> onNavigate("SETTINGS")
                }
            }, shape = RoundedCornerShape(8.dp), color = if (selected) blue else Color(0xFF202831)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(when(nav) { "HOME" -> Icons.Default.Home; "MAP" -> Icons.Default.Map; "ROUTE" -> Icons.Default.Route; "SERVICES" -> Icons.Default.LocalGasStation; "TRAFFIC" -> Icons.Default.Warning; else -> Icons.Default.MoreHoriz }, contentDescription = nav, tint = Color.White, modifier = Modifier.size(19.dp))
                    Text(nav, color = Color.White, fontSize = 9.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun HaulwaySectionScreen(
    section: String,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val bg = Color(0xFF101419)
    val card = Color(0xFF202831)
    val blue = Color(0xFF087ED1)
    val items = when (section) {
        "HOME" -> listOf("NAVIGATION", "360 STREET VIEW", "ROUTE PLANNER", "TRUCK PROFILE", "TRUCK PARKING", "SERVICES & POI", "TRAFFIC", "MY PLACES", "HISTORY", "SETTINGS")
        "360 STREET VIEW" -> listOf("Search coordinates", "Google Street View", "Mapillary imagery", "KartaView imagery")
        "ROUTE PLANNER" -> listOf("Start location", "Destination", "Add stop", "Route options", "Avoid toll roads", "Avoid low bridges", "Save route", "Start navigation")
        "TRUCK PROFILE" -> listOf("Vehicle type: Articulated", "Height: 4.00 m", "Width: 2.55 m", "Length: 16.50 m", "Gross weight: 44,000 kg", "Axle weight: 11,500 kg", "Load type: General", "Maximum speed: 56 mph", "Save vehicle profile")
        "TRUCK PARKING" -> listOf("Near me", "Along route", "At destination", "Truck Park M62", "Moto Hospitality", "Rotherham Truck Stop", "Doncaster Services")
        "SERVICES & POI" -> listOf("Truck parking", "Fuel stations", "Truck wash", "Restaurants", "Shops", "Rest areas", "Weighbridges", "Tyre services", "Repair workshops")
        "TRAFFIC" -> listOf("Traffic layer", "Roadworks", "Accidents", "Road closures", "Vehicle restrictions", "Low emission zones", "Speed cameras", "Weather along route")
        "MY PLACES" -> listOf("Home", "Work", "Favourite 1", "Favourite 2", "Add place")
        "HISTORY" -> listOf("Recent routes", "Manchester → Hull", "Liverpool → Leeds", "Skelmersdale → Doncaster", "Trip statistics", "Distance driven", "Journey time")
        "SETTINGS" -> listOf("Map settings", "Map appearance", "Navigation", "Warnings", "Display", "Voice guidance", "Units & regional", "Offline maps", "About HAULWAY")
        else -> listOf("Search address or postcode", "Recent destinations", "Saved places")
    }
    val title = when (section) {
        "HOME" -> "HAULWAY DRIVER CENTRE"
        "360 STREET VIEW" -> "360° STREET VIEW"
        "ROUTE PLANNER" -> "ROUTE PLANNER"
        "TRUCK PROFILE" -> "TRUCK PROFILE"
        "TRUCK PARKING" -> "TRUCK PARKING"
        "SERVICES & POI" -> "SERVICES & POI"
        "TRAFFIC" -> "ROAD & TRAFFIC"
        "MY PLACES" -> "MY PLACES"
        "HISTORY" -> "HISTORY & STATISTICS"
        "SETTINGS" -> "SETTINGS"
        else -> section
    }
    if (section == "360 STREET VIEW") {
        HaulwayStreetImageryScreen(onBack = onBack)
        return
    }
    if (section == "TRAFFIC") {
        Column(Modifier.fillMaxSize().background(bg)) {
            TrafficVisionPanel(Modifier.weight(1f).fillMaxWidth())
            TrafficBottomNavigation(section = section, blue = blue, onBack = onBack, onNavigate = onNavigate)
        }
        return
    }
    Column(Modifier.fillMaxSize().background(bg).then(if (section == "ROUTE PLANNER" || section == "TRUCK PROFILE") Modifier else Modifier.padding(if (section == "SETTINGS") 8.dp else 14.dp))) {
        if (section != "ROUTE PLANNER" && section != "TRUCK PROFILE" && section != "SETTINGS") {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                MapControl(Icons.Default.ArrowBack, "Back to map", White, onClick = onBack)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("HAULWAY", color = Color(0xFF58B9F2), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(title, color = White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                }
                MapControl(Icons.Default.Map, "Open map", White, onClick = onBack)
            }
            Spacer(Modifier.height(16.dp))
        }
        if (section == "SETTINGS") {
            SettingsVision10(Modifier.weight(1f).fillMaxWidth())
        } else if (section == "HOME") {
            Surface(Modifier.fillMaxWidth().padding(bottom = 14.dp), shape = RoundedCornerShape(14.dp), color = Color(0xFF152E42)) {
                Column(Modifier.padding(18.dp)) {
                    Text("READY FOR YOUR NEXT JOURNEY?", color = White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Plan a truck-safe route, find services or manage your vehicle.", color = Color(0xFFB8C9D7), fontSize = 13.sp)
                }
            }
        }
        if (section == "SETTINGS") {
            // Vision 10 is rendered above; keep the shared bottom navigation below.
        } else if (section == "ROUTE PLANNER") {
            RoutePlannerPanel(onRouteReady = { onNavigate("MAP") }, modifier = Modifier.weight(1f))
        } else if (section == "TRUCK PROFILE") {
            TruckProfileEditor(Modifier.weight(1f).fillMaxWidth())
        } else androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(9.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(items.size) { index ->
                val label = items[index]
                val icon = when {
                    label.contains("NAVIGATION") || label.contains("Start navigation") -> Icons.Default.Navigation
                    label.contains("360", true) -> Icons.Default.Visibility
                    label.contains("ROUTE") || label.contains("route", true) -> Icons.Default.Route
                    label.contains("TRUCK") || label.contains("Vehicle") || label.contains("vehicle", true) -> Icons.Default.LocalShipping
                    label.contains("PARK") || label.contains("parking", true) -> Icons.Default.Place
                    label.contains("FUEL", true) || label.contains("Fuel") -> Icons.Default.LocalGasStation
                    label.contains("TRAFFIC", true) || label.contains("Road") || label.contains("Accident") -> Icons.Default.Warning
                    label.contains("SETTINGS", true) || label.contains("Map settings", true) -> Icons.Default.Settings
                    label.contains("place", true) || label == "Home" || label == "Work" -> Icons.Default.Place
                    label.contains("HISTORY", true) || label.contains("Recent") || label.contains("statistics", true) -> Icons.Default.History
                    label.contains("Search", true) || label.contains("Destination", true) -> Icons.Default.Search
                    else -> Icons.Default.ChevronRight
                }
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable {
                        when {
                            label.contains("NAVIGATION") || label.contains("Start navigation") -> onBack()
                            label.contains("360 STREET VIEW", true) -> onNavigate("360 STREET VIEW")
                            label.contains("ROUTE", true) || label.contains("stop", true) -> onNavigate("ROUTE PLANNER")
                            label.contains("TRUCK", true) || label.contains("vehicle", true) -> onNavigate("TRUCK PROFILE")
                            label.contains("PARK", true) -> onNavigate("TRUCK PARKING")
                            label.contains("SERVICE", true) || label.contains("FUEL", true) -> onNavigate("SERVICES & POI")
                            label.contains("TRAFFIC", true) || label.contains("Road", true) || label.contains("Accident", true) -> onNavigate("TRAFFIC")
                            label.contains("SETTING", true) || label.contains("Map appearance", true) -> onNavigate("SETTINGS")
                            label.contains("place", true) || label == "Home" || label == "Work" -> onNavigate("MY PLACES")
                            label.contains("HISTORY", true) || label.contains("statistics", true) -> onNavigate("HISTORY")
                            label.contains("Save", true) -> Toast.makeText(context, "This feature will be connected in a later stage", Toast.LENGTH_SHORT).show()
                            else -> Toast.makeText(context, "$label — ready for implementation", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp), color = card, shadowElevation = 2.dp
                ) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(9.dp), color = Color(0xFF172F43)) {
                            Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                                Icon(icon, contentDescription = null, tint = Color(0xFF73C9FF), modifier = Modifier.size(25.dp))
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Text(label, color = White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF8C9AA8))
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().height(58.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("HOME", "MAP", "ROUTE", "SERVICES", "TRAFFIC", "MORE").forEach { nav ->
                val selected = when (section) {
                    "HOME" -> nav == "HOME"
                    "ROUTE PLANNER" -> nav == "ROUTE"
                    "TRUCK PARKING", "SERVICES & POI" -> nav == "SERVICES"
                    "TRAFFIC" -> nav == "TRAFFIC"
                    else -> nav == "MORE"
                }
                Surface(Modifier.weight(1f).fillMaxHeight().clickable {
                    when (nav) {
                        "MAP" -> onBack()
                        "HOME" -> onNavigate("HOME")
                        "ROUTE" -> onNavigate("ROUTE PLANNER")
                        "SERVICES" -> onNavigate("SERVICES & POI")
                        "TRAFFIC" -> onNavigate("TRAFFIC")
                        else -> onNavigate("SETTINGS")
                    }
                }, shape = RoundedCornerShape(9.dp), color = if (selected) blue else Color(0xFF202831)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(when(nav) { "HOME" -> Icons.Default.Home; "MAP" -> Icons.Default.Map; "ROUTE" -> Icons.Default.Route; "SERVICES" -> Icons.Default.LocalGasStation; "TRAFFIC" -> Icons.Default.Warning; else -> Icons.Default.MoreHoriz }, contentDescription = nav, tint = White, modifier = Modifier.size(21.dp))
                        Text(nav, color = White, fontSize = 9.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsVision10(modifier: Modifier = Modifier) {
    val categories = listOf(
        "Map Settings" to Icons.Default.Map,
        "Map Appearance" to Icons.Default.Palette,
        "Navigation" to Icons.Default.Navigation,
        "Warnings" to Icons.Default.Warning,
        "Display" to Icons.Default.Monitor,
        "Voice Guidance" to Icons.Default.VolumeUp,
        "Units & Regional" to Icons.Default.Public,
        "Offline Maps" to Icons.Default.Download,
        "About" to Icons.Default.Info
    )
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("haulway_settings_v10", Context.MODE_PRIVATE) }
    var selected by remember { mutableStateOf("Map Settings") }
    var mapMode by remember { mutableStateOf(prefs.getString("map_mode", "Day") ?: "Day") }
    var showPoi by remember { mutableStateOf(prefs.getBoolean("show_poi", true)) }
    var traffic by remember { mutableStateOf(prefs.getBoolean("traffic_layer", true)) }
    var buildings3d by remember { mutableStateOf(prefs.getBoolean("buildings_3d", false)) }
    var mapStyle by remember { mutableStateOf(prefs.getString("map_style", "Voyager") ?: "Voyager") }
    val panel = Color(0xFF111B23)
    val panelInner = Color(0xFF0D1720)
    val blue = Color(0xFF0788E8)
    val line = Color(0xFF293A48)

    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            modifier = Modifier.weight(0.34f).fillMaxHeight(),
            shape = RoundedCornerShape(16.dp), color = panel, border = androidx.compose.foundation.BorderStroke(1.dp, line)
        ) {
            Column(Modifier.fillMaxSize().padding(8.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                categories.forEach { (label, icon) ->
                    val active = selected == label
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(48.dp).clickable { selected = label },
                        shape = RoundedCornerShape(9.dp),
                        color = if (active) blue else Color.Transparent,
                        border = if (active) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF35B9FF)) else null
                    ) {
                        Row(Modifier.fillMaxSize().padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(icon, contentDescription = null, tint = if (active) White else Color(0xFFDCE7EF), modifier = Modifier.size(23.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(label, color = White, fontSize = 13.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
                        }
                    }
                }
            }
        }
        Surface(
            modifier = Modifier.weight(0.66f).fillMaxHeight(),
            shape = RoundedCornerShape(16.dp), color = panel, border = androidx.compose.foundation.BorderStroke(1.dp, line)
        ) {
            Column(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (selected == "Map Settings") "Map Appearance" else selected, color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                when (selected) {
                    "Map Settings", "Map Appearance" -> {
                        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            listOf("Day", "Night", "Auto").forEach { mode ->
                                Surface(
                                    modifier = Modifier.weight(1f).fillMaxHeight().clickable { mapMode = mode; prefs.edit().putString("map_mode", mode).apply() },
                                    shape = RoundedCornerShape(11.dp),
                                    color = if (mapMode == mode) Color(0xFF123D5B) else panelInner,
                                    border = androidx.compose.foundation.BorderStroke(if (mapMode == mode) 2.dp else 1.dp, if (mapMode == mode) Color(0xFF00B8FF) else line)
                                ) {
                                    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                        Box(Modifier.fillMaxWidth().weight(1f).padding(5.dp).clip(RoundedCornerShape(7.dp)).background(
                                            when (mode) { "Day" -> Brush.verticalGradient(listOf(Color(0xFF79C9F5), Color(0xFF315D37), Color(0xFF858C8B))); "Night" -> Brush.verticalGradient(listOf(Color(0xFF071B30), Color(0xFF0D3150), Color(0xFF07111D))); else -> Brush.verticalGradient(listOf(Color(0xFF9B6C47), Color(0xFF455D58), Color(0xFF182C35))) }
                                        ), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Map, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(34.dp))
                                        }
                                        Text(mode, color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 7.dp))
                                    }
                                }
                            }
                        }
                        SettingsToggleRow("Show POI on map", showPoi, { showPoi = it; prefs.edit().putBoolean("show_poi", it).apply() }, blue, line)
                        SettingsToggleRow("Traffic layer", traffic, { traffic = it; prefs.edit().putBoolean("traffic_layer", it).apply() }, blue, line)
                        SettingsToggleRow("3D buildings", buildings3d, { buildings3d = it; prefs.edit().putBoolean("buildings_3d", it).apply() }, blue, line)
                        Row(Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Map style", color = White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            Surface(Modifier.width(165.dp).fillMaxHeight().clickable { mapStyle = when (mapStyle) { "Voyager" -> "Dark Matter"; "Dark Matter" -> "Positron"; else -> "Voyager" }; prefs.edit().putString("map_style", mapStyle).apply() }, shape = RoundedCornerShape(8.dp), color = panelInner, border = androidx.compose.foundation.BorderStroke(1.dp, line)) {
                                Row(Modifier.fillMaxSize().padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(mapStyle, color = White, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.ExpandMore, contentDescription = "Change map style", tint = White)
                                }
                            }
                        }
                        Text("CARTO BASEMAP · ${mapStyle.uppercase()} · tap style to change", color = Color(0xFF7F9BAD), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    else -> {
                        Text("${selected} options", color = Color(0xFF9FB4C4), fontSize = 13.sp)
                        listOf("Enable $selected", "Use automatic settings", "Show notifications").forEachIndexed { i, label ->
                            var enabled by remember(selected) { mutableStateOf(i != 2) }
                            SettingsToggleRow(label, enabled, { enabled = it }, blue, line)
                        }
                        Spacer(Modifier.weight(1f))
                        Text("HAULWAY • Settings", color = Color(0xFF6F8798), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit, blue: Color, divider: Color) {
    Row(Modifier.fillMaxWidth().height(38.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = White, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Surface(Modifier.width(48.dp).height(25.dp).clickable { onChecked(!checked) }, shape = CircleShape, color = if (checked) blue else Color(0xFF53616D)) {
            Box(Modifier.fillMaxSize().padding(3.dp), contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart) {
                Surface(Modifier.size(19.dp), shape = CircleShape, color = White) {}
            }
        }
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(divider))
}

@Composable
private fun InfoTab(label: String, modifier: Modifier = Modifier, selected: Boolean) {
    Surface(
        modifier = modifier.height(34.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) Color(0xFF343A42) else Color(0xFF202329)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun HorizontalDividerDark() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF343840)))
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color(0xFF9BA2AD), fontSize = 13.sp)
        Text(value, color = White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun QuickAction(label: String, icon: ImageVector, modifier: Modifier = Modifier, active: Boolean = false) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(12.dp),
        color = if (active) Color(0xFF3298F6) else Color(0xFF25292E),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = White, modifier = Modifier.size(25.dp))
            Spacer(modifier = Modifier.height(5.dp))
            Text(label, color = White, fontSize = 10.sp, maxLines = 1)
        }
    }
}

@Composable
private fun MapControl(
    icon: ImageVector,
    description: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.size(50.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(15.dp),
        color = Color(0xE6212429),
        shadowElevation = 3.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = description, tint = accent, modifier = Modifier.size(25.dp))
        }
    }
}

@Composable
private fun MinimalMapBackground() {

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {

        // Neutral map-like background.
        // These are illustrative roads, not live map data.

        drawRect(
            color = MapBackground,
            size = size
        )

        val roadWidth = size.width * 0.065f

        // Main diagonal roads.
        drawLine(
            color = RoadColor,
            start = Offset(
                -size.width * 0.1f,
                size.height * 0.25f
            ),
            end = Offset(
                size.width * 0.9f,
                size.height * 1.05f
            ),
            strokeWidth = roadWidth,
            cap = StrokeCap.Round
        )

        drawLine(
            color = RoadColor,
            start = Offset(
                size.width * 0.2f,
                -size.height * 0.1f
            ),
            end = Offset(
                size.width * 0.75f,
                size.height * 1.1f
            ),
            strokeWidth = roadWidth * 0.75f,
            cap = StrokeCap.Round
        )

        // Horizontal streets.
        for (i in 1..5) {

            val y = size.height * (i / 6f)

            drawLine(
                color = RoadColor,
                start = Offset(0f, y),
                end = Offset(size.width, y + size.height * 0.08f),
                strokeWidth = roadWidth * 0.35f,
                cap = StrokeCap.Round
            )
        }

        // Vertical streets.
        for (i in 1..4) {

            val x = size.width * (i / 5f)

            drawLine(
                color = RoadColor,
                start = Offset(x, 0f),
                end = Offset(x - size.width * 0.08f, size.height),
                strokeWidth = roadWidth * 0.35f,
                cap = StrokeCap.Round
            )
        }
    }
}