package com.wesync.connection.callbacks

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.wesync.connection.ReceivedEndpoint
import com.wesync.util.ConnectionStatus

class SessionConnectionLifecycleCallback(
    private val context: Context,
    private val pay: MyPayloadCallback): ConnectionLifecycleCallback() {

    private val list = mutableListOf<ReceivedEndpoint>()
    private val _connectedSlaves = MutableLiveData<MutableList<ReceivedEndpoint>>(list)
        val connectedSlaves: LiveData<MutableList<ReceivedEndpoint>> =  _connectedSlaves

    private fun updateList() {
        _connectedSlaves.value = list
    }


    override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
        Nearby.getConnectionsClient(context).acceptConnection(endpointId, pay)
            list.add(ReceivedEndpoint(endpointId,info))
    }

    override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
        when (result.status.statusCode) {
            ConnectionsStatusCodes.STATUS_OK -> {
                Toast.makeText(context, "${endpointId} has joined your session!",Toast.LENGTH_SHORT).show()
                updateList()
            } // We're connected! Can now start sending and receiving data.
            ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                Toast.makeText(context, "You or $endpointId rejected the connection :(",Toast.LENGTH_SHORT).show()

            }
            ConnectionsStatusCodes.STATUS_ERROR -> { Toast.makeText(context, "${endpointId} has failed to join.",Toast.LENGTH_SHORT).show()} // The connection broke before it was able to be accepted.
        }
    }

    override fun onDisconnected(p0: String) {

    }
}