package com.tm470.cookhub.launcher

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tm470.cookhub.CurrentUser
import com.tm470.cookhub.MainActivity
import com.tm470.cookhub.R
import com.tm470.cookhub.models.User
import kotlinx.android.synthetic.main.fragment_register.*

@SuppressLint("RestrictedApi")
class RegisterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        buttonSubmitRegister.setOnClickListener {
            registerUser()
        }
    }

    private fun validateFields(): Boolean {
        val username = editTextUsernameRegister.text.toString()
        val email = editTextEmailRegister.text.toString()
        val emailConfirm = editTextEmailConfirmRegister.text.toString()
        val password = editTextPasswordRegister.text.toString()
        val passwordConfirm = editTextPasswordConfirmRegister.text.toString()

        val userList: MutableList<User> = mutableListOf()
        val usersRef = FirebaseDatabase.getInstance().getReference("/users")
        usersRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val user: User? = it.getValue(User::class.java)
                    userList.add(user!!)
                }
            }
        })

        userList.forEach {
            if (it.email == email) {
                Toast.makeText(this.context, "Email already in use", Toast.LENGTH_LONG).show()
                return false
            }
            if (it.username == username) {
                Toast.makeText(this.context, "Username already in use", Toast.LENGTH_LONG).show()
                return false
            }
        }

        if (email != emailConfirm) {
            Toast.makeText(this.context, "Email does not match", Toast.LENGTH_LONG).show()
            return false
        }

        if (password != passwordConfirm) {
            Toast.makeText(this.context, "Password does not match", Toast.LENGTH_LONG).show()
            return false
        }

        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Toast.makeText(this.context, "Please enter a username, email, and password", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    private fun registerUser() {
        val username = editTextUsernameRegister.text.toString()
        val email = editTextEmailRegister.text.toString()
        val password = editTextPasswordRegister.text.toString()
        if (validateFields()) {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                email,
                password
            )
                .addOnSuccessListener {
                    Log.d("Main", "User created with ID: ${it.user!!.uid}")
                    val uid = FirebaseAuth.getInstance().uid
                    val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
                    val user = User(
                        uid,
                        username,
                        email
                    )
                    CurrentUser.user = user
                    ref.setValue(user)
                        .addOnSuccessListener {
                            val intent = Intent(this.context, MainActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this.context,
                        "Invalid parameters: ${it.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
}