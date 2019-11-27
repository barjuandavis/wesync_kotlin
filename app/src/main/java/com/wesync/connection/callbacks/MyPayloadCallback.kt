package com.wesync.connection.callbacks


import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate


class MyPayloadCallback: PayloadCallback() {

    private var _observablePayload = MutableLiveData<Payload>()
        val payload = _observablePayload
    private var _payloadSender = MutableLiveData<String>("")
        val payloadSender = _payloadSender

    override fun onPayloadReceived(sender: String, payload: Payload) {
        _observablePayload.value = payload
        _payloadSender.value = sender
    }

    override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {}
}