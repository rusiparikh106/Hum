package com.hum.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class CategoryEntity(
    @DocumentId val id: String = "",
    val name: String = "",
    val label: String = "",
    val icon: String = "",
    val sortOrder: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null
)
