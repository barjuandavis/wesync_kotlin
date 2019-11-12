package com.wesync.ui.main

import android.util.Log
import androidx.arch.core.util.Function
import androidx.databinding.Bindable
import androidx.lifecycle.*
import com.wesync.ui.UIState
import com.wesync.util.Tempo
import com.wesync.util.UserTypes
import java.lang.NullPointerException

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

    fun getUIState() : UIState {
        return try
        {
            UIState(_isPlaying.value!!, _bpm.value!!)
        } catch (e: NullPointerException) {
            UIState(false, Tempo.DEFAULT_BPM)
        }
    }
}


