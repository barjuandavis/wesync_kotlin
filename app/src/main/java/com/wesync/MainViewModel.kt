package com.wesync

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.nearby.connection.Payload
import com.wesync.util.Config
import com.wesync.util.ConnectionStatus
import com.wesync.util.Tempo
import com.wesync.util.UserTypes
import com.wesync.util.service.ServiceSubscriber

class MainViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val SESSION_KEY = "currentSession"
        const val USER_TYPE_KEY = "currentUserType"
        const val BPM_KEY = "currentBPM"
        const val IS_PLAYING_KEY = "currentIsPlaying"
    }

    private val subscriber = ServiceSubscriber(this.getApplication(),null)

    // Viewmodel lifecycle management
    init {
        subscriber.subscribe()
    }

    override fun onCleared() {
        subscriber.unsubscribe()
        super.onCleared()
    }


    private val _bpm                                   = MutableLiveData<Long>(Tempo.DEFAULT_BPM)
        val bpm         :LiveData<Long>                    = _bpm
    private val _isPlaying                             = MutableLiveData<Boolean>(false)
        val isPlaying   :LiveData<Boolean>                 = _isPlaying
    private val _session                               = MutableLiveData<String>("MusicDirector")
        val session     :LiveData<String>                  = _session
    private val _userType                              = MutableLiveData<String>(UserTypes.SOLO)
        val userType    :LiveData<String>                  = _userType
        val connectionStatus                               = MutableLiveData<Int>() //TODO: OBSERVE FROM INTERNAL SERVICE
        val payload                                        = MutableLiveData<Payload>()
        val connectedEndpointId                            = MutableLiveData<String>(null)
    private val _isAdvertising                         = MutableLiveData<Boolean>(false)
        val isAdvertising: LiveData<Boolean> = _isAdvertising
    private val _isDiscovering           = MutableLiveData<Boolean>(false)
        val isDiscovering: LiveData<Boolean> = _isAdvertising


    private fun Long.setBPM() {
        //state.set(BPM_KEY, this)
        _bpm.value = this
        subscriber.metronomeService?.setBPM(this)
    }
    private fun setIsPlaying(b: Boolean) {
        _isPlaying.value = b
        if (b) subscriber.metronomeService?.play()
        else subscriber.metronomeService?.stop()
    }
    private fun setSession(sessionName: String?) {
            if (sessionName != null && sessionName.isNotEmpty()) {
                //state.set(SESSION_KEY,sessionName)
                _session.value = sessionName
            } else {
                _session.value = "MusicDirector"
            }
        }
    private fun setUserType(userTypes: String?) {
        //state.set(USER_TYPE_KEY,userTypes)
        if (userTypes!= null) _userType.value = userTypes
        else _userType.value = UserTypes.SOLO
    }
    private fun setIsAdvertising(a: Boolean) {
        _isAdvertising.value = a
        if (a) subscriber.connectionService?.startAdvertising(_session.value)
        else subscriber.connectionService?.stopAdvertising()
    }

    fun onNewSession(sessionName: String?) {
        setSession(sessionName)
        setUserType(UserTypes.SESSION_HOST)
    }
    fun onJoinSession(sessionName: String?) {
        setSession(sessionName)
        setUserType(UserTypes.SLAVE)
    }
    fun endSession() {setUserType(UserTypes.SOLO) }

    fun toggleAdvertise() {
        val p = _isAdvertising.value
        setIsAdvertising(!p!!)
    }
    fun flipIsPlaying() {
        //state.set(IS_PLAYING_KEY, !isPlaying.value!!)
        setIsPlaying(!_isPlaying.value!!)
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
        connectionStatus.value = ConnectionStatus.DISCONNECTED
        connectedEndpointId.value = null
    }

    fun getSessionName(): String {
        return _session.value!!
    }

    fun printValues() {
        Log.d("states","bpm::${bpm.value}")
        Log.d("states","isPlaying:${isPlaying.value}")
        Log.d("states","session: ${session.value}")
        Log.d("states","userType: ${userType.value}")
    }

}
