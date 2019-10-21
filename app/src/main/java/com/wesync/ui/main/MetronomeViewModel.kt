package com.wesync.ui.main

import android.util.Log
import androidx.databinding.Bindable
import androidx.lifecycle.*


class MetronomeViewModel : ViewModel() {



    private val _isPlaying              = MutableLiveData<Boolean>()

    private val _bpm                    = MutableLiveData<Long>(120)
    val isPlaying: LiveData<Boolean>    = _isPlaying
    val bpm: LiveData<Long> = _bpm


    fun onPlayClicked() {
        if (_isPlaying.value == null)
            _isPlaying.value = false
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


