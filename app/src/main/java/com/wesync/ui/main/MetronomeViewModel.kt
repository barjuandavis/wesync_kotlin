package com.wesync.ui.main

import android.util.Log
import androidx.databinding.Bindable
import androidx.lifecycle.*
import com.wesync.util.UserTypes

class MetronomeViewModel : ViewModel() {

    private val _session                = MutableLiveData<String>("MusicDirector")
    private val _userType               = MutableLiveData<UserTypes>(UserTypes.SOLO)
    private val _isPlaying              = MutableLiveData<Boolean>( false)
    private val _bpm                    = MutableLiveData<Long>(120)
    val isPlaying: LiveData<Boolean>     = _isPlaying
    val userType: LiveData<UserTypes> = _userType
    val session: LiveData<String>        = _session

    val bpm: LiveData<Long>
        get() = _bpm




    fun onPlayClicked() {
        val p = _isPlaying.value
        _isPlaying.value = !p!!
    }

    fun onNewSessionClicked() {
        //TODO: replace new session with "Dismiss session(?)"
        _userType.value = UserTypes.SESSION_HOST
    }

    fun onJoinSessionClicked() {
        _userType.value = UserTypes.SLAVE
    }

    fun modifyBPM(plus:Long) {
        val r = _bpm.value!!

        when {
            r+plus < 20 -> _bpm.value = 20
            r+plus > 400 -> _bpm.value = 400
            else -> _bpm.value = r + plus
        }
    }

}


