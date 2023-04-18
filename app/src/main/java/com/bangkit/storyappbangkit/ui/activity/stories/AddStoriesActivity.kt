package com.bangkit.storyappbangkit.ui.activity.stories

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bangkit.storyappbangkit.R
import com.bangkit.storyappbangkit.data.local.Session
import com.bangkit.storyappbangkit.data.remote.api.ApiConfig
import com.bangkit.storyappbangkit.data.remote.model.AddStory
import com.bangkit.storyappbangkit.databinding.ActivityAddStoriesBinding
import com.bangkit.storyappbangkit.ui.activity.MapsActivity
import com.bangkit.storyappbangkit.ui.activity.dataStore
import com.bangkit.storyappbangkit.ui.utils.reduceFileImage
import com.bangkit.storyappbangkit.ui.utils.rotateBitmap
import com.bangkit.storyappbangkit.ui.utils.rotateFile
import com.bangkit.storyappbangkit.ui.utils.uriToFile
import com.bangkit.storyappbangkit.ui.viewmodel.AddStoriesViewModel
import com.bangkit.storyappbangkit.ui.viewmodel.ViewModelFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File

class AddStoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoriesBinding
    private lateinit var addStoryViewModel: AddStoriesViewModel
    private var getFile: File? = null
    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = it.data?.getSerializableExtra("picture") as File
            getFile = myFile

            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean
            val result = rotateBitmap(
                BitmapFactory.decodeFile(myFile.path), isBackCamera
            )

            binding.previewImageView.setImageBitmap(result)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data ?: return@registerForActivityResult
            val myFile = uriToFile(selectedImg, this@AddStoriesActivity)
            getFile = myFile
            binding.previewImageView.setImageURI(selectedImg)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                showText()
                finish()
            }
        }
    }

    private fun showText() {
        Toast.makeText(
            this,
            "Tidak mendapatkan permission.",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun allPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                baseContext, it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pref = Session.getInstance(dataStore)
        addStoryViewModel =
            ViewModelProvider(this, ViewModelFactory(pref, this))[AddStoriesViewModel::class.java]

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        binding.edDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrBlank()) {
                    binding.uploadButton.isEnabled = true
                } else {
                    binding.uploadButton.isEnabled = false
                    binding.edDescription.error = getString(R.string.fill_in)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        binding.cameraxButton.setOnClickListener {
            startCameraX()
        }

        binding.galleryButton.setOnClickListener {
            startGallery()
        }

        binding.uploadButton.setOnClickListener {
            uploadImage()
        }
    }


    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, getString(R.string.choose_picture))
        launcherIntentGallery.launch(chooser)
    }

    private fun uploadImage() {
        showLoading(true)
        val file = getFile ?: run {
            Toast.makeText(this, getString(R.string.select_image), Toast.LENGTH_SHORT).show()
            return
        }
        reduceFileImage(file)
        val text = binding.edDescription.text.toString().takeIf { it.isNotEmpty() } ?: " "
        val description = text.toRequestBody("text/plain".toMediaType())

        // Rotate the file before uploading
        rotateFile(file, isBackCamera = true)

        // Create a MultipartBody.Part for uploading the image
        val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "photo",
            file.name,
            requestImageFile
        )

        addStoryViewModel.getToken().observe(this) { token ->
            val service = ApiConfig.getApiService()
                .uploadStories("Bearer $token", imageMultipart, description)
            service.enqueue(object : Callback<AddStory> {
                override fun onResponse(
                    call: Call<AddStory>,
                    response: Response<AddStory>
                ) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        responseBody?.let {
                            Toast.makeText(
                                this@AddStoriesActivity,
                                getString(R.string.success_upload),
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    } else {
                        Toast.makeText(
                            this@AddStoriesActivity,
                            response.message(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<AddStory>, t: Throwable) {
                    showLoading(false)
                    Toast.makeText(
                        this@AddStoriesActivity,
                        getString(R.string.failed_retrofit),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }



    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.apply {
            visibility = if (isLoading) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        val addItem = menu.findItem(R.id.add)
        val logoutItem = menu.findItem(R.id.logout)
        val languageItem = menu.findItem(R.id.language)
        addItem.isVisible = false
        logoutItem.isVisible = false
        languageItem.isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_back -> {
                onBackPressed()
                return true
            }
            R.id.map -> {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val CAMERA_X_RESULT = 200
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}