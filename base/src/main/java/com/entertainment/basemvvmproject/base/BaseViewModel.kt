package com.entertainment.basemvvmproject.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


abstract class BaseViewModel : ViewModel() {
    var messageError = SingleLiveEvent<Any>()
    var isLoading = MutableLiveData(false)
    var toastMessage = MutableLiveData("")
}