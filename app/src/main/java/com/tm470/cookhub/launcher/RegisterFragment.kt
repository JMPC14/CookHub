package com.tm470.cookhub.launcher

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.tm470.cookhub.CurrentUser
import com.tm470.cookhub.LandingActivity
import com.tm470.cookhub.R
import com.tm470.cookhub.models.CookHubUser
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_register.*
import java.util.*

@RequiresApi(Build.VERSION_CODES.P)
@SuppressLint("RestrictedApi")
class RegisterFragment : Fragment() {

    private var selectedPhotoUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        buttonSubmitRegister.setOnClickListener {
            if (selectedPhotoUri != null) {
                uploadImageToFirebase()
            } else {
                registerUser()
            }
        }

        selectPhotoRegister.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        backButtonRegister.setOnClickListener {
            val a = requireActivity() as LauncherActivity
            a.onBackPressed()
        }
    }

    private fun validateFields(): Boolean {
        val username = editTextUsernameRegister.text.toString()
        val email = editTextEmailRegister.text.toString()
        val emailConfirm = editTextEmailConfirmRegister.text.toString()
        val password = editTextPasswordRegister.text.toString()
        val passwordConfirm = editTextPasswordConfirmRegister.text.toString()

        val userList: MutableList<CookHubUser> = mutableListOf()
        val usersRef = FirebaseDatabase.getInstance().getReference("/users")
        usersRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val user: CookHubUser? = it.getValue(CookHubUser::class.java)
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

    private fun registerUser(profileImageUrl: String? = "https://i.imgur.com/RR5LUO1.jpg") {
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
                    val friendsRef = FirebaseDatabase.getInstance().getReference("/users/$uid/friends")
                    val user = CookHubUser(
                        uid,
                        username,
                        email,
                        profileImageUrl
                    )
                    CurrentUser.user = user
                    ref.setValue(user)
                        .addOnSuccessListener {
                            friendsRef.setValue(listOf<String>())
                            val intent = Intent(this.context, LandingActivity::class.java)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().contentResolver, selectedPhotoUri!!))
            imageViewRegister.setImageBitmap(bitmap)
            selectPhotoRegister.alpha = 0f
        }
    }

    private fun uploadImageToFirebase() {
        if (selectedPhotoUri == null) {
            return
        }

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("Main", "Successfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d("Main", "File Location: $it")

                    registerUser(it.toString())
                }
            }
            .addOnFailureListener {
                Log.d("Main", "Image upload failed")
            }
    }
}