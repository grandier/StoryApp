package com.bangkit.storyappbangkit.ui.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bangkit.storyappbangkit.R
import com.bangkit.storyappbangkit.data.local.Session
import com.bangkit.storyappbangkit.data.remote.model.ListStoryItem
import com.bangkit.storyappbangkit.databinding.ActivityMainBinding
import com.bangkit.storyappbangkit.ui.activity.stories.AddStoriesActivity
import com.bangkit.storyappbangkit.ui.activity.stories.StoryAdapter
import com.bangkit.storyappbangkit.ui.viewmodel.MainViewModel
import com.bangkit.storyappbangkit.ui.viewmodel.ViewModelFactory

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pref = Session.getInstance(dataStore)

        mainViewModel = ViewModelProvider(
            this, ViewModelFactory(pref)
        )[MainViewModel::class.java]

        mainViewModel.getToken().observe(this) { token ->
            if (token.isNotEmpty()) {
                mainViewModel.getAllStories(token)

            } else if(token.isEmpty()) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                mainViewModel.getAllStories(token)
            }
        }

        mainViewModel.listStory.observe(this) { listStory ->
            showStories(listStory)
        }

        mainViewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        mainViewModel.message.observe(this) {
            if (it == "Failure") {
                Toast.makeText(this, R.string.failed_retrofit, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        mainViewModel.acceptance.observe(this) {
            if (it) {
                Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnADD.setOnClickListener {
            val intent = Intent(this, AddStoriesActivity::class.java)
            startActivity(intent)
        }

        Log.d("MainAcitivy", "Haloo $pref")
    }

    private fun showStories(stories: List<ListStoryItem>) {
        binding.rvStories.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = StoryAdapter(ArrayList(stories))
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        val add = menu?.findItem(R.id.add)
        add?.isVisible = false
        val language = menu?.findItem(R.id.language)
        language?.isVisible = false
        val back = menu?.findItem(R.id.action_back)
        back?.isVisible = false
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                mainViewModel.logout()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(homeIntent)
    }
}