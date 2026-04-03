package com.hum.app.ui.familysetup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.hum.app.data.model.Family
import com.hum.app.data.repository.FamilyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FamilySetupUiState(
    val isLoading: Boolean = false,
    val family: Family? = null,
    val error: String? = null
)

@HiltViewModel
class FamilySetupViewModel @Inject constructor(
    private val familyRepository: FamilyRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(FamilySetupUiState())
    val uiState: StateFlow<FamilySetupUiState> = _uiState.asStateFlow()

    fun createFamily(name: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            familyRepository.createFamily(name, uid)
                .onSuccess { family ->
                    _uiState.value = _uiState.value.copy(isLoading = false, family = family)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to create family"
                    )
                }
        }
    }

    fun joinFamily(inviteCode: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            familyRepository.joinFamily(inviteCode, uid)
                .onSuccess { family ->
                    _uiState.value = _uiState.value.copy(isLoading = false, family = family)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to join family"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
