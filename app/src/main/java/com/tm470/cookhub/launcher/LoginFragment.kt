package com.tm470.cookhub.launcher

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.tm470.cookhub.LandingActivity
import com.tm470.cookhub.R
import com.tm470.cookhub.hideFragment
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        hideFragment(requireActivity(), this)
        buttonSubmitLogin.setOnClickListener {
            val email = editTextEmailLogin.text.toString()
            val password = editTextPasswordLogin.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this.context, "Please enter an email and password", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            Log.d("Login", "Attempt login with email/pw: $email, $password")

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val intent = Intent(this.context, LandingActivity::class.java)
                        intent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TASK).or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this.context, "Login failed: ${it.message}", Toast.LENGTH_LONG).show()
                    editTextPasswordLogin.text.clear()
                }
        }

        backButtonLogin.setOnClickListener {
            val a = requireActivity() as LauncherActivity
            a.onBackPressed()
        }
    }
}