package com.hum.app.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.hum.app.data.model.Expense
import com.hum.app.util.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun addExpense(expense: Expense): Result<String> = runCatching {
        val docRef = firestore.collection(Constants.COLLECTION_EXPENSES).document()
        val expenseWithId = expense.copy(id = docRef.id)
        docRef.set(expenseWithId).await()
        docRef.id
    }

    suspend fun updateExpense(expense: Expense): Result<Unit> = runCatching {
        firestore.collection(Constants.COLLECTION_EXPENSES)
            .document(expense.id)
            .set(expense)
            .await()
    }

    suspend fun deleteExpense(expenseId: String): Result<Unit> = runCatching {
        firestore.collection(Constants.COLLECTION_EXPENSES)
            .document(expenseId)
            .delete()
            .await()
    }

    fun observeExpenses(familyId: String): Flow<List<Expense>> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_EXPENSES)
            .whereEqualTo("familyId", familyId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "observeExpenses failed", error)
                    return@addSnapshotListener
                }
                val expenses = snapshot?.toObjects(Expense::class.java) ?: emptyList()
                trySend(expenses)
            }
        awaitClose { listener.remove() }
    }

    fun observeRecurringExpenses(familyId: String): Flow<List<Expense>> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_EXPENSES)
            .whereEqualTo("familyId", familyId)
            .whereEqualTo("isRecurring", true)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "observeRecurringExpenses failed", error)
                    return@addSnapshotListener
                }
                val expenses = snapshot?.toObjects(Expense::class.java) ?: emptyList()
                trySend(expenses)
            }
        awaitClose { listener.remove() }
    }

    fun observeMonthlyExpenses(familyId: String, monthsBack: Int = 6): Flow<List<Expense>> = callbackFlow {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.MONTH, -monthsBack)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val startTimestamp = Timestamp(calendar.time)

        val listener = firestore.collection(Constants.COLLECTION_EXPENSES)
            .whereEqualTo("familyId", familyId)
            .whereGreaterThanOrEqualTo("date", startTimestamp)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "observeMonthlyExpenses failed", error)
                    return@addSnapshotListener
                }
                val expenses = snapshot?.toObjects(Expense::class.java) ?: emptyList()
                trySend(expenses)
            }
        awaitClose { listener.remove() }
    }

    companion object {
        private const val TAG = "ExpenseRepository"
    }
}
