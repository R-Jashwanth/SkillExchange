package com.skillexchange.ui.profile

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.skillexchange.databinding.ActivityProfileBinding
import com.skillexchange.models.User
import com.skillexchange.repository.FirestoreRepository
import com.skillexchange.ui.utils.Constants
import com.skillexchange.ui.utils.SessionManager
import com.skillexchange.ui.auth.LoginActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.skillexchange.R

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val repository = FirestoreRepository()

    private val avatarColors = listOf(
        "#1A237E", "#0D47A1", "#006064", "#1B5E20",
        "#4A148C", "#880E4F", "#E65100", "#BF360C"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchUserData()

        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        binding.btnLogout.setOnClickListener {
            SessionManager.logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun fetchUserData() {
        val uid = SessionManager.getUid() ?: return

        // ✅ One-time fix: reset trust score if it's the old hardcoded 4.5
        repository.db.collection(Constants.USERS).document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val current = doc.getDouble("trustScore") ?: 0.0
                if (current == 4.5) {
                    repository.db.collection(Constants.USERS).document(uid)
                        .update("trustScore", 0.0)
                }
            }
        repository.db.collection(Constants.USERS).document(uid)
            .addSnapshotListener { snapshot, _ ->
                val user = snapshot?.toObject(User::class.java) ?: return@addSnapshotListener

                binding.tvName.text       = user.name
                binding.tvEmail.text      = user.email
                binding.tvSkill.text      = if (user.skill.isNotEmpty()) "Expertise: ${user.skill}" else ""
                binding.tvTrustScore.text = String.format("%.1f", user.trustScore)

                // ✅ swapsDone replaces skillPoints
                binding.tvSkillPoints.text = user.swapsDone.toString()

                setInitialsAvatar(user.name)
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

        // ✅ Mutate a COPY of the drawable so other views are not affected
        val originalDrawable = ContextCompat.getDrawable(this, R.drawable.bg_icon_button)!!
        val tintedDrawable = DrawableCompat.wrap(originalDrawable).mutate()
        DrawableCompat.setTint(tintedDrawable, Color.parseColor(avatarColors[colorIndex]))

        binding.tvAvatar.text = initials
        binding.tvAvatar.background = tintedDrawable
    }
}