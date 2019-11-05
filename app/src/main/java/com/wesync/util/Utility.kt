package com.wesync.util


class MetronomeCodes {
    companion object {
        const val START_METRONOME = 1001
        const val STOP_METRONOME = 401
        const val ON_BPM_CHANGED = 123
    }
}

enum class ConnectionCodes(val v:Int){
    NEW_SESSION(1), JOIN_SESSION(10)
}

enum class UserTypes {
    SESSION_HOST, SLAVE, SOLO
}

class Tempo {
    companion object {
        const val MINIMUM_BPM: Long = 20
        const val MAXIMUM_BPM: Long = 300
        const val DEFAULT_BPM: Long = 120
        const val OFFSET_IN_MILLIS: Long = 5
    }
}


