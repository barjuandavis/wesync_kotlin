package com.wesync.connection

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.google.android.gms.nearby.Nearby
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleService
import com.google.android.gms.nearby.connection.*
import com.wesync.connection.callbacks.*


class ConnectionManagerService : LifecycleService() {

    private val _binder = LocalBinder()
    private val strategy: Strategy = Strategy.P2P_STAR
    private val SERVICE_ID = "com.wesync"
    private val payloadCallback = MyPayloadCallback() //responsible for receiving payload
    private lateinit var con: MyConnectionLifecycleCallback
    private val endpointCallback = MyEndpointCallback()


    inner class LocalBinder : Binder() {
        fun getService() : ConnectionManagerService {
            return this@ConnectionManagerService
        }
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        con = MyConnectionLifecycleCallback(applicationContext,payloadCallback)
        return _binder
    }

    fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(strategy).build()
        Nearby.getConnectionsClient(applicationContext)
            .startAdvertising("MusicDirector",SERVICE_ID, con, advertisingOptions)
            .addOnSuccessListener { Log.d("startAdvertising","advertising...") }
            .addOnFailureListener { throw it }
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
    fun getPayload(): Payload? {
        return payloadCallback.payload
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
