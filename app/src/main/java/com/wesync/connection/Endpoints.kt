package com.wesync.connection

import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo

data class DiscoveredEndpoint(val endpointId: String, val info: DiscoveredEndpointInfo)
data class ReceivedEndpoint(val endpointId: String, val info: ConnectionInfo)