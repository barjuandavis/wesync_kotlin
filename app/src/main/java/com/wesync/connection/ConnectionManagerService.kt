package com.wesync.connection

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.nearby.connection.*
import com.wesync.MainActivity
import com.wesync.R
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
    val payloadCallback = MyPayloadCallback()
    private val CHANNEL_ID = "wesync_notification_bar"
    private lateinit var con: MyConnectionLifecycleCallback
    private var _advertising: Boolean = false
    private var _discovering: Boolean = false

    val endpointCallback = MyEndpointCallback()

    private val _endpoints = MutableLiveData<MutableList<Endpoint>>() //TODO: observed by ConnectionFragment
        val endpoints = _endpoints
    private val _payload = MutableLiveData<Payload>() //TODO: observed by MetronomeFragment
        val payload = _payload


    inner class LocalBinder : Binder() {
        fun getService() : ConnectionManagerService {
            return this@ConnectionManagerService
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Wesync Metronome Connection")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent).setPriority(PRIORITY_MIN)
            .build()
        startForeground(2, notification)
        LAUNCHER.onServiceCreated(this)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Wesync Notification Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager: NotificationManager? = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent): IBinder {
        con = MyConnectionLifecycleCallback(this.baseContext,payloadCallback)
        observePayloadAndEndpoints()
        super.onBind(intent)
        return _binder
    }


    fun startAdvertising(sessionName: String?) {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(strategy).build()
        Nearby.getConnectionsClient(applicationContext)
            .startAdvertising(sessionName!!
                ,SERVICE_ID, con, advertisingOptions)
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
        payloadCallback.payload.observe(this , Observer {this@ConnectionManagerService._payload.value = it})
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

    fun connect(endpoint: Endpoint) { //placeholder
        Nearby.getConnectionsClient(application).requestConnection("Slave", endpoint.endpointId, con)
            .addOnSuccessListener {
                Toast.makeText(applicationContext,"Connecting to ${endpoint.endpointId}",
                    Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext,"Failed to request connection to ${endpoint.endpointId}",
                    Toast.LENGTH_SHORT).show()
            }
    }
}
