package com.wesync.connection

import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.google.android.gms.nearby.Nearby
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.nearby.connection.*
import com.wesync.connection.callbacks.*
import com.wesync.util.ForegroundServiceLauncher
import com.wesync.util.ServiceUtil.Companion.SERVICE_ID


class ConnectionManagerService : LifecycleService() {

    companion object {
        private val LAUNCHER = ForegroundServiceLauncher(ConnectionManagerService::class.java)
        @JvmStatic
        fun start(context: Context) = LAUNCHER.startService(context)
        @JvmStatic
        fun stop(context: Context) = LAUNCHER.stopService(context)
    }

    private val _binder = LocalBinder()
    private val strategy: Strategy = Strategy.P2P_STAR
    private val payloadCallback = MyPayloadCallback()
    private lateinit var con: MyConnectionLifecycleCallback
    private var _advertising: Boolean = false
    private var _discovering: Boolean = false

    private val endpointCallback = MyEndpointCallback()

    private val _endpoints = MutableLiveData<MutableList<Endpoint>>() //TODO: observed by ConnectionFragment
        val endpoints = _endpoints
    private val _payload = MutableLiveData<Payload>() //TODO: observed by MetronomeFragment
        val payload = _payload


    inner class LocalBinder : Binder() {
        fun getService() : ConnectionManagerService {
            return this@ConnectionManagerService
        }
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        con = MyConnectionLifecycleCallback(applicationContext,payloadCallback)
        observePayloadAndEndpoints()
        return _binder
    }


    fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(strategy).build()
        Nearby.getConnectionsClient(applicationContext)
            .startAdvertising("MusicDirector",SERVICE_ID, con, advertisingOptions)
            .addOnSuccessListener { Toast.makeText(this,"Accepting User...",Toast.LENGTH_SHORT).show()}
            .addOnFailureListener { throw it }
    }

    fun stopAdvertising() {
        Nearby.getConnectionsClient(applicationContext).stopAdvertising()
    }

    fun startDiscovery() {
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(strategy).build()
        Nearby.getConnectionsClient(applicationContext)
            .startDiscovery(SERVICE_ID, endpointCallback, discoveryOptions)
            .addOnSuccessListener {Toast.makeText(this,"Finding nearby session...",Toast.LENGTH_SHORT).show()}
            .addOnFailureListener { throw it }
    }

    fun stopDiscovering() {
        Nearby.getConnectionsClient(applicationContext).stopDiscovery()
    }

    fun sendPayload(s: String, p: Payload) {
        Nearby.getConnectionsClient(applicationContext).sendPayload(s,p)
    }

    private fun observePayloadAndEndpoints() {
        payloadCallback.payload.observe(this, Observer {this@ConnectionManagerService._payload.value = it})
        endpointCallback.endpoints.observe(this, Observer {this@ConnectionManagerService._endpoints.value = it})
        //_endpoints.value = mockListFORTESTINGPURPOSES()
    }

    private fun mockListFORTESTINGPURPOSES(): MutableList<Endpoint> {
        val mock = mutableListOf<Endpoint>()
        mock.add(Endpoint("test1",DiscoveredEndpointInfo("test1","test1")))
        mock.add(Endpoint("test2",DiscoveredEndpointInfo("test2","test2")))
        mock.add(Endpoint("test3",DiscoveredEndpointInfo("test3","test3")))
        mock.add(Endpoint("test4",DiscoveredEndpointInfo("test4","test4")))
        return mock
    }

    fun connect(endpointId: String) { //placeholder
        Nearby.getConnectionsClient(application).requestConnection("Slave", endpointId, con)
            .addOnSuccessListener {
                Toast.makeText(applicationContext,"Connecting to $endpointId",
                    Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext,"Failed to request connection to $endpointId",
                    Toast.LENGTH_SHORT).show()
            }
    }




}
