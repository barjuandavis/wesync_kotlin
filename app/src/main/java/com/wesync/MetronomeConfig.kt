package com.wesync

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

object MetronomeConfig {
    private lateinit var _bpm: MutableLiveData<Int>
        var bpm: LiveData<Int> = this._bpm
    private lateinit var _beats: MutableLiveData<Int>
    private lateinit var _denom: MutableLiveData<Int>

    init {
        _bpm.value = 120
        _beats.value = 4
        _denom.value = 4
    }

}

class ConfigObserver: Observer<Int> {
    override fun onChanged(t: Int?) {

    }
}

