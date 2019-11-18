package com.wesync.connection.callbacks

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.wesync.connection.DiscoveredEndpoint

class MyEndpointCallback : EndpointDiscoveryCallback() {

    private val _sessions = MutableLiveData<MutableList<DiscoveredEndpoint>>()
        val sessions: LiveData<MutableList<DiscoveredEndpoint>> = _sessions

    init {
        _sessions.value = mutableListOf()
    }

    override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
        val cl = _sessions.value!!
        if (!cl.any{it.endpointId == endpointId} ) {
            cl.add(DiscoveredEndpoint(endpointId,info))
            Log.d("onEndpointFound","DiscoveredEndpoint added: $endpointId (${info.endpointName})")
        }
        _sessions.value = cl
    }
    override fun onEndpointLost(endpointId: String) {
        val cl = _sessions.value!!
        val removee = cl.find {it.endpointId == endpointId }
        if (removee != null) {
            cl.remove(removee)
        }
        _sessions.value = cl
    }


}