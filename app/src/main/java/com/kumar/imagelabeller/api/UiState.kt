package com.kumar.imagelabeller.api

data class UiState(
    val isLoading: Boolean = false,
    val result: Map<String, Any>? = null,
    val error: String? = null
)