package com.example.eventify.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun registerOrganizer(
        name: String,
        email: String,
        password: String,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { authResult ->
                    if (authResult.isSuccessful) {
                        val user = authResult.result?.user
                        if (user != null) {
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build()
                            user.updateProfile(profileUpdates)
                                .addOnCompleteListener { profileResult ->
                                    if (profileResult.isSuccessful) {
                                        val userData = hashMapOf(
                                            "name" to name,
                                            "email" to email,
                                            "role" to "organizer"
                                        )
                                        db.collection("users").document(user.uid)
                                            .set(userData)
                                            .addOnSuccessListener { onSuccess(user) }
                                            .addOnFailureListener { e ->
                                                onError("Erro ao criar documento Firestore: ${e.localizedMessage}")
                                            }
                                    } else {
                                        onError("Erro ao atualizar perfil do usuário")
                                    }
                                }
                        } else {
                            onError("Usuário auth result é nulo")
                        }
                    } else {
                        onError(authResult.exception?.localizedMessage ?: "Erro ao criar usuário")
                    }
                }
        }
    }
}
