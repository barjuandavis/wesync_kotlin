package com.wesync.ntp

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.apache.commons.net.ntp.NTPUDPClient
import java.net.InetAddress
import java.net.SocketException

class NTPClientWorker(val context: Context,val params: WorkerParameters)
    : Worker(context, params) {
    private var offset: Long         = 0
    private var time: Long           = 0
    private val s                    = "pool.ntp.org"


    override fun doWork(): Result {
        try {
            val ntpClient            = NTPUDPClient()
            val address  = InetAddress.getByName(s)
            val timeInfo = ntpClient.getTime(address)
            time = timeInfo.message.transmitTimeStamp.time
            offset = System.currentTimeMillis() - time
            return Result.success()
        } catch (e: SocketException) { Log.d("offset","failed. retrying")}
        return Result.failure()
    }
}