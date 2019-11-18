package com.wesync.connection

import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.widget.Toast
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.Strategy
import com.wesync.connection.callbacks.MyConnectionLifecycleCallback
import com.wesync.connection.callbacks.MyEndpointCallback
import com.wesync.connection.callbacks.MyPayloadCallback
import com.wesync.util.ConnectionStatus
import com.wesync.util.ServiceUtil.Companion.SERVICE_ID
import com.wesync.util.TestMode
import com.wesync.util.UserTypes
import com.wesync.util.service.ForegroundNotification
import com.wesync.util.service.ForegroundServiceLauncher


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
        var userType                        = UserTypes.SOLO
        var userName                               = ""
    private lateinit var connectionCallback      : MyConnectionLifecycleCallback
    private var currentByteArray  = ByteArray(5) {0}


    private val _payload                               = MutableLiveData<Payload>()
        val payload: LiveData<Payload>                     = _payload //INI YANG DITERIMA, BUKAN YANG DIKIRIM
    private val _foundSessions                         = MutableLiveData<MutableList<DiscoveredEndpoint>>()
        val foundSessions: LiveData<MutableList<DiscoveredEndpoint>> = _foundSessions
    private val _connectedEndpointId                   = MutableLiveData<String>(null)
        val connectedEndpointId:LiveData<String>           = _connectedEndpointId
    private val _connectionStatus                      = MutableLiveData<Int>()
        val connectionStatus:LiveData<Int>                 = _connectionStatus


    private fun setConnectionStatus(i:Int) {_connectionStatus.value = i}

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
        endpointCallback.sessions.observe(this, Observer {
            this@ConnectionManagerService._foundSessions.value = it})
        connectionCallback.connectedSessionId.observe(this, Observer {
            this@ConnectionManagerService._connectedEndpointId.value = it})
        connectionCallback.connectionStatus.observe(this, Observer {
            this@ConnectionManagerService._connectionStatus.value = it })
    }

    override fun onDestroy() {
        stopSelf()
        super.onDestroy()
    }


    fun startAdvertising() {
        if (TestMode.STATUS == TestMode.NEARBY_ON && userType == UserTypes.SESSION_HOST) {
            val advertisingOptions = AdvertisingOptions.Builder().setStrategy(strategy).build()
            Nearby.getConnectionsClient(applicationContext)
                .startAdvertising(userName,SERVICE_ID, connectionCallback, advertisingOptions)
                .addOnSuccessListener { Toast.makeText(this, "Accepting User...",Toast.LENGTH_SHORT).show() }
                .addOnFailureListener { throw it }
        }
    }

    fun stopAdvertising() {
        if (TestMode.STATUS == TestMode.NEARBY_ON)
            Nearby.getConnectionsClient(applicationContext).stopAdvertising()
    }

    fun startDiscovery() {
        if (TestMode.STATUS == TestMode.NEARBY_ON && userType == UserTypes.SLAVE) {
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

    fun setConfig(bpm: Long, isPlaying: Boolean) {
        val bins:Int = (bpm/100).toInt() - 1
        for (i in 0..2){
            if (i <= bins) {
                currentByteArray[i] = 100
            }
        }
        currentByteArray[3] = (bpm % 100).toByte()
        if (isPlaying) currentByteArray[4] = 1
            else currentByteArray[4] = 0
    }


    /*
    * TODO: untuk 19/11/2019
    *   Bikin MutableLiveData<MutableMap<ReceivedEndpoint>> jadi Service tau mau sendPayload kemana.
    *   connectedSessionId untuk slave hanya digunakan unutk
    * */

    fun sendPayload(toEndpointId: String) {
        if (TestMode.STATUS == TestMode.NEARBY_ON)
            Nearby.getConnectionsClient(applicationContext)
                .sendPayload(toEndpointId,Payload.fromBytes(currentByteArray))
    }

    fun connect(discoveredEndpoint: DiscoveredEndpoint, name: String) {
        if (TestMode.STATUS == TestMode.NEARBY_ON) {
            Nearby.getConnectionsClient(application)
                .requestConnection(name, discoveredEndpoint.endpointId, connectionCallback)
                .addOnSuccessListener { Toast.makeText(applicationContext,
                    "Connecting to ${discoveredEndpoint.endpointId}", Toast.LENGTH_SHORT).show()
                setConnectionStatus(ConnectionStatus.CONNECTING)
                }
                .addOnFailureListener { Toast.makeText(applicationContext,
                        "Failed to request connection to ${discoveredEndpoint.endpointId}", Toast.LENGTH_SHORT).show() }
        }
    }

    fun disconnect() {
        Nearby.getConnectionsClient(application).stopAllEndpoints()
        userType = UserTypes.SOLO
    }

}
