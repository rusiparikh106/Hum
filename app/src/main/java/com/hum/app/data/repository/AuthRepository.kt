package com.hum.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.hum.app.data.model.User
import com.hum.app.util.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    val isSignedIn: Boolean get() = currentUser != null

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val firebaseUser = result.user ?: throw Exception("Sign-in failed")

        val userDoc = firestore.collection(Constants.COLLECTION_USERS)
            .document(firebaseUser.uid)
            .get()
            .await()

        if (!userDoc.exists()) {
            val newUser = User(
                displayName = firebaseUser.displayName.orEmpty(),
                email = firebaseUser.email.orEmpty(),
                photoUrl = firebaseUser.photoUrl?.toString().orEmpty()
            )
            firestore.collection(Constants.COLLECTION_USERS)
                .document(firebaseUser.uid)
                .set(newUser)
                .await()
        }

        firebaseUser
    }

    fun observeCurrentUser(): Flow<User?> = callbackFlow {
        val uid = currentUser?.uid ?: run {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = firestore.collection(Constants.COLLECTION_USERS)
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(User::class.java))
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateFamilyId(familyId: String) {
        val uid = currentUser?.uid ?: return
        firestore.collection(Constants.COLLECTION_USERS)
            .document(uid)
            .update("familyId", familyId)
            .await()
    }

    fun signOut() {
        auth.signOut()
    }
}
