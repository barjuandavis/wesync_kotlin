package com.wesync.connection.callbacks

import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate


class MyPayloadCallback: PayloadCallback() {
    private var _payload: Payload? = null
        val payload = _payload
    override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {
        if (p1.status == PayloadTransferUpdate.Status.FAILURE) {
            //TODO: do something to inform the Host?
        }
    }
    override fun onPayloadReceived(p0: String, p1: Payload) {
        _payload = p1
    }
}