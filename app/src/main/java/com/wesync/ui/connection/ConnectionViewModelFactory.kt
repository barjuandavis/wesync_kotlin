package com.wesync.ui.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wesync.connection.callbacks.MyEndpointCallback
import com.wesync.connection.callbacks.MyPayloadCallback
import kotlin.IllegalArgumentException

class ConnectionViewModelFactory(
    private val endpointCallback: MyEndpointCallback?,
    private val payloadCallback: MyPayloadCallback?): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConnectionViewModel::class.java)) return ConnectionViewModel(endpointCallback,payloadCallback) as T
        throw IllegalArgumentException("Unknown ViewModel Class")
    }

}