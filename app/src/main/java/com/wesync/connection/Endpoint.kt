package com.wesync.connection

import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo

data class Endpoint(val endpointId: String, val info: DiscoveredEndpointInfo)