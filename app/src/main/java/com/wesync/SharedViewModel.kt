package com.wesync

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wesync.util.Config
import com.wesync.util.Tempo
import com.wesync.util.UserTypes

class SharedViewModel: ViewModel() {
    companion object {
        const val SESSION_KEY = "currentSession"
        const val USER_TYPE_KEY = "currentUserType"
        const val BPM_KEY = "currentBPM"
        const val IS_PLAYING_KEY = "currentIsPlaying"
    }
    private val _bpm                                   = MutableLiveData<Long>()
        val bpm         :LiveData<Long>                    = _bpm
    private val _isPlaying                             = MutableLiveData<Boolean>()
        val isPlaying   :LiveData<Boolean>                 = _isPlaying
    private val _session                               = MutableLiveData<String>()
        val session     :LiveData<String>                  = _session
    private val _userType                              = MutableLiveData<String>()
    val userType    :LiveData<String>                      = _userType


    private val _isAdvertising           = MutableLiveData<Boolean>(false)
        val isAdvertising: LiveData<Boolean> = _isAdvertising

    private val _isDiscovering           = MutableLiveData<Boolean>(false)
        val isDiscovering: LiveData<Boolean> = _isAdvertising

    private fun Long.setBPM() {
        //state.set(BPM_KEY, this)
        _bpm.value = this
    }
    private fun flipIsPlaying() {
        //state.set(IS_PLAYING_KEY, !isPlaying.value!!)
        _isPlaying.value = !_isPlaying.value!!
    }

    private fun setIsPlaying(b: Boolean) {
        _isPlaying.value = b
    }

    private fun setSession(sessionName: String?) {
            if (sessionName != null && sessionName.isNotEmpty()) {
                //state.set(SESSION_KEY,sessionName)
                _session.value = sessionName
            }
        }
    private fun setUserType(userTypes: String?) {
        //state.set(USER_TYPE_KEY,userTypes)
        if (userTypes!= null) _userType.value = userTypes
        else _userType.value = UserTypes.SOLO
    }


    fun onJoinSession(sessionName: String?) {
        setUserType(UserTypes.SLAVE)
    }
    fun endSession() {setUserType(UserTypes.SOLO) }
    fun onNewSession(sessionName: String?) {
        setSession(sessionName)
        setUserType(UserTypes.SESSION_HOST)
    }

    fun toggleAdvertise() {
        val p = _isAdvertising.value
        _isAdvertising.value = !p!!
    }

    fun onPlayClicked() {
        flipIsPlaying()
    }

    fun modifyBPM(plus:Long) {
        val r = bpm.value!!
        when {
            r+plus < Tempo.MINIMUM_BPM -> Tempo.MINIMUM_BPM.setBPM()
            r+plus > Tempo.MAXIMUM_BPM -> Tempo.MAXIMUM_BPM.setBPM()
            else -> (r + plus).setBPM()
        }
    }

    fun getConfig(): Config {
        return Config(
            bpm.value!!,
            isPlaying.value!!,
            session.value!!,
            userType.value!!
        )
    }

    fun unpackBundle(b: Bundle?) {
        if (b != null) {
            b.getLong(BPM_KEY).setBPM()
            setIsPlaying(b.getBoolean(IS_PLAYING_KEY))
            setSession(b.getString(SESSION_KEY))
            setUserType(b.getString(USER_TYPE_KEY))
        } else {
            Log.d("states","Bundle was null. resetting.")
            reset()
        }
    }

    private fun reset(){
        Tempo.DEFAULT_BPM.setBPM()
        setIsPlaying(false)
        setSession("MusicDirector")
        setUserType(UserTypes.SOLO)
    }


    fun getSessionName(): String {
        return session.value!!
    }

    fun printValues() {
        Log.d("states","bpm::${bpm.value}")
        Log.d("states","isPlaying:${isPlaying.value}")
        Log.d("states","session: ${session.value}")
        Log.d("states","userType: ${userType.value}")
    }

}
