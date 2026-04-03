package com.hum.app.data.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.hum.app.data.model.Family
import com.hum.app.data.model.User
import com.hum.app.util.Constants
import com.hum.app.util.generateInviteCode
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamilyRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun createFamily(name: String, userId: String): Result<Family> = runCatching {
        val inviteCode = generateInviteCode()
        val family = Family(
            name = name,
            createdBy = userId,
            inviteCode = inviteCode,
            memberIds = listOf(userId)
        )
        val docRef = firestore.collection(Constants.COLLECTION_FAMILIES).document()
        val familyWithId = family.copy(id = docRef.id)

        docRef.set(familyWithId).await()

        firestore.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .update("familyId", docRef.id)
            .await()

        familyWithId
    }

    suspend fun joinFamily(inviteCode: String, userId: String): Result<Family> = runCatching {
        val snapshot = firestore.collection(Constants.COLLECTION_FAMILIES)
            .whereEqualTo("inviteCode", inviteCode.uppercase())
            .get()
            .await()

        if (snapshot.isEmpty) throw Exception("No family found with this invite code")

        val familyDoc = snapshot.documents.first()
        val family = familyDoc.toObject(Family::class.java)
            ?: throw Exception("Failed to parse family data")

        if (userId in family.memberIds) throw Exception("You are already in this family")

        familyDoc.reference.update("memberIds", FieldValue.arrayUnion(userId)).await()

        firestore.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .update("familyId", familyDoc.id)
            .await()

        family.copy(memberIds = family.memberIds + userId)
    }

    fun observeFamily(familyId: String): Flow<Family?> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_FAMILIES)
            .document(familyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "observeFamily failed", error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(Family::class.java))
            }
        awaitClose { listener.remove() }
    }

    fun observeFamilyMembers(memberIds: List<String>): Flow<List<User>> = callbackFlow {
        if (memberIds.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection(Constants.COLLECTION_USERS)
            .whereIn("__name__", memberIds)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "observeFamilyMembers failed", error)
                    return@addSnapshotListener
                }
                val users = snapshot?.toObjects(User::class.java) ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    companion object {
        private const val TAG = "FamilyRepository"
    }

    suspend fun leaveFamily(familyId: String, userId: String) {
        firestore.collection(Constants.COLLECTION_FAMILIES)
            .document(familyId)
            .update("memberIds", FieldValue.arrayRemove(userId))
            .await()

        firestore.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .update("familyId", null)
            .await()
    }
}
