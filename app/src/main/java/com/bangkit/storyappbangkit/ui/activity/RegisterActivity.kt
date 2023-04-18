package com.bangkit.storyappbangkit.ui.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.bangkit.storyappbangkit.data.local.Session
import com.bangkit.storyappbangkit.databinding.ActivityRegisterBinding
import com.bangkit.storyappbangkit.ui.customview.ButtonRegister
import com.bangkit.storyappbangkit.ui.customview.EmailEditText
import com.bangkit.storyappbangkit.ui.customview.NameEditText
import com.bangkit.storyappbangkit.ui.customview.PasswordEditText
import com.bangkit.storyappbangkit.ui.viewmodel.RegisterViewModel
import com.bangkit.storyappbangkit.ui.viewmodel.ViewModelFactory

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var registerButton: ButtonRegister
    private lateinit var emailEditText: EmailEditText
    private lateinit var passwordEditText: PasswordEditText
    private lateinit var nameEditText: NameEditText

    private lateinit var registerViewModel: RegisterViewModel

    private var correctEmail: Boolean = false
    private var correctPassword: Boolean = false
    private var correctName: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        useAnimation()

        registerButton = binding.registerButton
        emailEditText = binding.etEmail
        passwordEditText = binding.etPassword
        nameEditText = binding.etName

        // Get the user session using data store.
        val pref = Session.getInstance(dataStore)

        registerViewModel = ViewModelProvider(
            this, ViewModelFactory(pref, this)
        )[RegisterViewModel::class.java]

        if (!intent.getStringExtra("name").isNullOrEmpty()) {
            nameEditText.setText(intent.getStringExtra("name"))
            correctName = true
        }
        if (!intent.getStringExtra("email").isNullOrEmpty()) {
            emailEditText.setText(intent.getStringExtra("email"))
            correctEmail = true
        }
        if (!intent.getStringExtra("password").isNullOrEmpty()) {
            passwordEditText.setText(intent.getStringExtra("password"))
            correctPassword = true
        }

        emailEditText.addTextChangedListener { text ->
            correctEmail =
                !text.isNullOrEmpty() && LoginActivity.emailRegex.matches(text.toString())
            setLoginButtonEnable()
        }

        passwordEditText.addTextChangedListener { text ->
            correctPassword = !text.isNullOrEmpty() && text.length >= 8
            setLoginButtonEnable()
        }

        nameEditText.addTextChangedListener { text ->
            correctName = !text.isNullOrEmpty() && text.length >= 3
            setLoginButtonEnable()
        }

        registerButton.setOnClickListener {
            registerViewModel.register(
                nameEditText.text.toString(),
                emailEditText.text.toString(),
                passwordEditText.text.toString()
            )
        }

        registerViewModel.acceptance.observe(this) {
            if (it) {
                goToLogin(emailEditText.text.toString(), passwordEditText.text.toString())
            }
        }

        registerViewModel.message.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        registerViewModel.isLoading.observe(this) {
            showProgressBar(it)
        }

        binding.tvAccount.setOnClickListener {
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i)
            finish()
        }
    }

    private fun setLoginButtonEnable() {
        registerButton.isEnabled = correctEmail && correctPassword && correctName
    }

    private fun showProgressBar(state: Boolean) {
        if (state) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun goToLogin(email: String, password: String) {
        val i = Intent(this, LoginActivity::class.java)
        i.putExtra("email", email)
        i.putExtra("password", password)
        startActivity(i)
        finish()
    }

    private fun useAnimation() {
        val viewsToAnimate = arrayOf(
            binding.ivApplogo,
            binding.tvLogin,
            binding.tvName,
            binding.etName,
            binding.tvEmail,
            binding.etEmail,
            binding.tvPassword,
            binding.etPassword,
            binding.registerButton,
            binding.tvAccount
        )

        AnimatorSet().apply {
            playSequentially(*viewsToAnimate.map {
                ObjectAnimator.ofFloat(it, View.ALPHA, ALPHA).setDuration(DURATION)
            }.toTypedArray())
            start()
        }
    }

    companion object {
        private const val DURATION = 100L
        private const val ALPHA = 1f
    }
}