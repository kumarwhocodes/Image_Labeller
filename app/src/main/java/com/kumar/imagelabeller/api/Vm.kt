package com.kumar.imagelabeller.api

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class Vm @Inject constructor(
    private val geminiRepository: Repo
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun processImage(imageFile: File) {
        viewModelScope.launch {
            _uiState.value = UiState(isLoading = true)
            try {
                val result = geminiRepository.processImage(imageFile)
                _uiState.value = UiState(result = result)
            } catch (e: Exception) {
                _uiState.value = UiState(error = e.message ?: "Unknown error")
            }
        }
    }
}