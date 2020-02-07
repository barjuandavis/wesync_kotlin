package com.wesync.util

import android.os.Bundle
import android.os.Parcelable
import com.wesync.MainViewModel
import kotlinx.android.parcel.Parcelize


class MetronomeCodes {
    companion object {
        const val START_METRONOME = 1001
        const val STOP_METRONOME = 401
        const val ON_BPM_CHANGED = 123
        const val TICK = 222
    }
}

class ServiceUtil {
    companion object {
        const val SERVICE_ID = "com.wesync"
    }
}

class NTPUtil {
    companion object {
        const val OFFSET = "OFFSET"
    }
}

enum class ConnectionCodes(val v:Int){
    NEW_SESSION(1), JOIN_SESSION(10)
}

class UserTypes {
    companion object  {
        const val SESSION_HOST = "USER_TYPE_SESSION_HOST"
        const val SLAVE = "USER_TYPE_SLAVE"
        const val SOLO = "USER_TYPE_SOLO"
    }
}

class ConnectionStatus {
    companion object  {
        const val DISCONNECTED = 0
        const val CONNECTING = 2
        const val CONNECTED = 4
    }
}

class Tempo {
    companion object {
        const val MINIMUM_BPM: Long = 20
        const val MAXIMUM_BPM: Long = 400
        const val DEFAULT_BPM: Long = 120
        const val OFFSET_IN_MILLIS: Long = 5
    }
}

@Parcelize
data class Config(
    val bpm: Long,
    val isPlaying: Boolean,
    val session: String,
    val userTypeString: String): Parcelable {
    fun getBundle(): Bundle {
        val b = Bundle()
        b.putLong(MainViewModel.BPM_KEY,bpm)
        b.putBoolean(MainViewModel.USER_TYPE_KEY,isPlaying)
        b.putString(MainViewModel.SESSION_KEY,session)
        b.putString(MainViewModel.USER_TYPE_KEY,userTypeString)
        return b
    }
}

class TestMode {
    companion object {
        const val NEARBY_OFF = false
        const val NEARBY_ON = true
        const val STATUS = NEARBY_ON
        const val PRE_START_TEST = 2 //0 = no pre start latency, 1: counted every time user is connected 2: counted every time config is changed
    }
}

class PayloadType { //ByteArray[0] adalah PAYLOADTYPE.
    companion object {
        const val CONFIG: Byte = 0 // onConfigChange. Dikirim setiap config berubah.
        const val PING: Byte = 1 // Dikirim dari HOST ke SLAVE. Untuk hitung latency ke semua slave.
        const val PING_RESPONSE: Byte = 2 // dikirim dari SLAVE ke HOST sebagai balasan dari PayloadType.PING
        const val PING_PRE_START_LATENCY: Byte = 3 // dikirim dari HOST ke SLAVE setelah HOST tahu latency dari semua user.
                                                    // Slot timestamp isinya adalah berapa offset yang SLAVE harus tambah ke preStartLatency di MetronomeService.
        const val PING_EXP: Byte = 4
        const val PING_RESPONSE_EXP: Byte = 5
    }
}

class PayloadSizes {
    companion object {
        const val CONFIG = 4
        const val PING = 3
        const val PING_RESPONSE = 3
        const val PING_PRE_START_LATENCY = 3
        const val SIZE_OF_TEST = 1000
    }
}


