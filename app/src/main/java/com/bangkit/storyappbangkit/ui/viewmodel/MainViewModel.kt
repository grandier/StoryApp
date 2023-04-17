package com.bangkit.storyappbangkit.ui.viewmodel

import android.content.Context
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.bangkit.storyappbangkit.data.local.Session
import com.bangkit.storyappbangkit.data.paging.StoryRepository
import com.bangkit.storyappbangkit.data.remote.api.ApiConfig
import com.bangkit.storyappbangkit.data.remote.model.GetStory
import com.bangkit.storyappbangkit.data.remote.model.ListStoryItem
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel(private val pref: Session, private val storyRepository: StoryRepository) : ViewModel() {

    fun getToken(): LiveData<String> {
        return pref.getToken().asLiveData()
    }

    fun saveToken(token: String) {
        viewModelScope.launch {
            pref.saveToken(token)
        }
    }

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _listStory = MutableLiveData<List<ListStoryItem>>()
    val listStory: LiveData<List<ListStoryItem>> = _listStory
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message
    private val _acceptance = MutableLiveData<Boolean>()
    val acceptance: LiveData<Boolean> = _acceptance

    fun getAllStories(token: String) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getAllStories("Bearer $token", 1, 100,)
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
                    _acceptance.value = false
                }
            }

            override fun onFailure(call: Call<GetStory>, t: Throwable) {
                _isLoading.value = false
                _message.value = "Failure"
            }
        })
    }

    fun getStories(token: String): LiveData<PagingData<ListStoryItem>> =
        storyRepository.getStory(token).cachedIn(viewModelScope)

    private fun clearToken() {
        viewModelScope.launch {
            pref.clearToken()
        }
    }

    fun logout() {
        clearToken()
    }

}
