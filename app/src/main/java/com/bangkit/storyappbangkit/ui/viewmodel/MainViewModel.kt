package com.bangkit.storyappbangkit.ui.viewmodel

import androidx.lifecycle.*
import com.bangkit.storyappbangkit.data.local.Session
import com.bangkit.storyappbangkit.data.remote.api.ApiConfig
import com.bangkit.storyappbangkit.data.remote.model.GetStory
import com.bangkit.storyappbangkit.data.remote.model.ListStoryItem
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel(private val pref: Session) : ViewModel() {
    fun getToken(): LiveData<String> {
        return pref.getToken().asLiveData()
    }

    fun saveToken(token: String) {
        viewModelScope.launch {
            pref.saveToken(token)
        }
    }

    private var token: String = pref.getToken().toString()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _listStory = MutableLiveData<List<ListStoryItem>>()
    val listStory: LiveData<List<ListStoryItem>> = _listStory
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun getAllStories(token: String) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getAllStories("Bearer $token", 1, 100, 0)
        client.enqueue(object : Callback<GetStory> {
            override fun onResponse(
                call: Call<GetStory>,
                response: Response<GetStory>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _message.value = response.body()?.message as String
                    _listStory.value = response.body()?.listStory as List<ListStoryItem>?
                } else {
                    _message.value = response.message()
                }
            }

            override fun onFailure(call: Call<GetStory>, t: Throwable) {
                _isLoading.value = false
                _message.value = "Failure"
            }
        })
    }

    fun clearToken() {
        viewModelScope.launch {
            pref.clearToken()
        }
    }

    fun logout() {
        clearToken()
    }

}