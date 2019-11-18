package com.wesync.connection

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.google.android.gms.nearby.Nearby
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.nearby.connection.*
import com.wesync.MainActivity
import com.wesync.R
import com.wesync.connection.callbacks.*
import com.wesync.util.service.ForegroundServiceLauncher
import com.wesync.util.ServiceUtil.Companion.SERVICE_ID
import com.wesync.util.TestMode
import com.wesync.util.service.ForegroundNotification


class ConnectionManagerService : LifecycleService() {

    companion object {
        private val LAUNCHER =
            ForegroundServiceLauncher(ConnectionManagerService::class.java)
        @JvmStatic
        fun start(context: Context) = LAUNCHER.startService(context)
        @JvmStatic
        fun stop(context: Context) = LAUNCHER.stopService(context)
    }

    private val _binder                         = LocalBinder()
    private val strategy: Strategy              = Strategy.P2P_STAR
    private val payloadCallback                 = MyPayloadCallback()
    private val endpointCallback                = MyEndpointCallback()
    private lateinit var connectionCallback      : MyConnectionLifecycleCallback
    // callback listeners, distributed to fragments
    private val _connectionStatus                      = MutableLiveData<Int>()
        val connectionStatus:LiveData<Int>             = _connectionStatus
    private val _endpoints                             = MutableLiveData<MutableList<Endpoint>>()
        val endpoints: LiveData<MutableList<Endpoint>> = _endpoints
    private val _payload                               = MutableLiveData<Payload>()
        val payload: LiveData<Payload>                 = _payload
    private val _connectedEndpointId                   = MutableLiveData<String>(null)
        val connectedEndpointId:LiveData<String>           = _connectedEndpointId
    // END OF LISTENERS

    inner class LocalBinder : Binder() {
        fun getService() : ConnectionManagerService {
            return this@ConnectionManagerService
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startForeground(
            ForegroundNotification.NOTIFICATION_ID,
            ForegroundNotification.getNotification(this))
        LAUNCHER.onServiceCreated(this)
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        connectionCallback = MyConnectionLifecycleCallback(
            applicationContext,payloadCallback)
        observePayloadAndEndpoints()
        super.onBind(intent)
        return this._binder
    }

    private fun observePayloadAndEndpoints() {
        payloadCallback.payload.observe(this , Observer {
            this@ConnectionManagerService._payload.value = it})
        endpointCallback.endpoints.observe(this, Observer {
            this@ConnectionManagerService._endpoints.value = it})
        connectionCallback.connectedEndpointId.observe(this, Observer {
            this@ConnectionManagerService._connectedEndpointId.value = it})
        connectionCallback.connectionStatus.observe(this, Observer {
            this@ConnectionManagerService._connectionStatus.value = it})
        //_endpoints.value = mockListFORTESTINGPURPOSES()
    }

    override fun onDestroy() {
        stopSelf()
        super.onDestroy()
    }


    fun startAdvertising(sessionName: String?) {
        if (TestMode.STATUS == TestMode.NEARBY_ON) {
            val advertisingOptions = AdvertisingOptions.Builder().setStrategy(strategy).build()
            Nearby.getConnectionsClient(applicationContext)
                .startAdvertising(sessionName!!,SERVICE_ID, connectionCallback, advertisingOptions)
                .addOnSuccessListener { Toast.makeText(this, "Accepting User...",Toast.LENGTH_SHORT).show() }
                .addOnFailureListener { throw it }
        }
    }

    fun stopAdvertising() {
        if (TestMode.STATUS == TestMode.NEARBY_ON)
            Nearby.getConnectionsClient(applicationContext).stopAdvertising()
    }

    fun startDiscovery() {
        if (TestMode.STATUS == TestMode.NEARBY_ON) {
            val discoveryOptions = DiscoveryOptions.Builder().setStrategy(strategy).build()
            Nearby.getConnectionsClient(applicationContext)
                .startDiscovery(SERVICE_ID, endpointCallback, discoveryOptions)
                .addOnSuccessListener { Toast.makeText(this, "Finding nearby session...", Toast.LENGTH_SHORT).show()}
                .addOnFailureListener { throw it }
        }
    }

    fun stopDiscovering() {
        if (TestMode.STATUS == TestMode.NEARBY_ON)
            Nearby.getConnectionsClient(applicationContext).stopDiscovery()
    }

    fun sendPayload(s: String, p: Payload) {
        if (TestMode.STATUS == TestMode.NEARBY_ON) Nearby.getConnectionsClient(applicationContext).sendPayload(s,p)
    }

    fun connect(endpoint: Endpoint, name: String) {
        if (TestMode.STATUS == TestMode.NEARBY_ON) {
            Nearby.getConnectionsClient(application)
                .requestConnection(name, endpoint.endpointId, connectionCallback)
                .addOnSuccessListener { Toast.makeText(applicationContext,
                    "Connecting to ${endpoint.endpointId}", Toast.LENGTH_SHORT).show() }
                .addOnFailureListener { Toast.makeText(applicationContext,
                        "Failed to request connection to ${endpoint.endpointId}",Toast.LENGTH_SHORT).show() }
        }
    }

    fun disconnect() {
        Nearby.getConnectionsClient(application).stopAllEndpoints()
    }

}
