package com.bangkit.storyappbangkit.ui.activity.stories

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.storyappbangkit.R
import com.bangkit.storyappbangkit.data.remote.model.ListStoryItem
import com.bangkit.storyappbangkit.databinding.ActivityDetailStoryBinding
import com.bangkit.storyappbangkit.ui.activity.MapsActivity
import com.bumptech.glide.Glide

class DetailStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val detailStories = intent.getParcelableExtra<ListStoryItem>("Story")

        binding.tvItemName.text = detailStories?.name
        binding.tvItemDescription.text = detailStories?.description

        Glide.with(this)
            .load(detailStories?.photoUrl)
            .into(binding.imItemPhoto)
        binding.tvItemName.text = detailStories?.name
        binding.tvItemDescription.text = detailStories?.description
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        menu.findItem(R.id.add).isVisible = false
        menu.findItem(R.id.logout).isVisible = false
        menu.findItem(R.id.language).isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_back -> {
                onBackPressed()
                true
            }
            R.id.map -> {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}