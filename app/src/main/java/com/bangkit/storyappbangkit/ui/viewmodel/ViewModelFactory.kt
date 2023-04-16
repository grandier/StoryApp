package com.bangkit.storyappbangkit.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bangkit.storyappbangkit.data.local.Session

class ViewModelFactory(private val pref: Session) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(pref) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(pref) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(pref) as T
            }
            modelClass.isAssignableFrom(AddStoriesViewModel::class.java) -> {
                AddStoriesViewModel(pref) as T
            }
            modelClass.isAssignableFrom(MapsViewModel::class.java) -> {
                MapsViewModel(pref) as T
            }
            else -> throw IllegalArgumentException("Unknown Viewmodel Class: " + modelClass.name)
        }
    }

}