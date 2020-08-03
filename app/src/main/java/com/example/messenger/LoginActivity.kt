package com.example.messenger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.login.*
import kotlinx.android.synthetic.main.register.*

class LoginActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        btn_login.setOnClickListener {
            val email: String = et_emailLogin.text.toString()
            val pass: String = et_passwordLogin.text.toString()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this,"Please enter username and password",Toast.LENGTH_SHORT).show();
                return@setOnClickListener
            }

            // Login via Firebase
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,pass)
                .addOnCompleteListener{
                    if (!it.isSuccessful) return@addOnCompleteListener

                    // else if successful
                    var intent = Intent(this,LatestMessagesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)

                    Log.d("Main","Successfully logged in as: ${it.result!!.user!!.uid}")

                }
                .addOnFailureListener {
                    Toast.makeText(this,"${it.message}",Toast.LENGTH_SHORT).show();
                    Log.d("Main","Failed to Login: ${it.message}")
                }
        }

        tw_backToRegister.setOnClickListener{
            finish()
        }
    }
}