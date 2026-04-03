package com.hum.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Expense(
    @DocumentId val id: String = "",
    val familyId: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val currency: String = "INR",
    val category: String = Category.OTHER.name,
    val paidBy: String = "",
    val paidByName: String = "",
    val isRecurring: Boolean = false,
    val recurringType: String? = null,
    val recurringDay: Int? = null,
    val notes: String? = null,
    val date: Timestamp = Timestamp.now(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

enum class Category(val label: String, val icon: String) {
    FOOD("Food", "🍕"),
    TRANSPORT("Transport", "🚗"),
    UTILITIES("Utilities", "💡"),
    RENT("Rent", "🏠"),
    MEDICAL("Medical", "🏥"),
    SHOPPING("Shopping", "🛒"),
    ENTERTAINMENT("Entertainment", "🎬"),
    OTHER("Other", "📦");

    companion object {
        fun fromName(name: String): Category =
            entries.find { it.name == name } ?: OTHER
    }
}

enum class RecurringType(val label: String) {
    MONTHLY("Monthly"),
    QUARTERLY("Quarterly"),
    YEARLY("Yearly")
}
