package com.skillexchange.ui.auth

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.skillexchange.databinding.ActivityRegisterBinding
import com.skillexchange.models.User
import com.skillexchange.repository.FirestoreRepository
import com.skillexchange.firebase.FirebaseClient

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val repository = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val skills = arrayOf("Plumbing", "Electrical", "Carpentry", "Painting", "Gardening","Masonry","Welding","Tiling","Roofing","Interior_Design","Furniture_Repair","Car_Mechanic","Construction_work","Water_Tank_Cleaning")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, skills)
        binding.spinnerSkills.adapter = adapter

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            val skill = binding.spinnerSkills.selectedItem.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword != password) {
                Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseClient.auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: return@addOnSuccessListener
                    val user = User(id = uid, name = name, skill = skill, email = email)
                    repository.createUser(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Registration Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        binding.btnGoToLogin.setOnClickListener { finish() }
    }
}
