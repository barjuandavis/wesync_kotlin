package com.wesync.connection

import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.wesync.connection.callbacks.MyConnectionLifecycleCallback
import com.wesync.connection.callbacks.MyEndpointCallback
import com.wesync.connection.callbacks.MyPayloadCallback
import com.wesync.connection.callbacks.SessionConnectionLifecycleCallback
import com.wesync.util.ServiceUtil.Companion.SERVICE_ID
import com.wesync.util.Tempo
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
    private lateinit var advertiserConnectionCallback : SessionConnectionLifecycleCallback
    var offset : Long = 0



    private val _payload                               = MutableLiveData<Payload>()
        val payload: LiveData<Payload>                     = _payload //INI YANG DITERIMA, BUKAN YANG DIKIRIM
    private val _foundSessions                         = MutableLiveData<MutableList<DiscoveredEndpoint>>()
        val foundSessions: LiveData<MutableList<DiscoveredEndpoint>> = _foundSessions
    private val _connectedEndpointId                   = MutableLiveData<String>(null)
        val connectedEndpointId:LiveData<String>           = _connectedEndpointId
    private val _connectionStatus                      = MutableLiveData<Int>()
        val connectionStatus:LiveData<Int>                 = _connectionStatus
    private val _connectedSlaves = MutableLiveData<MutableMap<String,ReceivedEndpoint>>()
    private val _latencyMap = mutableMapOf<String, Long>()


    inner class LocalBinder : Binder() {
        fun getService() : ConnectionManagerService {
            return this@ConnectionManagerService
        }
    }

    fun mockList(): MutableList<DiscoveredEndpoint> {
        val l = mutableListOf<DiscoveredEndpoint>()
        l.add(DiscoveredEndpoint("test1", DiscoveredEndpointInfo("test1","test1")))
        l.add(DiscoveredEndpoint("test2", DiscoveredEndpointInfo("test1","test1")))
        l.add(DiscoveredEndpoint("test3", DiscoveredEndpointInfo("test1","test1")))
        l.add(DiscoveredEndpoint("test4", DiscoveredEndpointInfo("test1","test1")))
        l.add(DiscoveredEndpoint("test5", DiscoveredEndpointInfo("test1","test1")))
        l.add(DiscoveredEndpoint("test6", DiscoveredEndpointInfo("test1","test1")))
        l.add(DiscoveredEndpoint("test7", DiscoveredEndpointInfo("test1","test1")))
        l.add(DiscoveredEndpoint("test8", DiscoveredEndpointInfo("test1","test1")))
        l.add(DiscoveredEndpoint("test9", DiscoveredEndpointInfo("test1","test1")))
        l.add(DiscoveredEndpoint("test10", DiscoveredEndpointInfo("test1","test1")))
        return l
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
        advertiserConnectionCallback = SessionConnectionLifecycleCallback(
            applicationContext,payloadCallback)
        observePayloadEndpointsAndCallbacks()
        super.onBind(intent)
        return this._binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("_con","connectionmanagerservice DISCONNECTED")
        if (userType == UserTypes.SOLO) {
            stopSelf()
        }
        return super.onUnbind(intent)
    }

    private fun packPingPayload(): Payload {
        val b = ByteArray(7) {1}
        val time = getCurrentTimeWithOffset() % 10000
        val timeString = time.toString(2)
        var timeString18 = timeString
        if (timeString.length < 18) {
            timeString18 = timeString.padStart(18)
        }
        val arr = Array(7) {""}
        arr[0] = timeString18.substring(0..6)
        arr[1] = timeString18.substring(7..13)
        arr[2] = timeString18.substring(14..17)

        b[0] = arr[0].toByte(2)
        b[0] = arr[1].toByte(2)
        b[0] = arr[2].toByte(2)

        return Payload.fromBytes(b)
    }
    private fun unpackPingPayload(payload: Payload) {
        val b = payload.asBytes()!!
        if (b[6] == 1.toByte()) {
            val b0 = b[0].toString(2)
            val b1 = b[1].toString(2)
            val b2 = b[2].toString(2)
            val advertiserTimestampString = b0 + b1 + b2
            val advertiserTimestamp = advertiserTimestampString.toLong(2)



        }
    }

    private fun getCurrentTimeWithOffset(): Long {
        return System.currentTimeMillis() + offset
    }

    private fun observePayloadEndpointsAndCallbacks() {
        payloadCallback.payload.observe(this , Observer {
            this@ConnectionManagerService._payload.value = it
            unpackPingPayload(it)
        })
        endpointCallback.sessions.observe(this, Observer {
            this@ConnectionManagerService._foundSessions.value = it
            Log.d("onEndpointFound","DiscoveredEndpoint added. List in ConnectionManagerService updated")})

        connectionCallback.connectedSessionId.observe(this, Observer {
            this@ConnectionManagerService._connectedEndpointId.value = it})
        connectionCallback.connectionStatus.observe(this, Observer {
            this@ConnectionManagerService._connectionStatus.value = it })

        advertiserConnectionCallback.connectedSlaves.observe(this, Observer {
            this@ConnectionManagerService._connectedSlaves.value = it
            for (slave in it) {
                sendPayload(slave.key,packPingPayload())
            }
        })
    }

    override fun onDestroy() {
        stopSelf()
        super.onDestroy()
    }
    fun startAdvertising() {
        if (TestMode.STATUS == TestMode.NEARBY_ON) {
            val advertisingOptions = AdvertisingOptions.Builder().setStrategy(strategy).build()
            Nearby.getConnectionsClient(applicationContext)
                .startAdvertising(userName,SERVICE_ID, advertiserConnectionCallback, advertisingOptions)
                .addOnSuccessListener { Toast.makeText(this, "Accepting User...",Toast.LENGTH_SHORT).show() }
                .addOnFailureListener { throw it }
        }
    }
    fun stopAdvertising() {
        if (TestMode.STATUS == TestMode.NEARBY_ON)
            Nearby.getConnectionsClient(applicationContext).stopAdvertising()
    }
    fun startDiscovery() {
        Log.d("startDiscovery","DISCOVERUING")
        if (TestMode.STATUS == TestMode.NEARBY_ON) {
            val discoveryOptions = DiscoveryOptions.Builder().setStrategy(strategy).build()
            Nearby.getConnectionsClient(applicationContext)
                .startDiscovery(SERVICE_ID, endpointCallback, discoveryOptions)
                .addOnSuccessListener { Toast.makeText(this, "Finding nearby session...", Toast.LENGTH_SHORT).show()}
                .addOnFailureListener { throw it }
        }
    }
    fun stopDiscovery() {
        if (TestMode.STATUS == TestMode.NEARBY_ON)
            Nearby.getConnectionsClient(applicationContext).stopDiscovery()
    }


    fun sendByteArray(b: ByteArray) {
        for (endpoint in _connectedSlaves.value!!) {
            sendByteArray(endpoint.key,b)
        }
    }

    private fun sendByteArray(toEndpointId: String, b: ByteArray) {
        sendPayload(toEndpointId, Payload.fromBytes(b))
    }

    private fun sendPayload(toEndpointId: String,payload: Payload) {
        if (TestMode.STATUS == TestMode.NEARBY_ON) {
            Nearby.getConnectionsClient(applicationContext)
                .sendPayload(toEndpointId, payload)
        }
    }

    fun connect(discoveredEndpoint: DiscoveredEndpoint, name: String) {
        if (TestMode.STATUS == TestMode.NEARBY_ON) {
            Nearby.getConnectionsClient(application)
                .requestConnection(name, discoveredEndpoint.endpointId, connectionCallback)
                .addOnSuccessListener { Toast.makeText(applicationContext,
                    "Connecting to ${discoveredEndpoint.endpointId}", Toast.LENGTH_SHORT).show() }
                .addOnFailureListener { Toast.makeText(applicationContext,
                        "Failed to request connection to ${discoveredEndpoint.info.endpointName} " +
                                "(${discoveredEndpoint.endpointId})", Toast.LENGTH_SHORT).show() }
        }
    }

    fun disconnect() {
        Nearby.getConnectionsClient(application).stopAllEndpoints()
        userType = UserTypes.SOLO
    }

}
