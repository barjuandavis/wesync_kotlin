package com.wesync.util

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import com.wesync.SharedViewModel
import kotlinx.android.parcel.Parcelize


class MetronomeCodes {
    companion object {
        const val START_METRONOME = 1001
        const val STOP_METRONOME = 401
        const val ON_BPM_CHANGED = 123
    }
}

class ServiceUtil {
    companion object {
        const val SERVICE_ID = "com.wesync"
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
        b.putLong(SharedViewModel.BPM_KEY,bpm)
        b.putBoolean(SharedViewModel.USER_TYPE_KEY,isPlaying)
        b.putString(SharedViewModel.SESSION_KEY,session)
        b.putString(SharedViewModel.USER_TYPE_KEY,userTypeString)
        return b
    }
}

class TestMode {
    companion object {
        const val NEARBY_OFF = false
        const val NEARBY_ON = true
        const val STATUS = NEARBY_OFF
    }
}


