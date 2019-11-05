package com.wesync.ui.main

import android.util.Log
import androidx.arch.core.util.Function
import androidx.databinding.Bindable
import androidx.lifecycle.*
import com.wesync.util.Tempo
import com.wesync.util.UserTypes

class MetronomeViewModel : ViewModel() {

    private val session                = MutableLiveData<String>("MusicDirector")
    private val userType               = MutableLiveData<UserTypes>(UserTypes.SOLO)
    private val _isPlaying              = MutableLiveData<Boolean>( false)
    private val _bpm                    = MutableLiveData<Long>(Tempo.DEFAULT_BPM)
    val isPlaying: LiveData<Boolean>     = _isPlaying



    val bpm: LiveData<Long>
        get() = _bpm

    fun onPlayClicked() {
        val p = _isPlaying.value
        _isPlaying.value = !p!!
    }

    fun onNewSessionClicked(sessionName: String?) {
        if (sessionName != null) {
            session.value = sessionName
        } else {
            session.value = "MusicDirector"
        }
        userType.value = UserTypes.SESSION_HOST
    }

    fun onJoinSessionClicked() {
        userType.value = UserTypes.SLAVE
    }

    fun dismissSession() {
        userType.value = UserTypes.SOLO
    }

    fun getSessionName() = session.value

    fun modifyBPM(plus:Long) {
        val r = _bpm.value!!

        when {
            r+plus < Tempo.MINIMUM_BPM -> _bpm.value = Tempo.MINIMUM_BPM
            r+plus > Tempo.MAXIMUM_BPM -> _bpm.value = Tempo.MAXIMUM_BPM
            else -> _bpm.value = r + plus
        }
    }

}


