package com.wesync.ui.connection

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wesync.connection.Endpoint
import com.wesync.connection.callbacks.MyConnectionLifecycleCallback
import com.wesync.connection.callbacks.MyEndpointCallback
import com.wesync.connection.callbacks.MyPayloadCallback

class ConnectionViewModel(
    private val endpointCallback: MyEndpointCallback?,
    private val payloadCallback: MyPayloadCallback?
) : ViewModel() {

    private val _availableSessions = endpointCallback?.endpoints
        val availableSessions = _availableSessions

    fun getAllSessions(): LiveData<List<String>> {
        val ld =  MutableLiveData<List<String>>()
        ld.postValue(listOf("Ayam","Bebek","Kucing","Ikan","Tikus"))
        return ld
    }

    fun onSessionClicked(it: Endpoint) {
        Log.d("henlo","henlo")
    }

}
