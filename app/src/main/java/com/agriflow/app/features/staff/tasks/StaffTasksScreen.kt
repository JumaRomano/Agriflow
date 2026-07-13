package com.agriflow.app.features.staff.tasks

import android.net.Uri
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.agriflow.app.features.staff.auth.AssignmentsDto
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffTasksScreen(
    viewModel: StaffTasksViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val uiState = state
    val uploadedPhotos by viewModel.uploadedPhotos.collectAsState()
    val isUploadingPhoto by viewModel.isUploadingPhoto.collectAsState()
    
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Pending", "Assigned", "Completed")

    var activeVerificationTask by remember { mutableStateOf<AssignmentsDto?>(null) }

    LaunchedEffect(selectedTabIndex) {
        when (selectedTabIndex) {
            0 -> viewModel.fetchPendingAssignments()
            1 -> viewModel.fetchAllAssignments()
            2 -> viewModel.fetchCompletedAssignments()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = { Text("Tasks", fontWeight = FontWeight.Bold) },
            actions = {
                IconButton(
                    onClick = {
                        when (selectedTabIndex) {
                            0 -> viewModel.fetchPendingAssignments()
                            1 -> viewModel.fetchAllAssignments()
                            2 -> viewModel.fetchCompletedAssignments()
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        )
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(
                            onClick = {
                                when (selectedTabIndex) {
                                    0 -> viewModel.fetchPendingAssignments()
                                    1 -> viewModel.fetchAllAssignments()
                                    2 -> viewModel.fetchCompletedAssignments()
                                }
                            }
                        ) {
                            Text("Retry")
                        }
                    }
                }
                else -> {
                    val currentTasks = when (selectedTabIndex) {
                        0 -> uiState.pendingTasks
                        1 -> uiState.allTasks
                        else -> uiState.completedTasks
                    }

                    if (currentTasks.isEmpty()) {
                        EmptyPlaceholder(tabTitle = tabs[selectedTabIndex])
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(currentTasks) { task ->
                                TaskCard(
                                    task = task,
                                    onVerifyClick = { activeVerificationTask = it }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Verification Dialog
    activeVerificationTask?.let { task ->
        VerifyAssignmentDialog(
            assignmentId = task.id,
            isUploadingPhoto = isUploadingPhoto,
            uploadedPhotos = uploadedPhotos,
            onUploadPhoto = { uri -> viewModel.uploadPhoto(uri) },
            onRemovePhoto = { url -> viewModel.removeUploadedPhoto(url) },
            onDismiss = {
                viewModel.clearUploadedPhotos()
                activeVerificationTask = null
            },
            onSubmit = { lat, lon ->
                viewModel.verifyAssignment(
                    assignmentId = task.id,
                    latitude = lat,
                    longitude = lon,
                    onSuccess = {
                        activeVerificationTask = null
                    }
                )
            }
        )
    }
}

@Composable
private fun EmptyPlaceholder(tabTitle: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "No ${tabTitle.lowercase()} tasks found.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TaskCard(
    task: AssignmentsDto,
    onVerifyClick: (AssignmentsDto) -> Unit
) {
    val isCompleted = task.status.equals("completed", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = "Task Icon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Business ID: ${task.businessId.take(8)}...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Status: ${task.status.uppercase()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Assigned on ${formatDate(task.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!isCompleted) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onVerifyClick(task) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Verify",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verify Business", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun VerifyAssignmentDialog(
    assignmentId: String,
    isUploadingPhoto: Boolean,
    uploadedPhotos: List<String>,
    onUploadPhoto: (Uri) -> Unit,
    onRemovePhoto: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: (latitude: Double, longitude: Double) -> Unit
) {
    var latText by remember { mutableStateOf("") }
    var lonText by remember { mutableStateOf("") }

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var isFetchingLocation by remember { mutableStateOf(false) }

    val fetchLocation = @SuppressLint("MissingPermission") {
        isFetchingLocation = true
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            isFetchingLocation = false
            if (location != null) {
                latText = location.latitude.toString()
                lonText = location.longitude.toString()
            } else {
                Toast.makeText(context, "Failed to retrieve location. Please ensure GPS is enabled.", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener { e ->
            isFetchingLocation = false
            Toast.makeText(context, "Location error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                fetchLocation()
            } else {
                Toast.makeText(context, "Location permissions are required to get current location.", Toast.LENGTH_LONG).show()
            }
        }
    )

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onUploadPhoto(uri)
            }
        }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Verify Business",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Record coordinates and upload photo evidence of the business to complete verification.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = latText,
                        onValueChange = { latText = it },
                        label = { Text("Latitude") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = lonText,
                        onValueChange = { lonText = it },
                        label = { Text("Longitude") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Button(
                    onClick = {
                        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        if (hasFine || hasCoarse) {
                            fetchLocation()
                        } else {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    enabled = !isFetchingLocation,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isFetchingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Fetching Location...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Get  Your Current Location ")
                    }
                }

                Text(
                    text = "Farm Photos",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .clickable(enabled = !isUploadingPhoto) {
                            photoLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isUploadingPhoto) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Camera",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Upload Farm Photos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                if (uploadedPhotos.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uploadedPhotos) { url ->
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.LightGray)
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                IconButton(
                                    onClick = { onRemovePhoto(url) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        confirmButton = {
            val latVal = latText.toDoubleOrNull()
            val lonVal = lonText.toDoubleOrNull()
            val isEnabled = latVal != null && lonVal != null && uploadedPhotos.isNotEmpty()
            Button(
                onClick = {
                    if (latVal != null && lonVal != null) {
                        onSubmit(latVal, lonVal)
                    }
                },
                enabled = isEnabled
            ) {
                Text("Submit")
            }
        }
    )
}

private fun formatDate(dateStr: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(dateStr)
        val formatter = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
        if (date != null) formatter.format(date) else dateStr
    } catch (e: Exception) {
        try {
            val parser2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val date = parser2.parse(dateStr)
            val formatter = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
            if (date != null) formatter.format(date) else dateStr
        } catch (e2: Exception) {
            dateStr
        }
    }
}
