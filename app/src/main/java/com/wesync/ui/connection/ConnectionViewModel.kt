package com.wesync.ui.connection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ConnectionViewModel : ViewModel() {
    private val _connected =  MutableLiveData<Boolean>(false)
        val connected: LiveData<Boolean> = _connected

    fun getAllSessions(): LiveData<List<String>> {
        val ld =  MutableLiveData<List<String>>()
        ld.postValue(listOf("Ayam","Bebek","Kucing","Ikan","Tikus"))
        return ld
    }

}
