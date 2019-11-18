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
import com.wesync.util.ConnectionStatus

open class MyConnectionLifecycleCallback(
    private val context: Context,
    private val pay: MyPayloadCallback) : ConnectionLifecycleCallback() {

    private val _connectedSessionId = MutableLiveData<String>(null)
        val connectedSessionId:LiveData<String> = _connectedSessionId

    private val _connectionStatus = MutableLiveData(ConnectionStatus.DISCONNECTED)
        val connectionStatus: LiveData<Int> = _connectionStatus

    override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
        Toast.makeText(context, "Initiating connection to ${endpointId}...",Toast.LENGTH_SHORT).show()
        _connectionStatus.value = ConnectionStatus.CONNECTING
        Nearby.getConnectionsClient(context).acceptConnection(endpointId, pay)
    }
    override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
        when (result.status.statusCode) {
            ConnectionsStatusCodes.STATUS_OK -> {
                Toast.makeText(context, "You are connected to ${endpointId}!",Toast.LENGTH_SHORT).show()
                _connectedSessionId.value = endpointId
                _connectionStatus.value = ConnectionStatus.CONNECTED
            } // We're connected! Can now start sending and receiving data.
            ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                Toast.makeText(context, "You or $endpointId rejected the connection :(",Toast.LENGTH_SHORT).show()}  // The connection was rejected by one or both sides.
            ConnectionsStatusCodes.STATUS_ERROR -> { Toast.makeText(context, "Failed to connect to ${endpointId} :(",Toast.LENGTH_SHORT).show()} // The connection broke before it was able to be accepted.
        }
    }
    override fun onDisconnected(endpointId: String) {
        Toast.makeText(context, "You are disconnected from ${endpointId}!",Toast.LENGTH_SHORT).show()
        _connectedSessionId.value = null
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
    }


}