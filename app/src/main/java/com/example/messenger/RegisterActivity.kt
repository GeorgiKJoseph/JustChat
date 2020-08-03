package com.example.messenger

import android.app.ActionBar
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.marginTop
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.register.*
import java.util.*

import com.example.messenger.models.User
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.resolution
import java.io.File

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        btn_register.setOnClickListener {
            performRegister()
        }

        btn_select_photo_register.setOnClickListener {
            Toast.makeText(this,"Upload photo",Toast.LENGTH_SHORT).show();
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }

        tw_AlreadyHaveAnAccount.setOnClickListener {
            // Launch Login activity
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }
    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            // Proceed and check the selected image...
            Log.d("RegisterActivity","Photo is selected")
            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)

            img_circular_register.setImageBitmap(bitmap)

            btn_select_photo_register.alpha = 0f

            setCircularProfilePic()
//            val bitmapDrawable = BitmapDrawable(bitmap)
//            btn_select_photo_register.setBackgroundDrawable(bitmapDrawable)

            // Remove text from the image
            btn_select_photo_register.text = ""

        }
    }

    private fun performRegister(){
        val userName: String? = et_usernameRegister.text.toString()
        val email: String = et_emailRegister.text.toString()
        val password: String = et_passwordRegister.text.toString()
        val password2: String = et_password2Register.text.toString()

        if(userName!!.isEmpty()){
            Toast.makeText(this,"Please enter Username",Toast.LENGTH_SHORT).show();
            return
        }
        if(email.isEmpty()){
            Toast.makeText(this,"Please enter email",Toast.LENGTH_SHORT).show();
            return
        }
        if(password.isEmpty()){
            Toast.makeText(this,"Please enter password",Toast.LENGTH_SHORT).show();
            return
        }
        if(password != password2){
            Toast.makeText(this,"Password does not match",Toast.LENGTH_SHORT).show();
            return
        }

        Toast.makeText(this,"Please Wait...",Toast.LENGTH_LONG).show();

        // Firebase Authentication to create a user with email and password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                // else if successful
                Log.d("RegisterActivity","Successfully created user: ${it.result!!.user!!.uid}")

                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener{
                Toast.makeText(this,"${it.message}",Toast.LENGTH_SHORT).show();
                Log.d("RegisterActivity","Failed to create user: ${it.message}")
            }
    }
    private fun uploadImageToFirebaseStorage(){
        if (selectedPhotoUri == null) return

        var filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("RegisterActivity","Successfully uploaded image: ${it.metadata!!.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d("RegisterActivity","File Location: $it")
                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener{
                Log.d("RegisterActivity", "Image upload failed")
            }
    }
    private fun saveUserToFirebaseDatabase(profileImageUrl: String){
        val uid= FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid, et_usernameRegister.text.toString(), profileImageUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity","Saved username to firebase")

                val intent = Intent(this,LatestMessagesActivity::class.java)
                // Clear off all previous activity from internal stack
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }
            .addOnFailureListener{
                Toast.makeText(this,"${it.message}",Toast.LENGTH_SHORT)
                Log.d("RegisterActivity","$it")
            }

    }
    private fun setCircularProfilePic(){
        // code works only in constraint layout
    }
}
