package com.example.myapplication

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userService: UserService,
    @Dispatcher(CoroutinesDispatcherType.IO) private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val filteredUsers: StateFlow<List<User>> = _uiState.map { state ->
        if (state.searchQuery.isBlank()) {
            state.users
        } else {
            state.users.filter { user ->
                user.name.contains(state.searchQuery, ignoreCase = true) ||
                        user.email.contains(state.searchQuery, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    init {
        loadUsers()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { currentState ->
            currentState.copy(searchQuery = query)
        }
    }

    fun loadUsers() {
        _uiState.update { currentState ->
            currentState.copy(isLoading = true, error = null)
        }

        viewModelScope.launch(dispatcher) {
            val result = runCatching {
                userService.getUsers()
            }

            result.fold(
                onSuccess = { users ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            users = users,
                            error = null
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            error = NetworkError.fromException(exception)
                        )
                    }
                }
            )
        }
    }

    fun deleteUser(user: User) {
        _uiState.update { currentState ->
            val updatedUsers = currentState.users.toMutableList().apply {
                remove(user)
            }
            currentState.copy(users = updatedUsers)
        }
    }

    fun retry() {
        loadUsers()
    }

    fun clearError() {
        _uiState.update { currentState ->
            currentState.copy(error = null)
        }
    }
}

sealed class NetworkError(val message: String) {
    object NoInternet : NetworkError("Brak połączenia z internetem")
    data class ServerError(val code: Int) : NetworkError("Błąd serwera: $code")
    data class GenericError(val errorMessage: String) : NetworkError(errorMessage)

    @Keep
    companion object {
        fun fromException(e: Throwable): NetworkError {
            return when (e) {
                is IOException -> NoInternet
                is HttpException -> ServerError(e.code())
                else -> GenericError(e.message ?: "Nieznany błąd")
            }
        }
    }
}
