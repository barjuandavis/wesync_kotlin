package com.wesync.connection

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.google.android.gms.nearby.Nearby
import android.util.Log
import androidx.lifecycle.LifecycleService
import com.google.android.gms.nearby.connection.*
import com.wesync.connection.callbacks.*


class ConnectionManagerService : LifecycleService() {

    private val _binder = LocalBinder()
    private val strategy: Strategy = Strategy.P2P_STAR
    private val SERVICE_ID = "com.wesync"
    private val payloadCallback = MyPayloadCallback() //responsible for receiving payload
    private val connectionLifecycleCallback =
        MyConnectionLifecycleCallback(
            applicationContext,
            payloadCallback
        )
    private val endpointDiscoveryCallback = MyEndpointCallback(
        applicationContext,
        connectionLifecycleCallback
    )


    inner class LocalBinder : Binder() {
        fun getService() : ConnectionManagerService {
            return this@ConnectionManagerService
        }
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return _binder
    }

    fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(strategy).build()
        Nearby.getConnectionsClient(applicationContext)
            .startAdvertising(
                "MusicDirector",SERVICE_ID, connectionLifecycleCallback, advertisingOptions
            )
            .addOnSuccessListener {
               Log.d("startAdvertising","advertising...")
            }
            .addOnFailureListener {

            }
    }
    fun startDiscovery() {
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(strategy).build()
        Nearby.getConnectionsClient(applicationContext)
            .startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)

            .addOnSuccessListener {
                Log.d("startDiscovery","discovering...")
            }

            .addOnFailureListener {
                // We're unable to start discovering.
            }

    }

    fun sendPayload(s: String, p: Payload) {
        Nearby.getConnectionsClient(applicationContext).sendPayload(s,p)
    }
    fun getPayload(): Payload? {
        return payloadCallback.payload
    }




}
