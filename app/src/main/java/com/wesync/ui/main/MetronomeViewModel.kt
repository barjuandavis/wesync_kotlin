package com.wesync.ui.main

import android.util.Log
import androidx.lifecycle.*


class MetronomeViewModel : ViewModel() {
    private var _bpm = MutableLiveData<Long>()
    private var _isPlaying = MutableLiveData<Boolean>()
    var isPlaying : LiveData<Boolean> = _isPlaying
    var bpm : LiveData<Long> = _bpm

    fun onPlayClicked() {
        if (_isPlaying.value == null) _isPlaying.value = false
        Log.d("expected","Should be playing on " + bpm.value)
        val p = _isPlaying.value
        _isPlaying.value = !p!!
    }
}
