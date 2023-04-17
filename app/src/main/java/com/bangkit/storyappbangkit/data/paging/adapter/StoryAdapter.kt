package com.bangkit.storyappbangkit.data.paging.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.storyappbangkit.data.remote.model.ListStoryItem
import com.bangkit.storyappbangkit.databinding.StoriesItemRowBinding
import com.bangkit.storyappbangkit.ui.activity.stories.DetailStoryActivity
import com.bumptech.glide.Glide

class StoryAdapter :
    PagingDataAdapter<ListStoryItem, StoryAdapter.ListViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = StoriesItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
        }
    }

    inner class ListViewHolder(private val binding: StoriesItemRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val photo: ImageView = binding.imItemPhoto
        private val name: TextView = binding.tvItemName
        private val desc: TextView = binding.tvItemDescription
        fun bind(stories: ListStoryItem) {
            name.text = stories.name
            desc.text = stories.description
            Glide.with(itemView.context).load(stories.photoUrl).into(photo)

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetailStoryActivity::class.java)
                intent.putExtra("Story", stories)


                val optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    itemView.context as Activity,
                    Pair.create(photo, "profile"),
                    Pair.create(name, "name"),
                    Pair.create(desc, "description")
                )
                itemView.context.startActivity(intent, optionsCompat.toBundle())
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}
