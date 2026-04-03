package com.hum.app.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hum.app.data.model.Category
import com.hum.app.data.model.Expense
import com.hum.app.data.repository.AuthRepository
import com.hum.app.data.repository.ExpenseRepository
import com.hum.app.util.isThisMonth
import com.hum.app.util.monthLabel
import com.hum.app.util.monthYearKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MonthlyData(val label: String, val amount: Double)
data class CategoryData(val category: Category, val amount: Double, val percentage: Float)

data class InsightsUiState(
    val isLoading: Boolean = true,
    val monthlyTotals: List<MonthlyData> = emptyList(),
    val categoryBreakdown: List<CategoryData> = emptyList(),
    val topSpender: String = "",
    val topSpenderAmount: Double = 0.0,
    val recurringTotal: Double = 0.0,
    val oneTimeTotal: Double = 0.0
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        loadInsights()
    }

    private fun loadInsights() {
        viewModelScope.launch {
            val user = authRepository.observeCurrentUser().first() ?: return@launch
            val familyId = user.familyId ?: return@launch

            expenseRepository.observeMonthlyExpenses(familyId).collect { expenses ->
                val monthlyTotals = expenses
                    .groupBy { it.date.monthYearKey() }
                    .map { (key, items) ->
                        MonthlyData(
                            label = items.first().date.monthLabel(),
                            amount = items.sumOf { it.amount }
                        )
                    }
                    .sortedBy { it.label }

                val thisMonthExpenses = expenses.filter { it.date.isThisMonth() }
                val totalThisMonth = thisMonthExpenses.sumOf { it.amount }

                val categoryBreakdown = thisMonthExpenses
                    .groupBy { Category.fromName(it.category) }
                    .map { (cat, items) ->
                        val sum = items.sumOf { it.amount }
                        CategoryData(
                            category = cat,
                            amount = sum,
                            percentage = if (totalThisMonth > 0) (sum / totalThisMonth * 100).toFloat() else 0f
                        )
                    }
                    .sortedByDescending { it.amount }

                val topSpenderEntry = thisMonthExpenses
                    .groupBy { it.paidByName }
                    .maxByOrNull { it.value.sumOf { e -> e.amount } }

                val recurringTotal = thisMonthExpenses.filter { it.isRecurring }.sumOf { it.amount }
                val oneTimeTotal = thisMonthExpenses.filter { !it.isRecurring }.sumOf { it.amount }

                _uiState.value = InsightsUiState(
                    isLoading = false,
                    monthlyTotals = monthlyTotals,
                    categoryBreakdown = categoryBreakdown,
                    topSpender = topSpenderEntry?.key.orEmpty(),
                    topSpenderAmount = topSpenderEntry?.value?.sumOf { it.amount } ?: 0.0,
                    recurringTotal = recurringTotal,
                    oneTimeTotal = oneTimeTotal
                )
            }
        }
    }
}
