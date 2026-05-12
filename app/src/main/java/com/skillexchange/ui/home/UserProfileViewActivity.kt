package com.skillexchange.ui.home

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.skillexchange.databinding.ActivityUserProfileViewBinding
import com.skillexchange.models.User
import com.skillexchange.repository.FirestoreRepository
import com.skillexchange.ui.utils.Constants

class UserProfileViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileViewBinding
    private val repository = FirestoreRepository()

    private val avatarColors = listOf(
        "#1A237E", "#0D47A1", "#006064", "#1B5E20",
        "#4A148C", "#880E4F", "#E65100", "#BF360C"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = intent.getStringExtra("user_id") ?: run { finish(); return }

        binding.btnBack.setOnClickListener { finish() }

        loadUserProfile(userId)
    }

    private fun loadUserProfile(userId: String) {
        repository.db.collection(Constants.USERS).document(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.toObject(User::class.java) ?: return@addOnSuccessListener

                binding.tvName.text        = user.name
                binding.tvExpertise.text   = if (user.skill.isNotEmpty()) "Expertise: ${user.skill}" else ""
                binding.tvEmail.text       = user.email
                binding.tvTrustScore.text  = String.format("%.1f", user.trustScore)
                binding.tvSwapsDone.text   = user.swapsDone.toString()

                setInitialsAvatar(user.name)
            }
            .addOnFailureListener {
                finish()
            }
    }

    private fun setInitialsAvatar(name: String) {
        if (name.isBlank()) return
        val parts    = name.trim().split(" ")
        val initials = when {
            parts.size >= 2      -> "${parts[0].first()}${parts[1].first()}".uppercase()
            parts[0].length >= 2 -> parts[0].take(2).uppercase()
            else                 -> parts[0].uppercase()
        }
        val colorIndex = (name.first().lowercaseChar().code) % avatarColors.size
        binding.tvAvatar.text = initials
        binding.tvAvatar.background.setTint(Color.parseColor(avatarColors[colorIndex]))
    }
}