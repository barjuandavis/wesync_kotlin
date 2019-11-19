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


    private val map = mutableMapOf<String,ReceivedEndpoint>()
    private val statusMap = mutableMapOf<String,Int>()
    private val _connectedSlaves = MutableLiveData<MutableMap<String,ReceivedEndpoint>>(map)
        val connectedSlaves: LiveData<MutableMap<String,ReceivedEndpoint>> =  _connectedSlaves

    private fun updateList() {
        _connectedSlaves.value = map
    }

    override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
        Nearby.getConnectionsClient(context).acceptConnection(endpointId, pay)
        map[endpointId] = ReceivedEndpoint(endpointId,info)
        statusMap[endpointId] = ConnectionStatus.CONNECTING
    }

    override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
        if (result.status.statusCode ==
            ConnectionsStatusCodes.STATUS_OK)  {
                Toast.makeText(context,
                    "${map[endpointId]?.info?.endpointName} ($endpointId) has joined your session!",Toast.LENGTH_SHORT).show()
                statusMap[endpointId] = ConnectionStatus.CONNECTED
                updateList()
            }
            else {
                statusMap[endpointId] = ConnectionStatus.DISCONNECTED
                map.remove(endpointId)
                updateList()
            }
    }

    override fun onDisconnected(endpointId: String) {
        statusMap[endpointId] = ConnectionStatus.DISCONNECTED
        map.remove(endpointId)
        updateList()
    }
}