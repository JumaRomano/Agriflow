/**
 * Jetpack Compose UI screen components for the AddProduct screen.
 */
package com.agriflow.app.features.addproduct

import android.net.Uri
import com.agriflow.app.features.marketplace.CategoryDto
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage

/**
 * Route wrapper that collects events and renders state for the Add Product flow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductRoute(
    onNavigateBack: () -> Unit,
    viewModel: AddProductViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                AddProductEvent.MapsBack -> onNavigateBack()
                is AddProductEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    AddProductScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack
    )
}

/**
 * Main dashboard screen to register products. Designed with material color spaces and dark mode capabilities.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    state: AddProductState,
    snackbarHostState: SnackbarHostState,
    onAction: (AddProductAction) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Remember initial values on first composition to detect unsaved changes
    val initialName = remember { state.name }
    val initialDescription = remember { state.description }
    val initialPrice = remember { state.price }
    val initialQuantity = remember { state.quantity }
    val initialCategoryId = remember { state.selectedCategoryId }
    val initialUnit = remember { state.selectedUnit }
    val initialImages = remember { state.selectedImageUris }

    val hasUnsavedChanges = if (state.productId == null) {
        state.name.isNotBlank() || 
        state.description.isNotBlank() || 
        state.price.isNotBlank() || 
        state.quantity.isNotBlank() || 
        state.selectedCategoryId != null || 
        state.selectedImageUris.isNotEmpty()
    } else {
        state.name != initialName ||
        state.description != initialDescription ||
        state.price != initialPrice ||
        state.quantity != initialQuantity ||
        state.selectedCategoryId != initialCategoryId ||
        state.selectedUnit != initialUnit ||
        state.selectedImageUris != initialImages
    }

    var showDiscardDialog by remember { mutableStateOf(false) }

    // Intercept system back button
    BackHandler(enabled = !state.isLoading) {
        if (hasUnsavedChanges) {
            showDiscardDialog = true
        } else {
            onNavigateBack()
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard unsaved changes?") },
            text = { Text("You have unsaved changes. If you go back, these changes will be lost.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDiscardDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Media photo picker launcher for up to 5 elements
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                onAction(AddProductAction.OnImagesSelected(uris))
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.productId != null) "Edit Product" else "Add Product", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (hasUnsavedChanges) {
                                showDiscardDialog = true
                            } else {
                                onNavigateBack()
                            }
                        },
                        enabled = !state.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 1. IMAGE CAPTURE SECTION (Extracted to private helper) ---
            ImageSection(
                selectedImageUris = state.selectedImageUris,
                imagesError = state.imagesError,
                isLoading = state.isLoading,
                onPickImagesClick = {
                    multiplePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onRemoveImage = { uri ->
                    onAction(AddProductAction.OnRemoveImage(uri))
                }
            )

            // --- 2. PRODUCT NAME ---
            OutlinedTextField(
                value = state.name,
                onValueChange = { onAction(AddProductAction.OnNameChanged(it)) },
                label = { Text("Product Name") },
                placeholder = { Text("e.g. Premium White Maize") },
                singleLine = true,
                enabled = !state.isLoading,
                isError = state.nameError != null,
                supportingText = state.nameError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            // --- 3. DESCRIPTION ---
            OutlinedTextField(
                value = state.description,
                onValueChange = { onAction(AddProductAction.OnDescriptionChanged(it)) },
                label = { Text("Description") },
                placeholder = { Text("Enter product details)") },
                minLines = 3,
                enabled = !state.isLoading,
                isError = state.descriptionError != null,
                supportingText = state.descriptionError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            // --- 4. PRICE & QUANTITY ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = state.price,
                    onValueChange = { onAction(AddProductAction.OnPriceChanged(it)) },
                    label = { Text("Price (KES)") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    enabled = !state.isLoading,
                    isError = state.priceError != null,
                    supportingText = state.priceError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = state.quantity,
                    onValueChange = { onAction(AddProductAction.OnQuantityChanged(it)) },
                    label = { Text("Available Qty") },
                    placeholder = { Text("e.g. 100") },
                    singleLine = true,
                    enabled = !state.isLoading,
                    isError = state.quantityError != null,
                    supportingText = state.quantityError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            // --- 5. CATEGORY DROPDOWN (Extracted to private helper) ---
            CategoryDropdown(
                selectedCategoryId = state.selectedCategoryId,
                categories = state.categories,
                categoryError = state.categoryError,
                isLoading = state.isLoading || state.isFetchingCategories,
                onCategorySelected = { id ->
                    onAction(AddProductAction.OnCategorySelected(id))
                }
            )

            // --- 6. UNIT DROPDOWN (Extracted to private helper) ---
            UnitDropdown(
                selectedUnit = state.selectedUnit,
                units = state.units,
                unitError = state.unitError,
                isLoading = state.isLoading,
                onUnitSelected = { unit ->
                    onAction(AddProductAction.OnUnitSelected(unit))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- 7. SUBMIT BUTTON ---
            Button(
                onClick = { onAction(AddProductAction.OnSubmitClicked) },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = if (state.productId != null) "Update Product" else "Submit Product",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Image upload trigger box and horizontal LazyRow list view displaying thumbnails.
 */
@Composable
private fun ImageSection(
    selectedImageUris: List<Uri>,
    imagesError: String?,
    isLoading: Boolean,
    onPickImagesClick: () -> Unit,
    onRemoveImage: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Picker Card
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(enabled = !isLoading && selectedImageUris.size < 5) { onPickImagesClick() },
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Add photo icon",
                        tint = if (imagesError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Add Photos (Max 3)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (imagesError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${selectedImageUris.size} / 3 selected",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // LazyRow of photo previews
        if (selectedImageUris.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(selectedImageUris) { uri ->
                    Box(
                        modifier = Modifier.size(80.dp)
                    ) {
                        // Thumbnail Preview
                        AsyncImage(
                            model = uri,
                            contentDescription = "Selected photo thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                        )

                        // Top-Right Close Button Overlay
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.TopEnd)
                                .clickable(enabled = !isLoading) { onRemoveImage(uri) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove photo item",
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }
                }
            }
        }

        imagesError?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * Dropdown Menu exposing product categories.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    selectedCategoryId: String?,
    categories: List<CategoryDto>,
    categoryError: String?,
    isLoading: Boolean,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCategoryName = categories.find { it.id == selectedCategoryId }?.name ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (!isLoading) expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedCategoryName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            enabled = !isLoading,
            isError = categoryError != null,
            supportingText = categoryError?.let { { Text(it) } },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = !isLoading)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onCategorySelected(category.id)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

/**
 * Dropdown Menu exposing units of measurement.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitDropdown(
    selectedUnit: String,
    units: List<String>,
    unitError: String?,
    isLoading: Boolean,
    onUnitSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (!isLoading) expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedUnit,
            onValueChange = {},
            readOnly = true,
            label = { Text("Unit of Measurement") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            enabled = !isLoading,
            isError = unitError != null,
            supportingText = unitError?.let { { Text(it) } },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = !isLoading)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            units.forEach { unitOption ->
                DropdownMenuItem(
                    text = { Text(unitOption) },
                    onClick = {
                        onUnitSelected(unitOption)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
