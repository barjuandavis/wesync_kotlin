package com.wesync.connection.callbacks

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.wesync.connection.Endpoint

class MyEndpointCallback : EndpointDiscoveryCallback() {

    private val _endpoints = MutableLiveData<MutableList<Endpoint>>()
        val endpoints = _endpoints

    init {
        _endpoints.value = mutableListOf()
    }


    override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
        val cl = _endpoints.value!!
        if (!cl.any{ it.endpointId == endpointId && it.info == info }) {
            cl.add(Endpoint(endpointId,info))
        }
        _endpoints.value = cl
    }
    override fun onEndpointLost(endpointId: String) {
        val cl = _endpoints.value!!
        val removee = cl.find {it.endpointId == endpointId }
        if (removee != null) {
            cl.remove(removee)
        }
        _endpoints.value = cl
    }


}