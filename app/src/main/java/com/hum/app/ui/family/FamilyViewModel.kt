package com.hum.app.ui.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hum.app.data.model.Family
import com.hum.app.data.model.User
import com.hum.app.data.repository.AuthRepository
import com.hum.app.data.repository.FamilyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FamilyUiState(
    val isLoading: Boolean = true,
    val family: Family? = null,
    val members: List<User> = emptyList()
)

@HiltViewModel
class FamilyViewModel @Inject constructor(
    private val familyRepository: FamilyRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FamilyUiState())
    val uiState: StateFlow<FamilyUiState> = _uiState.asStateFlow()

    init {
        loadFamily()
    }

    private fun loadFamily() {
        viewModelScope.launch {
            authRepository.observeCurrentUser().collect { user ->
                val familyId = user?.familyId ?: return@collect
                launch {
                    familyRepository.observeFamily(familyId).collect { family ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            family = family
                        )
                        family?.memberIds?.let { ids ->
                            launch {
                                familyRepository.observeFamilyMembers(ids).collect { members ->
                                    _uiState.value = _uiState.value.copy(members = members)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun leaveFamily() {
        viewModelScope.launch {
            val uid = authRepository.currentUser?.uid ?: return@launch
            val familyId = _uiState.value.family?.id ?: return@launch
            familyRepository.leaveFamily(familyId, uid)
        }
    }
}
