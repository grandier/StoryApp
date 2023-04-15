package com.bangkit.storyappbangkit.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.bangkit.storyappbangkit.data.local.Session
import java.io.File

class AddStoriesViewModel(private val pref: Session) : ViewModel() {

    private var file: File? = null

    fun getFile(): File? {
        return file
    }

    fun setFile(file: File?) {
        this.file = file
    }

    fun getToken(): LiveData<String> {
        return pref.getToken().asLiveData()
    }
}