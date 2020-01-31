package com.wesync

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.nearby.connection.Payload
import com.wesync.connection.ConnectionManagerService
import com.wesync.connection.DiscoveredEndpoint
import com.wesync.util.*
import com.wesync.util.service.ServiceSubscriber

class MainViewModel(application: Application) : AndroidViewModel(application), LifecycleObserver {
    companion object {
        const val SESSION_KEY = "currentSession"
        const val USER_TYPE_KEY = "currentUserType"
        const val BPM_KEY = "currentBPM"
        const val IS_PLAYING_KEY = "currentIsPlaying"
    }


    val subscriber = ServiceSubscriber(this.getApplication(),null)
    private var mCService: ConnectionManagerService? = null

    // Viewmodel lifecycle management
    init {
        subscriber.subscribe()
        subscriber.connServiceConnected.observeForever {
            if (it) mCService = subscriber.connectionService
            observeService()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun subscribe() {
        subscriber.subscribe()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun unsubscribe() {
        Log.d("_con","unsubscribe called onPause")
        subscriber.unsubscribe()
    }


    private val _bpm                                   = MutableLiveData<Long>(Tempo.DEFAULT_BPM)
        val bpm         :LiveData<Long>                    = _bpm
    private val _isPlaying                             = MutableLiveData<Boolean>(false)
        val isPlaying   :LiveData<Boolean>                 = _isPlaying

    private val _userName                               = MutableLiveData<String>("MusicDirector")
        val userName     :LiveData<String>                 = _userName
    private val _userType                              = MutableLiveData<String>(UserTypes.SOLO)
        val userType    :LiveData<String>                  = _userType

    private val _connectionStatus                      = MutableLiveData<Int>()
    private val _connectedEndpointId                    = MutableLiveData<String>(null)
        val connectedEndpointId: LiveData<String>       =_connectedEndpointId
    private val payload                                = MutableLiveData<Payload>()
    private val preStartLatency                        = MutableLiveData<Long>(0)
    private val _ntpOffset                                 = MutableLiveData<Long>(0)
        val ntpOffset: LiveData<Long> =_ntpOffset



    private val _isAdvertising                         = MutableLiveData<Boolean>(false)
        val isAdvertising: LiveData<Boolean> = _isAdvertising
    private val _isDiscovering           = MutableLiveData<Boolean>(false)
        val isDiscovering: LiveData<Boolean> = _isDiscovering

    private val _foundSessions = MutableLiveData<MutableList<DiscoveredEndpoint>>()
        val foundSessions: LiveData<MutableList<DiscoveredEndpoint>> = _foundSessions
    var currentFragment = MutableLiveData(0)



    private fun observeService() {
        subscriber.connectionService?.foundSessions?.observeForever { _foundSessions.value = it}
        subscriber.connectionService?.payload?.observeForever{
            payload.value = it
            unpackConfigPayload(it)
        }
        subscriber.connectionService?.connectionStatus?.observeForever {
           _connectionStatus.value = it
           if (it == ConnectionStatus.DISCONNECTED) {
               endSession()
           }
        }
        subscriber.connectionService?.connectedEndpointId?.observeForever {
            _connectedEndpointId.value = it
        }
        subscriber.connectionService?.preStartLatency?.observeForever {
            subscriber.metronomeService?.preStartLatency = it
        }
        subscriber.connectionService?.isDiscovering?.observeForever {
            _isDiscovering.value = it
        }
    }

    // a callback which always be called when BPM or isPlaying is changing.
    // it is used to inform mCService to send payload for every time changes at mService happens.
    private fun onConfigChanged() {
        if (userType.value == UserTypes.SESSION_HOST) {
            if (TestMode.PRE_START_TEST == 2) mCService?.sendTimestampedByteArray(type = PayloadType.PING)
            mCService?.sendByteArrayToAll(ByteArrayEncoderDecoder
                .encodeConfigByteArray(_bpm.value!!,_isPlaying.value!!))
        }
    }

    private fun connect(e: DiscoveredEndpoint) { mCService?.connect(e,_userName.value!!)}
    private fun disconnect() {mCService?.disconnect()}



    private fun Long.setBPM() {
        onConfigChanged()
        _bpm.value = this
        subscriber.metronomeService?.setBPM(this)
    }

    private fun setIsPlaying(b: Boolean) {
        onConfigChanged()
        _isPlaying.value = b
        if (b)  {
            subscriber.metronomeService?.play() //KASIH FAIL SAFE?
            if (userType.value!! == UserTypes.SESSION_HOST && isAdvertising.value!!) {
                setIsAdvertising(false)
            }
        }
        else subscriber.metronomeService?.stop()
    }
    private fun setUserName(sessionName: String?) {
        if (sessionName != null && sessionName.isNotEmpty()) {
            //state.set(SESSION_KEY,sessionName)
            _userName.value = sessionName
        } else
        { _userName.value = "MusicDirector" }
        mCService?.userName = _userName.value!!

    }
    private fun setUserType(userTypes: String?) {
        //state.set(USER_TYPE_KEY,userTypes)
        if (userTypes!= null) _userType.value = userTypes
        else _userType.value = UserTypes.SOLO
        mCService?.userType = _userType.value!!
    }
    private fun setIsDiscovering(a: Boolean){
        if (a) {
            subscriber.connectionService?.startDiscovery()
        }
        else {
            subscriber.connectionService?.stopDiscovery()
        }
    }
    private fun reset(){
        Tempo.DEFAULT_BPM.setBPM()
        setIsPlaying(false)
        setUserName("MusicDirector")
        setUserType(UserTypes.SOLO)
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
        _connectedEndpointId.value = null
    }
    private fun unpackConfigPayload(p: Payload) {
        val b = p.asBytes()!!
        if (b[0] == PayloadType.CONFIG) {
            val pair: Pair<Long, Boolean> = ByteArrayEncoderDecoder.decodeConfig(b)
            setIsPlaying(pair.second)
            pair.first.setBPM()
        } else if (b[0] == PayloadType.PING_EXP) {

        }
    }


    fun setOffset(offset: Long) {
        this._ntpOffset.value = offset
        mCService?.ntpOffset = offset
    }
    fun onNewSession(sessionName: String?) {
        setUserName(sessionName)
        setUserType(UserTypes.SESSION_HOST)
        if (!_isAdvertising.value!!) setIsAdvertising(true)
    }
    fun setIsAdvertising(a: Boolean) {
        _isAdvertising.value = a
        if (a) mCService?.startAdvertising()
        else mCService?.stopAdvertising()
    }
    fun onJoinSession(yourName: String?, it: DiscoveredEndpoint) {
        setUserName(yourName)
        _connectionStatus.value = ConnectionStatus.CONNECTING
        setUserType(UserTypes.SLAVE)
        connect(it)
    }
    fun endSession() {
        setUserType(UserTypes.SOLO)
        disconnect()
    }
    fun toggleAdvertise() {
        val p = _isAdvertising.value
        setIsAdvertising(!p!!)
    }
    fun startDiscovery() {
        setIsDiscovering(true)
    }
    fun stopDiscovery() {
        setIsDiscovering(false)
    }
    fun flipIsPlaying() {
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
            userName.value!!,
            userType.value!!
        )
    }
    fun unpackBundle(b: Bundle?) {
        if (b != null) {
            b.getLong(BPM_KEY).setBPM()
            setIsPlaying(b.getBoolean(IS_PLAYING_KEY))
            setUserName(b.getString(SESSION_KEY))
            setUserType(b.getString(USER_TYPE_KEY))
        } else {
            Log.d("states","Bundle was null. resetting.")
            reset()
        }
    }
    fun getSessionName(): String {
        return _userName.value!!
    }
    fun getConnectionStatus(): Int {
        return _connectionStatus.value!!
    }

    fun printValues() {
        Log.d("states","bpm::${bpm.value}")
        Log.d("states","isPlaying:${isPlaying.value}")
        Log.d("states","session: ${userName.value}")
        Log.d("states","userType: ${userType.value}")
    }



}
