package com.wesync.connection

import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.SystemClock
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
import com.wesync.util.*
import com.wesync.util.ServiceUtil.Companion.SERVICE_ID
import com.wesync.util.service.ForegroundNotification
import com.wesync.util.service.ForegroundServiceLauncher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


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
    var ntpOffset : Long = 0

    private val _payload                               = MutableLiveData<Payload>()
        val payload: LiveData<Payload>                     = _payload //INI YANG DITERIMA, BUKAN YANG DIKIRIM
    private val _payloadSender                         = MutableLiveData<String>()
    private val _foundSessions                         = MutableLiveData<MutableList<DiscoveredEndpoint>>()
        val foundSessions: LiveData<MutableList<DiscoveredEndpoint>> = _foundSessions
    private val _connectedEndpointId                   = MutableLiveData<String>("")
        val connectedEndpointId:LiveData<String>           = _connectedEndpointId
    private val _connectionStatus                      = MutableLiveData<Int>()
        val connectionStatus:LiveData<Int>                 = _connectionStatus
    private val _connectedSlaves = MutableLiveData<MutableMap<String,ReceivedEndpoint>>()
    private val _latencyMap = mutableMapOf<String, Long>()
    private val _leaveMap = mutableMapOf<String, Long>()

    private val _isDiscovering = MutableLiveData<Boolean>(false)
        val isDiscovering: LiveData<Boolean>  = _isDiscovering

    private val _preStartLatency = MutableLiveData<Long>()
        val preStartLatency: LiveData<Long> = _preStartLatency

    private val _pingTestLeaveTimes = mutableMapOf<String, Array<Long>>()

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
    override fun onDestroy() {
        stopSelf()
        super.onDestroy()
    }


    private fun unpackPingPayload(payload: Payload) {
        val b = payload.asBytes()!!
        val time = ByteArrayEncoderDecoder.decodeTimestamp(b)
        var currTime = getCurrentTimeWithOffset() % ByteArrayEncoderDecoder.TWO_14
        when (b[0]) {
            PayloadType.PING -> {
                // SLAVE balas ke HOST
                if (time > currTime) currTime += ByteArrayEncoderDecoder.TWO_14
                val hostToHereTime = currTime - time
                sendTimestampedByteArray(hostToHereTime,PayloadType.PING_RESPONSE)
        }
            PayloadType.PING_RESPONSE -> {
            // DITERIMA oleh HOST
            //HOST mencatat berapa pingnya?
            _latencyMap[_payloadSender.value!!] = getCurrentTimeWithOffset() - _leaveMap[_payloadSender.value!!]!!
            Log.d("leave_response","Received response from ${_payloadSender.value}. Latency = ${_latencyMap[_payloadSender.value!!]} ")
            var longest: Long = 0
            val longestAddress = _payloadSender.value!!
            for (i in _latencyMap) {
                if (i.value > longest) longest = i.value }
            for (i in _latencyMap) {
                if (i.key != longestAddress)
                    sendTimestampedByteArray((longest - i.value)/2, PayloadType.PING_PRE_START_LATENCY,to = i.key)
                else
                    sendTimestampedByteArray(0,PayloadType.PING_PRE_START_LATENCY,i.key)
            }
            _preStartLatency.value = longest
        }
            PayloadType.PING_PRE_START_LATENCY -> {
                //slave terima ini dari HOST
                // ganti ini jadi waktu preStartLatency.
                // makeSure viewModel tau tentang berapa preStartLatencynya.
                // Karena akan dipakai oleh MetronomeService
                _preStartLatency.value = time
                Log.d("rec_pre_start","prestart = $time")
                _connectionStatus.value = ConnectionStatus.CONNECTED
            }
            PayloadType.PING_EXP -> {
                /**
                    implemented FOR TESTING PURPOSES
                 */
                Log.d("response_exp","this is test #$time")
                sendTimestampedByteArray(time = time,type = PayloadType.PING_RESPONSE_EXP)
            }
            PayloadType.PING_RESPONSE_EXP -> {
                _pingTestLeaveTimes[_payloadSender.value]!![time.toInt()] = getCurrentTimeWithOffset() - _pingTestLeaveTimes[_payloadSender.value]!![time.toInt()]
                val p = _pingTestLeaveTimes[_payloadSender.value]!![time.toInt()]
                Log.d("leave_response_exp","${time.toInt()}:${_payloadSender.value}:$p")
            }
        }

    }
    private fun getCurrentTimeWithOffset(): Long {
        return System.currentTimeMillis() + ntpOffset
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
    private fun observePayloadEndpointsAndCallbacks() {
        payloadCallback.payload.observe(this, Observer {
            this@ConnectionManagerService._payload.value = it
        })
        payloadCallback.payloadSender.observe(this, Observer {
            this@ConnectionManagerService._payloadSender.value = it
            unpackPingPayload(_payload.value!!)
        })
        endpointCallback.sessions.observe(this, Observer {
            this@ConnectionManagerService._foundSessions.value = it
        })
        connectionCallback.connectedSessionId.observe(this, Observer {
            this@ConnectionManagerService._connectedEndpointId.value = it
        })
        connectionCallback.connectionStatus.observe(this, Observer {
            this@ConnectionManagerService._connectionStatus.value = it
        })
        advertiserConnectionCallback.connectedSlaves.observe(this, Observer {
            this@ConnectionManagerService._connectedSlaves.value = it
            if (it.isNotEmpty()) {
                sendTimestampedByteArray(type = PayloadType.PING)
                for (i in it) {
                    _pingTestLeaveTimes[i.key] = Array(PayloadSizes.SIZE_OF_TEST){ 0.toLong() }
                }
            }
        })
    }


    fun sendTimestampedByteArray(time: Long? = 0, type: Byte, to: String? = null, whichTest: Int = 0) {
        when (type) {
            PayloadType.PING -> {
                for (endpoint in _connectedSlaves.value!!) {
                    sendByteArray(endpoint.key,ByteArrayEncoderDecoder.encodeTimestampByteArray(getCurrentTimeWithOffset(),type))
                    //catet waktu untuk slave itu
                    _leaveMap[endpoint.key] = getCurrentTimeWithOffset()
                    Log.d("leave_time","send to ${endpoint.key}. LeaveTime = ${_leaveMap[endpoint.key]}")
                }
            }
            PayloadType.PING_RESPONSE -> {
                if (userType == UserTypes.SLAVE) {
                    if (time != null && time > 0)
                        sendByteArray(_connectedEndpointId.value!!,
                            ByteArrayEncoderDecoder
                                .encodeTimestampByteArray(time,type))
                }
            }
            PayloadType.PING_PRE_START_LATENCY -> {
                if (time != null && time > 0 && to != null)
                this.sendByteArray(to,ByteArrayEncoderDecoder
                    .encodeTimestampByteArray(time,type))

            }
            PayloadType.PING_EXP -> {
                for (endpoint in _connectedSlaves.value!!) {
                    sendByteArray(endpoint.key,ByteArrayEncoderDecoder.encodeTimestampByteArray(whichTest.toLong(),type))
                    //catet waktu untuk slave itu
                    _pingTestLeaveTimes[endpoint.key]!![whichTest] = getCurrentTimeWithOffset()
                    Log.d("leave_time_exp","$whichTest:${_pingTestLeaveTimes[endpoint.key]!![whichTest]}")
                }
            }
            PayloadType.PING_RESPONSE_EXP -> {
                sendByteArray(_connectedEndpointId.value!!,
                    ByteArrayEncoderDecoder
                        .encodeTimestampByteArray(time!!,PayloadType.PING_RESPONSE_EXP))
            }
        }
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
        Log.d("startDiscovery","DISCOVERING")
        if (TestMode.STATUS == TestMode.NEARBY_ON) {
            val discoveryOptions = DiscoveryOptions.Builder().setStrategy(strategy).build()
            if (_foundSessions.value!!.size > 0) _foundSessions.value = mutableListOf()
            Nearby.getConnectionsClient(applicationContext)
                .startDiscovery(SERVICE_ID, endpointCallback, discoveryOptions)
                .addOnSuccessListener {
                    Toast.makeText(this, "Finding nearby session...", Toast.LENGTH_SHORT).show()
                    _isDiscovering.value = true
                }
                .addOnFailureListener {
                    _isDiscovering.value = false
                    throw it
                }
        }
    }
    fun stopDiscovery() {
        if (TestMode.STATUS == TestMode.NEARBY_ON) {
            _isDiscovering.value = false
            Nearby.getConnectionsClient(applicationContext).stopDiscovery()
        }
    }
    fun sendByteArrayToAll(b: ByteArray) {
        if (userType == UserTypes.SESSION_HOST)
            for (endpoint in _connectedSlaves.value!!) {
            sendByteArray(endpoint.key,b)
        }
    }
    fun connect(discoveredEndpoint: DiscoveredEndpoint, name: String) {
        if (TestMode.STATUS == TestMode.NEARBY_ON) {
            Nearby.getConnectionsClient(application)
                .requestConnection(name, discoveredEndpoint.endpointId, connectionCallback)
                .addOnSuccessListener { Toast.makeText(applicationContext,
                    "Connecting to ${discoveredEndpoint.info.endpointName} (${discoveredEndpoint.endpointId})", Toast.LENGTH_SHORT).show()
                    _connectionStatus.value = ConnectionStatus.CONNECTING
                }
                .addOnFailureListener { Toast.makeText(applicationContext,
                        "Failed to request connection to ${discoveredEndpoint.info.endpointName} " +
                                "(${discoveredEndpoint.endpointId})", Toast.LENGTH_SHORT).show()
                    _connectionStatus.value = ConnectionStatus.DISCONNECTED
                }
        }
    }
    fun disconnect() {
        Nearby.getConnectionsClient(application).stopAllEndpoints()
        userType = UserTypes.SOLO
        _preStartLatency.value = 0
    }
    fun pingTest() {
        GlobalScope.launch {
            for (i in 0 until PayloadSizes.SIZE_OF_TEST) {
                sendTimestampedByteArray(whichTest = i, type = PayloadType.PING_EXP)
                delay(30)
            }
        }
    }

}
