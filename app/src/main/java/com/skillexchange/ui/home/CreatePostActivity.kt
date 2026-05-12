package com.skillexchange.ui.home

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.skillexchange.databinding.ActivityCreatePostBinding
import com.skillexchange.models.Post
import com.skillexchange.models.User
import com.skillexchange.repository.FirestoreRepository
import com.skillexchange.firebase.FirebaseClient
import com.skillexchange.ui.utils.Constants

class CreatePostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreatePostBinding
    private val repository = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val desc  = binding.etDescription.text.toString().trim()
            val skill = binding.etSkillRequired.text.toString().trim()
            val hours = binding.etHours.text.toString().toIntOrNull() ?: 0

            if (title.isEmpty() || desc.isEmpty() || skill.isEmpty() || hours == 0) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid   = FirebaseClient.currentUserId ?: return@setOnClickListener
            val docId = repository.db.collection(Constants.POSTS).document().id

            // Fetch user to get name, skill AND trust score
            repository.getUser(uid).addOnSuccessListener { snapshot ->
                val user = snapshot.toObject(User::class.java)
                val post = Post(
                    id           = docId,
                    userId       = uid,
                    authorName   = user?.name ?: "User",
                    authorSkill  = user?.skill ?: "",        // ← save skill
                    authorTrust  = user?.trustScore ?: 0.0,  // ← save trust score
                    title        = title,
                    description  = desc,
                    skillRequired = skill,
                    timeRequired = hours
                )
                repository.createPost(post).addOnSuccessListener {
                    Toast.makeText(this, "Post Created!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}