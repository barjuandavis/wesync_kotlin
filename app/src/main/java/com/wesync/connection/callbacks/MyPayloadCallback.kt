package com.wesync.connection.callbacks


import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate


class MyPayloadCallback: PayloadCallback() {

    private var _payload: Payload? = null
        val payload = _payload

    override fun onPayloadReceived(p0: String, p1: Payload) {
        _payload = p1
    }

    override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}