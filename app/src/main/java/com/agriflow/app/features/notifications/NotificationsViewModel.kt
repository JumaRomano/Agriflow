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
        loadNotifications()
    }

    fun onAction(action: NotificationsAction) {
        when (action) {
            NotificationsAction.RefreshNotifications -> {
                loadNotifications()
            }
            NotificationsAction.ClearAllNotifications -> {
                _state.update { it.copy(notifications = emptyList()) }
            }
            is NotificationsAction.MarkAsRead -> {
                _state.update { currentState ->
                    currentState.copy(
                        notifications = currentState.notifications.map { item ->
                            if (item.id == action.notificationId) item.copy(isRead = true) else item
                        }
                    )
                }
            }
        }
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = notificationsRepository.getMyNotifications()) {
                is Result.Success -> {
                    val items = result.data.map { dto ->
                        NotificationItem(
                            id = dto.id.orEmpty(),
                            title = dto.title ?: "Notification",
                            description = dto.body ?: "",
                            timestamp = dto.createdAt ?: "",
                            isRead = dto.read ?: false
                        )
                    }
                    _state.update { it.copy(isLoading = false, notifications = items) }
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
}
