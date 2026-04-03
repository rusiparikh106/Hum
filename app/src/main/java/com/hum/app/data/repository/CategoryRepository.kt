package com.hum.app.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.hum.app.data.model.Category
import com.hum.app.data.model.CategoryEntity
import com.hum.app.util.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection get() = firestore.collection(Constants.COLLECTION_CATEGORIES)

    fun observeCategories(): Flow<List<CategoryEntity>> = callbackFlow {
        val listener = collection
            .orderBy("sortOrder", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "observeCategories failed", error)
                    trySend(defaultCategories())
                    return@addSnapshotListener
                }
                val categories = snapshot?.toObjects(CategoryEntity::class.java) ?: emptyList()
                trySend(categories.ifEmpty { defaultCategories() })
            }
        awaitClose { listener.remove() }
    }

    suspend fun seedDefaultCategoriesIfEmpty() {
        try {
            val snapshot = collection.limit(1).get().await()
            if (!snapshot.isEmpty) return

            val batch = firestore.batch()
            Category.entries.forEachIndexed { index, cat ->
                val doc = collection.document(cat.name)
                val entity = CategoryEntity(
                    name = cat.name,
                    label = cat.label,
                    icon = cat.icon,
                    sortOrder = index
                )
                batch.set(doc, entity)
            }
            batch.commit().await()
        } catch (e: Exception) {
            Log.e(TAG, "seedDefaultCategoriesIfEmpty failed", e)
        }
    }

    private fun defaultCategories(): List<CategoryEntity> =
        Category.entries.mapIndexed { index, cat ->
            CategoryEntity(
                id = cat.name,
                name = cat.name,
                label = cat.label,
                icon = cat.icon,
                sortOrder = index
            )
        }

    suspend fun addCategory(name: String, label: String, icon: String): Result<CategoryEntity> = runCatching {
        val docName = name.uppercase().replace("\\s+".toRegex(), "_")
        val doc = collection.document(docName)
        val count = try { collection.get().await().size() } catch (_: Exception) { 100 }
        val entity = CategoryEntity(
            name = docName,
            label = name.trim(),
            icon = icon,
            sortOrder = count
        )
        doc.set(entity).await()
        entity.copy(id = docName)
    }

    suspend fun ensureCategoryExists(name: String, label: String, icon: String) {
        try {
            val doc = collection.document(name)
            val snapshot = doc.get().await()
            if (!snapshot.exists()) {
                val count = collection.get().await().size()
                val entity = CategoryEntity(
                    name = name,
                    label = label,
                    icon = icon,
                    sortOrder = count
                )
                doc.set(entity).await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "ensureCategoryExists failed", e)
        }
    }

    companion object {
        private const val TAG = "CategoryRepository"
    }
}
