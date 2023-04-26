package com.example.comp1786cw2project2.feature.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.comp1786cw2project2.feature.base.BaseViewModel
import com.example.comp1786cw2project2.local.database.UrlDao
import com.example.comp1786cw2project2.local.model.Url
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val urlDao: UrlDao
) : BaseViewModel() {
    private var _currentUrlMutableLiveData: MutableLiveData<Url> = MutableLiveData()
    var currentUrlLiveData : LiveData<Url> = _currentUrlMutableLiveData
    private var currentIndex = 0

    fun addUrl(url: String) {
        urlDao.addUrl(Url(url = url))
        currentIndex = urlDao.getListUrl().size - 1
        _currentUrlMutableLiveData.value = Url(url = url)
    }

    fun onNextClicked() {
        val range = urlDao.getListUrl().indices
        currentIndex++
        if (urlDao.getListUrl().isNotEmpty() && currentIndex in range) {
            getCurrentUrl(currentIndex)
        } else {
            currentIndex--
        }
    }

    fun getCurrentUrl(index: Int) {
        val currentListUrl = urlDao.getListUrl()
        val range = currentListUrl.indices
        if (currentListUrl.isNotEmpty() && index in range) {
            _currentUrlMutableLiveData.value = currentListUrl[index]
        }
    }

    fun onPreviousClicked() {
        val range = urlDao.getListUrl().indices
        currentIndex--
        if (urlDao.getListUrl().isNotEmpty() && currentIndex in range) {
            getCurrentUrl(currentIndex)
        } else {
            currentIndex++
        }
    }
}