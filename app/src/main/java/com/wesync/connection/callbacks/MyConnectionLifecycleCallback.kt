package com.wesync.connection.callbacks

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes

class MyConnectionLifecycleCallback(
    private val context: Context,
    private val pay: MyPayloadCallback) : ConnectionLifecycleCallback() {

    private val _connectedEndpointId = MutableLiveData<String?>(null)
        val connectedEndpointId = _connectedEndpointId

    override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
        /*AlertDialog.Builder(context)
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
            .setIcon(android.R.drawable.ic_dialog_info)
            .show()

         */
        Toast.makeText(context, "Initiating connection to ${endpointId}...",Toast.LENGTH_SHORT).show()
    }
    override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
        when (result.status.statusCode) {
            ConnectionsStatusCodes.STATUS_OK -> {
                Toast.makeText(context, "You are connected to ${endpointId}!",Toast.LENGTH_SHORT).show()
                _connectedEndpointId.value = endpointId
            } // We're connected! Can now start sending and receiving data.
            ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                Toast.makeText(context, "You or $endpointId rejected the connection :(",Toast.LENGTH_SHORT).show()}  // The connection was rejected by one or both sides.
            ConnectionsStatusCodes.STATUS_ERROR -> { Toast.makeText(context, "Failed to connect to ${endpointId} :(",Toast.LENGTH_SHORT).show()} // The connection broke before it was able to be accepted.
        }
    }
    override fun onDisconnected(endpointId: String) {
        Toast.makeText(context, "You are disconnected from ${endpointId}!",Toast.LENGTH_SHORT).show()
        _connectedEndpointId.value = null
    }


}