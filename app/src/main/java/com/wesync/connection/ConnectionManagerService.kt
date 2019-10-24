package com.wesync.connection

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.google.android.gms.nearby.Nearby
import android.content.DialogInterface
import android.app.AlertDialog
import android.util.Log
import com.google.android.gms.nearby.connection.*


class ConnectionManagerService : Service() {

    private val _binder = LocalBinder()
    private val strategy: Strategy = Strategy.P2P_STAR

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            AlertDialog.Builder(applicationContext)
                .setTitle("Accept connection to " + info.endpointName)
                .setMessage("Confirm the code matches on both devices: " + info.authenticationToken)

                .setPositiveButton("Accept") { _: DialogInterface, _: Int ->
                    // The user confirmed, so we can accept the connection.
                    Nearby.getConnectionsClient(applicationContext).acceptConnection(endpointId, payloadCallback)
                }
                .setNegativeButton(android.R.string.cancel) { _: DialogInterface, _: Int ->
                    // The user canceled, so we should reject the connection.
                    Nearby.getConnectionsClient(applicationContext).rejectConnection(endpointId)
                }

                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }
        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> { } // We're connected! Can now start sending and receiving data.
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> { }  // The connection was rejected by one or both sides.
                ConnectionsStatusCodes.STATUS_ERROR -> { } // The connection broke before it was able to be accepted.
            }
        }
        override fun onDisconnected(endpointId: String) {
            // We've been disconnected from this endpoint. No more data can be
            // sent or received.
        }
    }
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            // An endpoint was found. We request a connection to it.
            Nearby.getConnectionsClient(applicationContext)
                .requestConnection("Slave", endpointId, connectionLifecycleCallback)
                .addOnSuccessListener {
                    // We successfully requested a connection. Now both sides
                    // must accept before the connection is established.
                }
                .addOnFailureListener {
                    // Nearby Connections failed to request the connection.
                }
        }
        override fun onEndpointLost(endpointId: String) {
            // A previously discovered endpoint has gone away.
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            // A new payload is being sent over.
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Payload progress has updated.
        }
    }

    private val SERVICE_ID = "com.wesync"

    inner class LocalBinder : Binder() {
        fun getService() : ConnectionManagerService {
            return this@ConnectionManagerService
        }
    }

    override fun onBind(intent: Intent): IBinder {
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
                // We were unable to start advertising.
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


}
