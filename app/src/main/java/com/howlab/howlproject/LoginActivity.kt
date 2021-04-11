package com.howlab.howlproject

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.howlab.howlproject.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    lateinit var binding : ActivityLoginBinding
    var googleSigninInClient : GoogleSignInClient? = null
    var GoogleLoginCode = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_login)
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSigninInClient = GoogleSignIn.getClient(this,gso)

        binding.loginBtn.setOnClickListener {
            var i = googleSigninInClient?.signInIntent
            startActivityForResult(i,GoogleLoginCode)
        }
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO),0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == GoogleLoginCode){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account!!.idToken,null)

            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        saveUserDataToDatabase(task.result!!.user)
                    }
                }
        }
    }
    fun saveUserDataToDatabase(user : FirebaseUser?){
        val email = user?.email
        val uid = user?.uid

        var userDTO = UserDTO()
        userDTO.email = email

        FirebaseFirestore.getInstance().collection("users").document(uid!!).set(userDTO)
        finish()
        startActivity(Intent(this,MainActivity::class.java))
    }
}