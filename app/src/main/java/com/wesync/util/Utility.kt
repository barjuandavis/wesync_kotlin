package com.wesync.util


class MetronomeCodes {
    companion object {
        val START_METRONOME = 1001
        val STOP_METRONOME = (401)
        val ON_BPM_CHANGED = (123)
    }
}

enum class ConnectionCodes(val v:Int){
    NEW_SESSION(1), JOIN_SESSION(10)
}

enum class UserTypes {
    SESSION_HOST, SLAVE, SOLO
}

class Config {
    companion object {
        val MINIMUM_BPM: Long = 40
        val MAXIMUM_BPM: Long = 300
        val DEFAULT_BPM: Long = 120
    }
}


