package com.wesync.connection.callbacks

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes

class MyConnectionLifecycleCallback(private val context: Context,private val pay: MyPayloadCallback) : ConnectionLifecycleCallback() {
    override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
        AlertDialog.Builder(context)
            .setTitle("Accept connection to " + info.endpointName)
            .setMessage("Confirm the code matches on both devices: " + info.authenticationToken)

            .setPositiveButton("Accept") { _: DialogInterface, _: Int ->
                // The user confirmed, so we can accept the connection.
                Nearby.getConnectionsClient(context).acceptConnection(endpointId, pay)
            }
            .setNegativeButton(android.R.string.cancel) { _: DialogInterface, _: Int ->
                // The user canceled, so we should reject the connection.
                Nearby.getConnectionsClient(context).rejectConnection(endpointId)
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