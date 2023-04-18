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
import com.bangkit.storyappbangkit.R
import com.bangkit.storyappbangkit.data.local.Session
import com.bangkit.storyappbangkit.databinding.ActivityLoginBinding
import com.bangkit.storyappbangkit.ui.customview.ButtonLogin
import com.bangkit.storyappbangkit.ui.customview.EmailEditText
import com.bangkit.storyappbangkit.ui.customview.PasswordEditText
import com.bangkit.storyappbangkit.ui.viewmodel.LoginViewModel
import com.bangkit.storyappbangkit.ui.viewmodel.ViewModelFactory

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginButton: ButtonLogin
    private lateinit var emailEditText: EmailEditText
    private lateinit var passwordEditText: PasswordEditText

    private lateinit var loginViewModel: LoginViewModel

    private var correctEmail = false
    private var correctPassword = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        useAnimation()

        supportActionBar?.hide()

        loginButton = binding.loginButton
        emailEditText = binding.etEmail
        passwordEditText = binding.etPassword

        // Get the user session using data store.
        val pref = Session.getInstance(dataStore)

        // Initialize the LoginViewModel
        loginViewModel = ViewModelProvider(
            this, ViewModelFactory(pref, this)
        )[LoginViewModel::class.java]

        if (!intent.getStringExtra("email").isNullOrEmpty()) {
            emailEditText.setText(intent.getStringExtra("email"))
            correctEmail = true
        }
        if (!intent.getStringExtra("password").isNullOrEmpty()) {
            passwordEditText.setText(intent.getStringExtra("password"))
            correctPassword = true
        }

        emailEditText.addTextChangedListener { text ->
            correctEmail = !text.isNullOrEmpty() && emailRegex.matches(text.toString())
            setLoginButtonEnable()
        }

        passwordEditText.addTextChangedListener { text ->
            correctPassword = !text.isNullOrEmpty() && text.length >= 8
            setLoginButtonEnable()
        }


        loginButton.setOnClickListener {
            loginViewModel.login(emailEditText.text.toString(), passwordEditText.text.toString())
        }

        loginViewModel.message.observe(this) {
            if (it == "Failure") {
                Toast.makeText(this, R.string.login_error, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        loginViewModel.isLoading.observe(this) {
            showLoading(it)
        }

        loginViewModel.acceptance.observe(this) {
            if (it) {
                val i = Intent(this, MainActivity::class.java)
                startActivity(i)
                finish()
            }
        }

        binding.tvAccount.setOnClickListener {
            val i = Intent(this, RegisterActivity::class.java)
            startActivity(i)
            finish()
        }
    }

    private fun setLoginButtonEnable() {
        loginButton.isEnabled = correctEmail && correctPassword
    }

    private fun showLoading(state: Boolean) {
        if (state) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun useAnimation() {
        val objectView = listOf(
            binding.ivApplogo,
            binding.tvLogin,
            binding.tvEmail,
            binding.etEmail,
            binding.tvPassword,
            binding.etPassword,
            binding.loginButton,
            binding.tvAccount
        )

        AnimatorSet().apply {
            playSequentially(*objectView.map {
                ObjectAnimator.ofFloat(it, View.ALPHA, ALPHA_FULL).setDuration(ANIMATION_DURATION)
            }.toTypedArray())
            start()
        }
    }

    companion object {
        private const val ANIMATION_DURATION = 100L
        private const val ALPHA_FULL = 1.0f
        val emailRegex: Regex = Regex("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+\$")
    }
}
