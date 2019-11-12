package com.wesync.ui.main

import androidx.lifecycle.*
import com.wesync.util.Tempo

class MetronomeViewModel : ViewModel() {


    private val _isPlaying              = MutableLiveData<Boolean>( false)
    private val _bpm                    = MutableLiveData<Long>(Tempo.DEFAULT_BPM)
    val isPlaying: LiveData<Boolean>     = _isPlaying


    val bpm: LiveData<Long>
        get() = _bpm

    fun onPlayClicked() {
        val p = _isPlaying.value
        _isPlaying.value = !p!!
    }

    fun modifyBPM(plus:Long) {
        val r = _bpm.value!!

        when {
            r+plus < Tempo.MINIMUM_BPM -> _bpm.value = Tempo.MINIMUM_BPM
            r+plus > Tempo.MAXIMUM_BPM -> _bpm.value = Tempo.MAXIMUM_BPM
            else -> _bpm.value = r + plus
        }
    }

}


