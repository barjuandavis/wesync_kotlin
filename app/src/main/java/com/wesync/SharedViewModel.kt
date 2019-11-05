package com.wesync

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wesync.util.UserTypes

class SharedViewModel: ViewModel() {

    val config                           = MutableLiveData<Long>()
    private val _session                 = MutableLiveData<String>("MusicDirector")
    private val _userType                = MutableLiveData<UserTypes>(UserTypes.SOLO)
    private val _isAdvertising           = MutableLiveData<Boolean>(false)
        val isAdvertising: LiveData<Boolean> = _isAdvertising
        val userTypes: LiveData<UserTypes>  = _userType


    fun onJoinSession() {
        _userType.value = UserTypes.SLAVE
    }

    fun endSession() {
        _userType.value = UserTypes.SOLO
    }

    fun onNewSession(sessionName: String?) {
        if (sessionName != null && sessionName.isNotEmpty()) {
            _session.value = sessionName
        } else {
            _session.value = "MusicDirector"
        }
        _userType.value = UserTypes.SESSION_HOST
    }

    fun toggleAdvertise() {
        val p = _isAdvertising.value
        _isAdvertising.value = !p!!
    }

    fun getSessionName() = _session.value
}