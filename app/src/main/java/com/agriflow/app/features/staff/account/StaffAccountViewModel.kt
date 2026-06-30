package com.agriflow.app.features.staff.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriflow.app.core.security.TokenRepository
import com.agriflow.app.features.auth.UserDao
import com.agriflow.app.features.auth.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StaffAccountViewModel @Inject constructor(
    private val userDao: UserDao,
    private val tokenRepository: TokenRepository
) : ViewModel() {
    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user.asStateFlow()

    init {
        viewModelScope.launch {
            userDao.observeCurrentUser().collect {
                _user.value = it
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenRepository.clearTokens()
            userDao.clearUser()
        }
    }
}
