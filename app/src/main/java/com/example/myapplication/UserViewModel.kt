package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(private val userService: UserService) : ViewModel() {
    private val _uiState = MutableLiveData(UiState())
    val uiState: LiveData<UiState> get() = _uiState

    val users: LiveData<List<User>> = _uiState.map { it.users }

    init {
        loadUsers()
    }

    fun loadUsers() {
        _uiState.value = _uiState.value?.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val usersList = withContext(Dispatchers.IO) {
                    userService.getUsers()
                }
                _uiState.value = UiState(users = usersList)
            } catch (e: Exception) {
                _uiState.value = UiState(error = "Błąd: ${e.message}")
            }
        }
    }

    fun deleteUser(user: User) {
        val currentUsers = _uiState.value?.users ?: return
        _uiState.value = _uiState.value?.copy(users = currentUsers - user)
    }
}
