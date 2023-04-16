package com.bangkit.storyappbangkit.data.di

import android.content.Context
import com.bangkit.storyappbangkit.data.database.StoryDatabase
import com.bangkit.storyappbangkit.data.local.Session
import com.bangkit.storyappbangkit.data.paging.StoryRepository
import com.bangkit.storyappbangkit.data.remote.api.ApiConfig

object Injection {
    fun provideRepository(context: Context): StoryRepository {
        val database = StoryDatabase.getDatabase(context)
        val apiService = ApiConfig.getApiService()
        return StoryRepository(database, apiService)
    }
}