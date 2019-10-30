package com.wesync.util

class MetronomeConfig(val bpm: Long)

enum class MetronomeCodes(val v: Int) {
    START_METRONOME(100),STOP_METRONOME(401),ON_BPM_CHANGED(123)
}

enum class ConnectionCodes(val v:Int){
    NEW_SESSION(1), JOIN_SESSION(10)
}

enum class UserTypes {
    SESSION_HOST, SLAVE, SOLO
}
