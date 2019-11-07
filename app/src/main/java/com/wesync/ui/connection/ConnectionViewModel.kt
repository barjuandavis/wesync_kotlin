package com.wesync.ui.connection

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wesync.connection.Endpoint

class ConnectionViewModel : ViewModel() {
    private val _ConnectedSession = MutableLiveData<Endpoint>()

    fun getAllSessions(): LiveData<List<String>> {
        val ld =  MutableLiveData<List<String>>()
        ld.postValue(listOf("Ayam","Bebek","Kucing","Ikan","Tikus"))
        return ld
    }

    fun onSessionClicked(it: String) {
        Log.d("henlo","henlo")
    }

}
