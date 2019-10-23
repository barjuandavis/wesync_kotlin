package com.wesync.ui.connection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ConnectionViewModel : ViewModel() {
    private val _connected =  MutableLiveData<Boolean>(false)
        val connected: LiveData<Boolean> = _connected


}
