package com.agriflow.app.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriflow.app.core.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationsRepository: NotificationsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsState())
    val state = _state.asStateFlow()

    init {
        observeNotifications()
        loadNotifications()
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            notificationsRepository.observeNotifications().collect { entities ->
                val items = entities.map { entity ->
                    NotificationItem(
                        id = entity.notificationId,
                        title = entity.title,
                        description = entity.body,
                        timestamp = formatTimestamp(entity.timestamp),
                        isRead = entity.isRead
                    )
                }
                _state.update { it.copy(notifications = items) }
            }
        }
    }

    fun onAction(action: NotificationsAction) {
        when (action) {
            NotificationsAction.RefreshNotifications -> {
                loadNotifications()
            }
            NotificationsAction.ClearAllNotifications -> {
                viewModelScope.launch {
                    notificationsRepository.clearAllNotifications()
                }
            }
            is NotificationsAction.MarkAsRead -> {
                viewModelScope.launch {
                    notificationsRepository.markAsRead(action.notificationId)
                }
            }
        }
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = notificationsRepository.getMyNotifications()) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to load notifications: ${result.error.name}"
                        )
                    }
                }
            }
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            sdf.format(java.util.Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }
}
