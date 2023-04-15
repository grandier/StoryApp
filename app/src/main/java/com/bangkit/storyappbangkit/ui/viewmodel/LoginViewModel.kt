package com.bangkit.storyappbangkit.ui.viewmodel

import android.content.ContentValues.TAG
import android.content.Intent
import android.provider.Settings.Global.getString
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.*
import com.bangkit.storyappbangkit.R
import com.bangkit.storyappbangkit.data.local.Session
import com.bangkit.storyappbangkit.data.remote.api.ApiConfig
import com.bangkit.storyappbangkit.data.remote.model.Login
import com.bangkit.storyappbangkit.data.remote.model.LoginResult
import com.bangkit.storyappbangkit.ui.activity.LoginActivity
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.*

class LoginViewModel(private val pref: Session) : ViewModel() {

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
    private val _acceptance = MutableLiveData<Boolean>()
    val acceptance: LiveData<Boolean> = _acceptance
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message


    init{
        _acceptance.value = false
        if(pref.getToken().asLiveData().value != null){
            _acceptance.value = true
        }
    }

    fun login(email: String, password: String) {

        _isLoading.value = true

        ApiConfig.getApiService().login(email, password).enqueue(object : Callback<Login> {
            override fun onResponse(call: Call<Login>, response: Response<Login>) {
                _isLoading.value = false

                if (response.isSuccessful) {
                    if (response.body()?.message.equals("success")) {
                        _message.value = response.message()
                        _acceptance.value = true
                        saveToken(response.body()?.loginResult?.token.toString())
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage =
                        if (errorBody != null) JSONObject(errorBody).getString("message") else response.message()
                    _message.value = response.message()
                }
            }

            override fun onFailure(call: Call<Login>, t: Throwable) {
                _isLoading.value = false
                _acceptance.value = false
                _message.value = "Failure"
                Log.e(TAG, "onFailure: ${t.message}")
            }
        })

    }
}

