package com.wesync

import androidx.lifecycle.Observer

class MetronomeConfig(bpm: Long) {
    private var _bpm: Long = bpm
    val bpm = _bpm
    var observer = Observer<Long> {

    }

}

