package com.example.myapplication

data class UiState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val searchQuery: String = "",
    val error: NetworkError? = null
)
