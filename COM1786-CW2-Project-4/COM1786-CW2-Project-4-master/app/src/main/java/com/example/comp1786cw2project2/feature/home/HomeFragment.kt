package com.example.comp1786cw2project2.feature.home

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.webkit.URLUtil
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.com1786_cw2project1.R
import com.example.com1786_cw2project1.databinding.FragmentHomeBinding
import com.example.comp1786cw2project2.feature.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.File


@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {
    override val viewModel: HomeViewModel by viewModels()

    override fun onCreateViewBinding(inflater: LayoutInflater): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewModel()
    }

    private fun initViewModel() = with(viewModel) {
        getCurrentUrl(0)
        currentUrlLiveData.observe(viewLifecycleOwner) {
            if (it.url.isNotEmpty() && URLUtil.isValidUrl(it.url)) {
                Glide.with(requireActivity()).load(it.url).centerCrop().error(R.drawable.error).into(viewBinding.imgImage)
            } else {
                val file = File(it.url)
                if (file.exists()) {
                    viewBinding.imgImage.setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))
                }
            }
        }
    }

    private fun initView() = with(viewBinding) {
        initButton()
    }

    private fun initButton() = with(viewBinding) {
        btnAddUrl.setOnClickListener {
            val url = edtImageUrl.text.toString()
            saveUrlToDb(url)
        }

        btnCamera.setOnClickListener {
            dispatchTakePictureIntent()
        }

        btnPrevious.setOnClickListener {
            viewModel.onPreviousClicked()
        }

        btnNext.setOnClickListener {
            viewModel.onNextClicked()
        }
    }

    private fun saveUrlToDb(url: String) {
        if (URLUtil.isValidUrl(url)) {
            viewBinding.edtImageUrl.text?.clear()
            viewBinding.edtImageUrl.clearFocus()
            viewModel.addUrl(url)
        } else {
            Toast.makeText(requireContext(), "Invalid url!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(takePictureIntent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            saveImage(data?.extras?.get("data") as Bitmap)
        }
    }

    private fun saveImage(bitmap: Bitmap) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "my_picture.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
        val imageUri = context?.contentResolver?.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), values)
        context?.contentResolver?.openOutputStream(imageUri!!).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }

        imageUri?.let {
            val filePath: String = getRealPathImage(it)
            viewModel.addUrl(filePath)
        }
    }

    companion object {
        fun newInstance() = HomeFragment()
    }

    private fun getRealPathImage(uri: Uri): String {
        val cursor = requireContext().contentResolver.query(
            uri,
            arrayOf(MediaStore.Images.Media.DATA),
            null,
            null,
            null
        )
        val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val filePath = cursor.getString(columnIndex)
        cursor.close()
        return filePath
    }
}