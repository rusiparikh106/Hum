package com.hum.app.ui.addexpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.hum.app.data.model.Category
import com.hum.app.data.model.Expense
import com.hum.app.data.model.RecurringType
import com.hum.app.data.model.User
import com.hum.app.data.repository.AuthRepository
import com.hum.app.data.repository.ExpenseRepository
import com.hum.app.data.repository.FamilyRepository
import com.hum.app.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class AddExpenseUiState(
    val amount: String = "",
    val title: String = "",
    val category: Category = Category.OTHER,
    val date: Date = Date(),
    val paidByUserId: String = "",
    val paidByUserName: String = "",
    val familyMembers: List<User> = emptyList(),
    val isRecurring: Boolean = false,
    val recurringType: RecurringType = RecurringType.MONTHLY,
    val recurringDay: Int = 1,
    val notes: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val authRepository: AuthRepository,
    private val familyRepository: FamilyRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState(
        paidByUserId = auth.currentUser?.uid.orEmpty(),
        paidByUserName = auth.currentUser?.displayName.orEmpty()
    ))
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    init {
        loadFamilyMembers()
    }

    private fun loadFamilyMembers() {
        viewModelScope.launch {
            authRepository.observeCurrentUser()
                .filterNotNull()
                .flatMapLatest { user ->
                    val familyId = user.familyId ?: return@flatMapLatest flowOf(emptyList())
                    familyRepository.observeFamily(familyId)
                        .filterNotNull()
                        .flatMapLatest { family ->
                            familyRepository.observeFamilyMembers(family.memberIds)
                        }
                }
                .collect { members ->
                    val currentUid = auth.currentUser?.uid.orEmpty()
                    val sorted = members.sortedByDescending { it.id == currentUid }
                    _uiState.value = _uiState.value.copy(familyMembers = sorted)
                }
        }
    }

    fun updateAmount(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        _uiState.value = _uiState.value.copy(amount = filtered)
    }

    fun updateTitle(value: String) {
        _uiState.value = _uiState.value.copy(title = value)
    }

    fun updateCategory(category: Category) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    fun updateDate(date: Date) {
        _uiState.value = _uiState.value.copy(date = date)
    }

    fun updatePaidBy(user: User) {
        _uiState.value = _uiState.value.copy(
            paidByUserId = user.id,
            paidByUserName = user.displayName
        )
    }

    fun toggleRecurring() {
        _uiState.value = _uiState.value.copy(isRecurring = !_uiState.value.isRecurring)
    }

    fun updateRecurringType(type: RecurringType) {
        _uiState.value = _uiState.value.copy(recurringType = type)
    }

    fun updateRecurringDay(day: Int) {
        _uiState.value = _uiState.value.copy(recurringDay = day.coerceIn(1, 31))
    }

    fun updateNotes(value: String) {
        _uiState.value = _uiState.value.copy(notes = value)
    }

    fun saveExpense() {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.value = state.copy(error = "Enter a valid amount")
            return
        }
        if (state.title.isBlank()) {
            _uiState.value = state.copy(error = "Enter a title")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, error = null)

            val user = authRepository.observeCurrentUser().first()
            if (user?.familyId == null) {
                _uiState.value = state.copy(isSaving = false, error = "No family found")
                return@launch
            }

            val expense = Expense(
                familyId = user.familyId,
                title = state.title.trim(),
                amount = amount,
                currency = Constants.DEFAULT_CURRENCY,
                category = state.category.name,
                paidBy = state.paidByUserId,
                paidByName = state.paidByUserName,
                isRecurring = state.isRecurring,
                recurringType = if (state.isRecurring) state.recurringType.name else null,
                recurringDay = if (state.isRecurring) state.recurringDay else null,
                notes = state.notes.ifBlank { null },
                date = Timestamp(state.date)
            )

            expenseRepository.addExpense(expense)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = e.message ?: "Failed to save"
                    )
                }
        }
    }

    fun resetForm() {
        _uiState.value = _uiState.value.copy(
            amount = "",
            title = "",
            category = Category.OTHER,
            date = Date(),
            paidByUserId = auth.currentUser?.uid.orEmpty(),
            paidByUserName = auth.currentUser?.displayName.orEmpty(),
            isRecurring = false,
            recurringType = RecurringType.MONTHLY,
            recurringDay = 1,
            notes = "",
            isSaving = false,
            isSaved = false,
            error = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
