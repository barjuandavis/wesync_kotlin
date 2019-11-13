package com.wesync

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.wesync.util.Tempo
import com.wesync.util.UserTypes

class SharedViewModel(private val state: SavedStateHandle): ViewModel() {

    companion object {
        private const val SESSION_KEY = "currentSession"
        private const val USER_TYPE_KEY = "currentUserType"
        private const val BPM_KEY = "currentBPM"
        private const val IS_PLAYING_KEY = "currentIsPlaying"
    }

        val bpm         :LiveData<Long>                           = getBPM()
        val isPlaying   :LiveData<Boolean>                        = getIsPlaying()
        val session     :LiveData<String>                         = getSession()
        val userType    : LiveData<UserTypes>                     = getUserType()

    private val _isAdvertising           = MutableLiveData<Boolean>(false)
        val isAdvertising: LiveData<Boolean> = _isAdvertising

    private val _isDiscovering           = MutableLiveData<Boolean>(false)
        val isDiscovering: LiveData<Boolean> = _isAdvertising


    //state getters
        private fun getBPM()        :LiveData<Long> = state.getLiveData(BPM_KEY,Tempo.DEFAULT_BPM)
        private fun Long.setBPM() { state.set(BPM_KEY, this) }

        private fun getIsPlaying():LiveData<Boolean> = state.getLiveData(IS_PLAYING_KEY,false)
        private fun flipIsPlaying() { state.set(IS_PLAYING_KEY, !getIsPlaying().value!!) }

        private fun getSession(): MutableLiveData<String> = state.getLiveData(SESSION_KEY,"MusicDirector")
        private fun setSession(sessionName: String?) {
            if (sessionName != null && sessionName.isNotEmpty()) {
                state.set(SESSION_KEY,sessionName)
            }
        }

        private fun getUserType(): MutableLiveData<UserTypes> = state.getLiveData(USER_TYPE_KEY,UserTypes.SOLO)
        private fun setUserType(userTypes: UserTypes) { state.set(USER_TYPE_KEY,userTypes) }


        fun onJoinSession() {
            setUserType(UserTypes.SLAVE)
        }

        fun endSession() {
            setUserType(UserTypes.SOLO)
        }

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

    fun getSessionName(): String? {
        return session.value
    }

}
