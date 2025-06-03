package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val filteredUsers: StateFlow<List<User>> = combine(_users, _searchQuery) { users, query ->
        if (query.isBlank()) users else users.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.email.contains(query, ignoreCase = true)
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
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun loadUsers() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch(dispatcher) {
            val result = runCatching {
                userService.getUsers()
            }

            result.fold(
                onSuccess = { users ->
                    _users.value = users
                    _uiState.update { it.copy(isLoading = false, users = users) }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = NetworkError.fromException(exception)
                        )
                    }
                }
            )
        }
    }

    fun deleteUser(user: User) {
        val updatedUsers = _users.value.toMutableList().apply { remove(user) }
        _users.value = updatedUsers
        _uiState.update { it.copy(users = updatedUsers) }
    }

    fun retry() {
        loadUsers()
    }
}

sealed class NetworkError(val message: String) {
    object NoInternet : NetworkError("Brak połączenia z internetem")
    data class ServerError(val code: Int) : NetworkError("Błąd serwera: $code")
    data class GenericError(val errorMessage: String) : NetworkError(errorMessage)

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
