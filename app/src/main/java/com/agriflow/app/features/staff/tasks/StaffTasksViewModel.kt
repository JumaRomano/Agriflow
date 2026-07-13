package com.agriflow.app.features.staff.tasks

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriflow.app.core.util.FileHelper
import com.agriflow.app.core.util.Result
import com.agriflow.app.features.auth.AuthRepository
import com.agriflow.app.features.staff.auth.StaffAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StaffTasksViewModel @Inject constructor(
    private val staffAuthRepository: StaffAuthRepository,
    private val authRepository: AuthRepository,
    private val fileHelper: FileHelper
) : ViewModel() {

    private val _state = MutableStateFlow(StaffTasksState())
    val state = _state.asStateFlow()

    private val _uploadedPhotos = MutableStateFlow<List<String>>(emptyList())
    val uploadedPhotos = _uploadedPhotos.asStateFlow()

    private val _isUploadingPhoto = MutableStateFlow(false)
    val isUploadingPhoto = _isUploadingPhoto.asStateFlow()

    init {
        fetchPendingAssignments()
    }

    fun fetchPendingAssignments() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = staffAuthRepository.getpendingAssignments()) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            pendingTasks = result.data
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to load pending tasks: ${result.error.name}"
                        )
                    }
                }
            }
        }
    }

    fun fetchAllAssignments() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = staffAuthRepository.getAllAssignments()) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            allTasks = result.data
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to load all tasks: ${result.error.name}"
                        )
                    }
                }
            }
        }
    }

    fun fetchCompletedAssignments() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = staffAuthRepository.getCompletedAssignments()) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            completedTasks = result.data
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to load completed tasks: ${result.error.name}"
                        )
                    }
                }
            }
        }
    }

    fun uploadPhoto(uri: Uri) {
        viewModelScope.launch {
            _isUploadingPhoto.value = true
            val tempFile = fileHelper.uriToFile(uri)
            if (tempFile == null) {
                _isUploadingPhoto.value = false
                _state.update { it.copy(errorMessage = "Failed to process selected image file.") }
                return@launch
            }

            val part = fileHelper.fileToMultipartPart(tempFile)
            val uploadResult = authRepository.uploadProfileImage(part)

            tempFile.delete()
            _isUploadingPhoto.value = false

            when (uploadResult) {
                is Result.Success -> {
                    val url = uploadResult.data.url ?: uploadResult.data.imageUrl
                    if (!url.isNullOrBlank()) {
                        _uploadedPhotos.update { it + url }
                    } else {
                        _state.update { it.copy(errorMessage = "Failed to upload photo: Empty URL returned.") }
                    }
                }
                is Result.Error -> {
                    _state.update { it.copy(errorMessage = "Failed to upload photo: ${uploadResult.error.name}") }
                }
            }
        }
    }

    fun clearUploadedPhotos() {
        _uploadedPhotos.value = emptyList()
    }

    fun removeUploadedPhoto(url: String) {
        _uploadedPhotos.update { it - url }
    }

    fun verifyAssignment(
        assignmentId: String,
        latitude: Double,
        longitude: Double,
        onSuccess: () -> Unit
    ) {
        val photos = _uploadedPhotos.value
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = staffAuthRepository.verifyAssignment(
                assignmentId = assignmentId,
                evidencePhotos = photos,
                latitude = latitude,
                longitude = longitude
            )
            _state.update { it.copy(isLoading = false) }
            when (result) {
                is Result.Success -> {
                    onSuccess()
                    clearUploadedPhotos()
                    fetchPendingAssignments()
                    fetchAllAssignments()
                    fetchCompletedAssignments()
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            errorMessage = "Verification failed: ${result.error.name}"
                        )
                    }
                }
            }
        }
    }
}
