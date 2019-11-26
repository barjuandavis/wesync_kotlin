package com.wesync.ntp

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.wesync.util.NTPUtil
import org.apache.commons.net.ntp.NTPUDPClient
import java.net.InetAddress
import java.net.SocketException

class NTPClientWorker(val context: Context,val params: WorkerParameters)
    : Worker(context, params) {
    var offset: Long         = 0
    var time: Long           = 0
    private val s                    = "pool.ntp.org"

    override fun doWork(): Result {
        try {
            val ntpClient            = NTPUDPClient()
            val address  = InetAddress.getByName(s)
            val timeInfo = ntpClient.getTime(address)
            time = timeInfo.message.transmitTimeStamp.time
            offset = System.currentTimeMillis() - time
            val out = workDataOf(NTPUtil.OFFSET to offset)
            return Result.success(out)
        } catch (e: SocketException) { Log.d("offset","failed. retrying")}
        return Result.failure()
    }
}