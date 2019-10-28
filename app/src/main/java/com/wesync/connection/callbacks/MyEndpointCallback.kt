package com.wesync.connection.callbacks

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback

class MyEndpointCallback(private val context: Context,
                         private val con: MyConnectionLifecycleCallback
)
    : EndpointDiscoveryCallback() {

    override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
        // An endpoint was found. We request a connection to it.
        Nearby.getConnectionsClient(context)
            .requestConnection("Slave", endpointId, con)
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