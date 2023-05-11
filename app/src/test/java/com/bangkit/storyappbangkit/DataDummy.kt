package com.bangkit.storyappbangkit

import com.bangkit.storyappbangkit.data.remote.model.ListStoryItem

object DataDummy {

    fun dummyStoryResponse(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (i in 0..100) {
            val story = ListStoryItem(
                photoUrl = "https://example.com/photo_$i.jpg",
                createdAt = "2023-03-02T12:34:56",
                name = "Story $i",
                description = "This is the description for Story $i",
                lat = i.toDouble(),
                id = "story-$i",
                lon = i.toDouble()
            )
            items.add(story)
        }
        return items
    }


}