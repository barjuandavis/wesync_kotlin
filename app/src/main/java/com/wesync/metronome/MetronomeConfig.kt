package com.wesync.metronome

import androidx.lifecycle.Observer

class MetronomeConfig(bpm: Long) {
    private var _bpm: Long = bpm
    val bpm = _bpm
    var observer = Observer<Long> {

    }
}

enum class MetronomeCodes(val v: Int) {
    START_METRONOME(100),STOP_METRONOME(401),ON_BPM_CHANGED(123)
}

