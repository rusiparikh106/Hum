package com.hum.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hum.app.data.model.Expense
import com.hum.app.data.model.Family
import com.hum.app.data.model.User
import com.hum.app.data.repository.AuthRepository
import com.hum.app.data.repository.ExpenseRepository
import com.hum.app.data.repository.FamilyRepository
import com.hum.app.util.isThisMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val family: Family? = null,
    val expenses: List<Expense> = emptyList(),
    val monthlyTotal: Double = 0.0,
    val myMonthlyTotal: Double = 0.0
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val familyRepository: FamilyRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val userFlow = authRepository.observeCurrentUser()
            .filterNotNull()
            .map { user ->
                _uiState.value = _uiState.value.copy(user = user)
                user
            }

        viewModelScope.launch {
            userFlow
                .flatMapLatest { user ->
                    val familyId = user.familyId
                    if (familyId == null) {
                        flowOf(Triple(user, null as Family?, emptyList<Expense>()))
                    } else {
                        combine(
                            familyRepository.observeFamily(familyId),
                            expenseRepository.observeExpenses(familyId)
                        ) { family, expenses ->
                            Triple(user, family, expenses)
                        }
                    }
                }
                .collect { (user, family, expenses) ->
                    val monthExpenses = expenses.filter { it.date.isThisMonth() }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = user,
                        family = family,
                        expenses = expenses,
                        monthlyTotal = monthExpenses.sumOf { it.amount },
                        myMonthlyTotal = monthExpenses
                            .filter { it.paidBy == user.id }
                            .sumOf { it.amount }
                    )
                }
        }
    }

    fun signOut() {
        authRepository.signOut()
    }
}
