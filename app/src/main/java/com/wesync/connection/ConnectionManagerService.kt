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
        //Log.d("clientBinding","client is BINDING - ConnectionManagerService")
        super.onBind(intent)
        con = MyConnectionLifecycleCallback(applicationContext,payloadCallback)
        observePayloadAndEndpoints()
        return _binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        //Log.d("clientBinding","client is UNBINDING - ConnectionManagerService")
        return super.onUnbind(intent)
    }

    fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(strategy).build()
        Nearby.getConnectionsClient(applicationContext)
            .startAdvertising("MusicDirector",SERVICE_ID, con, advertisingOptions)
            .addOnSuccessListener { Log.d("startAdvertising","Accepting User...") }
            .addOnFailureListener { throw it }
    }

    fun stopAdvertising() {
        Nearby.getConnectionsClient(applicationContext).stopAdvertising()
    }

    fun startDiscovery() {
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(strategy).build()
        Nearby.getConnectionsClient(applicationContext)
            .startDiscovery(SERVICE_ID, endpointCallback, discoveryOptions)
            .addOnSuccessListener {Log.d("startDiscovery","discovering...")}
            .addOnFailureListener { throw it }
    }

    fun sendPayload(s: String, p: Payload) {
        Nearby.getConnectionsClient(applicationContext).sendPayload(s,p)
    }

    private fun observePayloadAndEndpoints() {
        payloadCallback.payload.observe(this, Observer {
            this@ConnectionManagerService._payload.value = it
        })
        endpointCallback.endpoints.observe(this, Observer {
            this@ConnectionManagerService._endpoints.value = it
        })
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
