package com.wesync.ui.main

import androidx.databinding.Bindable
import androidx.lifecycle.*

class MetronomeViewModel : ViewModel() {

    private val _session                = MutableLiveData<String>("MusicDirector")
    private val _isSessionHost          = MutableLiveData<Boolean>(false)
    private val _isPlaying              = MutableLiveData<Boolean>( false)
    private val _bpm                    = MutableLiveData<Long>(120)
    val isPlaying: LiveData<Boolean>     = _isPlaying
    val isSessionHost: LiveData<Boolean> = _isSessionHost
    val session: LiveData<String>        = _session
    val bpm: LiveData<Long> = _bpm


    fun onPlayClicked() {
        val p = _isPlaying.value
        _isPlaying.value = !p!!
    }

    fun modifyBPM(plus:Long) {
        val r = _bpm.value!!
        when {
            r+plus < 40 -> _bpm.value = 40
            r+plus > 300 -> _bpm.value = 300
            else -> _bpm.value = r + plus
        }
    }

}


