package com.skillexchange.ui.profile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.skillexchange.databinding.ActivityEditProfileBinding
import com.skillexchange.models.User
import com.skillexchange.repository.FirestoreRepository
import com.skillexchange.ui.utils.SessionManager

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private val repository = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uid = SessionManager.getUid() ?: return
        repository.getUser(uid).addOnSuccessListener { snapshot ->
            val user = snapshot.toObject(User::class.java) ?: return@addOnSuccessListener
            binding.etName.setText(user.name)
            binding.etSkill.setText(user.skill)
        }

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val skill = binding.etSkill.text.toString().trim()

            if (name.isEmpty() || skill.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            repository.updateProfile(uid, name, skill)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }

        binding.btnBack.setOnClickListener { finish() }
    }
}
